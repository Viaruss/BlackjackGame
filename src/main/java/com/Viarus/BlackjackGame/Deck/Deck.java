package com.Viarus.BlackjackGame.Deck;

import com.Viarus.BlackjackGame.Deck.Cards.Card;
import com.Viarus.BlackjackGame.Deck.Cards.Ranks;
import com.Viarus.BlackjackGame.Deck.Cards.Suits;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private final ArrayList<Card> cards;
    public Deck(){
        this.cards = new ArrayList<>();
        for(Suits suit : Suits.values()){
            for(Ranks rank : Ranks.values()){
                cards.add(new Card(suit, rank));
            }
        }
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void shuffleDeck(){
        Collections.shuffle(this.cards);
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (Card card : cards){
            temp.append(card).append("\n");
        }
        return temp.toString();
    }
}
