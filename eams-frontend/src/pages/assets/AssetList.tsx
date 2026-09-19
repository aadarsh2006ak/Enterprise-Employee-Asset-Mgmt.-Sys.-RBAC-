import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../context/AuthContext';
import { assetApi } from '../../api';
import { AssetResponse, AssetStatus } from '../../types';
import { AssetStatusBadge } from '../../components/common/Badge';
import { Pagination } from '../../components/common/Pagination';
import { AssetFormModal } from '../../components/assets/AssetFormModal';
import { AssignAssetModal } from '../../components/assets/AssignAssetModal';
import { ReturnAssetModal } from '../../components/assets/ReturnAssetModal';
import { AssetHistoryTimelineModal } from '../../components/assets/AssetHistoryTimelineModal';
import { CategoryModal } from '../../components/assets/CategoryModal';
import { useToast } from '../../components/common/Toast';
import {
  Boxes,
  Plus,
  Search,
  FolderPlus,
  UserCheck,
  RotateCcw,
  History,
  Edit2,
  Trash2,
  Filter,
} from 'lucide-react';

export const AssetList: React.FC = () => {
  const queryClient = useQueryClient();
  const { hasPermission } = useAuth();
  const { success, error: showError } = useToast();

  // Filters and Pagination State
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState<number | ''>('');
  const [status, setStatus] = useState<AssetStatus | ''>('');

  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [assetToEdit, setAssetToEdit] = useState<AssetResponse | null>(null);
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [assignAsset, setAssignAsset] = useState<AssetResponse | null>(null);
  const [returnAsset, setReturnAsset] = useState<AssetResponse | null>(null);
  const [historyAsset, setHistoryAsset] = useState<AssetResponse | null>(null);

  // Queries
  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => assetApi.getCategories(),
  });

  const { data: assetsData, isLoading, refetch } = useQuery({
    queryKey: ['assets', page, size, categoryId, status, search],
    queryFn: () =>
      assetApi.getAllAssets({
        page,
        size,
        categoryId: categoryId ? Number(categoryId) : undefined,
        status: status ? (status as AssetStatus) : undefined,
        search: search || undefined,
      }),
  });

  // Delete Mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => assetApi.deleteAsset(id),
    onSuccess: () => {
      success('Asset deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['assets'] });
    },
    onError: (err: any) => {
      showError(err.response?.data?.message || 'Failed to delete asset');
    },
  });

  const handleDelete = (asset: AssetResponse) => {
    if (window.confirm(`Are you sure you want to permanently delete asset "${asset.assetTag}"?`)) {
      deleteMutation.mutate(asset.id);
    }
  };

  const assets = assetsData?.content || [];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
            <Boxes className="w-7 h-7 text-indigo-400" />
            <span>Asset & Hardware Inventory</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Manage company equipment, track assignments, and maintain lifecycle records
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          {hasPermission('ASSET_CREATE') && (
            <>
              <button
                onClick={() => setIsCategoryOpen(true)}
                className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-medium transition-all"
              >
                <FolderPlus className="w-4 h-4 text-slate-400" />
                <span>New Category</span>
              </button>

              <button
                onClick={() => {
                  setAssetToEdit(null);
                  setIsFormOpen(true);
                }}
                className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-lg shadow-indigo-500/25 transition-all"
              >
                <Plus className="w-4 h-4" />
                <span>Register Asset</span>
              </button>
            </>
          )}
        </div>
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
            placeholder="Search tag, model, serial..."
            className="w-full pl-10 pr-3.5 py-2 rounded-xl text-xs glass-input"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
          <div className="flex items-center gap-1 text-xs text-slate-400">
            <Filter className="w-3.5 h-3.5" />
            <span>Filters:</span>
          </div>

          <select
            value={categoryId}
            onChange={(e) => {
              setCategoryId(e.target.value ? Number(e.target.value) : '');
              setPage(0);
            }}
            className="px-3 py-2 rounded-xl text-xs glass-input bg-slate-900"
          >
            <option value="">All Categories</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name} ({cat.assetCount})
              </option>
            ))}
          </select>

          <select
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as AssetStatus | '');
              setPage(0);
            }}
            className="px-3 py-2 rounded-xl text-xs glass-input bg-slate-900"
          >
            <option value="">All Statuses</option>
            <option value="AVAILABLE">Available</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="UNDER_MAINTENANCE">Maintenance</option>
            <option value="RETIRED">Retired</option>
          </select>
        </div>
      </div>

      {/* Table Section */}
      <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/90 border-b border-slate-800 text-slate-400 uppercase tracking-wider font-semibold">
              <tr>
                <th className="py-3.5 px-4">Asset Tag</th>
                <th className="py-3.5 px-4">Category & Model</th>
                <th className="py-3.5 px-4">Serial Number</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4">Current Assignment</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center gap-3">
                      <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
                      <span>Loading asset records...</span>
                    </div>
                  </td>
                </tr>
              ) : assets.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    No matching assets found in inventory.
                  </td>
                </tr>
              ) : (
                assets.map((asset) => (
                  <tr key={asset.id} className="hover:bg-slate-850/50 transition-colors group">
                    <td className="py-3.5 px-4 font-mono font-bold text-indigo-300">
                      {asset.assetTag}
                    </td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-white">{asset.modelName}</div>
                      <div className="text-[11px] text-slate-400">{asset.categoryName}</div>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400">
                      {asset.serialNumber || '—'}
                    </td>
                    <td className="py-3.5 px-4">
                      <AssetStatusBadge status={asset.status} />
                    </td>
                    <td className="py-3.5 px-4">
                      {asset.activeAssignment ? (
                        <div>
                          <div className="font-medium text-indigo-300">
                            {asset.activeAssignment.employeeName}
                          </div>
                          <div className="text-[10px] text-slate-500 font-mono">
                            {asset.activeAssignment.employeeCode} • Since{' '}
                            {new Date(asset.activeAssignment.assignedAt).toLocaleDateString()}
                          </div>
                        </div>
                      ) : (
                        <span className="text-slate-500 italic">Unassigned</span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {/* Assign Button */}
                        {hasPermission('ASSET_ASSIGN') && asset.status === 'AVAILABLE' && (
                          <button
                            onClick={() => setAssignAsset(asset)}
                            title="Assign to employee"
                            className="p-1.5 rounded-lg bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-400 transition-colors"
                          >
                            <UserCheck className="w-4 h-4" />
                          </button>
                        )}

                        {/* Return Button */}
                        {hasPermission('ASSET_RETURN') && asset.status === 'ASSIGNED' && (
                          <button
                            onClick={() => setReturnAsset(asset)}
                            title="Accept return"
                            className="p-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 transition-colors"
                          >
                            <RotateCcw className="w-4 h-4" />
                          </button>
                        )}

                        {/* History Timeline Button */}
                        <button
                          onClick={() => setHistoryAsset(asset)}
                          title="Assignment History"
                          className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
                        >
                          <History className="w-4 h-4" />
                        </button>

                        {/* Edit Button */}
                        {hasPermission('ASSET_UPDATE') && (
                          <button
                            onClick={() => {
                              setAssetToEdit(asset);
                              setIsFormOpen(true);
                            }}
                            title="Edit specifications"
                            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                        )}

                        {/* Delete Button */}
                        {hasPermission('ASSET_DELETE') && asset.status !== 'ASSIGNED' && (
                          <button
                            onClick={() => handleDelete(asset)}
                            title="Delete asset"
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

        {assetsData && (
          <Pagination
            pageNumber={assetsData.pageNumber}
            totalPages={assetsData.totalPages}
            totalElements={assetsData.totalElements}
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
      <AssetFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSuccess={() => refetch()}
        categories={categories}
        assetToEdit={assetToEdit}
      />

      <AssignAssetModal
        isOpen={!!assignAsset}
        onClose={() => setAssignAsset(null)}
        onSuccess={() => refetch()}
        asset={assignAsset}
      />

      <ReturnAssetModal
        isOpen={!!returnAsset}
        onClose={() => setReturnAsset(null)}
        onSuccess={() => refetch()}
        asset={returnAsset}
      />

      <AssetHistoryTimelineModal
        isOpen={!!historyAsset}
        onClose={() => setHistoryAsset(null)}
        asset={historyAsset}
      />

      <CategoryModal
        isOpen={isCategoryOpen}
        onClose={() => setIsCategoryOpen(false)}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['categories'] });
        }}
      />
    </div>
  );
};

export default AssetList;
