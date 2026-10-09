import React from 'react';

export default function LoadingSpinner({ message = "Loading data..." }) {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center">
      <div className="relative w-12 h-12 mb-4">
        <div className="absolute inset-0 rounded-full border-4 border-slate-800"></div>
        <div className="absolute inset-0 rounded-full border-4 border-teal-500 border-t-transparent animate-spin"></div>
      </div>
      <p className="text-sm font-medium text-slate-400">{message}</p>
    </div>
  );
}
