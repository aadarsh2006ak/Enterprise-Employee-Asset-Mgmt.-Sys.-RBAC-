import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { AssetResponse, AssetCategoryResponse, AssetStatus } from '../../types';
import { assetApi } from '../../api';
import { useToast } from '../common/Toast';

interface AssetFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  categories: AssetCategoryResponse[];
  assetToEdit?: AssetResponse | null;
}

export const AssetFormModal: React.FC<AssetFormModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  categories,
  assetToEdit,
}) => {
  const { success, error: showError } = useToast();
  const [loading, setLoading] = useState(false);

  const [assetTag, setAssetTag] = useState('');
  const [categoryId, setCategoryId] = useState<number>(categories[0]?.id || 1);
  const [modelName, setModelName] = useState('');
  const [serialNumber, setSerialNumber] = useState('');
  const [purchaseDate, setPurchaseDate] = useState('');
  const [status, setStatus] = useState<AssetStatus>('AVAILABLE');

  useEffect(() => {
    if (assetToEdit) {
      setAssetTag(assetToEdit.assetTag);
      setCategoryId(assetToEdit.categoryId);
      setModelName(assetToEdit.modelName);
      setSerialNumber(assetToEdit.serialNumber || '');
      setPurchaseDate(assetToEdit.purchaseDate || '');
      setStatus(assetToEdit.status);
    } else {
      setAssetTag('');
      setCategoryId(categories[0]?.id || 1);
      setModelName('');
      setSerialNumber('');
      setPurchaseDate('');
      setStatus('AVAILABLE');
    }
  }, [assetToEdit, categories, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (assetToEdit) {
        await assetApi.updateAsset(assetToEdit.id, {
          categoryId,
          modelName,
          serialNumber: serialNumber || undefined,
          purchaseDate: purchaseDate || undefined,
          status,
          version: assetToEdit.version,
        });
        success(`Asset "${assetTag}" updated successfully`);
      } else {
        await assetApi.createAsset({
          assetTag,
          categoryId,
          modelName,
          serialNumber: serialNumber || undefined,
          purchaseDate: purchaseDate || undefined,
        });
        success(`Asset "${assetTag}" registered successfully`);
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
      title={assetToEdit ? `Edit Asset (${assetToEdit.assetTag})` : 'Register New Asset'}
      subtitle={assetToEdit ? 'Update equipment specifications and status' : 'Add hardware or software equipment to inventory'}
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-300 mb-1.5">
            Asset Tag <span className="text-rose-400">*</span>
          </label>
          <input
            type="text"
            required
            disabled={!!assetToEdit}
            value={assetTag}
            onChange={(e) => setAssetTag(e.target.value)}
            placeholder="e.g. LAP-2024-001"
            className="w-full px-3.5 py-2 rounded-xl text-sm glass-input disabled:opacity-50 disabled:cursor-not-allowed"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Category <span className="text-rose-400">*</span>
            </label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(Number(e.target.value))}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            >
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id} className="bg-slate-900 text-white">
                  {cat.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Model Name <span className="text-rose-400">*</span>
            </label>
            <input
              type="text"
              required
              value={modelName}
              onChange={(e) => setModelName(e.target.value)}
              placeholder="e.g. MacBook Pro M3 Max"
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Serial Number
            </label>
            <input
              type="text"
              value={serialNumber}
              onChange={(e) => setSerialNumber(e.target.value)}
              placeholder="e.g. C02XYZ12345"
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Purchase Date
            </label>
            <input
              type="date"
              value={purchaseDate}
              onChange={(e) => setPurchaseDate(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            />
          </div>
        </div>

        {assetToEdit && (
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">
              Asset Status
            </label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as AssetStatus)}
              className="w-full px-3.5 py-2 rounded-xl text-sm glass-input"
            >
              <option value="AVAILABLE" className="bg-slate-900 text-white">AVAILABLE</option>
              <option value="ASSIGNED" className="bg-slate-900 text-white">ASSIGNED</option>
              <option value="UNDER_MAINTENANCE" className="bg-slate-900 text-white">UNDER_MAINTENANCE</option>
              <option value="RETIRED" className="bg-slate-900 text-white">RETIRED</option>
            </select>
          </div>
        )}

        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-medium rounded-xl border border-slate-700 bg-slate-800/80 text-slate-300 hover:bg-slate-800 hover:text-white transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-5 py-2 text-xs font-medium rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-500/25 transition-all disabled:opacity-50"
          >
            {loading ? 'Saving...' : assetToEdit ? 'Update Asset' : 'Register Asset'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
