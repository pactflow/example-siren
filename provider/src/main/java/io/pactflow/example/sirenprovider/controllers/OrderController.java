package io.pactflow.example.sirenprovider.controllers;

import io.pactflow.example.sirenprovider.models.Order;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@ExposesResourceFor(Order.class)
public class OrderController {

  @GetMapping("/orders")
  public RepresentationModel<?> orders() {
    Order order = new Order(1234L);
    EntityModel<Order> model = EntityModel.of(order);
    model.add(linkTo(methodOn(OrderController.class).order(1234L)).withSelfRel());
    RepresentationModel<?> orders = CollectionModel.of(model);
    return orders;
  }

  @GetMapping("/orders/{id}")
  public EntityModel<Order> order(@PathVariable(value = "id", required = true) Long id) {
    Order order = new Order(id);
    EntityModel<Order> model = EntityModel.of(order);
    model.add(linkTo(methodOn(OrderController.class).order(id)).withSelfRel());
    return model;
  }

}
