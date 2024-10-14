package com.Viarus.BlackjackGame.Table;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/table")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    //TODO: Delete after testing
    @GetMapping(path = "")
    public List<Table> getAllTables(){
        return tableService.getAllTables();
    }

    @GetMapping(path = "/{tableId}")
    public Table getTable(@PathVariable String tableId){
        return tableService.getTable(tableId);
    }

    @PostMapping()
    public Table createNewTable(){
        return tableService.createNewTable();
    }

    @PutMapping(path = "join/{tableId}")
    public Table joinTable(@PathVariable String tableId,
                           @RequestParam String playerId){
        return tableService.joinTable(tableId, playerId);
    }

    @PutMapping(path = "/leave/{tableId}")
    public Table leaveTable(@PathVariable String tableId,
                            @RequestParam String playerId){
        return tableService.leaveTable(tableId, playerId);
    }

    @PutMapping(path = "/bet/{tableId}")
    public Table placeBet(@PathVariable String tableId,
                          @RequestParam String playerId,
                          @RequestParam int amount){
        return tableService.placeBet(tableId, playerId, amount);
    }

    @PostMapping("/startGame/{tableId}")
    public Table startGame(@PathVariable String tableId){
        return tableService.startGame(tableId);
    }

    @PutMapping("/croupierTurn/{tableId}")
    public ResponseEntity<Table> croupierMove(@PathVariable String tableId){
        Table table = tableService.croupierMove(tableId);

        if(table == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(table);
    }

    @GetMapping("roundResult/{tableId}")
    public ResponseEntity<Table> roundResult(@PathVariable String tableId){
        Table table = tableService.roundResult(tableId);

        if(table == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(table);
    }

}
