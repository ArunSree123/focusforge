package com.focusforge.service;

import com.focusforge.common.ConflictException;
import com.focusforge.common.NotFoundException;
import com.focusforge.common.UnauthorizedException;
import com.focusforge.config.JwtService;
import com.focusforge.domain.AppUser;
import com.focusforge.dto.AuthDtos.*;
import com.focusforge.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UserSetupService setup;
    private final DemoDataService demo;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                       UserSetupService setup, DemoDataService demo) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.setup = setup;
        this.demo = demo;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        AppUser u = new AppUser();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u = users.save(u);
        setup.seedDefaults(u.getId());
        if (req.loadDemo()) demo.load(u.getId());
        return new AuthResponse(jwt.issue(u.getId()), toDto(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        AppUser u = users.findByEmailIgnoreCase(req.email().trim())
                .filter(found -> encoder.matches(req.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Incorrect email or password"));
        return new AuthResponse(jwt.issue(u.getId()), toDto(u));
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return toDto(find(userId));
    }

    @Transactional
    public UserDto updateSettings(Long userId, SettingsRequest req) {
        AppUser u = find(userId);
        u.setName(req.name().trim());
        u.setWakeTargetTime(req.wakeTargetTime());
        return toDto(users.save(u));
    }

    public AppUser find(Long userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("Account not found"));
    }

    private UserDto toDto(AppUser u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getWakeTargetTime());
    }
}
