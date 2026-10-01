package com.reelixen.splithelp.auth;

import com.reelixen.splithelp.auth.dto.RegisterRequest;
import com.reelixen.splithelp.auth.dto.CurrentUserResponse;
import org.springframework.security.authentication.BadCredentialsException;
import com.reelixen.splithelp.auth.dto.RegisterResponse;
import com.reelixen.splithelp.user.User;
import com.reelixen.splithelp.user.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository){
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public CurrentUserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        return new CurrentUserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public RegisterResponse register(RegisterRequest request) {
        // Locale.ROOT makes lowercasing independent of the computer's language settings
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)){
            throw new EmailAlreadyExistsException("Email already in use");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(request.name(), normalizedEmail, passwordHash);
        User savedUser;

        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException violation
                    && "users_email_key".equals(violation.getConstraintName())) {
                throw new EmailAlreadyExistsException("Email already in use");
            }

            throw exception;
        }

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }
}
