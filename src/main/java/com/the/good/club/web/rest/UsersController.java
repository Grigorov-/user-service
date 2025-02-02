package com.the.good.club.web.rest;

import com.the.good.club.core.service.UserPermissionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UsersController {

    private final UserPermissionService userPermissionService;

    public UsersController(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    @PostMapping("/users")
    public Map<String, String> registerUser(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "bozhidar.g.grigorov@gmail.com");
        userPermissionService.requestCorrelation(email);

        return body;
    }
}
