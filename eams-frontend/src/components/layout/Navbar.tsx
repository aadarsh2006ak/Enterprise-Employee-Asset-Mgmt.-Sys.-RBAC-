import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Menu, LogOut, ShieldCheck, User, ChevronDown } from 'lucide-react';
import { RoleBadge } from '../common/Badge';
import { ThemeToggle } from '../common/ThemeToggle';

interface NavbarProps {
  onToggleSidebar: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onToggleSidebar }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-30 h-16 glass-panel px-4 sm:px-6 flex items-center justify-between backdrop-blur-xl">
      {/* Left section: mobile menu toggle and status pill */}
      <div className="flex items-center gap-3">
        <button
          onClick={onToggleSidebar}
          className="p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/[0.08] lg:hidden transition-colors border border-transparent hover:border-slate-200 dark:hover:border-white/[0.1]"
          aria-label="Toggle Navigation"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-100 dark:bg-black/40 border border-slate-200 dark:border-white/[0.08] text-xs shadow-inner">
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span className="font-semibold text-slate-700 dark:text-slate-200 text-[11px] tracking-wide">RBAC Engine Active</span>
          <span className="text-slate-400 dark:text-slate-600">|</span>
          <span className="text-slate-500 dark:text-slate-400 text-[11px] font-mono">v1.0.0</span>
        </div>
      </div>

      {/* Right section: Theme Switcher & user profile dropdown */}
      <div className="flex items-center gap-3">
        <ThemeToggle />

        {user && (
          <div className="relative">
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="flex items-center gap-2.5 p-1.5 pr-3 rounded-full bg-slate-100 dark:bg-black/40 border border-slate-200 dark:border-white/[0.08] hover:border-indigo-500/40 hover:bg-slate-200/60 dark:hover:bg-slate-850/90 transition-all duration-200 group shadow-sm"
            >
              <div className="w-8 h-8 rounded-full gradient-accent text-white font-bold text-xs flex items-center justify-center shadow-md group-hover:scale-105 transition-transform">
                {user.username.charAt(0).toUpperCase()}
              </div>
              <div className="hidden md:flex flex-col text-left">
                <span className="text-xs font-semibold text-slate-800 dark:text-slate-200 group-hover:text-indigo-600 dark:group-hover:text-white transition-colors">
                  {user.username}
                </span>
                <span className="text-[10px] text-slate-500 dark:text-slate-400 font-mono tracking-tight">{user.email}</span>
              </div>
              <RoleBadge role={user.role} />
              <ChevronDown className={`w-3.5 h-3.5 text-slate-500 dark:text-slate-400 transition-transform duration-200 ${dropdownOpen ? 'rotate-180' : ''}`} />
            </button>

            {/* User Dropdown */}
            {dropdownOpen && (
              <>
                <div
                  className="fixed inset-0 z-40"
                  onClick={() => setDropdownOpen(false)}
                />
                <div className="absolute right-0 mt-2 w-72 glass-dropdown rounded-2xl shadow-2xl z-50 p-2.5 border animate-slide-up">
                  <div className="p-3 bg-slate-100 dark:bg-black/40 rounded-xl border border-slate-200 dark:border-white/[0.08] mb-2">
                    <div className="flex items-center gap-2">
                      <div className="w-9 h-9 rounded-full gradient-accent text-white font-bold text-xs flex items-center justify-center shadow-md">
                        {user.username.charAt(0).toUpperCase()}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{user.username}</p>
                        <p className="text-[11px] text-slate-500 dark:text-slate-400 truncate font-mono">{user.email}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-1.5 mt-2.5 pt-2 border-t border-slate-200 dark:border-white/[0.06] text-[11px] text-indigo-600 dark:text-indigo-400">
                      <ShieldCheck className="w-3.5 h-3.5 flex-shrink-0" />
                      <span>{user.permissions?.length || 0} permissions authorized</span>
                    </div>
                  </div>

                  <div className="space-y-1">
                    <button
                      onClick={() => {
                        setDropdownOpen(false);
                        navigate('/my-assets');
                      }}
                      className="w-full flex items-center gap-2.5 px-3 py-2 text-xs font-medium text-slate-700 dark:text-slate-300 hover:text-indigo-600 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-white/[0.06] rounded-xl transition-all text-left group"
                    >
                      <User className="w-4 h-4 text-slate-500 dark:text-slate-400 group-hover:text-indigo-500 transition-colors" />
                      <span>My Assigned Assets</span>
                    </button>
                    <button
                      onClick={() => {
                        setDropdownOpen(false);
                        handleLogout();
                      }}
                      className="w-full flex items-center gap-2.5 px-3 py-2 text-xs font-medium text-rose-500 dark:text-rose-400 hover:bg-rose-500/10 rounded-xl transition-all text-left"
                    >
                      <LogOut className="w-4 h-4 text-rose-500 dark:text-rose-400" />
                      <span>Sign Out</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
