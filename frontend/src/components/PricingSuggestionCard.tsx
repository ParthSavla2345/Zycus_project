import React, { useState } from 'react';
import { PricingSuggestion } from '../types';
import { Sparkles, Cpu, Check, X, TrendingUp, TrendingDown, Minus } from 'lucide-react';

interface PricingSuggestionCardProps {
  suggestion: PricingSuggestion;
  onAccept: (id: number) => Promise<void>;
  onReject: (id: number) => Promise<void>;
}

export const PricingSuggestionCard: React.FC<PricingSuggestionCardProps> = ({
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

  const getDirectionIcon = () => {
    switch (suggestion.direction) {
      case 'INCREASE':
        return <TrendingUp size={16} color="#34d399" />;
      case 'DECREASE':
        return <TrendingDown size={16} color="#f87171" />;
      default:
        return <Minus size={16} color="#94a3b8" />;
    }
  };

  const priceDiff = suggestion.recommendedPrice - suggestion.currentPrice;
  const percentChange = ((priceDiff / suggestion.currentPrice) * 100).toFixed(1);

  return (
    <div className="suggestion-card" id={`pricing-suggestion-${suggestion.id}`}>
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
          <span className="metric-label">Current Price</span>
          <span className="metric-value">${suggestion.currentPrice.toFixed(2)}</span>
        </div>
        <div className="metric-item">
          <span className="metric-label">Recommended</span>
          <span className="metric-value highlight" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            ${suggestion.recommendedPrice.toFixed(2)}
            {getDirectionIcon()}
            <span style={{ fontSize: '11px', color: priceDiff > 0 ? '#34d399' : '#94a3b8' }}>
              ({priceDiff >= 0 ? `+${percentChange}%` : `${percentChange}%`})
            </span>
          </span>
        </div>
        <div className="metric-item">
          <span className="metric-label">Confidence</span>
          <span className="metric-value">{(suggestion.confidence * 100).toFixed(0)}%</span>
        </div>
        <div className="metric-item">
          <span className="metric-label">Direction</span>
          <span className="metric-value">{suggestion.direction}</span>
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
            id={`reject-pricing-${suggestion.id}`}
          >
            <X size={14} /> Reject
          </button>
          <button
            className="btn btn-success btn-sm"
            onClick={() => handleAction('ACCEPT')}
            disabled={loading}
            id={`accept-pricing-${suggestion.id}`}
          >
            <Check size={14} /> Accept Price
          </button>
        </div>
      </div>
    </div>
  );
};
