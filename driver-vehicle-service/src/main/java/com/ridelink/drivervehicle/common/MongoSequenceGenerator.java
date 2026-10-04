package com.ridelink.drivervehicle.common;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class MongoSequenceGenerator {

    private final MongoTemplate mongoTemplate;

    public MongoSequenceGenerator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long nextSequence(String sequenceName) {
        MongoSequence sequence = mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(sequenceName)),
                new Update().inc("value", 1),
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                MongoSequence.class
        );

        if (sequence == null) {
            throw new IllegalStateException("Failed to generate sequence: " + sequenceName);
        }
        return sequence.getValue();
    }
}
