<template>
  <div class="cart-page">
    <h2>购物车</h2>

    <el-empty v-if="cart.state.items.length === 0" description="购物车是空的" />

    <template v-else>
      <el-table :data="cart.state.items" style="width: 100%">
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="item-info">
              <img v-if="row.imageUrl" :src="row.imageUrl" class="item-img" @error="onImgError" />
              <span v-else class="img-fallback">{{ row.productName.slice(0,1) }}</span>
              <span>{{ row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.maxStock"
              size="small"
              controls-position="right"
              @change="cart.updateQuantity(row.productId, row.quantity)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ (Number(row.price) * row.quantity).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" text size="small" @click="cart.removeItem(row.productId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <span class="total">合计：<strong>¥{{ cart.totalPrice }}</strong></span>
        <el-button type="primary" size="large" @click="showOrderDialog = true">提交订单</el-button>
      </div>
    </template>

    <el-dialog v-model="showOrderDialog" title="确认订单" width="420px">
      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="联系人">
          <el-input v-model="orderForm.userName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="orderForm.userPhone" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.orderNote" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import cart from '../stores/cartStore'
import api from '../api/index'

const router = useRouter()
const showOrderDialog = ref(false)
const submitting = ref(false)

const orderForm = reactive({
  userName: localStorage.getItem('userName') || '',
  userPhone: '',
  orderNote: '',
})

onMounted(async () => {
  try {
    const res = await api.get('/api/user/me')
    orderForm.userName = res.data.userName || orderForm.userName
    orderForm.userPhone = res.data.userPhone || ''
  } catch {}
})

function submitOrder() {
  if (!orderForm.userName.trim() || !orderForm.userPhone.trim()) {
    ElMessage.warning('请填写联系人和手机号')
    return
  }
  const items = cart.state.items.map(i => ({
    productId: i.productId,
    productQuantity: i.quantity,
  }))
  submitting.value = true
  api.post('/api/orders', {
    items,
    userName: orderForm.userName.trim(),
    userPhone: orderForm.userPhone.trim(),
    orderNote: orderForm.orderNote || null,
  }).then(() => {
    ElMessage.success('下单成功')
    cart.clearCart()
    router.push('/home/orders')
  }).catch(err => {
    ElMessage.error(err?.response?.data?.detail || '下单失败')
  }).finally(() => {
    submitting.value = false
  })
}

function onImgError(e) {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.cart-page { max-width: 900px; margin: 0 auto; padding: 20px; }
h2 { margin: 0 0 20px; }
.item-info { display: flex; align-items: center; gap: 10px; }
.item-img { width: 50px; height: 50px; object-fit: cover; border-radius: 6px; }
.img-fallback { width: 50px; height: 50px; border-radius: 6px; background: #f0f0f0; display: inline-flex; align-items: center; justify-content: center; font-size: 18px; color: #999; }
.cart-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.total { font-size: 16px; }
.total strong { color: #f56c6c; font-size: 20px; }
</style>
