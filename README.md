# Example Pact + Siren project
Example project using [Siren](https://github.com/kevinswiber/siren) for hypermedia entities and testing with Pact.

This project has two sub-projects, a provider springboot project which is using `spring-hateoas-siren` to provide Siren
responses and a Javascript consumer project `ketting` to parse and navigate the Siren responses. 

## Provider Project

The provider project is a springboot application with Siren support provided by `spring-hateoas-siren`. It has two
resources, a root resource which provides links to the other resources and an order resource for dealing with orders
in the system.

### Root Resource

This just provides the links to the other resources.

`GET /`:

 ```json
{
  "class": [
    "representation"
  ],
  "links": [
    {
      "rel": [
        "orders"
      ],
      "href": "http://localhost:8080/orders"
    }
  ]
}
```

### Order Resource

This provides all the CRUD operations on Orders: fetch all orders, fetch an order by ID, update a resource or delete one.

`GET /orders`

```json
{
  "class": [
    "entity"
  ],
  "entities": [
    {
      "class": [
        "entity"
      ],
      "rel": [
        "item"
      ],
      "properties": {
        "id": 1234
      },
      "links": [
        {
          "rel": [
            "self"
          ],
          "href": "http://localhost:8080/orders/1234"
        }
      ],
      "actions": [
        {
          "name": "update",
          "method": "PUT",
          "href": "http://localhost:8080/orders/1234"
        },
        {
          "name": "delete",
          "method": "DELETE",
          "href": "http://localhost:8080/orders/1234"
        }
      ]
    }
  ],
  "links": [
    {
      "rel": [
        "self"
      ],
      "href": "http://localhost:8080/orders"
    }
  ]
}
```

## Consumer Project

This is a simple Javascript application that uses [Ketting](https://github.com/badgateway/ketting) which is a 
hypermedia client for javascript. It has a single function in `consumer/src/consumer.js` that navigates the links from the provider to find the
orders resource, get all the orders, find the first one and execute the delete action.

The consumer does the following:

1. Get the root resource
2. Find the orders relation
3. Execute a GET to the URL of the orders relation
4. Extract the first order entity from the embedded entities
5. Find the delete action for that order
6. Execute the action (which executes a DELETE to the URL of the action) 

## Pact Tests

The problem with using normal Pact tests to test this scenario is that Siren responses contain URLs to the resources and
actions. The URLs when running the consumer test will be different than those when verifying the provider. This will 
result in a verification failure.

To get round this problem, we use the `url` matcher function from the consumer Pact DSL. This function takes a base URL,
and a list of path fragments. The path fragments can be either plain strings or regular expressions. It then constructs
the actual URL to use in the consumer test, and a regular expression matcher that can match the URLs in the provider
verification test.

To show this working, the consumer Pact test has the mock server running on port 9000, while the provider will be running on port 8080.
