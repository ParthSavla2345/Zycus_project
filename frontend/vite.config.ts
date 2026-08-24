import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
    strictPort: true,
    host: true,
    proxy: {
      '/products': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/pricing-suggestions': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/reorder-suggestions': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
