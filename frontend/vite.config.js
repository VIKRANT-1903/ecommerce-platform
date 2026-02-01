import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // ecomm service (port 8080) - Cart, Orders, Inventory, Checkout
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // ecomm1 service (port 8082) - Auth, Users, Merchants, Products, Offers
      '/auth': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/health': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/users': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/merchants': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/products': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/offers': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // search-service (port 8083)
      '/search': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      }
    }
  }
})
