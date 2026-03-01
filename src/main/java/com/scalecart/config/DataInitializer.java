package com.scalecart.config;

import com.scalecart.domain.Category;
import com.scalecart.domain.Product;
import com.scalecart.domain.User;
import com.scalecart.repository.CategoryRepository;
import com.scalecart.repository.ProductRepository;
import com.scalecart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner init(
        CategoryRepository categoryRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (categoryRepository.count() > 0) return;

            Category electronics = new Category();
            electronics.setName("Electronics");
            categoryRepository.save(electronics);

            Category home = new Category();
            home.setName("Home");
            categoryRepository.save(home);

            Product p1 = new Product();
            p1.setName("Widget A");
            p1.setSku("SKU-001");
            p1.setPrice(new BigDecimal("19.99"));
            p1.setStockQuantity(100);
            p1.setCategory(electronics);
            productRepository.save(p1);

            Product p2 = new Product();
            p2.setName("Widget B");
            p2.setSku("SKU-002");
            p2.setPrice(new BigDecimal("29.99"));
            p2.setStockQuantity(50);
            p2.setCategory(electronics);
            productRepository.save(p2);

            User user = new User();
            user.setUsername("user");
            user.setPasswordHash(passwordEncoder.encode("password"));
            user.getRoles().add("USER");
            userRepository.save(user);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.getRoles().add("USER");
            admin.getRoles().add("ADMIN");
            userRepository.save(admin);
        };
    }
}
