import React from 'react';
import { Shield, Check, X } from 'lucide-react';

export const RoleManagement: React.FC = () => {
  const permissionsMatrix = [
    {
      action: 'Create/Delete User',
      admin: true,
      manager: false,
      employee: false,
      code: 'USER_CREATE / USER_DELETE',
    },
    {
      action: 'Assign Role',
      admin: true,
      manager: false,
      employee: false,
      code: 'USER_UPDATE',
    },
    {
      action: 'View All Employees',
      admin: true,
      manager: false,
      employee: false,
      code: 'EMPLOYEE_READ',
    },
    {
      action: 'View Own Department Employees',
      admin: true,
      manager: true,
      employee: false,
      code: 'EMPLOYEE_READ',
    },
    {
      action: 'View Own Profile',
      admin: true,
      manager: true,
      employee: true,
      code: 'EMPLOYEE_READ / Self',
    },
    {
      action: 'Create / Register Asset',
      admin: true,
      manager: false,
      employee: false,
      code: 'ASSET_CREATE',
    },
    {
      action: 'Assign Asset to Employee',
      admin: true,
      manager: true,
      employee: false,
      code: 'ASSET_ASSIGN',
    },
    {
      action: 'Raise Asset Request',
      admin: true,
      manager: true,
      employee: true,
      code: 'ASSET_REQUEST_CREATE',
    },
    {
      action: 'Approve Asset Request',
      admin: true,
      manager: true,
      employee: false,
      code: 'ASSET_REQUEST_REVIEW',
    },
    {
      action: 'View Audit Logs',
      admin: true,
      manager: true,
      employee: false,
      code: 'AUDIT_READ',
    },
    {
      action: 'Export CSV/Excel',
      admin: true,
      manager: true,
      employee: false,
      code: 'EXPORT_DATA',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
          <Shield className="w-7 h-7 text-indigo-400" />
          <span>Role-Based Access Control (RBAC) Matrix</span>
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          Permission boundaries enforced at Spring Security method level (@PreAuthorize) and API gateway
        </p>
      </div>

      <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
              <tr>
                <th className="py-3.5 px-4">Action / Feature</th>
                <th className="py-3.5 px-4">Granular Permission Code</th>
                <th className="py-3.5 px-4 text-center">Admin</th>
                <th className="py-3.5 px-4 text-center">Manager</th>
                <th className="py-3.5 px-4 text-center">Employee</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {permissionsMatrix.map((row, i) => (
                <tr key={i} className="hover:bg-slate-850/50 transition-colors">
                  <td className="py-3.5 px-4 font-semibold text-white">{row.action}</td>
                  <td className="py-3.5 px-4 font-mono text-[11px] text-slate-400">{row.code}</td>
                  <td className="py-3.5 px-4 text-center">
                    {row.admin ? (
                      <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-emerald-500/10 text-emerald-400">
                        <Check className="w-3.5 h-3.5" />
                      </span>
                    ) : (
                      <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-rose-500/10 text-rose-400">
                        <X className="w-3.5 h-3.5" />
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 px-4 text-center">
                    {row.manager ? (
                      <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-emerald-500/10 text-emerald-400">
                        <Check className="w-3.5 h-3.5" />
                      </span>
                    ) : (
                      <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-rose-500/10 text-rose-400">
                        <X className="w-3.5 h-3.5" />
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 px-4 text-center">
                    {row.employee ? (
                      <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-emerald-500/10 text-emerald-400">
                        <Check className="w-3.5 h-3.5" />
                      </span>
                    ) : (
                      <span className="inline-flex items-center justify-center w-6 h-6 rounded-full bg-rose-500/10 text-rose-400">
                        <X className="w-3.5 h-3.5" />
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
