<template>
  <div class="page">
    <h3>我的订单</h3>
    <div class="toolbar">
      <el-select v-model="orderStatus" style="width: 120px" @change="fetchOrders">
        <el-option label="全部状态" :value="null" />
        <el-option label="未支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="待拿取" :value="2" />
        <el-option label="已拿取" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索商品名"
        clearable
        style="width: 200px; margin-left: 12px"
        @keyup.enter="fetchOrders"
        @clear="fetchOrders"
      />
      <el-button type="primary" style="margin-left: 12px" @click="fetchOrders">搜索</el-button>
    </div>
    <el-table :data="orders" border v-loading="loading" style="margin-top: 16px">
      <el-table-column label="批次号" width="150">
        <template #default="{ row }">
          <span v-if="row.batchNo" style="font-size:12px;font-family:monospace">{{ row.batchNo.slice(0,12) }}...</span>
          <span v-else style="color:#999">-</span>
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="200">
        <template #default="{ row }">
          <template v-for="item in row.items" :key="item.orderId">
            {{ item.productName }} x{{ item.productQuantity }}<br />
          </template>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="90">
        <template #default="{ row }">&yen;{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="['info','warning','','success','danger'][row.orderStatus]" size="small">
            {{ ['未支付','已支付','待拿取','已拿取','已取消'][row.orderStatus] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.orderStatus === 0">
            <el-button size="small" type="primary" @click="openPayDialog(row)">支付</el-button>
            <el-button size="small" type="danger" @click="cancelOrder(row)">取消</el-button>
          </template>
          <template v-else-if="row.orderStatus === 3 || row.orderStatus === 4">
            <el-button size="small" type="danger" plain @click="deleteOrder(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="payDialogVisible" title="扫码支付" width="360px" align-center>
      <div style="text-align:center">
        <img :src="qrCodeUrl" alt="支付二维码" style="width:200px;height:200px" />
        <p style="margin-top:12px;color:#666;font-size:13px">批次号: {{ payingBatchNo.slice(0, 18) }}{{ payingBatchNo.length > 18 ? '...' : '' }}</p>
        <p style="color:#333;font-size:20px;font-weight:bold">&yen;{{ payingAmount }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const orderStatus = ref(null)
const keyword = ref('')

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {}
    if (orderStatus.value !== null) params.orderStatus = orderStatus.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/orders', { params })
    orders.value = res.data.items
  } catch {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const payDialogVisible = ref(false)
const qrCodeUrl = ref('')
const payingBatchNo = ref('')
const payingAmount = ref(0)

const openPayDialog = (row) => {
  payingBatchNo.value = row.batchNo || ''
  payingAmount.value = row.totalAmount
  qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=PAY_${row.batchNo || 'N/A'}_${row.totalAmount}`
  payDialogVisible.value = true
}

const cancelOrder = async (row) => {
  const batchNo = row.batchNo || 'N/A'
  try {
    await ElMessageBox.confirm(`确定要取消该订单吗？（批次号: ${batchNo.slice(0, 12)}...）`, '确认取消', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      confirmButtonClass: 'el-button--danger',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await api.put(`/api/orders/${row.items[0].orderId}`, { orderStatus: 4 })
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '取消失败')
  }
}

const deleteOrder = async (row) => {
  const batchNo = row.batchNo || 'N/A'
  try {
    await ElMessageBox.confirm(`确定要删除该订单吗？（批次号: ${batchNo.slice(0, 12)}...）`, '确认删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '返回',
      confirmButtonClass: 'el-button--danger',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await api.delete(`/api/orders/${row.items[0].orderId}`)
    ElMessage.success('订单已删除')
    fetchOrders()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '删除失败')
  }
}

onMounted(() => fetchOrders())
</script>

<style scoped>
.page { max-width: 900px; margin: 0 auto; }
.toolbar { display: flex; align-items: center; margin-bottom: 0; }
</style>
