package com.jobportal.backend.service;

import com.jobportal.backend.dto.SkillGapAnalysisResponse;
import com.jobportal.backend.dto.SkillGapAnalysisResponse.*;
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
public class SkillGapAnalysisServiceImpl implements SkillGapAnalysisService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Override
    public SkillGapAnalysisResponse analyzeSkillGap(Long userId, Long jobId) {
        log.info("Analyzing skill gap for user {} and job {}", userId, jobId);

        // Get user and job
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Extract skills
        List<String> userSkills = extractUserSkills(user);
        List<String> jobSkills = extractJobSkills(job);

        log.info("User has {} skills, job requires {} skills", userSkills.size(), jobSkills.size());

        // Analyze matches
        List<SkillMatch> matchingSkills = new ArrayList<>();
        List<MissingSkill> missingSkills = new ArrayList<>();

        for (String jobSkill : jobSkills) {
            boolean hasSkill = userSkills.stream()
                    .anyMatch(us -> us.equalsIgnoreCase(jobSkill));

            if (hasSkill) {
                // User has this skill
                matchingSkills.add(SkillMatch.builder()
                        .skill(jobSkill)
                        .userLevel(determineSkillLevel(jobSkill, user))
                        .requiredLevel("Advanced") // Could be extracted from job description
                        .status(MatchStatus.MATCHES)
                        .build());
            } else {
                // User missing this skill
                missingSkills.add(MissingSkill.builder()
                        .skill(jobSkill)
                        .requiredLevel("Advanced")
                        .priority(determineSkillPriority(jobSkill, job))
                        .learningResources(getLearningResources(jobSkill))
                        .estimatedLearningTime(estimateLearningTime(jobSkill))
                        .build());
            }
        }

        // Calculate match percentage
        int matchPercentage = jobSkills.isEmpty() ? 100 : 
                (int) ((matchingSkills.size() * 100.0) / jobSkills.size());

        // Generate recommendations
        List<String> recommendations = generateRecommendations(
                matchPercentage, matchingSkills, missingSkills, job);

        return SkillGapAnalysisResponse.builder()
                .matchPercentage(matchPercentage)
                .matchingSkills(matchingSkills)
                .missingSkills(missingSkills)
                .recommendations(recommendations)
                .jobInfo(JobInfo.builder()
                        .jobId(job.getId())
                        .title(job.getTitle())
                        .company(job.getCompany())
                        .experienceLevel(job.getExperienceRequired())
                        .build())
                .build();
    }

    private List<String> extractUserSkills(User user) {
        // In production, get from user.getProfile().getSkills()
        List<String> skills = new ArrayList<>();
        skills.add("Java");
        skills.add("Spring Boot");
        skills.add("PostgreSQL");
        skills.add("REST API");
        skills.add("Git");
        return skills;
    }

    private List<String> extractJobSkills(Job job) {
        List<String> skills = new ArrayList<>();
        String description = (job.getDescription() != null ? job.getDescription() : "").toLowerCase();

        String[] skillKeywords = {
            "java", "python", "javascript", "react", "angular", "vue",
            "spring boot", "spring", "node.js", "express",
            "aws", "azure", "docker", "kubernetes",
            "postgresql", "mysql", "mongodb", "redis",
            "git", "jenkins", "rest api", "graphql", "microservices"
        };

        for (String keyword : skillKeywords) {
            if (description.contains(keyword.toLowerCase())) {
                String capitalized = Arrays.stream(keyword.split(" "))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .collect(Collectors.joining(" "));
                skills.add(capitalized);
            }
        }

        return skills.isEmpty() ? Arrays.asList("Java", "Spring Boot", "AWS", "Docker") : skills;
    }

    private String determineSkillLevel(String skill, User user) {
        // In production, get from user skill proficiency data
        return "Advanced";
    }

    private SkillPriority determineSkillPriority(String skill, Job job) {
        // Determine priority based on job title and description
        String title = job.getTitle().toLowerCase();
        String description = job.getDescription() != null ? job.getDescription().toLowerCase() : "";

        // High priority if skill is in job title
        if (title.contains(skill.toLowerCase())) {
            return SkillPriority.HIGH;
        }

        // High priority for core technical skills
        List<String> criticalSkills = Arrays.asList(
                "java", "python", "javascript", "aws", "spring boot", "react"
        );
        if (criticalSkills.stream().anyMatch(cs -> cs.equalsIgnoreCase(skill))) {
            return SkillPriority.HIGH;
        }

        // Medium priority if mentioned multiple times
        int mentionCount = description.split(skill.toLowerCase(), -1).length - 1;
        if (mentionCount > 2) {
            return SkillPriority.MEDIUM;
        }

        return SkillPriority.LOW;
    }

    private List<LearningResource> getLearningResources(String skill) {
        // Curated learning resources for common skills
        Map<String, List<LearningResource>> resourceMap = new HashMap<>();

        // AWS Resources
        resourceMap.put("aws", Arrays.asList(
                LearningResource.builder()
                        .title("AWS Certified Solutions Architect Course")
                        .url("https://www.udemy.com/course/aws-certified-solutions-architect-associate/")
                        .duration("24 hours")
                        .type(ResourceType.COURSE)
                        .build(),
                LearningResource.builder()
                        .title("AWS Official Documentation")
                        .url("https://docs.aws.amazon.com/")
                        .duration("Self-paced")
                        .type(ResourceType.DOCUMENTATION)
                        .build()
        ));

        // Docker Resources
        resourceMap.put("docker", Arrays.asList(
                LearningResource.builder()
                        .title("Docker Crash Course")
                        .url("https://www.youtube.com/watch?v=pg19Z8LL06w")
                        .duration("4 hours")
                        .type(ResourceType.VIDEO)
                        .build(),
                LearningResource.builder()
                        .title("Docker Official Docs")
                        .url("https://docs.docker.com/")
                        .duration("Self-paced")
                        .type(ResourceType.DOCUMENTATION)
                        .build()
        ));

        // Kubernetes Resources
        resourceMap.put("kubernetes", Arrays.asList(
                LearningResource.builder()
                        .title("Kubernetes for Beginners")
                        .url("https://www.udemy.com/course/learn-kubernetes/")
                        .duration("8 hours")
                        .type(ResourceType.COURSE)
                        .build(),
                LearningResource.builder()
                        .title("Kubernetes Official Tutorial")
                        .url("https://kubernetes.io/docs/tutorials/")
                        .duration("Self-paced")
                        .type(ResourceType.TUTORIAL)
                        .build()
        ));

        // React Resources
        resourceMap.put("react", Arrays.asList(
                LearningResource.builder()
                        .title("React - The Complete Guide")
                        .url("https://www.udemy.com/course/react-the-complete-guide/")
                        .duration("40 hours")
                        .type(ResourceType.COURSE)
                        .build(),
                LearningResource.builder()
                        .title("Official React Documentation")
                        .url("https://react.dev/")
                        .duration("Self-paced")
                        .type(ResourceType.DOCUMENTATION)
                        .build()
        ));

        // Python Resources
        resourceMap.put("python", Arrays.asList(
                LearningResource.builder()
                        .title("Python for Everybody")
                        .url("https://www.coursera.org/specializations/python")
                        .duration("32 hours")
                        .type(ResourceType.COURSE)
                        .build()
        ));

        // Return resources or generic ones
        return resourceMap.getOrDefault(skill.toLowerCase(), 
                Collections.singletonList(
                        LearningResource.builder()
                                .title(skill + " Tutorial on YouTube")
                                .url("https://www.youtube.com/results?search_query=" + 
                                      skill.replace(" ", "+") + "+tutorial")
                                .duration("Varies")
                                .type(ResourceType.VIDEO)
                                .build()
                ));
    }

    private String estimateLearningTime(String skill) {
        // Estimate learning time based on skill complexity
        Map<String, String> timeEstimates = new HashMap<>();
        timeEstimates.put("aws", "4-6 weeks");
        timeEstimates.put("docker", "1-2 weeks");
        timeEstimates.put("kubernetes", "3-4 weeks");
        timeEstimates.put("react", "4-6 weeks");
        timeEstimates.put("angular", "4-6 weeks");
        timeEstimates.put("python", "6-8 weeks");
        timeEstimates.put("microservices", "3-4 weeks");
        timeEstimates.put("graphql", "2-3 weeks");

        return timeEstimates.getOrDefault(skill.toLowerCase(), "2-4 weeks");
    }

    private List<String> generateRecommendations(
            int matchPercentage, 
            List<SkillMatch> matching, 
            List<MissingSkill> missing,
            Job job) {
        
        List<String> recommendations = new ArrayList<>();

        if (matchPercentage >= 80) {
            recommendations.add("🎉 Excellent match! You meet most requirements for this role.");
            recommendations.add("Apply now - you're well-qualified for this position!");
        } else if (matchPercentage >= 60) {
            recommendations.add("✅ Good match! You have a solid foundation for this role.");
            recommendations.add("Consider learning the missing skills to become a perfect fit.");
        } else if (matchPercentage >= 40) {
            recommendations.add("⚠️ Moderate match. You have some relevant skills.");
            recommendations.add("Focus on gaining the high-priority missing skills before applying.");
        } else {
            recommendations.add("📚 This role may be challenging with your current skillset.");
            recommendations.add("Consider roles that better match your current skills.");
        }

        // Highlight strengths
        if (!matching.isEmpty()) {
            String topSkills = matching.stream()
                    .limit(3)
                    .map(SkillMatch::getSkill)
                    .collect(Collectors.joining(", "));
            recommendations.add("💪 Your strengths: " + topSkills);
        }

        // Prioritize learning
        List<MissingSkill> highPriority = missing.stream()
                .filter(ms -> ms.getPriority() == SkillPriority.HIGH)
                .limit(2)
                .collect(Collectors.toList());

        if (!highPriority.isEmpty()) {
            String prioritySkills = highPriority.stream()
                    .map(MissingSkill::getSkill)
                    .collect(Collectors.joining(", "));
            recommendations.add("🎯 Priority skills to learn: " + prioritySkills);
        }

        return recommendations;
    }
}