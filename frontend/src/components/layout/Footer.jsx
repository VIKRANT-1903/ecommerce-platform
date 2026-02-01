import { Link } from 'react-router-dom';
import { Store, Mail, Phone, MapPin } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="bg-amazon-dark text-white mt-auto">
      {/* Back to top */}
      <button
        onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
        className="w-full bg-amazon-light-dark hover:bg-gray-700 py-4 text-sm transition-colors"
      >
        Back to top
      </button>

      {/* Main footer content */}
      <div className="container mx-auto px-4 py-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <Store className="w-8 h-8 text-amazon-orange" />
              <span className="text-xl font-bold">ShopZone</span>
            </div>
            <p className="text-gray-400 text-sm">
              Your one-stop destination for all your shopping needs. Quality products, great prices, fast delivery.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="font-semibold mb-4 text-lg">Quick Links</h3>
            <ul className="space-y-2 text-sm text-gray-400">
              <li>
                <Link to="/search?category=Electronics" className="hover:text-amazon-orange transition-colors">
                  Electronics
                </Link>
              </li>
              <li>
                <Link to="/search?category=Clothing" className="hover:text-amazon-orange transition-colors">
                  Clothing
                </Link>
              </li>
              <li>
                <Link to="/search?category=Books" className="hover:text-amazon-orange transition-colors">
                  Books
                </Link>
              </li>
              <li>
                <Link to="/search?category=Home" className="hover:text-amazon-orange transition-colors">
                  Home & Garden
                </Link>
              </li>
            </ul>
          </div>

          {/* Customer Service */}
          <div>
            <h3 className="font-semibold mb-4 text-lg">Customer Service</h3>
            <ul className="space-y-2 text-sm text-gray-400">
              <li>
                <Link to="/profile" className="hover:text-amazon-orange transition-colors">
                  Your Account
                </Link>
              </li>
              <li>
                <Link to="/cart" className="hover:text-amazon-orange transition-colors">
                  Your Cart
                </Link>
              </li>
              <li>
                <a href="#" className="hover:text-amazon-orange transition-colors">
                  Returns & Refunds
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-amazon-orange transition-colors">
                  Help Center
                </a>
              </li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="font-semibold mb-4 text-lg">Contact Us</h3>
            <ul className="space-y-3 text-sm text-gray-400">
              <li className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-amazon-orange" />
                support@shopzone.com
              </li>
              <li className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-amazon-orange" />
                1-800-SHOPZONE
              </li>
              <li className="flex items-start gap-2">
                <MapPin className="w-4 h-4 text-amazon-orange flex-shrink-0 mt-0.5" />
                123 Commerce Street, Tech City, TC 12345
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Bottom bar */}
      <div className="border-t border-gray-700">
        <div className="container mx-auto px-4 py-4">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4 text-sm text-gray-400">
            <p>© 2026 ShopZone. All rights reserved.</p>
            <div className="flex gap-6">
              <a href="#" className="hover:text-amazon-orange transition-colors">Privacy Policy</a>
              <a href="#" className="hover:text-amazon-orange transition-colors">Terms of Service</a>
              <a href="#" className="hover:text-amazon-orange transition-colors">Cookie Policy</a>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
