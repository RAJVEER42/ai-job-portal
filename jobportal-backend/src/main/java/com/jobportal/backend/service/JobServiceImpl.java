package com.jobportal.backend.service;

import com.jobportal.backend.dto.CreateJobRequest;
import com.jobportal.backend.dto.JobResponse;
import com.jobportal.backend.exception.ResourceNotFoundException;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.JobRepository;
import com.jobportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {
    
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = {"jobs", "job-search"}, allEntries = true)
    public JobResponse createJob(CreateJobRequest request, Long recruiterId) {
        log.info("Creating new job: {} by recruiter ID: {}", request.getTitle(), recruiterId);
        
        // Find recruiter
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with ID: " + recruiterId));
        
        // Create job entity
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .location(request.getDescription())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMinSalary())
                .experienceRequired(request.getExperienceRequired())
                .skillsRequired(request.getSkillsRequired())
                .jobType(request.getJobType())
                .applicationDeadline(request.getApplicationDeadline())
                .isActive(true)
                .recruiter(recruiter)
                .build();
        
        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getId());
        
        return mapToJobResponse(savedJob);
    }
    
    @Override
    @Cacheable(value = "jobs", key = "#id")
    public JobResponse getJobById(Long id) {
        log.info("Fetching job with ID: {}", id);
        
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));
        
        return mapToJobResponse(job);
    }
    
    @Override
    @Cacheable(value = "job-search", key = "'all-active-jobs'")
    public List<JobResponse> getAllActiveJobs() {
        log.info("Fetching all active jobs");
        
        return jobRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<JobResponse> getAllActiveJobsPaginated(Pageable pageable) {
        log.info("Fetching active jobs with pagination: page={}, size={}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        
        return jobRepository.findByIsActiveTrue(pageable)
                .map(this::mapToJobResponse);
    }
    
    @Override
    @Cacheable(value = "job-search", key = "'search-' + #keyword")
    public List<JobResponse> searchJobs(String keyword) {
        log.info("Searching jobs with keyword: {}", keyword);
        
        return jobRepository.searchJobs(keyword)
                .stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "job-search", key = "'location-' + #location")
    public List<JobResponse> getJobsByLocation(String location) {
        log.info("Fetching jobs by location: {}", location);
        
        return jobRepository.findByLocationContainingIgnoreCase(location)
                .stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "jobs", key = "'recruiter-' + #recruiterId")
    public List<JobResponse> getJobsByRecruiterId(Long recruiterId) {
        log.info("Fetching jobs posted by recruiter ID: {}", recruiterId);
        
        return jobRepository.findByRecruiterId(recruiterId)
                .stream()
                .map(this::mapToJobResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", key = "#id"),
        @CacheEvict(value = "jobs", key = "'recruiter-' + #recruiterId"),
        @CacheEvict(value = "job-search", allEntries = true)
    })
    public JobResponse updateJob(Long id, CreateJobRequest request, Long recruiterId) {
        log.info("Updating job ID: {} by recruiter ID: {}", id, recruiterId);
        
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));
        
        // Verify recruiter owns this job
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new RuntimeException("You are not authorized to update this job");
        }
        
        // Update fields
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setCompany(request.getCompany());
        job.setLocation(request.getDescription());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMinSalary());
        job.setExperienceRequired(request.getExperienceRequired());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setJobType(request.getJobType());
        job.setApplicationDeadline(request.getApplicationDeadline());
        
        Job updatedJob = jobRepository.save(job);
        log.info("Job updated successfully: ID {}", updatedJob.getId());
        
        return mapToJobResponse(updatedJob);
    }
    
    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "jobs", key = "#id"),
        @CacheEvict(value = "jobs", key = "'recruiter-' + #recruiterId"),
        @CacheEvict(value = "job-search", allEntries = true)
    })
    public void deleteJob(Long id, Long recruiterId) {
        log.info("Deleting job ID: {} by recruiter ID: {}", id, recruiterId);
        
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + id));
        
        // Verify recruiter owns this job
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new RuntimeException("You are not authorized to delete this job");
        }
        
        jobRepository.delete(job);
        log.info("Job deleted successfully: ID {}", id);
    }
    
    // Helper: Entity → DTO conversion
    // ...existing code...
private JobResponse mapToJobResponse(Job job) {
    return JobResponse.builder()
            .id(job.getId())
            .title(job.getTitle())
            .description(job.getDescription())
            .company(job.getCompany())
            .location(job.getLocation())
            .minSalary(job.getMinSalary())  // Fix: was getMaxSalary()
            .maxSalary(job.getMaxSalary())
            .experienceRequired(job.getExperienceRequired())  // Fix: was getSkillsRequired()
            .skillsRequired(job.getSkillsRequired())
            .jobType(job.getJobType())
            .applicationDeadline(job.getApplicationDeadline())
            .isActive(job.getIsActive())
            .postedAt(job.getCreatedAt())
            .recruiter(JobResponse.RecruiterInfo.builder()
                    .id(job.getRecruiter().getId())
                    .fullName(job.getRecruiter().getFullName())
                    .email(job.getRecruiter().getEmail())
                    .build())
            .build();
}
// ...existing code...
}