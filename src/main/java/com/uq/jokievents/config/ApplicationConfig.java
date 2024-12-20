package com.uq.jokievents.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.ClientRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class ApplicationConfig {

    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService customUserDetailsService() {
        return username -> {
            // Try to find the user in the admin repository first
            Optional<Admin> adminOpt = adminRepository.findByUsername(username);
            if (adminOpt.isPresent()) {
                return adminOpt.get();  // Return the Admin directly since it implements UserDetails
            }

            // If not found, check the client repository
            Optional<Client> clientOpt = clientRepository.findByEmail(username);
            if (clientOpt.isPresent()) {
                return clientOpt.get();  // Return the Client directly since it also implements UserDetails
            }

            throw new UsernameNotFoundException("User not found");
        };
    }

    @Bean
    public PasswordEncoder plainTextPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();  // No encoding, return raw password
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // Compare raw password directly with the stored plain text password
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Getter
    @Value("${base64.image}")
    private String base64Image;

    @Getter
    @Value("${paypal.client.id}")
    private String clientId;

    @Getter
    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Getter
    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        PayPalEnvironment.Sandbox environment = new PayPalEnvironment.Sandbox(this.getClientId(), this.getClientSecret());
        return new PayPalHttpClient(environment);
    }
}
