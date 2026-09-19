import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { employeeApi, departmentApi } from '../../api';
import { EmployeeSummaryResponse, EmployeeResponse, EmployeeStatus } from '../../types';
import { EmployeeStatusBadge } from '../../components/common/Badge';
import { Pagination } from '../../components/common/Pagination';
import { EmployeeFormModal } from '../../components/employees/EmployeeFormModal';
import { EmployeeStatusModal } from '../../components/employees/EmployeeStatusModal';
import { EmployeeDetailsModal } from '../../components/employees/EmployeeDetailsModal';
import { useToast } from '../../components/common/Toast';
import {
  Users,
  UserPlus,
  Search,
  Building,
  Edit2,
  Trash2,
  Eye,
  ShieldAlert,
} from 'lucide-react';

export const EmployeeList: React.FC = () => {
  const queryClient = useQueryClient();
  const { hasPermission, hasRole } = useAuth();
  const { success, error: showError } = useToast();

  // Filters and Pagination
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [search, setSearch] = useState('');
  const [departmentId, setDepartmentId] = useState<number | ''>('');
  const [status, setStatus] = useState<EmployeeStatus | ''>('');

  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [employeeToEdit, setEmployeeToEdit] = useState<EmployeeResponse | null>(null);
  const [statusEmployee, setStatusEmployee] = useState<EmployeeSummaryResponse | null>(null);
  const [detailsEmployeeId, setDetailsEmployeeId] = useState<number | null>(null);

  // Queries
  const { data: departments = [] } = useQuery({
    queryKey: ['departments-list'],
    queryFn: () => departmentApi.getAllDepartmentsList(),
  });

  const { data: employeesData, isLoading, refetch } = useQuery({
    queryKey: ['employees', page, size, departmentId, status, search],
    queryFn: () =>
      employeeApi.getAllEmployees({
        page,
        size,
        departmentId: departmentId ? Number(departmentId) : undefined,
        status: status ? (status as EmployeeStatus) : undefined,
        search: search || undefined,
      }),
  });

  // Delete Mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => employeeApi.deleteEmployee(id),
    onSuccess: () => {
      success('Employee profile and linked user account deleted');
      queryClient.invalidateQueries({ queryKey: ['employees'] });
    },
    onError: (err: any) => {
      showError(err.response?.data?.message || 'Failed to delete employee');
    },
  });

  const handleDelete = (emp: EmployeeSummaryResponse) => {
    if (
      window.confirm(
        `Are you sure you want to permanently delete employee "${emp.fullName}" and their linked user account?`
      )
    ) {
      deleteMutation.mutate(emp.id);
    }
  };

  const handleOpenEdit = async (id: number) => {
    try {
      const fullDetails = await employeeApi.getEmployeeById(id);
      setEmployeeToEdit(fullDetails);
      setIsFormOpen(true);
    } catch {
      showError('Failed to fetch full employee profile for editing');
    }
  };

  const employees = employeesData?.content || [];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <Users className="w-7 h-7 text-indigo-400" />
            <span>Employee Directory & Lifecycle</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Manage company staff, department associations, and system login access
          </p>
        </div>

        {hasPermission('EMPLOYEE_CREATE') && (
          <button
            onClick={() => {
              setEmployeeToEdit(null);
              setIsFormOpen(true);
            }}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-500/25 transition-all self-start sm:self-auto"
          >
            <UserPlus className="w-4 h-4" />
            <span>Provision New Employee</span>
          </button>
        )}
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-panel p-4 rounded-2xl border border-slate-800 flex flex-col md:flex-row gap-3 items-center justify-between">
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            placeholder="Search name, code, email..."
            className="w-full pl-10 pr-3.5 py-2 rounded-xl text-xs glass-input"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
          <select
            value={departmentId}
            onChange={(e) => {
              setDepartmentId(e.target.value ? Number(e.target.value) : '');
              setPage(0);
            }}
            className="px-3 py-2 rounded-xl text-xs glass-input bg-slate-900"
          >
            <option value="">All Departments</option>
            {departments.map((dept) => (
              <option key={dept.id} value={dept.id}>
                {dept.name}
              </option>
            ))}
          </select>

          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as EmployeeStatus | '');
              setPage(0);
            }}
            className="px-3 py-2 rounded-xl text-xs glass-input bg-slate-900"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="RESIGNED">Resigned</option>
            <option value="TERMINATED">Terminated</option>
          </select>
        </div>
      </div>

      {/* Table Section */}
      <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
              <tr>
                <th className="py-3.5 px-4">Employee Code</th>
                <th className="py-3.5 px-4">Full Name & Email</th>
                <th className="py-3.5 px-4">Department & Designation</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Joined Date</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center gap-3">
                      <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
                      <span>Loading employee directory...</span>
                    </div>
                  </td>
                </tr>
              ) : employees.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    No matching employees found in directory.
                  </td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <tr key={emp.id} className="hover:bg-slate-850/50 transition-colors group">
                    <td className="py-3.5 px-4 font-mono font-bold text-indigo-300">
                      {emp.employeeCode}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white">{emp.fullName}</div>
                      <div className="text-[11px] text-slate-400">{emp.email}</div>
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="text-white font-medium flex items-center gap-1.5">
                        <Building className="w-3.5 h-3.5 text-slate-500" />
                        <span>{emp.departmentName || 'No Department'}</span>
                      </div>
                      <div className="text-[11px] text-slate-400">
                        {emp.designation || 'Staff'}
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <EmployeeStatusBadge status={emp.status} />
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {emp.dateOfJoining ? new Date(emp.dateOfJoining).toLocaleDateString() : '—'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {/* Details View */}
                        <button
                          onClick={() => setDetailsEmployeeId(emp.id)}
                          title="View Profile Details"
                          className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
                        >
                          <Eye className="w-4 h-4" />
                        </button>

                        {/* Status Change Action (Admin Only) */}
                        {hasPermission('EMPLOYEE_STATUS_CHANGE') && (
                          <button
                            onClick={() => setStatusEmployee(emp)}
                            title="Update Status / Lockout"
                            className="p-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 transition-colors"
                          >
                            <ShieldAlert className="w-4 h-4" />
                          </button>
                        )}

                        {/* Edit Profile */}
                        {hasPermission('EMPLOYEE_UPDATE') && (
                          <button
                            onClick={() => handleOpenEdit(emp.id)}
                            title="Edit Profile"
                            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                        )}

                        {/* Delete (Admin Only) */}
                        {hasRole('ADMIN') && (
                          <button
                            onClick={() => handleDelete(emp)}
                            title="Delete Employee"
                            className="p-1.5 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 transition-colors"
                          >
                            <Trash2 className="w-4 h-4" />
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

        {employeesData && (
          <Pagination
            pageNumber={employeesData.pageNumber}
            totalPages={employeesData.totalPages}
            totalElements={employeesData.totalElements}
            pageSize={size}
            onPageChange={(newPage) => setPage(newPage)}
            onPageSizeChange={(newSize) => {
              setSize(newSize);
              setPage(0);
            }}
          />
        )}
      </div>

      {/* Modals */}
      <EmployeeFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSuccess={() => refetch()}
        employeeToEdit={employeeToEdit}
      />

      <EmployeeStatusModal
        isOpen={!!statusEmployee}
        onClose={() => setStatusEmployee(null)}
        onSuccess={() => refetch()}
        employee={statusEmployee}
      />

      <EmployeeDetailsModal
        isOpen={!!detailsEmployeeId}
        onClose={() => setDetailsEmployeeId(null)}
        employeeId={detailsEmployeeId}
      />
    </div>
  );
};

export default EmployeeList;
