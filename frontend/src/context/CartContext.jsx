import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { cartService, checkoutService } from '../services/ecommService';
import { productService, offerService } from '../services/authService';
import { useAuth } from './AuthContext';
import toast from 'react-hot-toast';

const CartContext = createContext(null);

const GUEST_CART_KEY = 'shopzone_guest_cart';

// Helper functions for localStorage guest cart
const getGuestCart = () => {
  try {
    const cart = localStorage.getItem(GUEST_CART_KEY);
    return cart ? JSON.parse(cart) : { items: [] };
  } catch {
    return { items: [] };
  }
};

const saveGuestCart = (cart) => {
  try {
    localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cart));
  } catch (error) {
    console.error('Failed to save guest cart:', error);
  }
};

const clearGuestCartStorage = () => {
  try {
    localStorage.removeItem(GUEST_CART_KEY);
  } catch (error) {
    console.error('Failed to clear guest cart:', error);
  }
};

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
  const [offerPrices, setOfferPrices] = useState({}); // Store real prices from API
  const [hasSyncedGuestCart, setHasSyncedGuestCart] = useState(false);

  // Fetch offer prices for cart items (source of truth for prices)
  const fetchOfferPrices = useCallback(async (items) => {
    if (!items?.length) return;
    
    const prices = {};
    for (const item of items) {
      const key = `${item.productId}_${item.merchantId}`;
      if (offerPrices[key]) continue; // Already have price
      
      try {
        const response = await offerService.getByProductId(item.productId);
        if (response.success && response.data?.length > 0) {
          // Find the offer for this specific merchant
          const offer = response.data.find(o => o.merchantId === item.merchantId) || response.data[0];

          // Normalize price to a numeric value (offer.price may be object)
          const raw = offer?.price;
          let numeric = 0;
          if (typeof raw === 'number') numeric = raw;
          else if (raw && typeof raw === 'object') {
            numeric = raw.amount ?? raw.value ?? raw.price ?? raw.cents ?? 0;
            if (raw.cents && !raw.amount && !raw.value) numeric = raw.cents / 100;
          } else if (typeof offer?.priceCents === 'number') {
            numeric = offer.priceCents / 100;
          } else if (typeof offer?.amount === 'number') {
            numeric = offer.amount;
          }
          prices[key] = isFinite(numeric) ? numeric : 0;
        }
      } catch (error) {
        console.error(`Failed to fetch price for ${item.productId}:`, error);
      }
    }
    setOfferPrices(prev => ({ ...prev, ...prices }));
  }, [offerPrices]);

  // Fetch product details for cart items
  const fetchProductDetails = useCallback(async (items) => {
    if (!items?.length) return;
    
    const uniqueProductIds = [...new Set(items.map(item => item.productId))];
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
    
    // Also fetch prices
    await fetchOfferPrices(items);
  }, [fetchOfferPrices]);

  // Fetch server cart for authenticated users
  const fetchCart = useCallback(async () => {
    if (!user?.id) return;
    
    setLoading(true);
    try {
      const response = await cartService.getCart(user.id);
      if (response.success) {
        setCart(response.data);
        await fetchProductDetails(response.data?.items);
      }
    } catch (error) {
      console.error('Failed to fetch cart:', error);
    } finally {
      setLoading(false);
    }
  }, [user?.id, fetchProductDetails]);

  // Sync guest cart to server cart when user logs in
  const syncGuestCartToServer = useCallback(async () => {
    if (!user?.id) return;
    
    const guestCart = getGuestCart();
    if (guestCart.items.length === 0) {
      setHasSyncedGuestCart(true);
      return;
    }

    setLoading(true);
    try {
      // Add each guest cart item to server cart
      // Backend will fetch the real price from offer service
      for (const item of guestCart.items) {
        try {
          await cartService.addItem(user.id, {
            productId: item.productId,
            merchantId: item.merchantId,
            quantity: item.quantity,
            // Don't send priceSnapshot - backend will fetch real price
          });
        } catch (error) {
          console.error('Failed to sync item:', item, error);
        }
      }
      
      // Clear guest cart after sync
      clearGuestCartStorage();
      toast.success('Your cart items have been saved!');
      
      // Fetch updated server cart
      await fetchCart();
      setHasSyncedGuestCart(true);
    } catch (error) {
      console.error('Failed to sync guest cart:', error);
      setHasSyncedGuestCart(true);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  // Initialize cart based on auth state
  useEffect(() => {
    if (isAuthenticated && user?.id) {
      // User is logged in - first sync guest cart, then fetch server cart
      if (!hasSyncedGuestCart) {
        syncGuestCartToServer();
      } else {
        // Already synced, just fetch the server cart
        fetchCart();
      }
    } else {
      // Guest user - load from localStorage
      const guestCart = getGuestCart();
      setCart(guestCart);
      fetchProductDetails(guestCart.items);
      setHasSyncedGuestCart(false);
    }
  }, [isAuthenticated, user?.id, hasSyncedGuestCart]);

  // Add item to cart
  const addToCart = async (item) => {
    if (isAuthenticated && user?.id) {
      // Authenticated user - use server cart
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
    } else {
      // Guest user - use localStorage cart
      const guestCart = getGuestCart();
      
      // Check if item already exists
      const existingIndex = guestCart.items.findIndex(
        i => i.productId === item.productId && i.merchantId === item.merchantId
      );
      
      if (existingIndex >= 0) {
        // Update quantity
        guestCart.items[existingIndex].quantity += item.quantity;
      } else {
        // Add new item with a temporary ID - NO price stored (fetched from API)
        guestCart.items.push({
          productId: item.productId,
          merchantId: item.merchantId,
          quantity: item.quantity,
          cartItemId: `guest_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        });
      }
      
      saveGuestCart(guestCart);
      setCart(guestCart);
      await fetchProductDetails(guestCart.items);
      toast.success('Added to cart!');
      return { success: true };
    }
  };

  // Update cart item quantity
  const updateCartItem = async (cartItemId, quantity) => {
    if (isAuthenticated && user?.id) {
      // Authenticated user - use server cart
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
    } else {
      // Guest user - update localStorage cart
      const guestCart = getGuestCart();
      const itemIndex = guestCart.items.findIndex(i => i.cartItemId === cartItemId);
      
      if (itemIndex >= 0) {
        if (quantity <= 0) {
          guestCart.items.splice(itemIndex, 1);
        } else {
          guestCart.items[itemIndex].quantity = quantity;
        }
        saveGuestCart(guestCart);
        setCart(guestCart);
        toast.success('Cart updated!');
        return { success: true };
      }
      return { success: false, message: 'Item not found' };
    }
  };

  // Remove item from cart
  const removeFromCart = async (cartItemId) => {
    if (isAuthenticated && user?.id) {
      // Authenticated user - use server cart
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
    } else {
      // Guest user - remove from localStorage cart
      const guestCart = getGuestCart();
      guestCart.items = guestCart.items.filter(i => i.cartItemId !== cartItemId);
      saveGuestCart(guestCart);
      setCart(guestCart);
      toast.success('Item removed from cart');
      return { success: true };
    }
  };

  // Checkout (requires authentication)
  const checkout = async (shippingAddress) => {
    if (!isAuthenticated || !user?.id) {
      toast.error('Please login to checkout');
      return { success: false, requiresAuth: true };
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

  // Clear cart
  const clearCart = () => {
    if (isAuthenticated) {
      setCart(null);
    } else {
      clearGuestCartStorage();
      setCart({ items: [] });
    }
    setProductDetails({});
    setOfferPrices({});
  };

  // Helper to get price for a cart item (from API or server cart)
  const getItemPrice = useCallback((item) => {
    // For authenticated users, server cart has the real price
    if (isAuthenticated && item.priceSnapshot) {
      return item.priceSnapshot;
    }
    // For guests, use the fetched offer price
    const key = `${item.productId}_${item.merchantId}`;
    return offerPrices[key] || 0;
  }, [isAuthenticated, offerPrices]);

  const cartItemCount = cart?.items?.reduce((total, item) => total + item.quantity, 0) || 0;
  
  // Calculate total using real prices from API
  const cartTotal = cart?.items?.reduce((total, item) => {
    const price = getItemPrice(item);
    return total + (price * item.quantity);
  }, 0) || 0;

  const value = {
    cart,
    loading,
    productDetails,
    offerPrices,
    getItemPrice,
    cartItemCount,
    cartTotal,
    isGuest: !isAuthenticated,
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
