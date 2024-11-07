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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@ExposesResourceFor(Order.class)
@RequestMapping(value = "/orders")
public class OrderController {

  @GetMapping
  public ResponseEntity<RepresentationModel<?>> orders() {
    Long id = Math.abs(new Random().nextLong());
    Order order = new Order(id);
    Link selfLink = actions(order);
    EntityModel<Order> model = EntityModel.of(order, selfLink);
    RepresentationModel<?> orders = CollectionModel.of(model);
    orders.add(linkTo(methodOn(OrderController.class).orders()).withSelfRel());
    return ResponseEntity.ok(orders);
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<EntityModel<Order>> order(@PathVariable(value = "id", required = true) Long id) {
    Order order = new Order(id);
    Link selfLink = actions(order);
    EntityModel<Order> model = EntityModel.of(order, selfLink);
    return ResponseEntity.ok(model);
  }

  @PutMapping("/{id}")
  public EntityModel<Order> update(@PathVariable(value = "id", required = true) Long id, Order order) {
    Link selfLink = actions(order);
    return EntityModel.of(order, selfLink);
  }

  private Link actions(Order order) {
    return linkTo(methodOn(OrderController.class).order(order.getId())).withSelfRel()
      .andAffordance(afford(methodOn(OrderController.class).update(order.getId(), null)))
      .andAffordance(afford(methodOn(OrderController.class).delete(order.getId())))
      .andAffordance(afford(methodOn(OrderController.class).changeStatus(order.getId(), null)));
  }

  @PutMapping("/{id}/status")
  public EntityModel<Order> changeStatus(@PathVariable(value = "id", required = true) Long id, String status) {
    Order order = new Order(id);
    Link selfLink = actions(order);
    return EntityModel.of(order, selfLink);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable(value = "id", required = true) Long id) {
    return ResponseEntity.ok().build();
  }
}
