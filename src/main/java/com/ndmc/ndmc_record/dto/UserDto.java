package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class UserDto {

    private Long userId;
    private List<Long> roles;

    private String userName;
    private String password;
    private String email;
    private String userType; // Hospital or CFC
    private String organizationCode; // eg: CFC1 for Mandir marg, AIIMS for All india institute of medical science
    private String organizationId;
    private String status;
    private String firstName;
    private String lastName;
    private String divisionCode;
    private String organizationName;
    private String employeeCode;
    private String designation;
    private String contactNo;
    private LocalDate validityStart;
    private LocalDate validityEnd;
    private String recordTypePermission; // BOTH, NEW, OLD


    //private Set<UserRoleDto> roles;

}
