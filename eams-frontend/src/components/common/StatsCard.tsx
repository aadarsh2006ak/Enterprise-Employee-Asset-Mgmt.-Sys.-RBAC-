import React from 'react';
import { LucideIcon } from 'lucide-react';

interface StatsCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  iconColor?: 'indigo' | 'emerald' | 'amber' | 'rose' | 'blue' | 'purple' | 'cyan';
  trend?: {
    value: string;
    isPositive: boolean;
  };
}

export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon,
  iconColor = 'indigo',
  trend,
}) => {
  const colorStyles = {
    indigo: {
      bg: 'bg-indigo-50 dark:bg-indigo-500/15 text-indigo-600 dark:text-indigo-400 border-indigo-200 dark:border-indigo-500/30 shadow-sm',
      glow: 'hover:border-indigo-400/50',
      aura: 'from-indigo-500/10 dark:from-indigo-500/15 to-transparent',
    },
    emerald: {
      bg: 'bg-emerald-50 dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-200 dark:border-emerald-500/30 shadow-sm',
      glow: 'hover:border-emerald-400/50',
      aura: 'from-emerald-500/10 dark:from-emerald-500/15 to-transparent',
    },
    cyan: {
      bg: 'bg-cyan-50 dark:bg-cyan-500/15 text-cyan-600 dark:text-cyan-400 border-cyan-200 dark:border-cyan-500/30 shadow-sm',
      glow: 'hover:border-cyan-400/50',
      aura: 'from-cyan-500/10 dark:from-cyan-500/15 to-transparent',
    },
    amber: {
      bg: 'bg-amber-50 dark:bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-200 dark:border-amber-500/30 shadow-sm',
      glow: 'hover:border-amber-400/50',
      aura: 'from-amber-500/10 dark:from-amber-500/15 to-transparent',
    },
    rose: {
      bg: 'bg-rose-50 dark:bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-200 dark:border-rose-500/30 shadow-sm',
      glow: 'hover:border-rose-400/50',
      aura: 'from-rose-500/10 dark:from-rose-500/15 to-transparent',
    },
    blue: {
      bg: 'bg-blue-50 dark:bg-blue-500/15 text-blue-600 dark:text-blue-400 border-blue-200 dark:border-blue-500/30 shadow-sm',
      glow: 'hover:border-blue-400/50',
      aura: 'from-blue-500/10 dark:from-blue-500/15 to-transparent',
    },
    purple: {
      bg: 'bg-purple-50 dark:bg-purple-500/15 text-purple-600 dark:text-purple-400 border-purple-200 dark:border-purple-500/30 shadow-sm',
      glow: 'hover:border-purple-400/50',
      aura: 'from-purple-500/10 dark:from-purple-500/15 to-transparent',
    },
  };

  const currentStyle = colorStyles[iconColor] || colorStyles.indigo;

  return (
    <div
      className={`glass-card glass-card-hover p-6 rounded-2xl group relative overflow-hidden transition-all duration-300 ${currentStyle.glow}`}
    >
      <div className="flex items-start justify-between relative z-10">
        <div>
          <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 tracking-wider uppercase font-sans">
            {title}
          </p>
          <h3 className="text-3xl font-extrabold text-slate-900 dark:text-white mt-2 tracking-tight font-sans">
            {value}
          </h3>
          {subtitle && (
            <p className="text-xs text-slate-600 dark:text-slate-400 mt-1.5 font-medium leading-relaxed">
              {subtitle}
            </p>
          )}
          {trend && (
            <div className="flex items-center gap-1.5 mt-3">
              <span
                className={`inline-flex items-center text-xs font-bold px-2 py-0.5 rounded-full ${
                  trend.isPositive
                    ? 'bg-emerald-50 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-500/25'
                    : 'bg-rose-50 dark:bg-rose-500/15 text-rose-700 dark:text-rose-400 border border-rose-200 dark:border-rose-500/25'
                }`}
              >
                {trend.isPositive ? '↑' : '↓'} {trend.value}
              </span>
              <span className="text-[11px] text-slate-500 font-medium">vs last month</span>
            </div>
          )}
        </div>
        <div
          className={`p-3.5 rounded-2xl border transition-transform duration-300 group-hover:scale-110 flex-shrink-0 ${currentStyle.bg}`}
        >
          <Icon className="w-6 h-6" />
        </div>
      </div>

      {/* Decorative ambient corner glow */}
      <div
        className={`absolute -bottom-10 -right-10 w-32 h-32 bg-gradient-to-tl ${currentStyle.aura} rounded-full blur-2xl pointer-events-none group-hover:scale-125 transition-transform duration-500`}
      />
    </div>
  );
};

export default StatsCard;
