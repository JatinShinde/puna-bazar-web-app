package com.punabazar.service;

import com.punabazar.model.AppUser;
import com.punabazar.model.SystemSetting;
import com.punabazar.repository.AppUserRepository;
import com.punabazar.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserAccessService {

    public static final String KEY_START_TIME = "OPERATING_START_TIME";
    public static final String KEY_END_TIME = "OPERATING_END_TIME";
    public static final String KEY_COMMON_PASS = "COMMON_USER_PASSWORD";
    public static final String KEY_ADMIN_PASS = "ADMIN_PASSWORD";
    public static final String DEFAULT_ADMIN_PASS = "456B@POONA";
    public static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private final AppUserRepository userRepository;
    private final SystemSettingRepository settingRepository;

    public UserAccessService(AppUserRepository userRepository, SystemSettingRepository settingRepository) {
        this.userRepository = userRepository;
        this.settingRepository = settingRepository;
    }

    public String getSetting(String key, String defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public void setSetting(String key, String value) {
        Optional<SystemSetting> opt = settingRepository.findBySettingKey(key);
        SystemSetting setting = opt.orElseGet(() -> new SystemSetting(key, value));
        setting.setSettingValue(value);
        settingRepository.save(setting);
    }

    public Map<String, String> getSystemSettings() {
        Map<String, String> map = new HashMap<>();
        map.put("startTime", getSetting(KEY_START_TIME, "00:00"));
        map.put("endTime", getSetting(KEY_END_TIME, "18:00"));
        map.put("commonPassword", getSetting(KEY_COMMON_PASS, "123456"));
        map.put("adminPassword", getSetting(KEY_ADMIN_PASS, DEFAULT_ADMIN_PASS));
        map.put("isWithinWindow", String.valueOf(isCurrentTimeWithinWindow()));
        return map;
    }

    public void updateSystemSettings(String startTime, String endTime, String commonPassword, String adminPassword) {
        if (startTime != null && !startTime.trim().isEmpty()) {
            setSetting(KEY_START_TIME, startTime.trim());
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            setSetting(KEY_END_TIME, endTime.trim());
        }
        if (commonPassword != null && !commonPassword.trim().isEmpty()) {
            setSetting(KEY_COMMON_PASS, commonPassword.trim());
        }
        if (adminPassword != null && !adminPassword.trim().isEmpty()) {
            setSetting(KEY_ADMIN_PASS, adminPassword.trim());
        }
    }

    public boolean isCurrentTimeWithinWindow() {
        try {
            String startStr = getSetting(KEY_START_TIME, "00:00");
            String endStr = getSetting(KEY_END_TIME, "18:00");

            LocalTime start = parseTimeFlexible(startStr, LocalTime.of(0, 0));
            LocalTime end = parseTimeFlexible(endStr, LocalTime.of(18, 0));
            LocalTime now = LocalTime.now(INDIA_ZONE);

            if (start.equals(end)) {
                return true; // 24-hour open
            }

            if (start.isBefore(end)) {
                return !now.isBefore(start) && !now.isAfter(end);
            } else {
                // Overnight window e.g. 22:00 to 06:00
                return !now.isBefore(start) || !now.isAfter(end);
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private LocalTime parseTimeFlexible(String input, LocalTime defaultTime) {
        if (input == null || input.trim().isEmpty()) return defaultTime;
        String str = input.trim().toUpperCase();

        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("hh:mm a"),
            DateTimeFormatter.ofPattern("h:mm a"),
            DateTimeFormatter.ofPattern("hh:mm:ss a"),
            DateTimeFormatter.ofPattern("h:mm:ss a")
        };

        for (DateTimeFormatter f : formatters) {
            try {
                return LocalTime.parse(str, f);
            } catch (Exception ignored) {}
        }
        return defaultTime;
    }

    public Map<String, Object> processUserLogin(String name, String phone, String password) {
        Map<String, Object> response = new HashMap<>();

        // 1. CHECK IF ADMIN PASSWORD ENTERED (Strictly checks currently saved Admin Password)
        String currentAdminPass = getSetting(KEY_ADMIN_PASS, DEFAULT_ADMIN_PASS);
        if (password != null && currentAdminPass.equalsIgnoreCase(password.trim())) {
            response.put("success", true);
            response.put("role", "ADMIN");
            response.put("message", "ADMIN_LOGIN_SUCCESS");
            return response;
        }

        // 2. CHECK COMMON USER PASSWORD
        String expectedPass = getSetting(KEY_COMMON_PASS, "123456");
        if (password == null || !password.trim().equals(expectedPass)) {
            response.put("success", false);
            response.put("message", "INVALID_PASSWORD");
            response.put("error", "Incorrect Password. Please check your password.");
            return response;
        }

        String cleanPhone = phone != null ? phone.replaceAll("[^0-9]", "") : "";
        if (cleanPhone.isEmpty()) {
            response.put("success", false);
            response.put("message", "INVALID_PHONE");
            response.put("error", "Please enter a valid mobile number.");
            return response;
        }

        Optional<AppUser> userOpt = userRepository.findByPhone(cleanPhone);
        AppUser user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
            if (name != null && !name.trim().isEmpty()) {
                user.setName(name.trim());
                userRepository.save(user);
            }
        } else {
            user = new AppUser(name != null ? name.trim() : "User " + cleanPhone, cleanPhone, "PENDING");
            userRepository.save(user);
        }

        String status = user.getStatus();
        if ("PENDING".equalsIgnoreCase(status)) {
            response.put("success", false);
            response.put("status", "PENDING");
            response.put("message", "ACCESS_PENDING");
            response.put("error", "Access Request Submitted. Waiting for One-Time Admin Approval.");
            return response;
        }

        if ("PAUSED".equalsIgnoreCase(status)) {
            response.put("success", false);
            response.put("status", "PAUSED");
            response.put("message", "ACCESS_PAUSED");
            response.put("error", "Your access is currently paused by Admin.");
            return response;
        }

        if ("REJECTED".equalsIgnoreCase(status)) {
            response.put("success", false);
            response.put("status", "REJECTED");
            response.put("message", "ACCESS_REJECTED");
            response.put("error", "Your access request was rejected by Admin.");
            return response;
        }

        // Check Operating Time Window (IST Zone)
        if (!isCurrentTimeWithinWindow()) {
            String startStr = getSetting(KEY_START_TIME, "00:00");
            String endStr = getSetting(KEY_END_TIME, "18:00");
            response.put("success", false);
            response.put("status", "OUTSIDE_WINDOW");
            response.put("message", "OUTSIDE_OPERATING_HOURS");
            response.put("error", "Market view is currently closed. Allowed operating hours: " + startStr + " to " + endStr);
            return response;
        }

        response.put("success", true);
        response.put("role", "USER");
        response.put("status", "APPROVED");
        response.put("user", user);
        return response;
    }

    public Map<String, Object> verifyUserSession(String phone) {
        Map<String, Object> result = new HashMap<>();

        if (!isCurrentTimeWithinWindow()) {
            String startStr = getSetting(KEY_START_TIME, "00:00");
            String endStr = getSetting(KEY_END_TIME, "18:00");
            result.put("allowed", false);
            result.put("reason", "OUTSIDE_HOURS");
            result.put("message", "Market operating hours closed (" + startStr + " to " + endStr + "). Auto logging out...");
            return result;
        }

        if (phone != null && !phone.trim().isEmpty()) {
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            Optional<AppUser> userOpt = userRepository.findByPhone(cleanPhone);
            if (userOpt.isPresent()) {
                AppUser user = userOpt.get();
                if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {
                    result.put("allowed", false);
                    result.put("reason", "REVOKED");
                    result.put("message", "Your access has been paused or revoked by Admin.");
                    return result;
                }
            }
        }

        result.put("allowed", true);
        result.put("message", "Session OK");
        return result;
    }

    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    public AppUser approveUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        user.setStatus("APPROVED");
        user.setApprovedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public AppUser togglePauseUser(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        if ("PAUSED".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("APPROVED");
        } else {
            user.setStatus("PAUSED");
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
