package com.Viarus.BlackjackGame.Game.Cards;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;

@Getter
@Setter
public class Deck {
    private ArrayList<Card> cards;
    private int cardsLeft;
    private int cardsDealt;

    public Deck(){
        this.cards = new ArrayList<>();
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"C", "D", "H", "S"};
        for(String suit : suits){
            for(String rank : ranks){
                cards.add(new Card(suit, rank));
            }
        }
        this.cardsLeft = cards.size();
        this.cardsDealt = 0;
        Deck.shuffleDeck(this);
    }

    public Deck(int count){
        this.cards = new ArrayList<>();
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"C", "D", "H", "S"};
        for (int i = 0; i < count; i++){
            for(String suit : suits){
                for(String rank : ranks){
                    cards.add(new Card(suit, rank));
                }
            }
        }
        this.cardsLeft = cards.size();
        this.cardsDealt = 0;
        Deck.shuffleDeck(this);
    }

    static public void shuffleDeck(Deck deck){
        Collections.shuffle(deck.cards);
    }

    public Card dealCard(){
        Card cardToDeal = this.cards.get(this.cards.size()-1);
        this.cards.remove(this.cards.size()-1);
        this.cardsDealt++;
        this.cardsLeft--;
        return cardToDeal;
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
