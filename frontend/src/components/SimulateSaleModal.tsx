import React, { useState } from 'react';
import { Product } from '../types';
import { ShoppingCart, X, AlertTriangle } from 'lucide-react';

interface SimulateSaleModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (productId: number, quantity: number) => Promise<void>;
}

export const SimulateSaleModal: React.FC<SimulateSaleModalProps> = ({
  product,
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [quantity, setQuantity] = useState<number>(1);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen || !product) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (quantity < 1) {
      setError('Quantity must be at least 1');
      return;
    }
    if (quantity > product.stockLevel) {
      setError(`Quantity cannot exceed available stock (${product.stockLevel})`);
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await onSubmit(product.id, quantity);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to simulate sale');
    } finally {
      setSubmitting(false);
    }
  };

  const willTriggerLowInventory = product.stockLevel - quantity < product.reorderThreshold;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShoppingCart size={20} color="#6366f1" />
            Simulate Sale
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div>
          <h3 style={{ fontSize: '15px', color: '#f8fafc' }}>{product.name}</h3>
          <p className="mono" style={{ fontSize: '12px', color: '#94a3b8' }}>
            SKU: {product.sku} | Price: ${product.currentPrice.toFixed(2)}
          </p>
        </div>

        <div style={{ background: '#151d2d', padding: '12px', borderRadius: '8px', fontSize: '13px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
          <div>
            <span style={{ color: '#64748b' }}>Current Stock: </span>
            <strong>{product.stockLevel} units</strong>
          </div>
          <div>
            <span style={{ color: '#64748b' }}>Reorder Threshold: </span>
            <strong>{product.reorderThreshold} units</strong>
          </div>
        </div>

        {willTriggerLowInventory && (
          <div style={{ background: 'rgba(245, 158, 11, 0.15)', border: '1px solid #f59e0b', padding: '10px 12px', borderRadius: '6px', fontSize: '12.5px', color: '#fde047', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertTriangle size={16} />
            <span>This sale will drop stock below the threshold ({product.reorderThreshold}) and trigger an autonomous AI recommendation!</span>
          </div>
        )}

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.15)', border: '1px solid #ef4444', padding: '10px 12px', borderRadius: '6px', fontSize: '12.5px', color: '#fca5a5' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div className="form-group">
            <label className="form-label" htmlFor="order-qty">
              Order Quantity (units):
            </label>
            <input
              id="order-qty"
              type="number"
              min="1"
              max={product.stockLevel}
              className="form-input"
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
              autoFocus
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting || product.stockLevel === 0}>
              {submitting ? 'Processing...' : `Confirm Sale (${quantity} units)`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
