import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useAuth } from './AuthContext';

export type ThemeMode = 'dark' | 'light' | 'system';
export type ResolvedMode = 'dark' | 'light';

interface ThemeContextType {
  mode: ThemeMode;
  resolvedMode: ResolvedMode;
  setMode: (mode: ThemeMode) => void;
  toggleMode: () => void;
  accountUsername: string | null;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

// Helper to detect device / OS preference
const getSystemPreference = (): ResolvedMode => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  return 'dark';
};

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const username = user?.username ? user.username.toLowerCase() : null;

  // Account-scoped localStorage key
  const getStorageKey = useCallback((targetUsername: string | null) => {
    return targetUsername ? `eams_mode_${targetUsername}` : 'eams_guest_mode';
  }, []);

  // Load theme mode for specific user account
  const loadUserMode = useCallback((targetUsername: string | null): ThemeMode => {
    const key = getStorageKey(targetUsername);
    const saved = localStorage.getItem(key) as ThemeMode | null;
    if (saved === 'dark' || saved === 'light' || saved === 'system') {
      return saved;
    }
    return 'dark'; // Default to enterprise Dark Mode
  }, [getStorageKey]);

  const [mode, setModeState] = useState<ThemeMode>(() => loadUserMode(username));

  // Whenever active user changes (Login/Logout/Switch user), load THAT user's saved preference
  useEffect(() => {
    const userMode = loadUserMode(username);
    setModeState(userMode);
  }, [username, loadUserMode]);

  // Compute resolved mode ('dark' or 'light')
  const resolvedMode: ResolvedMode = mode === 'system' ? getSystemPreference() : mode;

  const setMode = (newMode: ThemeMode) => {
    setModeState(newMode);
    const key = getStorageKey(username);
    localStorage.setItem(key, newMode);
  };

  const toggleMode = () => {
    const nextMode: ThemeMode = resolvedMode === 'dark' ? 'light' : 'dark';
    setMode(nextMode);
  };

  // Sync DOM HTML classes
  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove('dark', 'light');
    root.classList.add(resolvedMode);
  }, [resolvedMode]);

  // Listen for system changes if mode is 'system'
  useEffect(() => {
    if (mode !== 'system' || !window.matchMedia) return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleMediaChange = () => {
      const root = document.documentElement;
      root.classList.remove('dark', 'light');
      root.classList.add(getSystemPreference());
    };

    mediaQuery.addEventListener('change', handleMediaChange);
    return () => mediaQuery.removeEventListener('change', handleMediaChange);
  }, [mode]);

  return (
    <ThemeContext.Provider value={{ mode, resolvedMode, setMode, toggleMode, accountUsername: username }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};
