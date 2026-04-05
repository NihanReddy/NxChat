package com.chatflow.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.dao.DataAccessException;
import java.util.stream.Collectors;

@Service
public class GooglePeopleService {

    private static final String APPLICATION_NAME = "NxChat";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String REDIS_CONTACT_PREFIX = "contacts:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<String> getContactEmails(String accessToken, String userEmail) throws GeneralSecurityException, IOException {
        // 1. Check Cache
        String cacheKey = REDIS_CONTACT_PREFIX + userEmail;
        try {
            List<String> cachedContacts = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedContacts != null) {
                return cachedContacts;
            }
        } catch (DataAccessException | IllegalStateException ex) {
            // Redis is optional for contacts cache; continue with live Google data.
        }

        // 2. Fetch from Google
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

        PeopleService service = new PeopleService.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        ListConnectionsResponse response = service.people().connections()
                .list("people/me")
                .setPersonFields("emailAddresses,names")
                .execute();

        List<Person> connections = response.getConnections();
        List<String> emails = Collections.emptyList();

        if (connections != null && !connections.isEmpty()) {
            emails = connections.stream()
                    .filter(person -> person.getEmailAddresses() != null)
                    .flatMap(person -> person.getEmailAddresses().stream())
                    .map(email -> email.getValue())
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 3. Cache for 1 hour
        try {
            redisTemplate.opsForValue().set(cacheKey, emails, 1, TimeUnit.HOURS);
        } catch (DataAccessException | IllegalStateException ex) {
            // Ignore cache write failures so the contact sync still works without Redis.
        }

        return emails;
    }
}
