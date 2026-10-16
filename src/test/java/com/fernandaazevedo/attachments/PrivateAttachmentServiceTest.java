package com.fernandaazevedo.attachments;

import com.fernandaazevedo.securityscan.MalwareScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrivateAttachmentServiceTest {
  @TempDir Path temp;

  @Test
  void cleanPdfIsStoredPrivatelyWithRandomNameAndHash() throws Exception {
    AttachmentRepository repository = mock(AttachmentRepository.class);
    MalwareScanner scanner = mock(MalwareScanner.class);
    when(scanner.scan(any())).thenReturn(new MalwareScanner.ScanResult(MalwareScanner.Status.CLEAN, "OK"));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    PrivateAttachmentService service = new PrivateAttachmentService(temp.toString(), repository, scanner);
    MockMultipartFile file = new MockMultipartFile("files", "exame.pdf", "application/pdf", "%PDF-1.7\nconteudo".getBytes());

    Attachment stored = service.store(44L, 8L, file);

    assertEquals("CLEAN", stored.scanStatus);
    assertEquals("PDF", stored.detectedType);
    assertNotEquals("exame.pdf", stored.storageName);
    assertTrue(stored.storageName.endsWith(".bin"));
    assertEquals(64, stored.sha256.length());
    assertTrue(service.resolveClean(stored).startsWith(temp));
  }

  @Test
  void mismatchedOrExecutableFilesAreRejectedBeforeScanner() throws Exception {
    AttachmentRepository repository = mock(AttachmentRepository.class);
    MalwareScanner scanner = mock(MalwareScanner.class);
    PrivateAttachmentService service = new PrivateAttachmentService(temp.toString(), repository, scanner);
    MockMultipartFile disguised = new MockMultipartFile("files", "imagem.png", "image/png", "MZ executable".getBytes());
    MockMultipartFile executable = new MockMultipartFile("files", "programa.exe", "application/octet-stream", "MZ".getBytes());

    assertThrows(PrivateAttachmentService.InvalidAttachmentException.class, () -> service.store(1L, 1L, disguised));
    assertThrows(PrivateAttachmentService.InvalidAttachmentException.class, () -> service.store(1L, 1L, executable));
    verifyNoInteractions(scanner, repository);
  }

  @Test
  void missingAntimalwareKeepsFileInQuarantineAndBlocksDownload() throws Exception {
    AttachmentRepository repository = mock(AttachmentRepository.class);
    MalwareScanner scanner = mock(MalwareScanner.class);
    when(scanner.scan(any())).thenReturn(new MalwareScanner.ScanResult(MalwareScanner.Status.NOT_CONFIGURED, "not configured"));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    PrivateAttachmentService service = new PrivateAttachmentService(temp.toString(), repository, scanner);
    MockMultipartFile file = new MockMultipartFile("files", "laudo.pdf", "application/pdf", "%PDF-1.7\nteste".getBytes());

    Attachment stored = service.store(1L, 1L, file);

    assertEquals("QUARANTINED", stored.scanStatus);
    assertThrows(PrivateAttachmentService.InvalidAttachmentException.class, () -> service.resolveClean(stored));
  }
}
