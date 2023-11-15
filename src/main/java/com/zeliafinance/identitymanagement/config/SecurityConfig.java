package com.zeliafinance.identitymanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter){
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers(HttpMethod.POST, "/api/v1/users/registerUser").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/users/login").permitAll()
                                .requestMatchers(HttpMethod.GET, "/login").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}").hasRole("SUPER_ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/admin/acceptInvite").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/users/resetPassword").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/users/changePassword").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/users/validateOtp").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/users/sendOtp").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/users/adminLogin").permitAll()
                                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/users/validateToken").permitAll()
                                .requestMatchers("/").permitAll()
                                .anyRequest().authenticated()
                );
        httpSecurity.logout(logout -> logout.logoutUrl("/api/v1/users/logout").logoutSuccessHandler(logoutSuccessHandler()).invalidateHttpSession(true));
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).invalidSessionUrl("/api/v1/users/login?expired").maximumSessions(1).expiredUrl("/api/v1/users/login?expired"));
        httpSecurity.authenticationProvider(authenticationProvider());
        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));
        return httpSecurity.build();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(){
        return new HttpStatusReturningLogoutSuccessHandler();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://www.sandbox.zeliafinance.com",
                "https://www.sandbox.zeliafinance.com",
                "http://localhost:5173",
                "https://zelia-admin-app.vercel.app",
                "https://www.test-admin.zeliafinance.com",
                "*"
        ));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.addAllowedMethod(HttpMethod.POST);
        configuration.addAllowedMethod(HttpMethod.GET);
        configuration.addAllowedMethod(HttpMethod.HEAD);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.OPTIONS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
