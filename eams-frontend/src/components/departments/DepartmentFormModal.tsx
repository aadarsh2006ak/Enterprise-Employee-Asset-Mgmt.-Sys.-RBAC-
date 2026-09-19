import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { DepartmentResponse, EmployeeSummaryResponse } from '../../types';
import { departmentApi, employeeApi } from '../../api';
import { useToast } from '../common/Toast';

interface DepartmentFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  departmentToEdit?: DepartmentResponse | null;
}

export const DepartmentFormModal: React.FC<DepartmentFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  departmentToEdit,
}) => {
  const { success, error: showError } = useToast();
  const [name, setName] = useState('');
  const [managerId, setManagerId] = useState<number | ''>('');
  const [employees, setEmployees] = useState<EmployeeSummaryResponse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      employeeApi
        .getAllEmployees({ size: 100, status: 'ACTIVE' })
        .then((res) => setEmployees(res.content))
        .catch(() => setEmployees([]));

      if (departmentToEdit) {
        setName(departmentToEdit.name);
        setManagerId(departmentToEdit.managerId || '');
      } else {
        setName('');
        setManagerId('');
      }
    }
  }, [isOpen, departmentToEdit]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setLoading(true);
    try {
      if (departmentToEdit) {
        await departmentApi.updateDepartment(departmentToEdit.id, {
          name: name.trim(),
          managerId: managerId ? Number(managerId) : undefined,
        });
        success(`Department "${name}" updated successfully`);
      } else {
        await departmentApi.createDepartment({
          name: name.trim(),
          managerId: managerId ? Number(managerId) : undefined,
        });
        success(`Department "${name}" created successfully`);
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Department operation failed';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={departmentToEdit ? `Edit Department: ${departmentToEdit.name}` : 'Create Department'}
      subtitle="Organizational unit and team lead assignment"
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Department Name <span className="text-rose-400">*</span>
          </label>
          <input
            type="text"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Cloud Infrastructure, Marketing"
            className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Department Head / Manager
          </label>
          <select
            value={managerId}
            onChange={(e) => setManagerId(e.target.value ? Number(e.target.value) : '')}
            className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
          >
            <option value="" className="bg-slate-900 text-slate-400">
              No Assigned Manager
            </option>
            {employees.map((emp) => (
              <option key={emp.id} value={emp.id} className="bg-slate-900 text-white">
                {emp.fullName} ({emp.employeeCode})
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading || !name.trim()}
            className="px-5 py-2 text-xs font-medium rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-500/25 transition-all disabled:opacity-50"
          >
            {loading ? 'Saving...' : departmentToEdit ? 'Save Changes' : 'Create Department'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
