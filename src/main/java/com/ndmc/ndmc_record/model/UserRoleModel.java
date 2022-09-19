package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.enums.ApprovalEnum;
import com.ndmc.ndmc_record.enums.UserType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "user_role")
public class UserRoleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;
    private String roleName;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;
    private String createdBy;
    private String modifiedBy;
   // @Enumerated(EnumType.STRING)
    //private UserType type; //
    private String type; // CFC, HOSPITAL, ONLINE, ADMIN
    private String roleLabel;


}
