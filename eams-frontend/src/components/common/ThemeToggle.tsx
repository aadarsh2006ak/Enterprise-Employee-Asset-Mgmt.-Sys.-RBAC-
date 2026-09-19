import React, { useState } from 'react';
import { useTheme, ThemeMode } from '../../context/ThemeContext';
import { Moon, Sun, Laptop, Check, UserCheck, ChevronDown } from 'lucide-react';

interface ModeOption {
  id: ThemeMode;
  name: string;
  subtitle: string;
  icon: typeof Moon;
}

const MODE_OPTIONS: ModeOption[] = [
  {
    id: 'dark',
    name: 'Dark Mode',
    subtitle: 'High-contrast enterprise obsidian',
    icon: Moon,
  },
  {
    id: 'light',
    name: 'Light Mode',
    subtitle: 'Crisp bright enterprise canvas',
    icon: Sun,
  },
  {
    id: 'system',
    name: 'System Default',
    subtitle: 'Follows OS device settings',
    icon: Laptop,
  },
];

export const ThemeToggle: React.FC<{ compact?: boolean }> = ({ compact = false }) => {
  const { mode, resolvedMode, setMode, toggleMode, accountUsername } = useTheme();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="relative font-sans inline-flex items-center gap-1">
      {/* 1-Click Fast Toggle Button */}
      <button
        onClick={toggleMode}
        className="p-2 rounded-xl glass-card hover:border-indigo-500/40 text-slate-300 hover:text-white transition-all duration-200 shadow-sm flex items-center justify-center group"
        title={`Toggle to ${resolvedMode === 'dark' ? 'Light' : 'Dark'} Mode`}
        aria-label="Toggle Theme Mode"
      >
        {resolvedMode === 'dark' ? (
          <Moon className="w-4 h-4 text-indigo-400 group-hover:text-indigo-300 transition-transform group-hover:-rotate-12" />
        ) : (
          <Sun className="w-4 h-4 text-amber-500 group-hover:text-amber-400 transition-transform group-hover:rotate-45" />
        )}
      </button>

      {/* Mode Selector Dropdown Button */}
      {!compact && (
        <div className="relative">
          <button
            onClick={() => setIsOpen(!isOpen)}
            className="flex items-center gap-1.5 py-1.5 px-2.5 rounded-xl glass-card hover:border-white/20 text-xs font-semibold text-slate-300 hover:text-white transition-all duration-200 shadow-sm"
            title={`Mode: ${mode} (Saved for ${accountUsername ? `@${accountUsername}` : 'Guest'})`}
          >
            <span className="capitalize text-[11px] font-medium">
              {mode === 'system' ? 'System' : resolvedMode === 'dark' ? 'Dark' : 'Light'}
            </span>
            <ChevronDown className={`w-3 h-3 text-slate-400 transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`} />
          </button>

          {/* Dropdown Menu */}
          {isOpen && (
            <>
              <div className="fixed inset-0 z-40" onClick={() => setIsOpen(false)} />
              <div className="absolute right-0 mt-2 w-64 glass-dropdown rounded-2xl p-2 z-50 shadow-2xl border animate-slide-up">
                <div className="px-3 py-2 border-b border-white/[0.08] mb-1">
                  <p className="text-[10px] font-extrabold uppercase tracking-widest text-slate-400">
                    Theme Preference
                  </p>
                  {accountUsername && (
                    <p className="text-[10px] text-indigo-400 font-mono mt-0.5 flex items-center gap-1">
                      <UserCheck className="w-3 h-3" /> Saved for @{accountUsername}
                    </p>
                  )}
                </div>

                <div className="space-y-1">
                  {MODE_OPTIONS.map((opt) => {
                    const isSelected = mode === opt.id;
                    const Icon = opt.icon;
                    return (
                      <button
                        key={opt.id}
                        onClick={() => {
                          setMode(opt.id);
                          setIsOpen(false);
                        }}
                        className={`w-full flex items-center justify-between p-2.5 rounded-xl text-left transition-all ${
                          isSelected
                            ? 'bg-indigo-600/20 border border-indigo-500/40 text-white shadow-inner'
                            : 'hover:bg-white/[0.05] text-slate-300 hover:text-white border border-transparent'
                        }`}
                      >
                        <div className="flex items-center gap-2.5">
                          <div className={`p-1.5 rounded-lg ${isSelected ? 'bg-indigo-500/30 text-indigo-300' : 'bg-slate-800 text-slate-400'}`}>
                            <Icon className="w-3.5 h-3.5" />
                          </div>
                          <div>
                            <p className="text-xs font-bold leading-none">{opt.name}</p>
                            <p className="text-[10px] text-slate-400 mt-0.5">{opt.subtitle}</p>
                          </div>
                        </div>
                        {isSelected && <Check className="w-3.5 h-3.5 text-indigo-400 flex-shrink-0" />}
                      </button>
                    );
                  })}
                </div>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default ThemeToggle;
