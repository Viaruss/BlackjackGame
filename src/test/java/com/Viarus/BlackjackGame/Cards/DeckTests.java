package com.Viarus.BlackjackGame.Cards;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeckTests {
    @Test
    public void newDeckCount() {
        Deck SUT = new Deck();
        assertThat(SUT.getCards(), hasSize(52));
    }

    @Test
    public void newMultiDeckCount() {
        Deck SUT = new Deck(3);
        assertThat(SUT.getCards(), hasSize(156));
    }

    @Test
    public void shuffleDeck() {
        //given
        Deck SUT = new Deck();
        Deck notShuffledDeck = new Deck();

        //When
        Deck.shuffleDeck(SUT);

        //Then
        assertTrue(SUT.getCards().containsAll(notShuffledDeck.getCards()));
        assertFalse(SUT.getCards().equals(notShuffledDeck.getCards()));
    }
}
