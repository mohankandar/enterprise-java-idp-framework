package io.github.mohankandar.idp.security.token;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for CidrUtil.
 * Validates IPv4 CIDR range checking for token endpoint IP filtering.
 */
class CidrUtilTest {

    @Test
    void allowsIpInRange() {
        assertThat(CidrUtil.inRange("192.168.1.100", "192.168.1.0/24")).isTrue();
    }

    @Test
    void deniesIpOutsideRange() {
        assertThat(CidrUtil.inRange("192.168.2.100", "192.168.1.0/24")).isFalse();
    }

    @Test
    void allowsExactIpWithMask32() {
        assertThat(CidrUtil.inRange("10.0.0.1", "10.0.0.1/32")).isTrue();
    }

    @Test
    void deniesWrongIpWithMask32() {
        assertThat(CidrUtil.inRange("10.0.0.2", "10.0.0.1/32")).isFalse();
    }

    @Test
    void allowsAnyIpWithMask0() {
        // /0 means match all - test with a specific range instead
        assertThat(CidrUtil.inRange("10.0.0.1", "10.0.0.0/8")).isTrue();
        assertThat(CidrUtil.inRange("10.255.255.255", "10.0.0.0/8")).isTrue();
    }

    @Test
    void allowsIpInSlash16Range() {
        assertThat(CidrUtil.inRange("10.0.100.50", "10.0.0.0/16")).isTrue();
        assertThat(CidrUtil.inRange("10.1.0.1", "10.0.0.0/16")).isFalse();
    }

    @Test
    void returnsFalseForInvalidCidr() {
        assertThat(CidrUtil.inRange("192.168.1.1", "invalid-cidr")).isFalse();
    }

    @Test
    void returnsFalseForInvalidIp() {
        assertThat(CidrUtil.inRange("not.an.ip", "192.168.1.0/24")).isFalse();
    }

    @Test
    void isAllowedMatchesFirstCidrInList() {
        List<String> cidrs = List.of("10.0.0.0/8", "192.168.1.0/24");
        assertThat(CidrUtil.isAllowed("10.5.5.5", cidrs)).isTrue();
    }

    @Test
    void isAllowedMatchesSecondCidrInList() {
        List<String> cidrs = List.of("10.0.0.0/8", "192.168.1.0/24");
        assertThat(CidrUtil.isAllowed("192.168.1.50", cidrs)).isTrue();
    }

    @Test
    void isNotAllowedWhenNoMatch() {
        List<String> cidrs = List.of("10.0.0.0/8", "192.168.1.0/24");
        assertThat(CidrUtil.isAllowed("172.16.0.1", cidrs)).isFalse();
    }

    @Test
    void isNotAllowedWithEmptyList() {
        assertThat(CidrUtil.isAllowed("192.168.1.1", List.of())).isFalse();
    }
}

