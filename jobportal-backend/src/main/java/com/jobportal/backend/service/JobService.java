package com.jobportal.backend.service;

import com.jobportal.backend.dto.CreateJobRequest;
import com.jobportal.backend.dto.JobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobService {
    JobResponse createJob(CreateJobRequest request, Long recruiterId);
    JobResponse getJobById(Long id);
    List<JobResponse> getAllActiveJobs();
    Page<JobResponse> getAllActiveJobsPaginated(Pageable pageable);
    List<JobResponse> searchJobs(String keyword);
    List<JobResponse> getJobsByLocation(String location);
    List<JobResponse> getJobsByRecruiterId(Long recruiterId);
    JobResponse updateJob(Long id, CreateJobRequest request, Long recruiterId);
    void deleteJob(Long id, Long recruiterId);
}