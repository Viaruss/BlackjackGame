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

    public PracticeTable createNewTable() {
        PracticeTable table = new PracticeTable();
        practiceTableDAO.save(table);
        return table;
    }

    public List<PracticeTable> getAllTables() {
        return practiceTableDAO.findAll();
    }

    public PracticeTable joinTable(String tableId, String playerId) throws Exception {
        PracticeTable table = getTable(tableId);
        Player player = playerService.getPlayerById(playerId);
        if (!table.players.contains(player) && Objects.equals(player.getCurrentTableId(), null)) {
            player.setCurrentAction(table.getGameState() == BETTING ? PlayerActions.BETTING : PlayerActions.WAITING);
            table.addPlayer(player);
            player.setCurrentTableId(tableId);
            practiceTableDAO.save(table);
            playerDAO.save(player);
        }
        if (Objects.equals(player.getCurrentTableId(), tableId)) {
            if (table.getPlayers().stream().filter(Objects::nonNull).count() == 1) {
                table.setStateMessage("Preparing Table");
                table.setCountdownTime(gameTimingConfig.initialWaiting);
                practiceTableDAO.save(table);
                practiceGameStateManager.notifyClients(table.getId());
                practiceGameStateManager.scheduleStateChange(tableId, BETTING, gameTimingConfig.initialWaiting);
            }
            return table;
        } else {
            throw new Exception("Player is already at another table");
        }
    }

    public PracticeTable leaveTable(String tableId, String playerId) {
        PracticeTable table = getTable(tableId);
        Player player = playerService.getPlayerById(playerId);

        practiceGameStateManager.cancelScheduledTask();
        table.removePlayer(player);
        player.leaveTable();

        if(table.players.stream().filter(Objects::nonNull).toList().isEmpty()) {
            table.getCroupier().setHand(new Hand());
            table.setGameState(WAITING_FOR_PLAYERS);
        }

        practiceTableDAO.save(table);
        playerDAO.save(player);

        return table;
    }

    public PracticeTable placeBet(String tableId, String playerId, int amount) throws Exception {
        if (playerService.getPlayerById(playerId).getCurrentAction() == PlayerActions.BETTING) {
            PracticeTable table = getTable(tableId);
            if(table.getGameState() != BETTING) {
                throw new Exception("This table is not in betting state");
            }
            Player player = playerService.placeBet(playerId, amount);
            player.setCurrentAction(PlayerActions.WAITING);

            table.updatePlayer(player);
            checkBettingStatus(table);
            practiceTableDAO.save(table);
            return table;
        } else throw new Exception("This player cannot place bets now");
    }

    private void checkBettingStatus(PracticeTable table) {
        List<Player> currentPlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
        int betCount = 0;
        for (Player player : currentPlayers) {
            betCount += player.getBet() == 0 ? 0 : 1;
        }
        if (betCount == 1) {
            table.setStateMessage("Waiting for other bets");
            table.setCountdownTime(gameTimingConfig.betting);
            practiceGameStateManager.scheduleStateChange(table.getId(), PLAYING, gameTimingConfig.betting);
        } else if (betCount == currentPlayers.size()){
            table.setStateMessage("All bets placed");
            table.setCountdownTime(1);
            practiceGameStateManager.scheduleStateChange(table.getId(), PLAYING, 1);
        }
    }

    public void changeGameState(String tableId, GameState gameState) throws Exception {
        PracticeTable table = getTable(tableId);
        PracticeTable updatedTable = switch (gameState) {
            case BETTING -> startBetting(table);
            case PLAYING -> startGame(table);
            case NEXT_TURN -> passTurn(table);
            case CROUPIER_TURN -> croupierTurn(table);
            case ROUND_SUMMARY -> roundResult(table);
            case WAITING_FOR_PLAYERS -> prepareGame(table);
        };
        practiceTableDAO.save(updatedTable);
        practiceGameStateManager.notifyClients(table.getId());
    }

    private PracticeTable prepareGame(PracticeTable table) {
        table.setGameState(WAITING_FOR_PLAYERS);
        clearCards(table);
        table.setStateMessage("Waiting for players");
        table.setCountdownTime(gameTimingConfig.postGameWaiting);
        practiceGameStateManager.scheduleStateChange(table.getId(), BETTING, gameTimingConfig.initialWaiting);
        return table;
    }

    public PracticeTable startBetting(PracticeTable table) {
        if (table.getGameState() == WAITING_FOR_PLAYERS) {
            table.setGameState(BETTING);
            table.setStateMessage("");
            table.setCountdownTime(0);
            List<Player> currentPlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
            for (Player player : currentPlayers) {
                player.setCurrentAction(PlayerActions.BETTING);
                table.updatePlayer(player);
                playerService.save(player);
            }
        }
        return table;
    }

    public PracticeTable startGame(PracticeTable table) {
        if (table.getGameState() != BETTING) {
            return table;
        } else if (table.getPlayers().stream().filter(Objects::nonNull).noneMatch(player -> player.getBet() > 0)) {
            System.out.println("No bets placed");
            table.setStateMessage("Waiting for bets");
            table.setCountdownTime(gameTimingConfig.betting);
            practiceGameStateManager.scheduleStateChange(table.getId(), PLAYING, gameTimingConfig.betting);
            return table;
        }
        table.setGameState(PLAYING);
        clearCards(table);

        List<Player> currentPlayers = table.getPlayers()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard().hide());
        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());

        for (Player player : currentPlayers) {
            player.setPlaying(false);
            if (player.getBet() == 0) {
                player.setCurrentAction(PlayerActions.WAITING);
                continue;
            }
            player.setPlaying(true);
            for (int i = 0; i < 2; i++) {
                player.getHand().addCard(table.getCardsInPlay().dealCard());
            }
        }
        List<Player> availablePlayers = currentPlayers
                .stream()
                .filter(Player::isPlaying)
                .toList();

        table.setTurnNumber(
                table.getPlayers()
                        .stream()
                        .filter(Objects::nonNull)
                        .toList()
                        .indexOf(availablePlayers.get(0))
        );

        availablePlayers.get(0).setCurrentAction(PlayerActions.DECIDING);
        for (Player player : currentPlayers) {
            player.evaluatePossibleDecisions();
            table.updatePlayer(player);
            playerService.save(player);
        }
        table.setCountdownTime(gameTimingConfig.turnDelay);
        table.setStateMessage("%s's turn".formatted(availablePlayers.get(0).getName()));
        practiceGameStateManager.scheduleStateChange(table.getId(), NEXT_TURN, gameTimingConfig.turnDelay);
        return table;
    }

    private PracticeTable passTurn(PracticeTable table) throws Exception {
        processPlayerDecision(table.players.get(table.getTurnNumber()), table, PlayerDecisions.STAND);
        return table;
    }

    private void clearCards(PracticeTable table) {
        table.getCroupier().setHand(new Hand());
        for (Player player : table.getPlayers().stream().filter(Objects::nonNull).toList()) {
            player.setHand(new Hand());
        }
    }

    public PracticeTable croupierTurn(PracticeTable table) {
        if (table.getGameState() != PLAYING) {
            return null;
        }
        table.setGameState(GameState.CROUPIER_TURN);

        table.getCroupier().showCards();
        while (table.getCroupier().getHand().value < gameplayConfig.getCroupierLimit()) {
            table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());
        }
        table.setStateMessage("Waiting for summary");
        table.setCountdownTime(gameTimingConfig.summaryDelay);
        practiceGameStateManager.scheduleStateChange(table.getId(), GameState.ROUND_SUMMARY, gameTimingConfig.summaryDelay);
        return table;
    }

    public PracticeTable processPlayerDecision(Player player, PracticeTable table, PlayerDecisions decision) throws Exception {
        if (table.players.indexOf(player) != table.getTurnNumber() ||
                player.getHand().isOver ||
                table.getGameState() != PLAYING) {
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
        List<Player> availablePlayers = table.getPlayers()
                .stream()
                .filter(Objects::nonNull)
                .toList();
        if (table.getTurnNumber() == availablePlayers.size()) {
            table.setStateMessage("Croupier Turn");
            table.setCountdownTime(gameTimingConfig.croupierDelay);
            practiceGameStateManager.scheduleStateChange(table.getId(), GameState.CROUPIER_TURN, gameTimingConfig.croupierDelay);
        } else if (!availablePlayers.get(table.getTurnNumber()).isPlaying()) {
            nextTurn(table);
        } else {
            Player player = availablePlayers.get(table.getTurnNumber());
            player.setCurrentAction(PlayerActions.DECIDING);
            table.updatePlayer(player);
            playerService.save(player);
            practiceTableDAO.save(table);
            table.setCountdownTime(gameTimingConfig.turnDelay);
            table.setStateMessage("%s's turn".formatted(player.getName()));
            practiceGameStateManager.scheduleStateChange(table.getId(), NEXT_TURN, gameTimingConfig.turnDelay);
        }
    }

    public void save(PracticeTable table) {
        practiceTableDAO.save(table);
    }

    public PracticeTable roundResult(PracticeTable table) {
        table.setGameState(ROUND_SUMMARY);
        int croupierValue = table.getCroupier().getHand().value;
        System.out.printf("Croupier %d\n", croupierValue);
        for (Player player : table.getPlayers().stream().filter(Objects::nonNull).toList()) {
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
