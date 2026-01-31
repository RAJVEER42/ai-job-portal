package com.jobportal.backend.repository;

import com.jobportal.backend.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
    
    List<Application> findByUserId(Long userId);
    
    List<Application> findByJobId(Long jobId);
    
    long countByJobId(Long jobId);
    
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.user u " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.recruiter " +
           "WHERE j.recruiter.id = :recruiterId")
    List<Application> findApplicationsForRecruiter(@Param("recruiterId") Long recruiterId);
    
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.user " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.recruiter " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);
}