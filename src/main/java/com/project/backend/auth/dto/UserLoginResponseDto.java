package com.project.backend.auth.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponseDto {
    private Long id;
    private String accountId;
    private String name;
}
