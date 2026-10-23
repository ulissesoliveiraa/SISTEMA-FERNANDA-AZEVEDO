package com.fernandaazevedo.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface AttachmentStorage {
  void put(String key, Path source) throws IOException;
  InputStream open(String key) throws IOException;
  void delete(String key) throws IOException;
}
