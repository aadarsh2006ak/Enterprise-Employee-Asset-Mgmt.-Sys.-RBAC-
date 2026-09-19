import React from 'react';

interface TableProps {
  children: React.ReactNode;
  className?: string;
}

export const Table: React.FC<TableProps> = ({ children, className = '' }) => {
  return (
    <div className={`overflow-x-auto rounded-2xl border border-slate-200 dark:border-white/[0.08] glass-panel shadow-glass ${className}`}>
      <table className="w-full text-left text-xs font-sans">{children}</table>
    </div>
  );
};

export const TableHeader: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <thead className="bg-slate-50 dark:bg-slate-950/80 border-b border-slate-200 dark:border-white/[0.08] text-slate-500 dark:text-slate-400 uppercase tracking-wider font-bold text-[10px]">
    {children}
  </thead>
);

export const TableBody: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <tbody className="divide-y divide-slate-100 dark:divide-white/[0.05]">{children}</tbody>
);

export const TableRow: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => (
  <tr className={`hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors duration-150 group ${className}`}>{children}</tr>
);

export const TableHead: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => <th className={`py-3.5 px-4 font-bold text-slate-700 dark:text-slate-300 ${className}`}>{children}</th>;

export const TableCell: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => <td className={`py-3.5 px-4 text-slate-800 dark:text-slate-200 font-medium ${className}`}>{children}</td>;
