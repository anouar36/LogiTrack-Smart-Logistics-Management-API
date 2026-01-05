    package com.logitrack.logitrack.dto;

    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public class LoginRequest {

        @NotBlank(message = "Please make sure your email is correct anouar . ")
        private String username;
        @Size(min = 6, message = "Your password must be at least 6 characters long.")
        private String password;
    }
