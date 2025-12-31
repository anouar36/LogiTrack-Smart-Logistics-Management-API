package com.logitrack.logitrack.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig  {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable())
                .sessionManagement(sission->sission.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/products/**").permitAll()

                        // ADMIN only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/warehouses/**").hasRole("ADMIN")
                        .requestMatchers("/api/suppliers/**").hasRole("ADMIN")
                        .requestMatchers("/api/purchase-orders/**").hasRole("ADMIN")

                        // ADMIN + WAREHOUSE_MANAGER
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/inventory-movements/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/shipments/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // Orders
                        .requestMatchers(HttpMethod.POST, "/api/salesOrder/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/salesOrder/**").hasAnyRole("CLIENT", "ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/salesOrder/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers("/api/orders/**").hasAnyRole("CLIENT", "ADMIN")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();

    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("ROLE_");
        converter.setAuthoritiesClaimName("realm_access.roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }

}