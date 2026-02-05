import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { orderService } from '../services/ecommService';
import { productService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';
import {
  ChevronLeft,
  Package,
  Truck,
  CheckCircle,
  Clock,
  XCircle,
} from 'lucide-react';

const OrderDetails = () => {
  const { orderId } = useParams();
  const [order, setOrder] = useState(null);
  const [productDetails, setProductDetails] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrder();
  }, [orderId]);

  const fetchOrder = async () => {
    try {
      const response = await orderService.getOrderById(orderId);
      if (response.success) {
        setOrder(response.data);
        
        // Fetch product details for images and additional info
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
      setError('Order not found');
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PAID':
      case 'DELIVERED':
        return <CheckCircle className="w-5 h-5 text-green-500" />;
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-500" />;
      case 'SHIPPED':
        return <Truck className="w-5 h-5 text-blue-500" />;
      default:
        return <Clock className="w-5 h-5 text-yellow-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PAID':
      case 'DELIVERED':
        return 'bg-green-100 text-green-700';
      case 'FAILED':
        return 'bg-red-100 text-red-700';
      case 'SHIPPED':
        return 'bg-blue-100 text-blue-700';
      default:
        return 'bg-yellow-100 text-yellow-700';
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="container mx-auto px-4 py-12">
        <Alert
          type="error"
          title="Order Not Found"
          message={error || "We couldn't find this order."}
        />
        <Link to="/profile" className="btn-primary mt-4 inline-block">
          Go to Profile
        </Link>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <Link
        to="/profile"
        className="inline-flex items-center gap-1 text-amazon-blue hover:text-amazon-orange mb-6"
      >
        <ChevronLeft className="w-4 h-4" />
        Back to Profile
      </Link>

      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">
          Order #{order.orderId}
        </h1>
        <div className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(order.orderStatus)}`}>
          {order.orderStatus}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Order Items */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <Package className="w-5 h-5 text-amazon-orange" />
              Order Items
            </h2>
            
            <div className="divide-y divide-gray-100">
              {order.items?.map((item) => {
                const product = productDetails[item.productId];
                return (
                  <div key={item.orderItemId} className="py-4 flex gap-4">
                    <Link
                      to={`/product/${item.productId}`}
                      className="w-20 h-20 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0"
                    >
                      <img
                        src={product?.imageUrl || `https://via.placeholder.com/80x80?text=P`}
                        alt={product?.name || 'Product'}
                        className="w-full h-full object-cover"
                      />
                    </Link>
                    <div className="flex-1">
                      <Link
                        to={`/product/${item.productId}`}
                        className="font-medium text-gray-900 hover:text-amazon-orange"
                      >
                        {item.productName || product?.name || `Product ${item.productId}`}
                      </Link>
                      {product?.brand && (
                        <p className="text-sm text-gray-500">{product.brand}</p>
                      )}
                      <p className="text-sm text-gray-500">Qty: {item.quantity}</p>
                      <p className="text-sm text-gray-500">Merchant: #{item.merchantId}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-gray-900">
                        ${(item.price * item.quantity).toFixed(2)}
                      </p>
                      <p className="text-sm text-gray-500">
                        ${item.price.toFixed(2)} each
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1 space-y-6">
          {/* Order Info */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Order Information</h2>
            
            <div className="space-y-4 text-sm">
              <div>
                <span className="text-gray-500 block">Order Date</span>
                <span className="font-medium">
                  {new Date(order.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </span>
              </div>
              
              <div>
                <span className="text-gray-500 block">Order Status</span>
                <div className="flex items-center gap-2 mt-1">
                  {getStatusIcon(order.orderStatus)}
                  <span className="font-medium">{order.orderStatus}</span>
                </div>
              </div>
              
              <div>
                <span className="text-gray-500 block">Payment Status</span>
                <div className="flex items-center gap-2 mt-1">
                  {getStatusIcon(order.paymentStatus)}
                  <span className="font-medium">{order.paymentStatus}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Shipping Address */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Shipping Address</h2>
            <p className="text-gray-600">{order.shippingAddress}</p>
          </div>

          {/* Order Total */}
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Order Total</h2>
            
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Subtotal</span>
                <span>${order.totalAmount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Shipping</span>
                <span className="text-green-600">Free</span>
              </div>
              <div className="border-t border-gray-200 pt-2 mt-2">
                <div className="flex justify-between text-lg font-bold">
                  <span>Total</span>
                  <span>${order.totalAmount.toFixed(2)}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderDetails;
