package com.ridelink.drivervehicle.common;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequences")
public class MongoSequence {

    @Id
    private String id;

    private long value;

    public MongoSequence() {
    }

    public long getValue() {
        return value;
    }
}
