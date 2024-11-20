package com.Viarus.BlackjackGame.Table.Player;

import com.Viarus.BlackjackGame.Cards.Hand;
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
    private String currentTableId;
    String name;

    int balance;
    int totalWinnings;
    int totalLosings;

    PlayerActions currentAction;
    List<PlayerDecisions> availableDecisions;
    Hand hand;
    int bet;
    boolean canDouble;

    public Player(String name, int balance) {
        this.currentTableId = null;
        this.name = name;
        this.balance = balance;
        this.totalWinnings = 0;
        this.totalLosings = 0;
        currentAction = PlayerActions.WAITING;
        availableDecisions = List.of();
        this.hand = new Hand();
        this.bet = 0;
        this.canDouble = true;
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
                ", tableId='" + currentTableId + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", hand=" + hand +
                ", bet=" + bet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return id != null && id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}