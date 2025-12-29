    package com.logitrack.logitrack;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.boot.autoconfigure.domain.EntityScan;

    @SpringBootApplication
    @EntityScan(basePackages = "com.logitrack.logitrack.entity")
    public class LogiTrackApplication {

        public static void main(String[] args) {
            SpringApplication.run(LogiTrackApplication.class, args);
        }

    }
