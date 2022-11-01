package dev.revington.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import dev.revington.entity.User;
import java.util.List;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;

public interface UserRepository extends MongoRepository<User, String> {

    @Query("{'email': ?0}")
    Optional<User> findByEmail(String email);

    @Query(value = "{'_id' : ?0}", fields = "{email: 0, attempts: 0, socketId: 0, status: 0, validity: 0, password: 0}")
    Optional<User> findPersonal(String id);
    
    @Query(value = "{'_id' : ?0}", fields = "{created: 0, active: 0, dateOfBirth: 0,email: 0, attempts: 0, socketId: 0, status: 0, validity: 0, password: 0}")
    Optional<User> findInfo(String id);

    @Aggregation(pipeline = {
            "{$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"active\", \"created\"]}",
            "{$match: {$expr: {$ne: [ {$toString: \"$_id\"}, ?0 ]}}}",
            "{$lookup: {from: \"friends\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$or: [{$and: [{$eq: [\"$first\", $$id]}, {$eq: [\"$second\", ?0]}]}, {$and: [{$eq: [\"$second\", $$id]}, {$eq: [\"$first\", ?0]}]}]}}}], as: \"data\"}}",
            "{$lookup: {from: \"messages\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$and: [{$eq: [\"$from\", $$id]}, {$eq: [\"$to\", ?0]}]}}}], as: \"messages\"}}",
            "{$match: {$expr: {$gt: [{$size: \"$data\"}, 0]}}}",
            "{$set: {\"count\": {$size: \"$messages\"}}}"
            // "{$lookup: {from: \"friends\", localField: {$toString: \"$_id\"},
            // foreignField: \"first\", as: \"C\"}}"
    })
    List<User> findFriends(String id);

    @Aggregation(pipeline = {
            "{$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"created\"]}",
            "{$match: {$expr: {$and: [{$ne: [ {$toString: \"$_id\"}, ?0 ]}, {$eq: [\"$activity\", 1]}]}}}",
            "{$lookup: {from: \"friends\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$or: [{$and: [{$eq: [\"$first\", $$id]}, {$eq: [\"$second\", ?0]}]}, {$and: [{$eq: [\"$second\", $$id]}, {$eq: [\"$first\", ?0]}]}]}}}], as: \"data\"}}",
            "{$match: {$expr: {$gt: [{$size: \"$data\"}, 0]}}}"
            // "{$lookup: {from: \"friends\", localField: {$toString: \"$_id\"},
            // foreignField: \"first\", as: \"C\"}}"
    })
    List<User> findOnlineFriends(String id);

    @Aggregation(pipeline = {
            "{$unset: [\"email\",\"password\",\"validity\",\"status\",\"attempts\", \"created\"]}",
            "{$match: {$expr: {$and: [{$ne: [ {$toString: \"$_id\"}, ?0 ]}, {$eq: [\"$activity\", 1]}]}}}",
            "{$lookup: {from: \"friends\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$or: [{$and: [{$eq: [\"$first\", $$id]}, {$eq: [\"$second\", ?0]}]}, {$and: [{$eq: [\"$second\", $$id]}, {$eq: [\"$first\", ?0]}]}]}}}], as: \"data\"}}",
            "{$match: {$expr: {$gt: [{$size: \"$data\"}, 0]}}}"
            // "{$lookup: {from: \"friends\", localField: {$toString: \"$_id\"},
            // foreignField: \"first\", as: \"C\"}}"
    })
    List<User> findOnlineFriendsWithSocketId(String id);

    @Aggregation(pipeline = {
            "{$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"created\"]}",
            "{$match: {$expr: {$and: [{$ne: [ {$toString: \"$_id\"}, ?0 ]}, {$eq: [\"$activity\", 0]}]}}}",
            "{$lookup: {from: \"friends\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$or: [{$and: [{$eq: [\"$first\", $$id]}, {$eq: [\"$second\", ?0]}]}, {$and: [{$eq: [\"$second\", $$id]}, {$eq: [\"$first\", ?0]}]}]}}}], as: \"data\"}}",
            "{$match: {$expr: {$gt: [{$size: \"$data\"}, 0]}}}"
            // "{$lookup: {from: \"friends\", localField: {$toString: \"$_id\"},
            // foreignField: \"first\", as: \"C\"}}"
    })
    List<User> findOfflineFriends(String id);

    @Aggregation(pipeline = {
            "{$match: {$expr: {$and: [{$eq: [ {$toString: \"$_id\"}, ?1 ]}]}}}",
            "{$lookup: {from: \"friends\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$or: [{$and: [{$eq: [\"$first\", $$id]}, {$eq: [\"$second\", ?0]}]}, {$and: [{$eq: [\"$second\", $$id]}, {$eq: [\"$first\", ?0]}]}]}}}], as: \"data\"}}",
            "{$match: {$expr: {$gt: [{$size: \"$data\"}, 0]}}}"
    })
    User findFriend(String id, String friend);

    @Aggregation(pipeline = {
            "{$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"activity\", \"created\"]}",
            "{$match: {$expr: {$ne: [ {$toString: \"$_id\"}, ?0 ]}}}",
            "{$match: {$expr: {$or: [{$regexMatch: {input: \"$name\", regex: ?1, options: i}}, {$regexMatch: {input: \"$email\", regex: ?1, options: i}}]}}}",
            "{$lookup: {from: \"friends\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$or: [{$and: [{$eq: [\"$first\", $$id]}, {$eq: [\"$second\", ?0]}]}, {$and: [{$eq: [\"$second\", $$id]}, {$eq: [\"$first\", ?0]}]}]}}}], as: \"friend\"}}",
            "{$lookup: {from: \"requests\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$and: [{$eq: [\"$from\", ?0]}, {$eq: [\"$to\", $$id]}]}}}], as: \"requested\"}}",
            "{$lookup: {from: \"requests\", let: {\"id\": {$toString: \"$_id\"}}, pipeline: [{$match: {$expr: {$and: [{$eq: [\"$to\", ?0]}, {$eq: [\"$from\", $$id]}]}}}], as: \"pending\"}}",
            "{$set: {\"type\": {$cond: [{$gt: [{$size: \"$friend\"}, 0]}, \"friend\", {$cond: [{$gt: [{$size: \"$requested\"}, 0]}, \"requested\", {$cond: [{$gt: [{$size: \"$pending\"}, 0]}, \"pending\", \"stranger\"]}]}]}}}"
    })
    Slice<User> queryPeople(String id, String value, Pageable pageable);

}
