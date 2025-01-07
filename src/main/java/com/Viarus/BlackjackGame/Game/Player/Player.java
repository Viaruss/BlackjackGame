package com.Viarus.BlackjackGame.Game.Player;

import com.Viarus.BlackjackGame.Cards.Hand;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerActions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerDecisions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerRoundResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@NoArgsConstructor
@Getter
@Setter
public class Player {
    @Id
    @Setter(lombok.AccessLevel.NONE)
    String id;

    public String currentTableId;
    String name;

    int balance;
    int learningBalance;
    int totalWinnings;
    int totalLosings;

    PlayerActions currentAction;
    List<PlayerDecisions> availableDecisions;
    Hand hand;
    int bet;

    boolean isPlaying;
    PlayerRoundResult lastRoundResult;

    public Player(String name, int balance) {
        this.currentTableId = null;
        this.name = name;
        this.balance = balance;
        this.learningBalance = 100000;
        this.totalWinnings = 0;
        this.totalLosings = 0;
        currentAction = PlayerActions.WAITING;
        availableDecisions = List.of();
        this.hand = new Hand();
        this.bet = 0;
        this.isPlaying = false;
        this.lastRoundResult = PlayerRoundResult.DRAW;
    }

    public void placeBet(int amount) {
        if (amount <= balance) {
            balance -= amount;
            bet += amount;
        }
    }

    public void addBalance(int value) {
        balance += value;
    }

    public void addWinnings(int value) {
        totalWinnings += value;
    }

    public void addLosings(int value) {
        totalLosings += value;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", currentTableId='" + currentTableId + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", learningBalance=" + learningBalance +
                ", totalWinnings=" + totalWinnings +
                ", totalLosings=" + totalLosings +
                ", currentAction=" + currentAction +
                ", availableDecisions=" + availableDecisions +
                ", hand=" + hand +
                ", bet=" + bet +
                ", isPlaying=" + isPlaying +
                ", lastRoundResult=" + lastRoundResult +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return id != null && id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public void evaluatePossibleDecisions() {
        availableDecisions.clear();
        if (isPlaying) {
            if (hand.value == 21) {
                availableDecisions = List.of(PlayerDecisions.STAND);
            } else if (hand.cards.size() == 2 && balance >= bet) {
                availableDecisions = List.of(PlayerDecisions.HIT, PlayerDecisions.STAND, PlayerDecisions.DOUBLE);
            } else if (hand.value < 21) {
                availableDecisions = List.of(PlayerDecisions.HIT, PlayerDecisions.STAND);
            }
        }

    }

    public void leaveTable() {
        this.setCurrentTableId(null);
        this.setBet(0);
        this.hand = new Hand();
        this.currentAction = PlayerActions.WAITING;
        this.isPlaying = false;
    }
}