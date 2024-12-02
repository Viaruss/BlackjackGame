package com.Viarus.BlackjackGame.Table;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/table")
public class TableController {

    private final TableService tableService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public TableController(TableService tableService, SimpMessagingTemplate simpMessagingTemplate) {
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
    public Table leaveTable(@PathVariable String tableId,
                            @RequestParam String playerId) {
        return tableService.leaveTable(tableId, playerId);
    }

    @PutMapping(path = "/bet/{tableId}")
    public ResponseEntity<Object>  placeBet(@PathVariable String tableId,
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

    @PostMapping("/startGame/{tableId}")
    public Table startGame(@PathVariable String tableId) {
        Table table = tableService.startGame(tableId);
        simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
        return table;
    }

    @PutMapping("/croupierTurn/{tableId}")
    public ResponseEntity<Table> croupierMove(@PathVariable String tableId) {
        Table table = tableService.croupierMove(tableId);

        if (table == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        simpMessagingTemplate.convertAndSend("/topic/table/" + table.getId(), table);
        return ResponseEntity.ok(table);
    }

    @GetMapping("roundResult/{tableId}")
    public ResponseEntity<Table> roundResult(@PathVariable String tableId) {
        Table table = tableService.roundResult(tableId);

        if (table == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(table);
    }

}
