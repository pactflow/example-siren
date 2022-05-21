package de.juplo.demos.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.fail;


@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "Siren Order Provider")
public class ContractTest
{
  @Pact(consumer="SpringConsumer")
  public RequestResponsePact getOrders(PactDslWithProvider builder)
  {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("name")
        .booleanType("happy")
        .hexValue("hexCode")
        .id()
        .ipAddress("localAddress")
        .numberValue("age", 100);
    return builder
          .uponReceiving("get all orders")
            .path("/orders")
            .method("GET")
          .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/vnd.siren+json"))
            .body(body)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "getOrders")
  public void testGetExistingUserByEmail(MockServer mockServer)
  {
    RestTemplate restTemplate =
        new RestTemplateBuilder()
            .rootUri(mockServer.getUrl())
            .build();
    try
    {
      restTemplate.getForEntity("/orders", String.class);
    }
    catch (Exception e)
    {
      fail("Unexpected exception", e);
    }
  }
}
