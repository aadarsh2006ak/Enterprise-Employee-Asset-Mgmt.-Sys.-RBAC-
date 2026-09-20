import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { assetApi, employeeApi, departmentApi, auditApi } from '../../api';
import { StatsCard } from '../../components/common/StatsCard';
import {
  Boxes,
  CheckCircle2,
  Clock,
  Wrench,
  Users,
  Building2,
  Plus,
  ArrowUpRight,
  Activity,
  Sparkles,
  KeyRound,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { AuditActionBadge, RoleBadge } from '../../components/common/Badge';

export const Dashboard: React.FC = () => {
  const { user, hasPermission, hasAnyRole } = useAuth();

  // Queries for stats
  const { data: assetsData, isLoading: loadingAssets } = useQuery({
    queryKey: ['dashboard-assets'],
    queryFn: () => assetApi.getAllAssets({ size: 100 }),
    enabled: hasPermission('ASSET_READ') || hasAnyRole('ADMIN', 'MANAGER'),
  });

  const { data: myAssetsData } = useQuery({
    queryKey: ['my-assets'],
    queryFn: () => assetApi.getMyActiveAssets(),
  });

  const { data: employeesData } = useQuery({
    queryKey: ['dashboard-employees'],
    queryFn: () => employeeApi.getAllEmployees({ size: 1 }),
    enabled: hasPermission('EMPLOYEE_READ') || hasAnyRole('ADMIN', 'MANAGER'),
  });

  const { data: departmentsData } = useQuery({
    queryKey: ['dashboard-departments'],
    queryFn: () => departmentApi.getAllDepartments({ size: 1 }),
  });

  const { data: auditLogsData } = useQuery({
    queryKey: ['dashboard-audit'],
    queryFn: () => auditApi.getAllAuditLogs({ size: 5 }),
    enabled: hasPermission('AUDIT_READ') || hasAnyRole('ADMIN', 'MANAGER'),
  });

  // Calculate asset counts
  const allAssets = assetsData?.content || [];
  const totalAssets = assetsData?.totalElements || 0;
  const availableCount = allAssets.filter((a) => a.status === 'AVAILABLE').length;
  const assignedCount = allAssets.filter((a) => a.status === 'ASSIGNED').length;
  const maintenanceCount = allAssets.filter((a) => a.status === 'UNDER_MAINTENANCE').length;

  return (
    <div className="space-y-8 font-sans">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden p-6 sm:p-8 rounded-3xl glass-panel gradient-hero-banner shadow-glass">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 relative z-10">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="text-[10px] text-indigo-600 dark:text-emerald-300 font-extrabold tracking-widest uppercase flex items-center gap-1">
                <Sparkles className="w-3 h-3" /> System Active
              </span>
              <RoleBadge role={user?.role || 'EMPLOYEE'} />
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-white tracking-tight">
              Welcome, <span className="gradient-accent-text">{user?.username}</span> 👋
            </h1>
            <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-300 mt-1.5 max-w-xl leading-relaxed">
              {user?.role === 'ADMIN'
                ? 'Full enterprise administration active. Manage role-based access, hardware lifecycles, user allocations, and tamper-evident audit trails.'
                : user?.role === 'MANAGER'
                ? 'Department management portal active. Manage team hardware inventory, approve allocations, and download real-time telemetry exports.'
                : 'Employee self-service dashboard. Inspect your assigned devices, request hardware, and review profile credentials.'}
            </p>
          </div>

          <div className="flex items-center gap-3 flex-shrink-0">
            {hasPermission('ASSET_CREATE') && (
              <Link
                to="/assets"
                className="inline-flex items-center justify-center gap-2 px-4 py-2.5 h-10 rounded-xl btn-theme-primary text-xs font-bold whitespace-nowrap transition-all active:scale-[0.98] shadow-sm hover:shadow-glow"
              >
                <Plus className="w-4 h-4 flex-shrink-0" />
                <span>Catalog New Asset</span>
              </Link>
            )}
            <Link
              to="/my-assets"
              className="inline-flex items-center justify-center gap-2 px-4 py-2.5 h-10 rounded-xl bg-slate-100 dark:bg-black/40 hover:bg-slate-200 dark:hover:bg-black/60 border border-slate-300 dark:border-white/[0.12] text-slate-800 dark:text-slate-200 text-xs font-bold whitespace-nowrap transition-all shadow-sm active:scale-[0.98]"
            >
              <span>My Assets ({myAssetsData?.length || 0})</span>
              <ArrowUpRight className="w-4 h-4 text-slate-500 dark:text-slate-400 flex-shrink-0" />
            </Link>
          </div>
        </div>

        {/* Decorative background aura blur */}
        <div className="absolute -top-10 -right-10 w-96 h-96 bg-indigo-500/10 dark:bg-white/[0.04] rounded-full blur-3xl pointer-events-none" />
      </div>

      {/* KPI Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatsCard
          title="Total Assets"
          value={loadingAssets ? '...' : totalAssets}
          subtitle="Inventory Catalog"
          icon={Boxes}
          iconColor="indigo"
        />

        <StatsCard
          title="In Stock"
          value={loadingAssets ? '...' : availableCount}
          subtitle="Available for allocation"
          icon={CheckCircle2}
          iconColor="emerald"
        />

        <StatsCard
          title="Active Assignments"
          value={loadingAssets ? '...' : assignedCount}
          subtitle="Deployed with employees"
          icon={Clock}
          iconColor="cyan"
        />

        <StatsCard
          title="Under Maintenance"
          value={loadingAssets ? '...' : maintenanceCount}
          subtitle="Repairs & servicing"
          icon={Wrench}
          iconColor="amber"
        />
      </div>

      {/* Secondary Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
        <StatsCard
          title="Registered Employees"
          value={employeesData?.totalElements ?? '—'}
          subtitle="Total workforce records"
          icon={Users}
          iconColor="purple"
        />

        <StatsCard
          title="Org Departments"
          value={departmentsData?.totalElements ?? '—'}
          subtitle="Operational divisions"
          icon={Building2}
          iconColor="emerald"
        />
      </div>

      {/* Bottom Section: Recent Activities / Audit Trail */}
      {hasPermission('AUDIT_READ') && auditLogsData && auditLogsData.content.length > 0 && (
        <div className="glass-panel rounded-3xl p-6 border shadow-glass space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-xl gradient-accent text-white shadow-sm">
                <Activity className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-slate-900 dark:text-white tracking-wide">Security Audit Feed</h3>
                <p className="text-[11px] text-slate-500 dark:text-slate-400">Real-time tamper-evident system transactions</p>
              </div>
            </div>
            <Link
              to="/audit-logs"
              className="text-xs font-bold text-indigo-600 dark:text-emerald-400 hover:text-indigo-500 dark:hover:text-emerald-300 flex items-center gap-1 transition-colors"
            >
              <span>View full log</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="overflow-x-auto rounded-2xl border border-slate-200 dark:border-white/[0.08] bg-slate-50/50 dark:bg-black/40">
            <table className="w-full text-left text-xs">
              <thead className="border-b border-slate-200 dark:border-white/[0.08] text-slate-500 dark:text-slate-400 uppercase tracking-widest font-bold text-[10px] bg-slate-100 dark:bg-black/40">
                <tr>
                  <th className="py-3 px-4">Action</th>
                  <th className="py-3 px-4">Entity</th>
                  <th className="py-3 px-4">Operator</th>
                  <th className="py-3 px-4">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-white/[0.05]">
                {auditLogsData.content.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-100 dark:hover:bg-white/[0.04] transition-colors">
                    <td className="py-3 px-4">
                      <AuditActionBadge action={log.action} />
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-900 dark:text-white">
                      {log.entityName} <span className="text-slate-500 dark:text-slate-400 font-mono">#{log.entityId ?? '—'}</span>
                    </td>
                    <td className="py-3 px-4 text-slate-700 dark:text-slate-300 font-mono text-[11px]">
                      {log.usernameSnapshot || (log.userId ? `User #${log.userId}` : 'System')}
                    </td>
                    <td className="py-3 px-4 text-slate-500 dark:text-slate-400 font-mono text-[11px]">
                      {new Date(log.createdAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Role Permissions Card */}
      <div className="glass-panel rounded-3xl p-6 border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-glass">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-2xl gradient-accent text-white shadow-md">
            <KeyRound className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900 dark:text-white">RBAC Security Clearances</h4>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Role: <strong className="text-slate-900 dark:text-white">{user?.role}</strong> • Granted:{' '}
              <span className="font-mono text-indigo-600 dark:text-emerald-400 font-bold">{user?.permissions?.length || 0}</span> permissions
            </p>
          </div>
        </div>

        <div className="flex flex-wrap gap-1.5 max-w-lg">
          {(user?.permissions || []).slice(0, 8).map((perm) => (
            <span
              key={perm}
              className="text-[10px] font-mono px-2.5 py-1 rounded-lg bg-slate-100 dark:bg-black/40 border border-slate-200 dark:border-white/[0.1] text-slate-700 dark:text-slate-300 shadow-sm"
            >
              {perm}
            </span>
          ))}
          {(user?.permissions?.length || 0) > 8 && (
            <span className="text-[10px] font-mono px-2.5 py-1 rounded-lg bg-slate-100 dark:bg-white/[0.05] border border-slate-200 dark:border-white/[0.08] text-slate-500 dark:text-slate-400">
              +{(user?.permissions?.length || 0) - 8} more
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
