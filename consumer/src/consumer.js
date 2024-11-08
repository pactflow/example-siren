const { Client } = require('ketting')

async function deleteFirstOrder(url) {
  const client = new Client(url)

  const ordersResource = await client.follow('orders')
  const ordersResp = await ordersResource.get()
  const firstOrder = ordersResp.getEmbedded()[0]
  let deleteAction = firstOrder.action('delete')
  deleteAction.client = client
  await deleteAction.submit()

  return true
}

module.exports = {
  deleteFirstOrder
}
