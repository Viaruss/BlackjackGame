package com.Viarus.BlackjackGame.Game.PracticeTable;

import com.Viarus.BlackjackGame.Cards.Deck;
import com.Viarus.BlackjackGame.Game.Croupier.Croupier;
import com.Viarus.BlackjackGame.Game.Player.Player;
import com.Viarus.BlackjackGame.Game.PracticeTable.Utils.BotPlayer;
import com.Viarus.BlackjackGame.Game.Table.Utils.GameState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class PracticeTable {
    @Id
    @Setter(AccessLevel.NONE)
    String id;

    Player player;
    BotPlayer botPlayer;
    Croupier croupier;

    private Deck cardsInPlay;
    private GameState gameState;
    private int turnNumber;

    private int countdownTime;
    private String stateMessage;

    //game stats
    int runningValue;
    int trueValue;
    double houseEdge;
    int totalCards;
    int cardsLeft;
    int cardsDealt;

    //TODO: Add loading from properties (might be possible to do it in a constructor when creating new table - verify)
    private int maxPlayers = 3;
    private int decksCount = 3;
    private int blackJackMultiplier = 3;

    public PracticeTable() {
        this.player = null;
        this.croupier = new Croupier();
        this.cardsInPlay = new Deck(decksCount);
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.countdownTime = 0;
        this.stateMessage = "";
        this.runningValue = 0;
        this.trueValue = 0;
        this.houseEdge = 0.5;
        this.totalCards = cardsInPlay.getCards().size();
        this.cardsLeft = totalCards;
        this.cardsDealt = 0;
    }

    public PracticeTable(Player player) {
        this.player = player;
        this.croupier = new Croupier();
        this.cardsInPlay = new Deck(decksCount);
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.stateMessage = "";
        this.runningValue = 0;
        this.trueValue = 0;
        this.houseEdge = 0.5;
        this.totalCards = cardsInPlay.getCards().size();
        this.cardsLeft = totalCards;
        this.cardsDealt = 0;
    }

    public void updatePlayer(Player updatedPlayer) {
        this.player = updatedPlayer;
    }
}