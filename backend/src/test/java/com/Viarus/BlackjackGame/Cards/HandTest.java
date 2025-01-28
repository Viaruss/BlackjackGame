package com.Viarus.BlackjackGame.Cards;

import com.Viarus.BlackjackGame.Game.Cards.Card;
import com.Viarus.BlackjackGame.Game.Cards.Hand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HandTest {

    @Test
    public void testValue(){
        //Given
        Hand SUT = new Hand();

        //When
        SUT.addCard(new Card("D", "A"));
        SUT.addCard(new Card("S", "2"));

        //Then
        assertEquals(SUT.value, 13);
    }

    @Test
    public void testAceCount(){
        //Given
        Hand SUT = new Hand();

        //When
        SUT.addCard(new Card("D", "A"));
        SUT.addCard(new Card("S", "2"));

        //Then
        assertEquals(SUT.aceCount, 1);
    }

}