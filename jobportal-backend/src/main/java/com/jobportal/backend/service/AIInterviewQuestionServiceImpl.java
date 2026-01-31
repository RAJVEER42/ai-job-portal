package com.jobportal.backend.service;

import com.jobportal.backend.dto.GenerateInterviewQuestionsRequest;
import com.jobportal.backend.dto.InterviewQuestionResponse;
import com.jobportal.backend.dto.InterviewQuestionResponse.QuestionDifficulty;
import com.jobportal.backend.model.Job;
import com.jobportal.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIInterviewQuestionServiceImpl implements AIInterviewQuestionService {

    private final JobRepository jobRepository;
    
    // Question bank organized by skill and difficulty
    private static final Map<String, Map<QuestionDifficulty, List<QuestionTemplate>>> QUESTION_BANK = new HashMap<>();
    
    static {
        initializeQuestionBank();
    }

    @Override
    public List<InterviewQuestionResponse> generateQuestions(GenerateInterviewQuestionsRequest request) {
        log.info("Generating {} interview questions for job {} at difficulty {}", 
                request.getCount(), request.getJobId(), request.getDifficulty());

        // Get job details
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + request.getJobId()));

        // Extract skills from job
        List<String> jobSkills = extractJobSkills(job);
        log.info("Found {} skills in job: {}", jobSkills.size(), jobSkills);

        // Generate questions based on skills and difficulty
        List<InterviewQuestionResponse> questions = new ArrayList<>();
        QuestionDifficulty targetDifficulty = request.getDifficulty() != null ? 
                request.getDifficulty() : QuestionDifficulty.MEDIUM;

        for (String skill : jobSkills) {
            List<QuestionTemplate> templates = getQuestionsForSkill(skill, targetDifficulty);
            
            for (QuestionTemplate template : templates) {
                if (questions.size() >= request.getCount()) {
                    break;
                }
                questions.add(buildQuestion(template, job));
            }
            
            if (questions.size() >= request.getCount()) {
                break;
            }
        }

        // If not enough skill-specific questions, add general questions
        if (questions.size() < request.getCount()) {
            List<QuestionTemplate> generalQuestions = getGeneralQuestions(targetDifficulty);
            for (QuestionTemplate template : generalQuestions) {
                if (questions.size() >= request.getCount()) {
                    break;
                }
                questions.add(buildQuestion(template, job));
            }
        }

        // Shuffle for variety
        Collections.shuffle(questions);
        
        log.info("Generated {} questions successfully", questions.size());
        return questions.stream().limit(request.getCount()).collect(Collectors.toList());
    }

    private List<String> extractJobSkills(Job job) {
        List<String> skills = new ArrayList<>();
        String description = (job.getDescription() != null ? job.getDescription() : "").toLowerCase();

        String[] skillKeywords = {
            "java", "python", "javascript", "react", "angular", "spring boot",
            "node.js", "aws", "docker", "kubernetes", "postgresql", "mongodb",
            "microservices", "rest api", "graphql", "git"
        };

        for (String keyword : skillKeywords) {
            if (description.contains(keyword)) {
                skills.add(keyword);
            }
        }

        return skills.isEmpty() ? Arrays.asList("java", "general") : skills;
    }

    private List<QuestionTemplate> getQuestionsForSkill(String skill, QuestionDifficulty difficulty) {
        Map<QuestionDifficulty, List<QuestionTemplate>> skillQuestions = QUESTION_BANK.get(skill.toLowerCase());
        
        if (skillQuestions == null) {
            return Collections.emptyList();
        }

        return skillQuestions.getOrDefault(difficulty, Collections.emptyList());
    }

    private List<QuestionTemplate> getGeneralQuestions(QuestionDifficulty difficulty) {
        return QUESTION_BANK.getOrDefault("general", new HashMap<>())
                .getOrDefault(difficulty, Collections.emptyList());
    }

    private InterviewQuestionResponse buildQuestion(QuestionTemplate template, Job job) {
        return InterviewQuestionResponse.builder()
                .question(template.question)
                .difficulty(template.difficulty)
                .category(template.category)
                .expectedAnswer(template.expectedAnswer)
                .tags(template.tags)
                .followUpQuestions(template.followUpQuestions)
                .build();
    }

    // Initialize comprehensive question bank
    private static void initializeQuestionBank() {
        // JAVA QUESTIONS
        Map<QuestionDifficulty, List<QuestionTemplate>> javaQuestions = new HashMap<>();
        
        javaQuestions.put(QuestionDifficulty.EASY, Arrays.asList(
            new QuestionTemplate(
                "What is the difference between JDK, JRE, and JVM?",
                QuestionDifficulty.EASY,
                "Java Basics",
                "JDK (Java Development Kit) is a software development kit that includes JRE plus development tools like compiler (javac). JRE (Java Runtime Environment) provides libraries and JVM to run Java applications. JVM (Java Virtual Machine) executes Java bytecode and provides platform independence.",
                Arrays.asList("Java", "JVM", "Basics"),
                Arrays.asList("Can you explain Java's 'Write Once, Run Anywhere' principle?")
            ),
            new QuestionTemplate(
                "Explain the difference between == and .equals() in Java.",
                QuestionDifficulty.EASY,
                "Java Basics",
                "== compares object references (memory addresses), while .equals() compares object contents. For primitives, == compares values. For objects, == checks if both references point to the same object, whereas .equals() (when properly overridden) checks logical equality.",
                Arrays.asList("Java", "Operators", "Object Comparison"),
                Arrays.asList("What happens if you don't override .equals() in a custom class?")
            )
        ));

        javaQuestions.put(QuestionDifficulty.MEDIUM, Arrays.asList(
            new QuestionTemplate(
                "Explain Java's memory management and garbage collection.",
                QuestionDifficulty.MEDIUM,
                "Java Memory",
                "Java uses automatic memory management. Objects are created in heap memory. Garbage Collection automatically frees memory by removing objects with no references. Key areas: Young Generation (Eden, Survivor spaces) and Old Generation. Common GC algorithms: Serial, Parallel, CMS, G1GC.",
                Arrays.asList("Java", "Memory Management", "GC"),
                Arrays.asList("How would you tune JVM for better performance?", "What is a memory leak in Java?")
            ),
            new QuestionTemplate(
                "What are Java Streams and how do they work?",
                QuestionDifficulty.MEDIUM,
                "Java 8+ Features",
                "Streams are a sequence of elements supporting sequential and parallel aggregate operations. They enable functional-style operations on collections. Key features: lazy evaluation, intermediate (filter, map) and terminal (collect, forEach) operations, and support for parallel processing with parallelStream().",
                Arrays.asList("Java", "Streams", "Functional Programming"),
                Arrays.asList("What's the difference between Collection and Stream?")
            )
        ));

        javaQuestions.put(QuestionDifficulty.HARD, Arrays.asList(
            new QuestionTemplate(
                "Explain the Java Memory Model and happens-before relationship.",
                QuestionDifficulty.HARD,
                "Concurrency",
                "The Java Memory Model defines how threads interact through memory and what behaviors are allowed. Happens-before establishes memory visibility guarantees between operations. Key rules: synchronization, volatile variables, thread start/join, and final fields create happens-before relationships ensuring visibility of changes across threads.",
                Arrays.asList("Java", "Concurrency", "Memory Model"),
                Arrays.asList("How does volatile keyword work?", "Explain the double-checked locking problem.")
            ),
            new QuestionTemplate(
                "Design a thread-safe singleton class in Java.",
                QuestionDifficulty.HARD,
                "Design Patterns",
                "Several approaches: 1) Eager initialization (thread-safe by default), 2) Synchronized method (performance overhead), 3) Double-checked locking with volatile, 4) Bill Pugh Singleton (inner static helper class - recommended), 5) Enum singleton (Joshua Bloch recommended). Each has trade-offs in performance and lazy initialization.",
                Arrays.asList("Java", "Design Patterns", "Thread Safety"),
                Arrays.asList("How would you prevent singleton from being broken by reflection or serialization?")
            )
        ));

        QUESTION_BANK.put("java", javaQuestions);

        // SPRING BOOT QUESTIONS
        Map<QuestionDifficulty, List<QuestionTemplate>> springQuestions = new HashMap<>();
        
        springQuestions.put(QuestionDifficulty.EASY, Arrays.asList(
            new QuestionTemplate(
                "What is Spring Boot and how does it differ from Spring Framework?",
                QuestionDifficulty.EASY,
                "Spring Boot Basics",
                "Spring Boot is an opinionated framework built on top of Spring that simplifies application development through auto-configuration, embedded servers, and starter dependencies. Unlike traditional Spring which requires extensive XML/annotation configuration, Spring Boot provides sensible defaults and 'convention over configuration' approach.",
                Arrays.asList("Spring Boot", "Framework"),
                Arrays.asList("What are Spring Boot starters?")
            ),
            new QuestionTemplate(
                "Explain the difference between @Component, @Service, and @Repository annotations.",
                QuestionDifficulty.EASY,
                "Spring Annotations",
                "@Component is a generic stereotype for any Spring-managed component. @Service is used for service layer classes (business logic). @Repository is for DAO classes (data access) and provides additional features like exception translation. All are functionally similar but serve different semantic purposes for better code organization.",
                Arrays.asList("Spring Boot", "Annotations", "Stereotype"),
                Arrays.asList("When would you use @Controller vs @RestController?")
            )
        ));

        springQuestions.put(QuestionDifficulty.MEDIUM, Arrays.asList(
            new QuestionTemplate(
                "Explain Spring Boot's auto-configuration mechanism.",
                QuestionDifficulty.MEDIUM,
                "Spring Boot",
                "Auto-configuration automatically configures Spring application based on classpath dependencies. It uses @Conditional annotations to apply configurations only when certain conditions are met. Defined in spring.factories files, it checks for specific classes/beans and configures them automatically. Can be customized/disabled using @EnableAutoConfiguration(exclude=...) or properties.",
                Arrays.asList("Spring Boot", "Auto-configuration"),
                Arrays.asList("How would you create custom auto-configuration?")
            ),
            new QuestionTemplate(
                "How does Spring Boot handle transaction management?",
                QuestionDifficulty.MEDIUM,
                "Spring Transactions",
                "Spring Boot uses @Transactional annotation for declarative transaction management. It creates proxies around beans to handle transaction boundaries. Key aspects: propagation levels (REQUIRED, REQUIRES_NEW, etc.), isolation levels, rollback rules (checked vs unchecked exceptions), and the importance of public methods for proxy-based transactions.",
                Arrays.asList("Spring Boot", "Transactions", "Database"),
                Arrays.asList("What happens if you call @Transactional method from another method in the same class?")
            )
        ));

        springQuestions.put(QuestionDifficulty.HARD, Arrays.asList(
            new QuestionTemplate(
                "Design a microservices architecture using Spring Boot.",
                QuestionDifficulty.HARD,
                "Microservices Architecture",
                "Key components: 1) Service Discovery (Eureka), 2) API Gateway (Spring Cloud Gateway), 3) Config Server (centralized configuration), 4) Circuit Breaker (Resilience4j), 5) Distributed Tracing (Sleuth + Zipkin), 6) Message Queue (RabbitMQ/Kafka), 7) Database per service pattern. Challenges: distributed transactions, data consistency, service communication.",
                Arrays.asList("Spring Boot", "Microservices", "Architecture"),
                Arrays.asList("How would you handle distributed transactions?", "Explain the saga pattern.")
            )
        ));

        QUESTION_BANK.put("spring boot", springQuestions);
        QUESTION_BANK.put("spring", springQuestions);

        // DATABASE QUESTIONS
        Map<QuestionDifficulty, List<QuestionTemplate>> dbQuestions = new HashMap<>();
        
        dbQuestions.put(QuestionDifficulty.MEDIUM, Arrays.asList(
            new QuestionTemplate(
                "Explain database indexing and when to use it.",
                QuestionDifficulty.MEDIUM,
                "Database",
                "Indexes are data structures (usually B-trees) that improve query performance by providing quick lookups. They speed up SELECT queries but slow down INSERT/UPDATE/DELETE. Use indexes on: frequently queried columns, foreign keys, columns in WHERE/JOIN/ORDER BY clauses. Avoid on: frequently updated columns, small tables, columns with low cardinality.",
                Arrays.asList("Database", "PostgreSQL", "Performance"),
                Arrays.asList("What's the difference between clustered and non-clustered indexes?")
            )
        ));

        QUESTION_BANK.put("postgresql", dbQuestions);
        QUESTION_BANK.put("mongodb", dbQuestions);

        // AWS QUESTIONS
        Map<QuestionDifficulty, List<QuestionTemplate>> awsQuestions = new HashMap<>();
        
        awsQuestions.put(QuestionDifficulty.MEDIUM, Arrays.asList(
            new QuestionTemplate(
                "Explain the difference between EC2, ECS, and Lambda.",
                QuestionDifficulty.MEDIUM,
                "AWS Compute",
                "EC2 (Elastic Compute Cloud) provides virtual servers with full control over OS. ECS (Elastic Container Service) manages Docker containers on EC2 instances. Lambda is serverless - runs code without managing servers, pay per execution. Use EC2 for traditional apps, ECS for containerized workloads, Lambda for event-driven/short-running tasks.",
                Arrays.asList("AWS", "Cloud", "Compute"),
                Arrays.asList("When would you choose Lambda over EC2?")
            )
        ));

        QUESTION_BANK.put("aws", awsQuestions);

        // REACT QUESTIONS
        Map<QuestionDifficulty, List<QuestionTemplate>> reactQuestions = new HashMap<>();
        
        reactQuestions.put(QuestionDifficulty.EASY, Arrays.asList(
            new QuestionTemplate(
                "What is the virtual DOM and how does React use it?",
                QuestionDifficulty.EASY,
                "React Basics",
                "Virtual DOM is a lightweight JavaScript representation of the actual DOM. React maintains two virtual DOM trees: current and updated. When state changes, React creates a new virtual DOM, compares it with the previous one (diffing), and updates only the changed parts in real DOM. This makes updates efficient.",
                Arrays.asList("React", "Virtual DOM", "Performance"),
                Arrays.asList("How does React's reconciliation algorithm work?")
            )
        ));

        reactQuestions.put(QuestionDifficulty.MEDIUM, Arrays.asList(
            new QuestionTemplate(
                "Explain React hooks and when to use useState vs useEffect.",
                QuestionDifficulty.MEDIUM,
                "React Hooks",
                "Hooks let you use state and lifecycle features in functional components. useState manages component state. useEffect handles side effects (API calls, subscriptions, DOM updates) and replaces lifecycle methods. useState for reactive data that triggers re-renders. useEffect for operations that need to run after render or cleanup.",
                Arrays.asList("React", "Hooks", "State Management"),
                Arrays.asList("What are the rules of hooks?", "Explain useCallback and useMemo.")
            )
        ));

        QUESTION_BANK.put("react", reactQuestions);

        // GENERAL/SYSTEM DESIGN QUESTIONS
        Map<QuestionDifficulty, List<QuestionTemplate>> generalQuestions = new HashMap<>();
        
        generalQuestions.put(QuestionDifficulty.MEDIUM, Arrays.asList(
            new QuestionTemplate(
                "How would you design a URL shortening service like bit.ly?",
                QuestionDifficulty.MEDIUM,
                "System Design",
                "Key components: 1) Hash function to generate short URLs (Base62 encoding), 2) Database to store mappings (URL_ID, original_URL, short_URL, created_at), 3) Cache layer (Redis) for popular URLs, 4) Load balancer for scalability, 5) Analytics tracking. Consider: collision handling, expiration, custom URLs, rate limiting.",
                Arrays.asList("System Design", "Architecture", "Scalability"),
                Arrays.asList("How would you handle 1 billion URLs?", "What database would you choose and why?")
            ),
            new QuestionTemplate(
                "Explain REST API best practices.",
                QuestionDifficulty.MEDIUM,
                "API Design",
                "Best practices: 1) Use HTTP methods correctly (GET, POST, PUT, DELETE), 2) Resource-based URLs (/users/123), 3) Proper status codes (200, 201, 400, 404, 500), 4) Versioning (/api/v1/), 5) Pagination for large datasets, 6) Authentication (JWT/OAuth), 7) Rate limiting, 8) HATEOAS for discoverability, 9) Consistent error responses, 10) Documentation (Swagger/OpenAPI).",
                Arrays.asList("REST API", "Best Practices", "Web Services"),
                Arrays.asList("What's the difference between PUT and PATCH?")
            )
        ));

        generalQuestions.put(QuestionDifficulty.HARD, Arrays.asList(
            new QuestionTemplate(
                "Design a real-time chat application architecture.",
                QuestionDifficulty.HARD,
                "System Design",
                "Architecture: 1) WebSocket servers for real-time bi-directional communication, 2) Message queue (Kafka/RabbitMQ) for reliable delivery, 3) Chat servers (stateful) with session affinity, 4) Database (Cassandra/MongoDB) for message persistence, 5) Redis for online user status, 6) CDN for media, 7) Load balancer with sticky sessions. Handle: message ordering, offline messages, read receipts, typing indicators.",
                Arrays.asList("System Design", "Real-time", "Architecture"),
                Arrays.asList("How would you handle message ordering in a distributed system?")
            )
        ));

        QUESTION_BANK.put("general", generalQuestions);
        QUESTION_BANK.put("rest api", generalQuestions);
        QUESTION_BANK.put("microservices", generalQuestions);
    }

    // Helper class to hold question templates
    private static class QuestionTemplate {
        String question;
        QuestionDifficulty difficulty;
        String category;
        String expectedAnswer;
        List<String> tags;
        List<String> followUpQuestions;

        QuestionTemplate(String question, QuestionDifficulty difficulty, String category,
                        String expectedAnswer, List<String> tags, List<String> followUpQuestions) {
            this.question = question;
            this.difficulty = difficulty;
            this.category = category;
            this.expectedAnswer = expectedAnswer;
            this.tags = tags;
            this.followUpQuestions = followUpQuestions;
        }
    }
}