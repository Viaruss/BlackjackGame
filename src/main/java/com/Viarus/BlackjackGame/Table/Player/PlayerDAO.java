package com.Viarus.BlackjackGame.Table.Player;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerDAO extends MongoRepository<Player, String> {
    Player findPlayerByName(String name);
}
