package io.github.mohankandar.idp.partner.feign;

import io.github.mohankandar.idp.partner.auth.OAuthClientCredentialsTokenService;
import io.github.mohankandar.idp.partner.config.IdpPartnerProperties;
import io.github.mohankandar.idp.partner.registry.PartnerServiceRegistry;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class PartnerFeignRequestInterceptor implements RequestInterceptor {

    private final PartnerServiceRegistry registry;
    private final OAuthClientCredentialsTokenService tokenService;

    public PartnerFeignRequestInterceptor(PartnerServiceRegistry registry, OAuthClientCredentialsTokenService tokenService) {
        this.registry = registry;
        this.tokenService = tokenService;
    }

    private static String resolvePartnerName(RequestTemplate template) {
        try {
            Object md = template.getClass().getMethod("methodMetadata").invoke(template);
            if (md != null) {
                Object tt = md.getClass().getMethod("targetType").invoke(md);
                if (tt instanceof Class<?> iface) {
                    Class<?> annType = Class.forName("org.springframework.cloud.openfeign.FeignClient");
                    @SuppressWarnings("unchecked")
                    Annotation ann = iface.getAnnotation((Class<Annotation>) annType);
                    if (ann != null) {
                        Method nameMethod = annType.getMethod("name");
                        String name = (String) nameMethod.invoke(ann);
                        if (StringUtils.hasText(name)) {
                            return name;
                        }

                        Method valueMethod = annType.getMethod("value");
                        String value = (String) valueMethod.invoke(ann);
                        if (StringUtils.hasText(value)) {
                            return value;
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 SecurityException ignored) {
        }
        return null;
    }

    @Override
    public void apply(RequestTemplate template) {
        String partnerName = resolvePartnerName(template);

        if (!StringUtils.hasText(partnerName) || !registry.has(partnerName)) {
            return;
        }

        IdpPartnerProperties.RestProperties rest = registry.rest(partnerName);

        Map<String, String> headers = rest.getHeaders();
        headers.forEach((k, v) -> {
            if (StringUtils.hasText(k) && StringUtils.hasText(v) && !hasHeader(template, k)) {
                template.header(k, v);
            }
        });

        IdpPartnerProperties.RestAuthProperties auth = rest.getAuth();
        if (auth == null || auth.getMode() == null) {
            return;
        }

        switch (auth.getMode()) {
            case API_KEY -> {
                String header = auth.getApiKeyHeader();
                String value = auth.getApiKeyValue();
                if (StringUtils.hasText(header) && StringUtils.hasText(value) && !hasHeader(template, header)) {
                    template.header(header, value);
                }
            }
            case PROPAGATE_BEARER -> {
                if (!hasHeader(template, HttpHeaders.AUTHORIZATION)) {
                    String token = resolveBearerToken();
                    if (StringUtils.hasText(token)) {
                        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    }
                }
            }
            case OAUTH -> {
                if (!hasHeader(template, HttpHeaders.AUTHORIZATION)) {
                    String token = tokenService.getBearerToken(partnerName);
                    if (StringUtils.hasText(token)) {
                        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    }
                }
            }
            case NONE -> {
                // no-op
            }
        }
    }

    private static boolean hasHeader(RequestTemplate template, String header) {
        return template.headers().keySet().stream().anyMatch(h -> h.equalsIgnoreCase(header));
    }

    private static String resolveBearerToken() {
        try {
            var ctx = org.springframework.security.core.context.SecurityContextHolder.getContext();
            if (ctx == null || ctx.getAuthentication() == null) {
                return null;
            }
            Object credentials = ctx.getAuthentication().getCredentials();
            if (credentials instanceof String s) {
                return s;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
