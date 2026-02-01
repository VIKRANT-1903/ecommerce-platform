import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { cartService, checkoutService } from '../services/ecommService';
import { productService } from '../services/authService';
import { useAuth } from './AuthContext';
import toast from 'react-hot-toast';

const CartContext = createContext(null);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const { user, isAuthenticated } = useAuth();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);
  const [productDetails, setProductDetails] = useState({});

  const fetchCart = useCallback(async () => {
    if (!user?.id) return;
    
    setLoading(true);
    try {
      const response = await cartService.getCart(user.id);
      if (response.success) {
        setCart(response.data);
        // Fetch product details for each item
        if (response.data?.items?.length > 0) {
          const uniqueProductIds = [...new Set(response.data.items.map(item => item.productId))];
          const details = {};
          for (const productId of uniqueProductIds) {
            try {
              const productResponse = await productService.getById(productId);
              if (productResponse.success) {
                details[productId] = productResponse.data;
              }
            } catch (error) {
              console.error(`Failed to fetch product ${productId}:`, error);
            }
          }
          setProductDetails(prev => ({ ...prev, ...details }));
        }
      }
    } catch (error) {
      console.error('Failed to fetch cart:', error);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    if (isAuthenticated && user?.id) {
      fetchCart();
    } else {
      setCart(null);
      setProductDetails({});
    }
  }, [isAuthenticated, user?.id, fetchCart]);

  const addToCart = async (item) => {
    if (!user?.id) {
      toast.error('Please login to add items to cart');
      return { success: false };
    }

    try {
      const response = await cartService.addItem(user.id, item);
      if (response.success) {
        setCart(response.data);
        toast.success('Added to cart!');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to add item to cart';
      toast.error(message);
      return { success: false, message };
    }
  };

  const updateCartItem = async (cartItemId, quantity) => {
    if (!user?.id) return { success: false };

    try {
      const response = await cartService.updateItem(user.id, cartItemId, quantity);
      if (response.success) {
        setCart(response.data);
        toast.success('Cart updated!');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to update cart';
      toast.error(message);
      return { success: false, message };
    }
  };

  const removeFromCart = async (cartItemId) => {
    if (!user?.id) return { success: false };

    try {
      const response = await cartService.removeItem(user.id, cartItemId);
      if (response.success) {
        setCart(response.data);
        toast.success('Item removed from cart');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.message || 'Failed to remove item';
      toast.error(message);
      return { success: false, message };
    }
  };

  const checkout = async (shippingAddress) => {
    if (!user?.id) {
      toast.error('Please login to checkout');
      return { success: false };
    }

    try {
      const response = await checkoutService.checkout(user.id, shippingAddress);
      if (response.success && response.data?.success) {
        setCart(null);
        toast.success('Checkout completed successfully!');
        return { success: true, data: response.data };
      }
      const message = response.data?.message || response.message || 'Checkout failed';
      toast.error(message);
      return { success: false, message, data: response.data };
    } catch (error) {
      const message = error.response?.data?.message || error.response?.data?.data?.message || 'Checkout failed';
      toast.error(message);
      return { success: false, message };
    }
  };

  const clearCart = () => {
    setCart(null);
    setProductDetails({});
  };

  const cartItemCount = cart?.items?.reduce((total, item) => total + item.quantity, 0) || 0;
  
  const cartTotal = cart?.items?.reduce((total, item) => total + (item.priceSnapshot * item.quantity), 0) || 0;

  const value = {
    cart,
    loading,
    productDetails,
    cartItemCount,
    cartTotal,
    fetchCart,
    addToCart,
    updateCartItem,
    removeFromCart,
    checkout,
    clearCart,
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
