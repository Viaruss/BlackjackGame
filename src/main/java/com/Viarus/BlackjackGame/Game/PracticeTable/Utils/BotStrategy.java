package com.Viarus.BlackjackGame.Game.PracticeTable.Utils;

import com.Viarus.BlackjackGame.Game.Cards.Card;
import com.Viarus.BlackjackGame.Game.Cards.Hand;

public class BotStrategy {

    public static int calculateBet(double trueValue) {
        int bet = 100;
        if (trueValue > 0) {
            bet += (int) (trueValue * bet);
        } else if (trueValue < 0) {
            bet = 25;
        }
        return bet - (bet % 5);
    }

    public enum Decision {
        HIT, STAND, DOUBLE
    }

    public static Decision decide(Hand playerHand, Card croupierCard, boolean canDouble) {
        int playerValue = playerHand.value;
        boolean isSoft = playerHand.aceCount > 1;

        int croupierValue = Math.min(croupierCard.value, 10);

        if (playerValue >= 17) {
            return Decision.STAND;
        }

        if (playerValue <= 8) {
            return Decision.HIT;
        }

        return switch (playerValue) {
            case 9 -> (canDouble && croupierValue >= 3 && croupierValue <= 6) ? Decision.DOUBLE : Decision.HIT;
            case 10, 11 -> canDouble ? Decision.DOUBLE : Decision.HIT;
            default ->
                    isSoft ? handleSoftHands(playerValue, croupierValue, canDouble) : handleHardHands(playerValue, croupierValue);
        };

    }

    private static Decision handleSoftHands(int playerValue, int croupierValue, boolean canDouble) {
        switch (playerValue) {
            case 19:
            case 20:
                return Decision.STAND;
            case 18:
                if (croupierValue >= 3 && croupierValue <= 6 && canDouble) {
                    return Decision.DOUBLE;
                } else if (croupierValue == 2 || croupierValue >= 7) {
                    return Decision.STAND;
                }
                break;
            default:
                if (canDouble && croupierValue >= 4 && croupierValue <= 6) {
                    return Decision.DOUBLE;
                }
        }
        return Decision.HIT;
    }

    private static Decision handleHardHands(int playerValue, int croupierValue) {
        if (playerValue >= 12 && playerValue <= 16) {
            return (croupierValue >= 2 && croupierValue <= 6) ? Decision.STAND : Decision.HIT;
        }
        return Decision.HIT;
    }
}
