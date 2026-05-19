import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'   // 后面会创建

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')