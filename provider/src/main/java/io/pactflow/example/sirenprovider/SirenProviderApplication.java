package io.pactflow.example.sirenprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication
@EnableHypermediaSupport(type = {})
public class SirenProviderApplication {

  public static void main(String[] args) {
    SpringApplication.run(SirenProviderApplication.class, args);
  }

}
