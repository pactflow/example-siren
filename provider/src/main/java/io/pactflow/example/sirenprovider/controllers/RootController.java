package io.pactflow.example.sirenprovider.controllers;

import io.pactflow.example.sirenprovider.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

  static class Root extends RepresentationModel<Root> {}

  @Autowired
  private EntityLinks entityLinks;

  @GetMapping("/")
  public ResponseEntity<RepresentationModel<?>> orders() {
    Link link = entityLinks.linkToCollectionResource(Order.class).withRel("orders");
    return ResponseEntity.ok().body(new Root().add(link));
  }
}
