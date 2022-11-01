package dev.revington.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import dev.revington.entity.Friend;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface FriendRepository extends MongoRepository<Friend, String> {

    @Query("{ $or: [{friends : ?0}, {friends : ?1}] }")
    Optional<Friend> findByFriend(String S, String K);

    @Query(value = "{$expr: {$or: [{$eq: [\"$friends\", ?0]}, {$eq: [\"$friends\", ?1]}]}}", delete = true)
    void deleteFriend(String _1, String _2);

}