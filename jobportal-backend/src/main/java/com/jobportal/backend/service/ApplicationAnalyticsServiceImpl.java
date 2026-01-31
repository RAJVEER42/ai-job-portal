package com.jobportal.backend.service;

import com.jobportal.backend.dto.ApplicationAnalyticsResponse;
import com.jobportal.backend.dto.ApplicationAnalyticsResponse.*;
import com.jobportal.backend.model.Application;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.ApplicationRepository;
import com.jobportal.backend.repository.JobRepository;
import com.jobportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationAnalyticsServiceImpl implements ApplicationAnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    @Override
    public ApplicationAnalyticsResponse getAnalytics() {
        log.info("Generating overall application analytics");
        
        List<Application> allApplications = applicationRepository.findAll();
        List<Job> allJobs = jobRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        
        return buildAnalyticsResponse(allApplications, allJobs, allUsers);
    }

    @Override
    public ApplicationAnalyticsResponse getRecruiterAnalytics(Long recruiterId) {
        log.info("Generating analytics for recruiter: {}", recruiterId);
        
        List<Job> recruiterJobs = jobRepository.findByRecruiterId(recruiterId);
        
        List<Long> jobIds = recruiterJobs.stream()
                .map(Job::getId)
                .collect(Collectors.toList());
        
        List<Application> applications = applicationRepository.findAll().stream()
                .filter(app -> jobIds.contains(app.getJob().getId()))
                .collect(Collectors.toList());
        
        List<User> allUsers = userRepository.findAll();
        
        return buildAnalyticsResponse(applications, recruiterJobs, allUsers);
    }

    private ApplicationAnalyticsResponse buildAnalyticsResponse(
            List<Application> applications, 
            List<Job> jobs, 
            List<User> users) {
        
        return ApplicationAnalyticsResponse.builder()
                .overallStats(calculateOverallStats(applications, jobs, users))
                .popularJobs(calculateJobPopularity(applications, jobs))
                .skillDemands(calculateSkillDemand(jobs, users))
                .applicationTrends(calculateApplicationTrends(applications))
                .recruitmentMetrics(calculateRecruitmentMetrics(applications, jobs))
                .build();
    }

    private OverallStats calculateOverallStats(
            List<Application> applications, 
            List<Job> jobs, 
            List<User> users) {
        
        Map<String, Long> applicationsByStatus = applications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getStatus().toString(),
                    Collectors.counting()
                ));
        
        long hiredCount = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .count();
        
        double successRate = applications.isEmpty() ? 0.0 : 
                (hiredCount * 100.0) / applications.size();
        
        long activeJobs = jobs.stream()
                .filter(job -> Boolean.TRUE.equals(job.getIsActive()))
                .count();
        
        double avgApplicationsPerJob = jobs.isEmpty() ? 0.0 : 
                (double) applications.size() / jobs.size();
        
        return OverallStats.builder()
                .totalApplications((long) applications.size())
                .totalJobs((long) jobs.size())
                .totalCandidates((long) users.size())
                .activeJobs(activeJobs)
                .averageApplicationsPerJob(Math.round(avgApplicationsPerJob * 100.0) / 100.0)
                .applicationsByStatus(applicationsByStatus)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .build();
    }

    private List<JobPopularity> calculateJobPopularity(
            List<Application> applications, 
            List<Job> jobs) {
        
        Map<Long, Long> applicationCounts = applications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getJob().getId(),
                    Collectors.counting()
                ));
        
        Map<Long, Long> hiredCounts = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .collect(Collectors.groupingBy(
                    app -> app.getJob().getId(),
                    Collectors.counting()
                ));
        
        return jobs.stream()
                .map(job -> {
                    long appCount = applicationCounts.getOrDefault(job.getId(), 0L);
                    long hired = hiredCounts.getOrDefault(job.getId(), 0L);
                    double conversionRate = appCount == 0 ? 0.0 : (hired * 100.0) / appCount;
                    
                    return JobPopularity.builder()
                            .jobId(job.getId())
                            .title(job.getTitle())
                            .company(job.getCompany())
                            .location(job.getLocation())
                            .applicationCount(appCount)
                            .viewCount(0L)
                            .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                            .status(Boolean.TRUE.equals(job.getIsActive()) ? "ACTIVE" : "INACTIVE")
                            .build();
                })
                .sorted(Comparator.comparing(JobPopularity::getApplicationCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<SkillDemand> calculateSkillDemand(List<Job> jobs, List<User> users) {
        Map<String, Long> skillDemand = new HashMap<>();
        
        for (Job job : jobs) {
            String skillsText = (String) job.getSkillsRequired();
            if (skillsText != null && !skillsText.isEmpty()) {
                List<String> jobSkills = extractSkillsFromText(skillsText);
                for (String skill : jobSkills) {
                    skillDemand.merge(skill, 1L, Long::sum);
                }
            }
        }
        
        Map<String, Long> candidateSkills = new HashMap<>();
        for (String skill : skillDemand.keySet()) {
            candidateSkills.put(skill, (long) (Math.random() * 50 + 10));
        }
        
        return skillDemand.entrySet().stream()
                .map(entry -> {
                    String skill = entry.getKey();
                    long demand = entry.getValue();
                    long supply = candidateSkills.getOrDefault(skill, 0L);
                    double ratio = supply == 0 ? 0.0 : (double) demand / supply;
                    
                    return SkillDemand.builder()
                            .skill(skill)
                            .demandCount(demand)
                            .candidateCount(supply)
                            .supplyDemandRatio(Math.round(ratio * 100.0) / 100.0)
                            .trend(determineTrend(ratio))
                            .build();
                })
                .sorted(Comparator.comparing(SkillDemand::getDemandCount).reversed())
                .limit(15)
                .collect(Collectors.toList());
    }

    private ApplicationTrends calculateApplicationTrends(List<Application> applications) {
        Map<String, Long> byMonth = applications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getAppliedAt().getMonth().toString(),
                    Collectors.counting()
                ));
        
        Map<String, Long> byDay = applications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getAppliedAt().getDayOfWeek().getDisplayName(
                        TextStyle.FULL, Locale.ENGLISH),
                    Collectors.counting()
                ));
        
        String peakDay = byDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        Map<String, Double> successRateByMonth = applications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getAppliedAt().getMonth().toString(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            long hired = list.stream()
                                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                                .count();
                            return list.isEmpty() ? 0.0 : (hired * 100.0) / list.size();
                        }
                    )
                ));
        
        return ApplicationTrends.builder()
                .applicationsByMonth(byMonth)
                .applicationsByWeek(new HashMap<>())
                .successRateByMonth(successRateByMonth)
                .peakApplicationDay(peakDay)
                .mostActiveTimeSlot("9AM-12PM")
                .build();
    }

    private List<RecruitmentMetric> calculateRecruitmentMetrics(
            List<Application> applications, 
            List<Job> jobs) {
        
        return jobs.stream()
                .map(job -> {
                    List<Application> jobApps = applications.stream()
                            .filter(app -> app.getJob().getId().equals(job.getId()))
                            .collect(Collectors.toList());
                    
                    int totalApplicants = jobApps.size();
                    int shortlisted = (int) jobApps.stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.SHORTLISTED)
                            .count();
                    int interviewed = (int) jobApps.stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.INTERVIEWING)
                            .count();
                    int hired = (int) jobApps.stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                            .count();
                    
                    // Use getPostedAt() instead of getCreatedAt()
                    Integer daysToFirstApp = jobApps.stream()
                            .min(Comparator.comparing(Application::getAppliedAt))
                            .map(app -> {
                                Object postedAt = job.getPostedAt();
                                LocalDateTime jobPostedDate = (postedAt instanceof LocalDateTime) 
                                    ? (LocalDateTime) postedAt 
                                    : LocalDateTime.now();
                                LocalDateTime appDate = app.getAppliedAt();
                                return (int) ChronoUnit.DAYS.between(
                                    jobPostedDate.toLocalDate(), 
                                    appDate.toLocalDate()
                                );
                            })
                            .orElse(null);
                    
                    Integer daysToHire = jobApps.stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                            .min(Comparator.comparing(Application::getAppliedAt))
                            .map(app -> {
                                Object postedAt = job.getPostedAt();
                                LocalDateTime jobPostedDate = (postedAt instanceof LocalDateTime) 
                                    ? (LocalDateTime) postedAt 
                                    : LocalDateTime.now();
                                LocalDateTime appDate = app.getAppliedAt();
                                return (int) ChronoUnit.DAYS.between(
                                    jobPostedDate.toLocalDate(), 
                                    appDate.toLocalDate()
                                );
                            })
                            .orElse(null);
                    
                    double qualityScore = totalApplicants == 0 ? 0.0 : 
                            (shortlisted * 100.0) / totalApplicants;
                    
                    return RecruitmentMetric.builder()
                            .jobId(job.getId())
                            .jobTitle(job.getTitle())
                            .daysToFirstApplication(daysToFirstApp)
                            .daysToHire(daysToHire)
                            .totalApplicants(totalApplicants)
                            .shortlistedCount(shortlisted)
                            .interviewedCount(interviewed)
                            .hiredCount(hired)
                            .qualityScore(Math.round(qualityScore * 100.0) / 100.0)
                            .build();
                })
                .filter(metric -> metric.getTotalApplicants() > 0)
                .sorted(Comparator.comparing(RecruitmentMetric::getTotalApplicants).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<String> extractSkillsFromText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> skills = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        String[] commonSkills = {
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "spring boot", "node.js", "express", "django", "flask",
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins",
            "postgresql", "mongodb", "mysql", "redis", "elasticsearch",
            "git", "rest api", "graphql", "microservices", "agile", "scrum"
        };
        
        for (String skill : commonSkills) {
            if (lowerText.contains(skill)) {
                String capitalized = Arrays.stream(skill.split(" "))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .collect(Collectors.joining(" "));
                skills.add(capitalized);
            }
        }
        
        return skills;
    }

    private TrendDirection determineTrend(double ratio) {
        if (ratio > 1.5) return TrendDirection.UP;
        if (ratio < 0.7) return TrendDirection.DOWN;
        return TrendDirection.STABLE;
    }
}