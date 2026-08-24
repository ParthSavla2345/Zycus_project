import {
  Product,
  PricingSuggestion,
  ReorderSuggestion,
  OrderRequest,
  SuggestionActionRequest,
  StrategyResponse,
  StrategyUsed,
  Category,
  SuggestionStatus,
} from '../types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    let errorMsg = `HTTP Error ${res.status}: ${res.statusText}`;
    try {
      const errorJson = await res.json();
      errorMsg = errorJson.message || errorJson.error || errorMsg;
    } catch {
      // ignore
    }
    throw new Error(errorMsg);
  }

  return res.json();
}

export const api = {
  // Products
  getProducts: (category?: Category, search?: string) => {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    if (search) params.append('search', search);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return fetchJson<Product[]>(`/products${queryString}`);
  },

  getProductById: (id: number) => fetchJson<Product>(`/products/${id}`),

  createProduct: (data: any) =>
    fetchJson<Product>('/products', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  createOrder: (productId: number, data: OrderRequest) =>
    fetchJson<Product>(`/products/${productId}/orders`, {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateStock: (productId: number, stockLevel: number) =>
    fetchJson<Product>(`/products/${productId}/stock`, {
      method: 'PATCH',
      body: JSON.stringify({ stockLevel }),
    }),

  suggestPricing: (productId: number) =>
    fetchJson<PricingSuggestion>(`/products/${productId}/suggest-pricing`, {
      method: 'POST',
    }),

  suggestReorder: (productId: number) =>
    fetchJson<ReorderSuggestion>(`/products/${productId}/suggest-reorder`, {
      method: 'POST',
    }),

  // Suggestions
  getPricingSuggestions: (status?: SuggestionStatus) => {
    const url = status ? `/pricing-suggestions?status=${status}` : '/pricing-suggestions';
    return fetchJson<PricingSuggestion[]>(url);
  },

  processPricingAction: (id: number, action: 'ACCEPT' | 'REJECT') =>
    fetchJson<PricingSuggestion>(`/pricing-suggestions/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ action } as SuggestionActionRequest),
    }),

  getReorderSuggestions: (status?: SuggestionStatus) => {
    const url = status ? `/reorder-suggestions?status=${status}` : '/reorder-suggestions';
    return fetchJson<ReorderSuggestion[]>(url);
  },

  processReorderAction: (id: number, action: 'ACCEPT' | 'REJECT') =>
    fetchJson<ReorderSuggestion>(`/reorder-suggestions/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ action } as SuggestionActionRequest),
    }),

  // Strategy switch
  getStrategy: () => fetchJson<StrategyResponse>('/admin/strategy'),

  setStrategy: (strategy: StrategyUsed) =>
    fetchJson<StrategyResponse>('/admin/strategy', {
      method: 'PUT',
      body: JSON.stringify({ strategy }),
    }),
};
