package com.Viarus.BlackjackGame.Cards;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardTests {
    @Test
    public void testConstructor() {
        //Given
        Card SUT = new Card("H", "A");

        //When

        //Then
        assertThat(SUT.suit, is("H"));
        assertThat(SUT.rank, is("A"));
    }

    @Test
    public void testCalculateValue() {
        //Given
        Card card1 = new Card("H", "A");
        Card card2 = new Card("D", "6");
        Card card3 = new Card("S", "10");
        Card card4 = new Card("C", "Q");

        //When

        //Then
        assertThat(card1.value, is(11));
        assertThat(card2.value, is(6));
        assertThat(card3.value, is(10));
        assertThat(card4.value, is(10));
    }

    @Test
    public void testGetCode() {
        //Given
        Card card1 = new Card("H", "A");
        Card card2 = new Card("D", "6");
        Card card3 = new Card("S", "10");
        Card card4 = new Card("C", "Q");

        //When

        //Then
        assertThat(card1.getCode(), is("A-H"));
        assertThat(card2.getCode(), is("6-D"));
        assertThat(card3.getCode(), is("10-S"));
        assertThat(card4.getCode(), is("Q-C"));
    }

    @Test
    public void testIsAce(){
        //Given
        Card SUT = new Card("C", "A");

        //When

        //Then
        assertTrue(SUT.isAce());
    }
}
