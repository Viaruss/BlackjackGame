package com.Viarus.BlackjackGame.Table;

import com.Viarus.BlackjackGame.Table.Player.Player;
import com.Viarus.BlackjackGame.Table.Croupier.Croupier;

import java.util.ArrayList;

public class Table {
    ArrayList<Player> players;
    private final int maxPlayers = 5;
    private final Croupier croupier = new Croupier();
}
