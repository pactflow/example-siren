package io.pactflow.example.sirenconsumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.*;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import java.util.Map;

import static org.assertj.core.api.Assertions.fail;


@ExtendWith(PactConsumerTestExt.class)
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@PactTestFor(providerName = "SirenOrderProvider")
public class ContractTest
{
  @Pact(consumer="SpringConsumer")
  public RequestResponsePact deletesTheFirstOrderUsingtheDeleteAction(PactDslWithProvider builder)
  {
    return builder
        .uponReceiving("get root")
        .path("/")
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/vnd.siren+json"))
        .body(LambdaDsl.newJsonBody(body ->
        {
          body.array("class", classArray ->
          {
            classArray.stringValue("representation");
          });
          body.array("links", linksArray ->
          {
            linksArray.object(object->
            {
              object.matchUrl2("href", "orders");
              object.array("rel", relArray ->
              {
                relArray.stringValue("orders");
              });
            });
          });
        }).build())
        .uponReceiving("get all orders")
        .path("/orders")
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/vnd.siren+json"))
        .body(LambdaDsl.newJsonBody(body ->
        {
          body.array("class", classArray ->
          {
            classArray.stringValue("entity");
          });
          body.eachLike("entities", entities ->
          {
            entities.arrayContaining("actions", actionsArray->
            {
              actionsArray.object(object ->
              {
                object.stringValue("name","update");
                object.stringValue("method", "PUT");
                object.matchUrl2("href", "orders", Matchers.regexp("\\d+", "1234").getMatcher());
              });
              actionsArray.object(object ->
              {
                object.stringValue("name","delete");
                object.stringValue("method", "DELETE");
                object.matchUrl2("href", "orders", Matchers.regexp("\\d+", "1234").getMatcher());
              });
            });
            entities.array("class", classArray ->
            {
              classArray.stringValue("entity");
            });
            entities.array("links", linksArray ->
            {
              linksArray.object(object->
              {
                object.matchUrl2("href", "orders", Matchers.regexp("\\d+", "1234").getMatcher());
                object.array("rel", relArray ->
                {
                  relArray.stringValue("self");
                });
              });
            });
            entities.object("properties", object->
            {
              object.integerType("id", 1234);
            });
            entities.array("rel", relArray ->
            {
              relArray.stringValue("item");
            });
          });
          body.array("links", linksArray ->
          {
            linksArray.object(object->
            {
              object.matchUrl2("href", "orders");
              object.array("rel", relArray ->
              {
                relArray.stringValue("self");
              });
            });
          });
        }).build())
        .uponReceiving("delete order")
        .matchPath("/orders/\\d+", "/orders/1234")
        .method("DELETE")
        .willRespondWith()
        .status(200)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "deletesTheFirstOrderUsingtheDeleteAction", pactVersion = PactSpecVersion.V3)
  public void testDeletesTheFirstOrderUsingtheDeleteAction(MockServer mockServer)
  {
    try
    {
      Application.deleteFirstOrder(mockServer.getUrl());
    }
    catch (Exception e)
    {
      fail("Unexpected exception", e);
    }
  }
}
