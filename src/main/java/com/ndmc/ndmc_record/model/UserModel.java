package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    private String userName;
    @JsonIgnore
    private String password;
    private String email;
    private String status;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;
    private String modifiedBy;
    private String createdBy;
    @Transient
    private String userType; // Hospital or CFC
    private String organizationId; // eg: CFC1 for Mandir marg, AIIMS for All india institute of medical science
    @Transient
    private String organizationAddress;
    @Transient
    private String organizationCode;
    private String firstName;
    private String lastName;
//    @Transient
//    private String divisionCode;
    @Transient
    private String organizationName;

    private String employeeCode;
    private String designation;
    private String contactNo;
    private LocalDate validityStart;
    private LocalDate validityEnd;

    //IP address authentication integration 03-05-22
    //Deepak
    private String ipAddress;

//    private String recordTypePermission; // BOTH, NEW, OLD



    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles_mapper",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRoleModel> roles = new HashSet<>();

    public void addRole(UserRoleModel role) {
        this.roles.add(role);
    }

}
