package com.Viarus.BlackjackGame.Game.Player;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerDAO extends MongoRepository<Player, String> {
    Player findPlayerByName(String name);
}
