package com.Viarus.BlackjackGame.Table.Player;

import com.Viarus.BlackjackGame.Table.Table;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/player")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(
            @Lazy //to resolve circular dependency
            PlayerService playerService) {
        this.playerService = playerService;
    }

    //TODO: Delete After Testing
    @GetMapping()
    public ResponseEntity<Player> getPlayerByName(@RequestParam String name) {
        Player player = playerService.getPlayerByName(name);
        System.out.printf("Player: %s\n", player);
        if (player == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else return ResponseEntity.ok(player);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable String id) {
        Player player = playerService.getPlayerById(id);
        if (player == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else return ResponseEntity.ok(player);
    }

    @PostMapping()
    public ResponseEntity<Player> createNewPlayer(@RequestParam String name) {
        Player player = playerService.createNewPlayer(name);
        System.out.printf("Player: %s\n", player);
        if (player == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else return ResponseEntity.ok(player);
    }

    @PutMapping(path = "/{tableId}/makeMove")
    public ResponseEntity<Table> playerMove(@PathVariable String tableId,
                                            @RequestParam String playerId,
                                            @RequestParam String playerDecision) {

        Table table = playerService.makeMove(tableId, playerId, playerDecision);

        if (table == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(table);
    }

}
