package com.Viarus.BlackjackGame.Table;

import com.Viarus.BlackjackGame.Table.Player.Player;
import com.Viarus.BlackjackGame.Table.Player.PlayerActions;
import com.Viarus.BlackjackGame.Table.Player.PlayerDAO;
import com.Viarus.BlackjackGame.Table.Player.PlayerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TableService {
    private final TableDAO tableDAO;
    private final PlayerService playerService;
    private final PlayerDAO playerDAO;

    public TableService(TableDAO tableDAO, PlayerService playerService, PlayerDAO playerDAO) {
        this.tableDAO = tableDAO;
        this.playerService = playerService;
        this.playerDAO = playerDAO;
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

            playerService.save(player);
            tableDAO.save(table);
            return table;
        }
        else throw new Exception("This player cannot place bets now");

    }

    public Table startGame(String tableId) {
        Table table = getTable(tableId);
        table = table.startGame();

        for (Player player : table.players) {
            playerService.save(player);
        }

        tableDAO.save(table);
        return table;
    }

    public void save(Table table) {
        tableDAO.save(table);
    }

    public Table croupierMove(String tableId) {
        Table table = getTable(tableId);
        table.croupierMove();

        tableDAO.save(table);
        return table;
    }

    public Table roundResult(String tableId) {
        Table table = getTable(tableId);
        if (table == null) {
            return null;
        }

        int croupierValue = table.getCroupier().getHand().value;
        for (Player player : table.getPlayers()) {
            boolean playerWon = false;
            int playerValue = player.getHand().value;
            boolean playerBlackJack = (playerValue == 21 && player.getHand().cards.size() == 2);
            if ((croupierValue > 21 || croupierValue < playerValue) && croupierValue != 21) {
                playerWon = true;
            }

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

        tableDAO.save(table);
        return table;
    }
}
