package com.jobportal.backend.service;

import com.jobportal.backend.dto.JobRecommendationResponse;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.JobRepository;
import com.jobportal.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobRecommendationServiceImpl implements JobRecommendationService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Override
    public List<JobRecommendationResponse> getRecommendations(Long userId, Integer limit) {
        log.info("Generating job recommendations for user ID: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get user's skills (parse from profile or use a dedicated field)
        List<String> userSkills = extractUserSkills(user);
        log.info("User has {} skills: {}", userSkills.size(), userSkills);

        // Get all active jobs
        List<Job> allJobs = jobRepository.findAll();
        log.info("Found {} total jobs in database", allJobs.size());

        // Calculate match scores for each job
        List<JobRecommendationResponse> recommendations = allJobs.stream()
                .map(job -> calculateMatchScore(job, user, userSkills))
                .filter(rec -> rec.getMatchScore() > 0) // Only show jobs with some match
                .sorted(Comparator.comparingInt(JobRecommendationResponse::getMatchScore).reversed())
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        log.info("Generated {} recommendations with scores > 0", recommendations.size());
        return recommendations;
    }

    private JobRecommendationResponse calculateMatchScore(Job job, User user, List<String> userSkills) {
        List<String> matchReasons = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        int totalScore = 0;

        // Extract required skills from job description or skills field
        List<String> jobSkills = extractJobSkills(job);

        // 1. SKILL MATCHING (60 points max)
        if (!jobSkills.isEmpty()) {
            int matchingSkillsCount = 0;
            for (String jobSkill : jobSkills) {
                boolean matched = userSkills.stream()
                        .anyMatch(userSkill -> userSkill.equalsIgnoreCase(jobSkill));
                if (matched) {
                    matchingSkillsCount++;
                } else {
                    missingSkills.add(jobSkill);
                }
            }

            int skillScore = (int) ((matchingSkillsCount * 60.0) / jobSkills.size());
            totalScore += skillScore;

            if (matchingSkillsCount > 0) {
                matchReasons.add(String.format("Skills match: %d/%d required skills (%s)",
                        matchingSkillsCount, jobSkills.size(),
                        userSkills.stream()
                                .filter(us -> jobSkills.stream().anyMatch(js -> js.equalsIgnoreCase(us)))
                                .limit(3)
                                .collect(Collectors.joining(", "))));
            }
        }

        // 2. LOCATION MATCHING (20 points max)
        if (job.getDescription() != null && user.getEmail() != null) {
            // Simple location match - in production, use user's profile location
            String userLocation = extractUserLocation(user);
            if (userLocation != null && 
                job.getDescription().toLowerCase().contains(userLocation.toLowerCase())) {
                totalScore += 20;
                matchReasons.add("Location match: " + job.getDescription());
            } else if (job.getDescription().toLowerCase().contains("remote")) {
                totalScore += 15;
                matchReasons.add("Remote work available");
            }
        }

        // 3. EXPERIENCE LEVEL MATCHING (20 points max)
        if (job.getExperienceRequired() != null) {
            // Simple heuristic - in production, get from user profile
            int userYearsExp = estimateUserExperience(user);
            boolean expMatch = checkExperienceMatch(job.getExperienceRequired(), userYearsExp);
            
            if (expMatch) {
                totalScore += 20;
                matchReasons.add(String.format("Experience level matches: %s", job.getExperienceRequired()));
            } else {
                int expGap = calculateExperienceGap(job.getExperienceRequired(), userYearsExp);
                if (expGap <= 2) {
                    totalScore += 10;
                    matchReasons.add("Close experience match");
                }
            }
        }

        // Build response
        return JobRecommendationResponse.builder()
                .job(JobRecommendationResponse.JobSummary.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .company(job.getCompany())
                        .location(job.getDescription())
                        .jobType(job.getJobType() != null ? job.getJobType().toString() : null)
                        .experienceLevel(job.getExperienceRequired())
                        .requiredSkills(jobSkills)
                        .build())
                .matchScore(totalScore)
                .matchReasons(matchReasons.isEmpty() ? 
                        Collections.singletonList("No specific matches found") : matchReasons)
                .missingSkills(missingSkills)
                .build();
    }

    private List<String> extractUserSkills(User user) {
        // In production, get from user.getSkills() or user.getProfile().getSkills()
        // For now, extract from email or use hardcoded demo skills
        List<String> skills = new ArrayList<>();
        
        // Simple heuristic based on user data
        // In real app, you'd have a separate user.skills field
        skills.add("Java");
        skills.add("Spring Boot");
        skills.add("PostgreSQL");
        
        log.debug("Extracted {} skills for user {}", skills.size(), user.getId());
        return skills;
    }

    private List<String> extractJobSkills(Job job) {
        List<String> skills = new ArrayList<>();
        
        // Extract skills from job description using keywords
        String description = (job.getDescription() != null ? job.getDescription() : "").toLowerCase();
        
        // Common tech skills to look for
        String[] skillKeywords = {
            "java", "python", "javascript", "react", "angular", "vue",
            "spring boot", "spring", "node.js", "express",
            "aws", "azure", "gcp", "docker", "kubernetes",
            "postgresql", "mysql", "mongodb", "redis",
            "git", "jenkins", "ci/cd", "microservices",
            "rest api", "graphql"
        };
        
        for (String keyword : skillKeywords) {
            if (description.contains(keyword.toLowerCase())) {
                // Capitalize first letter of each word
                String capitalized = Arrays.stream(keyword.split(" "))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .collect(Collectors.joining(" "));
                skills.add(capitalized);
            }
        }
        
        return skills;
    }

    private String extractUserLocation(User user) {
        // In production, get from user.getProfile().getLocation()
        // For now, return a default location
        return "Bengaluru";
    }

    private int estimateUserExperience(User user) {
        // In production, calculate from user.getProfile().getExperience()
        // For now, estimate 2-3 years
        return 3;
    }

    private boolean checkExperienceMatch(String jobExpLevel, int userYears) {
        if (jobExpLevel == null) return true;
        
        String level = jobExpLevel.toLowerCase();
        
        if (level.contains("entry") || level.contains("junior") || level.contains("0-2")) {
            return userYears <= 2;
        } else if (level.contains("mid") || level.contains("2-5") || level.contains("3-5")) {
            return userYears >= 2 && userYears <= 5;
        } else if (level.contains("senior") || level.contains("5+") || level.contains("lead")) {
            return userYears >= 5;
        }
        
        return true; // Default to match if can't determine
    }

    private int calculateExperienceGap(String jobExpLevel, int userYears) {
        if (jobExpLevel == null) return 0;
        
        String level = jobExpLevel.toLowerCase();
        
        if (level.contains("0-2") || level.contains("entry")) {
            return Math.abs(userYears - 1);
        } else if (level.contains("2-5") || level.contains("mid")) {
            return Math.abs(userYears - 3);
        } else if (level.contains("5+") || level.contains("senior")) {
            return Math.abs(userYears - 6);
        }
        
        return 0;
    }
}