import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { productService, offerService } from '../services/authService';
import { inventoryService } from '../services/ecommService';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Alert from '../components/common/Alert';
import {
  ShoppingCart,
  Star,
  Truck,
  Shield,
  RotateCcw,
  ChevronRight,
  Minus,
  Plus,
  Check,
  Package,
  Store,
} from 'lucide-react';
import toast from 'react-hot-toast';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { isAuthenticated, isMerchant } = useAuth();
  
  const [product, setProduct] = useState(null);
  const [offers, setOffers] = useState([]);
  const [selectedOffer, setSelectedOffer] = useState(null);
  const [inventory, setInventory] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [addingToCart, setAddingToCart] = useState(false);

  useEffect(() => {
    fetchProductData();
  }, [id]);

  const fetchProductData = async () => {
    setLoading(true);
    try {
      // Fetch product
      const productResponse = await productService.getById(id);
      if (productResponse.success) {
        setProduct(productResponse.data);
        
        // Fetch offers
        try {
          const offersResponse = await offerService.getByProductId(id);
          if (offersResponse.success && offersResponse.data?.length > 0) {
            const activeOffers = offersResponse.data.filter(o => o.status === 'ACTIVE');
            setOffers(activeOffers);
            // Select the best price offer by default
            if (activeOffers.length > 0) {
              const bestOffer = activeOffers.reduce((best, current) =>
                current.price < best.price ? current : best
              );
              setSelectedOffer(bestOffer);
              
              // Fetch inventory for the selected offer
              try {
                const inventoryResponse = await inventoryService.getInventory(id, bestOffer.merchantId);
                if (inventoryResponse.success) {
                  setInventory(inventoryResponse.data);
                }
              } catch (error) {
                console.log('Inventory not available');
              }
            }
          }
        } catch (error) {
          console.log('No offers available');
        }
      }
    } catch (error) {
      console.error('Failed to fetch product:', error);
      toast.error('Product not found');
      navigate('/search');
    } finally {
      setLoading(false);
    }
  };

  const handleOfferSelect = async (offer) => {
    setSelectedOffer(offer);
    setQuantity(1);
    
    // Fetch inventory for this offer
    try {
      const inventoryResponse = await inventoryService.getInventory(id, offer.merchantId);
      if (inventoryResponse.success) {
        setInventory(inventoryResponse.data);
      } else {
        setInventory(null);
      }
    } catch (error) {
      setInventory(null);
    }
  };

  const handleAddToCart = async () => {
    if (!selectedOffer) {
      toast.error('Please select an offer');
      return;
    }
    
    setAddingToCart(true);
    const result = await addToCart({
      productId: id,
      merchantId: selectedOffer.merchantId,
      quantity,
      priceSnapshot: selectedOffer.price,
    });
    setAddingToCart(false);
    
    if (result.success) {
      // Optional: navigate to cart
    }
  };

  const handleBuyNow = async () => {
    if (!selectedOffer) {
      toast.error('Please select an offer');
      return;
    }
    
    setAddingToCart(true);
    const result = await addToCart({
      productId: id,
      merchantId: selectedOffer.merchantId,
      quantity,
      priceSnapshot: selectedOffer.price,
    });
    setAddingToCart(false);
    
    if (result.success) {
      navigate('/checkout');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="container mx-auto px-4 py-12">
        <Alert
          type="error"
          title="Product not found"
          message="The product you're looking for doesn't exist or has been removed."
        />
        <Link to="/search" className="btn-primary mt-4 inline-block">
          Back to Search
        </Link>
      </div>
    );
  }

  const placeholderImage = `https://via.placeholder.com/600x600?text=${encodeURIComponent(product.name)}`;
  const availableQty = inventory?.availableQty || 0;
  const isInStock = availableQty > 0;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Breadcrumb */}
      <nav className="flex items-center gap-2 text-sm text-gray-500 mb-6">
        <Link to="/" className="hover:text-amazon-orange">Home</Link>
        <ChevronRight className="w-4 h-4" />
        <Link to={`/search?category=${product.category}`} className="hover:text-amazon-orange">
          {product.category}
        </Link>
        <ChevronRight className="w-4 h-4" />
        <span className="text-gray-900 truncate max-w-xs">{product.name}</span>
      </nav>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Product Image */}
        <div className="bg-white rounded-lg p-4">
          <div className="aspect-square bg-gray-100 rounded-lg overflow-hidden">
            <img
              src={product.imageUrl || placeholderImage}
              alt={product.name}
              className="w-full h-full object-cover"
              onError={(e) => {
                e.target.src = placeholderImage;
              }}
            />
          </div>
        </div>

        {/* Product Info */}
        <div className="space-y-6">
          <div>
            {product.brand && (
              <Link
                to={`/search?name=${product.brand}`}
                className="text-amazon-blue hover:underline text-sm"
              >
                {product.brand}
              </Link>
            )}
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900 mt-1">
              {product.name}
            </h1>
            
            {/* Rating */}
            <div className="flex items-center gap-2 mt-2">
              <div className="flex">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={`w-5 h-5 ${i < 4 ? 'fill-amazon-orange text-amazon-orange' : 'text-gray-300'}`}
                  />
                ))}
              </div>
              <span className="text-amazon-blue hover:text-amazon-orange cursor-pointer">
                128 ratings
              </span>
            </div>
          </div>

          {/* Price */}
          {selectedOffer ? (
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-baseline gap-2">
                <span className="text-3xl font-bold text-gray-900">
                  ${selectedOffer.price.toFixed(2)}
                </span>
                <span className="text-lg text-gray-500 line-through">
                  ${(selectedOffer.price * 1.2).toFixed(2)}
                </span>
                <span className="text-green-600 font-medium">
                  Save 20%
                </span>
              </div>
              
              {/* Stock Status */}
              <div className="mt-2">
                {isInStock ? (
                  <div className="flex items-center gap-2 text-green-600">
                    <Check className="w-5 h-5" />
                    <span className="font-medium">In Stock</span>
                    <span className="text-gray-500">({availableQty} available)</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-2 text-red-600">
                    <Package className="w-5 h-5" />
                    <span className="font-medium">Out of Stock</span>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <Alert
              type="warning"
              title="No offers available"
              message="This product currently has no sellers."
            />
          )}

          {/* Multiple Offers */}
          {offers.length > 1 && (
            <div>
              <h3 className="font-medium text-gray-900 mb-2">
                Choose a seller ({offers.length} offers):
              </h3>
              <div className="space-y-2">
                {offers.map((offer) => (
                  <button
                    key={offer.offerId}
                    onClick={() => handleOfferSelect(offer)}
                    className={`w-full p-3 rounded-lg border-2 text-left transition-all ${
                      selectedOffer?.offerId === offer.offerId
                        ? 'border-amazon-orange bg-amazon-orange/5'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Store className="w-4 h-4 text-gray-400" />
                        <span className="text-sm text-gray-600">Merchant #{offer.merchantId}</span>
                      </div>
                      <span className="font-bold text-lg">${offer.price.toFixed(2)}</span>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Quantity & Add to Cart */}
          {selectedOffer && !isMerchant && (
            <div className="space-y-4">
              {/* Quantity Selector */}
              <div className="flex items-center gap-4">
                <span className="text-gray-700">Quantity:</span>
                <div className="flex items-center border border-gray-300 rounded-lg">
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="p-2 hover:bg-gray-100 transition-colors"
                    disabled={quantity <= 1}
                  >
                    <Minus className="w-4 h-4" />
                  </button>
                  <span className="px-4 py-2 font-medium min-w-[50px] text-center">
                    {quantity}
                  </span>
                  <button
                    onClick={() => setQuantity(Math.min(availableQty || 10, quantity + 1))}
                    className="p-2 hover:bg-gray-100 transition-colors"
                    disabled={quantity >= (availableQty || 10)}
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-3">
                <button
                  onClick={handleAddToCart}
                  disabled={addingToCart || !isInStock}
                  className="flex-1 btn-add-to-cart py-3 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {addingToCart ? (
                    <LoadingSpinner size="sm" />
                  ) : (
                    <ShoppingCart className="w-5 h-5" />
                  )}
                  Add to Cart
                </button>
                <button
                  onClick={handleBuyNow}
                  disabled={addingToCart || !isInStock}
                  className="flex-1 btn-buy-now py-3 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Buy Now
                </button>
              </div>
            </div>
          )}

          {/* Features */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-4 border-t border-gray-200">
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <Truck className="w-5 h-5 text-amazon-orange" />
              <span>Free Delivery</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <RotateCcw className="w-5 h-5 text-amazon-orange" />
              <span>30-Day Returns</span>
            </div>
            <div className="flex items-center gap-2 text-sm text-gray-600">
              <Shield className="w-5 h-5 text-amazon-orange" />
              <span>Secure Payment</span>
            </div>
          </div>
        </div>
      </div>

      {/* Product Description */}
      <div className="mt-8 bg-white rounded-lg p-6">
        <h2 className="text-xl font-bold text-gray-900 mb-4">About this item</h2>
        <p className="text-gray-600 leading-relaxed">
          {product.description || 'No description available for this product.'}
        </p>
        
        <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <h3 className="font-medium text-gray-900 mb-2">Product Details</h3>
            <ul className="space-y-2 text-sm text-gray-600">
              <li className="flex">
                <span className="w-32 font-medium">Category:</span>
                <span>{product.category}</span>
              </li>
              {product.brand && (
                <li className="flex">
                  <span className="w-32 font-medium">Brand:</span>
                  <span>{product.brand}</span>
                </li>
              )}
              <li className="flex">
                <span className="w-32 font-medium">Product ID:</span>
                <span className="font-mono text-xs">{product.id}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
