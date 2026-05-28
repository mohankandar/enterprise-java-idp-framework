package io.github.mohankandar.idp.demo.service;

import io.github.mohankandar.idp.demo.soap.hello.HelloEndpoint;
import io.github.mohankandar.idp.demo.soap.hello.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class HelloSoapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(HelloSoapRunner.class);

    private final HelloEndpoint port;

    public HelloSoapRunner(HelloEndpoint port) {
        this.port = port;
    }

    @Override
    public void run(String... args) {
        try {
            HelloRequest req = new HelloRequest();
            req.setName("Mohan");
            var res = port.sayHello(req);
            log.info("SOAP says: " + res.getMessage());
        } catch (Exception e) {
            log.warn("Hello SOAP call failed at startup (continuing). Reason={}", e.getMessage());
            log.warn("Hello SOAP call failed", e);
        }
    }
}