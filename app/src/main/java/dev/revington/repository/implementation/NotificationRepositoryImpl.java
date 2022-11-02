package dev.revington.repository.implementation;

import dev.revington.entity.Notification;
import dev.revington.repository.AtomicNotificationRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
 
@Repository
public class NotificationRepositoryImpl implements AtomicNotificationRepository {
 
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public void updateUnreadNoifications(String owner) {
        Query query = new Query(Criteria.where("owner").is(owner));
        Update update = new Update();
        update.set("read", true);
        mongoTemplate.update(Notification.class).matching(query).apply(update).all();
    }
    
    
    
}
