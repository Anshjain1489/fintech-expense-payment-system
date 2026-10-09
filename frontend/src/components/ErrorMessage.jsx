import React from 'react';
import { AlertCircle } from 'lucide-react';

export default function ErrorMessage({ title = "An error occurred", message, onRetry }) {
  return (
    <div className="p-4 bg-rose-950/40 border border-rose-800/60 rounded-xl flex items-start space-x-3 text-rose-200">
      <AlertCircle className="w-5 h-5 text-rose-400 mt-0.5 shrink-0" />
      <div className="flex-1 text-sm">
        <h4 className="font-semibold text-rose-300">{title}</h4>
        <p className="text-rose-200/90 mt-1">{message || "Unable to complete request. Please try again."}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="mt-3 px-3 py-1 bg-rose-900/60 hover:bg-rose-900 border border-rose-700/60 text-rose-100 rounded-lg text-xs font-medium transition-colors"
          >
            Retry Request
          </button>
        )}
      </div>
    </div>
  );
}
