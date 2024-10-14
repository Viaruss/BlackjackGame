package com.Viarus.BlackjackGame.Table.Player;

import com.Viarus.BlackjackGame.Table.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PlayerControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PlayerDAO playerDAO;

    @BeforeEach
    public void setUp() {
        playerDAO.deleteAll();
    }

    @Test
    public void testCreateNewPlayer() {
        ResponseEntity<Player> response = restTemplate.postForEntity("/api/v1/player?name=John", null, Player.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("John");
    }

    @Test
    public void testGetAllPlayers() {
        restTemplate.postForEntity("/api/v1/player?name=John", null, Player.class);
        ResponseEntity<Player[]> response = restTemplate.getForEntity("/api/v1/player", Player[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    public void testMakeMove() {
        Table table = restTemplate.postForEntity("/api/v1/table", null, Table.class).getBody();
        Player player = restTemplate.postForEntity("/api/v1/player?name=John", null, Player.class).getBody();

        ResponseEntity<Table> response = restTemplate.exchange(
                "/api/v1/player/{tableId}/makeMove?playerId={playerId}&playerDecision={decision}",
                HttpMethod.PUT,
                null,
                Table.class,
                table.getId(), player.getId(), "HIT");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }


    // More tests for other endpoints...
}
