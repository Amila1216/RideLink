package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.model.Account;
import com.ridelink.account_service.repository.AccountRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Register new account
    public Account registerAccount(Account account) {

        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        // Hash password before saving
        account.setPassword(
                passwordEncoder.encode(account.getPassword())
        );

        // Public registration can only create PASSENGER or DRIVER accounts
        if (account.getRole() == null ||
                account.getRole().isBlank()) {

            account.setRole("PASSENGER");

        } else {

            String role = account.getRole().toUpperCase();

            if (!role.equals("PASSENGER")
                    && !role.equals("DRIVER")) {

                throw new IllegalArgumentException(
                        "Registration role must be PASSENGER or DRIVER"
                );
            }

            account.setRole(role);
        }

        // New accounts are active by default
        account.setStatus("ACTIVE");

        return accountRepository.save(account);
    }

    // Login
    public LoginResponse login(LoginRequest loginRequest) {

        Account account = accountRepository
                .findByEmail(loginRequest.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        ));

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                account.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        // Suspended/disabled accounts cannot login
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }

        String token = jwtService.generateToken(account);

        return new LoginResponse(
                token,
                account.getEmail(),
                account.getRole()
        );
    }

    // Update logged-in user's profile
    public Account updateProfile(
            String email,
            UpdateProfileRequest request) {

        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        ));

        if (request.getName() != null
                && !request.getName().isBlank()) {

            account.setName(request.getName());
        }

        if (request.getPhone() != null
                && !request.getPhone().isBlank()) {

            account.setPhone(request.getPhone());
        }

        return accountRepository.save(account);
    }

    // ADMIN - Update account role
    public Account updateRole(
            String accountId,
            String role) {

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        ));

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Role is required"
            );
        }

        String normalizedRole = role
                .trim()
                .toUpperCase();

        if (!normalizedRole.equals("PASSENGER")
                && !normalizedRole.equals("DRIVER")
                && !normalizedRole.equals("ADMIN")) {

            throw new IllegalArgumentException(
                    "Invalid role. Allowed roles: PASSENGER, DRIVER, ADMIN"
            );
        }

        account.setRole(normalizedRole);

        return accountRepository.save(account);
    }

    // ADMIN - Update account status
    public Account updateStatus(
            String accountId,
            String status) {

        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        ));

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required"
            );
        }

        String normalizedStatus = status
                .trim()
                .toUpperCase();

        if (!normalizedStatus.equals("ACTIVE")
                && !normalizedStatus.equals("SUSPENDED")
                && !normalizedStatus.equals("DISABLED")) {

            throw new IllegalArgumentException(
                    "Invalid status. Allowed statuses: ACTIVE, SUSPENDED, DISABLED"
            );
        }

        account.setStatus(normalizedStatus);

        return accountRepository.save(account);
    }

    public Optional<Account> getAccountById(String id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return accountRepository.existsByEmail(email);
    }
}