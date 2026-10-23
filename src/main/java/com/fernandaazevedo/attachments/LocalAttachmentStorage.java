package com.fernandaazevedo.attachments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalAttachmentStorage implements AttachmentStorage {
  private final Path root;

  public LocalAttachmentStorage(@Value("${storage.private.root:./private-storage}") String root) throws IOException {
    this.root = Paths.get(root).toAbsolutePath().normalize();
    Files.createDirectories(this.root);
  }

  @Override public void put(String key, Path source) throws IOException {
    Files.copy(source, resolve(key), StandardCopyOption.REPLACE_EXISTING);
  }

  @Override public InputStream open(String key) throws IOException {
    Path path = resolve(key);
    if (!Files.isRegularFile(path)) throw new NoSuchFileException(key);
    return Files.newInputStream(path, StandardOpenOption.READ);
  }

  @Override public void delete(String key) throws IOException { Files.deleteIfExists(resolve(key)); }

  private Path resolve(String key) {
    Path path = root.resolve(key).normalize();
    if (!path.startsWith(root)) throw new IllegalArgumentException("Chave de armazenamento inválida.");
    return path;
  }
}
