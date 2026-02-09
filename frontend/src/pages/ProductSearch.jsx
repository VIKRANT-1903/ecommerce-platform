import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productService, offerService } from '../services/authService';
import ProductCard from '../components/common/ProductCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState'; // Ensure you have this component or remove it
import { Search, Filter, X, SlidersHorizontal } from 'lucide-react';

const ProductSearch = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [productOffers, setProductOffers] = useState({});
  const [loading, setLoading] = useState(true);

  // Initialize state from URL params
  const initialName = searchParams.get('name') || '';
  const initialCategory = searchParams.get('category') || '';

  const [searchQuery, setSearchQuery] = useState(initialName);
  const [selectedCategory, setSelectedCategory] = useState(initialCategory);
  const [showFilters, setShowFilters] = useState(false);

  const categories = [
    'Electronics', 'Clothing', 'Books', 'Home', 'Sports',
    'Toys', 'Beauty', 'Automotive', 'Garden', 'Office',
  ];

  // Sync local state when URL changes (e.g. back button)
  useEffect(() => {
    const name = searchParams.get('name') || '';
    const category = searchParams.get('category') || '';
    setSearchQuery(name);
    setSelectedCategory(category);

    // Fetch Data
    fetchProducts(name, category);
  }, [searchParams]);

  const fetchProducts = async (name, category) => {
    setLoading(true);
    // Reset offers when searching again to avoid showing old prices
    setProductOffers({});

    try {
      const params = {};
      if (name) params.name = name;
      if (category) params.category = category;

      // 1. Fetch Products
      const response = await productService.search(params);

      if (response.success) {
        const foundProducts = response.data || [];
        setProducts(foundProducts);

        // --- OPTIMIZATION START: BULK FETCH ---
        const productIds = foundProducts.map(p => p.id);

        if (productIds.length > 0) {
          try {
            // One single API call for all prices
            const offerResponse = await offerService.getBulkOffers(productIds);

            if (offerResponse.success && offerResponse.data) {
              const bulkData = offerResponse.data;
              const bestOffersMap = {};

              // Find best price for each product
              Object.keys(bulkData).forEach(productId => {
                const offers = bulkData[productId];
                if (offers && offers.length > 0) {
                  // Reduce to find the cheapest offer
                  const bestOffer = offers.reduce((min, cur) => {
                    const priceMin = typeof min.price === 'number' ? min.price : Infinity;
                    const priceCur = typeof cur.price === 'number' ? cur.price : 0;
                    return priceCur < priceMin ? cur : min;
                  }, offers[0]); // Start with first offer

                  bestOffersMap[productId] = bestOffer;
                }
              });

              setProductOffers(bestOffersMap);
            }
          } catch (offerError) {
            console.error('Failed to fetch bulk offers:', offerError);
          }
        }
        // --- OPTIMIZATION END ---
      } else {
        setProducts([]);
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
    updateParams(searchQuery, selectedCategory);
  };

  const handleCategoryClick = (category) => {
    // If clicking the same category, toggle it off
    const newCategory = selectedCategory === category ? '' : category;
    setSelectedCategory(newCategory);
    updateParams(searchQuery, newCategory);
    setShowFilters(false);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedCategory('');
    setSearchParams({}); // Clears URL
  };

  // Helper to update URL params cleanly
  const updateParams = (name, category) => {
    const newParams = new URLSearchParams();
    if (name) newParams.set('name', name);
    if (category) newParams.set('category', category);
    setSearchParams(newParams);
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
              className="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-amazon-orange focus:outline-none"
            />
          </div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setShowFilters(!showFilters)}
              className="md:hidden px-4 py-2 border rounded-lg flex items-center gap-2 hover:bg-gray-50"
            >
              <SlidersHorizontal className="w-4 h-4" />
              Filters
            </button>
            <button type="submit" className="btn-primary flex items-center gap-2 px-6 py-2">
              <Search className="w-4 h-4" />
              Search
            </button>
          </div>
        </form>

        {/* Active Filters Display */}
        {(searchParams.get('name') || searchParams.get('category')) && (
          <div className="flex flex-wrap items-center gap-2 mt-4 pt-4 border-t border-gray-100">
            <span className="text-sm text-gray-500">Active Filters:</span>

            {searchParams.get('name') && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 rounded-full text-sm">
                "{searchParams.get('name')}"
                <button onClick={() => updateParams('', selectedCategory)} className="hover:text-red-500 ml-1">
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}

            {searchParams.get('category') && (
              <span className="inline-flex items-center gap-1 px-3 py-1 bg-orange-50 text-amazon-orange rounded-full text-sm border border-orange-100">
                {searchParams.get('category')}
                <button onClick={() => updateParams(searchQuery, '')} className="hover:text-red-500 ml-1">
                  <X className="w-3 h-3" />
                </button>
              </span>
            )}

            <button
              onClick={clearFilters}
              className="text-sm text-gray-500 hover:text-amazon-orange ml-2 underline"
            >
              Clear all
            </button>
          </div>
        )}
      </div>

      <div className="flex flex-col md:flex-row gap-6">
        {/* Sidebar Filters - Desktop */}
        <aside className="hidden md:block w-64 flex-shrink-0">
          <div className="bg-white rounded-lg shadow-sm p-4 sticky top-24">
            <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <Filter className="w-4 h-4" />
              Categories
            </h3>
            <ul className="space-y-1">
              {categories.map((category) => (
                <li key={category}>
                  <button
                    onClick={() => handleCategoryClick(category)}
                    className={`w-full text-left px-3 py-2 rounded-md transition-colors text-sm ${
                      selectedCategory === category
                        ? 'bg-orange-50 text-amazon-orange font-medium'
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

        {/* Mobile Filters Overlay */}
        {showFilters && (
          <div className="fixed inset-0 bg-black bg-opacity-50 z-50 md:hidden flex justify-end">
            <div className="w-80 bg-white h-full shadow-xl p-4 overflow-y-auto">
              <div className="flex items-center justify-between mb-6">
                <h3 className="font-semibold text-gray-900">Filters</h3>
                <button onClick={() => setShowFilters(false)}>
                  <X className="w-6 h-6 text-gray-500" />
                </button>
              </div>
              <div>
                <h4 className="font-medium text-gray-700 mb-2">Categories</h4>
                <ul className="space-y-2">
                  {categories.map((category) => (
                    <li key={category}>
                      <button
                        onClick={() => handleCategoryClick(category)}
                        className={`w-full text-left px-3 py-2 rounded-md transition-colors ${
                          selectedCategory === category
                            ? 'bg-orange-50 text-amazon-orange font-medium'
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
            <div className="flex justify-center py-20">
              <LoadingSpinner size="lg" />
            </div>
          ) : products.length > 0 ? (
            <>
              <div className="mb-4">
                <p className="text-gray-600">
                  Found <span className="font-bold text-gray-900">{products.length}</span> results
                </p>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    offer={productOffers[product.id]} // Passing the bulk-fetched offer
                  />
                ))}
              </div>
            </>
          ) : (
            <div className="bg-white rounded-lg shadow-sm p-12 text-center">
              <Search className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-xl font-medium text-gray-900 mb-2">No products found</h3>
              <p className="text-gray-500 mb-6">
                We couldn't find any products matching your search. Try checking for typos or using different keywords.
              </p>
              <button onClick={clearFilters} className="btn-primary">
                Clear Filters
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductSearch;