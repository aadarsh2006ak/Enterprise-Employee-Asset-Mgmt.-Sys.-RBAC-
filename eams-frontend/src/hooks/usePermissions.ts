import { useAuth } from '../context/AuthContext';
import { RoleType } from '../types';

export const usePermissions = () => {
  const { user, hasRole, hasPermission, hasAnyRole } = useAuth();

  return {
    user,
    role: user?.role,
    permissions: user?.permissions || [],
    isAdmin: user?.role === 'ADMIN',
    isManager: user?.role === 'MANAGER',
    isEmployee: user?.role === 'EMPLOYEE',
    hasRole: (role: RoleType | RoleType[]) => hasRole(role),
    hasPermission: (permission: string | string[]) => hasPermission(permission),
    hasAnyRole: (...roles: RoleType[]) => hasAnyRole(...roles),
  };
};
