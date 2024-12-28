package com.Viarus.BlackjackGame.Game.PracticeTable;


import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping(path = "")
    public List<PracticeTable> getAllTables() {
        return practiceTableService.getAllTables();
    }

    @GetMapping(path = "/{tableId}")
    public PracticeTable getTable(@PathVariable String tableId) {
        return practiceTableService.getTable(tableId);
    }

    @PostMapping()
    public PracticeTable createNewTable() {
        return practiceTableService.createNewTable();
    }

    @PutMapping(path = "join/{tableId}")
    public ResponseEntity<Object> joinTable(@PathVariable String tableId,
                                            @RequestParam String playerId) {
        try {
            PracticeTable table = practiceTableService.joinTable(tableId, playerId);
            simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping(path = "/leave/{tableId}")
    public ResponseEntity<Object> leaveTable(@PathVariable String tableId,
                                             @RequestParam String playerId) {
        try {
            PracticeTable table = practiceTableService.leaveTable(tableId, playerId);
            simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PutMapping(path = "/bet/{tableId}")
    public ResponseEntity<Object> placeBet(@PathVariable String tableId,
                                           @RequestParam String playerId,
                                           @RequestParam int amount) {
        try {
            PracticeTable table = practiceTableService.placeBet(tableId, playerId, amount);
            simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
