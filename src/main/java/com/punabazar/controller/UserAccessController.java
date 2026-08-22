package com.punabazar.controller;

import com.punabazar.model.AppUser;
import com.punabazar.service.UserAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-access")
public class UserAccessController {

    private final UserAccessService userAccessService;

    public UserAccessController(UserAccessService userAccessService) {
        this.userAccessService = userAccessService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String name = payload.get("name");
        String phone = payload.get("phone");
        String password = payload.get("password");
        Map<String, Object> result = userAccessService.processUserLogin(name, phone, password);

        if (Boolean.TRUE.equals(result.get("success"))) {
            String role = (String) result.get("role");
            List<SimpleGrantedAuthority> authorities;
            String principalName;

            if ("ADMIN".equalsIgnoreCase(role)) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                principalName = name != null && !name.trim().isEmpty() ? name.trim() : "POONA@B456";
            } else {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                principalName = name != null && !name.trim().isEmpty() ? name.trim() : "User";
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    principalName, null, authorities
            );
            SecurityContext sc = SecurityContextHolder.createEmptyContext();
            sc.setAuthentication(auth);
            SecurityContextHolder.setContext(sc);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
            session.setAttribute("user_phone", phone);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/verify-session")
    public ResponseEntity<Map<String, Object>> verifySession(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If Admin is logged in, always allowed!
        if (auth != null && auth.getAuthorities() != null &&
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))) {
            return ResponseEntity.ok(Map.of("allowed", true, "message", "Admin Session"));
        }

        String phone = (String) request.getSession().getAttribute("user_phone");
        Map<String, Object> verification = userAccessService.verifyUserSession(phone);
        return ResponseEntity.ok(verification);
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(userAccessService.getSystemSettings());
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, String> payload) {
        String startTime = payload.get("startTime");
        String endTime = payload.get("endTime");
        String commonPassword = payload.get("commonPassword");
        String adminPassword = payload.get("adminPassword");
        userAccessService.updateSystemSettings(startTime, endTime, commonPassword, adminPassword);
        return ResponseEntity.ok(userAccessService.getSystemSettings());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(userAccessService.getAllUsers());
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<AppUser> approveUser(@PathVariable Long id) {
        return ResponseEntity.ok(userAccessService.approveUser(id));
    }

    @PostMapping("/users/{id}/pause")
    public ResponseEntity<AppUser> togglePauseUser(@PathVariable Long id) {
        return ResponseEntity.ok(userAccessService.togglePauseUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAccessService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
