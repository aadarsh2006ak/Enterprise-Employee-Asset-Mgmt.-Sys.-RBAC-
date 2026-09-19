import React from 'react';
import { AssetStatus, EmployeeStatus, RoleType, AuditAction, ExportStatus } from '../../types';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info' | 'purple' | 'amber' | 'cyan';
  size?: 'sm' | 'md';
  dot?: boolean;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'default',
  size = 'md',
  dot = false,
}) => {
  const variantClasses = {
    default:
      'bg-slate-100 dark:bg-slate-800/80 text-slate-700 dark:text-slate-300 border-slate-200 dark:border-white/[0.08]',
    success:
      'bg-emerald-50 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-500/30 shadow-sm',
    warning:
      'bg-amber-50 dark:bg-amber-500/15 text-amber-800 dark:text-amber-300 border-amber-200 dark:border-amber-500/30 shadow-sm',
    danger:
      'bg-rose-50 dark:bg-rose-500/15 text-rose-700 dark:text-rose-300 border-rose-200 dark:border-rose-500/30 shadow-sm',
    info:
      'bg-sky-50 dark:bg-blue-500/15 text-sky-700 dark:text-blue-300 border-sky-200 dark:border-blue-500/30 shadow-sm',
    purple:
      'bg-indigo-50 dark:bg-indigo-500/15 text-indigo-700 dark:text-indigo-300 border-indigo-200 dark:border-indigo-500/30 shadow-sm',
    amber:
      'bg-amber-50 dark:bg-amber-500/15 text-amber-800 dark:text-amber-300 border-amber-200 dark:border-amber-500/30 shadow-sm',
    cyan:
      'bg-cyan-50 dark:bg-cyan-500/15 text-cyan-800 dark:text-cyan-300 border-cyan-200 dark:border-cyan-500/30 shadow-sm',
  };

  const dotClasses = {
    default: 'bg-slate-500 dark:bg-slate-400',
    success: 'bg-emerald-600 dark:bg-emerald-400',
    warning: 'bg-amber-600 dark:bg-amber-400',
    danger: 'bg-rose-600 dark:bg-rose-400',
    info: 'bg-sky-600 dark:bg-blue-400',
    purple: 'bg-indigo-600 dark:bg-indigo-400',
    amber: 'bg-amber-600 dark:bg-amber-400',
    cyan: 'bg-cyan-600 dark:bg-cyan-400',
  };

  const sizeClasses = {
    sm: 'text-[10px] px-2 py-0.5 tracking-wide font-bold',
    md: 'text-xs px-2.5 py-1 tracking-wide font-bold',
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 font-sans rounded-full border backdrop-blur-sm ${variantClasses[variant]} ${sizeClasses[size]}`}
    >
      {dot && (
        <span className="relative flex h-1.5 w-1.5">
          <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${dotClasses[variant]}`} />
          <span className={`relative inline-flex rounded-full h-1.5 w-1.5 ${dotClasses[variant]}`} />
        </span>
      )}
      <span>{children}</span>
    </span>
  );
};

export const AssetStatusBadge: React.FC<{ status: AssetStatus }> = ({ status }) => {
  switch (status) {
    case 'AVAILABLE':
      return <Badge variant="success" dot>Available</Badge>;
    case 'ASSIGNED':
      return <Badge variant="purple" dot>Assigned</Badge>;
    case 'UNDER_MAINTENANCE':
      return <Badge variant="warning" dot>Maintenance</Badge>;
    case 'RETIRED':
      return <Badge variant="danger">Retired</Badge>;
    default:
      return <Badge>{status}</Badge>;
  }
};

export const EmployeeStatusBadge: React.FC<{ status: EmployeeStatus }> = ({ status }) => {
  switch (status) {
    case 'ACTIVE':
      return <Badge variant="success" dot>Active</Badge>;
    case 'RESIGNED':
      return <Badge variant="warning">Resigned</Badge>;
    case 'TERMINATED':
      return <Badge variant="danger">Terminated</Badge>;
    default:
      return <Badge>{status}</Badge>;
  }
};

export const RoleBadge: React.FC<{ role: RoleType }> = ({ role }) => {
  switch (role) {
    case 'ADMIN':
      return <Badge variant="danger">Admin</Badge>;
    case 'MANAGER':
      return <Badge variant="purple">Manager</Badge>;
    case 'EMPLOYEE':
      return <Badge variant="cyan">Employee</Badge>;
    default:
      return <Badge>{role}</Badge>;
  }
};

export const AuditActionBadge: React.FC<{ action: AuditAction }> = ({ action }) => {
  switch (action) {
    case 'CREATE':
      return <Badge variant="success">CREATE</Badge>;
    case 'UPDATE':
      return <Badge variant="info">UPDATE</Badge>;
    case 'DELETE':
      return <Badge variant="danger">DELETE</Badge>;
    case 'ASSIGN':
      return <Badge variant="purple">ASSIGN</Badge>;
    case 'RETURN':
      return <Badge variant="warning">RETURN</Badge>;
    case 'STATUS_CHANGE':
      return <Badge variant="amber">STATUS</Badge>;
    case 'LOGIN':
    case 'LOGOUT':
    case 'TOKEN_REFRESH':
      return <Badge variant="default">{action}</Badge>;
    default:
      return <Badge>{action}</Badge>;
  }
};

export const ExportStatusBadge: React.FC<{ status: ExportStatus }> = ({ status }) => {
  switch (status) {
    case 'SUBMITTED':
      return <Badge variant="info" dot>Queued</Badge>;
    case 'PROCESSING':
      return <Badge variant="warning" dot>Processing</Badge>;
    case 'COMPLETED':
      return <Badge variant="success">Completed</Badge>;
    case 'FAILED':
      return <Badge variant="danger">Failed</Badge>;
    default:
      return <Badge>{status}</Badge>;
  }
};
