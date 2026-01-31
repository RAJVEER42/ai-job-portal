package com.jobportal.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration for JobPortal API Documentation
 * 
 * Features:
 * - Comprehensive API documentation
 * - JWT authentication setup
 * - Environment-based server configuration
 * - Professional API documentation presentation
 * - Rate limiting documentation
 * - Error response documentation
 */
@Configuration
public class OpenAPIConfig {

    @Value("${app.api.version:1.0.0}")
    private String apiVersion;

    @Value("${app.api.title:JobPortal API}")
    private String apiTitle;

    @Value("${app.api.description:AI-Powered Job Portal Backend API}")
    private String apiDescription;

    @Value("${app.api.server-url:http://localhost:8080}")
    private String serverUrl;

    @Value("${app.api.server-description:Development Server}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(List.of(getServer()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", getBearerAuthScheme()));
    }

    private Info getApiInfo() {
        return new Info()
                .title(apiTitle)
                .description(getDetailedDescription())
                .version(apiVersion)
                .contact(getContactInfo())
                .license(getLicenseInfo());
    }

    private String getDetailedDescription() {
        return apiDescription + "\n\n" +
                "## Features\n" +
                "- 🔐 **JWT Authentication** - Secure token-based authentication\n" +
                "- 🚀 **AI-Powered** - Resume parsing and job recommendations\n" +
                "- 📧 **Email Notifications** - Automated email system\n" +
                "- ⚡ **High Performance** - Redis caching and performance monitoring\n" +
                "- 🛡️ **Rate Limited** - Request throttling for stability\n" +
                "- 📊 **Monitoring** - Prometheus metrics and health checks\n" +
                "- 🔍 **Advanced Search** - Full-text job search capabilities\n" +
                "- 📋 **Application Tracking** - Complete application lifecycle management\n\n" +
                "## Rate Limiting\n" +
                "- **General Endpoints**: 60 requests/minute\n" +
                "- **Authentication**: 10 requests/minute\n" +
                "- **Search**: 100 requests/minute\n" +
                "- **Burst Limit**: 10 requests/10 seconds\n\n" +
                "## Authentication\n" +
                "1. Register or login at `/api/auth/login`\n" +
                "2. Use the returned JWT token in the Authorization header\n" +
                "3. Format: `Authorization: Bearer <token>`\n\n" +
                "## Error Handling\n" +
                "All endpoints return standardized error responses:\n" +
                "```json\n" +
                "{\n" +
                "  \"error\": \"Error type\",\n" +
                "  \"message\": \"Detailed error message\",\n" +
                "  \"status\": 400,\n" +
                "  \"timestamp\": \"2026-01-26T10:30:00Z\"\n" +
                "}\n" +
                "```";
    }

    private Contact getContactInfo() {
        return new Contact()
                .name("JobPortal API Team")
                .email("api-support@jobportal.com")
                .url("https://jobportal.com/support");
    }

    private License getLicenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private Server getServer() {
        return new Server()
                .url(serverUrl)
                .description(serverDescription);
    }

    private SecurityScheme getBearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token obtained from login endpoint");
    }
}
