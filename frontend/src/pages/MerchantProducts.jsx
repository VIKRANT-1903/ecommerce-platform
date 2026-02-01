import { useState } from 'react';
import { Link } from 'react-router-dom';
import { productService } from '../services/authService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';
import {
  Package,
  Plus,
  Image,
  Tag,
  FileText,
  Briefcase,
  ChevronLeft,
  Check,
} from 'lucide-react';
import toast from 'react-hot-toast';

const MerchantProducts = () => {
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
        // Reset form
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
    // Navigate to offers page with the product ID
    window.location.href = `/merchant/offers?productId=${createdProduct.id}`;
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <Link
        to="/merchant/dashboard"
        className="inline-flex items-center gap-1 text-amazon-blue hover:text-amazon-orange mb-6"
      >
        <ChevronLeft className="w-4 h-4" />
        Back to Dashboard
      </Link>

      <h1 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
        <Package className="w-6 h-6 text-amazon-orange" />
        Add New Product
      </h1>

      {createdProduct && (
        <div className="mb-6">
          <Alert
            type="success"
            title="Product Created Successfully!"
            message={`Product "${createdProduct.name}" has been created. Now you can create an offer for it.`}
          />
          <div className="mt-4 flex gap-4">
            <button
              onClick={handleCreateOffer}
              className="btn-primary flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Create Offer for This Product
            </button>
            <button
              onClick={() => setCreatedProduct(null)}
              className="btn-secondary"
            >
              Create Another Product
            </button>
          </div>
        </div>
      )}

      {!createdProduct && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Product Form */}
          <div className="lg:col-span-2">
            <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-sm p-6">
              <div className="space-y-6">
                {/* Product Name */}
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

                {/* Category */}
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

                {/* Brand */}
                <div>
                  <label htmlFor="brand" className="block text-sm font-medium text-gray-700 mb-1">
                    Brand <span className="text-gray-400">(optional)</span>
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

                {/* Image URL */}
                <div>
                  <label htmlFor="imageUrl" className="block text-sm font-medium text-gray-700 mb-1">
                    Image URL <span className="text-gray-400">(optional)</span>
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

                {/* Description */}
                <div>
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                    Description <span className="text-gray-400">(optional)</span>
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

                {/* Submit Button */}
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

          {/* Preview */}
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
      )}
    </div>
  );
};

export default MerchantProducts;
