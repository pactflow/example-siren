const path = require("path")
const chai = require("chai")
const chaiAsPromised = require("chai-as-promised")
const expect = chai.expect
const { PactV3, MatchersV3 } = require("@pact-foundation/pact/v3")
const { deleteFirstOrder } = require('../consumer')

chai.use(chaiAsPromised)

const {
  eachLike,
  url,
  integer,
  regex
} = MatchersV3

describe("Siren Pact test", () => {
  let provider

  beforeEach(() => {
    provider = new PactV3({
      consumer: "Siren Order Provider",
      provider: "Siren Order Service",
      port: 9000,
      dir: path.resolve(process.cwd(), "pacts")
    })
  })

  it('deletes the first order using the delete action', () => {
    provider
      .uponReceiving("get all orders")
      .withRequest({
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
                "href": url("http://localhost:9000", ["orders", regex("\\d+", "1234")])
              }
            ],
            "actions": [
              {
                "name": "update",
                "method": "PUT",
                "href": url("http://localhost:9000", ["orders", regex("\\d+", "1234")])
              },
              {
                "name": "delete",
                "method": "DELETE",
                "href": url("http://localhost:9000", ["orders", regex("\\d+", "1234")])
              }
            ]
          }, 2),
          links: [
            {
              rel: [ "self" ],
              href: url("http://localhost:9000", ["orders"])
            }
          ]
        }
      })
      .uponReceiving("delete order")
      .withRequest({
        method: "DELETE",
        path: regex("/orders/\\d+", "/orders/1234"),
      })
      .willRespondWith({
        status: 200
      })

    return provider.executeTest(mockserver => {
      return expect(deleteFirstOrder(mockserver.url)).to.eventually.be.true
    })
  })
})
