package edu.carpool.carpool_rideshare.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.carpool.carpool_rideshare.dto.AuthResponse;
import edu.carpool.carpool_rideshare.dto.LoginRequest;
import edu.carpool.carpool_rideshare.dto.RegisterRequest;
import edu.carpool.carpool_rideshare.entity.User;
import edu.carpool.carpool_rideshare.entity.Role;
import edu.carpool.carpool_rideshare.repository.UserRepository;
import edu.carpool.carpool_rideshare.security.JwtService;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already registered");
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordhash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(request.getRoles() != null && !request.getRoles().isEmpty() ? request.getRoles() : Set.of(Role.RIDER));

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName());

    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName());
    }

}
