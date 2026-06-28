package com.evaluate.ai.langchain.controller;

import com.evaluate.ai.langchain.model.LoginRequest;
import com.evaluate.ai.langchain.model.SignUpRequest;
import com.evaluate.ai.langchain.model.UserDetailsResponse;
import com.evaluate.ai.langchain.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<UserDetailsResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDetailsResponse> registerUser(@RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.registerUser(signUpRequest));
    }
}
