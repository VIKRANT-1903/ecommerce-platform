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

  // --- [NEW] Get All Products (Safe Listing) ---
  getAll: async () => {
    const response = await api.get(`${AUTH_SERVICE_URL}/products`);
    return response.data;
  },

  // --- [FIXED] Search products (Safe params) ---
  search: async (params) => {
    const queryParams = new URLSearchParams();
    
    // Only append if value exists (prevents sending "name=" with empty value)
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

  // Get offers for a single product (Old way, still useful for details page)
  getByProductId: async (productId) => {
    const response = await api.get(`${AUTH_SERVICE_URL}/offers/product/${productId}`);
    return response.data;
  },

  // --- [NEW] Bulk Fetch (The Performance Fix) ---
  getBulkOffers: async (productIds) => {
    // Sends [ "id1", "id2" ] -> Returns { "id1": [offers], "id2": [offers] }
    const response = await api.post(`${AUTH_SERVICE_URL}/offers/bulk`, productIds);
    return response.data;
  },
};