import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { productService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';
import {
  MapPin,
  CreditCard,
  Lock,
  ShieldCheck,
  ChevronLeft,
  Check,
} from 'lucide-react';

const Checkout = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { cart, cartItemCount, cartTotal, checkout, productDetails, fetchCart } = useCart();
  
  const [shippingAddress, setShippingAddress] = useState({
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'United States',
  });
  const [localProductDetails, setLocalProductDetails] = useState({});
  const [loading, setLoading] = useState(false);
  const [checkoutError, setCheckoutError] = useState(null);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (user?.id) {
      fetchCart();
    }
  }, [user?.id]);

  useEffect(() => {
    // Redirect if cart is empty
    if (!cart?.items || cart.items.length === 0) {
      navigate('/cart');
    }
  }, [cart, navigate]);

  useEffect(() => {
    // Fetch product details
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

  const validate = () => {
    const newErrors = {};
    if (!shippingAddress.street.trim()) {
      newErrors.street = 'Street address is required';
    }
    if (!shippingAddress.city.trim()) {
      newErrors.city = 'City is required';
    }
    if (!shippingAddress.state.trim()) {
      newErrors.state = 'State is required';
    }
    if (!shippingAddress.zipCode.trim()) {
      newErrors.zipCode = 'ZIP code is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setCheckoutError(null);
    
    if (!validate()) return;

    const fullAddress = `${shippingAddress.street}, ${shippingAddress.city}, ${shippingAddress.state} ${shippingAddress.zipCode}, ${shippingAddress.country}`;

    setLoading(true);
    const result = await checkout(fullAddress);
    setLoading(false);

    if (result.success) {
      navigate(`/order-confirmation/${result.data.orderId}`);
    } else {
      setCheckoutError(result.message || 'Checkout failed. Please try again.');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setShippingAddress(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const getProductInfo = (productId) => {
    return productDetails[productId] || localProductDetails[productId] || null;
  };

  if (!cart?.items || cart.items.length === 0) {
    return <LoadingSpinner fullScreen />;
  }

  const tax = cartTotal * 0.08;
  const total = cartTotal + tax;

  return (
    <div className="container mx-auto px-4 py-8">
      <Link
        to="/cart"
        className="inline-flex items-center gap-1 text-amazon-blue hover:text-amazon-orange mb-6"
      >
        <ChevronLeft className="w-4 h-4" />
        Back to Cart
      </Link>

      <h1 className="text-2xl font-bold text-gray-900 mb-6">Checkout</h1>

      {checkoutError && (
        <div className="mb-6">
          <Alert
            type="error"
            title="Checkout Failed"
            message={checkoutError}
            onClose={() => setCheckoutError(null)}
          />
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Shipping & Payment */}
          <div className="lg:col-span-2 space-y-6">
            {/* Shipping Address */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <MapPin className="w-5 h-5 text-amazon-orange" />
                Shipping Address
              </h2>
              
              <div className="space-y-4">
                <div>
                  <label htmlFor="street" className="block text-sm font-medium text-gray-700 mb-1">
                    Street Address
                  </label>
                  <input
                    type="text"
                    id="street"
                    name="street"
                    value={shippingAddress.street}
                    onChange={handleChange}
                    className={`input-field ${errors.street ? 'border-red-500' : ''}`}
                    placeholder="123 Main St"
                  />
                  {errors.street && <p className="mt-1 text-sm text-red-500">{errors.street}</p>}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-1">
                      City
                    </label>
                    <input
                      type="text"
                      id="city"
                      name="city"
                      value={shippingAddress.city}
                      onChange={handleChange}
                      className={`input-field ${errors.city ? 'border-red-500' : ''}`}
                      placeholder="New York"
                    />
                    {errors.city && <p className="mt-1 text-sm text-red-500">{errors.city}</p>}
                  </div>
                  <div>
                    <label htmlFor="state" className="block text-sm font-medium text-gray-700 mb-1">
                      State
                    </label>
                    <input
                      type="text"
                      id="state"
                      name="state"
                      value={shippingAddress.state}
                      onChange={handleChange}
                      className={`input-field ${errors.state ? 'border-red-500' : ''}`}
                      placeholder="NY"
                    />
                    {errors.state && <p className="mt-1 text-sm text-red-500">{errors.state}</p>}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label htmlFor="zipCode" className="block text-sm font-medium text-gray-700 mb-1">
                      ZIP Code
                    </label>
                    <input
                      type="text"
                      id="zipCode"
                      name="zipCode"
                      value={shippingAddress.zipCode}
                      onChange={handleChange}
                      className={`input-field ${errors.zipCode ? 'border-red-500' : ''}`}
                      placeholder="10001"
                    />
                    {errors.zipCode && <p className="mt-1 text-sm text-red-500">{errors.zipCode}</p>}
                  </div>
                  <div>
                    <label htmlFor="country" className="block text-sm font-medium text-gray-700 mb-1">
                      Country
                    </label>
                    <select
                      id="country"
                      name="country"
                      value={shippingAddress.country}
                      onChange={handleChange}
                      className="input-field"
                    >
                      <option value="United States">United States</option>
                      <option value="Canada">Canada</option>
                      <option value="United Kingdom">United Kingdom</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            {/* Payment (Simulated) */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-amazon-orange" />
                Payment Method
              </h2>
              
              <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-amazon-orange/10 rounded-full flex items-center justify-center">
                    <Check className="w-5 h-5 text-amazon-orange" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Demo Payment</p>
                    <p className="text-sm text-gray-500">
                      This is a demo checkout. No real payment will be processed.
                    </p>
                  </div>
                </div>
              </div>
              
              <div className="mt-4 flex items-center gap-2 text-sm text-gray-500">
                <Lock className="w-4 h-4" />
                <span>Your payment information is secure</span>
              </div>
            </div>

            {/* Order Items */}
            <div className="bg-white rounded-lg shadow-sm p-6">
              <h2 className="text-lg font-bold text-gray-900 mb-4">
                Order Items ({cartItemCount})
              </h2>
              
              <div className="divide-y divide-gray-100">
                {cart.items.map((item) => {
                  const product = getProductInfo(item.productId);
                  return (
                    <div key={item.cartItemId} className="py-4 flex gap-4">
                      <div className="w-16 h-16 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                        <img
                          src={product?.imageUrl || `https://via.placeholder.com/64x64?text=P`}
                          alt={product?.name || 'Product'}
                          className="w-full h-full object-cover"
                        />
                      </div>
                      <div className="flex-1">
                        <p className="font-medium text-gray-900 line-clamp-1">
                          {product?.name || `Product ${item.productId}`}
                        </p>
                        <p className="text-sm text-gray-500">Qty: {item.quantity}</p>
                      </div>
                      <div className="text-right">
                        <p className="font-medium text-gray-900">
                          ${(item.priceSnapshot * item.quantity).toFixed(2)}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
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
                  <span>${tax.toFixed(2)}</span>
                </div>
              </div>
              
              <div className="border-t border-gray-200 pt-4 mb-6">
                <div className="flex justify-between text-lg font-bold text-gray-900">
                  <span>Order Total</span>
                  <span>${total.toFixed(2)}</span>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full btn-buy-now py-3 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <>
                    <LoadingSpinner size="sm" />
                    Processing...
                  </>
                ) : (
                  <>
                    <Lock className="w-5 h-5" />
                    Place Order
                  </>
                )}
              </button>

              <div className="mt-4 text-center text-sm text-gray-500">
                By placing your order, you agree to our Terms of Service
              </div>

              {/* Trust Badges */}
              <div className="mt-6 pt-4 border-t border-gray-200">
                <div className="flex items-center justify-center gap-2 text-sm text-gray-600">
                  <ShieldCheck className="w-5 h-5 text-green-600" />
                  <span>Secure SSL Checkout</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

export default Checkout;
