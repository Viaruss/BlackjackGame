package com.Viarus.BlackjackGame.Game.PracticeTable;

import com.Viarus.BlackjackGame.Cards.Deck;
import com.Viarus.BlackjackGame.Game.Croupier.Croupier;
import com.Viarus.BlackjackGame.Game.Player.Player;
import com.Viarus.BlackjackGame.Game.Table.Utils.GameState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document
@Getter
@Setter
public class PracticeTable {
    @Id
    @Setter(AccessLevel.NONE)
    String id;
    ArrayList<Player> players;


    private Croupier croupier;
    private Deck cardsInPlay;
    private GameState gameState;
    private int turnNumber;
    private int countdownTime;
    private String stateMessage;

    //TODO: Add loading from properties (might be possible to do it in a constructor when creating new table - verify)
    private int maxPlayers = 3;
    private int decksCount = 3;
    private int blackJackMultiplier = 3;

    public PracticeTable() {
        this.players = new ArrayList<>();
        for (int i = 0; i < maxPlayers; i++) players.add(null);
        this.croupier = new Croupier();
        this.cardsInPlay = new Deck(decksCount);
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.countdownTime = 0;
        this.stateMessage = "";
    }

    public void addPlayer(Player player) throws Exception {
        if (this.players.contains(null)) {
            this.players.set(this.players.indexOf(null), player);
        } else {
            throw new Exception("Table is already full");
        }
    }

    public void removePlayer(Player player) {
        int playerIndex = this.players.indexOf(player);
        this.players.set(playerIndex, null);
    }


    public void updatePlayer(Player updatedPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(updatedPlayer.getId())) {
                players.set(i, updatedPlayer);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Table{" +
                "id='" + id + '\'' +
                ", players=" + players +
                ", maxPlayers=" + maxPlayers +
                ", decksCount=" + decksCount +
                ", croupier=" + croupier +
                ", cardsInPlay=" + cardsInPlay +
                '}';
    }
}