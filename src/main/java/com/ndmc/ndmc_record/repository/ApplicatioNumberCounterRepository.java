package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.ApplicationNumberCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicatioNumberCounterRepository extends JpaRepository <ApplicationNumberCounter, Long> {

     @Query(value="select * from application_number_counter where org_code =:orgCode and  year =:year and  registration_type =:registrationType", nativeQuery = true)
     ApplicationNumberCounter getCounter(String orgCode, int year, String registrationType);

    // @Query(value = "{call get_unique_application_counter(:orgCode, :year,:registrationType)}", nativeQuery = true)
   // public ApplicationNumberCounter getCounter(@Param("orgCode") String orgCode, @Param("year") int year, @Param("registrationType") String registrationType);
}
