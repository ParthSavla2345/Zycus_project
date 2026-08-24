import React from 'react';
import { StrategyUsed } from '../types';
import { Sparkles, Cpu, Activity, RefreshCw } from 'lucide-react';

interface HeaderProps {
  strategy: StrategyUsed;
  onStrategyChange: (strategy: StrategyUsed) => void;
  onRefresh: () => void;
  isRefreshing: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  strategy,
  onStrategyChange,
  onRefresh,
  isRefreshing,
}) => {
  return (
    <header className="app-header">
      <div className="brand-section">
        <div className="brand-logo">
          <Activity size={26} color="#ffffff" />
        </div>
        <div className="brand-text">
          <h1>StockPulse</h1>
          <p>AI Inventory & Dynamic Pricing</p>
        </div>
      </div>

      <div className="header-controls">
        <div className="strategy-box">
          {strategy === 'AI' ? (
            <Sparkles size={16} color="#c084fc" />
          ) : (
            <Cpu size={16} color="#38bdf8" />
          )}
          <label htmlFor="strategy-select">Current Strategy:</label>
          <select
            id="strategy-select"
            className="strategy-select"
            value={strategy}
            onChange={(e) => onStrategyChange(e.target.value as StrategyUsed)}
          >
            <option value="AI">AI (LiteLLM + Rule Fallback)</option>
            <option value="RULE">RULE (Deterministic Rules)</option>
          </select>
        </div>

        <button
          className="btn btn-secondary btn-sm"
          onClick={onRefresh}
          title="Manual refresh"
        >
          <RefreshCw size={14} className={isRefreshing ? 'animate-spin' : ''} />
          Refresh
        </button>

        <div className="live-indicator">
          <span className="pulse-dot"></span>
          <span>Live Polling (3s)</span>
        </div>
      </div>
    </header>
  );
};
