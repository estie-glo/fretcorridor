package com.flysoft.fretcorridor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class FretCorridorApplication {

    public static void main(String[] args) {

        System.out.println(
            new BCryptPasswordEncoder().encode("1234")
        );

        SpringApplication.run(FretCorridorApplication.class, args);
    }
}
