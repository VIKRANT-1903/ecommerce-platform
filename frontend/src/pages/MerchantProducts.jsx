import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productService, offerService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';
import EmptyState from '../components/common/EmptyState';
import {
  Package,
  Plus,
  Image,
  Tag,
  FileText,
  Briefcase,
  ChevronLeft,
  Check,
  DollarSign,
  Eye,
  X,
  Layers,
} from 'lucide-react';
import toast from 'react-hot-toast';

const MerchantProducts = () => {
  // View mode: 'list' or 'add'
  const [viewMode, setViewMode] = useState('list');
  
  // Products list state
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(true);
  
  // Add product form state
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: '',
    brand: '',
    imageUrl: '',
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [createdProduct, setCreatedProduct] = useState(null);

  const categories = [
    'Electronics',
    'Clothing',
    'Books',
    'Home',
    'Sports',
    'Toys',
    'Beauty',
    'Automotive',
    'Garden',
    'Office',
    'Food & Grocery',
    'Health',
    'Pet Supplies',
    'Baby',
    'Other',
  ];

  // Fetch merchant's products via offers
  useEffect(() => {
    fetchMyProducts();
  }, []);

  const fetchMyProducts = async () => {
    setLoadingProducts(true);
    try {
      // Get all offers for this merchant
      const offersResponse = await offerService.getMyOffers();
      if (offersResponse.success && offersResponse.data) {
        // Get unique product IDs from offers
        const productIds = [...new Set(offersResponse.data.map(offer => offer.productId))];
        
        // Fetch product details for each unique product
        const productPromises = productIds.map(async (productId) => {
          try {
            const productResponse = await productService.getById(productId);
            if (productResponse.success) {
              // Find offers for this product
              const productOffers = offersResponse.data.filter(o => o.productId === productId);
              return {
                ...productResponse.data,
                offers: productOffers,
                lowestPrice: Math.min(...productOffers.map(o => o.price)),
                totalOffers: productOffers.length,
              };
            }
            return null;
          } catch (error) {
            console.log(`Failed to fetch product ${productId}`);
            return null;
          }
        });

        const fetchedProducts = (await Promise.all(productPromises)).filter(p => p !== null);
        setProducts(fetchedProducts);
      }
    } catch (error) {
      console.error('Failed to fetch products:', error);
      toast.error('Failed to load products');
    } finally {
      setLoadingProducts(false);
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.name.trim()) {
      newErrors.name = 'Product name is required';
    } else if (formData.name.length > 255) {
      newErrors.name = 'Product name must be less than 255 characters';
    }
    if (!formData.category) {
      newErrors.category = 'Category is required';
    }
    if (formData.description && formData.description.length > 5000) {
      newErrors.description = 'Description must be less than 5000 characters';
    }
    if (formData.brand && formData.brand.length > 255) {
      newErrors.brand = 'Brand must be less than 255 characters';
    }
    if (formData.imageUrl && formData.imageUrl.length > 2000) {
      newErrors.imageUrl = 'Image URL must be less than 2000 characters';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      const response = await productService.create({
        name: formData.name,
        description: formData.description || undefined,
        category: formData.category,
        brand: formData.brand || undefined,
        imageUrl: formData.imageUrl || undefined,
      });

      if (response.success) {
        setCreatedProduct(response.data);
        toast.success('Product created successfully!');
        setFormData({
          name: '',
          description: '',
          category: '',
          brand: '',
          imageUrl: '',
        });
      }
    } catch (error) {
      const message = error.response?.data?.error?.detail || 'Failed to create product';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateOffer = () => {
    window.location.href = `/merchant/offers?productId=${createdProduct.id}`;
  };

  const handleAddAnother = () => {
    setCreatedProduct(null);
  };

  const handleBackToList = () => {
    setCreatedProduct(null);
    setViewMode('list');
    fetchMyProducts();
  };

  // Render Products List
  const renderProductsList = () => {
    if (loadingProducts) {
      return (
        <div className="flex items-center justify-center py-12">
          <LoadingSpinner />
        </div>
      );
    }

    if (products.length === 0) {
      return (
        <div className="text-center py-12">
          <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Products with Offers Yet</h3>
          <p className="text-gray-500 mb-6 max-w-md mx-auto">
            Products appear here after you create at least one offer for them.
          </p>
          
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              onClick={() => setViewMode('add')}
              className="btn-primary flex items-center justify-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Add New Product
            </button>
            <Link
              to="/merchant/offers"
              className="btn-secondary flex items-center justify-center gap-2"
            >
              <DollarSign className="w-4 h-4" />
              Create Offer for Existing Product
            </Link>
          </div>
          
          <p className="text-sm text-gray-400 mt-6">
            Already created a product? Go to <Link to="/merchant/offers" className="text-amazon-blue hover:underline">My Offers</Link> to search and add pricing.
          </p>
        </div>
      );
    }

    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product) => (
          <div key={product.id} className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
            <div className="aspect-video bg-gray-100 flex items-center justify-center">
              {product.imageUrl ? (
                <img
                  src={product.imageUrl}
                  alt={product.name}
                  className="w-full h-full object-cover"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.style.display = 'none';
                  }}
                />
              ) : (
                <Image className="w-12 h-12 text-gray-300" />
              )}
            </div>

            <div className="p-4">
              <div className="flex items-start justify-between gap-2 mb-2">
                <span className="text-xs font-medium text-amazon-orange bg-amazon-orange/10 px-2 py-1 rounded">
                  {product.category}
                </span>
                <span className="text-xs text-gray-500 flex items-center gap-1">
                  <Layers className="w-3 h-3" />
                  {product.totalOffers} offer{product.totalOffers !== 1 ? 's' : ''}
                </span>
              </div>
              
              {product.brand && (
                <p className="text-xs text-gray-500 uppercase tracking-wide">{product.brand}</p>
              )}
              
              <h3 className="font-medium text-gray-900 line-clamp-2 mt-1">{product.name}</h3>
              
              <div className="mt-3 flex items-center justify-between">
                <div>
                  <p className="text-xs text-gray-500">Starting from</p>
                  <p className="text-lg font-bold text-green-600">${product.lowestPrice.toFixed(2)}</p>
                </div>
                <div className="flex gap-2">
                  <Link
                    to={`/product/${product.id}`}
                    className="p-2 text-gray-500 hover:text-amazon-blue hover:bg-gray-100 rounded-lg transition-colors"
                    title="View Product"
                  >
                    <Eye className="w-4 h-4" />
                  </Link>
                  <Link
                    to={`/merchant/offers?productId=${product.id}`}
                    className="p-2 text-gray-500 hover:text-amazon-orange hover:bg-gray-100 rounded-lg transition-colors"
                    title="Manage Offers"
                  >
                    <DollarSign className="w-4 h-4" />
                  </Link>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  // Render Add Product Form
  const renderAddProductForm = () => {
    if (createdProduct) {
      return (
        <div className="max-w-2xl mx-auto">
          <Alert
            type="success"
            title="Product Created Successfully!"
            message={`Product "${createdProduct.name}" has been created. Now you need to create an offer with pricing to start selling.`}
          />
          <div className="mt-6 flex flex-col sm:flex-row gap-4">
            <button
              onClick={handleCreateOffer}
              className="btn-primary flex items-center justify-center gap-2 flex-1"
            >
              <DollarSign className="w-4 h-4" />
              Create Offer for This Product
            </button>
            <button
              onClick={handleAddAnother}
              className="btn-secondary flex items-center justify-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Add Another Product
            </button>
          </div>
          
          {/* Important note about finding the product later */}
          <div className="mt-6 bg-amber-50 border border-amber-200 rounded-lg p-4">
            <p className="text-sm text-amber-800">
              <strong>💡 Tip:</strong> If you don't create an offer now, you can find this product later by:
            </p>
            <ul className="mt-2 text-sm text-amber-700 list-disc list-inside space-y-1">
              <li>Going to <strong>My Offers</strong> → <strong>Create Offer</strong> → Search for "<strong>{createdProduct.name}</strong>"</li>
              <li>Product ID: <code className="bg-amber-100 px-1 rounded">{createdProduct.id}</code></li>
            </ul>
          </div>
          
          <button
            onClick={handleBackToList}
            className="mt-4 text-amazon-blue hover:text-amazon-orange text-sm flex items-center gap-1"
          >
            <ChevronLeft className="w-4 h-4" />
            Back to My Products
          </button>
        </div>
      );
    }

    return (
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm p-6">
            <div className="space-y-6">
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                  Product Name <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Package className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    className={`input-field ${errors.name ? 'border-red-500' : ''}`}
                    style={{ paddingLeft: '2.5rem' }}
                    placeholder="e.g., Wireless Bluetooth Headphones"
                  />
                </div>
                {errors.name && <p className="mt-1 text-sm text-red-500">{errors.name}</p>}
              </div>

              <div>
                <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-1">
                  Category <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Tag className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                  <select
                    id="category"
                    name="category"
                    value={formData.category}
                    onChange={handleChange}
                    className={`input-field ${errors.category ? 'border-red-500' : ''}`}
                    style={{ paddingLeft: '2.5rem' }}
                  >
                    <option value="">Select a category</option>
                    {categories.map(cat => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                </div>
                {errors.category && <p className="mt-1 text-sm text-red-500">{errors.category}</p>}
              </div>

              <div>
                <label htmlFor="brand" className="block text-sm font-medium text-gray-700 mb-1">
                  Brand <span className="text-gray-400 text-xs">(optional)</span>
                </label>
                <div className="relative">
                  <Briefcase className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                  <input
                    type="text"
                    id="brand"
                    name="brand"
                    value={formData.brand}
                    onChange={handleChange}
                    className={`input-field ${errors.brand ? 'border-red-500' : ''}`}
                    style={{ paddingLeft: '2.5rem' }}
                    placeholder="e.g., Sony"
                  />
                </div>
                {errors.brand && <p className="mt-1 text-sm text-red-500">{errors.brand}</p>}
              </div>

              <div>
                <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700 mb-1">
                  Image URL <span className="text-gray-400 text-xs">(optional)</span>
                </label>
                <div className="relative">
                  <Image className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                  <input
                    type="url"
                    id="imageUrl"
                    name="imageUrl"
                    value={formData.imageUrl}
                    onChange={handleChange}
                    className={`input-field ${errors.imageUrl ? 'border-red-500' : ''}`}
                    style={{ paddingLeft: '2.5rem' }}
                    placeholder="https://example.com/image.jpg"
                  />
                </div>
                {errors.imageUrl && <p className="mt-1 text-sm text-red-500">{errors.imageUrl}</p>}
              </div>

              <div>
                <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                  Description <span className="text-gray-400 text-xs">(optional)</span>
                </label>
                <div className="relative">
                  <FileText className="absolute left-3 top-3 w-5 h-5 text-gray-400 pointer-events-none z-10" />
                  <textarea
                    id="description"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={5}
                    className={`input-field ${errors.description ? 'border-red-500' : ''}`}
                    style={{ paddingLeft: '2.5rem' }}
                    placeholder="Describe your product..."
                  />
                </div>
                <div className="flex justify-between mt-1">
                  {errors.description && <p className="text-sm text-red-500">{errors.description}</p>}
                  <p className="text-sm text-gray-400 ml-auto">
                    {formData.description.length}/5000
                  </p>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full btn-primary py-3 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <>
                    <LoadingSpinner size="sm" />
                    Creating Product...
                  </>
                ) : (
                  <>
                    <Plus className="w-5 h-5" />
                    Create Product
                  </>
                )}
              </button>
            </div>
          </form>
        </div>

        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg shadow-sm p-6 sticky top-24">
            <h3 className="font-semibold text-gray-900 mb-4">Preview</h3>
            
            <div className="border border-gray-200 rounded-lg overflow-hidden">
              <div className="aspect-square bg-gray-100 flex items-center justify-center">
                {formData.imageUrl ? (
                  <img
                    src={formData.imageUrl}
                    alt="Preview"
                    className="w-full h-full object-cover"
                    onError={(e) => {
                      e.target.style.display = 'none';
                    }}
                  />
                ) : (
                  <Image className="w-16 h-16 text-gray-300" />
                )}
              </div>
              <div className="p-4">
                <p className="text-xs text-gray-500 uppercase mb-1">
                  {formData.brand || 'Brand'}
                </p>
                <h4 className="font-medium text-gray-900 line-clamp-2">
                  {formData.name || 'Product Name'}
                </h4>
                <p className="text-sm text-gray-500 mt-1">
                  {formData.category || 'Category'}
                </p>
              </div>
            </div>

            <div className="mt-6 space-y-3">
              <h4 className="font-medium text-gray-900">Quick Tips</h4>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-start gap-2">
                  <Check className="w-4 h-4 text-green-500 flex-shrink-0 mt-0.5" />
                  Use clear, descriptive product names
                </li>
                <li className="flex items-start gap-2">
                  <Check className="w-4 h-4 text-green-500 flex-shrink-0 mt-0.5" />
                  Choose the most relevant category
                </li>
                <li className="flex items-start gap-2">
                  <Check className="w-4 h-4 text-green-500 flex-shrink-0 mt-0.5" />
                  Add a high-quality product image
                </li>
                <li className="flex items-start gap-2">
                  <Check className="w-4 h-4 text-green-500 flex-shrink-0 mt-0.5" />
                  Write detailed descriptions
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <Link
            to="/merchant/dashboard"
            className="inline-flex items-center gap-1 text-amazon-blue hover:text-amazon-orange mb-2 text-sm"
          >
            <ChevronLeft className="w-4 h-4" />
            Back to Dashboard
          </Link>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Package className="w-6 h-6 text-amazon-orange" />
            {viewMode === 'list' ? 'My Products' : 'Add New Product'}
          </h1>
        </div>

        {viewMode === 'list' && (
          <button
            onClick={() => setViewMode('add')}
            className="btn-primary flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            Add New Product
          </button>
        )}
        {viewMode === 'add' && !createdProduct && (
          <button
            onClick={() => setViewMode('list')}
            className="btn-secondary flex items-center gap-2"
          >
            <X className="w-4 h-4" />
            Cancel
          </button>
        )}
      </div>

      {viewMode === 'list' && products.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <p className="text-sm text-blue-800">
            <strong>Note:</strong> Products appear here after you create at least one offer for them. 
            To sell a product, you need both a product and a price offer.
          </p>
        </div>
      )}

      {viewMode === 'list' ? renderProductsList() : renderAddProductForm()}
    </div>
  );
};

export default MerchantProducts;
