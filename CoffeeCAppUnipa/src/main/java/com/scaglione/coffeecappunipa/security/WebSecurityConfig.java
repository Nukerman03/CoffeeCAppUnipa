package com.scaglione.coffeecappunipa.security;

import com.scaglione.coffeecappunipa.dao.UserDao;
import com.scaglione.coffeecappunipa.dao.VendingMachineDao;
import com.scaglione.coffeecappunipa.entity.User;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.utils.RoleNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDao userDao;

    @Autowired
    private VendingMachineDao vendingMachineDao;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/api/machine/**", "/distributore/**").permitAll()
                .requestMatchers("/dashboard/**").hasAuthority(RoleNames.MANAGER)
                .requestMatchers("/maintenance/**").hasAuthority(RoleNames.MAINTAINER)
                .requestMatchers("/home/**", "/connect", "/disconnect", "/recharge").hasAnyAuthority(RoleNames.USER, RoleNames.MANAGER, RoleNames.MAINTAINER)
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    if (RoleNames.MANAGER.equals(role)) {
                        response.sendRedirect("/dashboard");
                    } else if (RoleNames.MAINTAINER.equals(role)) {
                        response.sendRedirect("/maintenance");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
                .permitAll()
            )
            .logout((logout) -> logout
                .addLogoutHandler(logoutHandler())
                .logoutRequestMatcher(request -> "/logout".equals(request.getServletPath()))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/machine/**"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            if (authentication != null && authentication.getName() != null) {
                String username = authentication.getName();
                User user = userDao.findByUsername(username).orElse(null);
                
                if (user != null) {
                    List<VendingMachine> machines = vendingMachineDao.findAll();
                    for (VendingMachine vm : machines) {
                        if (user.getId().equals(vm.getConnectedUserId())) {
                            vm.setConnectedUserId(null);
                            vendingMachineDao.save(vm);
                        }
                    }
                }
            }
        };
    }
}