import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost/resort-pos/backend',
        changeOrigin: true,
        rewrite: (path) => `/index.php?route=${path.replace(/^\/api\/?/, '')}`
      }
    }
  }
});
