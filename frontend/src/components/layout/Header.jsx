import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { 
  ShoppingCart, 
  User, 
  Search, 
  Menu, 
  X, 
  ChevronDown,
  Package,
  LogOut,
  Store,
  LayoutDashboard
} from 'lucide-react';

const Header = () => {
  const { user, isAuthenticated, isMerchant, logout } = useAuth();
  const { cartItemCount } = useCart();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?name=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  const handleLogout = () => {
    logout();
    setIsUserMenuOpen(false);
    navigate('/');
  };

  return (
    <header className="bg-amazon-dark text-white sticky top-0 z-50">
      {/* Main header */}
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16 gap-4">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 flex-shrink-0">
            <Store className="w-8 h-8 text-amazon-orange" />
            <span className="text-xl font-bold text-white hidden sm:block">ShopZone</span>
          </Link>

          {/* Search bar */}
          <form onSubmit={handleSearch} className="flex-1 max-w-2xl hidden md:flex">
            <div className="flex w-full">
              <input
                type="text"
                placeholder="Search products..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="flex-1 px-4 py-2 text-gray-900 rounded-l-md focus:outline-none focus:ring-2 focus:ring-amazon-orange"
              />
              <button
                type="submit"
                className="bg-amazon-orange hover:bg-amazon-orange-hover px-4 py-2 rounded-r-md transition-colors"
              >
                <Search className="w-5 h-5" />
              </button>
            </div>
          </form>

          {/* Right side actions */}
          <div className="flex items-center gap-2 sm:gap-4">
            {/* User menu */}
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center gap-1 hover:text-amazon-orange transition-colors py-2"
                >
                  <User className="w-5 h-5" />
                  <span className="hidden sm:block text-sm">
                    Hello, {user?.firstName || 'User'}
                  </span>
                  <ChevronDown className="w-4 h-4" />
                </button>

                {isUserMenuOpen && (
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg py-2 z-50 fade-in">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="text-sm text-gray-500">Signed in as</p>
                      <p className="text-sm font-medium text-gray-900 truncate">{user?.email}</p>
                      <span className="inline-block mt-1 px-2 py-0.5 text-xs rounded-full bg-amazon-orange text-white">
                        {user?.role}
                      </span>
                    </div>
                    
                    <Link
                      to="/profile"
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100 transition-colors"
                    >
                      <User className="w-4 h-4" />
                      My Profile
                    </Link>
                    
                    {isMerchant && (
                      <>
                        <Link
                          to="/merchant/dashboard"
                          onClick={() => setIsUserMenuOpen(false)}
                          className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100 transition-colors"
                        >
                          <LayoutDashboard className="w-4 h-4" />
                          Merchant Dashboard
                        </Link>
                        <Link
                          to="/merchant/products"
                          onClick={() => setIsUserMenuOpen(false)}
                          className="flex items-center gap-2 px-4 py-2 text-gray-700 hover:bg-gray-100 transition-colors"
                        >
                          <Package className="w-4 h-4" />
                          My Products
                        </Link>
                      </>
                    )}
                    
                    <div className="border-t border-gray-100 mt-2 pt-2">
                      <button
                        onClick={handleLogout}
                        className="flex items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-50 w-full text-left transition-colors"
                      >
                        <LogOut className="w-4 h-4" />
                        Sign Out
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-1 hover:text-amazon-orange transition-colors"
              >
                <User className="w-5 h-5" />
                <span className="hidden sm:block text-sm">Sign In</span>
              </Link>
            )}

            {/* Cart */}
            {!isMerchant && (
              <Link
                to="/cart"
                className="flex items-center gap-1 hover:text-amazon-orange transition-colors relative"
              >
                <ShoppingCart className="w-6 h-6" />
                {cartItemCount > 0 && (
                  <span className="absolute -top-2 -right-2 bg-amazon-orange text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                    {cartItemCount > 99 ? '99+' : cartItemCount}
                  </span>
                )}
                <span className="hidden sm:block text-sm">Cart</span>
              </Link>
            )}

            {/* Mobile menu toggle */}
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="md:hidden p-2"
            >
              {isMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile search */}
        <div className="md:hidden pb-3">
          <form onSubmit={handleSearch} className="flex">
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="flex-1 px-4 py-2 text-gray-900 rounded-l-md focus:outline-none"
            />
            <button
              type="submit"
              className="bg-amazon-orange hover:bg-amazon-orange-hover px-4 py-2 rounded-r-md transition-colors"
            >
              <Search className="w-5 h-5" />
            </button>
          </form>
        </div>
      </div>

      {/* Secondary nav */}
      <div className="bg-amazon-light-dark">
        <div className="container mx-auto px-4">
          <nav className="flex items-center gap-6 h-10 text-sm overflow-x-auto">
            <Link to="/search?category=Electronics" className="hover:text-amazon-orange whitespace-nowrap transition-colors">
              Electronics
            </Link>
            <Link to="/search?category=Clothing" className="hover:text-amazon-orange whitespace-nowrap transition-colors">
              Clothing
            </Link>
            <Link to="/search?category=Books" className="hover:text-amazon-orange whitespace-nowrap transition-colors">
              Books
            </Link>
            <Link to="/search?category=Home" className="hover:text-amazon-orange whitespace-nowrap transition-colors">
              Home & Garden
            </Link>
            <Link to="/search?category=Sports" className="hover:text-amazon-orange whitespace-nowrap transition-colors">
              Sports
            </Link>
            <Link to="/search?category=Toys" className="hover:text-amazon-orange whitespace-nowrap transition-colors">
              Toys
            </Link>
          </nav>
        </div>
      </div>

      {/* Mobile menu overlay */}
      {isMenuOpen && (
        <div className="md:hidden absolute top-full left-0 right-0 bg-amazon-light-dark py-4 px-4 fade-in">
          <nav className="flex flex-col gap-2">
            <Link
              to="/search?category=Electronics"
              onClick={() => setIsMenuOpen(false)}
              className="py-2 hover:text-amazon-orange transition-colors"
            >
              Electronics
            </Link>
            <Link
              to="/search?category=Clothing"
              onClick={() => setIsMenuOpen(false)}
              className="py-2 hover:text-amazon-orange transition-colors"
            >
              Clothing
            </Link>
            <Link
              to="/search?category=Books"
              onClick={() => setIsMenuOpen(false)}
              className="py-2 hover:text-amazon-orange transition-colors"
            >
              Books
            </Link>
          </nav>
        </div>
      )}

      {/* Click outside to close menus */}
      {(isUserMenuOpen || isMenuOpen) && (
        <div
          className="fixed inset-0 z-40"
          onClick={() => {
            setIsUserMenuOpen(false);
            setIsMenuOpen(false);
          }}
        />
      )}
    </header>
  );
};

export default Header;
