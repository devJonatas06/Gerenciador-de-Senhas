package com.project.passwordmanager.auth.service;

import com.project.passwordmanager.auth.entity.User;
import com.project.passwordmanager.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
public class CustomDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug(
                "Security | Loading user details | email={}",
                username
        );

        User user = this.repository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn(
                            "Security | UserDetails not found | email={}",
                            username
                    );
                    return new UsernameNotFoundException("User Not Found");
                });

        log.debug(
                "Security | User details loaded | email={} | userId={}",
                username,
                user.getId()
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}