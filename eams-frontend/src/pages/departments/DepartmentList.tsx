import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { departmentApi } from '../../api';
import { DepartmentResponse } from '../../types';
import { DepartmentFormModal } from '../../components/departments/DepartmentFormModal';
import { useToast } from '../../components/common/Toast';
import { Building2, Plus, Edit2, Trash2, Users, User } from 'lucide-react';

export const DepartmentList: React.FC = () => {
  const queryClient = useQueryClient();
  const { hasPermission } = useAuth();
  const { success, error: showError } = useToast();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [departmentToEdit, setDepartmentToEdit] = useState<DepartmentResponse | null>(null);

  const { data: departments = [], isLoading, refetch } = useQuery({
    queryKey: ['departments-all'],
    queryFn: () => departmentApi.getAllDepartmentsList(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => departmentApi.deleteDepartment(id),
    onSuccess: () => {
      success('Department deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['departments-all'] });
    },
    onError: (err: any) => {
      showError(err.response?.data?.message || 'Failed to delete department (ensure no employees assigned)');
    },
  });

  const handleDelete = (dept: DepartmentResponse) => {
    if (
      window.confirm(
        `Are you sure you want to delete department "${dept.name}"? Only empty departments can be deleted.`
      )
    ) {
      deleteMutation.mutate(dept.id);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <Building2 className="w-7 h-7 text-indigo-400" />
            <span>Organizational Departments</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Functional divisions, assigned managers, and staff headcounts
          </p>
        </div>

        {hasPermission('DEPARTMENT_MANAGE') && (
          <button
            onClick={() => {
              setDepartmentToEdit(null);
              setIsFormOpen(true);
            }}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-500/25 transition-all self-start sm:self-auto"
          >
            <Plus className="w-4 h-4" />
            <span>Create Department</span>
          </button>
        )}
      </div>

      {/* Grid of Department Cards */}
      {isLoading ? (
        <div className="py-16 flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading departments...</p>
        </div>
      ) : departments.length === 0 ? (
        <div className="glass-panel rounded-3xl p-12 text-center border border-slate-800 space-y-3">
          <Building2 className="w-10 h-10 text-slate-500 mx-auto" />
          <h3 className="text-base font-semibold text-white">No Departments Registered</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            Click "Create Department" to set up your first organizational business unit.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {departments.map((dept) => (
            <div
              key={dept.id}
              className="glass-panel glass-panel-hover rounded-2xl p-6 border border-slate-800 flex flex-col justify-between space-y-4"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center">
                      <Building2 className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-base font-bold text-white">{dept.name}</h3>
                      <span className="text-[11px] text-slate-500 font-mono">ID: #{dept.id}</span>
                    </div>
                  </div>

                  {hasPermission('DEPARTMENT_MANAGE') && (
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => {
                          setDepartmentToEdit(dept);
                          setIsFormOpen(true);
                        }}
                        title="Edit Department"
                        className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDelete(dept)}
                        title="Delete Department"
                        className="p-1.5 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 transition-colors"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  )}
                </div>

                <div className="mt-4 space-y-2 text-xs">
                  <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900 border border-slate-800">
                    <div className="flex items-center gap-2 text-slate-400">
                      <User className="w-4 h-4 text-indigo-400" />
                      <span>Department Head</span>
                    </div>
                    <span className="font-semibold text-white">
                      {dept.managerName || <span className="text-slate-500 italic">None</span>}
                    </span>
                  </div>

                  <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900 border border-slate-800">
                    <div className="flex items-center gap-2 text-slate-400">
                      <Users className="w-4 h-4 text-emerald-400" />
                      <span>Employee Headcount</span>
                    </div>
                    <span className="font-bold text-emerald-300 px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
                      {dept.employeeCount ?? 0} members
                    </span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Department Modal */}
      <DepartmentFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSuccess={() => refetch()}
        departmentToEdit={departmentToEdit}
      />
    </div>
  );
};

export default DepartmentList;
