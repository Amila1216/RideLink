package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.UpdateRoleRequest;
import com.ridelink.account_service.dto.UpdateStatusRequest;
import com.ridelink.account_service.model.Account;
import com.ridelink.account_service.service.AccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(
            @RequestBody Account account) {

        try {
            Account savedAccount =
                    accountService.registerAccount(account);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedAccount);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest) {

        try {
            LoginResponse response =
                    accountService.login(loginRequest);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAccount(
            Authentication authentication) {

        return accountService
                .getAccountByEmail(authentication.getName())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("Account not found"));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentAccount(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {

        try {
            Account updatedAccount =
                    accountService.updateProfile(
                            authentication.getName(),
                            request
                    );

            return ResponseEntity.ok(updatedAccount);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable String id,
            @RequestBody UpdateRoleRequest request) {

        try {
            Account updatedAccount =
                    accountService.updateRole(
                            id,
                            request.getRole()
                    );

            return ResponseEntity.ok(updatedAccount);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request) {

        try {
            Account updatedAccount =
                    accountService.updateStatus(
                            id,
                            request.getStatus()
                    );

            return ResponseEntity.ok(updatedAccount);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}