package com.example.musicapi.security;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;

    private final Map<String, Queue<Long>> userRequests = new ConcurrentHashMap<>();

    public boolean tryConsume(String username) {
        long currentTime = System.currentTimeMillis();
        Queue<Long> requests = userRequests.computeIfAbsent(username, k -> new LinkedList<>());

        synchronized (requests) {
            // Remove requisições mais velhas que 1 minuto
            while (!requests.isEmpty() && currentTime - requests.peek() > MINUTE_IN_MILLIS) {
                requests.poll();
            }

            if (requests.size() < MAX_REQUESTS_PER_MINUTE) {
                requests.add(currentTime);
                return true;
            }
            return false;
        }
    }

    public void reset(String username) {
        userRequests.remove(username);
    }
}
