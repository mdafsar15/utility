package com.ndmc.ndmc_record.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordDto {
    private String mobileNoOrEmail;
    //private String emailId;
}
