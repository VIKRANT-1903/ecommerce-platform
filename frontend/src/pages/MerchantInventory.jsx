import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { inventoryService } from '../services/ecommService';
import { productService, offerService } from '../services/authService';
import { FiPackage, FiPlus, FiEdit2, FiSave, FiX, FiAlertCircle, FiCheck } from 'react-icons/fi';
import LoadingSpinner from '../components/common/LoadingSpinner';

const MerchantInventory = () => {
  const { merchantProfile } = useAuth();
  const [inventoryList, setInventoryList] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  
  // For creating new inventory
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newInventory, setNewInventory] = useState({
    productId: '',
    availableQty: 0
  });
  const [creating, setCreating] = useState(false);
  
  // For editing existing inventory
  const [editingId, setEditingId] = useState(null);
  const [editQty, setEditQty] = useState(0);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    if (merchantProfile?.merchantId) {
      fetchData();
    }
  }, [merchantProfile]);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch merchant's offers and inventory in parallel
      const [offersResponse, inventoryResponse] = await Promise.all([
        offerService.getMyOffers(),
        inventoryService.getByMerchant(merchantProfile.merchantId)
      ]);

      const offers = offersResponse.data || [];
      setInventoryList(inventoryResponse.data || []);
      
      // Get unique product IDs from offers
      const productIds = [...new Set(offers.map(offer => offer.productId))];
      
      // Fetch product details for each unique productId
      if (productIds.length > 0) {
        const productPromises = productIds.map(id => 
          productService.getById(id).catch(() => null)
        );
        const productResponses = await Promise.all(productPromises);
        const fetchedProducts = productResponses
          .filter(res => res && res.data)
          .map(res => res.data);
        setProducts(fetchedProducts);
      } else {
        setProducts([]);
      }
    } catch (err) {
      console.error('Error fetching data:', err);
      setError('Failed to load inventory data');
    } finally {
      setLoading(false);
    }
  };

  const getProductName = (productId) => {
    const product = products.find(p => p.id === productId);
    return product?.name || productId;
  };

  const handleCreateInventory = async (e) => {
    e.preventDefault();
    if (!newInventory.productId) {
      setError('Please select a product');
      return;
    }

    try {
      setCreating(true);
      setError(null);
      
      await inventoryService.create({
        productId: newInventory.productId,
        merchantId: merchantProfile.merchantId,
        availableQty: parseInt(newInventory.availableQty)
      });

      setSuccess('Inventory created successfully!');
      setShowCreateForm(false);
      setNewInventory({ productId: '', availableQty: 0 });
      fetchData();
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error creating inventory:', err);
      setError(err.response?.data?.message || 'Failed to create inventory');
    } finally {
      setCreating(false);
    }
  };

  const handleStartEdit = (inv) => {
    setEditingId(inv.inventoryId);
    setEditQty(inv.availableQty);
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditQty(0);
  };

  const handleUpdateInventory = async (inv) => {
    try {
      setUpdating(true);
      setError(null);

      await inventoryService.update({
        productId: inv.productId,
        merchantId: inv.merchantId,
        availableQty: parseInt(editQty)
      });

      setSuccess('Inventory updated successfully!');
      setEditingId(null);
      fetchData();
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Error updating inventory:', err);
      setError(err.response?.data?.message || 'Failed to update inventory');
    } finally {
      setUpdating(false);
    }
  };

  // Products that don't have inventory yet
  const productsWithoutInventory = products.filter(
    p => !inventoryList.some(inv => inv.productId === p.id)
  );

  if (!merchantProfile?.merchantId) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
          <FiAlertCircle className="mx-auto text-yellow-500 text-4xl mb-3" />
          <h2 className="text-xl font-semibold text-yellow-800">Merchant Access Required</h2>
          <p className="text-yellow-600 mt-2">You need to be registered as a merchant to manage inventory.</p>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <FiPackage className="text-amazon-orange" />
            Inventory Management
          </h1>
          <p className="text-gray-600 mt-1">Manage stock levels for your products</p>
        </div>
        
        {productsWithoutInventory.length > 0 && (
          <button
            onClick={() => setShowCreateForm(true)}
            className="btn-primary flex items-center gap-2"
          >
            <FiPlus />
            Add Inventory
          </button>
        )}
      </div>

      {/* Alerts */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6 flex items-center gap-2">
          <FiAlertCircle className="text-red-500" />
          <span className="text-red-700">{error}</span>
          <button onClick={() => setError(null)} className="ml-auto text-red-500 hover:text-red-700">
            <FiX />
          </button>
        </div>
      )}

      {success && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6 flex items-center gap-2">
          <FiCheck className="text-green-500" />
          <span className="text-green-700">{success}</span>
        </div>
      )}

      {/* Create Inventory Form */}
      {showCreateForm && (
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-lg font-semibold mb-4">Add New Inventory</h2>
          <form onSubmit={handleCreateInventory} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Select Product
              </label>
              <select
                value={newInventory.productId}
                onChange={(e) => setNewInventory({ ...newInventory, productId: e.target.value })}
                className="input-field"
                required
              >
                <option value="">-- Select a product --</option>
                {productsWithoutInventory.map(product => (
                  <option key={product.id} value={product.id}>
                    {product.name}
                  </option>
                ))}
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Available Quantity
              </label>
              <input
                type="number"
                min="0"
                value={newInventory.availableQty}
                onChange={(e) => setNewInventory({ ...newInventory, availableQty: e.target.value })}
                className="input-field"
                required
              />
            </div>
            
            <div className="flex gap-3">
              <button
                type="submit"
                disabled={creating}
                className="btn-primary flex items-center gap-2"
              >
                {creating ? <LoadingSpinner size="sm" /> : <FiSave />}
                Create Inventory
              </button>
              <button
                type="button"
                onClick={() => setShowCreateForm(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Inventory Table */}
      {inventoryList.length === 0 ? (
        <div className="bg-white rounded-lg shadow-md p-8 text-center">
          <FiPackage className="mx-auto text-gray-400 text-5xl mb-4" />
          <h2 className="text-xl font-semibold text-gray-700">No Inventory Found</h2>
          <p className="text-gray-500 mt-2">
            {products.length > 0 
              ? 'Click "Add Inventory" to set stock levels for your products.'
              : 'Create products first, then add inventory for them.'}
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Product
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Available Qty
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Reserved Qty
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Last Updated
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {inventoryList.map((inv) => (
                <tr key={inv.inventoryId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {getProductName(inv.productId)}
                    </div>
                    <div className="text-xs text-gray-500">{inv.productId}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {editingId === inv.inventoryId ? (
                      <input
                        type="number"
                        min="0"
                        value={editQty}
                        onChange={(e) => setEditQty(e.target.value)}
                        className="w-24 px-2 py-1 border border-gray-300 rounded focus:ring-amazon-orange focus:border-amazon-orange"
                      />
                    ) : (
                      <span className={`text-sm font-semibold ${inv.availableQty > 10 ? 'text-green-600' : inv.availableQty > 0 ? 'text-yellow-600' : 'text-red-600'}`}>
                        {inv.availableQty}
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-gray-600">{inv.reservedQty}</span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {inv.availableQty > 10 ? (
                      <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-800 rounded-full">
                        In Stock
                      </span>
                    ) : inv.availableQty > 0 ? (
                      <span className="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-800 rounded-full">
                        Low Stock
                      </span>
                    ) : (
                      <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-800 rounded-full">
                        Out of Stock
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(inv.updatedAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    {editingId === inv.inventoryId ? (
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => handleUpdateInventory(inv)}
                          disabled={updating}
                          className="text-green-600 hover:text-green-800"
                          title="Save"
                        >
                          {updating ? <LoadingSpinner size="sm" /> : <FiSave size={18} />}
                        </button>
                        <button
                          onClick={handleCancelEdit}
                          className="text-gray-600 hover:text-gray-800"
                          title="Cancel"
                        >
                          <FiX size={18} />
                        </button>
                      </div>
                    ) : (
                      <button
                        onClick={() => handleStartEdit(inv)}
                        className="text-amazon-orange hover:text-amazon-orange-dark"
                        title="Edit"
                      >
                        <FiEdit2 size={18} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="text-sm font-medium text-gray-500">Total Products</h3>
          <p className="text-2xl font-bold text-gray-900">{products.length}</p>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="text-sm font-medium text-gray-500">With Inventory</h3>
          <p className="text-2xl font-bold text-green-600">{inventoryList.length}</p>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="text-sm font-medium text-gray-500">Needs Inventory</h3>
          <p className="text-2xl font-bold text-yellow-600">{productsWithoutInventory.length}</p>
        </div>
      </div>
    </div>
  );
};

export default MerchantInventory;
