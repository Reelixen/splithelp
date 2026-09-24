package com.reelixen.splithelp.auth;

import com.reelixen.splithelp.auth.dto.RegisterRequest;
import com.reelixen.splithelp.auth.dto.RegisterResponse;
import com.reelixen.splithelp.user.User;
import com.reelixen.splithelp.user.UserRepository;
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

    public RegisterResponse register(RegisterRequest request) {
        // Locale.ROOT makes lowercasing independent of the computer's language settings
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)){
            throw new EmailAlreadyExistsException("Email already in use");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(request.name(), normalizedEmail, passwordHash);
        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt()
        );
    }
}
