import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Boxes,
  Laptop,
  Users,
  Building2,
  FileSpreadsheet,
  History,
  Shield,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { RoleBadge } from '../common/Badge';

interface SidebarProps {
  isOpen: boolean;
  onClose?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const { user, hasPermission, hasAnyRole } = useAuth();

  const navItems = [
    {
      to: '/',
      label: 'Dashboard',
      icon: LayoutDashboard,
      show: true,
    },
    {
      to: '/assets',
      label: 'Asset Inventory',
      icon: Boxes,
      show: hasPermission('ASSET_READ') || hasAnyRole('ADMIN', 'MANAGER'),
    },
    {
      to: '/my-assets',
      label: 'My Assigned Assets',
      icon: Laptop,
      show: true,
    },
    {
      to: '/employees',
      label: 'Employees',
      icon: Users,
      show: hasPermission('EMPLOYEE_READ') || hasAnyRole('ADMIN', 'MANAGER'),
    },
    {
      to: '/departments',
      label: 'Departments',
      icon: Building2,
      show: true,
    },
    {
      to: '/audit-logs',
      label: 'Audit Trail',
      icon: History,
      show: hasPermission('AUDIT_READ') || hasAnyRole('ADMIN', 'MANAGER'),
    },
    {
      to: '/exports',
      label: 'Export Center',
      icon: FileSpreadsheet,
      show: hasPermission('EXPORT_DATA') || hasAnyRole('ADMIN', 'MANAGER'),
    },
    {
      to: '/admin/users',
      label: 'User Accounts',
      icon: Users,
      show: hasAnyRole('ADMIN'),
    },
    {
      to: '/admin/roles',
      label: 'RBAC Matrix',
      icon: Shield,
      show: hasAnyRole('ADMIN'),
    },
  ];

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-slate-950/60 backdrop-blur-md z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 left-0 bottom-0 z-40 w-64 glass-panel border-r flex flex-col transition-transform duration-300 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="h-16 flex items-center gap-3 px-5 border-b border-slate-200 dark:border-white/[0.08] bg-slate-50/50 dark:bg-black/30">
          <div className="w-10 h-10 rounded-xl gradient-accent p-[1.5px] shadow-lg flex-shrink-0 animate-pulse-subtle">
            <div className="w-full h-full bg-white dark:bg-slate-950 rounded-[10px] flex items-center justify-center">
              <ShieldCheck className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
            </div>
          </div>
          <div className="min-w-0">
            <h1 className="font-extrabold text-base text-slate-900 dark:text-white tracking-wide flex items-center gap-1.5 font-sans">
              <span className="gradient-accent-text font-black">EAMS</span>
              <span className="text-[9px] uppercase tracking-widest px-1.5 py-0.5 rounded-full bg-indigo-50 dark:bg-white/[0.1] text-indigo-700 dark:text-white font-bold border border-indigo-200 dark:border-white/[0.15]">
                PRO
              </span>
            </h1>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 font-medium truncate">Enterprise Asset System</p>
          </div>
        </div>

        {/* Navigation Items */}
        <div className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          <div className="px-3 pb-2 text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest flex items-center justify-between">
            <span>Core Navigation</span>
            <Zap className="w-3 h-3 text-slate-400" />
          </div>

          {navItems
            .filter((item) => item.show)
            .map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/'}
                  onClick={onClose}
                  className={({ isActive }) =>
                    `group relative flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 ${
                      isActive
                        ? 'bg-indigo-50 dark:bg-white/[0.09] text-indigo-700 dark:text-white border border-indigo-200 dark:border-white/[0.18] shadow-sm'
                        : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-white/[0.05] border border-transparent'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      {isActive && (
                        <span className="absolute left-0 top-1.5 bottom-1.5 w-1 rounded-r-full gradient-accent" />
                      )}
                      <Icon
                        className={`w-4 h-4 flex-shrink-0 transition-transform duration-200 group-hover:scale-110 ${
                          isActive ? 'text-indigo-600 dark:text-white' : 'text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-200'
                        }`}
                      />
                      <span className="tracking-wide">{item.label}</span>
                    </>
                  )}
                </NavLink>
              );
            })}
        </div>

        {/* Bottom Profile Summary Card */}
        {user && (
          <div className="p-3 border-t border-slate-200 dark:border-white/[0.08] bg-slate-50/50 dark:bg-black/30">
            <div className="p-2.5 rounded-xl bg-white dark:bg-black/40 border border-slate-200 dark:border-white/[0.08] flex items-center gap-2.5 shadow-sm">
              <div className="relative">
                <div className="w-8 h-8 rounded-full gradient-accent text-white font-bold text-xs flex items-center justify-center shadow-md">
                  {user.username.charAt(0).toUpperCase()}
                </div>
                <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 rounded-full bg-emerald-500 border-2 border-white dark:border-black" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{user.username}</p>
                <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate font-mono">{user.email}</p>
              </div>
              <RoleBadge role={user.role} />
            </div>
          </div>
        )}
      </aside>
    </>
  );
};

export default Sidebar;
