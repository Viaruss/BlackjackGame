package com.Viarus.BlackjackGame.Table;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TableDAO extends MongoRepository<Table, String> {
}
