package com.Viarus.BlackjackGame.Game.Table;

import com.Viarus.BlackjackGame.Cards.Hand;
import com.Viarus.BlackjackGame.Game.Player.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
            table.addPlayer(player);
            player.setCurrentTableId(tableId);
            tableDAO.save(table);
            playerDAO.save(player);
        }
        if (Objects.equals(player.getCurrentTableId(), tableId)) {
            if (table.getPlayers().stream().filter(Objects::nonNull).count() == 1) {
                gameStateManager.scheduleStateChange(tableId, GameState.BETTING, gameTimingSettings.initialWaiting);
            }
            return table;
        } else {
            throw new Exception("Player is already at another table");
        }
    }

    public Table leaveTable(String tableId, String playerId) {
        Table table = getTable(tableId);
        Player player = playerService.getPlayerById(playerId);

        table.removePlayer(player);
        player.setCurrentTableId(null);

        tableDAO.save(table);
        playerDAO.save(player);

        return table;
    }

    public Table placeBet(String tableId, String playerId, int amount) throws Exception {
        if (playerService.getPlayerById(playerId).getCurrentAction() == PlayerActions.BETTING) {
            Table table = getTable(tableId);
            Player player = playerService.placeBet(playerId, amount);
            player.setCurrentAction(PlayerActions.WAITING);

            table.updatePlayer(player);

            tableDAO.save(table);
            return table;
        } else throw new Exception("This player cannot place bets now");
    }

    public void changeGameState(String tableId, GameState gameState) {
        Table table = getTable(tableId);
        Table updatedTable = switch (gameState) {
            case BETTING -> startBetting(table);
            case PLAYING -> startGame(table);
            case CROUPIER_TURN -> croupierTurn(table);
            case ROUND_SUMMARY -> roundResult(table);
            case WAITING_FOR_PLAYERS -> prepareGame(table);
        };
        tableDAO.save(updatedTable);
        gameStateManager.notifyClients(table.getId());
    }

    private Table prepareGame(Table table) {
        table.setGameState(GameState.WAITING_FOR_PLAYERS);
        clearCards(table);
        gameStateManager.notifyClients(table.getId());
        gameStateManager.scheduleStateChange(table.getId(), GameState.BETTING, gameTimingSettings.initialWaiting);
        return table;
    }

    public Table startBetting(Table table) {
        if (table.getGameState() == GameState.WAITING_FOR_PLAYERS) {
            table.setGameState(GameState.BETTING);
            List<Player> currentPlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
            for (Player player : currentPlayers) {
                player.setCurrentAction(PlayerActions.BETTING);
                table.updatePlayer(player);
                playerService.save(player);
            }
            gameStateManager.scheduleStateChange(table.getId(), GameState.PLAYING, gameTimingSettings.betting);
        }
        return table;
    }

    public Table startGame(Table table) {
        if (table.getGameState() != GameState.BETTING) {
            return table;
        }else if (!table.getPlayers().stream().filter(Objects::nonNull).anyMatch(player -> player.getBet() > 0)) {
            System.out.println("No bets placed");
            gameStateManager.scheduleStateChange(table.getId(), GameState.PLAYING, gameTimingSettings.betting);
            return table;
        }
        table.setGameState(GameState.PLAYING);
        //clearCards(table); TODO: Check if this is necessary

        List<Player> currentPlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard().hide());
        table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());

        for (Player player : currentPlayers) {
            for (int i = 0; i < 2; i++) {
                player.getHand().addCard(table.getCardsInPlay().dealCard());
            }
        }
        table.setTurnNumber(0);

        List<Player> availablePlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
        availablePlayers.get(0).setCurrentAction(PlayerActions.DECIDING);
        for (Player player : availablePlayers) {
            player.evaluatePossibleDecisions();
            table.updatePlayer(player);
            playerService.save(player);
        }

        return table;
    }

    private void clearCards(Table table){
        table.getCroupier().setHand(new Hand());
        for (Player player : table.getPlayers().stream().filter(Objects::nonNull).toList()) {
            player.setHand(new Hand());
        }
    }

    public Table croupierTurn(Table table) {
        if (table.getGameState() != GameState.PLAYING) {
            return null;
        }
        table.setGameState(GameState.CROUPIER_TURN);

        table.getCroupier().showCards();
        while (table.getCroupier().getHand().value < gameplaySettings.getCroupierLimit()) {
            table.getCroupier().getHand().addCard(table.getCardsInPlay().dealCard());
        }
        gameStateManager.notifyClients(table.getId());
        gameStateManager.scheduleStateChange(table.getId(), GameState.ROUND_SUMMARY, gameTimingSettings.summaryDelay);
        return table;
    }

    public Table processPlayerDecision(Player player, Table table, PlayerDecisions decision) throws Exception {
        if (table.players.indexOf(player) != table.getTurnNumber() || player.getHand().isOver || table.getGameState() != GameState.PLAYING) {
            throw new Exception("This player is not allowed to make a move now");
        }
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
        gameStateManager.notifyClients(table.getId());

        return table;
    }

    private void nextTurn(Table table) {
        table.setTurnNumber(table.getTurnNumber() + 1);
        List<Player> availablePlayers = table.getPlayers().stream().filter(Objects::nonNull).toList();
        if (table.getTurnNumber() == availablePlayers.size()) {
            gameStateManager.scheduleStateChange(table.getId(), GameState.CROUPIER_TURN, gameTimingSettings.croupierDelay);
        } else {
            Player player = availablePlayers.get(table.getTurnNumber());
            player.setCurrentAction(PlayerActions.DECIDING);
            table.updatePlayer(player);
            playerService.save(player);
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
                } else {
                    playerWinnings = player.getBet() * 2;
                }
                player.addWinnings(playerWinnings - player.getBet());
                player.addBalance(playerWinnings);
                table.getCroupier().addLosings(playerWinnings);
            } else if (playerValue == croupierValue) {
                player.addBalance(player.getBet());
            } else {
                player.addLosings(player.getBet());
                table.getCroupier().addWinnings(player.getBet());
            }

            player.setBet(0);

            playerService.save(player);
            table.updatePlayer(player);
        }
        //TODO: add something to directly notify clients about the round result for them
        gameStateManager.notifyClients(table.getId());
        gameStateManager.scheduleStateChange(table.getId(), GameState.WAITING_FOR_PLAYERS, gameTimingSettings.postGameWaiting);
        tableDAO.save(table);
        return table;
    }
}
