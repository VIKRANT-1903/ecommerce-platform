import { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { productService, offerService } from '../services/authService';
import ProductCard from '../components/common/ProductCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { Search, Filter, X, SlidersHorizontal } from 'lucide-react';

const ProductSearch = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [productOffers, setProductOffers] = useState({});
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState(searchParams.get('name') || '');
  const [selectedCategory, setSelectedCategory] = useState(searchParams.get('category') || '');
  const [showFilters, setShowFilters] = useState(false);

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
  ];

  useEffect(() => {
    const name = searchParams.get('name');
    const category = searchParams.get('category');
    
    if (name || category) {
      fetchProducts(name, category);
    } else {
      // Default search
      fetchProducts('a', null);
    }
  }, [searchParams]);

  const fetchProducts = async (name, category) => {
    setLoading(true);
    try {
      const params = {};
      if (name) params.name = name;
      if (category) params.category = category;

      const response = await productService.search(params);
      if (response.success) {
        setProducts(response.data || []);
        
        // Fetch offers for each product
        const offers = {};
        for (const product of response.data || []) {
          try {
            const offerResponse = await offerService.getByProductId(product.id);
            if (offerResponse.success && offerResponse.data?.length > 0) {
              // Get the best price offer
              const bestOffer = offerResponse.data.reduce((best, current) => 
                current.price < best.price ? current : best
              );
              offers[product.id] = bestOffer;
            }
          } catch (error) {
            console.log(`No offers for product ${product.id}`);
          }
        }
        setProductOffers(offers);
      }
    } catch (error) {
      console.error('Failed to fetch products:', error);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const newParams = new URLSearchParams();
    if (searchQuery) newParams.set('name', searchQuery);
    if (selectedCategory) newParams.set('category', selectedCategory);
    if (!searchQuery && !selectedCategory) {
      newParams.set('name', 'a');
    }
    setSearchParams(newParams);
  };

  const handleCategoryClick = (category) => {
    setSelectedCategory(category);
    const newParams = new URLSearchParams();
    if (searchQuery) newParams.set('name', searchQuery);
    newParams.set('category', category);
    setSearchParams(newParams);
    setShowFilters(false);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedCategory('');
    setSearchParams({ name: 'a' });
  };

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Search Header */}
      <div className="bg-white rounded-lg shadow-sm p-4 mb-6">
        <form onSubmit={handleSearch} className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none z-10" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search products..."
              className="input-field"
              style={{ paddingLeft: '2.5rem' }}
            />
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setShowFilters(!showFilters)}
              className="btn-secondary flex items-center gap-2 md:hidden"
            >
              <SlidersHorizontal className="w-4 h-4" />
              Filters
            </button>
            <button type="submit" className="btn-primary flex items-center gap-2">
              <Search className="w-4 h-4" />
              Search
            </button>
          </div>
        </form>

        {/* Active Filters */}
        {(searchParams.get('name') || searchParams.get('category')) && (
          <div className="flex flex-wrap items-center gap-2 mt-4 pt-4 border-t border-gray-100">
            <span className="text-sm text-gray-500">Filters:</span>
            {searchParams.get('name') && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 rounded-full text-sm">
                Search: "{searchParams.get('name')}"
              </span>
            )}
            {searchParams.get('category') && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-amazon-orange/10 text-amazon-orange rounded-full text-sm">
                {searchParams.get('category')}
                <button
                  onClick={() => {
                    const newParams = new URLSearchParams(searchParams);
                    newParams.delete('category');
                    setSelectedCategory('');
                    if (!newParams.get('name')) newParams.set('name', 'a');
                    setSearchParams(newParams);
                  }}
                  className="hover:bg-amazon-orange/20 rounded-full p-0.5"
                >
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}
            <button
              onClick={clearFilters}
              className="text-sm text-amazon-orange hover:underline"
            >
              Clear all
            </button>
          </div>
        )}
      </div>

      <div className="flex gap-6">
        {/* Sidebar Filters - Desktop */}
        <aside className="hidden md:block w-64 flex-shrink-0">
          <div className="bg-white rounded-lg shadow-sm p-4 sticky top-24">
            <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Filter className="w-4 h-4" />
              Categories
            </h3>
            <ul className="space-y-2">
              {categories.map((category) => (
                <li key={category}>
                  <button
                    onClick={() => handleCategoryClick(category)}
                    className={`w-full text-left px-3 py-2 rounded-md transition-colors ${
                      selectedCategory === category
                        ? 'bg-amazon-orange/10 text-amazon-orange font-medium'
                        : 'text-gray-600 hover:bg-gray-50'
                    }`}
                  >
                    {category}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        </aside>

        {/* Mobile Filters */}
        {showFilters && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-50 md:hidden">
            <div className="absolute right-0 top-0 bottom-0 w-80 bg-white shadow-xl p-4 overflow-y-auto">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-gray-900">Filters</h3>
                <button onClick={() => setShowFilters(false)}>
                  <X className="w-6 h-6 text-gray-500" />
                </button>
              </div>
              <div className="mb-4">
                <h4 className="font-medium text-gray-700 mb-2">Categories</h4>
                <ul className="space-y-2">
                  {categories.map((category) => (
                    <li key={category}>
                      <button
                        onClick={() => handleCategoryClick(category)}
                        className={`w-full text-left px-3 py-2 rounded-md transition-colors ${
                          selectedCategory === category
                            ? 'bg-amazon-orange/10 text-amazon-orange font-medium'
                            : 'text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        {category}
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}

        {/* Products Grid */}
        <div className="flex-1">
          {loading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : products.length > 0 ? (
            <>
              <div className="flex items-center justify-between mb-4">
                <p className="text-gray-600">
                  Showing <span className="font-medium">{products.length}</span> results
                </p>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    offer={productOffers[product.id]}
                  />
                ))}
              </div>
            </>
          ) : (
            <EmptyState
              icon={Search}
              title="No products found"
              message="Try adjusting your search or filter to find what you're looking for."
              actionText="Clear Filters"
              onAction={clearFilters}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductSearch;
