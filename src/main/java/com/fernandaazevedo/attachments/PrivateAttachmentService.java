package com.fernandaazevedo.attachments;

import com.fernandaazevedo.securityscan.MalwareScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PrivateAttachmentService {
  private static final Set<String> ALLOWED = Set.of("pdf", "png", "jpg", "jpeg", "webp", "txt", "doc", "docx", "wav", "mp3", "m4a");
  private static final Set<String> AUDIO = Set.of("wav", "mp3", "m4a");
  private final Path stagingRoot;
  private final AttachmentRepository repository;
  private final MalwareScanner malwareScanner;
  private final AttachmentStorage storage;

  public PrivateAttachmentService(@Value("${storage.staging.root:${java.io.tmpdir}/fernanda-azevedo-scan}") String root,
      AttachmentRepository repository, MalwareScanner malwareScanner, AttachmentStorage storage) throws IOException {
    this.stagingRoot = Paths.get(root).toAbsolutePath().normalize();
    this.repository = repository;
    this.malwareScanner = malwareScanner;
    this.storage = storage;
    Files.createDirectories(this.stagingRoot);
  }

  public Attachment store(Long consultationId, Long uploaderUserId, MultipartFile file) throws IOException {
    String original = sanitize(Optional.ofNullable(file.getOriginalFilename()).orElse("arquivo"));
    String extension = extension(original);
    if (!ALLOWED.contains(extension)) throw new InvalidAttachmentException("Tipo de arquivo não permitido.");
    long limit = AUDIO.contains(extension) ? 20_000_000L : 15_000_000L;
    if (file.isEmpty() || file.getSize() > limit) throw new InvalidAttachmentException("Arquivo vazio ou acima do limite permitido.");
    String declaredMime = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream").toLowerCase(Locale.ROOT);
    byte[] header;
    try (InputStream in = file.getInputStream()) { header = in.readNBytes(16); }
    String detected = detect(header, extension);
    if (detected == null || !mimeCompatible(extension, declaredMime)) throw new InvalidAttachmentException("O conteúdo do arquivo não corresponde ao tipo informado.");

    String id = UUID.randomUUID().toString();
    String storageName = id + ".bin";
    Path target = stagingRoot.resolve(storageName + ".upload").normalize();
    if (!target.startsWith(stagingRoot)) throw new InvalidAttachmentException("Nome de arquivo inválido.");
    try (InputStream in = file.getInputStream()) { Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING); }
    try {
      Attachment attachment = new Attachment();
      attachment.id = id;
      attachment.consultationId = consultationId;
      attachment.uploaderUserId = uploaderUserId;
      attachment.originalName = original;
      attachment.storageName = storageName;
      attachment.extension = extension;
      attachment.declaredMime = declaredMime;
      attachment.detectedType = detected;
      attachment.size = file.getSize();
      attachment.sha256 = sha256(target);
      MalwareScanner.ScanResult scan = malwareScanner.scan(target);
      attachment.scanStatus = switch (scan.status()) {
        case CLEAN -> "CLEAN";
        case INFECTED -> "BLOCKED";
        case SCAN_FAILED, NOT_CONFIGURED -> "QUARANTINED";
      };
      attachment.scanDetail = scan.detail();
      storage.put(storageName, target);
      return repository.save(attachment);
    } catch (RuntimeException | IOException error) {
      try { storage.delete(storageName); } catch (Exception ignored) {}
      throw error;
    } finally {
      Files.deleteIfExists(target);
    }
  }

  public InputStream openClean(Attachment attachment) throws IOException {
    if (!"CLEAN".equals(attachment.scanStatus)) throw new InvalidAttachmentException("Arquivo indisponível enquanto estiver em quarentena ou bloqueado.");
    return storage.open(attachment.storageName);
  }

  private String sanitize(String name) { String safe = name.replaceAll("[\\\\/\\r\\n\\t<>:\"|?*]", "_").strip(); return safe.isBlank() ? "arquivo" : safe.substring(0, Math.min(180, safe.length())); }
  private String extension(String name) { int dot = name.lastIndexOf('.'); return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT); }
  private boolean mimeCompatible(String ext, String mime) { return switch (ext) {
    case "pdf" -> mime.equals("application/pdf"); case "png" -> mime.equals("image/png"); case "jpg", "jpeg" -> mime.equals("image/jpeg"); case "webp" -> mime.equals("image/webp"); case "txt" -> mime.equals("text/plain"); case "doc" -> mime.equals("application/msword") || mime.equals("application/octet-stream"); case "docx" -> mime.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || mime.equals("application/zip"); case "wav" -> mime.equals("audio/wav") || mime.equals("audio/x-wav"); case "mp3" -> mime.equals("audio/mpeg"); case "m4a" -> mime.equals("audio/mp4") || mime.equals("audio/x-m4a"); default -> false; }; }
  private String detect(byte[] h, String ext) { if (starts(h, "%PDF-")) return ext.equals("pdf") ? "PDF" : null; if (starts(h, new byte[]{(byte)0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a})) return ext.equals("png") ? "PNG" : null; if (h.length>=3&&(h[0]&255)==255&&(h[1]&255)==216&&(h[2]&255)==255) return Set.of("jpg","jpeg").contains(ext)?"JPEG":null; if (h.length>=12&&ascii(h,0,4).equals("RIFF")&&ascii(h,8,4).equals("WEBP")) return ext.equals("webp")?"WEBP":null; if (h.length>=12&&ascii(h,0,4).equals("RIFF")&&ascii(h,8,4).equals("WAVE")) return ext.equals("wav")?"WAV":null; if (starts(h,"ID3")||(h.length>=2&&(h[0]&255)==255&&((h[1]&224)==224))) return ext.equals("mp3")?"MP3":null; if (h.length>=8&&ascii(h,4,4).equals("ftyp")) return ext.equals("m4a")?"M4A":null; if (starts(h,new byte[]{(byte)0xD0,(byte)0xCF,0x11,(byte)0xE0})) return ext.equals("doc")?"OLE_DOC":null; if (starts(h,new byte[]{0x50,0x4b,0x03,0x04})) return ext.equals("docx")?"ZIP_DOCX":null; if (ext.equals("txt")&&Arrays.stream(toUnsigned(h)).noneMatch(x->x==0)) return "TEXT"; return null; }
  private boolean starts(byte[] data, String value) { return starts(data, value.getBytes(java.nio.charset.StandardCharsets.US_ASCII)); }
  private boolean starts(byte[] data, byte[] prefix) { if(data.length<prefix.length)return false;for(int i=0;i<prefix.length;i++)if(data[i]!=prefix[i])return false;return true; }
  private String ascii(byte[] data,int offset,int length){return data.length<offset+length?"":new String(data,offset,length,java.nio.charset.StandardCharsets.US_ASCII);}
  private int[] toUnsigned(byte[] bytes){int[] result=new int[bytes.length];for(int i=0;i<bytes.length;i++)result[i]=bytes[i]&255;return result;}
  private String sha256(Path path) throws IOException { try { MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream in=Files.newInputStream(path)){byte[] buffer=new byte[8192];for(int read;(read=in.read(buffer))>=0;)if(read>0)digest.update(buffer,0,read);}return HexFormat.of().formatHex(digest.digest()); } catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);} }

  public static class InvalidAttachmentException extends RuntimeException { public InvalidAttachmentException(String message){super(message);} }
}
