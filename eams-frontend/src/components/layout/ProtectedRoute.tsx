import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { RoleType } from '../../types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: RoleType[];
  requiredPermissions?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles,
  requiredPermissions,
}) => {
  const { isAuthenticated, isLoading, user, hasRole, hasPermission } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950 text-slate-200">
        <div className="flex flex-col items-center gap-4">
          <div className="w-10 h-10 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
          <p className="text-sm text-slate-400 font-medium">Verifying authorization...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check role restrictions
  if (allowedRoles && allowedRoles.length > 0) {
    const isRoleAllowed = allowedRoles.some((role) => hasRole(role));
    if (!isRoleAllowed) {
      return <Navigate to="/unauthorized" replace />;
    }
  }

  // Check permission restrictions
  if (requiredPermissions && requiredPermissions.length > 0) {
    const isPermissionAllowed = requiredPermissions.some((permission) => hasPermission(permission));
    if (!isPermissionAllowed) {
      return <Navigate to="/unauthorized" replace />;
    }
  }

  return <>{children}</>;
};

export const RoleGuard: React.FC<{
  roles: RoleType[];
  fallback?: React.ReactNode;
  children: React.ReactNode;
}> = ({ roles, fallback = null, children }) => {
  const { hasRole } = useAuth();
  if (!hasRole(roles)) {
    return <>{fallback}</>;
  }
  return <>{children}</>;
};

export const PermissionGuard: React.FC<{
  permissions: string[];
  fallback?: React.ReactNode;
  children: React.ReactNode;
}> = ({ permissions, fallback = null, children }) => {
  const { hasPermission } = useAuth();
  if (!hasPermission(permissions)) {
    return <>{fallback}</>;
  }
  return <>{children}</>;
};

export default ProtectedRoute;
