export type Category = 'ELECTRONICS' | 'APPAREL' | 'HOME';
export type ProductStatus = 'ACTIVE' | 'OUT_OF_STOCK' | 'PRICE_REVIEW_PENDING';
export type Direction = 'INCREASE' | 'DECREASE' | 'HOLD';
export type SuggestionStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';
export type TriggerReason = 'INITIAL' | 'INVENTORY_LOW' | 'DEMAND_SPIKE' | 'MANUAL';
export type StrategyUsed = 'AI' | 'RULE';

export interface Product {
  id: number;
  sku: string;
  name: string;
  category: Category;
  currentPrice: number;
  stockLevel: number;
  reorderThreshold: number;
  demandVelocity: number;
  status: ProductStatus;
  costPrice?: number | null;
  supplierId?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  category: Category;
  currentPrice: number;
  stockLevel: number;
  reorderThreshold: number;
  demandVelocity: number;
  costPrice?: number | null;
  supplierId?: string | null;
}

export interface PricingSuggestion {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  currentPrice: number;
  recommendedPrice: number;
  direction: Direction;
  confidence: number;
  reasoning: string;
  status: SuggestionStatus;
  triggerReason: TriggerReason;
  strategyUsed: StrategyUsed;
  createdAt: string;
}

export interface ReorderSuggestion {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  currentStock: number;
  recommendedQuantity: number;
  suggestedLeadTimeDays: number;
  confidence: number;
  reasoning: string;
  status: SuggestionStatus;
  triggerReason: TriggerReason;
  strategyUsed: StrategyUsed;
  createdAt: string;
}

export interface OrderRequest {
  quantity: number;
}

export interface SuggestionActionRequest {
  action: 'ACCEPT' | 'REJECT';
}

export interface StrategyResponse {
  strategy: StrategyUsed;
}
