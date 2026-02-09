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
  // [CHANGE 1] Get isMerchant from Auth
  const { user, isAuthenticated, isMerchant } = useAuth();

  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);
  const [productDetails, setProductDetails] = useState({});
  const [offerPrices, setOfferPrices] = useState({});
  const [hasSyncedGuestCart, setHasSyncedGuestCart] = useState(false);

  // [CHANGE 2] Optimized Bulk Fetch for Prices (1 Call instead of N loops)
  const fetchOfferPrices = useCallback(async (items) => {
    if (!items?.length) return;

    // Filter out items we already have prices for
    const itemsNeedingPrices = items.filter(
      item => !offerPrices[`${item.productId}_${item.merchantId}`]
    );

    if (itemsNeedingPrices.length === 0) return;

    // Get unique Product IDs
    const productIds = [...new Set(itemsNeedingPrices.map(item => item.productId))];
    const newPrices = {};

    try {
      // ONE single API call for all products
      const response = await offerService.getBulkOffers(productIds);

      if (response.success && response.data) {
        const bulkData = response.data; // { productId: [offers] }

        itemsNeedingPrices.forEach(item => {
          const key = `${item.productId}_${item.merchantId}`;
          const productOffers = bulkData[item.productId];

          if (productOffers && Array.isArray(productOffers)) {
            // Find the offer for this specific merchant
            const offer = productOffers.find(o => o.merchantId === item.merchantId) || productOffers[0];

            if (offer) {
              // Normalize price
              const raw = offer.price;
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
              newPrices[key] = isFinite(numeric) ? numeric : 0;
            }
          }
        });

        setOfferPrices(prev => ({ ...prev, ...newPrices }));
      }
    } catch (error) {
      console.error('Failed to fetch bulk prices:', error);
    }
  }, [offerPrices]);

  // [CHANGE 3] Parallel Fetch for Products (Faster than sequential loop)
  const fetchProductDetails = useCallback(async (items) => {
    if (!items?.length) return;

    const uniqueProductIds = [...new Set(items.map(item => item.productId))];
    // Filter ones we don't have
    const idsToFetch = uniqueProductIds.filter(id => !productDetails[id]);

    if (idsToFetch.length === 0) {
      // Just check prices if we already have products
      await fetchOfferPrices(items);
      return;
    }

    const newDetails = {};

    // Execute all requests in parallel
    await Promise.all(idsToFetch.map(async (productId) => {
      try {
        const productResponse = await productService.getById(productId);
        if (productResponse.success) {
          newDetails[productId] = productResponse.data;
        }
      } catch (error) {
        console.error(`Failed to fetch product ${productId}:`, error);
      }
    }));

    setProductDetails(prev => ({ ...prev, ...newDetails }));

    // Also fetch prices
    await fetchOfferPrices(items);
  }, [productDetails, fetchOfferPrices]);

  // Fetch server cart
  const fetchCart = useCallback(async () => {
    // [CHANGE 4] Guard: Merchants don't need carts
    if (isMerchant) return;
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
  }, [user?.id, fetchProductDetails, isMerchant]);

  // Sync guest cart
  const syncGuestCartToServer = useCallback(async () => {
    // [CHANGE 5] Guard: Merchants don't sync carts
    if (isMerchant) {
        setHasSyncedGuestCart(true);
        clearGuestCartStorage(); // Optional: clear garbage data
        return;
    }
    if (!user?.id) return;

    const guestCart = getGuestCart();
    if (guestCart.items.length === 0) {
      setHasSyncedGuestCart(true);
      return;
    }

    setLoading(true);
    try {
      for (const item of guestCart.items) {
        try {
          await cartService.addItem(user.id, {
            productId: item.productId,
            merchantId: item.merchantId,
            quantity: item.quantity,
          });
        } catch (error) {
          console.error('Failed to sync item:', item, error);
        }
      }

      clearGuestCartStorage();
      toast.success('Your cart items have been saved!');

      await fetchCart();
      setHasSyncedGuestCart(true);
    } catch (error) {
      console.error('Failed to sync guest cart:', error);
      setHasSyncedGuestCart(true);
    } finally {
      setLoading(false);
    }
  }, [user?.id, fetchCart, isMerchant]);

  // Initialize cart
  useEffect(() => {
    // [CHANGE 6] CRITICAL: Stop everything if Merchant
    if (isMerchant) return;

    if (isAuthenticated && user?.id) {
      if (!hasSyncedGuestCart) {
        syncGuestCartToServer();
      } else {
        fetchCart();
      }
    } else {
      const guestCart = getGuestCart();
      setCart(guestCart);
      fetchProductDetails(guestCart.items);
      setHasSyncedGuestCart(false);
    }
  }, [isAuthenticated, user?.id, hasSyncedGuestCart, isMerchant, fetchCart, syncGuestCartToServer, fetchProductDetails]);

  // Actions
  const addToCart = async (item) => {
    if (isMerchant) {
        toast.error("Merchants cannot shop from their own account.");
        return { success: false };
    }

    if (isAuthenticated && user?.id) {
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
      const guestCart = getGuestCart();
      const existingIndex = guestCart.items.findIndex(
        i => i.productId === item.productId && i.merchantId === item.merchantId
      );

      if (existingIndex >= 0) {
        guestCart.items[existingIndex].quantity += item.quantity;
      } else {
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

  const updateCartItem = async (cartItemId, quantity) => {
    if (isAuthenticated && user?.id) {
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

  const removeFromCart = async (cartItemId) => {
    if (isAuthenticated && user?.id) {
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
      const guestCart = getGuestCart();
      guestCart.items = guestCart.items.filter(i => i.cartItemId !== cartItemId);
      saveGuestCart(guestCart);
      setCart(guestCart);
      toast.success('Item removed from cart');
      return { success: true };
    }
  };

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

  const getItemPrice = useCallback((item) => {
    if (isAuthenticated && item.priceSnapshot) {
      return item.priceSnapshot;
    }
    const key = `${item.productId}_${item.merchantId}`;
    return offerPrices[key] || 0;
  }, [isAuthenticated, offerPrices]);

  const cartItemCount = cart?.items?.reduce((total, item) => total + item.quantity, 0) || 0;

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