package com.Viarus.BlackjackGame.Table;

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
    private TableService tableService; // Mocked service

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Mock service behavior with pre-prepared JSON response
        Table table = objectMapper.readValue(loadJson("src/test/resources/getTableResponse.json"), Table.class);
        Mockito.when(tableService.getAllTables()).thenReturn(List.of(table));
    }

    @Test
    public void testGetAllTables() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/table"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the content matches your expected response (in JSON format)
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
