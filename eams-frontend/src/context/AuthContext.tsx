import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { UserSummaryDto, RoleType, LoginRequest } from '../types';
import { authApi } from '../api';

interface AuthContextType {
  user: UserSummaryDto | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: RoleType | RoleType[]) => boolean;
  hasPermission: (permission: string | string[]) => boolean;
  hasAnyRole: (...roles: RoleType[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserSummaryDto | null>(() => {
    const savedUser = localStorage.getItem('eams_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('eams_access_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Initialize and verify authentication
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('eams_access_token');
      if (storedToken) {
        try {
          const currentUser = await authApi.getCurrentUser();
          setUser(currentUser);
          localStorage.setItem('eams_user', JSON.stringify(currentUser));
        } catch {
          // Token expired or invalid
          setUser(null);
          setToken(null);
          localStorage.removeItem('eams_access_token');
          localStorage.removeItem('eams_refresh_token');
          localStorage.removeItem('eams_user');
        }
      }
      setIsLoading(false);
    };

    initAuth();

    // Listen for custom logout event dispatched by Axios interceptor
    const handleLogoutEvent = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener('auth:logout', handleLogoutEvent);
    return () => {
      window.removeEventListener('auth:logout', handleLogoutEvent);
    };
  }, []);

  const login = async (credentials: LoginRequest) => {
    setIsLoading(true);
    try {
      const authResponse = await authApi.login(credentials);
      localStorage.setItem('eams_access_token', authResponse.accessToken);
      if (authResponse.refreshToken) {
        localStorage.setItem('eams_refresh_token', authResponse.refreshToken);
      }
      localStorage.setItem('eams_user', JSON.stringify(authResponse.user));

      setToken(authResponse.accessToken);
      setUser(authResponse.user);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('eams_refresh_token') || undefined;
    try {
      await authApi.logout(refreshToken);
    } catch {
      // Ignore network errors on logout
    } finally {
      localStorage.removeItem('eams_access_token');
      localStorage.removeItem('eams_refresh_token');
      localStorage.removeItem('eams_user');
      setUser(null);
      setToken(null);
    }
  };

  const hasRole = useCallback(
    (role: RoleType | RoleType[]): boolean => {
      if (!user) return false;
      if (Array.isArray(role)) {
        return role.includes(user.role);
      }
      return user.role === role;
    },
    [user]
  );

  const hasAnyRole = useCallback(
    (...roles: RoleType[]): boolean => {
      if (!user) return false;
      return roles.includes(user.role);
    },
    [user]
  );

  const hasPermission = useCallback(
    (permission: string | string[]): boolean => {
      if (!user || !user.permissions) return false;
      // Admin has all permissions automatically
      if (user.role === 'ADMIN') return true;

      if (Array.isArray(permission)) {
        return permission.some((p) => user.permissions.includes(p));
      }
      return user.permissions.includes(permission);
    },
    [user]
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        login,
        logout,
        hasRole,
        hasPermission,
        hasAnyRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
