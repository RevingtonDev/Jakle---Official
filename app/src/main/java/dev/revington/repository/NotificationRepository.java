package dev.revington.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;

import dev.revington.entity.Notification;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;

/**
 *
 * @author micha
 */ 
public interface NotificationRepository extends MongoRepository<Notification, String> {

    @Query("{ owner: ?0 }")
    List<Notification> findByOwner(String owner);

    @Aggregation(pipeline = {
            "{$match: {$expr: {$eq: [\"$owner\", ?0]}}}",
            "{$lookup: {from: \"users\", let: {\"id\": {$toString: \"$notification.second\"}}, pipeline: [{$match: {$expr: {$eq: [$$id, {$toString: \"$_id\"}]}}}, {$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"activity\", \"count\", \"time\", \"active\", \"id\"]}], as: \"values\"}}",
            "{$set:  {\"detail\": {$first: \"$values\"}}}",
            "{$unset: [\"owner\"]}"
    })
    Slice<Notification> findAllByOwner(String owner, Pageable pageable);
    
    @Aggregation(pipeline = {
            "{$match: {$expr: {$and: [{$eq: [\"$owner\", ?0], {$eq: [\"$category\", ?1]}}]}}}",
            "{$lookup: {from: \"users\", let: {\"id\": {$toString: \"$notification.second\"}}, pipeline: [{$match: {$expr: {$eq: [$$id, {$toString: \"$_id\"}]}}}, {$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"activity\", \"count\", \"time\", \"active\", \"id\"]}], as: \"values\"}}",
            "{$set:  {\"detail\": {$first: \"$values\"}}}",
            "{$unset: [\"owner\"]}"
    })
    Slice<Notification> findAllByOwnerWithFilter(String owner, String category, Pageable pageable);

    @Aggregation(pipeline = {
            "{$match: {$expr: {$and: [{$eq: [\"$owner\", ?0]}, {$eq: [\"$category\", ?1]}]}}}",
            "{$lookup: {from: \"users\", let: {\"id\": {$toString: \"$notification.second\"}}, pipeline: [{$match: {$expr: {$eq: [$$id, {$toString: \"$_id\"}]}}}, {$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"activity\", \"count\", \"time\", \"active\", \"id\"]}], as: \"values\"}}",
            "{$set:  {\"detail\": {$first: \"$values\"}}}",
            "{$unset: [\"owner\"]}"
    })
    Slice<Notification> findFilteredByOwner(String owner, String filter,Pageable pageable);

    @Aggregation(pipeline = {
            "{$match: {$expr: {$and: [{$eq: [\"$owner\", ?0]}, {$eq: [\"$read\", false]}]}}}",
            "{$lookup: {from: \"users\", let: {\"id\": {$toString: \"$notification.second\"}}, pipeline: [{$match: {$expr: {$eq: [$$id, {$toString: \"$_id\"}]}}}, {$unset: [\"email\",\"password\",\"socketId\",\"validity\",\"status\",\"attempts\", \"activity\", \"count\", \"time\", \"active\", \"id\"]}], as: \"values\"}}",
            "{$set:  {\"detail\": {$first: \"$values\"}}}",
            "{$unset: [\"owner\"]}"
    })
    List<Notification> findUnread(String owner);

    @Query(value = "{ category: \"F_R_A\", $or: [{owner: ?0, \"notification.second\": ?1}, {owner: ?1, \"notification.second\": ?0}] }", delete = true)
    void onDeleteFriend(String owner, String id);

    @Query(value = "{ category: \"F_R_\", $or: [{owner: ?0, \"notification.second\": ?1}, {owner: ?1, \"notification.second\": ?0}] }", delete = true)
    void onAcceptFriend(String owner, String id);

    // void updateAllFromOwner(String owner, boolean read);
     
}
