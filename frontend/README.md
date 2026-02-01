# ShopZone - E-commerce Frontend

A full-featured Amazon-like e-commerce frontend built with React, Vite, and Tailwind CSS.

## Features

### Customer Features
- **User Authentication**: Register, login, logout
- **Product Browsing**: Search products by name or category
- **Product Details**: View detailed product information with offers from multiple sellers
- **Shopping Cart**: Add/remove items, update quantities
- **Checkout**: Complete purchases with address input
- **Order Tracking**: View order confirmation and status

### Merchant Features
- **Merchant Dashboard**: Overview of store statistics
- **Product Management**: Create new products
- **Offer Management**: Create and manage product offers
- **Store Settings**: Update store name, toggle active status

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool
- **React Router v6** - Routing
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **Lucide React** - Icons
- **React Hot Toast** - Notifications

## Prerequisites

- Node.js 18+
- npm or yarn
- Backend services running (ecomm on port 8080, ecomm1 on port 8080)

## Getting Started

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The app will be available at **http://localhost:3000**

### 3. Build for Production

```bash
npm run build
```

### 4. Preview Production Build

```bash
npm run preview
```

## Project Structure

```
frontend/
├── public/
├── src/
│   ├── components/
│   │   ├── common/          # Reusable components
│   │   │   ├── Alert.jsx
│   │   │   ├── EmptyState.jsx
│   │   │   ├── LoadingSpinner.jsx
│   │   │   └── ProductCard.jsx
│   │   └── layout/          # Layout components
│   │       ├── Footer.jsx
│   │       ├── Header.jsx
│   │       └── Layout.jsx
│   ├── context/             # React Context providers
│   │   ├── AuthContext.jsx
│   │   └── CartContext.jsx
│   ├── pages/               # Page components
│   │   ├── Cart.jsx
│   │   ├── Checkout.jsx
│   │   ├── Home.jsx
│   │   ├── Login.jsx
│   │   ├── MerchantDashboard.jsx
│   │   ├── MerchantOffers.jsx
│   │   ├── MerchantProducts.jsx
│   │   ├── OrderConfirmation.jsx
│   │   ├── OrderDetails.jsx
│   │   ├── ProductDetail.jsx
│   │   ├── ProductSearch.jsx
│   │   ├── Profile.jsx
│   │   └── Register.jsx
│   ├── services/            # API service layer
│   │   ├── api.js           # Axios instance
│   │   ├── authService.js   # Auth/User/Product/Offer APIs
│   │   └── ecommService.js  # Cart/Order/Checkout APIs
│   ├── App.jsx
│   ├── index.css
│   └── main.jsx
├── index.html
├── package.json
├── postcss.config.js
├── tailwind.config.js
└── vite.config.js
```

## API Integration

### Service Architecture

The frontend integrates with two backend services:

1. **ecomm1** (Auth Service) - Port 8080
   - Authentication (login, register)
   - User management
   - Merchant management
   - Product catalog (MongoDB)
   - Offers management

2. **ecomm** (Commerce Service) - Port 8080
   - Shopping cart
   - Order management
   - Inventory management
   - Checkout orchestration

### Proxy Configuration

In development, Vite proxies API requests to the backend:

```javascript
// vite.config.js
proxy: {
  '/api': { target: 'http://localhost:8080' },
  '/auth': { target: 'http://localhost:8080' },
  '/users': { target: 'http://localhost:8080' },
  // ... etc
}
```

## User Roles

### Customer
- Browse and search products
- View product details and offers
- Add items to cart
- Complete checkout
- View order history

### Merchant
- All customer capabilities (except cart/checkout)
- Access merchant dashboard
- Create products
- Create and manage offers
- Update store settings

## Environment Configuration

The app uses relative URLs in development (proxied by Vite). For production, update the API base URLs in the service files.

## Styling

The app uses Tailwind CSS with a custom theme matching Amazon's color palette:

- **amazon-orange**: #FF9900 (Primary)
- **amazon-dark**: #131921 (Header)
- **amazon-light-dark**: #232F3E (Secondary header)
- **amazon-blue**: #146EB4 (Links)

## License

Internal use only.
