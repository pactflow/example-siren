const path = require("path")
const { PactV3, MatchersV3 } = require("@pact-foundation/pact")
const { deleteFirstOrder } = require('../consumer')

const {
  eachLike,
  url,
  integer,
  regex,
  arrayContaining
} = MatchersV3

describe("Siren Pact test", () => {
  let provider

  beforeEach(() => {
    provider = new PactV3({
      consumer: "SirenConsumer",
      provider: "SirenOrderService",
      dir: path.resolve(process.cwd(), "pacts")
    })
  })

  it('deletes the first order using the delete action', () => {
    provider

      // Get Root Request
      .uponReceiving("get root")
      .withRequest({
        method: "GET",
        path: "/"
      })
      .willRespondWith({
        status: 200,
        headers: {
          'Content-Type': 'application/vnd.siren+json'
        },
        body: {
          class: [ "representation"],
          links: [{"rel":["orders"], "href":  url(["orders"]) }]
        }
      })

      // Get Orders Request
      .uponReceiving("get all orders")
      .withRequest({
        method: "GET",
        path: "/orders",
      })
      .willRespondWith({
        status: 200,
        headers: {
          'Content-Type': 'application/vnd.siren+json'
        },
        body: {
          class: [ "entity" ],
          entities: eachLike({
            class: [ "entity" ],
            rel: [ "item" ],
            properties: {
              "id": integer(1234)
            },
            links: [
              {
                "rel": [ "self" ],
                "href": url(["orders", regex("\\d+", "1234")])
              }
            ],
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
          }),
          links: [
            {
              rel: [ "self" ],
              href: url(["orders"])
            }
          ]
        }
      })

      // Delete Order Request
      .uponReceiving("delete order")
      .withRequest({
        method: "DELETE",
        path: regex("/orders/\\d+", "/orders/1234"),
      })
      .willRespondWith({
        status: 200
      })

    return provider.executeTest(mockserver => {
      return expect(deleteFirstOrder(mockserver.url)).resolves.toBe(true)
    })
  })
})
