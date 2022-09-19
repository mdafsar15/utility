package com.ndmc.ndmc_record.repository;

import com.ndmc.ndmc_record.model.OnlineAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnlineAppointmentRepository extends JpaRepository<OnlineAppointment, Long> {


    OnlineAppointment findBySlaIdOrderByAptIdDesc(Long slaId);


    List<OnlineAppointment> findByStatus(String open);
}
