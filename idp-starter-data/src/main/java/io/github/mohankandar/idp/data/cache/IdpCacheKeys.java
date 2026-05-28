package io.github.mohankandar.idp.data.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Small helper to generate stable, readable cache keys.
 * <p>
 * ...
 */
public final class IdpCacheKeys {

  private static final int DEFAULT_MAX_LEN = 512;

  private IdpCacheKeys() {
    // util
  }

  public static String of(String namespace, Object... parts) {
    return of(namespace, DEFAULT_MAX_LEN, parts);
  }

  public static String of(String namespace, int maxLen, Object... parts) {
    String ns = (namespace == null || namespace.isBlank()) ? "cache" : namespace.trim();
    String base = ns + "|" + join(parts);
    if (maxLen <= 0) {
      maxLen = DEFAULT_MAX_LEN;
    }
    if (base.length() <= maxLen) {
      return base;
    }
    String hash = sha256Hex(base);
    // Keep the key readable but bounded, append 8 chars of hash to avoid collisions.
    int keep = Math.max(0, Math.min(maxLen - 10, maxLen));
    String head = base.substring(0, keep);
    return head + "|" + hash.substring(0, 8);
  }

  private static String join(Object... parts) {
    if (parts == null || parts.length == 0) {
      return "";
    }
    List<String> tokens = new ArrayList<>(parts.length);
    for (Object p : parts) {
      tokens.add(render(p));
    }
    return String.join("|", tokens);
  }

  private static String render(Object v) {
    if (v == null) {
      return "null";
    }
    if (v instanceof String s) {
      return s.isBlank() ? "" : s.trim();
    }
    if (v instanceof Pageable p) {
      return "page=" + p.getPageNumber() + ",size=" + p.getPageSize() + ",sort=" + render(p.getSort());
    }
    if (v instanceof Sort s) {
      if (s.isUnsorted()) {
        return "unsorted";
      }
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Sort.Order o : s) {
        if (!first) sb.append(';');
        first = false;
        sb.append(o.getProperty()).append(',').append(o.getDirection().name().toLowerCase());
      }
      return sb.toString();
    }
    return String.valueOf(v);
  }

  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        sb.append(Character.forDigit((b >> 4) & 0xF, 16));
        sb.append(Character.forDigit(b & 0xF, 16));
      }
      return sb.toString();
    } catch (Exception e) {
      // extremely unlikely; fallback to hashCode
      return Integer.toHexString(s.hashCode());
    }
  }
}
