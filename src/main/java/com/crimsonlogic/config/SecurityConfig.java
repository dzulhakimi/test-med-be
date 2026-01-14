package com.crimsonlogic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.crimsonlogic.service.AppUserDetailsService;

@Configuration
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final AppUserDetailsService userDetailsService;
	private final CorsConfigurationSource corsConfigurationSource;

	public SecurityConfig(JwtFilter jwtFilter, AppUserDetailsService userDetailsService, CorsConfigurationSource corsConfigurationSource) {
		this.jwtFilter = jwtFilter;
		this.userDetailsService = userDetailsService;
		this.corsConfigurationSource = corsConfigurationSource;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource)) // Enable CORS with custom config
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
					.antMatchers("/health", "/").permitAll()
					.antMatchers("/favicon.ico").permitAll()
					.antMatchers("/api/leave/**").permitAll()
					.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.antMatchers("/api/auth/login", "/h2-console/**").permitAll()
					.antMatchers("/uploads/**").permitAll()
					.antMatchers("/api/documents/download/**").permitAll()
					.anyRequest().authenticated())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers -> headers.frameOptions().disable())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
