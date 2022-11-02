package dev.revington.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import dev.revington.entity.Token;
import org.springframework.data.mongodb.repository.Query; 

public interface TokenRepository extends MongoRepository<Token, String> {

    @Query(value = "{ token: ?0 }")
    Token findByToken(String token);

    @Query(value = "{clientId: ?0, grants: ?#{[1]}}")
    Token findTokenByOwner(String id, int grants);
    
    @Query(value = "{email: ?0, grants: ?#{[1]}}")
    Token findTokenByEmail(String email, int grants);
    
    void deleteAllByClientId(String clientId);

    void deleteByToken(String token);
}
