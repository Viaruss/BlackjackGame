package com.Viarus.BlackjackGame.Game.Player;

import com.Viarus.BlackjackGame.Game.PracticeTable.PracticeTable;
import com.Viarus.BlackjackGame.Game.Table.Table;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/player")
public class PlayerController {
    private final PlayerService playerService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public PlayerController(
            @Lazy
            PlayerService playerService,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.playerService = playerService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping()
    public ResponseEntity<Player> getPlayerByName(@RequestParam String name) {
        Player player = playerService.getPlayerByName(name);
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
        if (player == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        else return ResponseEntity.ok(player);
    }

    @PutMapping(path = "/{tableId}/makeMove")
    public ResponseEntity<Object> playerMove(@PathVariable String tableId,
                                             @RequestParam String playerId,
                                             @RequestParam String playerDecision) {
        try {
            Table table = playerService.makeMove(tableId, playerId, playerDecision);
            simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping(path = "/practice/{tableId}/makeMove")
    public ResponseEntity<Object> playerPracticeMove(@PathVariable String tableId,
                                             @RequestParam String playerDecision) {
        try {
            PracticeTable table = playerService.makePracticeMove(tableId, playerDecision);
            simpMessagingTemplate.convertAndSend("/topic/practiceTable/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
