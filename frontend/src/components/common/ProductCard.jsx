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
import { Link } from 'react-router-dom';
import { ShoppingCart, Star } from 'lucide-react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';

const ProductCard = ({ product, offer }) => {
  const { addToCart } = useCart();
  const { isAuthenticated, isMerchant } = useAuth();

  const handleAddToCart = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!offer) return;
    
    await addToCart({
      productId: product.id,
      merchantId: offer.merchantId,
      quantity: 1,
      // No priceSnapshot - backend will fetch real price
    });
  };

  // Inline SVG fallback to avoid external placeholder DNS failures
  const fallbackSvg = `data:image/svg+xml;utf8,${encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' width='300' height='300'><rect width='100%' height='100%' fill='#f3f4f6'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='14' fill='#9CA3AF'>${product.name}</text></svg>`
  )}`;

  return (
    <Link
      to={`/product/${product.id}`}
      className="product-card block hover:transform hover:scale-[1.02] transition-transform duration-200"
    >
      <div className="relative aspect-square bg-gray-100 overflow-hidden">
        <img
          src={product.imageUrl || fallbackSvg}
          alt={product.name}
          className="w-full h-full object-cover"
          onError={(e) => {
            // prevent infinite loop if fallback also fails
            e.target.onerror = null;
            e.target.src = fallbackSvg;
          }}
        />
        {offer && (
          <div className="absolute top-2 right-2 bg-green-500 text-white text-xs px-2 py-1 rounded-full">
            In Stock
          </div>
        )}
      </div>
      
      <div className="p-4">
        <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">
          {product.brand || 'Generic'}
        </p>
        <h3 className="font-medium text-gray-900 line-clamp-2 mb-1 min-h-[2.5rem]">
          {product.name}
        </h3>
        <p className="text-xs text-gray-500 mb-2">{product.category}</p>
        
        {/* Rating placeholder */}
        <div className="flex items-center gap-1 mb-2">
          {[...Array(5)].map((_, i) => (
            <Star
              key={i}
              className={`w-4 h-4 ${i < 4 ? 'fill-amazon-orange text-amazon-orange' : 'text-gray-300'}`}
            />
          ))}
          <span className="text-xs text-gray-500 ml-1">(128)</span>
        </div>
        
        {offer ? (
          <div className="space-y-2">
            <div className="flex items-baseline gap-2">
              <span className="text-xl font-bold text-gray-900">
                {`$${getOfferNumericPrice(offer).toFixed(2)}`}
              </span>
              <span className="text-sm text-gray-500 line-through">
                {`$${(getOfferNumericPrice(offer) * 1.2).toFixed(2)}`}
              </span>
            </div>
            
            {/* Show Add to Cart for everyone except merchants */}
            {!isMerchant && (
              <button
                onClick={handleAddToCart}
                className="w-full btn-add-to-cart flex items-center justify-center gap-2"
              >
                <ShoppingCart className="w-4 h-4" />
                Add to Cart
              </button>
            )}
          </div>
        ) : (
          <p className="text-gray-500 text-sm">No offers available</p>
        )}
      </div>
    </Link>
  );
};

export default ProductCard;
