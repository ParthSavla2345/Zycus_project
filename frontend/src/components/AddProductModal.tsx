import React, { useState } from 'react';
import { Category, CreateProductRequest } from '../types';
import { PlusCircle, X } from 'lucide-react';

interface AddProductModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: CreateProductRequest) => Promise<void>;
}

export const AddProductModal: React.FC<AddProductModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
}) => {
  const [sku, setSku] = useState('');
  const [name, setName] = useState('');
  const [category, setCategory] = useState<Category>('ELECTRONICS');
  const [currentPrice, setCurrentPrice] = useState<number>(49.99);
  const [stockLevel, setStockLevel] = useState<number>(50);
  const [reorderThreshold, setReorderThreshold] = useState<number>(20);
  const [demandVelocity, setDemandVelocity] = useState<number>(5.0);
  const [costPrice, setCostPrice] = useState<string>('25.00');
  const [supplierId, setSupplierId] = useState<string>('SUP-01');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sku.trim()) {
      setError('SKU is required');
      return;
    }
    if (!name.trim()) {
      setError('Product name is required');
      return;
    }
    if (currentPrice <= 0) {
      setError('Price must be greater than $0');
      return;
    }
    if (stockLevel < 0 || reorderThreshold < 0) {
      setError('Stock and threshold must be non-negative');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      await onSubmit({
        sku: sku.trim().toUpperCase(),
        name: name.trim(),
        category,
        currentPrice: Number(currentPrice),
        stockLevel: Number(stockLevel),
        reorderThreshold: Number(reorderThreshold),
        demandVelocity: Number(demandVelocity),
        costPrice: costPrice ? Number(costPrice) : null,
        supplierId: supplierId.trim() || null,
      });
      // Reset form
      setSku('');
      setName('');
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to create product');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: '560px' }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <PlusCircle size={20} color="#6366f1" />
            Add New Product
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.15)', border: '1px solid #ef4444', padding: '10px 12px', borderRadius: '6px', fontSize: '12.5px', color: '#fca5a5' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="new-sku">SKU *</label>
              <input
                id="new-sku"
                type="text"
                placeholder="e.g. PRD-050"
                className="form-input mono"
                value={sku}
                onChange={(e) => setSku(e.target.value)}
                required
                autoFocus
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="new-name">Product Name *</label>
              <input
                id="new-name"
                type="text"
                placeholder="e.g. Ultra Ergonomic Mouse"
                className="form-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="new-cat">Category *</label>
              <select
                id="new-cat"
                className="form-input"
                value={category}
                onChange={(e) => setCategory(e.target.value as Category)}
              >
                <option value="ELECTRONICS">Electronics</option>
                <option value="APPAREL">Apparel</option>
                <option value="HOME">Home</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="new-price">Retail Price ($) *</label>
              <input
                id="new-price"
                type="number"
                step="0.01"
                min="0.01"
                className="form-input mono"
                value={currentPrice}
                onChange={(e) => setCurrentPrice(parseFloat(e.target.value) || 0)}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="new-stock">Stock Level *</label>
              <input
                id="new-stock"
                type="number"
                min="0"
                className="form-input mono"
                value={stockLevel}
                onChange={(e) => setStockLevel(parseInt(e.target.value) || 0)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="new-threshold">Reorder Threshold *</label>
              <input
                id="new-threshold"
                type="number"
                min="0"
                className="form-input mono"
                value={reorderThreshold}
                onChange={(e) => setReorderThreshold(parseInt(e.target.value) || 0)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="new-velocity">Velocity (orders/d) *</label>
              <input
                id="new-velocity"
                type="number"
                step="0.1"
                min="0"
                className="form-input mono"
                value={demandVelocity}
                onChange={(e) => setDemandVelocity(parseFloat(e.target.value) || 0)}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="new-cost">Cost Price ($)</label>
              <input
                id="new-cost"
                type="number"
                step="0.01"
                placeholder="Optional"
                className="form-input mono"
                value={costPrice}
                onChange={(e) => setCostPrice(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="new-supplier">Supplier ID</label>
              <input
                id="new-supplier"
                type="text"
                placeholder="e.g. SUP-01"
                className="form-input mono"
                value={supplierId}
                onChange={(e) => setSupplierId(e.target.value)}
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '12px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Creating...' : 'Save Product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
