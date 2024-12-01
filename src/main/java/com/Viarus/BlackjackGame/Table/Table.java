package com.Viarus.BlackjackGame.Table;

import com.Viarus.BlackjackGame.Cards.Deck;
import com.Viarus.BlackjackGame.Cards.Hand;
import com.Viarus.BlackjackGame.Table.Croupier.Croupier;
import com.Viarus.BlackjackGame.Table.Player.Player;
import com.Viarus.BlackjackGame.Table.Player.PlayerDecisions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document
@Getter
@Setter
public class Table {
    @Id
    @Setter(AccessLevel.NONE)
    String id;

    ArrayList<Player> players;
    //TODO: Add loading from properties
    //@Value("${game.settings.table.maxPlayers}")
    private int maxPlayers = 3;

    //@Value("${game.settings.table.deckCount}")
    private int decksCount = 3;

    //@Value("${game.settings.table.blackJackMultiplier}")
    private int blackJackMultiplier = 3;

    private Croupier croupier;

    private Deck cardsInPlay;

    private boolean betsPlaceable;
    private boolean roundInProgress;
    private boolean croupierTurn;
    private int turnNumber;

    public Table() {
        this.players = new ArrayList<>();
        for (int i = 0; i < this.maxPlayers; i++) players.add(null);
        this.croupier = new Croupier();
        this.croupierTurn = false;
        this.betsPlaceable = false;
        this.roundInProgress = false;
        this.cardsInPlay = new Deck(decksCount);
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

    public Table startGame() {
        betsPlaceable = false;
        List<Player> currentPlayers = players.stream().filter(Objects::nonNull).toList();
        for (Player player : currentPlayers) {
            player.setHand(new Hand());
        }
        croupier.setHand(new Hand());

        for (int i = 0; i < 2; i++) {
            for (Player player : currentPlayers) {
                player.getHand().addCard(cardsInPlay.dealCard());
                player.setCanDouble(true);
            }
            croupier.getHand().addCard(cardsInPlay.dealCard());
            turnNumber = 0;
            croupierTurn = false;
            roundInProgress = true;
        }

        return this;
    }

    public void updatePlayer(Player updatedPlayer) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(updatedPlayer.getId())) {
                players.set(i, updatedPlayer);
                break;
            }
        }
    }

    //TODO: any buttons that correspond to invalid decisions should be greyed out on the front end
    // (maybe add availableDecision field to player?)
    public Table processPlayerDecision(Player player, PlayerDecisions decision) {
        if (players.indexOf(player) != turnNumber || player.getHand().isOver) {
            return null;
        }
        switch (decision) {
            case HIT -> {
                player.getHand().addCard(cardsInPlay.dealCard());
                if (player.getHand().isOver) {
                    player.setCanDouble(false);
                    turnNumber++;
                }
            }
            case STAY -> {
                player.setCanDouble(false);
                turnNumber++;
            }
            case DOUBLE -> { //TODO: nie do konca dziala, dobiera karte - nie podwaja stawki
                if (player.isCanDouble()) {
                    player.placeBet(player.getBet());
                    player.getHand().addCard(cardsInPlay.dealCard());
                    player.setCanDouble(false);
                    turnNumber++;
                }
            }
            //TODO: Split
            //case SPLIT -> {}
        }
        updatePlayer(player);

        if (turnNumber == players.size()) {
            croupierTurn = true;
        }
        return this;
    }

    public void croupierMove() {
        if (croupierTurn) {
            while (croupier.getHand().value <= croupier.getMaxHitValue()) {
                croupier.getHand().addCard(cardsInPlay.dealCard());
            }
        }
    }
}
