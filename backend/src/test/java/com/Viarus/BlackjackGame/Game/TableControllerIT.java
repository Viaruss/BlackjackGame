package com.Viarus.BlackjackGame.Game;

import com.Viarus.BlackjackGame.Game.Table.Table;
import com.Viarus.BlackjackGame.Game.Table.TableController;
import com.Viarus.BlackjackGame.Game.Table.TableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TableController.class)
public class TableControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableService tableService;

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @BeforeEach
    public void setUp() throws Exception {
        Table table = objectMapper.readValue(loadJson("src/test/resources/getTableResponse.json"), Table.class);
        Mockito.when(tableService.getAllTables()).thenReturn(List.of(table));
    }

    @Test
    public void testGetAllTables() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/table"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponse = loadJson("src/test/resources/getTableResponse.json");
        assertEquals(expectedResponse, result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateNewTable() throws Exception {
        Table table = objectMapper.readValue(loadJson("src/test/resources/getTableResponse.json"), Table.class);

        Mockito.when(tableService.createNewTable()).thenReturn(table);

        mockMvc.perform(post("/api/v1/table"))
                .andExpect(status().isOk());
    }
}
