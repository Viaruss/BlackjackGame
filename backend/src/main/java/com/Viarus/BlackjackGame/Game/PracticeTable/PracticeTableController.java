package com.Viarus.BlackjackGame.Game.PracticeTable;


import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/practiceTable")
public class PracticeTableController {

    private final PracticeTableService practiceTableService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public PracticeTableController(PracticeTableService practiceTableService,
                                   SimpMessagingTemplate simpMessagingTemplate) {
        this.practiceTableService = practiceTableService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping(path = "/{tableId}")
    public PracticeTable getTable(@PathVariable String tableId) {
        return practiceTableService.getTable(tableId);
    }

    @PutMapping(path = "/join")
    public ResponseEntity<Object> joinTable(@RequestParam String playerId) {
        try {
            PracticeTable table = practiceTableService.joinTable(playerId);
            simpMessagingTemplate.convertAndSend("/topic/practiceTable/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping(path = "/leave/{tableId}")
    public ResponseEntity<Object> leaveTable(@PathVariable String tableId) {
        try {
            PracticeTable table = practiceTableService.leaveTable(tableId);
            simpMessagingTemplate.convertAndSend("/topic/practiceTable/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping(path = "/bet/{tableId}")
    public ResponseEntity<Object> placeBet(@PathVariable String tableId,
                                           @RequestParam int amount) {
        try {
            PracticeTable table = practiceTableService.placeBet(tableId, amount);
            simpMessagingTemplate.convertAndSend("/topic/practiceTable/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
