package com.lion.bank.controller;

import com.lion.bank.dto.AuthRequest;
import com.lion.bank.security.JwtService;
import com.lion.bank.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody AuthRequest authRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        return ResponseEntity.ok(Map.of("token", jwtService.generateToken(authRequest.getEmail())));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        authService.register(request.get("name"), request.get("email"), request.get("password"));
        return ResponseEntity.ok(Map.of("message", "Usuario registrado. Se ha enviado un código de verificación a tu correo."));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de verificación es requerido");
        }
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Cuenta verificada exitosamente"));
    }
}
