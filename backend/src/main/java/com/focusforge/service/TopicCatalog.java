package com.focusforge.service;

import com.focusforge.domain.TopicArea;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Starter topics seeded for each new user. Users can add their own. */
public final class TopicCatalog {
    private TopicCatalog() {}

    public static final String HANDS_ON_GROUP = "Hands-on";

    public static final Map<TopicArea, Map<String, List<String>>> ALL = new LinkedHashMap<>();

    static {
        Map<String, List<String>> aws = new LinkedHashMap<>();
        aws.put("Fundamentals", List.of("Cloud computing", "Regions", "Availability Zones", "Shared Responsibility Model"));
        aws.put("IAM", List.of("Users", "Groups", "Roles", "Policies", "Permissions"));
        aws.put("Compute", List.of("EC2", "Lambda"));
        aws.put("Storage", List.of("S3"));
        aws.put("Database", List.of("DynamoDB", "RDS"));
        aws.put("API", List.of("API Gateway"));
        aws.put("Networking & Monitoring", List.of("VPC basics", "CloudWatch"));
        aws.put("Deployment", List.of("Backend deployment", "API deployment", "Cloud architecture"));
        aws.put(HANDS_ON_GROUP, List.of("Created S3 bucket", "Created EC2 instance", "Configured IAM role",
                "Created Lambda function", "Created API Gateway", "Deployed application"));
        ALL.put(TopicArea.AWS, aws);

        Map<String, List<String>> dsa = new LinkedHashMap<>();
        dsa.put("Foundations", List.of("Arrays", "Strings", "Hashing", "Two Pointers", "Sliding Window"));
        dsa.put("Searching & Sorting", List.of("Binary Search", "Sorting"));
        dsa.put("Linear structures", List.of("Linked List", "Stack", "Queue"));
        dsa.put("Non-linear structures", List.of("Trees", "Graphs", "Heap"));
        dsa.put("Techniques", List.of("Recursion", "Backtracking", "Greedy", "Dynamic Programming"));
        ALL.put(TopicArea.DSA, dsa);

        Map<String, List<String>> sql = new LinkedHashMap<>();
        sql.put("Querying", List.of("SELECT", "WHERE", "GROUP BY", "HAVING", "Aggregations"));
        sql.put("Combining data", List.of("JOIN", "SELF JOIN", "Subqueries", "CTE"));
        sql.put("Advanced", List.of("Window Functions", "Indexes", "Transactions", "Normalization"));
        ALL.put(TopicArea.SQL, sql);

        Map<String, List<String>> java = new LinkedHashMap<>();
        java.put("Object-oriented", List.of("OOP", "Classes & Objects", "Inheritance", "Polymorphism", "Abstraction", "Interfaces"));
        java.put("Core APIs", List.of("Collections", "Generics", "Exception Handling", "Streams", "Lambda"));
        java.put("Runtime & concurrency", List.of("Multithreading", "JDBC", "JVM", "Memory management"));
        ALL.put(TopicArea.JAVA, java);

        Map<String, List<String>> interview = new LinkedHashMap<>();
        interview.put("Core", List.of("Java", "OOP", "SQL", "DBMS"));
        interview.put("Backend", List.of("Spring Boot", "REST APIs", "AWS"));
        interview.put("Other", List.of("AI/ML", "Projects", "HR"));
        ALL.put(TopicArea.INTERVIEW, interview);
    }
}
