/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.revington.repository;

import dev.revington.entity.Message;
import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository; 
import org.springframework.data.mongodb.repository.Query;

/**
 *
 * @author micha
 */
public interface MessageRepository extends MongoRepository<Message, String> {
    
    @Aggregation(pipeline = {
        "{$match: {$expr: {$or: [{$eq: [?0, \"$to\"]}, {$eq: [?0, \"$from\"]}]}}}",
        "{$set: {\"_id\": {$cond: [{$eq: [?0, \"$to\"]}, \"$from\", \"$to\"]}}}",
        "{$set: {\"send\": {$cond: {if: {$eq: [?0, \"$from\"]}, then: true, else: false}}}}",
        "{$unset: [\"from\", \"to\", \"time\"]}"
    })
    List<Message> findMessages(String id);
    
    int countByFromAndTo(String from, String to);
    
    void deleteByFromAndTo(String from, String to);
    
    @Query(value="{$expr: {$or: [{$and: [{$eq: [?0, \"$to\"]}, {$eq: [?1, \"$from\"]}]}, {$and: [{$eq: [?1, \"$to\"]}, {$eq: [?0, \"$from\"]}]}]}}", delete = true)
    void deleteByFromAndToOrToAndFrom(String from, String to);
}
