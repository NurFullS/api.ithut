package com.backend.ithut.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class R2Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create("https://79a8e357c58a264c4a73f3ee6afbce0c.r2.cloudflarestorage.com"))

                .region(Region.US_EAST_1)

                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                "507b9f5d7eef06ecb41674004fb55121",
                                "c0b69884ab0e647a4810e08d19f88570c057e27d047125ae8b418ccef3cae19b"
                        )
                ))

                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
