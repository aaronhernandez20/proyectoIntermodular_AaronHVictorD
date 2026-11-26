package com.vulnfollowup.repository;

import com.vulnfollowup.entity.CvssAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CvssAssessmentRepository extends JpaRepository<CvssAssessment, Long> {

    @Query("SELECT a FROM CvssAssessment a WHERE a.vulnerability.id = :vulnId ORDER BY a.assessedAt DESC, a.id DESC")
    List<CvssAssessment> findHistory(@Param("vulnId") Long vulnerabilityId);
}
