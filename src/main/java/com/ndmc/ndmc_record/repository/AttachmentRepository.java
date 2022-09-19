package com.ndmc.ndmc_record.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ndmc.ndmc_record.model.AttachmentModel;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentModel, Long> {

	Optional<AttachmentModel> findByBndIdAndSlaDetailsIdAndFileType(Long bndId, Long slaId, String certificateType);

    List<AttachmentModel> findBySlaDetailsId(Long slaDetailsId);

}