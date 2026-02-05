// Helper to extract numeric price from offer
function getOfferNumericPrice(offer) {
  if (!offer) return 0;
  const raw = offer.price;
  let price = 0;
  if (typeof raw === 'number') price = raw;
  else if (raw && typeof raw === 'object') {
    price = raw.amount ?? raw.value ?? raw.price ?? raw.cents ?? 0;
    if (raw.cents && !raw.amount && !raw.value) price = raw.cents / 100;
  } else if (typeof offer?.priceCents === 'number') {
    price = offer.priceCents / 100;
  } else if (typeof offer?.amount === 'number') {
    price = offer.amount;
  }
  return isFinite(price) ? price : 0;
}
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { merchantService } from '../services/authService';
import { offerService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';
import {
  Store,
  Package,
  DollarSign,
  TrendingUp,
  Settings,
  Star,
  Edit3,
  Save,
  X,
  Power,
  CheckCircle,
  XCircle,
} from 'lucide-react';
import toast from 'react-hot-toast';

const MerchantDashboard = () => {
  const { merchantProfile, refreshMerchantProfile } = useAuth();
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isEditingName, setIsEditingName] = useState(false);
  const [newName, setNewName] = useState('');
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      await refreshMerchantProfile();
      
      const offersResponse = await offerService.getMyOffers();
      if (offersResponse.success) {
        setOffers(offersResponse.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateName = async () => {
    if (!newName.trim()) {
      toast.error('Store name is required');
      return;
    }

    setUpdating(true);
    try {
      const response = await merchantService.updateProfile({ name: newName });
      if (response.success) {
        toast.success('Store name updated');
        await refreshMerchantProfile();
        setIsEditingName(false);
      }
    } catch (error) {
      toast.error('Failed to update store name');
    } finally {
      setUpdating(false);
    }
  };

  const handleStatusToggle = async () => {
    const newStatus = merchantProfile?.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    
    setUpdating(true);
    try {
      const response = await merchantService.updateStatus(newStatus);
      if (response.success) {
        toast.success(`Store ${newStatus === 'ACTIVE' ? 'activated' : 'deactivated'}`);
        await refreshMerchantProfile();
      }
    } catch (error) {
      toast.error('Failed to update status');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const activeOffers = offers.filter(o => o.status === 'ACTIVE').length;
  const totalRevenue = offers.reduce((sum, o) => sum + getOfferNumericPrice(o), 0);

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Merchant Dashboard</h1>
        <div className="flex gap-3">
          <Link to="/merchant/inventory" className="btn-secondary flex items-center gap-2">
            <Package className="w-4 h-4" />
            Manage Inventory
          </Link>
          <Link to="/merchant/products" className="btn-primary flex items-center gap-2">
            <Package className="w-4 h-4" />
            Add Product
          </Link>
        </div>
      </div>

      {/* Store Status Alert */}
      {merchantProfile?.status === 'INACTIVE' && (
        <div className="mb-6">
          <Alert
            type="warning"
            title="Store Inactive"
            message="Your store is currently inactive. Customers cannot see your offers."
          />
        </div>
      )}

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Total Offers</p>
              <p className="text-3xl font-bold text-gray-900">{offers.length}</p>
            </div>
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
              <Package className="w-6 h-6 text-blue-600" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Active Offers</p>
              <p className="text-3xl font-bold text-green-600">{activeOffers}</p>
            </div>
            <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
              <CheckCircle className="w-6 h-6 text-green-600" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Store Rating</p>
              <p className="text-3xl font-bold text-gray-900">
                {merchantProfile?.rating?.toFixed(1) || '0.0'}
              </p>
            </div>
            <div className="w-12 h-12 bg-yellow-100 rounded-full flex items-center justify-center">
              <Star className="w-6 h-6 text-yellow-600" />
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500">Avg. Price</p>
              <p className="text-3xl font-bold text-gray-900">
                ${offers.length > 0 ? (totalRevenue / offers.length).toFixed(2) : '0.00'}
              </p>
            </div>
            <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
              <DollarSign className="w-6 h-6 text-purple-600" />
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Store Settings */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <Settings className="w-5 h-5 text-amazon-orange" />
              Store Settings
            </h2>

            {/* Store Name */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-500 mb-1">
                Store Name
              </label>
              {isEditingName ? (
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={newName}
                    onChange={(e) => setNewName(e.target.value)}
                    className="input-field flex-1"
                    placeholder="Enter store name"
                  />
                  <button
                    onClick={handleUpdateName}
                    disabled={updating}
                    className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50"
                  >
                    <Save className="w-5 h-5" />
                  </button>
                  <button
                    onClick={() => setIsEditingName(false)}
                    className="p-2 bg-gray-200 text-gray-600 rounded-lg hover:bg-gray-300"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
              ) : (
                <div className="flex items-center justify-between">
                  <p className="font-semibold text-gray-900">
                    {merchantProfile?.name || 'My Store'}
                  </p>
                  <button
                    onClick={() => {
                      setNewName(merchantProfile?.name || '');
                      setIsEditingName(true);
                    }}
                    className="text-amazon-blue hover:text-amazon-orange"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                </div>
              )}
            </div>

            {/* Merchant ID */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-500 mb-1">
                Merchant ID
              </label>
              <p className="font-mono text-gray-900">#{merchantProfile?.merchantId}</p>
            </div>

            {/* Store Status */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-500 mb-1">
                Store Status
              </label>
              <div className="flex items-center justify-between">
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                  merchantProfile?.status === 'ACTIVE'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-red-100 text-red-700'
                }`}>
                  {merchantProfile?.status || 'UNKNOWN'}
                </span>
                <button
                  onClick={handleStatusToggle}
                  disabled={updating}
                  className={`flex items-center gap-2 px-3 py-1 rounded-lg text-sm ${
                    merchantProfile?.status === 'ACTIVE'
                      ? 'bg-red-100 text-red-700 hover:bg-red-200'
                      : 'bg-green-100 text-green-700 hover:bg-green-200'
                  } disabled:opacity-50`}
                >
                  <Power className="w-4 h-4" />
                  {merchantProfile?.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                </button>
              </div>
            </div>

            {/* Member Since */}
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">
                Member Since
              </label>
              <p className="text-gray-900">
                {merchantProfile?.createdAt
                  ? new Date(merchantProfile.createdAt).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                    })
                  : 'N/A'}
              </p>
            </div>
          </div>
        </div>

        {/* Recent Offers */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                <TrendingUp className="w-5 h-5 text-amazon-orange" />
                Recent Offers
              </h2>
              <Link
                to="/merchant/offers"
                className="text-amazon-blue hover:text-amazon-orange text-sm"
              >
                View All
              </Link>
            </div>

            {offers.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-3 text-sm font-medium text-gray-500">
                        Offer ID
                      </th>
                      <th className="text-left py-3 text-sm font-medium text-gray-500">
                        Product ID
                      </th>
                      <th className="text-left py-3 text-sm font-medium text-gray-500">
                        Price
                      </th>
                      <th className="text-left py-3 text-sm font-medium text-gray-500">
                        Status
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {offers.slice(0, 5).map((offer) => (
                      <tr key={offer.offerId} className="border-b border-gray-100">
                        <td className="py-3 text-sm text-gray-900">#{offer.offerId}</td>
                        <td className="py-3 text-sm text-gray-600 font-mono text-xs">
                          {offer.productId.substring(0, 12)}...
                        </td>
                        <td className="py-3 text-sm font-semibold text-gray-900">
                          {`$${getOfferNumericPrice(offer).toFixed(2)}`}
                        </td>
                        <td className="py-3">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                            offer.status === 'ACTIVE'
                              ? 'bg-green-100 text-green-700'
                              : 'bg-gray-100 text-gray-700'
                          }`}>
                            {offer.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-8">
                <Package className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p className="text-gray-500">No offers yet</p>
                <Link
                  to="/merchant/products"
                  className="btn-primary mt-4 inline-block"
                >
                  Create Your First Offer
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MerchantDashboard;
