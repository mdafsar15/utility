package com.ndmc.ndmc_record.dto;

import com.ndmc.ndmc_record.enums.UserType;
import lombok.*;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRoleDto {

    private Long roleId;
    private String roleName;
    //@Enumerated(EnumType.STRING)
   // private UserType type;
    private String type;
    private String roleLabel;

}
