package com.hackplay.hackplay.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hackplay.hackplay.common.ApiErrorResponses;
import com.hackplay.hackplay.common.BaseResponseStatus;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info().title("HackPlay API").version("v1.0"))
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                .addSecuritySchemes("BearerAuth",
                    new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer apiErrorResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            // 모든 엔드포인트 공통 에러
            addResponse(responses, BaseResponseStatus.INVALID_TOKEN);
            addResponse(responses, BaseResponseStatus.NO_PERMISSION);
            addResponse(responses, BaseResponseStatus.INTERNAL_SERVER_ERROR);

            // 메서드별 에러
            ApiErrorResponses annotation = handlerMethod.getMethodAnnotation(ApiErrorResponses.class);
            if (annotation == null) return operation;

            Map<String, List<String>> grouped = new LinkedHashMap<>();
            for (BaseResponseStatus status : annotation.value()) {
                String code = String.valueOf(status.getHttpStatus().value());
                grouped.computeIfAbsent(code, k -> new ArrayList<>()).add(status.getMessage());
            }

            for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                responses.addApiResponse(entry.getKey(),
                    new ApiResponse().description(String.join(" / ", entry.getValue())));
            }

            return operation;
        };
    }

    private void addResponse(ApiResponses responses, BaseResponseStatus status) {
        responses.addApiResponse(
            String.valueOf(status.getHttpStatus().value()),
            new ApiResponse().description(status.getMessage())
        );
    }
}
