import React from 'react';
import { Product } from '../types';
import { ShoppingCart, Sparkles, AlertCircle, CheckCircle2, TrendingUp } from 'lucide-react';

interface ProductsTableProps {
  products: Product[];
  onSimulateSale: (product: Product) => void;
  onManualPricing: (product: Product) => void;
  onManualReorder: (product: Product) => void;
}

export const ProductsTable: React.FC<ProductsTableProps> = ({
  products,
  onSimulateSale,
  onManualPricing,
  onManualReorder,
}) => {
  const getStockBadge = (product: Product) => {
    if (product.stockLevel === 0) {
      return <span className="badge badge-out"><AlertCircle size={12} /> Out of Stock</span>;
    }
    if (product.stockLevel < product.reorderThreshold) {
      return <span className="badge badge-low"><AlertCircle size={12} /> Low Stock</span>;
    }
    return <span className="badge badge-active"><CheckCircle2 size={12} /> Healthy</span>;
  };

  return (
    <div className="products-table-container">
      <table className="products-table">
        <thead>
          <tr>
            <th>SKU</th>
            <th>Product Name</th>
            <th>Category</th>
            <th>Price</th>
            <th>Stock Level</th>
            <th>Threshold</th>
            <th>Demand Velocity</th>
            <th>Stock Health</th>
            <th style={{ textAlign: 'right' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id} id={`product-row-${product.id}`}>
              <td className="mono" style={{ fontWeight: 600, color: '#c7d2fe' }}>
                {product.sku}
              </td>
              <td style={{ fontWeight: 500 }}>{product.name}</td>
              <td>
                <span className="badge" style={{ background: '#1e293b', color: '#94a3b8' }}>
                  {product.category}
                </span>
              </td>
              <td className="mono" style={{ fontWeight: 700, color: '#f8fafc' }}>
                ${product.currentPrice.toFixed(2)}
              </td>
              <td>
                <span
                  className="mono"
                  style={{
                    fontWeight: 700,
                    color: product.stockLevel < product.reorderThreshold ? '#f59e0b' : '#f8fafc',
                  }}
                >
                  {product.stockLevel}
                </span>
              </td>
              <td className="mono" style={{ color: '#94a3b8' }}>
                {product.reorderThreshold}
              </td>
              <td>
                <span className="mono" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                  <TrendingUp size={13} color="#6366f1" />
                  {product.demandVelocity.toFixed(1)}/d
                </span>
              </td>
              <td>{getStockBadge(product)}</td>
              <td style={{ textAlign: 'right' }}>
                <div style={{ display: 'flex', gap: '6px', justifyContent: 'flex-end' }}>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => onSimulateSale(product)}
                    id={`simulate-sale-${product.id}`}
                    title="Simulate Sale"
                  >
                    <ShoppingCart size={13} />
                    Simulate Sale
                  </button>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => onManualPricing(product)}
                    title="Generate AI/Rule Pricing Recommendation"
                  >
                    <Sparkles size={13} /> Pricing
                  </button>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => onManualReorder(product)}
                    title="Generate AI/Rule Reorder Recommendation"
                  >
                    <Sparkles size={13} /> Reorder
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
