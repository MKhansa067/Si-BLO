package com.siblo.rent.controller;

import com.siblo.rent.dto.UserDTO;
import com.siblo.rent.entity.User;
import com.siblo.rent.entity.User.Role;
import com.siblo.rent.repository.UserRepository;
import com.siblo.rent.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider tokenProvider,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of(
            "token", token, "email", user.getEmail(),
            "name", user.getName(), "role", user.getRole().name(),
            "membershipTier", user.getMembershipTier()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (body.containsKey("name")) user.setName(body.get("name"));
        if (body.containsKey("email")) {
            String newEmail = body.get("email");
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail))
                return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
            user.setEmail(newEmail);
        }
        userRepository.save(user);
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (userRepository.existsByEmail(email))
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        User user = User.builder()
            .name(body.get("name")).email(email)
            .password(passwordEncoder.encode(body.get("password")))
            .role(Role.MEMBER).membershipTier("PREMIUM MEMBER").build();
        userRepository.save(user);
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of(
            "token", token, "email", user.getEmail(),
            "name", user.getName(), "role", user.getRole().name()
        ));
    }
}
