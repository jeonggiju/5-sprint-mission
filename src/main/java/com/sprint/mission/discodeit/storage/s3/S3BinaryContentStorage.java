package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.s3.AwsProperties;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;


    @Override
    public UUID put(UUID binaryContentId, byte[] bytes) {
        String key = "binary/" + binaryContentId;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(awsProperties.getBucket())
                .key(key)
                .contentType("application/octet-stream")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        return binaryContentId;

    }


    @Override
    public InputStream get(UUID binaryContentId) {
        String key = "binary/" + binaryContentId;

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperties.getBucket())
                .key(key)
                .build();

        byte[] byteArray = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        return new ByteArrayInputStream(byteArray);
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto metaData) {
        String key = "binary/" + metaData.id();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(awsProperties.getBucket())
                .key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        URI uri = s3Presigner.presignGetObject(getObjectPresignRequest).httpRequest().getUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(uri)
                .build();
    }
}
