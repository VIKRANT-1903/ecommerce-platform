import { createContext, useContext, useState, useEffect } from 'react';
import { authService, merchantService } from '../services/authService';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [merchantProfile, setMerchantProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const token = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (token && storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        setUser(userData);
        setIsAuthenticated(true);
        
        // Fetch fresh profile
        const response = await authService.getProfile();
        if (response.success) {
          const updatedUser = { ...userData, ...response.data };
          setUser(updatedUser);
          localStorage.setItem('user', JSON.stringify(updatedUser));
          
          // If merchant, fetch merchant profile
          if (updatedUser.role === 'MERCHANT') {
            try {
              const merchantResponse = await merchantService.getProfile();
              if (merchantResponse.success) {
                setMerchantProfile(merchantResponse.data);
              }
            } catch (error) {
              console.error('Failed to fetch merchant profile:', error);
            }
          }
        }
      } catch (error) {
        console.error('Auth check failed:', error);
        logout();
      }
    }
    setLoading(false);
  };

  const login = async (credentials) => {
    try {
      const response = await authService.login(credentials);
      if (response.success) {
        const { token, userId, email, role } = response.data;
        localStorage.setItem('token', token);
        const userData = { id: userId, email, role };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        setIsAuthenticated(true);
        
        // Fetch full profile
        try {
          const profileResponse = await authService.getProfile();
          if (profileResponse.success) {
            const fullUser = { ...userData, ...profileResponse.data };
            setUser(fullUser);
            localStorage.setItem('user', JSON.stringify(fullUser));
          }
        } catch (error) {
          console.error('Failed to fetch profile:', error);
        }
        
        // If merchant, fetch merchant profile
        if (role === 'MERCHANT') {
          try {
            const merchantResponse = await merchantService.getProfile();
            if (merchantResponse.success) {
              setMerchantProfile(merchantResponse.data);
            }
          } catch (error) {
            console.error('Failed to fetch merchant profile:', error);
          }
        }
        
        toast.success('Login successful!');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.error?.detail || error.response?.data?.message || 'Login failed';
      toast.error(message);
      return { success: false, message };
    }
  };

  const registerCustomer = async (data) => {
    try {
      const response = await authService.registerCustomer(data);
      if (response.success) {
        const { token, userId, email, role } = response.data;
        localStorage.setItem('token', token);
        const userData = { id: userId, email, role, firstName: data.firstName, lastName: data.lastName, phone: data.phone };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        setIsAuthenticated(true);
        toast.success('Registration successful!');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.error?.detail || error.response?.data?.message || 'Registration failed';
      toast.error(message);
      return { success: false, message };
    }
  };

  const registerMerchant = async (data) => {
    try {
      const response = await authService.registerMerchant(data);
      if (response.success) {
        const { token, userId, email, role } = response.data;
        localStorage.setItem('token', token);
        const userData = { id: userId, email, role, firstName: data.firstName, lastName: data.lastName, phone: data.phone };
        localStorage.setItem('user', JSON.stringify(userData));
        setUser(userData);
        setIsAuthenticated(true);
        
        // Fetch merchant profile
        try {
          const merchantResponse = await merchantService.getProfile();
          if (merchantResponse.success) {
            setMerchantProfile(merchantResponse.data);
          }
        } catch (error) {
          console.error('Failed to fetch merchant profile:', error);
        }
        
        toast.success('Merchant registration successful!');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.error?.detail || error.response?.data?.message || 'Registration failed';
      toast.error(message);
      return { success: false, message };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setMerchantProfile(null);
    setIsAuthenticated(false);
    toast.success('Logged out successfully');
  };

  const updateProfile = async (data) => {
    try {
      const response = await authService.updateProfile(data);
      if (response.success) {
        const updatedUser = { ...user, ...response.data };
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
        toast.success('Profile updated successfully!');
        return { success: true };
      }
      return { success: false, message: response.message };
    } catch (error) {
      const message = error.response?.data?.error?.detail || 'Failed to update profile';
      toast.error(message);
      return { success: false, message };
    }
  };

  const value = {
    user,
    merchantProfile,
    loading,
    isAuthenticated,
    isMerchant: user?.role === 'MERCHANT',
    isCustomer: user?.role === 'CUSTOMER',
    login,
    logout,
    registerCustomer,
    registerMerchant,
    updateProfile,
    refreshMerchantProfile: async () => {
      try {
        const response = await merchantService.getProfile();
        if (response.success) {
          setMerchantProfile(response.data);
        }
      } catch (error) {
        console.error('Failed to refresh merchant profile:', error);
      }
    },
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
