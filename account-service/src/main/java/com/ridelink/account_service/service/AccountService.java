package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
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

    public AccountService(AccountRepository accountRepository,PasswordEncoder passwordEncoder,JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Account registerAccount(Account account) {

        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        account.setPassword(passwordEncoder.encode(account.getPassword()));

        if (account.getRole() == null || account.getRole().isBlank()) {
            account.setRole("PASSENGER");
        }

        if (account.getStatus() == null || account.getStatus().isBlank()) {
            account.setStatus("ACTIVE");
        }

        return accountRepository.save(account);
    }

    public LoginResponse login(LoginRequest loginRequest) {

        Account account = accountRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                account.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalArgumentException("Account is not active");
        }

        String token = jwtService.generateToken(account);

        return new LoginResponse(
                token,
                account.getEmail(),
                account.getRole()
        );
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