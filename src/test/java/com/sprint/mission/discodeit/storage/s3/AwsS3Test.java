package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.s3.AwsProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

@SpringBootTest
public class AwsS3Test {

    @Autowired
    private AwsProperties awsProperties;

    @Autowired
    private S3Client s3;


    @Test
    public void uploadTest() {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getBucket())
                .key("test/hello.txt")
                .contentType("text/plain; charset=UTF-8")
                .build();

        s3.putObject(putRequest, new File("hello.txt").toPath());
        Assertions.assertThat(new File("hello.txt").exists()).isTrue();
    }

    @Test
    public void downloadTest() {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(awsProperties.getBucket())
                .key("test/hello.txt")
                .build();

        s3.getObject(getRequest, ResponseTransformer.toFile(Paths.get("download_hello.txt")));
        Assertions.assertThat(new File("download_hello.txt").exists()).isTrue();
    }

    @Test
    public void presignedUrlTest() {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey())
                ))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsProperties.getBucket())
                    .key("test/hello.txt")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String url = presigner.presignGetObject(presignRequest).url().toString();
            System.out.println("Presigned URL: " + url);

            Assertions.assertThat(url).contains("https://");
        }
    }


}
