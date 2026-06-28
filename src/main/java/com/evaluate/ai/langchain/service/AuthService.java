package com.evaluate.ai.langchain.service;

import com.evaluate.ai.langchain.entity.AppUser;
import com.evaluate.ai.langchain.model.LoginRequest;
import com.evaluate.ai.langchain.model.SignUpRequest;
import com.evaluate.ai.langchain.model.UserDetailsResponse;
import com.evaluate.ai.langchain.repository.AppUserRepository;
import com.evaluate.ai.langchain.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public UserDetailsResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken((UserDetails) authentication.getPrincipal());
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(loginRequest.getUsername());
        if (appUserOptional.isPresent()) {
            UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
            userDetailsResponse.setId(appUserOptional.get().getId());
            userDetailsResponse.setName(appUserOptional.get().getName());
            userDetailsResponse.setUsername(appUserOptional.get().getUsername());
            userDetailsResponse.setEmail(appUserOptional.get().getEmail());
            userDetailsResponse.setCreatedAt(appUserOptional.get().getCreatedAt());
            userDetailsResponse.setJwtToken(token);
            return userDetailsResponse;
        }
        return null;
    }

    public UserDetailsResponse registerUser(SignUpRequest signUpRequest) {
        if (appUserRepository.findByUsername(signUpRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        if (appUserRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email Address already in use!");
        }

        AppUser user = AppUser.builder()
                .name(signUpRequest.getName())
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build();

        AppUser savedUser = appUserRepository.save(user);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        userDetailsResponse.setId(savedUser.getId());
        userDetailsResponse.setName(savedUser.getName());
        userDetailsResponse.setUsername(savedUser.getUsername());
        userDetailsResponse.setEmail(savedUser.getEmail());
        userDetailsResponse.setCreatedAt(savedUser.getCreatedAt());
        return userDetailsResponse;
    }
}
