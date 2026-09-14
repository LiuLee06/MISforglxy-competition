import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ command }) => ({
  // 生产构建用 /mis/ 子路径，开发服务器用根路径方便本地调试
  base: command === 'build' ? '/mis/' : '/',
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/mis-api': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/mis-api/, '')
      },
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/file': {
        target: 'http://localhost:8888',
        changeOrigin: true
      }
    }
  }
}))
