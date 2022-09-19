package com.ndmc.ndmc_record.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDto {


    private String currentPassword;
    private String newPassword;
   // private String confirmPassword;

}
