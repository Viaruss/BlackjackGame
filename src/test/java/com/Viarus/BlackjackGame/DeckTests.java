package com.Viarus.BlackjackGame;

import com.Viarus.BlackjackGame.Deck.Cards.Card;
import com.Viarus.BlackjackGame.Deck.Cards.Ranks;
import com.Viarus.BlackjackGame.Deck.Cards.Suits;
import com.Viarus.BlackjackGame.Deck.Deck;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DeckTests {
    @Test
    public void newDeckCount(){
        Deck SUT = new Deck();

        assertThat(SUT.getCards(), hasSize(52));
    }

    @Test
    public void shuffleDeckSize(){
        Deck SUT = new Deck();
        SUT.shuffleDeck();

        assertThat(SUT.getCards(), hasSize(52));
    }


}
