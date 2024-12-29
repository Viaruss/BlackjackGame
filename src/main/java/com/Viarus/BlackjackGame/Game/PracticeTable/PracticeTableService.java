package com.Viarus.BlackjackGame.Game.PracticeTable;

import com.Viarus.BlackjackGame.Cards.Hand;
import com.Viarus.BlackjackGame.Game.Player.Player;
import com.Viarus.BlackjackGame.Game.Player.PlayerDAO;
import com.Viarus.BlackjackGame.Game.Player.PlayerService;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerActions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerDecisions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerRoundResult;
import com.Viarus.BlackjackGame.Game.PracticeTable.Utils.PracticeGameStateManager;
import com.Viarus.BlackjackGame.Game.Table.Utils.GameState;
import com.Viarus.BlackjackGame.config.GameTimingConfig;
import com.Viarus.BlackjackGame.config.GameplayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.Viarus.BlackjackGame.Game.Table.Utils.GameState.*;

@Slf4j
@Service
public class PracticeTableService {
    private final PracticeTableDAO practiceTableDAO;
    private final PlayerService playerService;
    private final PlayerDAO playerDAO;
    public final GameTimingConfig gameTimingConfig;
    public final PracticeGameStateManager practiceGameStateManager;
    public final GameplayConfig gameplayConfig;

    public PracticeTableService(PracticeTableDAO practiceTableDAO,
                                PlayerService playerService,
                                PlayerDAO playerDAO,
                                PracticeGameStateManager practiceGameStateManager,
                                GameTimingConfig gameTimingConfig,
                                GameplayConfig gameplayConfig) {
        this.practiceTableDAO = practiceTableDAO;
        this.playerService = playerService;
        this.playerDAO = playerDAO;
        this.practiceGameStateManager = practiceGameStateManager;
        this.gameTimingConfig = gameTimingConfig;
        this.gameplayConfig = gameplayConfig;
    }

    public PracticeTable getTable(String tableId) {
        return practiceTableDAO.findById(tableId).orElse(null);
    }

    public PracticeTable joinTable(String playerId) throws Exception {
        Player player = playerService.getPlayerById(playerId);
        if (Objects.equals(player.getCurrentTableId(), null)) {
            PracticeTable table = new PracticeTable(player);
            player.setCurrentTableId(table.getId());
            player.setCurrentAction(PlayerActions.WAITING);
            table.setStateMessage("Preparing Table");
            table.setCountdownTime(gameTimingConfig.initialWaiting);
            playerDAO.save(player);
            practiceTableDAO.save(table);
            practiceGameStateManager.notifyClients(table.getId());
            practiceGameStateManager.scheduleStateChange(table.id, BETTING, gameTimingConfig.initialWaiting);
            return table;
        } else {
            throw new Exception("Player is already at another table");
        }
    }

    public PracticeTable leaveTable(String playerId) {
        Player player = playerService.getPlayerById(playerId);
        PracticeTable table = practiceTableDAO.findById(player.getCurrentTableId()).orElse(null);
        if (table == null) {
            return null;
        }
        practiceGameStateManager.cancelScheduledTask();
        player.leaveTable();

        practiceTableDAO.delete(table);
        playerDAO.save(player);

        return table;
    }

    public PracticeTable placeBet(String tableId, int amount) throws Exception {
        PracticeTable table = getTable(tableId);
        if (table.player.getCurrentAction() == PlayerActions.BETTING) {
            if (table.getGameState() != BETTING) {
                throw new Exception("This table is not in betting state");
            }
            Player player = playerService.placeBet(table.player.getId(), amount);
            player.setCurrentAction(PlayerActions.WAITING);

            table.updatePlayer(player);
            changeGameState(tableId, PLAYING);

            return table;
        } else throw new Exception("This player cannot place bets now");
    }

    public void changeGameState(String tableId, GameState gameState) {
        PracticeTable table = getTable(tableId);
        PracticeTable updatedTable = switch (gameState) {
            case BETTING -> startBetting(table);
            case PLAYING -> startGame(table);
            case CROUPIER_TURN -> croupierTurn(table);
            case ROUND_SUMMARY -> roundResult(table);
            case WAITING_FOR_PLAYERS -> prepareGame(table);
            default -> throw new IllegalStateException("Unexpected value: " + gameState);
        };
        practiceTableDAO.save(updatedTable);
        practiceGameStateManager.notifyClients(table.getId());
    }

    private PracticeTable prepareGame(PracticeTable table) {
        table.setGameState(WAITING_FOR_PLAYERS);
        clearCards(table);
        table.setStateMessage("Waiting for next round");
        table.setCountdownTime(gameTimingConfig.postGameWaiting);
        practiceGameStateManager.scheduleStateChange(table.getId(), BETTING, gameTimingConfig.initialWaiting);
        return table;
    }

    public PracticeTable startBetting(PracticeTable table) {
        if (table.getGameState() == WAITING_FOR_PLAYERS) {
            table.setGameState(BETTING);
            table.setStateMessage("");
            table.setCountdownTime(0);
            Player player = table.player;
            player.setCurrentAction(PlayerActions.BETTING);
            table.updatePlayer(player);
            playerService.save(player);
        }
        return table;
    }

    public PracticeTable startGame(PracticeTable table) {
        if (table.getGameState() != BETTING) {
            return table;
        } else if (table.player.getBet() == 0) {
            System.out.println("No bets placed");
            table.setStateMessage("Waiting for bets");
            table.setCountdownTime(gameTimingConfig.betting);
            practiceGameStateManager.scheduleStateChange(table.getId(), PLAYING, gameTimingConfig.betting);
            return table;
        }
        table.setGameState(PLAYING);
        clearCards(table);

        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard().hide());
        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());

        table.player.setPlaying(true);
        for (int i = 0; i < 2; i++) {
            table.player.getHand().addCard(table.getCardsInPlay().dealCard());
        }

        table.setTurnNumber(0);

        table.player.setCurrentAction(PlayerActions.DECIDING);
        table.player.evaluatePossibleDecisions();
        playerService.save(table.player);

        table.setCountdownTime(gameTimingConfig.turnDelay);
        table.setStateMessage("Your turn");
        return table;
    }

    private void clearCards(PracticeTable table) {
        table.getCroupier().setHand(new Hand());
        table.player.setHand(new Hand());
    }

    public PracticeTable croupierTurn(PracticeTable table) {
        if (table.getGameState() != PLAYING) {
            return null;
        }
        table.setGameState(CROUPIER_TURN);

        table.getCroupier().showCards();
        while (table.getCroupier().getHand().value < gameplayConfig.getCroupierLimit()) {
            table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());
        }
        table.setStateMessage("Waiting for summary");
        table.setCountdownTime(gameTimingConfig.summaryDelay);
        practiceGameStateManager.scheduleStateChange(table.getId(), ROUND_SUMMARY, gameTimingConfig.summaryDelay);
        return table;
    }

    public PracticeTable processPlayerDecision(Player player, PracticeTable table, PlayerDecisions decision) throws Exception {
        if (player.getHand().isOver || table.getGameState() != PLAYING) {
            throw new Exception("This player is not allowed to make a move now");
        } else {
            if (!player.getAvailableDecisions().contains(decision)) {
                throw new Exception("Player decision is invalid");
            }
            switch (decision) {
                case HIT -> {
                    player.getHand().addCard(table.getCardsInPlay().dealCard());
                    if (player.getHand().isOver) {
                        player.setCurrentAction(PlayerActions.WAITING);
                        nextTurn(table);
                    } else {
                        table.setCountdownTime(gameTimingConfig.turnDelay);
                        table.setStateMessage("%s's turn".formatted(player.getName()));
                        practiceGameStateManager.scheduleStateChange(table.getId(), NEXT_TURN, gameTimingConfig.turnDelay);
                    }
                }
                case STAND -> {
                    player.setCurrentAction(PlayerActions.WAITING);
                    nextTurn(table);
                }
                case DOUBLE -> {
                    player.placeBet(player.getBet());
                    player.getHand().addCard(table.getCardsInPlay().dealCard());
                    player.setCurrentAction(PlayerActions.WAITING);
                    nextTurn(table);
                }
            }
            player.evaluatePossibleDecisions();
            table.updatePlayer(player);
        }
        practiceGameStateManager.notifyClients(table.getId());

        return table;
    }

    private void nextTurn(PracticeTable table) {
        table.setTurnNumber(table.getTurnNumber() + 1);
        switch (table.getTurnNumber()) {
            case 1:
//                TODO: BOT TURN
                break;

            case 2:
                table.setStateMessage("Croupier Turn");
                table.setCountdownTime(gameTimingConfig.croupierDelay);
                practiceGameStateManager.scheduleStateChange(table.getId(), CROUPIER_TURN, gameTimingConfig.croupierDelay);
                break;

            default:
                table.player.setCurrentAction(PlayerActions.DECIDING);
                playerService.save(table.player);
                practiceTableDAO.save(table);
                table.setCountdownTime(gameTimingConfig.turnDelay);
                table.setStateMessage("Your turn");
                break;
        }
    }

    public PracticeTable roundResult(PracticeTable table) {
        table.setGameState(ROUND_SUMMARY);
        int croupierValue = table.getCroupier().getHand().value;
        System.out.printf("Croupier %d\n", croupierValue);
        for (Player player : List.of(table.player, table.botPlayer)) {
            boolean playerWon = false;
            int playerValue = player.getHand().value;
            boolean playerBlackJack = (playerValue == 21 && player.getHand().cards.size() == 2);
            if ((croupierValue > 21 || (croupierValue < playerValue)) && playerValue <= 21) {
                playerWon = true;
            }
            System.out.println(player.getName() + " " + playerValue + " " + (playerWon ? "won" : "lost"));
            if (playerWon) {
                int playerWinnings;
                if (playerBlackJack) {
                    playerWinnings = player.getBet() * table.getBlackJackMultiplier();
                    player.setLastRoundResult(PlayerRoundResult.BLACKJACK);
                } else {
                    playerWinnings = player.getBet() * 2;
                    player.setLastRoundResult(PlayerRoundResult.WON);
                }
                player.addWinnings(playerWinnings - player.getBet());
                player.addBalance(playerWinnings);
                table.getCroupier().addLosings(playerWinnings);
            } else if (playerValue == croupierValue) {
                player.addBalance(player.getBet());
                player.setLastRoundResult(PlayerRoundResult.DRAW);
            } else {
                player.addLosings(player.getBet());
                table.getCroupier().addWinnings(player.getBet());
                player.setLastRoundResult(PlayerRoundResult.LOST);
            }

            player.setBet(0);

            playerService.save(player);
            table.updatePlayer(player);
        }

        table.setStateMessage("Preparing next round");
        table.setCountdownTime(gameTimingConfig.postGameWaiting);
        practiceGameStateManager.scheduleStateChange(table.getId(), WAITING_FOR_PLAYERS, gameTimingConfig.postGameWaiting);
        return table;
    }
}
