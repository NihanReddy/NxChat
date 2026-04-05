package com.chatflow.security;

import com.chatflow.model.User;
import com.chatflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findActiveByEmail(usernameOrEmail)
            .or(() -> userRepository.findActiveByUsername(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or username: " + usernameOrEmail));

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(user.getEmail());
        builder.password(user.getPassword());
        String role = user.getRole() == null ? "USER" : user.getRole();
        builder.roles(role);

        return builder.build();
    }

    public String encodePassword(String rawPassword) {
        return new BCryptPasswordEncoder().encode(rawPassword);
    }
}
