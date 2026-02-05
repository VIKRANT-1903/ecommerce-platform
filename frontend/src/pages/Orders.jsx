import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { orderService } from '../services/ecommService';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import Alert from '../components/common/Alert';
import { Package, ChevronRight } from 'lucide-react';

const Orders = () => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (user?.id) fetchOrders();
  }, [user]);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const resp = await orderService.getOrdersByUser(user.id);
      if (resp.success) {
        setOrders(resp.data || []);
      } else {
        setError(resp.message || 'Failed to fetch orders');
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch orders');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner fullScreen />;

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Alert 
          type="error" 
          title="Error Loading Orders" 
          message={error}
          onClose={() => setError(null)}
        />
      </div>
    );
  }

  if (!orders || orders.length === 0) {
    return (
      <div className="container mx-auto px-4 py-12">
        <EmptyState
          icon={Package}
          title="No orders yet"
          message="You haven't placed any orders yet. Start shopping to see orders here."
          actionText="Start Shopping"
          actionLink="/search"
        />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">My Orders</h1>
      </div>

      <div className="space-y-4">
        {orders.map(order => (
          <div key={order.orderId} className="bg-white rounded-lg shadow-sm p-4 flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Order #{order.orderId}</p>
              <p className="font-medium text-gray-900">{new Date(order.createdAt).toLocaleString()}</p>
              <p className="text-sm text-gray-600">Total: ${order.totalAmount.toFixed(2)} • {order.orderStatus}</p>
            </div>
            <div>
              <Link to={`/order/${order.orderId}`} className="inline-flex items-center gap-2 text-amazon-blue hover:text-amazon-orange">
                View Details
                <ChevronRight />
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Orders;
