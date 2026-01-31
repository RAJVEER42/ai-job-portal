package com.jobportal.backend.repository;

import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    // Find all active jobs
    List<Job> findByIsActiveTrue();
    
    // Find jobs by location
    List<Job> findByLocationContainingIgnoreCase(String location);
    
    // Find jobs by company
    List<Job> findByCompanyContainingIgnoreCase(String company);
    
    // Find jobs by type
    List<Job> findByJobType(JobType jobType);
    
    // Find jobs posted by specific recruiter
    List<Job> findByRecruiterId(Long recruiterId);
    
    // Advanced: Search jobs by title or description (LIKE query)
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Job> searchJobs(@Param("keyword") String keyword);
    
    // Pagination: Get all active jobs with pagination
    Page<Job> findByIsActiveTrue(Pageable pageable);
}