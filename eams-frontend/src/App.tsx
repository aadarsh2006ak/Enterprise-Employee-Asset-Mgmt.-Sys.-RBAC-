import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, Layout } from './components/layout';

// Pages matching Blueprint Structure (Section 14.2)
import { Login } from './pages/auth/Login';
import { Dashboard } from './pages/dashboard/Dashboard';
import { AssetList } from './pages/assets/AssetList';
import { MyAssets } from './pages/assets/MyAssets';
import { EmployeeList } from './pages/employees/EmployeeList';
import { DepartmentList } from './pages/departments/DepartmentList';
import { AuditLogViewer } from './pages/audit/AuditLogViewer';
import { ExportManager } from './pages/exports/ExportManager';
import { UserManagement } from './pages/admin/UserManagement';
import { RoleManagement } from './pages/admin/RoleManagement';
import { Unauthorized } from './pages/common/Unauthorized';
import { NotFound } from './pages/common/NotFound';

export const App: React.FC = () => {
  return (
    <Routes>
      {/* Public Route */}
      <Route path="/login" element={<Login />} />

      {/* Authenticated Layout Routes */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard */}
        <Route index element={<Dashboard />} />

        {/* Assets (Admin & Manager, or ASSET_READ permission) */}
        <Route
          path="assets"
          element={
            <ProtectedRoute requiredPermissions={['ASSET_READ']}>
              <AssetList />
            </ProtectedRoute>
          }
        />

        {/* Employee Self-Service My Assets (All Authenticated Users) */}
        <Route path="my-assets" element={<MyAssets />} />

        {/* Employees (Admin & Manager, or EMPLOYEE_READ permission) */}
        <Route
          path="employees"
          element={
            <ProtectedRoute requiredPermissions={['EMPLOYEE_READ']}>
              <EmployeeList />
            </ProtectedRoute>
          }
        />

        {/* Departments (All Authenticated Users) */}
        <Route path="departments" element={<DepartmentList />} />

        {/* Audit Logs (Admin & Manager, or AUDIT_READ permission) */}
        <Route
          path="audit-logs"
          element={
            <ProtectedRoute requiredPermissions={['AUDIT_READ']}>
              <AuditLogViewer />
            </ProtectedRoute>
          }
        />

        {/* Export Center (Admin & Manager, or EXPORT_DATA permission) */}
        <Route
          path="exports"
          element={
            <ProtectedRoute requiredPermissions={['EXPORT_DATA']}>
              <ExportManager />
            </ProtectedRoute>
          }
        />

        {/* Admin Management (Section 8.2 & 14.2) */}
        <Route
          path="admin/users"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <UserManagement />
            </ProtectedRoute>
          }
        />

        <Route
          path="admin/roles"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <RoleManagement />
            </ProtectedRoute>
          }
        />

        {/* Access Denied */}
        <Route path="unauthorized" element={<Unauthorized />} />
      </Route>

      {/* 404 Catch-All */}
      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  );
};

export default App;
