package com.ndmc.ndmc_record.repository;


import com.ndmc.ndmc_record.domain.ChildDetails;
import com.ndmc.ndmc_record.enums.DataFlagEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChildDetailsRepository extends JpaRepository<ChildDetails,Long> {

    Optional<ChildDetails> findByChildId(UUID childId);
    ChildDetails findByRegistrationNumber(String registrationNumber);

    @Query(value="select * from child_details ",nativeQuery = true)
    List<Map<String,String>> getAllData();


    @Query(value="select * from child_details d where d.registration_number=:registrationNumber",nativeQuery = true)
    List<Map<String,String>> getFindByRegistrationNumber(String registrationNumber);

    List<ChildDetails> findByFlag(DataFlagEnum flag);
    @Modifying(clearAutomatically = true)
    @Query(value="update child_details d set d.flag =:flag where d.registration_number=:childRegNo",nativeQuery = true)
    int updateFlag(String childRegNo,String flag);

    @Modifying(clearAutomatically = true)
    @Query(value="update child_details d set d.req_status =:flag where d.registration_number=:childRegNo",nativeQuery = true)
    int updateChildDetailsRequestStatus(String childRegNo,String flag);
}
