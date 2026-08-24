import React, { useState } from 'react';
import { ReorderSuggestion } from '../types';
import { Sparkles, Cpu, Check, X, PackagePlus, Clock } from 'lucide-react';

interface ReorderSuggestionCardProps {
  suggestion: ReorderSuggestion;
  onAccept: (id: number) => Promise<void>;
  onReject: (id: number) => Promise<void>;
}

export const ReorderSuggestionCard: React.FC<ReorderSuggestionCardProps> = ({
  suggestion,
  onAccept,
  onReject,
}) => {
  const [loading, setLoading] = useState(false);

  const handleAction = async (action: 'ACCEPT' | 'REJECT') => {
    setLoading(true);
    try {
      if (action === 'ACCEPT') {
        await onAccept(suggestion.id);
      } else {
        await onReject(suggestion.id);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="suggestion-card" id={`reorder-suggestion-${suggestion.id}`}>
      <div className="card-header">
        <div>
          <div className="card-title">{suggestion.productName}</div>
          <div className="card-sku mono">{suggestion.productSku}</div>
        </div>

        <div className="card-badges">
          <span className={`badge ${suggestion.strategyUsed === 'AI' ? 'badge-ai' : 'badge-rule'}`}>
            {suggestion.strategyUsed === 'AI' ? <Sparkles size={12} /> : <Cpu size={12} />}
            {suggestion.strategyUsed}
          </span>
          <span className="badge badge-trigger">{suggestion.triggerReason}</span>
        </div>
      </div>

      <div className="card-metrics">
        <div className="metric-item">
          <span className="metric-label">Current Stock</span>
          <span className="metric-value">{suggestion.currentStock} units</span>
        </div>
        <div className="metric-item">
          <span className="metric-label">Recommended Order</span>
          <span className="metric-value highlight" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <PackagePlus size={16} color="#34d399" />
            +{suggestion.recommendedQuantity} units
          </span>
        </div>
        <div className="metric-item">
          <span className="metric-label">Confidence</span>
          <span className="metric-value">{(suggestion.confidence * 100).toFixed(0)}%</span>
        </div>
        <div className="metric-item">
          <span className="metric-label">Lead Time</span>
          <span className="metric-value" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Clock size={14} color="#94a3b8" />
            {suggestion.suggestedLeadTimeDays} days
          </span>
        </div>
      </div>

      <div className="card-reasoning">
        <strong>Reasoning: </strong>
        {suggestion.reasoning}
      </div>

      <div className="card-footer">
        <span className="card-time mono">
          {new Date(suggestion.createdAt).toLocaleTimeString()}
        </span>
        <div className="card-actions">
          <button
            className="btn btn-danger btn-sm"
            onClick={() => handleAction('REJECT')}
            disabled={loading}
            id={`reject-reorder-${suggestion.id}`}
          >
            <X size={14} /> Reject
          </button>
          <button
            className="btn btn-success btn-sm"
            onClick={() => handleAction('ACCEPT')}
            disabled={loading}
            id={`accept-reorder-${suggestion.id}`}
          >
            <Check size={14} /> Accept Reorder
          </button>
        </div>
      </div>
    </div>
  );
};
