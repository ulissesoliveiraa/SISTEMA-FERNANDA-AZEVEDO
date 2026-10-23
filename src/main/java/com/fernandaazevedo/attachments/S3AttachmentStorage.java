package com.fernandaazevedo.attachments;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3AttachmentStorage implements AttachmentStorage {
  private final S3Client client;
  private final String bucket;
  private final String prefix;

  public S3AttachmentStorage(
      @Value("${storage.s3.bucket:}") String bucket,
      @Value("${storage.s3.region:us-east-1}") String region,
      @Value("${storage.s3.endpoint:}") String endpoint,
      @Value("${storage.s3.access-key:}") String accessKey,
      @Value("${storage.s3.secret-key:}") String secretKey,
      @Value("${storage.s3.prefix:clinical-attachments/}") String prefix) {
    if (bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
      throw new IllegalStateException("Configure STORAGE_S3_BUCKET, STORAGE_S3_ACCESS_KEY e STORAGE_S3_SECRET_KEY.");
    }
    var builder = S3Client.builder()
      .region(Region.of(region))
      .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
      .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(!endpoint.isBlank()).build());
    if (!endpoint.isBlank()) builder.endpointOverride(URI.create(endpoint));
    this.client = builder.build();
    this.bucket = bucket;
    this.prefix = prefix.endsWith("/") ? prefix : prefix + "/";
  }

  @Override public void put(String key, Path source) throws IOException {
    try {
      client.putObject(PutObjectRequest.builder().bucket(bucket).key(objectKey(key)).contentType("application/octet-stream").build(), RequestBody.fromFile(source));
    } catch (S3Exception error) { throw new IOException("Falha ao gravar no armazenamento privado.", error); }
  }

  @Override public InputStream open(String key) throws IOException {
    try {
      return client.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey(key)).build());
    } catch (S3Exception error) { throw new IOException("Arquivo não encontrado no armazenamento privado.", error); }
  }

  @Override public void delete(String key) throws IOException {
    try { client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey(key)).build()); }
    catch (S3Exception error) { throw new IOException("Falha ao remover arquivo do armazenamento privado.", error); }
  }

  private String objectKey(String key) {
    if (!key.matches("[a-f0-9-]{36}\\.bin")) throw new IllegalArgumentException("Chave de armazenamento inválida.");
    return prefix + key;
  }

  @PreDestroy public void close() { client.close(); }
}
