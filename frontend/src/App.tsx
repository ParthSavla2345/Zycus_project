import React, { useState, useEffect, useCallback } from 'react';
import { Product, PricingSuggestion, ReorderSuggestion, StrategyUsed, Category } from './types';
import { api } from './api/client';
import { Header } from './components/Header';
import { ProductsTable } from './components/ProductsTable';
import { PricingSuggestionCard } from './components/PricingSuggestionCard';
import { ReorderSuggestionCard } from './components/ReorderSuggestionCard';
import { SimulateSaleModal } from './components/SimulateSaleModal';
import { AddProductModal } from './components/AddProductModal';
import { Package, DollarSign, Boxes, Sparkles, CheckCircle2, AlertCircle, RefreshCw, PlusCircle } from 'lucide-react';

export const App: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [pricingSuggestions, setPricingSuggestions] = useState<PricingSuggestion[]>([]);
  const [reorderSuggestions, setReorderSuggestions] = useState<ReorderSuggestion[]>([]);
  const [strategy, setStrategy] = useState<StrategyUsed>('AI');
  const [selectedCategory, setSelectedCategory] = useState<Category | ''>('');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const [loading, setLoading] = useState<boolean>(true);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [toastMessage, setToastMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Modal State
  const [saleProduct, setSaleProduct] = useState<Product | null>(null);
  const [isSaleModalOpen, setIsSaleModalOpen] = useState<boolean>(false);
  const [isAddProductModalOpen, setIsAddProductModalOpen] = useState<boolean>(false);

  const showToast = (text: string, type: 'success' | 'error' = 'success') => {
    setToastMessage({ text, type });
    setTimeout(() => {
      setToastMessage(null);
    }, 4000);
  };

  const loadData = useCallback(async (isPolling = false) => {
    if (!isPolling) setIsRefreshing(true);
    try {
      const [prods, pricing, reorder, strat] = await Promise.all([
        api.getProducts(selectedCategory || undefined, searchQuery || undefined),
        api.getPricingSuggestions('PENDING'),
        api.getReorderSuggestions('PENDING'),
        api.getStrategy(),
      ]);

      setProducts(prods);
      setPricingSuggestions(pricing);
      setReorderSuggestions(reorder);
      setStrategy(strat.strategy);
    } catch (err: any) {
      if (!isPolling) {
        showToast(err.message || 'Failed to fetch dashboard data', 'error');
      }
    } finally {
      setLoading(false);
      setIsRefreshing(false);
    }
  }, [selectedCategory, searchQuery]);

  // Initial load
  useEffect(() => {
    loadData();
  }, [loadData]);

  // Real-time polling every 3 seconds for agentic recommendations!
  useEffect(() => {
    const interval = setInterval(() => {
      loadData(true);
    }, 3000);
    return () => clearInterval(interval);
  }, [loadData]);

  const handleStrategyChange = async (newStrategy: StrategyUsed) => {
    try {
      const res = await api.setStrategy(newStrategy);
      setStrategy(res.strategy);
      showToast(`Runtime strategy switched to: ${res.strategy}`);
    } catch (err: any) {
      showToast(err.message || 'Failed to switch strategy', 'error');
    }
  };

  const handleSimulateSaleSubmit = async (productId: number, quantity: number) => {
    try {
      const updatedProduct = await api.createOrder(productId, { quantity });
      showToast(`Sale recorded for ${updatedProduct.sku}! Remaining stock: ${updatedProduct.stockLevel}. Agentic loop triggered.`);
      // Reload immediately
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to process order', 'error');
    }
  };

  const handleAcceptPricing = async (id: number) => {
    try {
      const updated = await api.processPricingAction(id, 'ACCEPT');
      showToast(`Accepted pricing recommendation! New price: $${updated.recommendedPrice.toFixed(2)}`);
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to accept pricing', 'error');
    }
  };

  const handleRejectPricing = async (id: number) => {
    try {
      await api.processPricingAction(id, 'REJECT');
      showToast('Pricing recommendation rejected.');
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to reject pricing', 'error');
    }
  };

  const handleAcceptReorder = async (id: number) => {
    try {
      const updated = await api.processReorderAction(id, 'ACCEPT');
      showToast(`Accepted reorder recommendation! Added +${updated.recommendedQuantity} units to stock.`);
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to accept reorder', 'error');
    }
  };

  const handleRejectReorder = async (id: number) => {
    try {
      await api.processReorderAction(id, 'REJECT');
      showToast('Reorder recommendation rejected.');
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to reject reorder', 'error');
    }
  };

  const handleManualPricing = async (product: Product) => {
    try {
      await api.suggestPricing(product.id);
      showToast(`Generated pricing suggestion for ${product.sku}`);
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to generate pricing', 'error');
    }
  };

  const handleManualReorder = async (product: Product) => {
    try {
      await api.suggestReorder(product.id);
      showToast(`Generated reorder suggestion for ${product.sku}`);
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to generate reorder', 'error');
    }
  };

  const handleCreateProduct = async (data: any) => {
    try {
      const created = await api.createProduct(data);
      showToast(`Created product: ${created.name} (${created.sku})!`);
      await loadData();
    } catch (err: any) {
      showToast(err.message || 'Failed to create product', 'error');
    }
  };

  return (
    <div className="app-container">
      <Header
        strategy={strategy}
        onStrategyChange={handleStrategyChange}
        onRefresh={() => loadData()}
        isRefreshing={isRefreshing}
      />

      {toastMessage && (
        <div className={`toast ${toastMessage.type}`}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {toastMessage.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
            <span>{toastMessage.text}</span>
          </div>
        </div>
      )}

      {/* Agentic Flow Banner */}
      <div
        style={{
          background: 'linear-gradient(90deg, #1e1b4b, #1e293b)',
          border: '1px solid #3730a3',
          padding: '14px 20px',
          borderRadius: '10px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '12px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Sparkles size={22} color="#a5b4fc" />
          <div>
            <div style={{ fontSize: '14px', fontWeight: 600, color: '#e0e7ff' }}>
              Autonomous Inventory Replenishment & Dynamic Pricing Engine
            </div>
            <div style={{ fontSize: '12px', color: '#94a3b8' }}>
              Order event &rarr; signal detection &rarr; async AI advisor (or rule fallback) &rarr; pending human approval. AI never mutates products automatically.
            </div>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        {/* Pending Recommendations Section */}
        <div className="section-card">
          <div className="section-header">
            <div className="section-title">
              <Sparkles size={20} color="#a855f7" />
              <span>Pending Action Recommendations</span>
              <span className="badge-count">
                {pricingSuggestions.length + reorderSuggestions.length}
              </span>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
            {/* Dynamic Pricing Column */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
                <DollarSign size={18} color="#6366f1" />
                <h3 style={{ fontSize: '15px', fontWeight: 600 }}>
                  Pricing Recommendations ({pricingSuggestions.length})
                </h3>
              </div>

              {pricingSuggestions.length === 0 ? (
                <div className="empty-state">
                  No pending pricing recommendations. Simulate a sale or trigger manual suggestion.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {pricingSuggestions.map((s) => (
                    <PricingSuggestionCard
                      key={s.id}
                      suggestion={s}
                      onAccept={handleAcceptPricing}
                      onReject={handleRejectPricing}
                    />
                  ))}
                </div>
              )}
            </div>

            {/* Inventory Reorder Column */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
                <Boxes size={18} color="#06b6d4" />
                <h3 style={{ fontSize: '15px', fontWeight: 600 }}>
                  Reorder Recommendations ({reorderSuggestions.length})
                </h3>
              </div>

              {reorderSuggestions.length === 0 ? (
                <div className="empty-state">
                  No pending reorder recommendations. All stock levels are currently healthy.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {reorderSuggestions.map((s) => (
                    <ReorderSuggestionCard
                      key={s.id}
                      suggestion={s}
                      onAccept={handleAcceptReorder}
                      onReject={handleRejectReorder}
                    />
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Product Inventory Section */}
        <div className="section-card">
          <div className="section-header">
            <div className="section-title">
              <Package size={20} color="#6366f1" />
              <span>Catalog & Inventory Status</span>
              <span className="badge-count">{products.length} Products</span>
            </div>

            <div className="filter-bar">
              <input
                type="text"
                placeholder="Search products..."
                className="search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />

              <select
                className="category-filter"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value as Category | '')}
              >
                <option value="">All Categories</option>
                <option value="ELECTRONICS">Electronics</option>
                <option value="APPAREL">Apparel</option>
                <option value="HOME">Home</option>
              </select>

              <button
                className="btn btn-primary btn-sm"
                onClick={() => setIsAddProductModalOpen(true)}
                id="btn-add-product"
              >
                <PlusCircle size={14} /> Add Product
              </button>
            </div>
          </div>

          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>
              <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 10px' }} />
              Loading inventory catalog...
            </div>
          ) : (
            <ProductsTable
              products={products}
              onSimulateSale={(p) => {
                setSaleProduct(p);
                setIsSaleModalOpen(true);
              }}
              onManualPricing={handleManualPricing}
              onManualReorder={handleManualReorder}
            />
          )}
        </div>
      </div>

      <SimulateSaleModal
        product={saleProduct}
        isOpen={isSaleModalOpen}
        onClose={() => {
          setIsSaleModalOpen(false);
          setSaleProduct(null);
        }}
        onSubmit={handleSimulateSaleSubmit}
      />

      <AddProductModal
        isOpen={isAddProductModalOpen}
        onClose={() => setIsAddProductModalOpen(false)}
        onSubmit={handleCreateProduct}
      />
    </div>
  );
};

export default App;
