# Example Pact + Siren project
Example project using [Siren](https://github.com/kevinswiber/siren) for hypermedia entities and testing with Pact.

This project has two sub-projects, a provider springboot project which is using `spring-hateoas-siren` to provide Siren
responses and a Javascript consumer project using `ketting` to parse and navigate the Siren responses. 

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

To get round this problem, we use the `url` matcher function from the consumer Pact DSL. This function takes a list of 
path fragments. The path fragments can be either plain strings or regular expressions. It then constructs
the actual URL to use in the consumer test using the mock servers base URL, and a regular expression matcher that can 
match the URLs in the provider verification test.

### Dealing with hypermedia formats like Siren actions

Siren takes hypermedia links one step further by introducing resource actions. These encode the URL, HTTP method and
optionally any required parameters needed to make the requests for the actions supported by the resource.

The problem could then arise that the consumer make only use a few actions provided by the provider. We would want to
ensure that these actions are present in the list for the resource, and ignore the ones we are not using. The other issue
is that our tests should not be dependent on the order of the actions.

This is where the "array contains" matcher can help. It will allow us to match the resource actions for the ones we are
using, and ignore the others. It will also not depend on the order the actions are returned.

This is the actions for the order resource in the provider:

```json
{
  "actions": [
        {
          "name": "update",
          "method": "PUT",
          "href": "http://localhost:8080/orders/6774860028109588394"
        },
        {
          "name": "delete",
          "method": "DELETE",
          "href": "http://localhost:8080/orders/6774860028109588394"
        },
        {
          "name": "changeStatus",
          "method": "PUT",
          "href": "http://localhost:8080/orders/6774860028109588394/status"
        }
  ]
}
```

For example, in the consumer test we can specify:

```js
"actions": arrayContaining(
  {
    "name": "update",
    "method": "PUT",
    "href": url(["orders", regex("\\d+", "1234")])
  },
  {
    "name": "delete",
    "method": "DELETE",
    "href": url(["orders", regex("\\d+", "1234")])
  }
)
```

This will match the actions if they contain the update and delete actions. it will ignore the other actions.

You can see this in work if you remove one of the controller methods in the provider. For instance, if we commented out
the delete endpoint, and then run the pact verification in the provider, we get this error:

```console
$ ./gradlew pactverify

> Task :startServer

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.4.RELEASE)

2020-11-09 14:53:32.046  INFO 39485 --- [           main] i.p.e.s.SirenProviderApplication         : Starting SirenProviderApplication on ronald-P95xER with PID 39485 (/home/ronald/Development/Projects/Pact/example-siren/provider/build/libs/siren-provider-0.0.1.jar started by ronald in /home/ronald/Development/Projects/Pact/example-siren/provider)
2020-11-09 14:53:32.048  INFO 39485 --- [           main] i.p.e.s.SirenProviderApplication         : No active profile set, falling back to default profiles: default
2020-11-09 14:53:32.797  INFO 39485 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2020-11-09 14:53:32.808  INFO 39485 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2020-11-09 14:53:32.808  INFO 39485 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.38]
2020-11-09 14:53:32.870  INFO 39485 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2020-11-09 14:53:32.870  INFO 39485 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 759 ms
2020-11-09 14:53:33.071  INFO 39485 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2020-11-09 14:53:33.221  INFO 39485 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2020-11-09 14:53:33.229  INFO 39485 --- [           main] i.p.e.s.SirenProviderApplication         : Started SirenProviderApplication in 1.53 seconds (JVM running for 1.903)
java -jar /home/ronald/Development/Projects/Pact/example-siren/provider/build/libs/siren-provider-0.0.1.jar is ready.

> Task :pactVerify_Siren_Order_Provider FAILED

Verifying a pact between Siren Consumer and Siren Order Provider
  [Using File /home/ronald/Development/Projects/Pact/example-siren/consumer/pacts/Siren Order Provider-Siren Order Service.json]
  get root
    returns a response which
      has status code 200 (OK)
      has a matching body (OK)
  get all orders
    returns a response which
      has status code 200 (OK)
      has a matching body (FAILED)
  delete order
    returns a response which
      has status code 200 (FAILED)
      has a matching body (OK)

NOTE: Skipping publishing of verification results as it has been disabled (pact.verifier.publishResults is not 'true')


Failures:

1) Verifying a pact between Siren Consumer and Siren Order Provider - get all orders

    1.1) body: $.entities.0.actions Variant at index 1 ({"href":http://localhost:9000/orders/1234,"method":DELETE,"name":delete}) was not found in the actual list

        [
          {
        -    "href": "http://localhost:9000/orders/1234",
        +    "href": "http://localhost:8080/orders/7779028774458252624",
            "method": "PUT",
            "name": "update"
          },
          {
        -    "href": "http://localhost:9000/orders/1234",
        -    "method": "DELETE",
        -    "name": "delete"
        +    "href": "http://localhost:8080/orders/7779028774458252624/status",
        +    "method": "PUT",
        +    "name": "changeStatus"
          }
        ]


    1.2) status: expected status of 200 but was 405



FAILURE: Build failed with an exception.

* What went wrong:
There were 2 non-pending pact failures for provider Siren Order Provider

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/6.6.1/userguide/command_line_interface.html#sec:command_line_warnings

BUILD FAILED in 4s
8 actionable tasks: 6 executed, 2 up-to-date
```
