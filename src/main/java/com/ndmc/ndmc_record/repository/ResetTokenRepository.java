package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.ResetTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetTokenModel, Long> {
    ResetTokenModel findByToken(String token);

    ResetTokenModel findByOtp(String otp);


    @Query(value="select * from reset_token where user_id =:userId AND expiry_date >:currentTime ORDER BY token_id DESC LIMIT 1", nativeQuery = true)
    ResetTokenModel findByUserId(Long userId, LocalDateTime currentTime);
}
