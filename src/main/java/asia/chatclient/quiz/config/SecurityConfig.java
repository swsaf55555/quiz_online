package asia.chatclient.quiz.config;

import asia.chatclient.quiz.handler.CustomAuthenticationFailureHandler;
import asia.chatclient.quiz.handler.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import asia.chatclient.quiz.handler.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler failureHandler, CustomAuthenticationSuccessHandler successHandler) {
        this.failureHandler = failureHandler;
        this.successHandler = successHandler;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/images/**", "/js/**", "/error", "/")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/quiz", true)
                        .failureHandler(failureHandler)
                .successHandler(successHandler)
                .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
