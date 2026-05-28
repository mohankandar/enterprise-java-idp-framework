package io.github.mohankandar.idp.data.jpa.audit;

import org.springframework.data.domain.AuditorAware;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Optional;

public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Class<?> ctxHolderCls = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object ctx = ctxHolderCls.getMethod("getContext").invoke(null);
            if (ctx != null) {
                Object auth = ctx.getClass().getMethod("getAuthentication").invoke(ctx);
                if (auth != null) {
                    Object principal = auth.getClass().getMethod("getPrincipal").invoke(auth);
                    String user = extractUser(principal);
                    if (user != null && !user.isBlank()) {
                        return Optional.of(user);
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Spring Security not on classpath - fall through to default
        } catch (ReflectiveOperationException e) {
            // Reflection issue - fall through to default
        }
        return Optional.of("system");
    }

    private String extractUser(Object principal) {
        if (principal == null) {
            return "";
        }

        try {
            Object value = principal.getClass().getMethod("networkId").invoke(principal);
            if (value != null) {
                return String.valueOf(value);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Not a supported principal type for reflective networkId access
        }

        if (principal instanceof Principal p) {
            return p.getName();
        }

        return String.valueOf(principal);
    }
}