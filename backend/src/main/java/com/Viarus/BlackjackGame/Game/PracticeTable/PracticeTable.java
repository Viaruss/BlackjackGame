package com.Viarus.BlackjackGame.Game.PracticeTable;

import com.Viarus.BlackjackGame.Game.Cards.Card;
import com.Viarus.BlackjackGame.Game.Cards.Deck;
import com.Viarus.BlackjackGame.Game.Croupier.Croupier;
import com.Viarus.BlackjackGame.Game.Player.Player;
import com.Viarus.BlackjackGame.Game.PracticeTable.Utils.BotPlayer;
import com.Viarus.BlackjackGame.Game.Table.Utils.GameState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.Viarus.BlackjackGame.Game.PracticeTable.Utils.CardCountingStrategy.BASE_HOUSE_EDGE;
import static com.Viarus.BlackjackGame.Game.PracticeTable.Utils.CardCountingStrategy.IMPACT_PER_TRUE_VALUE;

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
    private Deck botCardsInPlay;
    private GameState gameState;
    private int turnNumber;

    private int countdownTime;
    private String stateMessage;

    int runningValue;
    double trueValue;
    double houseEdge;
    int totalCards;
    int playerValue, botValue, croupierValue;

    private int learningBalance = 100000;
    private int maxPlayers = 3;
    private int decksCount = 3;
    private int blackJackMultiplier = 3;

    public PracticeTable() {
        this.player = null;
        this.croupier = new Croupier();
        this.botPlayer = new BotPlayer("Maestro", this.id, learningBalance);
        this.cardsInPlay = new Deck(decksCount);
        this.botCardsInPlay = cardsInPlay;
        this.gameState = GameState.WAITING_FOR_PLAYERS;
        this.countdownTime = 0;
        this.stateMessage = "";
        this.runningValue = 0;
        this.trueValue = 0;
        this.houseEdge = 0.5;
        this.totalCards = cardsInPlay.getCards().size();
        this.playerValue = 0;
        this.botValue = 0;
        this.croupierValue = 0;
    }

    public void updatePlayer(Player updatedPlayer) {
        this.player = updatedPlayer;
    }

    public void setTrueValueAndHouseEdge(double trueValue) {
        this.trueValue = trueValue;
        double houseEdge = BASE_HOUSE_EDGE - (trueValue * IMPACT_PER_TRUE_VALUE);
        this.houseEdge = Math.min(Math.max(houseEdge, -0.03), 0.03) * 100;
    }

    @Override
    public String toString() {
        return "PracticeTable{" +
                "id='" + id + '\'' +
                ", player=" + player +
                ", botPlayer=" + botPlayer +
                ", croupier=" + croupier +
                ", cardsInPlay=" + cardsInPlay +
                ", gameState=" + gameState +
                ", turnNumber=" + turnNumber +
                ", countdownTime=" + countdownTime +
                ", stateMessage='" + stateMessage + '\'' +
                ", runningValue=" + runningValue +
                ", trueValue=" + trueValue +
                ", houseEdge=" + houseEdge +
                ", totalCards=" + totalCards +
                ", maxPlayers=" + maxPlayers +
                ", decksCount=" + decksCount +
                ", blackJackMultiplier=" + blackJackMultiplier +
                '}';
    }

    public void calculateCardValues() {
        this.playerValue = player.getHand().value;
        this.botValue = botPlayer.getHand().value;
        this.croupierValue = croupier.getHand().value;
        for(Card card : croupier.getHand().cards){
            if(card.isHidden()){
                this.croupierValue -= card.getValue();
            }
        }
    }

    public void resetCardValues() {
        this.playerValue = 0;
        this.botValue = 0;
        this.croupierValue = 0;
    }
}