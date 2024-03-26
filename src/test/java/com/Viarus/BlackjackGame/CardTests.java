package com.Viarus.BlackjackGame;

import com.Viarus.BlackjackGame.Deck.Cards.Card;
import com.Viarus.BlackjackGame.Deck.Cards.Ranks;
import com.Viarus.BlackjackGame.Deck.Cards.Suits;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CardTests {
    @Test
    public void testConstructor(){
        Card SUT = new Card(Suits.HEARTS, Ranks.ACE);

        assertThat(SUT.suit, is(Suits.HEARTS));
        assertThat(SUT.rank, is(Ranks.ACE));

    }
}
