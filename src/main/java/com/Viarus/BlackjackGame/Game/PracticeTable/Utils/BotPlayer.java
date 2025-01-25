package com.Viarus.BlackjackGame.Game.PracticeTable.Utils;

import com.Viarus.BlackjackGame.Game.Cards.Hand;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerActions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerDecisions;
import com.Viarus.BlackjackGame.Game.Player.Utils.PlayerRoundResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class BotPlayer {
    public String currentTableId;
    String name;
    int balance;

    PlayerActions currentAction;
    List<PlayerDecisions> availableDecisions;
    Hand hand;
    int bet;

    boolean isPlaying;
    PlayerRoundResult lastRoundResult;

    ArrayList<BotStrategy.Decision> lastDecisions;

    public BotPlayer(String name, String currentTableId, int balance) {
        this.currentTableId = currentTableId;
        this.name = name;
        this.balance = balance;
        currentAction = PlayerActions.WAITING;
        availableDecisions = List.of();
        this.hand = new Hand();
        this.bet = 0;
        this.isPlaying = false;
        this.lastRoundResult = PlayerRoundResult.DRAW;
        this.lastDecisions = new ArrayList<>();
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

    @Override
    public String toString() {
        return "BotPlayer{" +
                "currentTableId='" + currentTableId + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                ", currentAction=" + currentAction +
                ", availableDecisions=" + availableDecisions +
                ", hand=" + hand +
                ", bet=" + bet +
                ", isPlaying=" + isPlaying +
                ", lastRoundResult=" + lastRoundResult +
                '}';
    }
}