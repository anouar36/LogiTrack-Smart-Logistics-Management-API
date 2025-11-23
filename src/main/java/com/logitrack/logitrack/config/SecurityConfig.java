package com.logitrack.logitrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // this for BCrypt Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spesific User Details Service (InMemory)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // creat user Admin
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        // creat user Warehouse Manager
        UserDetails manager = User.withUsername("manager")
                .password(passwordEncoder.encode("manager123"))
                .roles("WAREHOUSE_MANAGER")
                .build();

        // creat user Client
        UserDetails client = User.withUsername("client")
                .password(passwordEncoder.encode("client123"))
                .roles("CLIENT")
                .build();

        //return th user in Memory
        return new InMemoryUserDetailsManager(admin, manager, client);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/inventory/**", "/api/shipments/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        .requestMatchers("/api/orders/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/salesOrder/**", "/api/salesOrderByClient").hasAnyRole("ADMIN", "CLIENT")

                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/api/purchase-orders/**","/api/purchase-orders").hasRole("ADMIN")
                        .requestMatchers("/api/inventory/**","/api/inventory").hasRole("ADMIN")


                        .anyRequest().authenticated()
                )


                .httpBasic(Customizer.withDefaults());


        return http.build();
    }
}