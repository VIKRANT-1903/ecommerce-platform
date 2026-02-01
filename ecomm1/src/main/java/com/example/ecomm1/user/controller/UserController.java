package com.example.ecomm1.user.controller;

import com.example.ecomm1.common.dto.ApiResponse;
import com.example.ecomm1.user.dto.UpdateUserRequest;
import com.example.ecomm1.user.dto.UserProfileResponse;
import com.example.ecomm1.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        log.debug("Fetching current user profile");
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.ok(profile, "User profile fetched", "/users/me"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating current user profile");
        UserProfileResponse updatedProfile = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(ApiResponse.ok(updatedProfile, "User profile updated", "/users/me"));
    }
}
