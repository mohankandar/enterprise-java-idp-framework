package io.github.mohankandar.idp.data.jpa;

import io.github.mohankandar.idp.data.jpa.audit.SecurityAuditorAware;
import io.github.mohankandar.idp.data.jpa.paging.PageMapper;
import io.github.mohankandar.idp.data.jpa.paging.PageableUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@AutoConfiguration
@ConditionalOnClass(Pageable.class) // only activates when spring-data is present
@EnableJpaAuditing(auditorAwareRef = "idpAuditorAware")
public class IdpJpaAutoConfiguration {

    @Bean
    public AuditorAware<String> idpAuditorAware() {
        return new SecurityAuditorAware();
    }

    // Utility beans are simple singletons; app can inject or use statically
    @Bean
    public PageableUtil pageableUtil() { return new PageableUtil(); }

    @Bean
    public PageMapper pageMapper() { return new PageMapper(); }
}
