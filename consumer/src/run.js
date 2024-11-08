const { deleteFirstOrder } = require('./consumer')

deleteFirstOrder("http://127.0.0.1:8080")
  .then(response => {
    console.log('Order deleted successfully:', response);
  })
  .catch(error => {
    console.error('Error deleting order:', error);
  });