import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import Layout from './components/layout/Layout';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ProductSearch from './pages/ProductSearch';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import OrderConfirmation from './pages/OrderConfirmation';
import Profile from './pages/Profile';
import MerchantDashboard from './pages/MerchantDashboard';
import MerchantProducts from './pages/MerchantProducts';
import MerchantOffers from './pages/MerchantOffers';
import MerchantInventory from './pages/MerchantInventory';
import OrderDetails from './pages/OrderDetails';
import Orders from './pages/Orders';
import LoadingSpinner from './components/common/LoadingSpinner';

// Protected Route wrapper
const ProtectedRoute = ({ children, merchantOnly = false }) => {
  const { isAuthenticated, loading, isMerchant } = useAuth();

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (merchantOnly && !isMerchant) {
    return <Navigate to="/" replace />;
  }

  return children;
};

// Public Route wrapper (redirect if already authenticated)
const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return children;
};

function App() {
  const { loading } = useAuth();

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        {/* Public routes */}
        <Route index element={<Home />} />
        <Route
          path="login"
          element={
            <PublicRoute>
              <Login />
            </PublicRoute>
          }
        />
        <Route
          path="register"
          element={
            <PublicRoute>
              <Register />
            </PublicRoute>
          }
        />

        {/* Public shopping routes (guests can browse) */}
        <Route path="search" element={<ProductSearch />} />
        <Route path="product/:id" element={<ProductDetail />} />
        <Route path="cart" element={<Cart />} />

        {/* Protected routes (require login) */}
        <Route
          path="checkout"
          element={
            <ProtectedRoute>
              <Checkout />
            </ProtectedRoute>
          }
        />
        <Route
          path="order-confirmation/:orderId"
          element={
            <ProtectedRoute>
              <OrderConfirmation />
            </ProtectedRoute>
          }
        />
        <Route
          path="order/:orderId"
          element={
            <ProtectedRoute>
              <OrderDetails />
            </ProtectedRoute>
          }
        />
        <Route
          path="profile"
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          }
        />
        <Route
          path="orders"
          element={
            <ProtectedRoute>
              <Orders />
            </ProtectedRoute>
          }
        />

        {/* Merchant only routes */}
        <Route
          path="merchant/dashboard"
          element={
            <ProtectedRoute merchantOnly>
              <MerchantDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="merchant/products"
          element={
            <ProtectedRoute merchantOnly>
              <MerchantProducts />
            </ProtectedRoute>
          }
        />
        <Route
          path="merchant/offers"
          element={
            <ProtectedRoute merchantOnly>
              <MerchantOffers />
            </ProtectedRoute>
          }
        />
        <Route
          path="merchant/inventory"
          element={
            <ProtectedRoute merchantOnly>
              <MerchantInventory />
            </ProtectedRoute>
          }
        />

        {/* Catch all */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default App;
