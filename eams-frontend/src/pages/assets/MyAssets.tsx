import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { assetApi } from '../../api';
import { Laptop, Calendar, User, ShieldCheck, CheckCircle } from 'lucide-react';

export const MyAssets: React.FC = () => {
  const { data: myAssets = [], isLoading } = useQuery({
    queryKey: ['my-active-assets'],
    queryFn: () => assetApi.getMyActiveAssets(),
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2.5">
          <Laptop className="w-7 h-7 text-indigo-400" />
          <span>My Assigned Assets</span>
        </h1>
        <p className="text-xs text-slate-400 mt-1">
          Hardware and equipment issued to your employee account for daily operations
        </p>
      </div>

      {isLoading ? (
        <div className="py-16 flex flex-col items-center justify-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
          <p className="text-xs text-slate-400">Loading your assigned equipment...</p>
        </div>
      ) : myAssets.length === 0 ? (
        <div className="glass-panel rounded-3xl p-12 text-center border border-slate-800 space-y-3">
          <div className="w-12 h-12 rounded-full bg-slate-900 border border-slate-800 text-slate-500 flex items-center justify-center mx-auto">
            <Laptop className="w-6 h-6" />
          </div>
          <h3 className="text-base font-semibold text-white">No Active Hardware Assigned</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            You do not currently have any active equipment issued. If you need a laptop or workstation, please contact your department manager.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {myAssets.map((asset) => (
            <div
              key={asset.id}
              className="glass-panel glass-panel-hover rounded-2xl p-6 border border-slate-800 flex flex-col justify-between space-y-4"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/20 uppercase font-semibold tracking-wider">
                      {asset.categoryName || 'Equipment'}
                    </span>
                    <h3 className="text-lg font-bold text-white mt-1.5">{asset.assetModel}</h3>
                    <p className="text-xs font-mono font-bold text-indigo-400">{asset.assetTag}</p>
                  </div>
                  <span className="flex items-center gap-1 text-[11px] font-medium px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    <CheckCircle className="w-3 h-3" /> Active
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs text-slate-400 mt-4 pt-4 border-t border-slate-800">
                  <div className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-slate-500" />
                    <span>Issued: {new Date(asset.assignedAt).toLocaleDateString()}</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <User className="w-3.5 h-3.5 text-slate-500" />
                    <span>Issued By: @{asset.assignedByUsername}</span>
                  </div>
                </div>

                {asset.conditionNotes && (
                  <div className="mt-3 p-3 rounded-xl bg-slate-900 text-xs text-slate-300 font-mono border border-slate-800">
                    <span className="text-[10px] text-slate-500 uppercase tracking-wider block mb-1 font-sans">
                      Handover Condition Notes:
                    </span>
                    {asset.conditionNotes}
                  </div>
                )}
              </div>

              <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500">
                <div className="flex items-center gap-1 text-emerald-400">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Company Asset Policy Active</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyAssets;
