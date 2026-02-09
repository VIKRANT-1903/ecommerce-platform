import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderService } from '../services/ecommService';
import { productService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import {
  CheckCircle,
  Package,
  Truck,
  Home,
  ArrowRight,
  Mail,
} from 'lucide-react';

// --- Safe Local Fallback Image (Prevents broken images) ---
const getFallbackImage = (text) => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64">
      <rect width="64" height="64" fill="#f3f4f6"/>
      <text x="50%" y="50%" font-family="sans-serif" font-size="10" fill="#9ca3af" text-anchor="middle" dominant-baseline="middle">
        ${text}
      </text>
    </svg>
  `;
  return `data:image/svg+xml;base64,${btoa(svg)}`;
};

const OrderConfirmation = () => {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [productDetails, setProductDetails] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrder();
  }, [orderId]);

  const fetchOrder = async () => {
    try {
      const response = await orderService.getOrderById(orderId);
      if (response.success) {
        const orderData = response.data;
        setOrder(orderData);

        if (orderData.items && orderData.items.length > 0) {
          const promises = orderData.items.map(item =>
            productService.getById(item.productId)
              .then(res => ({ id: item.productId, data: res.data }))
              .catch(err => ({ id: item.productId, data: null }))
          );

          const results = await Promise.all(promises);
          const detailsMap = {};
          results.forEach(result => {
            if (result.data) {
              detailsMap[result.id] = result.data;
            }
          });
          setProductDetails(detailsMap);
        }
      }
    } catch (error) {
      console.error('Failed to fetch order:', error);
    } finally {
      setLoading(false);
    }
  };

  const isAtLeast = (status, required) => {
    const levels = { 'PENDING': 0, 'PAID': 1, 'SHIPPED': 2, 'DELIVERED': 3, 'FAILED': -1 };
    const currentLevel = levels[status] || 0;
    const requiredLevel = levels[required] || 0;
    return currentLevel >= requiredLevel;
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="container mx-auto px-4 py-12 text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Order Not Found</h1>
        <p className="text-gray-600 mb-6">We couldn't find the order you're looking for.</p>
        <Link to="/" className="btn-primary">
          Go Home
        </Link>
      </div>
    );
  }

  const steps = [
    { icon: CheckCircle, label: 'Order Placed', completed: true },
    { icon: Package, label: 'Processing', completed: isAtLeast(order.orderStatus, 'PAID') },
    { icon: Truck, label: 'Shipped', completed: isAtLeast(order.orderStatus, 'SHIPPED') },
    { icon: Home, label: 'Delivered', completed: isAtLeast(order.orderStatus, 'DELIVERED') },
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Success Banner */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-6 mb-8 text-center">
        <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          Thank you for your order!
        </h1>
        <p className="text-gray-600 mb-4">
          Your order has been placed successfully.
        </p>
        <div className="inline-block bg-white px-4 py-2 rounded-full border border-green-200">
          <p className="text-sm font-semibold text-gray-700">
            {/* CHANGED: Use friendly order number */}
            Order Number: <span className="text-amazon-orange">#{order.userOrderNumber || order.orderId}</span>
          </p>
        </div>
      </div>

      {/* Email Confirmation Notification */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-8 flex items-start gap-4">
        <Mail className="w-6 h-6 text-blue-500 flex-shrink-0 mt-0.5" />
        <div>
          <h3 className="font-semibold text-blue-900">Confirmation Email Sent</h3>
          <p className="text-sm text-blue-800 mt-1">
            We have sent a confirmation email to your registered address.
          </p>
        </div>
      </div>

      {/* Order Progress */}
      <div className="bg-white rounded-lg shadow-sm p-6 mb-8 overflow-x-auto">
        <h2 className="text-lg font-bold text-gray-900 mb-6">Order Status</h2>
        <div className="flex items-center justify-between min-w-[600px]">
          {steps.map((step, index) => (
            <div key={step.label} className="flex items-center flex-1 last:flex-none">
              <div className="flex flex-col items-center relative z-10">
                <div className={`w-12 h-12 rounded-full flex items-center justify-center transition-colors duration-300 ${
                    step.completed ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-400'
                  }`}
                >
                  <step.icon className="w-6 h-6" />
                </div>
                <span className={`mt-2 text-sm font-medium ${step.completed ? 'text-green-600' : 'text-gray-400'}`}>
                  {step.label}
                </span>
              </div>
              {index < steps.length - 1 && (
                <div className="flex-1 h-1 mx-4 bg-gray-200 rounded">
                  <div className={`h-full rounded transition-all duration-500 ${
                      steps[index + 1].completed ? 'bg-green-500 w-full' : 'w-0'
                    }`}
                  />
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Order Details */}
        <div className="lg:col-span-2 space-y-8">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Items Ordered</h2>
            <div className="divide-y divide-gray-100">
              {order.items?.map((item) => {
                const product = productDetails[item.productId];
                const itemTotal = (item.price || 0) * (item.quantity || 1);

                return (
                  <div key={item.orderItemId || item.productId} className="py-4 flex gap-4">
                    <div className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0 border border-gray-200">
                      <img
                        src={product?.imageUrl || getFallbackImage(product?.name || 'Product')}
                        alt={product?.name || 'Product'}
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src = getFallbackImage('Product');
                        }}
                      />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-gray-900 truncate">
                        {product?.name || `Product #${item.productId}`}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">Qty: {item.quantity}</p>
                      <p className="text-sm text-gray-500">Price: ${item.price?.toFixed(2)}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-gray-900">${itemTotal.toFixed(2)}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>
            <div className="space-y-3 text-sm pb-4 border-b border-gray-100">
              <div className="flex justify-between text-gray-600">
                 <span>Order Number</span>
                 {/* CHANGED: Use friendly number */}
                 <span className="font-medium text-gray-900">#{order.userOrderNumber || order.orderId}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Subtotal</span>
                <span>${order.totalAmount?.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Shipping</span>
                <span className="text-green-600 font-medium">Free</span>
              </div>
            </div>
            <div className="pt-4 flex justify-between items-center">
              <span className="font-bold text-gray-900 text-lg">Total</span>
              <span className="font-bold text-amazon-orange text-xl">
                ${order.totalAmount?.toFixed(2)}
              </span>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-4">Shipping Details</h2>
            <div className="text-sm text-gray-600 space-y-3">
              <div>
                <span className="block text-xs text-gray-400 mb-1">Status</span>
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                  order.orderStatus === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                  order.orderStatus === 'FAILED' ? 'bg-red-100 text-red-800' :
                  'bg-blue-100 text-blue-800'
                }`}>
                  {order.orderStatus}
                </span>
              </div>
              <div>
                <span className="block text-xs text-gray-400 mb-1">Shipping Address</span>
                <p className="font-medium">{order.shippingAddress}</p>
              </div>
              <div>
                <span className="block text-xs text-gray-400 mb-1">Order Date</span>
                <p>
                  {new Date(order.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric', month: 'long', day: 'numeric',
                    hour: '2-digit', minute: '2-digit'
                  })}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-12 text-center">
        <Link
          to="/search?name=a"
          className="btn-primary inline-flex items-center gap-2 px-8 py-3 text-lg"
        >
          Continue Shopping
          <ArrowRight className="w-5 h-5" />
        </Link>
      </div>
    </div>
  );
};

export default OrderConfirmation;