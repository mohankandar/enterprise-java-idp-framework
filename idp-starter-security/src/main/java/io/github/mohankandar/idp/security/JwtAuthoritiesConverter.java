package io.github.mohankandar.idp.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/** SPI: convert a Jwt to a collection of GrantedAuthority. */
public interface JwtAuthoritiesConverter extends Converter<Jwt, Collection<GrantedAuthority>> { }
