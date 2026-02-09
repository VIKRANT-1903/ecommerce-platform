import { Link } from 'react-router-dom';
import { ShoppingCart, Star, Edit, Package } from 'lucide-react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';

// Helper to extract numeric price safely
function getOfferNumericPrice(offer) {
  if (!offer) return 0;
  const raw = offer.price;
  let price = 0;

  if (typeof raw === 'number') {
    price = raw;
  } else if (raw && typeof raw === 'object') {
    // Handle complex price objects if backend returns BigDecimal/Money objects
    price = raw.amount ?? raw.value ?? raw.price ?? raw.cents ?? 0;
    if (raw.cents && !raw.amount && !raw.value) price = raw.cents / 100;
  }

  return isFinite(price) ? price : 0;
}

const ProductCard = ({ product, offer }) => {
  // Safe destructuring with defaults to prevent crashes
  const cartContext = useCart();
  const addToCart = cartContext?.addToCart || (() => {});

  const authContext = useAuth();
  const isAuthenticated = authContext?.isAuthenticated || false;
  const isMerchant = authContext?.isMerchant || false;

  const handleAddToCart = async (e) => {
    e.preventDefault(); // Prevent clicking the Link
    e.stopPropagation();

    if (!offer) return;

    await addToCart({
      productId: product.id,
      merchantId: offer.merchantId,
      quantity: 1,
    });
  };

  // Safe fallback SVG image
  const fallbackSvg = `data:image/svg+xml;utf8,${encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' width='300' height='300'><rect width='100%' height='100%' fill='#f3f4f6'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='14' fill='#9CA3AF'>${product.name || 'Product'}</text></svg>`
  )}`;

  const displayPrice = getOfferNumericPrice(offer);

  return (
    <Link
      to={`/product/${product.id}`}
      className="product-card block bg-white border border-gray-100 rounded-lg overflow-hidden hover:shadow-lg hover:-translate-y-1 transition-all duration-200 h-full flex flex-col"
    >
      {/* Image Container */}
      <div className="relative aspect-square bg-gray-100 overflow-hidden">
        <img
          src={product.imageUrl || fallbackSvg}
          alt={product.name}
          className="w-full h-full object-cover"
          onError={(e) => {
            e.target.onerror = null;
            e.target.src = fallbackSvg;
          }}
        />
        {/* Badge: Show ID for Merchant, Stock for Customer */}
        <div className="absolute top-2 right-2">
          {isMerchant ? (
            <span className="bg-black/70 text-white text-[10px] px-2 py-1 rounded backdrop-blur-sm">
              ID: {product.id}
            </span>
          ) : (
             offer && (
              <span className="bg-green-500 text-white text-xs px-2 py-1 rounded-full shadow-sm">
                In Stock
              </span>
            )
          )}
        </div>
      </div>

      {/* Details Container */}
      <div className="p-4 flex flex-col flex-1">
        <p className="text-xs text-gray-500 uppercase tracking-wide mb-1 truncate">
          {product.brand || product.category || 'Generic'}
        </p>

        <h3 className="font-medium text-gray-900 line-clamp-2 mb-1 min-h-[2.5rem]" title={product.name}>
          {product.name}
        </h3>

        {/* Rating Mockup */}
        <div className="flex items-center gap-1 mb-3">
          {[...Array(5)].map((_, i) => (
            <Star
              key={i}
              className={`w-3.5 h-3.5 ${i < 4 ? 'fill-amazon-orange text-amazon-orange' : 'text-gray-300'}`}
            />
          ))}
          <span className="text-xs text-gray-500 ml-1">(128)</span>
        </div>

        <div className="mt-auto">
          {offer ? (
            <div className="space-y-3">
              <div className="flex items-baseline gap-2">
                <span className="text-xl font-bold text-gray-900">
                  {`$${displayPrice.toFixed(2)}`}
                </span>
                <span className="text-xs text-gray-400 line-through">
                  {`$${(displayPrice * 1.2).toFixed(2)}`}
                </span>
              </div>

              {/* Conditional Buttons based on Role */}
              {isMerchant ? (
                <Link
                  to={`/merchant/offers?productId=${product.id}`}
                  onClick={(e) => e.stopPropagation()}
                  className="w-full py-2 rounded-lg bg-gray-100 text-gray-700 font-medium text-sm hover:bg-amazon-orange hover:text-white transition-colors flex items-center justify-center gap-2"
                >
                  <Edit className="w-4 h-4" />
                  Manage Offer
                </Link>
              ) : (
                <button
                  onClick={handleAddToCart}
                  className="w-full py-2 rounded-lg bg-amazon-orange text-white font-medium text-sm hover:bg-amazon-orange-hover transition-colors flex items-center justify-center gap-2 shadow-sm"
                >
                  <ShoppingCart className="w-4 h-4" />
                  Add to Cart
                </button>
              )}
            </div>
          ) : (
            <div className="mt-auto">
              <p className="text-gray-500 text-sm italic mb-3">Currently unavailable</p>
              {isMerchant && (
                 <Link
                  to={`/merchant/offers?productId=${product.id}`}
                  onClick={(e) => e.stopPropagation()}
                  className="w-full py-2 rounded-lg border border-dashed border-gray-300 text-gray-600 font-medium text-sm hover:border-amazon-orange hover:text-amazon-orange transition-colors flex items-center justify-center gap-2"
                >
                  <Package className="w-4 h-4" />
                  Create Offer
                </Link>
              )}
            </div>
          )}
        </div>
      </div>
    </Link>
  );
};

export default ProductCard;