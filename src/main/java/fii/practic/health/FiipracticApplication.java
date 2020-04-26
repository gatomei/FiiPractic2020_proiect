package fii.practic.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FiipracticApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiipracticApplication.class, args);
    }
}
