package com.project.passwordmanager.auth.infra.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 5;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        attemptsCache.remove(email);
    }

    public void loginFailed(String email) {
        attemptsCache.merge(email, 1, Integer::sum);
    }

    public boolean isBlocked(String email) {
        return attemptsCache.getOrDefault(email, 0) >= MAX_ATTEMPTS;
    }
}