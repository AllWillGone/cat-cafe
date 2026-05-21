<template>
  <div class="page">
    <h3>商品列表</h3>
    <div class="toolbar">
      <el-select v-model="category" style="width: 140px" @change="fetchProducts">
        <el-option label="全部分类" :value="null" />
        <el-option label="服务" :value="0" />
        <el-option label="餐饮" :value="1" />
        <el-option label="猫咪用品" :value="2" />
      </el-select>
      <el-select v-model="sortBy" style="width: 140px" @change="fetchProducts">
        <el-option label="默认排序" value="default" />
        <el-option label="点赞最多" value="likeCount" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索商品名"
        clearable
        style="width: 200px; margin-left: 12px"
        @keyup.enter="fetchProducts"
        @clear="fetchProducts"
      />
      <el-button type="primary" style="margin-left: 12px" @click="fetchProducts">搜索</el-button>
    </div>
    <el-table
      :data="products"
      border
      v-loading="loading"
      style="margin-top: 16px"
      @row-click="(row) => $router.push(`/home/products/${row.productId}`)"
    >
      <el-table-column prop="productId" label="ID" width="60" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img
            :src="row.imageUrl"
            style="width:50px;height:50px;border-radius:6px;object-fit:cover;cursor:pointer"
            @click.stop="previewProductImage(row.imageUrl)"
            @error="(e) => { e.target.style.display='none'; e.target.nextElementSibling.style.display='inline'; }"
          />
          <span style="font-size:24px;display:none">📦</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名" min-width="140" />
      <el-table-column label="分类" width="100">
        <template #default="{ row }">
          {{ ['服务','餐饮','猫咪用品'][row.category] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="价格" width="90">
        <template #default="{ row }">&yen;{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stockQuantity" label="库存" width="70" />
      <el-table-column prop="description" label="描述" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <el-button size="small" type="primary" @click.stop="openOrderDialog(row)">下单</el-button>
            <el-tooltip :content="row._liked ? '点击取消点赞' : ''" :disabled="!row._liked" placement="top">
              <el-button size="small" @click.stop="handleLike(row)" :class="{ 'is-liked': row._liked }">
                <svg viewBox="0 0 24 24" width="14" height="14" :fill="row._liked ? '#f56c6c' : 'none'" :stroke="row._liked ? '#f56c6c' : '#606266'" stroke-width="2" style="margin-right:2px;vertical-align:middle">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                {{ row.likeCount || 0 }}
              </el-button>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="orderDialogVisible" title="下单" width="480px">
      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="商品">
          <span>{{ orderForm.productName }}</span>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="orderForm.quantity" :min="1" :max="orderForm.maxStock" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="orderForm.paymentMethod">
            <el-option label="微信" :value="0" />
            <el-option label="支付宝" :value="1" />
            <el-option label="现金" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="orderForm.userName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="orderForm.userPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.orderNote" type="textarea" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>

    <teleport to="body">
      <div v-if="productPreviewVisible" class="image-preview-overlay" @click.self="productPreviewVisible = false">
        <img :src="productPreviewUrl" class="image-preview-img" />
        <button class="image-preview-close" @click="productPreviewVisible = false">✕</button>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage } from 'element-plus'

const products = ref([])
const loading = ref(false)
const category = ref(null)
const keyword = ref('')
const sortBy = ref('default')

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = { sortBy: sortBy.value }
    if (category.value !== null) params.category = category.value
    if (keyword.value) params.keyword = keyword.value
    const [productRes, myLikesRes] = await Promise.all([
      api.get('/api/products', { params }),
      api.get('/api/likes', { params: { likeType: 0 } }),
    ])
    const likeMap = new Map(myLikesRes.data.items.map(l => [l.objectId, l.likeId]))
    products.value = productRes.data.items.map(item => ({
      ...item,
      _liked: likeMap.has(item.productId),
      _likeId: likeMap.get(item.productId) || null,
    }))
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async (row) => {
  try {
    if (row._liked) {
      await api.delete(`/api/likes/${row._likeId}`)
      row._liked = false
      row._likeId = null
      row.likeCount = Math.max(0, (row.likeCount || 0) - 1)
      ElMessage.success('已取消点赞')
    } else {
      const res = await api.post('/api/likes', { likeType: 0, objectId: row.productId })
      row._liked = true
      row._likeId = res.data.likeId
      row.likeCount = (row.likeCount || 0) + 1
      ElMessage.success('点赞成功')
    }
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '操作失败')
  }
}

const orderDialogVisible = ref(false)
const submitting = ref(false)
const orderForm = ref({
  productId: 0,
  productName: '',
  quantity: 1,
  maxStock: 1,
  paymentMethod: 0,
  userName: '',
  userPhone: '',
  orderNote: '',
})

const openOrderDialog = (row) => {
  orderForm.value = {
    productId: row.productId,
    productName: row.productName,
    quantity: 1,
    maxStock: row.stockQuantity,
    paymentMethod: 0,
    userName: localStorage.getItem('userName') || '',
    userPhone: '',
    orderNote: '',
  }
  orderDialogVisible.value = true
}

const submitOrder = async () => {
  if (!orderForm.value.userName.trim()) {
    ElMessage.warning('请输入联系人姓名')
    return
  }
  if (!orderForm.value.userPhone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(orderForm.value.userPhone.trim())) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  submitting.value = true
  try {
    await api.post('/api/orders', {
      items: [{ productId: orderForm.value.productId, productQuantity: orderForm.value.quantity }],
      paymentMethod: orderForm.value.paymentMethod,
      userPhone: orderForm.value.userPhone,
      userName: orderForm.value.userName,
      orderNote: orderForm.value.orderNote || undefined,
    })
    ElMessage.success('下单成功')
    orderDialogVisible.value = false
    fetchProducts()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '下单失败')
  } finally {
    submitting.value = false
  }
}

const productPreviewUrl = ref('')
const productPreviewVisible = ref(false)
const previewProductImage = (url) => {
  productPreviewUrl.value = url
  productPreviewVisible.value = true
}

onMounted(() => fetchProducts())
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; }
.toolbar { display: flex; align-items: center; }
.is-liked { color: #f56c6c; border-color: #f56c6c; }
.action-btns { display: flex; gap: 6px; align-items: center; }
</style>

<style>
.image-preview-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(0,0,0,0.85);
  display: flex; align-items: center; justify-content: center;
  pointer-events: auto;
}
.image-preview-img { max-width: 90vw; max-height: 90vh; object-fit: contain; border-radius: 8px; }
.image-preview-close {
  position: fixed; top: 20px; right: 20px;
  width: 44px; height: 44px; border: none;
  background: rgba(255,255,255,0.15); color: #fff;
  font-size: 24px; border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  z-index: 10000;
}
.image-preview-close:hover { background: rgba(255,255,255,0.3); }
</style>
