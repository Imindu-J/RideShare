package edu.carpool.carpool_rideshare.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.exception.ResourceNotFoundException;
import edu.carpool.carpool_rideshare.repository.UserRepository;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

}
