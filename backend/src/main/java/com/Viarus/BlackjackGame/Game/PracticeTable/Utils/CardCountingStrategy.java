package com.Viarus.BlackjackGame.Game.PracticeTable.Utils;

import com.Viarus.BlackjackGame.Game.Cards.Card;

import java.util.List;

public class CardCountingStrategy {

    public static final double BASE_HOUSE_EDGE = 0.005;
    public static final double IMPACT_PER_TRUE_VALUE = 0.005;

    public static int updateRunningValue(int runningValue, List<Card> dealtCards) {
        for (Card card : dealtCards) {
            runningValue += getCardCountValue(card);
        }
        return runningValue;
    }

    public static double calculateTrueValue(int runningValue, int cardsLeft) {
        int remainingDecks = Math.max(cardsLeft / 52, 1);
        return (double) runningValue / remainingDecks;
    }

    private static int getCardCountValue(Card card) {
        int rank = card.value;
        if (rank >= 2 && rank <= 6) {
            return +1;
        } else if (rank == 10 || rank == 11) {
            return -1;
        } else {
            return 0;
        }
    }
}
