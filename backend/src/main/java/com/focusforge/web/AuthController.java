package com.focusforge.web;

import com.focusforge.dto.AuthDtos.*;
import com.focusforge.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.focusforge.common.CurrentUser.id;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) { return auth.register(req); }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) { return auth.login(req); }

    @GetMapping("/auth/me")
    public UserDto me() { return auth.me(id()); }

    @GetMapping("/settings")
    public UserDto settings() { return auth.me(id()); }

    @PutMapping("/settings")
    public UserDto updateSettings(@Valid @RequestBody SettingsRequest req) { return auth.updateSettings(id(), req); }
}
