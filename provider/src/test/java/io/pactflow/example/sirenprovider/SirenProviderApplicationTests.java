package io.pactflow.example.sirenprovider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;


@SpringBootTest(webEnvironment = DEFINED_PORT)
@Provider("SirenOrderProvider")
@PactFolder("../spring-consumer/target/pacts")
class SirenProviderApplicationTests
{
  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider.class)
  public void pactVerificationTestTemplate(PactVerificationContext context)
  {
    context.verifyInteraction();
  }
}
