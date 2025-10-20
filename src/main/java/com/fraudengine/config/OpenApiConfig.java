package com.fraudengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudRuleEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud Rule Engine API")
                        .version("1.0.0")
                        .description("""
                                A configurable and composable fraud detection engine built with Spring Boot 3 and PostgreSQL.
                                
                                The API allows:
                                - Evaluating single or batch transactions
                                - Retrieving flagged transaction results
                                - Exploring rule outcomes and reason codes
                                """)
                        .contact(new Contact()
                                .name("Fraud Engine Demo")
                                .email("demo@example.com")
                                .url("https://github.com/fraud-engine"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Docker / Development Server")
                ))
                .components(new Components()
                        .addResponses("ExampleResponses", new ApiResponse()
                                .description("Example Fraud Evaluation Response")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("example",
                                                new Example().value("""
                                                        {
                                                          "compositeScore": 47.7,
                                                          "flagged": true,
                                                          "decision": "REVIEW",
                                                          "reasonCodes": ["AMOUNT_SPIKE", "MCC_RISK"],
                                                          "details": {
                                                            "AMOUNT_SPIKE": {
                                                              "rawScore": 100,
                                                              "weighted": 27.0,
                                                              "details": {
                                                                "amount": 9500,
                                                                "avgAmount": 100,
                                                                "ratio": 95.0
                                                              }
                                                            },
                                                            "MCC_RISK": {
                                                              "rawScore": 50,
                                                              "weighted": 6.2,
                                                              "details": {
                                                                "mcc": "5812",
                                                                "scoreBase": 50
                                                              }
                                                            }
                                                          }
                                                        }
                                                        """))
                                ))
                        )
                        .addParameters("paginationParams",
                                new Parameter()
                                        .name("page")
                                        .description("Page number (starting at 0)")
                                        .example(0)
                                        .required(false))
                        .addParameters("pageSizeParams",
                                new Parameter()
                                        .name("size")
                                        .description("Page size (default 10)")
                                        .example(10)
                                        .required(false))
                        .addParameters("filterParams",
                                new Parameter()
                                        .name("decision")
                                        .description("Decision filter (APPROVE, REVIEW, DECLINE)")
                                        .example("REVIEW")
                                        .required(false))
                        .addParameters("fromParam",
                                new Parameter()
                                        .name("from")
                                        .description("Start of date range (ISO-8601)")
                                        .example("2025-10-19T00:00:00Z")
                                        .required(false))
                        .addParameters("toParam",
                                new Parameter()
                                        .name("to")
                                        .description("End of date range (ISO-8601)")
                                        .example("2025-10-20T00:00:00Z")
                                        .required(false))
                        .addParameters("customerParam",
                                new Parameter()
                                        .name("customerId")
                                        .description("Customer UUID to query flagged transactions")
                                        .example("11111111-2222-3333-4444-555555555555")
                                        .required(true))
                );
    }
}
