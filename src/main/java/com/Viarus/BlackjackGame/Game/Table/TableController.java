package com.Viarus.BlackjackGame.Game.Table;


import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/table")
public class TableController {

    private final TableService tableService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public TableController(TableService tableService,
                           SimpMessagingTemplate simpMessagingTemplate) {
        this.tableService = tableService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @GetMapping(path = "")
    public List<Table> getAllTables() {
        return tableService.getAllTables();
    }

    @GetMapping(path = "/{tableId}")
    public Table getTable(@PathVariable String tableId) {
        return tableService.getTable(tableId);
    }

    @PostMapping()
    public Table createNewTable() {
        return tableService.createNewTable();
    }

    @PutMapping(path = "join/{tableId}")
    public ResponseEntity<Object> joinTable(@PathVariable String tableId,
                                            @RequestParam String playerId) {
        try {
            Table table = tableService.joinTable(tableId, playerId);
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
            Table table = tableService.leaveTable(tableId, playerId);
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
            Table table = tableService.placeBet(tableId, playerId, amount);
            simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

/*    TODO: Might be useless, should be automatically called by a scheduler
    @PostMapping("/startGame/{tableId}")
    public ResponseEntity<Object> startGame(@PathVariable String tableId) {
        try {
            Table table = tableService.startGame(tableId);
            simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
            return ResponseEntity.ok(table);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }*/
/*  TODO: Might be useless, should be automatically called by a scheduler
    @GetMapping("roundResult/{tableId}")
    public ResponseEntity<Table> roundResult(@PathVariable String tableId) {
        Table table = tableService.roundResult(tableId);

        if (table == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(table);
    }*/
}
