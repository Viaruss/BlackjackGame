package com.Viarus.BlackjackGame.Game.Player;

import com.Viarus.BlackjackGame.Game.Table.Table;
import com.Viarus.BlackjackGame.Game.Table.TableService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    PlayerDAO playerDAO;
    TableService tableService;
    public PlayerService(PlayerDAO playerDAO,
                         @Lazy TableService tableService) {
        this.playerDAO = playerDAO;
        this.tableService = tableService;
    }

    public Player createNewPlayer(String name) {
        Player player = new Player(name, 1000);
        playerDAO.save(player);
        return player;
    }

    public Player getPlayerById(String id){
        return playerDAO.findById(id).orElse(null);
    }

    public Player placeBet(String playerId, int amount) {
        Player player = getPlayerById(playerId);
        player.placeBet(amount);
        playerDAO.save(player);
        return player;
    }

    public void save(Player player) {
        playerDAO.save(player);
    }

    public Table makeMove(String tableId, String playerId, String playerDecision) throws Exception {
        Table table = tableService.getTable(tableId);
        if(table == null) {
            throw new Exception("Table not found");
        }
        Player player = table.getPlayers()
                .stream()
                .filter(p -> p.getId()
                        .equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null) {
            throw new Exception("Player not found");
        }

        table = tableService.processPlayerDecision(player, table, PlayerDecisions.valueOf(playerDecision.toUpperCase()));

        save(player);
        tableService.save(table);

        return table;
    }

    public Player getPlayerByName(String name)  {
        return playerDAO.findPlayerByName(name);
    }
}
