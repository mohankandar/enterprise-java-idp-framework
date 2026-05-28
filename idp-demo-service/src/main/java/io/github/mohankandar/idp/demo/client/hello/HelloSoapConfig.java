package io.github.mohankandar.idp.demo.client.hello;

import io.github.mohankandar.idp.soap.client.SoapClientFactory;
import io.github.mohankandar.idp.demo.soap.hello.HelloEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloSoapConfig {

  @Bean
  public HelloEndpoint helloPort(SoapClientFactory factory) {
    return factory.createClient("hello", HelloEndpoint.class);
  }
}