package com.focusforge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Please enter your name") @Size(max = 80) String name,
            @NotBlank(message = "Please enter your email") @Email(message = "Please enter a valid email") String email,
            @NotBlank(message = "Please choose a password") @Size(min = 8, max = 100, message = "Password must be 8-100 characters") String password,
            boolean loadDemo) {}

    public record LoginRequest(
            @NotBlank(message = "Please enter your email") @Email(message = "Please enter a valid email") String email,
            @NotBlank(message = "Please enter your password") String password) {}

    public record UserDto(Long id, String name, String email, LocalTime wakeTargetTime) {}

    public record AuthResponse(String token, UserDto user) {}

    public record SettingsRequest(
            @NotBlank(message = "Name is required") @Size(max = 80) String name,
            @NotNull(message = "Wake-up target is required") LocalTime wakeTargetTime) {}
}
