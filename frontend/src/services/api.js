import axios from 'axios';

// Base URL configuration
// In development with Vite proxy, we use relative URLs
// The proxy in vite.config.js forwards requests to the backend
const API_BASE_URL = '';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Only redirect to login for protected routes, not for browsing
      const currentPath = window.location.pathname;
      const publicPaths = ['/', '/login', '/register', '/search', '/cart'];
      const isPublicPath = publicPaths.some(path => currentPath.startsWith(path)) ||
                           currentPath.startsWith('/product/');
      
      if (!isPublicPath) {
        // Clear token and redirect to login if unauthorized on protected route
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
