package com.ndmc.ndmc_record.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {
    private String newPassword;
    private String token;
}
