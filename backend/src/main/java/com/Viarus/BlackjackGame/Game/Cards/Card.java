package com.Viarus.BlackjackGame.Game.Cards;

import com.sun.jdi.InternalException;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Card {
    public String suit;
    public String rank;
    public int value;
    public boolean isHidden = false;

    public Card(String suit, String rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = calculateValue(rank);
    }

    private int calculateValue(String rank) {
        if (rank.equals("A")) return 11;
        return "JQK".contains(rank) ?
                10 :  Integer.parseInt(rank);
    }

    public String getCode() {
        return rank + "-" + suit;
    }

    @Override
    public String toString() {
        String fullRank = switch(this.rank){
            case "A": yield "Ace";
            case "2": yield "Two";
            case "3": yield "Three";
            case "4": yield "Four";
            case "5": yield "Five";
            case "6": yield "Six";
            case "7": yield "Seven";
            case "8": yield "Eight";
            case "9": yield "Nine";
            case "10": yield "Ten";
            case "J": yield "Jack";
            case "Q": yield "Queen";
            case "K": yield "King";
            default: throw new InternalException("Invalid card rank");
        };

        String fullSuit = switch (this.suit){
            case "C": yield "Clubs";
            case "D": yield "Diamonds";
            case "H": yield "Hearts";
            case "S": yield "Spades";
            default: throw new InternalException("Invalid card suit");
        };
        return "%s of %s".formatted(fullRank, fullSuit);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;
        return value == card.value &&
                suit.equals(card.suit) &&
                rank.equals(card.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank, value);
    }

    public boolean isAce() {
        return this.rank.equals("A");
    }

    public Card hide() {
        this.isHidden = true;
        return this;
    }
}
