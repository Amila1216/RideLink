package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.model.Account;
import com.ridelink.account_service.repository.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(
                accountRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void registerAccount_shouldHashPasswordAndSaveAccount() {

        Account account = new Account();
        account.setName("Test Passenger");
        account.setEmail("test@gmail.com");
        account.setPassword("Test1234");
        account.setPhone("0712345678");
        account.setRole("PASSENGER");

        when(accountRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Test1234"))
                .thenReturn("hashed-password");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.registerAccount(account);

        assertNotNull(result);
        assertEquals("hashed-password", result.getPassword());
        assertEquals("PASSENGER", result.getRole());
        assertEquals("ACTIVE", result.getStatus());

        verify(accountRepository).save(account);
    }

    @Test
    void registerAccount_shouldRejectDuplicateEmail() {

        Account account = new Account();
        account.setEmail("existing@gmail.com");

        when(accountRepository.existsByEmail("existing@gmail.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> accountService.registerAccount(account)
                );

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void login_shouldRejectWrongPassword() {

        Account account = new Account();
        account.setEmail("test@gmail.com");
        account.setPassword("hashed-password");
        account.setRole("PASSENGER");
        account.setStatus("ACTIVE");

        when(accountRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashed-password"
        )).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("WrongPassword");

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.login(request)
        );
    }

    @Test
    void login_shouldRejectInactiveAccount() {

        Account account = new Account();
        account.setEmail("test@gmail.com");
        account.setPassword("hashed-password");
        account.setRole("PASSENGER");
        account.setStatus("SUSPENDED");

        when(accountRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "Test1234",
                "hashed-password"
        )).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("Test1234");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> accountService.login(request)
                );

        assertEquals(
                "Account is not active",
                exception.getMessage()
        );
    }

    @Test
    void updateRole_shouldUpdateValidRole() {

        Account account = new Account();
        account.setId("123");
        account.setRole("PASSENGER");

        when(accountRepository.findById("123"))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result =
                accountService.updateRole(
                        "123",
                        "DRIVER"
                );

        assertEquals(
                "DRIVER",
                result.getRole()
        );

        verify(accountRepository).save(account);
    }

    @Test
    void updateRole_shouldRejectInvalidRole() {

        Account account = new Account();
        account.setId("123");

        when(accountRepository.findById("123"))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateRole(
                        "123",
                        "INVALID_ROLE"
                )
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateStatus_shouldUpdateValidStatus() {

        Account account = new Account();
        account.setId("123");
        account.setStatus("ACTIVE");

        when(accountRepository.findById("123"))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result =
                accountService.updateStatus(
                        "123",
                        "SUSPENDED"
                );

        assertEquals(
                "SUSPENDED",
                result.getStatus()
        );

        verify(accountRepository).save(account);
    }

    @Test
    void updateStatus_shouldRejectInvalidStatus() {

        Account account = new Account();
        account.setId("123");

        when(accountRepository.findById("123"))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateStatus(
                        "123",
                        "INVALID_STATUS"
                )
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldUpdateNameAndPhone() {

        Account account = new Account();
        account.setEmail("test@gmail.com");
        account.setName("Old Name");
        account.setPhone("0700000000");

        when(accountRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request =
                new UpdateProfileRequest();

        request.setName("Updated Name");
        request.setPhone("0712345678");

        Account result =
                accountService.updateProfile(
                        "test@gmail.com",
                        request
                );

        assertEquals(
                "Updated Name",
                result.getName()
        );

        assertEquals(
                "0712345678",
                result.getPhone()
        );

        verify(accountRepository).save(account);
    }
}