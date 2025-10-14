package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class S3BinaryContentStorageTest {


    @Autowired
    private BinaryContentStorage storage;

    @Test
    @DisplayName("S3에 byte 데이터 업로드 및 다운로드")
    void uploadAndDownload_realS3() throws Exception {

        UUID id = UUID.randomUUID();
        byte[] data = "Hello S3 World!".getBytes();

        UUID savedId = storage.put(id, data);
        Assertions.assertThat(savedId).isEqualTo(id);

        InputStream is = storage.get(id);
        byte[] downloaded = is.readAllBytes();
        Assertions.assertThat(downloaded).isEqualTo(data);

        BinaryContentDto dto = new BinaryContentDto(id, "test.txt", (long)data.length, "application/octet-stream");

        ResponseEntity<?> response = storage.download(dto);
        Assertions.assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
        Assertions.assertThat(response.getHeaders().getLocation().toString()).contains("https://");
    }
}
