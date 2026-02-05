import api from './api';

// ecomm service endpoints (Cart, Orders, Inventory, Checkout)
const ECOMM_SERVICE_URL = '/api';

export const cartService = {
  // Get or create cart
  getCart: async (userId) => {
    const response = await api.get(`${ECOMM_SERVICE_URL}/users/${userId}/cart`);
    return response.data;
  },

  // View cart (404 if none exists)
  viewCart: async (userId) => {
    const response = await api.get(`${ECOMM_SERVICE_URL}/users/${userId}/cart/view`);
    return response.data;
  },

  // Add item to cart
  addItem: async (userId, item) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/users/${userId}/cart/items`, item);
    return response.data;
  },

  // Update item quantity
  updateItem: async (userId, cartItemId, quantity) => {
    const response = await api.patch(`${ECOMM_SERVICE_URL}/users/${userId}/cart/items/${cartItemId}`, { quantity });
    return response.data;
  },

  // Remove item from cart
  removeItem: async (userId, cartItemId) => {
    const response = await api.delete(`${ECOMM_SERVICE_URL}/users/${userId}/cart/items/${cartItemId}`);
    return response.data;
  },
};

export const orderService = {
  // Create order from cart
  createOrder: async (userId, shippingAddress) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/users/${userId}/orders`, { shippingAddress });
    return response.data;
  },

  // Get order by ID
  getOrderById: async (orderId) => {
    const response = await api.get(`${ECOMM_SERVICE_URL}/orders/${orderId}`);
    return response.data;
  },
  
  // List orders for a user
  getOrdersByUser: async (userId) => {
    const response = await api.get(`${ECOMM_SERVICE_URL}/users/${userId}/orders`);
    return response.data;
  },
};

export const inventoryService = {
  // Get inventory
  getInventory: async (productId, merchantId) => {
    const response = await api.get(`${ECOMM_SERVICE_URL}/inventory`, {
      params: { productId, merchantId },
    });
    return response.data;
  },

  // Get all inventory for a merchant
  getByMerchant: async (merchantId) => {
    const response = await api.get(`${ECOMM_SERVICE_URL}/inventory/merchant`, {
      params: { merchantId },
    });
    return response.data;
  },

  // Create inventory
  create: async (data) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/inventory`, data);
    return response.data;
  },

  // Update inventory
  update: async (data) => {
    const response = await api.put(`${ECOMM_SERVICE_URL}/inventory`, data);
    return response.data;
  },

  // Reserve inventory
  reserve: async (data) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/inventory/reserve`, data);
    return response.data;
  },

  // Confirm inventory
  confirm: async (data) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/inventory/confirm`, data);
    return response.data;
  },

  // Release inventory
  release: async (data) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/inventory/release`, data);
    return response.data;
  },
};

export const checkoutService = {
  // Perform checkout
  checkout: async (userId, shippingAddress) => {
    const response = await api.post(`${ECOMM_SERVICE_URL}/users/${userId}/checkout`, { shippingAddress });
    return response.data;
  },
};
