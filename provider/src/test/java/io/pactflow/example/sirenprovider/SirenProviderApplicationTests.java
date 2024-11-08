package io.pactflow.example.sirenprovider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Provider("SirenOrderProvider")
@PactFolder("../spring-consumer/target/pacts")
class SirenProviderApplicationTests
{
  @LocalServerPort
  int port;


  @BeforeEach
  public void init(PactVerificationContext context)
  {
    context.setTarget(new HttpTestTarget("localhost", port));
  }

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider.class)
  public void pactVerificationTestTemplate(PactVerificationContext context)
  {
    context.verifyInteraction();
  }
}
