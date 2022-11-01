package dev.revington.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import dev.revington.entity.Request;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface RequestRepository extends MongoRepository<Request, String> {

    @Query("{ $or: [{'from': ?0, 'to': ?1}, {'from': ?1, 'to': ?0}] }")
    Optional<Request> findByFromAndTo(String S, String K);

    @Query("{from: ?0, to: ?1}")
    Optional<Request> findByFrom(String id, String clientId);

    void deleteByFromAndTo(String from, String to);
}
