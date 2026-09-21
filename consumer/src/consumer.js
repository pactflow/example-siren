const { Client } = require('ketting')

async function deleteFirstOrder(url) {
  const client = new Client(url)

  const [firstOrder] = await client.follow('orders').followAll('item')
  // Ketting drops the actions of embedded Siren sub-entities and caches the
  // stripped state, so refresh the order to get a state that carries them.
  const order = await firstOrder.refresh()
  await order.action('delete').submit()

  return true
}

module.exports = {
  deleteFirstOrder
}
