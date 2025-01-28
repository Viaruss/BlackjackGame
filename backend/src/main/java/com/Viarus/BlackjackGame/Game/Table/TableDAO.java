package com.Viarus.BlackjackGame.Game.Table;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TableDAO extends MongoRepository<Table, String> {
}
