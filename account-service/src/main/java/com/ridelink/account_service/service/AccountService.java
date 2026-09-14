package com.ridelink.account_service.service;

import com.ridelink.account_service.model.Account;
import com.ridelink.account_service.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository,PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Account registerAccount(Account account) {

        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Encrypt password before saving to MongoDB
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        // Default role if not provided
        if (account.getRole() == null || account.getRole().isBlank()) {
            account.setRole("PASSENGER");
        }

        // Default account status
        if (account.getStatus() == null || account.getStatus().isBlank()) {
            account.setStatus("ACTIVE");
        }

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