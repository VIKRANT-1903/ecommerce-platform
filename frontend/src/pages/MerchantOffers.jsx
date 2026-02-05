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
import { Link, useSearchParams } from 'react-router-dom';
import { offerService, productService } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import Alert from '../components/common/Alert';
import {
  DollarSign,
  Plus,
  Trash2,
  ChevronLeft,
  Tag,
  Package,
  Search,
  Info,
} from 'lucide-react';
import toast from 'react-hot-toast';

const MerchantOffers = () => {
  const [searchParams] = useSearchParams();
  const prefilledProductId = searchParams.get('productId');
  const { merchantProfile } = useAuth();
  
  const [offers, setOffers] = useState([]);
  const [productDetails, setProductDetails] = useState({});
  const [myProductIds, setMyProductIds] = useState(new Set()); // Products this merchant has created/has offers for
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(!!prefilledProductId);
  
  const [formData, setFormData] = useState({
    productId: prefilledProductId || '',
    price: '',
    currency: 'USD',
  });
  const [errors, setErrors] = useState({});
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  useEffect(() => {
    fetchOffers();
  }, []);

  useEffect(() => {
    if (prefilledProductId) {
      fetchProductById(prefilledProductId);
      // If coming from product creation, this is merchant's own product
      setMyProductIds(prev => new Set([...prev, prefilledProductId]));
    }
  }, [prefilledProductId]);

  // Debounced search as user types
  useEffect(() => {
    if (searchQuery.trim().length < 2) {
      setSearchResults([]);
      setHasSearched(false);
      return;
    }

    const debounceTimer = setTimeout(() => {
      handleSearchProducts();
    }, 400);

    return () => clearTimeout(debounceTimer);
  }, [searchQuery]);

  const fetchOffers = async () => {
    try {
      const response = await offerService.getMyOffers();
      if (response.success) {
        setOffers(response.data || []);
        
        // Build set of product IDs this merchant has offers for
        const productIds = new Set((response.data || []).map(o => o.productId));
        setMyProductIds(productIds);
        
        // Fetch product details
        for (const offer of response.data || []) {
          if (!productDetails[offer.productId]) {
            fetchProductById(offer.productId);
          }
        }
      }
    } catch (error) {
      console.error('Failed to fetch offers:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchProductById = async (productId) => {
    try {
      const response = await productService.getById(productId);
      if (response.success) {
        setProductDetails(prev => ({
          ...prev,
          [productId]: response.data
        }));
      }
    } catch (error) {
      console.error(`Failed to fetch product ${productId}`);
    }
  };

  const handleSearchProducts = async () => {
    if (!searchQuery.trim()) return;
    
    setSearching(true);
    try {
      const response = await productService.search({ name: searchQuery });
      if (response.success) {
        // Show all products from search - like Amazon, any seller can add an offer
        // The product will be linked to this merchant once they create an offer
        setSearchResults(response.data || []);
        setHasSearched(true);
      }
    } catch (error) {
      console.error('Search failed:', error);
      setSearchResults([]);
    } finally {
      setSearching(false);
    }
  };

  const handleSelectProduct = (product) => {
    setFormData(prev => ({ ...prev, productId: product.id }));
    setProductDetails(prev => ({ ...prev, [product.id]: product }));
    setSearchResults([]);
    setSearchQuery('');
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.productId.trim()) {
      newErrors.productId = 'Product is required';
    }
    if (!formData.price || parseFloat(formData.price) <= 0) {
      newErrors.price = 'Price must be a positive number';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setCreating(true);
    try {
      const response = await offerService.create({
        productId: formData.productId,
        price: parseFloat(formData.price),
        currency: formData.currency,
      });

      if (response.success) {
        toast.success('Offer created successfully!');
        setFormData({ productId: '', price: '', currency: 'USD' });
        setShowCreateForm(false);
        fetchOffers();
      }
    } catch (error) {
      const message = error.response?.data?.error?.detail || 'Failed to create offer';
      toast.error(message);
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (offerId) => {
    if (!confirm('Are you sure you want to delete this offer?')) return;

    setDeleting(offerId);
    try {
      await offerService.delete(offerId);
      toast.success('Offer deleted');
      setOffers(prev => prev.filter(o => o.offerId !== offerId));
    } catch (error) {
      toast.error('Failed to delete offer');
    } finally {
      setDeleting(null);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <Link
        to="/merchant/dashboard"
        className="inline-flex items-center gap-1 text-amazon-blue hover:text-amazon-orange mb-6"
      >
        <ChevronLeft className="w-4 h-4" />
        Back to Dashboard
      </Link>

      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <Tag className="w-6 h-6 text-amazon-orange" />
          My Offers
        </h1>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn-primary flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          {showCreateForm ? 'Cancel' : 'Create Offer'}
        </button>
      </div>

      {/* Create Form */}
      {showCreateForm && (
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Create New Offer</h2>
          
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Product Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Select Product <span className="text-red-500">*</span>
              </label>
              
              {formData.productId && productDetails[formData.productId] ? (
                <div className="flex items-center gap-4 p-3 bg-green-50 rounded-lg border border-green-200">
                  <div className="w-12 h-12 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                    {productDetails[formData.productId]?.imageUrl ? (
                      <img
                        src={productDetails[formData.productId].imageUrl}
                        alt=""
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center">
                        <Package className="w-6 h-6 text-gray-300" />
                      </div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-900 truncate">
                      {productDetails[formData.productId]?.name}
                    </p>
                    <p className="text-sm text-gray-500">
                      {productDetails[formData.productId]?.category}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setFormData(prev => ({ ...prev, productId: '' }))}
                    className="text-gray-400 hover:text-red-500 p-1"
                  >
                    ×
                  </button>
                </div>
              ) : (
                <div className="space-y-3">
                  {/* Info about creating offers */}
                  <div className="flex items-start gap-3 p-4 bg-blue-50 rounded-lg border border-blue-100">
                    <Info className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                    <div className="text-sm">
                      <p className="text-blue-800 font-medium">Search for a product</p>
                      <p className="text-blue-600 mt-1">
                        Search to find products and add your offer. Multiple sellers can offer the same product at different prices.
                      </p>
                    </div>
                  </div>

                  {/* Search for product */}
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                    <input
                      type="text"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                      className="input-field"
                      style={{ paddingLeft: '2.5rem' }}
                      placeholder="Start typing product name..."
                    />
                    {searching && (
                      <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <LoadingSpinner size="sm" />
                      </div>
                    )}
                  </div>
                  
                  {/* Search Results */}
                  {searchResults.length > 0 && (
                    <div className="border border-gray-200 rounded-lg max-h-60 overflow-y-auto">
                      {searchResults.map(product => (
                        <button
                          key={product.id}
                          type="button"
                          onClick={() => handleSelectProduct(product)}
                          className="w-full flex items-center gap-3 p-3 hover:bg-amazon-orange/10 border-b border-gray-100 last:border-b-0 text-left transition-colors"
                        >
                          <div className="w-10 h-10 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                            {product.imageUrl ? (
                              <img
                                src={product.imageUrl}
                                alt=""
                                className="w-full h-full object-cover"
                              />
                            ) : (
                              <div className="w-full h-full flex items-center justify-center">
                                <Package className="w-5 h-5 text-gray-300" />
                              </div>
                            )}
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="font-medium text-gray-900 truncate">{product.name}</p>
                            <p className="text-sm text-gray-500">{product.category}</p>
                          </div>
                        </button>
                      ))}
                    </div>
                  )}

                  {/* No results message */}
                  {hasSearched && searchResults.length === 0 && searchQuery.trim().length >= 2 && !searching && (
                    <div className="text-center py-4 bg-gray-50 rounded-lg border border-gray-200">
                      <Package className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                      <p className="text-sm text-gray-600">No products found matching "{searchQuery}"</p>
                      <p className="text-xs text-gray-400 mt-1">
                        Product not in catalog? Create it first!
                      </p>
                      <Link 
                        to="/merchant/products" 
                        className="inline-block mt-2 text-sm text-amazon-blue hover:text-amazon-orange"
                      >
                        Create New Product →
                      </Link>
                    </div>
                  )}
                </div>
              )}
              {errors.productId && <p className="mt-1 text-sm text-red-500">{errors.productId}</p>}
            </div>

            {/* Price */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Price <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={formData.price}
                    onChange={(e) => setFormData(prev => ({ ...prev, price: e.target.value }))}
                    className={`input-field ${errors.price ? 'border-red-500' : ''}`}
                    style={{ paddingLeft: '2.5rem' }}
                    placeholder="0.00"
                  />
                </div>
                {errors.price && <p className="mt-1 text-sm text-red-500">{errors.price}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Currency
                </label>
                <select
                  value={formData.currency}
                  onChange={(e) => setFormData(prev => ({ ...prev, currency: e.target.value }))}
                  className="input-field"
                >
                  <option value="USD">USD</option>
                  <option value="EUR">EUR</option>
                  <option value="GBP">GBP</option>
                  <option value="INR">INR</option>
                </select>
              </div>
            </div>

            <button
              type="submit"
              disabled={creating}
              className="btn-primary py-2 px-6 flex items-center gap-2 disabled:opacity-50"
            >
              {creating ? <LoadingSpinner size="sm" /> : <Plus className="w-4 h-4" />}
              Create Offer
            </button>
          </form>
        </div>
      )}

      {/* Offers List */}
      {offers.length > 0 ? (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="text-left py-4 px-6 text-sm font-medium text-gray-500">
                    Product
                  </th>
                  <th className="text-left py-4 px-6 text-sm font-medium text-gray-500">
                    Price
                  </th>
                  <th className="text-left py-4 px-6 text-sm font-medium text-gray-500">
                    Status
                  </th>
                  <th className="text-left py-4 px-6 text-sm font-medium text-gray-500">
                    Created
                  </th>
                  <th className="text-right py-4 px-6 text-sm font-medium text-gray-500">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {offers.map((offer) => {
                  const product = productDetails[offer.productId];
                  return (
                    <tr key={offer.offerId} className={deleting === offer.offerId ? 'opacity-50' : ''}>
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="w-12 h-12 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
                            <img
                              src={product?.imageUrl || 'https://via.placeholder.com/48'}
                              alt=""
                              className="w-full h-full object-cover"
                            />
                          </div>
                          <div>
                            <p className="font-medium text-gray-900 line-clamp-1">
                              {product?.name || 'Loading...'}
                            </p>
                            <p className="text-xs text-gray-500 font-mono">
                              ID: {offer.productId.substring(0, 12)}...
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="py-4 px-6">
                        <span className="font-bold text-gray-900">
                          {`$${getOfferNumericPrice(offer).toFixed(2)}`}
                        </span>
                        <span className="text-sm text-gray-500 ml-1">
                          {offer.currency}
                        </span>
                      </td>
                      <td className="py-4 px-6">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                          offer.status === 'ACTIVE'
                            ? 'bg-green-100 text-green-700'
                            : 'bg-gray-100 text-gray-700'
                        }`}>
                          {offer.status}
                        </span>
                      </td>
                      <td className="py-4 px-6 text-sm text-gray-500">
                        {new Date(offer.createdAt).toLocaleDateString()}
                      </td>
                      <td className="py-4 px-6 text-right">
                        <button
                          onClick={() => handleDelete(offer.offerId)}
                          disabled={deleting === offer.offerId}
                          className="text-red-600 hover:text-red-700 p-2 rounded-lg hover:bg-red-50"
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <EmptyState
          icon={Package}
          title="No offers yet"
          message="Create your first offer to start selling products on ShopZone."
          actionText="Create Offer"
          onAction={() => setShowCreateForm(true)}
        />
      )}
    </div>
  );
};

export default MerchantOffers;
