import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { productService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import {
  ShoppingCart,
  Minus,
  Plus,
  Trash2,
  ArrowRight,
  Package,
  ShieldCheck,
} from 'lucide-react';

const Cart = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const {
    cart,
    loading,
    cartItemCount,
    cartTotal,
    productDetails,
    updateCartItem,
    removeFromCart,
    fetchCart,
  } = useCart();
  
  const [localProductDetails, setLocalProductDetails] = useState({});
  const [updating, setUpdating] = useState(null);

  useEffect(() => {
    if (user?.id) {
      fetchCart();
    }
  }, [user?.id]);

  useEffect(() => {
    // Fetch product details for items not already loaded
    const fetchMissingProducts = async () => {
      if (!cart?.items) return;
      
      const missingProducts = cart.items.filter(
        item => !productDetails[item.productId] && !localProductDetails[item.productId]
      );
      
      for (const item of missingProducts) {
        try {
          const response = await productService.getById(item.productId);
          if (response.success) {
            setLocalProductDetails(prev => ({
              ...prev,
              [item.productId]: response.data
            }));
          }
        } catch (error) {
          console.error(`Failed to fetch product ${item.productId}`);
        }
      }
    };
    
    fetchMissingProducts();
  }, [cart?.items, productDetails]);

  const handleUpdateQuantity = async (cartItemId, newQuantity) => {
    if (newQuantity < 1) return;
    setUpdating(cartItemId);
    await updateCartItem(cartItemId, newQuantity);
    setUpdating(null);
  };

  const handleRemoveItem = async (cartItemId) => {
    setUpdating(cartItemId);
    await removeFromCart(cartItemId);
    setUpdating(null);
  };

  const getProductInfo = (productId) => {
    return productDetails[productId] || localProductDetails[productId] || null;
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!cart?.items || cart.items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-12">
        <EmptyState
          icon={ShoppingCart}
          title="Your cart is empty"
          message="Looks like you haven't added anything to your cart yet. Start shopping to fill it up!"
          actionText="Start Shopping"
          actionLink="/search?name=a"
        />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Shopping Cart</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => {
            const product = getProductInfo(item.productId);
            const placeholderImage = `https://via.placeholder.com/150x150?text=Product`;
            
            return (
              <div
                key={item.cartItemId}
                className={`bg-white rounded-lg shadow-sm p-4 flex gap-4 ${
                  updating === item.cartItemId ? 'opacity-50' : ''
                }`}
              >
                {/* Product Image */}
                <Link
                  to={`/product/${item.productId}`}
                  className="w-24 h-24 flex-shrink-0"
                >
                  <img
                    src={product?.imageUrl || placeholderImage}
                    alt={product?.name || 'Product'}
                    className="w-full h-full object-cover rounded-lg"
                    onError={(e) => {
                      e.target.src = placeholderImage;
                    }}
                  />
                </Link>

                {/* Product Info */}
                <div className="flex-1 min-w-0">
                  <Link
                    to={`/product/${item.productId}`}
                    className="text-lg font-medium text-gray-900 hover:text-amazon-orange line-clamp-2"
                  >
                    {product?.name || `Product ${item.productId}`}
                  </Link>
                  
                  {product?.brand && (
                    <p className="text-sm text-gray-500 mt-1">{product.brand}</p>
                  )}
                  
                  <p className="text-sm text-gray-500">
                    Merchant ID: {item.merchantId}
                  </p>
                  
                  <p className="text-green-600 text-sm mt-1">In Stock</p>

                  {/* Quantity & Remove - Mobile */}
                  <div className="flex items-center justify-between mt-4 lg:hidden">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity - 1)}
                        disabled={updating === item.cartItemId || item.quantity <= 1}
                        className="p-1 rounded-md border border-gray-300 hover:bg-gray-100 disabled:opacity-50"
                      >
                        <Minus className="w-4 h-4" />
                      </button>
                      <span className="w-8 text-center font-medium">{item.quantity}</span>
                      <button
                        onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity + 1)}
                        disabled={updating === item.cartItemId}
                        className="p-1 rounded-md border border-gray-300 hover:bg-gray-100 disabled:opacity-50"
                      >
                        <Plus className="w-4 h-4" />
                      </button>
                    </div>
                    <button
                      onClick={() => handleRemoveItem(item.cartItemId)}
                      disabled={updating === item.cartItemId}
                      className="text-red-600 hover:text-red-700 text-sm flex items-center gap-1"
                    >
                      <Trash2 className="w-4 h-4" />
                      Remove
                    </button>
                  </div>
                </div>

                {/* Price & Actions - Desktop */}
                <div className="hidden lg:flex flex-col items-end justify-between">
                  <div className="text-right">
                    <p className="text-lg font-bold text-gray-900">
                      ${(item.priceSnapshot * item.quantity).toFixed(2)}
                    </p>
                    <p className="text-sm text-gray-500">
                      ${item.priceSnapshot.toFixed(2)} each
                    </p>
                  </div>
                  
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity - 1)}
                        disabled={updating === item.cartItemId || item.quantity <= 1}
                        className="p-1 rounded-md border border-gray-300 hover:bg-gray-100 disabled:opacity-50"
                      >
                        <Minus className="w-4 h-4" />
                      </button>
                      <span className="w-8 text-center font-medium">{item.quantity}</span>
                      <button
                        onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity + 1)}
                        disabled={updating === item.cartItemId}
                        className="p-1 rounded-md border border-gray-300 hover:bg-gray-100 disabled:opacity-50"
                      >
                        <Plus className="w-4 h-4" />
                      </button>
                    </div>
                    <button
                      onClick={() => handleRemoveItem(item.cartItemId)}
                      disabled={updating === item.cartItemId}
                      className="text-red-600 hover:text-red-700"
                    >
                      <Trash2 className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>
            
            <div className="space-y-3 mb-4">
              <div className="flex justify-between text-gray-600">
                <span>Items ({cartItemCount})</span>
                <span>${cartTotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Shipping</span>
                <span className="text-green-600">Free</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Estimated Tax</span>
                <span>${(cartTotal * 0.08).toFixed(2)}</span>
              </div>
            </div>
            
            <div className="border-t border-gray-200 pt-4 mb-6">
              <div className="flex justify-between text-lg font-bold text-gray-900">
                <span>Order Total</span>
                <span>${(cartTotal * 1.08).toFixed(2)}</span>
              </div>
            </div>

            <button
              onClick={() => navigate('/checkout')}
              className="w-full btn-buy-now py-3 flex items-center justify-center gap-2"
            >
              Proceed to Checkout
              <ArrowRight className="w-5 h-5" />
            </button>

            {/* Trust Badges */}
            <div className="mt-6 pt-4 border-t border-gray-200 space-y-3">
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <Package className="w-4 h-4 text-amazon-orange" />
                <span>Free shipping on orders over $50</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <ShieldCheck className="w-4 h-4 text-amazon-orange" />
                <span>Secure checkout</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
