package io.pactflow.example.sirenprovider.controllers;

import io.pactflow.example.sirenprovider.models.Order;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@ExposesResourceFor(Order.class)
public class OrderController {

  @GetMapping("/orders")
  public RepresentationModel<?> orders() {
    Order order = new Order(1234L);
    Link selfLink = linkTo(methodOn(OrderController.class).order(order.getId())).withSelfRel()
      .andAffordance(afford(methodOn(OrderController.class).update(order.getId(), null)))
      .andAffordance(afford(methodOn(OrderController.class).delete(order.getId())));
    EntityModel<Order> model = EntityModel.of(order, selfLink);
    RepresentationModel<?> orders = CollectionModel.of(model);
    orders.add(linkTo(methodOn(OrderController.class).orders()).withSelfRel());
    return orders;
  }

  @GetMapping("/orders/{id}")
  public EntityModel<Order> order(@PathVariable(value = "id", required = true) Long id) {
    Order order = new Order(id);
    Link selfLink = linkTo(methodOn(OrderController.class).order(order.getId())).withSelfRel()
      .andAffordance(afford(methodOn(OrderController.class).update(order.getId(), null)))
      .andAffordance(afford(methodOn(OrderController.class).delete(order.getId())));
    EntityModel<Order> model = EntityModel.of(order, selfLink);
    return model;
  }

  @PutMapping("/orders/{id}")
  public EntityModel<Order> update(@PathVariable(value = "id", required = true) Long id, Order order) {
    Link selfLink = linkTo(methodOn(OrderController.class).order(order.getId())).withSelfRel()
      .andAffordance(afford(methodOn(OrderController.class).update(order.getId(), null)))
      .andAffordance(afford(methodOn(OrderController.class).delete(order.getId())));
    EntityModel<Order> model = EntityModel.of(order, selfLink);
    return model;
  }

  @DeleteMapping("/orders/{id}")
  public ResponseEntity<Void> delete(@PathVariable(value = "id", required = true) Long id) {
    return ResponseEntity.ok().build();
  }
}
