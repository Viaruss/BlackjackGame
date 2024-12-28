package com.Viarus.BlackjackGame.Game.Table;

import com.Viarus.BlackjackGame.Cards.Hand;
import com.Viarus.BlackjackGame.Game.Player.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.Viarus.BlackjackGame.Game.Table.GameState.*;

@Slf4j
@Service
public class TableService {
    private final TableDAO tableDAO;
    private final PlayerService playerService;
    private final PlayerDAO playerDAO;
    public final GameTimingSettings gameTimingSettings;
    public final GameStateManager gameStateManager;
    public final GameplaySettings gameplaySettings;


    public TableService(TableDAO tableDAO,
                        PlayerService playerService,
                        PlayerDAO playerDAO,
                        GameStateManager gameStateManager,
                        GameTimingSettings gameTimingSettings,
                        GameplaySettings gameplaySettings) {
        this.tableDAO = tableDAO;
        this.playerService = playerService;
        this.playerDAO = playerDAO;
        this.gameStateManager = gameStateManager;
        this.gameTimingSettings = gameTimingSettings;
        this.gameplaySettings = gameplaySettings;
    }

    public Table getTable(String tableId) {
        return tableDAO.findById(tableId).orElse(null);
    }

    public Table createNewTable() {
        Table table = new Table();
        tableDAO.save(table);
        return table;
    }

    public List<Table> getAllTables() {
        return tableDAO.findAll();
    }

    public Table joinTable(String tableId, String playerId) throws Exception {
        Table table = getTable(tableId);
        Player player = playerService.getPlayerById(playerId);
        if (!table.players.contains(player) && Objects.equals(player.getCurrentTableId(), null)) {
            player.setCurrentAction(table.getGameState() == BETTING ? PlayerActions.BETTING : PlayerActions.WAITING);
            table.addPlayer(player);
            player.setCurrentTableId(tableId);
            tableDAO.save(table);
            playerDAO.save(player);
        }
        if (Objects.equals(player.getCurrentTableId(), tableId)) {
            if (table.getPlayers().stream().filter(Objects::nonNull).count() == 1) {
                table.setStateMessage("Preparing Table");
                table.setCountdownTime(gameTimingSettings.initialWaiting);
                tableDAO.save(table);
                gameStateManager.notifyClients(table.getId());
                gameStateManager.scheduleStateChange(tableId, BETTING, gameTimingSettings.initialWaiting);
            }
            return table;
        } else {
            throw new Exception("Player is already at another table");
        }
    }

    public Table leaveTable(String tableId, String playerId) {
        Table table = getTable(tableId);
        Player player = playerService.getPlayerById(playerId);

        gameStateManager.cancelScheduledTask();
        table.removePlayer(player);
        player.leaveTable();

        if(table.players.stream().filter(Objects::nonNull).toList().isEmpty()) {
            table.getCroupier().setHand(new Hand());
            table.setGameState(WAITING_FOR_PLAYERS);
        }

        tableDAO.save(table);
        playerDAO.save(player);

        return table;
    }

    public Table placeBet(String tableId, String playerId, int amount) throws Exception {
        if (playerService.getPlayerById(playerId).getCurrentAction() == PlayerActions.BETTING) {
            Table table = getTable(tableId);
            if(table.getGameState() != BETTING) {
                throw new Exception("This table is not in betting state");
            }
            Player player = playerService.placeBet(playerId, amount);
            player.setCurrentAction(PlayerActions.WAITING);

            table.updatePlayer(player);
            checkBettingStatus(table);
            tableDAO.save(table);
            return table;
        } else throw new Exception("This player cannot place bets now");
    }

    private void checkBettingStatus(Table table) {
        List<Player> currentPlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
        int betCount = 0;
        for (Player player : currentPlayers) {
            betCount += player.getBet() == 0 ? 0 : 1;
        }
        if (betCount == 1) {
            table.setStateMessage("Waiting for other bets");
            table.setCountdownTime(gameTimingSettings.betting);
            gameStateManager.scheduleStateChange(table.getId(), PLAYING, gameTimingSettings.betting);
        } else if (betCount == currentPlayers.size()){
            table.setStateMessage("All bets placed");
            table.setCountdownTime(1);
            gameStateManager.scheduleStateChange(table.getId(), PLAYING, 1);
        }
    }

    public void changeGameState(String tableId, GameState gameState) throws Exception {
        Table table = getTable(tableId);
        Table updatedTable = switch (gameState) {
            case BETTING -> startBetting(table);
            case PLAYING -> startGame(table);
            case NEXT_TURN -> passTurn(table);
            case CROUPIER_TURN -> croupierTurn(table);
            case ROUND_SUMMARY -> roundResult(table);
            case WAITING_FOR_PLAYERS -> prepareGame(table);
        };
        tableDAO.save(updatedTable);
        gameStateManager.notifyClients(table.getId());
    }

    private Table prepareGame(Table table) {
        table.setGameState(WAITING_FOR_PLAYERS);
        clearCards(table);
        table.setStateMessage("Waiting for players");
        table.setCountdownTime(gameTimingSettings.postGameWaiting);
        gameStateManager.scheduleStateChange(table.getId(), BETTING, gameTimingSettings.initialWaiting);
        return table;
    }

    public Table startBetting(Table table) {
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

    public Table startGame(Table table) {
        if (table.getGameState() != BETTING) {
            return table;
        } else if (table.getPlayers().stream().filter(Objects::nonNull).noneMatch(player -> player.getBet() > 0)) {
            System.out.println("No bets placed");
            table.setStateMessage("Waiting for bets");
            table.setCountdownTime(gameTimingSettings.betting);
            gameStateManager.scheduleStateChange(table.getId(), PLAYING, gameTimingSettings.betting);
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
        table.setCountdownTime(gameTimingSettings.turnDelay);
        table.setStateMessage("%s's turn".formatted(availablePlayers.get(0).getName()));
        gameStateManager.scheduleStateChange(table.getId(), NEXT_TURN, gameTimingSettings.turnDelay);
        return table;
    }

    private Table passTurn(Table table) throws Exception {
        processPlayerDecision(table.players.get(table.getTurnNumber()), table, PlayerDecisions.STAND);
        return table;
    }

    private void clearCards(Table table) {
        table.getCroupier().setHand(new Hand());
        for (Player player : table.getPlayers().stream().filter(Objects::nonNull).toList()) {
            player.setHand(new Hand());
        }
    }

    public Table croupierTurn(Table table) {
        if (table.getGameState() != PLAYING) {
            return null;
        }
        table.setGameState(GameState.CROUPIER_TURN);

        table.getCroupier().showCards();
        while (table.getCroupier().getHand().value < gameplaySettings.getCroupierLimit()) {
            table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());
        }
        table.setStateMessage("Waiting for summary");
        table.setCountdownTime(gameTimingSettings.summaryDelay);
        gameStateManager.scheduleStateChange(table.getId(), GameState.ROUND_SUMMARY, gameTimingSettings.summaryDelay);
        return table;
    }

    public Table processPlayerDecision(Player player, Table table, PlayerDecisions decision) throws Exception {
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
        gameStateManager.notifyClients(table.getId());

        return table;
    }

    private void nextTurn(Table table) {
        table.setTurnNumber(table.getTurnNumber() + 1);
        List<Player> availablePlayers = table.getPlayers()
                .stream()
                .filter(Objects::nonNull)
                .toList();
        if (table.getTurnNumber() == availablePlayers.size()) {
            table.setStateMessage("Croupier Turn");
            table.setCountdownTime(gameTimingSettings.croupierDelay);
            gameStateManager.scheduleStateChange(table.getId(), GameState.CROUPIER_TURN, gameTimingSettings.croupierDelay);
        } else if (!availablePlayers.get(table.getTurnNumber()).isPlaying()) {
            nextTurn(table);
        } else {
            Player player = availablePlayers.get(table.getTurnNumber());
            player.setCurrentAction(PlayerActions.DECIDING);
            table.updatePlayer(player);
            playerService.save(player);
            tableDAO.save(table);
            table.setCountdownTime(gameTimingSettings.turnDelay);
            table.setStateMessage("%s's turn".formatted(player.getName()));
            gameStateManager.scheduleStateChange(table.getId(), NEXT_TURN, gameTimingSettings.turnDelay);
        }
    }

    public void save(Table table) {
        tableDAO.save(table);
    }

    public Table roundResult(Table table) {
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
        table.setCountdownTime(gameTimingSettings.postGameWaiting);
        gameStateManager.scheduleStateChange(table.getId(), WAITING_FOR_PLAYERS, gameTimingSettings.postGameWaiting);
        return table;
    }
}
