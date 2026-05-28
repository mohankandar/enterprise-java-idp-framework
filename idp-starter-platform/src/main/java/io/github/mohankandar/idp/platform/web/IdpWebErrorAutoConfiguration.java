package io.github.mohankandar.idp.platform.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Auto-registers IDP's REST error handling (ControllerAdvice) so consumer apps
 * do not need to declare their own @RestControllerAdvice.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ DispatcherServlet.class })
public class IdpWebErrorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(GlobalExceptionHandler.class)
  public GlobalExceptionHandler idpGlobalExceptionHandler() {
    return new GlobalExceptionHandler();
  }
}
