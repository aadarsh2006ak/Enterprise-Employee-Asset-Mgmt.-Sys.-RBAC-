import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { EmployeeResponse } from '../../types';
import { employeeApi } from '../../api';
import { EmployeeStatusBadge } from '../common/Badge';
import { Mail, Briefcase, Building, Calendar } from 'lucide-react';

interface EmployeeDetailsModalProps {
  isOpen: boolean;
  onClose: () => void;
  employeeId: number | null;
}

export const EmployeeDetailsModal: React.FC<EmployeeDetailsModalProps> = ({
  isOpen,
  onClose,
  employeeId,
}) => {
  const [employee, setEmployee] = useState<EmployeeResponse | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && employeeId) {
      setLoading(true);
      employeeApi
        .getEmployeeById(employeeId)
        .then((data) => setEmployee(data))
        .catch(() => setEmployee(null))
        .finally(() => setLoading(false));
    }
  }, [isOpen, employeeId]);

  if (!employeeId) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Employee Profile Details"
      subtitle="Complete organizational metadata and account profile"
      maxWidth="lg"
    >
      {loading ? (
        <div className="py-12 flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading profile...</p>
        </div>
      ) : !employee ? (
        <div className="py-8 text-center text-slate-400 text-sm">Failed to load profile.</div>
      ) : (
        <div className="space-y-4">
          {/* Header Card */}
          <div className="p-4 rounded-xl bg-slate-850 border border-slate-700/60 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-600 text-white font-bold text-lg flex items-center justify-center">
                {employee.fullName.charAt(0)}
              </div>
              <div>
                <h4 className="text-base font-bold text-white">{employee.fullName}</h4>
                <p className="text-xs text-slate-400 font-mono">{employee.employeeCode}</p>
              </div>
            </div>
            <EmployeeStatusBadge status={employee.status} />
          </div>

          {/* Key Details Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <div className="flex items-center gap-1.5 text-slate-400">
                <Mail className="w-3.5 h-3.5 text-indigo-400" />
                <span>Email & Username</span>
              </div>
              <p className="text-white font-medium truncate">{employee.email}</p>
              <p className="text-[11px] text-slate-400 font-mono">@{employee.username}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <div className="flex items-center gap-1.5 text-slate-400">
                <Building className="w-3.5 h-3.5 text-indigo-400" />
                <span>Department</span>
              </div>
              <p className="text-white font-medium">{employee.departmentName || 'Not Assigned'}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <div className="flex items-center gap-1.5 text-slate-400">
                <Briefcase className="w-3.5 h-3.5 text-indigo-400" />
                <span>Designation</span>
              </div>
              <p className="text-white font-medium">{employee.designation || 'Staff'}</p>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1">
              <div className="flex items-center gap-1.5 text-slate-400">
                <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                <span>Date of Joining</span>
              </div>
              <p className="text-white font-medium">
                {employee.dateOfJoining ? new Date(employee.dateOfJoining).toLocaleDateString() : 'N/A'}
              </p>
            </div>
          </div>

          <div className="text-[11px] text-slate-500 pt-2 border-t border-slate-800 flex justify-between">
            <span>Created: {new Date(employee.createdAt).toLocaleDateString()}</span>
            <span>Version: {employee.version}</span>
          </div>

          <div className="flex justify-end pt-2">
            <button
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </Modal>
  );
};
