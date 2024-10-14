package com.Viarus.BlackjackGame.Cards;

import java.util.ArrayList;
import java.util.List;

public class Hand {
    public List<Card> cards;
    public int value;
    public int aceCount;
    public boolean isOver;

    public Hand() {
        this.cards = new ArrayList<>();
        this.value = 0;
        this.aceCount = 0;
        this.isOver = false;
    }

    public void addCard(Card card){
        this.cards.add(card);
        value += card.value;
        aceCount += card.isAce() ? 1 : 0;
        if(value > 21){
            if (aceCount > 0){
                value -= 10;
                aceCount--;
            } else {
                isOver = true;
            }
        }
    }

    @Override
    public String toString() {
        return "Hand{" +
                "cards=" + cards +
                ", value=" + value +
                ", aceCount=" + aceCount +
                '}';
    }
}
