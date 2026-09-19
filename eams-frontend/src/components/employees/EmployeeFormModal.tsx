import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import {
  EmployeeResponse,
  DepartmentResponse,
  RoleType,
} from '../../types';
import { employeeApi, departmentApi } from '../../api';
import { useToast } from '../common/Toast';

interface EmployeeFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  employeeToEdit?: EmployeeResponse | null;
}

export const EmployeeFormModal: React.FC<EmployeeFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  employeeToEdit,
}) => {
  const { success, error: showError } = useToast();
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [loading, setLoading] = useState(false);

  // Form Fields for Create
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<RoleType>('EMPLOYEE');

  // Shared Fields
  const [fullName, setFullName] = useState('');
  const [departmentId, setDepartmentId] = useState<number | ''>('');
  const [designation, setDesignation] = useState('');
  const [dateOfJoining, setDateOfJoining] = useState('');

  useEffect(() => {
    if (isOpen) {
      departmentApi
        .getAllDepartmentsList()
        .then((data) => setDepartments(data))
        .catch(() => setDepartments([]));

      if (employeeToEdit) {
        setFullName(employeeToEdit.fullName);
        setDepartmentId(employeeToEdit.departmentId || '');
        setDesignation(employeeToEdit.designation || '');
        setDateOfJoining(employeeToEdit.dateOfJoining || '');
      } else {
        setUsername('');
        setEmail('');
        setPassword('');
        setRole('EMPLOYEE');
        setFullName('');
        setDepartmentId('');
        setDesignation('');
        setDateOfJoining(new Date().toISOString().split('T')[0]);
      }
    }
  }, [isOpen, employeeToEdit]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (employeeToEdit) {
        await employeeApi.updateEmployee(employeeToEdit.id, {
          fullName,
          departmentId: departmentId ? Number(departmentId) : undefined,
          designation: designation || undefined,
          dateOfJoining: dateOfJoining || undefined,
          version: employeeToEdit.version,
        });
        success(`Employee profile for "${fullName}" updated`);
      } else {
        await employeeApi.createEmployee({
          username,
          email,
          password,
          role,
          fullName,
          departmentId: departmentId ? Number(departmentId) : undefined,
          designation: designation || undefined,
          dateOfJoining: dateOfJoining || undefined,
        });
        success(`Employee "${fullName}" and system user "${username}" created`);
      }
      onSuccess();
      onClose();
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Operation failed';
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={employeeToEdit ? `Edit Employee (${employeeToEdit.employeeCode})` : 'Provision New Employee & User Account'}
      subtitle={
        employeeToEdit
          ? 'Update organizational details and designations'
          : 'Creates linked user authentication record and employee profile'
      }
      maxWidth="2xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* User Account Credentials Section (Only on Create) */}
        {!employeeToEdit && (
          <div className="p-4 rounded-xl bg-slate-850 border border-slate-700/60 space-y-3">
            <h4 className="text-xs font-semibold text-indigo-300 uppercase tracking-wider">
              1. Authentication Credentials
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Username <span className="text-rose-400">*</span>
                </label>
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="e.g. jdoe"
                  className="w-full px-3 py-1.5 rounded-lg text-sm glass-input"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Email Address <span className="text-rose-400">*</span>
                </label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="e.g. jdoe@company.com"
                  className="w-full px-3 py-1.5 rounded-lg text-sm glass-input"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Initial Password <span className="text-rose-400">*</span>
                </label>
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Minimum 8 characters"
                  className="w-full px-3 py-1.5 rounded-lg text-sm glass-input"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  System Role <span className="text-rose-400">*</span>
                </label>
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value as RoleType)}
                  className="w-full px-3 py-1.5 rounded-lg text-sm glass-input"
                >
                  <option value="EMPLOYEE" className="bg-slate-900">EMPLOYEE (Standard)</option>
                  <option value="MANAGER" className="bg-slate-900">MANAGER (Team Lead)</option>
                  <option value="ADMIN" className="bg-slate-900">ADMIN (Full Access)</option>
                </select>
              </div>
            </div>
          </div>
        )}

        {/* Profile Information Section */}
        <div className="space-y-3">
          {!employeeToEdit && (
            <h4 className="text-xs font-semibold text-indigo-300 uppercase tracking-wider">
              2. Employee Profile Details
            </h4>
          )}

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Full Name <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="e.g. Jane Doe"
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Department
              </label>
              <select
                value={departmentId}
                onChange={(e) => setDepartmentId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
              >
                <option value="" className="bg-slate-900 text-slate-400">
                  Select Department...
                </option>
                {departments.map((dept) => (
                  <option key={dept.id} value={dept.id} className="bg-slate-900 text-white">
                    {dept.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">
                Designation / Title
              </label>
              <input
                type="text"
                value={designation}
                onChange={(e) => setDesignation(e.target.value)}
                placeholder="e.g. Senior DevOps Engineer"
                className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1">
              Date of Joining
            </label>
            <input
              type="date"
              value={dateOfJoining}
              onChange={(e) => setDateOfJoining(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>
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
            disabled={loading}
            className="px-5 py-2 text-xs font-medium rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-500/25 transition-all disabled:opacity-50"
          >
            {loading ? 'Saving...' : employeeToEdit ? 'Save Changes' : 'Provision Employee'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
