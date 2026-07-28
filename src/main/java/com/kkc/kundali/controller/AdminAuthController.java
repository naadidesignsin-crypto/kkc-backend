package com.kkc.kundali.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class AdminAuthController {

    @GetMapping("/api/admin/auth/me")
    public Map<String, Object> me(Principal principal) {
        return Map.of(
                "authenticated", true,
                "username", principal != null ? principal.getName() : "admin",
                "role", "ADMIN"
        );
    }
}