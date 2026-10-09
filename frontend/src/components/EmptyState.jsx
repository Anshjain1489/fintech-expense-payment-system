import React from 'react';
import { Inbox } from 'lucide-react';

export default function EmptyState({ title = "No data found", description = "Get started by creating a new entry.", actionLabel, onAction }) {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center bg-slate-900/50 border border-slate-800 rounded-2xl">
      <div className="p-4 bg-slate-800/80 rounded-full text-teal-400 mb-4 shadow-inner">
        <Inbox className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-semibold text-slate-200 mb-1">{title}</h3>
      <p className="text-sm text-slate-400 max-w-sm mb-6">{description}</p>
      {actionLabel && onAction && (
        <button
          onClick={onAction}
          className="px-4 py-2 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-400 hover:to-cyan-400 text-slate-950 font-semibold rounded-xl text-sm transition-all shadow-lg shadow-teal-500/20 active:scale-95"
        >
          {actionLabel}
        </button>
      )}
    </div>
  );
}
