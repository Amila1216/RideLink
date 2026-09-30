package com.ridelink.ride_service.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/ridelink}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:ridelink}")
    private String defaultDatabaseName;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        ConnectionString connectionString = new ConnectionString(mongoUri);

        String databaseName = connectionString.getDatabase();

        if (databaseName == null || databaseName.isBlank()) {
            databaseName = defaultDatabaseName;
        }

        return new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
    }
}