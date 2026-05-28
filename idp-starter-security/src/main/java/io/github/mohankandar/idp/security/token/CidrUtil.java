package io.github.mohankandar.idp.security.token;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Lightweight IPv4 CIDR range checker used by /api/token endpoint.
 */
final class CidrUtil {

  static boolean isAllowed(String ip, List<String> cidrs) {
    for (String cidr : cidrs) {
      if (inRange(ip, cidr)) return true;
    }
    return false;
  }

  static boolean inRange(String ip, String cidr) {
    try {
      var parts = cidr.split("/");
      var addr = InetAddress.getByName(parts[0]).getAddress();
      var target = InetAddress.getByName(ip).getAddress();
      int prefix = Integer.parseInt(parts[1]);
      int mask = ~((1 << (32 - prefix)) - 1);
      int a = ((addr[0] & 0xFF) << 24) | ((addr[1] & 0xFF) << 16) |
          ((addr[2] & 0xFF) << 8) | (addr[3] & 0xFF);
      int t = ((target[0] & 0xFF) << 24) | ((target[1] & 0xFF) << 16) |
          ((target[2] & 0xFF) << 8) | (target[3] & 0xFF);
      return (a & mask) == (t & mask);
    } catch (UnknownHostException | RuntimeException e) {
      return false;
    }
  }

  private CidrUtil() {}
}
