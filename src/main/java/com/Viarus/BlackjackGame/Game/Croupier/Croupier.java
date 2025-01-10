package com.Viarus.BlackjackGame.Game.Croupier;


import com.Viarus.BlackjackGame.Cards.Card;
import com.Viarus.BlackjackGame.Cards.Hand;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Croupier {
    Hand hand;
    private int totalWinnings;
    private int totalLosings;

    //TODO: Load this value from properties
    //@Value("${game.settings.croupier.maxHitValue}")
    private int maxHitValue;

    public Croupier() {
        this.hand = new Hand();
        this.totalWinnings = 0;
        this.totalLosings = 0;
        this.maxHitValue = 16;
    }

    public void addLosings(int value){
        totalLosings += value;
    }

    public void addWinnings(int value){
        totalWinnings += value;
    }

    public String toString() {
        return "Croupier{" +
                "cards=" + hand +
                '}';
    }

    public void showCards() {
        for (Card card : hand.cards) {
            card.setHidden(false);
        }
    }
}
