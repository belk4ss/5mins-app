package com.fivemins.web;

import com.fivemins.dto.Dtos.*;
import com.fivemins.model.*;
import com.fivemins.repo.UserRepository;
import com.fivemins.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        if (users.existsByEmail(r.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        User u = new User();
        u.setName(r.name());
        u.setEmail(r.email().toLowerCase());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(Role.STUDENT);
        users.save(u);
        return response(u);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        User u = users.findByEmail(r.email().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        return response(u);
    }

    private AuthResponse response(User u) {
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getName(), u.getEmail(), u.getRole().name());
    }
}
