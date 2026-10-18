package com.project.backend.auth.domain;

import com.project.backend.auth.domain.type.EmailVerified;
import com.project.backend.auth.domain.type.Role;
import com.project.backend.auth.domain.type.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String accountId;

    @Setter
    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.CUSTOMER;

    @Column(nullable = false, length = 20)
    private String name;

    @Setter
    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Setter
    @Column(nullable = false, length = 50)
    private String address;

    @Setter
    @Column(nullable = false, length = 50)
    private String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private EmailVerified emailVerified = EmailVerified.No;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void active(){
        this.status = Status.ACTIVE;
    }

    public void suspend(){
        this.status = Status.SUSPEND;
    }

    public void withdraw(){
        this.status = Status.WITHDRAW;
    }

    public void changeToAdmin(){
        this.role = Role.ADMIN;
    }

    public void unVerify(){
        this.emailVerified = EmailVerified.No;
    }
    public void verify(){
        this.emailVerified = EmailVerified.Yes;
    }

    public boolean matchPassword(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }
}
