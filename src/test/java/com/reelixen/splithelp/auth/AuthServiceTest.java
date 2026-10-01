package com.reelixen.splithelp.auth;

import com.reelixen.splithelp.auth.dto.RegisterRequest;
import com.reelixen.splithelp.user.UserRepository;
import com.reelixen.splithelp.user.User;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final AuthService authService = new AuthService(passwordEncoder, userRepository);

    RegisterRequest request = new RegisterRequest(
            "Nazar",
            "nazar@example.com",
            "TestPassword123!"
    );

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("nazar@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );
    }

    @Test
    void registerShouldTranslateEmailConstraintViolation() {
        DataIntegrityViolationException failure = constraintFailure("users_email_key");
        when(userRepository.save(any(User.class))).thenThrow(failure);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void registerShouldPreserveOtherConstraintViolations() {
        DataIntegrityViolationException failure = constraintFailure("users_pkey");
        when(userRepository.save(any(User.class))).thenThrow(failure);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> authService.register(request)));
    }

    @Test
    void registerShouldPreserveViolationWithoutConstraintName() {
        DataIntegrityViolationException failure = constraintFailure(null);
        when(userRepository.save(any(User.class))).thenThrow(failure);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> authService.register(request)));
    }

    @Test
    void registerShouldPreserveUnrecognizedIntegrityFailure() {
        DataIntegrityViolationException failure = new DataIntegrityViolationException("Insert failed");
        when(userRepository.save(any(User.class))).thenThrow(failure);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> authService.register(request)));
    }

    private DataIntegrityViolationException constraintFailure(String constraintName) {
        ConstraintViolationException cause = new ConstraintViolationException(
                "Constraint violated", new SQLException("Constraint violated", "23505"), constraintName
        );
        return new DataIntegrityViolationException("Insert failed", cause);
    }
}
