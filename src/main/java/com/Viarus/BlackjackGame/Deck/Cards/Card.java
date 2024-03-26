package com.Viarus.BlackjackGame.Deck.Cards;

public class Card {
    public Suits suit;
    public Ranks rank;

    public Card(Suits suit, Ranks rank) {
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
