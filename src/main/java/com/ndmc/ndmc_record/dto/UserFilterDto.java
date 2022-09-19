package com.ndmc.ndmc_record.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDto {

    private String firstName;
    private String lastName;
    private String orgId;
    private String mobileNo;
}
