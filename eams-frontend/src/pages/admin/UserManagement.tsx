import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi, AdminUserResponse, UserCreateAdminRequest } from '../../api/endpoints/users';
import { RoleType } from '../../types';
import { RoleBadge, Badge } from '../../components/common/Badge';
import { Pagination } from '../../components/common/Pagination';
import { Modal } from '../../components/common/Modal';
import { Button } from '../../components/common/Button';
import { useToast } from '../../components/common/Toast';
import { Shield, UserPlus, Search, Edit3, UserX } from 'lucide-react';

export const UserManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const { success, error: showError } = useToast();

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [search, setSearch] = useState('');

  // Modals state
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [roleUser, setRoleUser] = useState<AdminUserResponse | null>(null);
  const [newRole, setNewRole] = useState<RoleType>('EMPLOYEE');

  // Create Form State
  const [form, setForm] = useState<UserCreateAdminRequest>({
    username: '',
    email: '',
    password: '',
    role: 'EMPLOYEE',
  });
  const [createLoading, setCreateLoading] = useState(false);

  // Queries
  const { data: usersData, isLoading, refetch } = useQuery({
    queryKey: ['admin-users', page, size, search],
    queryFn: () => usersApi.getAllUsers({ page, size, search: search || undefined }),
  });

  // Role Update Mutation
  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: number; role: RoleType }) =>
      usersApi.updateUserRole(id, { role }),
    onSuccess: () => {
      success('User role updated successfully');
      setRoleUser(null);
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
    onError: (err: any) => {
      showError(err.response?.data?.message || 'Failed to update user role');
    },
  });

  // Deactivate Mutation
  const deactivateMutation = useMutation({
    mutationFn: (id: number) => usersApi.deactivateUser(id),
    onSuccess: () => {
      success('User account deactivated (soft-deleted)');
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
    onError: (err: any) => {
      showError(err.response?.data?.message || 'Failed to deactivate user');
    },
  });

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateLoading(true);
    try {
      await usersApi.createUser(form);
      success(`User account "${form.username}" created with role ${form.role}`);
      setIsCreateOpen(false);
      setForm({ username: '', email: '', password: '', role: 'EMPLOYEE' });
      refetch();
    } catch (err: any) {
      showError(err.response?.data?.message || 'Failed to create user');
    } finally {
      setCreateLoading(false);
    }
  };

  const users = usersData?.content || [];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <Shield className="w-7 h-7 text-indigo-400" />
            <span>User & Access Control Management</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Provision system accounts, manage RBAC role bindings, and audit active logins (Section 8.2)
          </p>
        </div>

        <Button
          variant="primary"
          icon={UserPlus}
          onClick={() => setIsCreateOpen(true)}
          className="self-start sm:self-auto"
        >
          Create User Account
        </Button>
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-panel p-4 rounded-2xl border border-slate-800 flex items-center justify-between">
        <div className="relative w-full max-w-sm">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            placeholder="Search username or email address..."
            className="w-full pl-10 pr-3.5 py-2 rounded-xl text-xs glass-input"
          />
        </div>
      </div>

      {/* Users Table */}
      <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
              <tr>
                <th className="py-3.5 px-4">User ID</th>
                <th className="py-3.5 px-4">Username & Email</th>
                <th className="py-3.5 px-4">Assigned Role</th>
                <th className="py-3.5 px-4">Account Status</th>
                <th className="py-3.5 px-4">Created Date</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    Loading users...
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    No users found matching search.
                  </td>
                </tr>
              ) : (
                users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-850/50 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-bold text-slate-400">#{u.id}</td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white">@{u.username}</div>
                      <div className="text-[11px] text-slate-400">{u.email}</div>
                    </td>
                    <td className="py-3.5 px-4">
                      <RoleBadge role={u.role} />
                    </td>
                    <td className="py-3.5 px-4">
                      {u.isActive ? (
                        <Badge variant="success" dot>
                          Active
                        </Badge>
                      ) : (
                        <Badge variant="danger">Deactivated</Badge>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {new Date(u.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => {
                            setRoleUser(u);
                            setNewRole(u.role);
                          }}
                          title="Change Role"
                          className="p-1.5 rounded-lg bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-400 transition-colors"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                        {u.isActive && u.username !== 'admin' && (
                          <button
                            onClick={() => {
                              if (
                                window.confirm(
                                  `Are you sure you want to deactivate account "@${u.username}"?`
                                )
                              ) {
                                deactivateMutation.mutate(u.id);
                              }
                            }}
                            title="Deactivate User (Soft Delete)"
                            className="p-1.5 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 transition-colors"
                          >
                            <UserX className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {usersData && (
          <Pagination
            pageNumber={usersData.pageNumber}
            totalPages={usersData.totalPages}
            totalElements={usersData.totalElements}
            pageSize={size}
            onPageChange={(newPage) => setPage(newPage)}
            onPageSizeChange={(newSize) => {
              setSize(newSize);
              setPage(0);
            }}
          />
        )}
      </div>

      {/* Create User Modal */}
      <Modal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        title="Create System User Account"
        subtitle="Provision a login record with assigned RBAC role"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Username <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              required
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              placeholder="e.g. jsmith"
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Email Address <span className="text-rose-400">*</span>
            </label>
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="e.g. jsmith@company.com"
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Password <span className="text-rose-400">*</span>
            </label>
            <input
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="Minimum 8 characters"
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Assigned Role <span className="text-rose-400">*</span>
            </label>
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value as RoleType })}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            >
              <option value="EMPLOYEE" className="bg-slate-900">
                EMPLOYEE (Self-service access)
              </option>
              <option value="MANAGER" className="bg-slate-900">
                MANAGER (Team lead & departmental assets)
              </option>
              <option value="ADMIN" className="bg-slate-900">
                ADMIN (Full enterprise privileges)
              </option>
            </select>
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
            <Button variant="secondary" onClick={() => setIsCreateOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={createLoading}>
              Create Account
            </Button>
          </div>
        </form>
      </Modal>

      {/* Change Role Modal */}
      <Modal
        isOpen={!!roleUser}
        onClose={() => setRoleUser(null)}
        title={`Change Role for @${roleUser?.username}`}
        subtitle="Update RBAC authorization level"
        maxWidth="md"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Select New RBAC Role
            </label>
            <select
              value={newRole}
              onChange={(e) => setNewRole(e.target.value as RoleType)}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            >
              <option value="EMPLOYEE" className="bg-slate-900">EMPLOYEE</option>
              <option value="MANAGER" className="bg-slate-900">MANAGER</option>
              <option value="ADMIN" className="bg-slate-900">ADMIN</option>
            </select>
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
            <Button variant="secondary" onClick={() => setRoleUser(null)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              loading={roleMutation.isPending}
              onClick={() => {
                if (roleUser) {
                  roleMutation.mutate({ id: roleUser.id, role: newRole });
                }
              }}
            >
              Update Role
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
