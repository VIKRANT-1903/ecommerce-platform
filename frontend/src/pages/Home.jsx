import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productService, offerService } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import ProductCard from '../components/common/ProductCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { ShoppingBag, Truck, Shield, HeadphonesIcon, ArrowRight, Sparkles, Package, Store } from 'lucide-react';

const Home = () => {
  const { user, merchantProfile } = useAuth();
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [productOffers, setProductOffers] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFeaturedProducts();
  }, []);

  const fetchFeaturedProducts = async () => {
    try {
      setLoading(true);
      let allProducts = [];

      // --- [CHANGE 1] Use getAll() instead of crashing search ---
      try {
        const response = await productService.getAll();
        if (response.success && response.data) {
          allProducts = response.data;
        }
      } catch (error) {
        console.warn('Failed to fetch all products, attempting fallback');
      }

      // Fallback strategy if main list fails
      if (allProducts.length === 0) {
        try {
          const fallback = await productService.search({ name: 'a' });
          if (fallback.success) allProducts = fallback.data || [];
        } catch (e) {
          console.error("Fallback failed", e);
        }
      }

      // Slice to 12 items for the homepage
      const displayProducts = allProducts.slice(0, 12);
      setFeaturedProducts(displayProducts);

      // --- [CHANGE 2] Use Bulk Fetch instead of Loop ---
      const productIds = displayProducts.map(p => p.id);
      
      if (productIds.length > 0) {
        // One single call for all 12 prices
        const offerResponse = await offerService.getBulkOffers(productIds);
        
        if (offerResponse.success) {
          const bulkData = offerResponse.data || {};
          const bestOffersMap = {};

          // Process the bulk data to find the lowest price for each product
          Object.keys(bulkData).forEach(productId => {
            const offers = bulkData[productId];
            if (offers && offers.length > 0) {
              // Find the offer with the lowest price
              const bestOffer = offers.reduce((min, cur) => 
                cur.price < min.price ? cur : min
              );
              bestOffersMap[productId] = bestOffer;
            }
          });
          
          setProductOffers(bestOffersMap);
        }
      }
    } catch (error) {
      console.error('Failed to fetch home data:', error);
    } finally {
      setLoading(false);
    }
  };

  const categories = [
    { name: 'Electronics', icon: '🎧', color: 'from-blue-500 to-blue-600' },
    { name: 'Clothing', icon: '👕', color: 'from-pink-500 to-rose-600' },
    { name: 'Books', icon: '📚', color: 'from-amber-500 to-orange-600' },
    { name: 'Home', icon: '🏠', color: 'from-green-500 to-emerald-600' },
    { name: 'Sports', icon: '⚽', color: 'from-purple-500 to-violet-600' },
    { name: 'Toys', icon: '🧸', color: 'from-red-500 to-rose-600' },
  ];

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-r from-amazon-dark to-amazon-light-dark text-white overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute inset-0" style={{
            backgroundImage: 'radial-gradient(circle at 25% 25%, #FF9900 0%, transparent 50%)',
          }} />
        </div>
        <div className="container mx-auto px-4 py-16 md:py-24 relative">
          <div className="max-w-2xl">
            <div className="flex items-center gap-2 mb-4">
              <Sparkles className="w-5 h-5 text-amazon-orange" />
              <span className="text-amazon-orange font-medium">
                {user ? `Welcome back, ${user.firstName || 'Shopper'}!` : 'Welcome to ShopZone'}
              </span>
            </div>
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold mb-6">
              Discover Amazing <span className="text-amazon-orange">Deals</span> Every Day
            </h1>
            <p className="text-lg text-gray-300 mb-8">
              Shop millions of products from trusted sellers. Great prices, fast delivery, and excellent customer service.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link
                to="/search?name=a"
                className="btn-primary flex items-center gap-2 text-lg px-8 py-3"
              >
                Start Shopping
                <ArrowRight className="w-5 h-5" />
              </Link>
              {!user ? (
                <Link
                  to="/register"
                  className="btn-secondary flex items-center gap-2 text-lg px-8 py-3"
                >
                  Join Now
                </Link>
              ) : (
                <div className="flex items-center gap-3">
                  <Link
                    to="/orders"
                    className="btn-secondary flex items-center gap-2 px-6 py-3"
                  >
                    <Package className="w-5 h-5" />
                    Your Orders
                  </Link>
                  {!merchantProfile && (
                    <Link
                      to="/register?type=merchant"
                      className="flex items-center gap-2 px-6 py-3 bg-white/10 hover:bg-white/20 rounded-lg transition-colors text-white"
                    >
                      <Store className="w-5 h-5" />
                      Start Selling
                    </Link>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="bg-white py-8 border-b">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-amazon-orange/10 flex items-center justify-center">
                <Truck className="w-6 h-6 text-amazon-orange" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Free Shipping</h3>
                <p className="text-sm text-gray-500">On orders over $50</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-amazon-orange/10 flex items-center justify-center">
                <Shield className="w-6 h-6 text-amazon-orange" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Secure Payment</h3>
                <p className="text-sm text-gray-500">100% secure checkout</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-amazon-orange/10 flex items-center justify-center">
                <HeadphonesIcon className="w-6 h-6 text-amazon-orange" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">24/7 Support</h3>
                <p className="text-sm text-gray-500">Dedicated support</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-amazon-orange/10 flex items-center justify-center">
                <ShoppingBag className="w-6 h-6 text-amazon-orange" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Easy Returns</h3>
                <p className="text-sm text-gray-500">30-day return policy</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Categories */}
      <section className="py-12 bg-gray-50">
        <div className="container mx-auto px-4">
          <h2 className="text-2xl font-bold text-gray-900 mb-8">Shop by Category</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {categories.map((category) => (
              <Link
                key={category.name}
                to={`/search?category=${category.name}`}
                className="group relative overflow-hidden rounded-xl bg-white shadow-sm hover:shadow-lg transition-all duration-300"
              >
                <div className={`absolute inset-0 bg-gradient-to-br ${category.color} opacity-0 group-hover:opacity-10 transition-opacity`} />
                <div className="p-6 text-center">
                  <span className="text-4xl mb-3 block">{category.icon}</span>
                  <h3 className="font-medium text-gray-900">{category.name}</h3>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-12">
        <div className="container mx-auto px-4">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl font-bold text-gray-900">Featured Products</h2>
            <Link
              to="/search?name=a"
              className="text-amazon-orange hover:text-amazon-orange-hover font-medium flex items-center gap-1"
            >
              View All
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>

          {loading ? (
            <div className="flex justify-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : featuredProducts.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
              {featuredProducts.map((product) => (
                <ProductCard
                  key={product.id}
                  product={product}
                  offer={productOffers[product.id]}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-12 bg-white rounded-lg">
              <ShoppingBag className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-700 mb-2">No products available yet</h3>
              <p className="text-gray-500 mb-6">Be the first to add products to our marketplace!</p>
              <Link to="/register" className="btn-primary">
                Become a Seller
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-amazon-dark py-16">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold text-white mb-4">Ready to Start Selling?</h2>
          <p className="text-gray-300 mb-8 max-w-2xl mx-auto">
            Join thousands of merchants already selling on ShopZone. Easy setup, powerful tools, and millions of customers waiting.
          </p>
          <Link
            to="/register?type=merchant"
            className="btn-primary text-lg px-8 py-3 inline-flex items-center gap-2"
          >
            Register as Merchant
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>
    </div>
  );
};

export default Home;