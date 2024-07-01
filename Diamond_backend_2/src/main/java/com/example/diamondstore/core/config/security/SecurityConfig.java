package com.example.diamondstore.core.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JWTAthenticationEntryPoint point;

    @Autowired
    private JWTAuthenticationFilter filter;

    @Autowired
    private CorsFilter corsFilter; // Inject the CorsFilter

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class) // Add the CorsFilter before other filters
                .authorizeRequests(authorize -> authorize
                        .requestMatchers("/*/*/login", "/*/*/logout", "/*/*/register", "/*/*/payment/*",
                                "/*/*/forgot-password", "/*/*/reset-password", "/*/product/list/*",
                                "/*/*/createWithDetails", "/*/comment/product/*", "/api/order/*",
                                "/*/product/all", "/*/*/showProduct/*", "/*/product/search/*",
                                "/*/*/list/prices").permitAll() // Permit access to /login endpoint

                        .requestMatchers("/api/comment/add", "/api/comment/edit/*", "/api/order/createWithDetails",
                                "/api/order_detail/create", "/api/order_detail/*", "/api/order_detail/update/*",
                                "/*/order/history/*", "/api/user/update/*", "/api/user/change-password/*",
                                "/api/voucher/create/*").hasRole("Member")

                        .requestMatchers("/api/certificate/create", "/api/certificate/all", "/api/certificate/delete/*",
                                "/api/certificate/update/*", "api/comment/user/*", "api/comment/user/*/product/*",
                                "/api/diamond/create", "/api/diamond/update/*", "/api/mount/create", "/api/mount/update/*",
                                "/api/mount/list/*", "/api/mount/type", "/api/inventory/all", "/api/inventory/create",
                                "/api/inventory/*", "/api/inventory/update/*", "/api/inventory/delete/*", "/api/promotion/*",
                                "/api/order_detail/orderDetail/*", "/api/order_detail/list/all", "/api/order_detail/order/*",
                                "/*/order/assign", "/api/productdiamond/update/*", "/api/productprice/product/*", "/api/productdiamond/*",
                                "/api/productprice/*", "/api/productpromotion/*", "/api/productpromotion/product/*", "/*/order/cancel/*",
                                "/api/productpromotion/promotion/*", "/api/promotion/*", "/api/promotion/update/*",
                                "/api/promotion/status/*", "/api/product/create","/api/product/update/*", "/api/user/all",
                                "/api/user/status/*", "/api/voucher/all", "/api/vouchertype/all","/api/vouchertype/create",
                                "/api/vouchertype/update/*", "/api/warranty/delete/*", "/api/collection/create" ,
                                "/api/collection/update/*", "/api/collection/deleteCollection/*", "/api/collection/addCollection",
                                "/api/collection/deleteProduct", "/api/order/list/deliveryOrder").hasRole("Manager")

                        .requestMatchers("/api/order/delivery/*", "/api/order/delivery/status" ).hasRole("Delivery Staff")

                        .requestMatchers("/api/diamond/all", "/api/mount/all", "/api/diamond/*",
                                "/api/mount/*", "/api/order/all", "/api/order/user/*", "/api/warranty/create",
                                "/api/warranty/update/*", "/api/warranty/all",
                                "/api/warranty/orderDetail/*").hasAnyRole("Sales Staff", "Manager")

                        .requestMatchers("/api/user/role/*").hasAnyRole("Manager", "Admin")

                        .requestMatchers("/api/comment/delete/*", "/api/order/update/*",
                                "/api/voucher/member/*").hasAnyRole("Member", "Manager")

                        .requestMatchers("/api/dashboard/countMember", "/api/dashboard/countProcessingOrder",
                                "/api/dashboard/countCompleteOrder", "/api/dashboard/countCancelOrder",
                                "/api/dashboard/totalRevenue").hasRole("Admin")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}



