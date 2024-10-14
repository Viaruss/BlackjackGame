package com.Viarus.BlackjackGame.Table.Player;

import com.Viarus.BlackjackGame.Table.Table;
import com.Viarus.BlackjackGame.Table.TableService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    PlayerDAO playerDAO;
    TableService tableService;
    public PlayerService(PlayerDAO playerDAO,
                         @Lazy //to resolve circular dependency
                         TableService tableService) {
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
        return player;
    }

    public void save(Player player) {
        playerDAO.save(player);
    }

    public Table makeMove(String tableId, String playerId, String playerDecision) {
        Table table = tableService.getTable(tableId);
        if(table == null) {
            return null;
        }
        Player player = table.getPlayers()
                .stream()
                .filter(p -> p.getId()
                        .equals(playerId))
                .findFirst()
                .orElse(null);

        if (player == null) {
            return null;
        }

        table = table.processPlayerDecision(player,
                PlayerDecisions.valueOf(playerDecision.toUpperCase())
        );

        if (table == null) {
            //this will be usefull to give a feedback to frontend, when we return null
            // the response Entity will return 4xx status, and we know that the move was not made
            return null;
        }

        save(player);
        tableService.save(table);

        return table;
    }

    public Player getPlayerByName(String name)  {
        return playerDAO.findPlayerByName(name);
    }
}
