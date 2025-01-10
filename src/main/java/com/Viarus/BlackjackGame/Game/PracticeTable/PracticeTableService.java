package com.Viarus.BlackjackGame.Game.PracticeTable;

import com.Viarus.BlackjackGame.Cards.Card;
import com.Viarus.BlackjackGame.Cards.Hand;
import com.Viarus.BlackjackGame.Game.Player.Player;
import com.Viarus.BlackjackGame.Game.Player.PlayerDAO;
import com.Viarus.BlackjackGame.Game.Player.PlayerService;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerActions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerDecisions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerRoundResult;
import com.Viarus.BlackjackGame.Game.PracticeTable.Utils.BotStrategy;
import com.Viarus.BlackjackGame.Game.PracticeTable.Utils.CardCountingStrategy;
import com.Viarus.BlackjackGame.Game.PracticeTable.Utils.PracticeGameStateManager;
import com.Viarus.BlackjackGame.Game.Table.Utils.GameState;
import com.Viarus.BlackjackGame.config.GameTimingConfig;
import com.Viarus.BlackjackGame.config.GameplayConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static com.Viarus.BlackjackGame.Game.Table.Utils.GameState.*;

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

    public void save(PracticeTable practiceTable) {
        practiceTableDAO.save(practiceTable);
    }

    public PracticeTable joinTable(String playerId) throws Exception {
        Player player = playerService.getPlayerById(playerId);
        PracticeTable table;
        if (player.getCurrentTableId() == null) {
            // TODO: check if any existing table has player with this id
            table = new PracticeTable();
            player.setHand(new Hand());
            table.setPlayer(player);
            player.setCurrentAction(PlayerActions.WAITING);
            table.setStateMessage("Preparing Table");
            table.setCountdownTime(gameTimingConfig.initialWaiting);
            player.setLearningBalance(table.getLearningBalance());
            table.updatePlayer(player);
            practiceTableDAO.save(table);
            player.currentTableId = table.getId();
            playerDAO.save(player);
            practiceGameStateManager.notifyClients(table.getId());
            practiceGameStateManager.scheduleStateChange(table.id, BETTING, gameTimingConfig.initialWaiting);
        } else if (practiceTableDAO.findById(player.getCurrentTableId()).isPresent()) {
            table = practiceTableDAO.findById(player.getCurrentTableId()).get();
        } else {
            System.out.println("error - Player is already at another table");
            throw new Exception("Player is already at another table");
        }
        return table;
    }

    public PracticeTable leaveTable(String tableId) throws NoSuchElementException {
        PracticeTable table = practiceTableDAO.findById(tableId).orElseThrow();
        Player player = table.player;

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
            if(amount <= 0) {
                return table;
            }
            Player player = playerService.placePracticeBet(table.player.getId(), amount);
            player.setCurrentAction(PlayerActions.WAITING);

            table.updatePlayer(player);
            table.botPlayer.placeBet(BotStrategy.calculateBet(table.getTrueValue()));

            changeGameState(tableId, PLAYING);

            practiceTableDAO.save(table);
            playerDAO.save(player);

            return table;
        } else throw new Exception("This player cannot place bets now");
    }

    public void changeGameState(String tableId, GameState gameState) {
        PracticeTable table = getTable(tableId);
        PracticeTable updatedTable = switch (gameState) {
            case BETTING -> startBetting(table);
            case PLAYING -> startGame(table);
            case BOT_TURN -> botTurn(table);
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
        table.botPlayer.getLastDecisions().clear();
        table.setStateMessage("Waiting for next round");
        table.setCountdownTime(gameTimingConfig.initialWaiting);
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
            table.setStateMessage("Waiting for bets");
            table.setCountdownTime(gameTimingConfig.betting);
            practiceGameStateManager.scheduleStateChange(table.getId(), PLAYING, 1);
            return table;
        }
        table.setGameState(PLAYING);
        clearCards(table);
        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard().hide());
        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());
        ArrayList<Card> dealtCards = new ArrayList<>(table.getCroupier().getHand().cards);

        table.player.setPlaying(true);
        for (int i = 0; i < 2; i++) {
            table.player.getHand().addCard(table.getCardsInPlay().dealCard());
        }
        dealtCards.addAll(table.player.getHand().cards);

        table.runningValue = CardCountingStrategy.updateRunningValue(table.runningValue, dealtCards);
        table.setTrueValueAndHouseEdge(CardCountingStrategy.calculateTrueValue(table.runningValue, table.getCardsInPlay().getCardsLeft()));

        table.botPlayer.setHand(table.player.getHand());
        table.setBotCardsInPlay(table.getCardsInPlay());

        table.setTurnNumber(0);

        table.player.setCurrentAction(PlayerActions.DECIDING);
        table.player.evaluatePossibleDecisions();
        playerService.save(table.player);

        table.calculateCardValues();

        table.setCountdownTime(gameTimingConfig.practiceTurnDelay);
        table.setStateMessage("Your turn");
        return table;
    }

    private void clearCards(PracticeTable table) {
        table.getCroupier().setHand(new Hand());
        table.player.setHand(new Hand());
        table.botPlayer.setHand(new Hand());
        table.resetCardValues();
    }

    public PracticeTable croupierTurn(PracticeTable table) {
        if (table.getGameState() != BOT_TURN) {
            return null;
        }
        table.setGameState(CROUPIER_TURN);
        table.getCroupier().showCards();

        ArrayList<Card> dealtCards = new ArrayList<>();
        while (table.getCroupier().getHand().value < gameplayConfig.getCroupierLimit()) {
            Card card = table.getCardsInPlay().dealCard();
            table.getCroupier().getHand().addCard(card);
            dealtCards.add(card);
        }

        table.runningValue = CardCountingStrategy.updateRunningValue(table.runningValue, dealtCards);
        table.setTrueValueAndHouseEdge(CardCountingStrategy.calculateTrueValue(table.runningValue, table.getCardsInPlay().getCardsLeft()));
        table.calculateCardValues();

        table.setStateMessage("Waiting for summary");
        table.setCountdownTime(gameTimingConfig.summaryDelay);
        practiceGameStateManager.scheduleStateChange(table.getId(), ROUND_SUMMARY, gameTimingConfig.summaryDelay);
        return table;
    }

    public PracticeTable botTurn(PracticeTable table) {
        if (table.getGameState() != PLAYING) {
            return null;
        }
        table.setGameState(BOT_TURN);
        table.getBotPlayer().setCurrentAction(PlayerActions.DECIDING);

        while (table.getBotPlayer().getHand().value <= 21) {
            switch (BotStrategy.decide(table.getBotPlayer().getHand(), table.getCroupier().getHand().cards.get(1), table.getBotPlayer().getHand().cards.size() == 2)) {
                case HIT -> {
                    Card card = table.getBotCardsInPlay().dealCard();
                    table.getBotPlayer().getHand().addCard(card);
                    table.getBotPlayer().getLastDecisions().add(BotStrategy.Decision.HIT);
                }
                case STAND -> {
                    table.getBotPlayer().setCurrentAction(PlayerActions.WAITING);
                    table.getBotPlayer().getLastDecisions().add(BotStrategy.Decision.STAND);
                }
                case DOUBLE -> {
                    table.getBotPlayer().placeBet(table.getBotPlayer().getBet());
                    Card card = table.getBotCardsInPlay().dealCard();
                    table.getBotPlayer().getHand().addCard(card);
                    table.getBotPlayer().setCurrentAction(PlayerActions.WAITING);
                    table.getBotPlayer().getLastDecisions().add(BotStrategy.Decision.DOUBLE);
                }
            }

            if (table.getBotPlayer().getCurrentAction() == PlayerActions.WAITING) {
                break;
            }
        }
        table.getBotPlayer().setCurrentAction(PlayerActions.WAITING);

        table.calculateCardValues();

        nextTurn(table);
        practiceGameStateManager.notifyClients(table.getId());
        return table;
    }

    public PracticeTable processPlayerDecision(Player player, PracticeTable table, PlayerDecisions decision) throws Exception {
        if (player.getHand().isOver || table.getGameState() != PLAYING) {
            throw new Exception("This player is not allowed to make a move now");
        } else {
            if (!player.getAvailableDecisions().contains(decision)) {
                throw new Exception("Player decision is invalid");
            }
            ArrayList<Card> dealtCards = new ArrayList<>();
            switch (decision) {
                case HIT -> {
                    Card card = table.getCardsInPlay().dealCard();
                    player.getHand().addCard(card);
                    dealtCards.add(card);
                    if (player.getHand().isOver) {
                        player.setCurrentAction(PlayerActions.WAITING);
                        nextTurn(table);
                    } else {
                        table.setCountdownTime(gameTimingConfig.practiceTurnDelay);
                        table.setStateMessage("%s's turn".formatted(player.getName()));
                        practiceGameStateManager.scheduleStateChange(table.getId(), NEXT_TURN, gameTimingConfig.practiceTurnDelay);
                    }
                }
                case STAND -> {
                    player.setCurrentAction(PlayerActions.WAITING);
                    nextTurn(table);
                }
                case DOUBLE -> {
                    player.placeBet(player.getBet(), true);

                    Card card = table.getCardsInPlay().dealCard();
                    player.getHand().addCard(card);
                    dealtCards.add(card);

                    player.setCurrentAction(PlayerActions.WAITING);
                    nextTurn(table);
                }
            }
            table.runningValue = CardCountingStrategy.updateRunningValue(table.runningValue, dealtCards);
            table.setTrueValueAndHouseEdge(CardCountingStrategy.calculateTrueValue(table.runningValue, table.getCardsInPlay().getCardsLeft()));
            player.evaluatePossibleDecisions();
            table.updatePlayer(player);
        }
        table.calculateCardValues();
        practiceGameStateManager.notifyClients(table.getId());

        return table;
    }

    private void nextTurn(PracticeTable table) {
        table.setTurnNumber(table.getTurnNumber() + 1);
        switch (table.getTurnNumber()) {
            case 1:
                table.setStateMessage("Bot Turn");
                table.setCountdownTime(gameTimingConfig.botDelay);
                practiceGameStateManager.scheduleStateChange(table.getId(), BOT_TURN, gameTimingConfig.botDelay);
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
        Player player = table.player;
        boolean playerWon = false;
        int playerValue = player.getHand().value;
        boolean playerBlackJack = (playerValue == 21 && player.getHand().cards.size() == 2);
        if ((croupierValue > 21 || (croupierValue < playerValue)) && playerValue <= 21) {
            playerWon = true;
        }
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
            player.addPracticeBalance(playerWinnings);
            table.getCroupier().addLosings(playerWinnings);
        } else if (playerValue == croupierValue) {
            player.addPracticeBalance(player.getBet());
            player.setLastRoundResult(PlayerRoundResult.DRAW);
        } else {
            player.addLosings(player.getBet());
            table.getCroupier().addWinnings(player.getBet());
            player.setLastRoundResult(PlayerRoundResult.LOST);
        }
        player.setBet(0);
        playerService.save(player);
        table.updatePlayer(player);

        boolean botWon = false;
        int botValue = table.getBotPlayer().getHand().value;
        boolean botBlackjack = (botValue == 21 && table.getBotPlayer().getHand().cards.size() == 2);
        if ((croupierValue > 21 || (croupierValue < botValue)) && botValue <= 21) {
            botWon = true;
        }
        if (botWon) {
            int botWinnings;
            if (botBlackjack) {
                botWinnings = table.getBotPlayer().getBet() * table.getBlackJackMultiplier();
                table.getBotPlayer().setLastRoundResult(PlayerRoundResult.BLACKJACK);
            } else {
                botWinnings = table.getBotPlayer().getBet() * 2;
                table.getBotPlayer().setLastRoundResult(PlayerRoundResult.WON);
            }
            table.getBotPlayer().addBalance(botWinnings);
        } else if (botValue == croupierValue) {
            table.getBotPlayer().addBalance(table.getBotPlayer().getBet());
            table.getBotPlayer().setLastRoundResult(PlayerRoundResult.DRAW);
        } else {
            table.getBotPlayer().setLastRoundResult(PlayerRoundResult.LOST);
        }
        table.getBotPlayer().setBet(0);
        player.setBet(0);
        playerService.save(player);
        table.updatePlayer(player);

        table.setStateMessage("Preparing next round");
        table.setCountdownTime(gameTimingConfig.postGameWaiting);
        practiceGameStateManager.scheduleStateChange(table.getId(), WAITING_FOR_PLAYERS, gameTimingConfig.postGameWaiting);
        return table;
    }
}
