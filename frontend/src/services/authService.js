import api from './api';

// ecomm1 service endpoints (Auth, Users, Merchants, Products, Offers)
const AUTH_SERVICE_URL = '';

export const authService = {
  // Register a new customer
  registerCustomer: async (data) => {
    const response = await api.post(`${AUTH_SERVICE_URL}/auth/register/customer`, data);
    return response.data;
  },

  // Register a new merchant
  registerMerchant: async (data) => {
    const response = await api.post(`${AUTH_SERVICE_URL}/auth/register/merchant`, data);
    return response.data;
  },

  // Login
  login: async (credentials) => {
    const response = await api.post(`${AUTH_SERVICE_URL}/auth/login`, credentials);
    return response.data;
  },

  // Get current user profile
  getProfile: async () => {
    const response = await api.get(`${AUTH_SERVICE_URL}/users/me`);
    return response.data;
  },

  // Update user profile
  updateProfile: async (data) => {
    const response = await api.put(`${AUTH_SERVICE_URL}/users/me`, data);
    return response.data;
  },

  // Health check
  healthCheck: async () => {
    const response = await api.get(`${AUTH_SERVICE_URL}/health`);
    return response.data;
  },
};

export const merchantService = {
  // Get merchant profile
  getProfile: async () => {
    const response = await api.get(`${AUTH_SERVICE_URL}/merchants/me`);
    return response.data;
  },

  // Update merchant profile
  updateProfile: async (data) => {
    const response = await api.put(`${AUTH_SERVICE_URL}/merchants/me`, data);
    return response.data;
  },

  // Update merchant status
  updateStatus: async (status) => {
    const response = await api.patch(`${AUTH_SERVICE_URL}/merchants/me/status`, { status });
    return response.data;
  },
};

export const productService = {
  // Create product (merchant only)
  create: async (data) => {
    const response = await api.post(`${AUTH_SERVICE_URL}/products`, data);
    return response.data;
  },

  // Get product by ID
  getById: async (id) => {
    const response = await api.get(`${AUTH_SERVICE_URL}/products/${id}`);
    return response.data;
  },

  // Search products
  search: async (params) => {
    const queryParams = new URLSearchParams();
    if (params.name) queryParams.append('name', params.name);
    if (params.category) queryParams.append('category', params.category);
    const response = await api.get(`${AUTH_SERVICE_URL}/products/search?${queryParams.toString()}`);
    return response.data;
  },
};

export const offerService = {
  // Create offer (merchant only)
  create: async (data) => {
    const response = await api.post(`${AUTH_SERVICE_URL}/offers`, data);
    return response.data;
  },

  // Delete offer (merchant only)
  delete: async (offerId) => {
    const response = await api.delete(`${AUTH_SERVICE_URL}/offers/${offerId}`);
    return response.data;
  },

  // Get my offers (merchant only)
  getMyOffers: async () => {
    const response = await api.get(`${AUTH_SERVICE_URL}/offers/my`);
    return response.data;
  },

  // Get offers for a product
  getByProductId: async (productId) => {
    const response = await api.get(`${AUTH_SERVICE_URL}/offers/product/${productId}`);
    return response.data;
  },
};
