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
        setOrder(response.data);
        
        // Fetch product details
        for (const item of response.data.items || []) {
          try {
            const productResponse = await productService.getById(item.productId);
            if (productResponse.success) {
              setProductDetails(prev => ({
                ...prev,
                [item.productId]: productResponse.data
              }));
            }
          } catch (error) {
            console.error(`Failed to fetch product ${item.productId}`);
          }
        }
      }
    } catch (error) {
      console.error('Failed to fetch order:', error);
    } finally {
      setLoading(false);
    }
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
        <Link to="/" className="btn-primary">
          Go Home
        </Link>
      </div>
    );
  }

  const steps = [
    { icon: CheckCircle, label: 'Order Placed', completed: true },
    { icon: Package, label: 'Processing', completed: order.orderStatus === 'PAID' || order.orderStatus === 'SHIPPED' },
    { icon: Truck, label: 'Shipped', completed: order.orderStatus === 'SHIPPED' || order.orderStatus === 'DELIVERED' },
    { icon: Home, label: 'Delivered', completed: order.orderStatus === 'DELIVERED' },
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
          Your order has been placed and is being processed.
        </p>
        <p className="text-lg font-semibold text-amazon-orange">
          Order #{order.orderId}
        </p>
      </div>

      {/* Email Confirmation */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-8 flex items-center gap-4">
        <Mail className="w-6 h-6 text-blue-500 flex-shrink-0" />
        <p className="text-blue-800">
          A confirmation email will be sent to your registered email address with order details.
        </p>
      </div>

      {/* Order Progress */}
      <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
        <h2 className="text-lg font-bold text-gray-900 mb-6">Order Status</h2>
        <div className="flex items-center justify-between">
          {steps.map((step, index) => (
            <div key={step.label} className="flex items-center">
              <div className="flex flex-col items-center">
                <div
                  className={`w-12 h-12 rounded-full flex items-center justify-center ${
                    step.completed
                      ? 'bg-green-500 text-white'
                      : 'bg-gray-200 text-gray-400'
                  }`}
                >
                  <step.icon className="w-6 h-6" />
                </div>
                <span
                  className={`mt-2 text-sm ${
                    step.completed ? 'text-green-600 font-medium' : 'text-gray-400'
                  }`}
                >
                  {step.label}
                </span>
              </div>
              {index < steps.length - 1 && (
                <div
                  className={`h-1 w-16 md:w-24 lg:w-32 mx-2 ${
                    steps[index + 1].completed ? 'bg-green-500' : 'bg-gray-200'
                  }`}
                />
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Order Details */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Order Details</h2>
          
          <div className="space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-500">Order Number</span>
              <span className="font-medium">#{order.orderId}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Order Date</span>
              <span className="font-medium">
                {new Date(order.createdAt).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                })}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Status</span>
              <span className={`font-medium px-2 py-0.5 rounded-full text-xs ${
                order.orderStatus === 'PAID' || order.orderStatus === 'DELIVERED'
                  ? 'bg-green-100 text-green-700'
                  : order.orderStatus === 'FAILED'
                  ? 'bg-red-100 text-red-700'
                  : 'bg-yellow-100 text-yellow-700'
              }`}>
                {order.orderStatus}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Payment Status</span>
              <span className={`font-medium px-2 py-0.5 rounded-full text-xs ${
                order.paymentStatus === 'PAID'
                  ? 'bg-green-100 text-green-700'
                  : order.paymentStatus === 'FAILED'
                  ? 'bg-red-100 text-red-700'
                  : 'bg-yellow-100 text-yellow-700'
              }`}>
                {order.paymentStatus}
              </span>
            </div>
            <div className="border-t border-gray-100 pt-3 mt-3">
              <span className="text-gray-500">Shipping Address</span>
              <p className="font-medium mt-1">{order.shippingAddress}</p>
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>
          
          <div className="divide-y divide-gray-100 mb-4">
            {order.items?.map((item) => {
              const product = productDetails[item.productId];
              return (
                <div key={item.orderItemId} className="py-3 flex gap-4">
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
                      ${(item.price * item.quantity).toFixed(2)}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>

          <div className="border-t border-gray-200 pt-4 space-y-2">
            <div className="flex justify-between text-gray-600">
              <span>Subtotal</span>
              <span>${order.totalAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Shipping</span>
              <span className="text-green-600">Free</span>
            </div>
            <div className="flex justify-between text-lg font-bold text-gray-900 pt-2">
              <span>Total</span>
              <span>${order.totalAmount.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Continue Shopping */}
      <div className="mt-8 text-center">
        <Link
          to="/search?name=a"
          className="btn-primary inline-flex items-center gap-2"
        >
          Continue Shopping
          <ArrowRight className="w-5 h-5" />
        </Link>
      </div>
    </div>
  );
};

export default OrderConfirmation;
