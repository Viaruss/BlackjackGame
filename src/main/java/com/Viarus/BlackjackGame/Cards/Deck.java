package com.Viarus.BlackjackGame.Cards;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;

@Document
@Getter
@Setter
public class Deck {
    @Id
    @Setter(lombok.AccessLevel.NONE)
    String id;

    private ArrayList<Card> cards;
    public Deck(){
        //Default deck
        this.cards = new ArrayList<>();
        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] suits = {"C", "D", "H", "S"};
        for(String suit : suits){
            for(String rank : ranks){
                cards.add(new Card(suit, rank));
            }
        }
        Deck.shuffleDeck(this);
    }

    public Deck(int count){
        //Multi-deck deck of cards
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
        Deck.shuffleDeck(this);
    }

    static public void shuffleDeck(Deck deck){
        Collections.shuffle(deck.cards);
    }

    public Card dealCard(){
        Card cardToDeal = this.cards.get(this.cards.size()-1);
        this.cards.remove(this.cards.size()-1);
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
