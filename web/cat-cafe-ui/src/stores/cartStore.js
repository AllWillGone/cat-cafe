import { reactive, computed } from 'vue'

const STORAGE_KEY = 'cat_cafe_cart'

function loadCart() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveCart(items) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
}

const state = reactive({
  items: loadCart(),
})

export default {
  state,

  addItem(product, quantity = 1) {
    const existing = state.items.find(i => i.productId === product.productId)
    if (existing) {
      existing.quantity = Math.min(existing.quantity + quantity, existing.maxStock)
    } else {
      state.items.push({
        productId: product.productId,
        productName: product.productName,
        price: product.price,
        imageUrl: product.imageUrl,
        quantity,
        maxStock: product.stockQuantity,
      })
    }
    saveCart(state.items)
  },

  removeItem(productId) {
    state.items = state.items.filter(i => i.productId !== productId)
    saveCart(state.items)
  },

  updateQuantity(productId, quantity) {
    const item = state.items.find(i => i.productId === productId)
    if (item) {
      item.quantity = Math.max(1, Math.min(quantity, item.maxStock))
      saveCart(state.items)
    }
  },

  clearCart() {
    state.items = []
    saveCart(state.items)
  },

  get totalCount() {
    return state.items.reduce((sum, i) => sum + i.quantity, 0)
  },

  get totalPrice() {
    return state.items.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0).toFixed(2)
  },
}
