package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "reset_token")
public class ResetTokenModel {

    private static final int EXPIRATION = 60 * 24;
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;
    private String token;
    private String otp;

    @OneToOne(targetEntity = UserModel.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserModel user;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;
    private Integer count;
}
