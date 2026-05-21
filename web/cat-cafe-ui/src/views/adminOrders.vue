<template>
  <div>
    <h3>订单状态管理</h3>

    <div class="toolbar">
      <el-select v-model="orderStatus" style="width: 140px" @change="search">
        <el-option label="全部订单" :value="null" />
        <el-option label="未支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="待拿取" :value="2" />
        <el-option label="已拿取" :value="3" />
        <el-option label="已取消" :value="4" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索批次号 / 用户名"
        clearable
        style="width: 260px; margin-left: 12px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" style="margin-left: 12px" @click="search">搜索</el-button>
    </div>

    <el-table :data="orders" border v-loading="loading" style="margin-top: 16px">
      <el-table-column label="批次号" width="160">
        <template #default="{ row }">
          <span v-if="row.batchNo" style="font-size: 12px; font-family: monospace">
            {{ row.batchNo.slice(0, 12) }}...
          </span>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
      <el-table-column label="商品摘要" min-width="200">
        <template #default="{ row }">
          <template v-for="item in row.items" :key="item.orderId">
            <span>{{ item.productName }} x{{ item.productQuantity }}</span>
            <br />
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户名" width="110" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">
          &yen;{{ row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag
            :type="statusType(row.orderStatus)"
            size="small"
          >
            {{ statusText(row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-select
            :model-value="row.orderStatus"
            size="small"
            style="width: 130px"
            @change="(val) => changeStatus(row, val)"
          >
            <el-option :value="0" label="未支付" />
            <el-option :value="1" label="已支付" />
            <el-option :value="2" label="待拿取" />
            <el-option :value="3" label="已拿取" />
            <el-option :value="4" label="已取消" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="删除" width="80" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="danger"
            plain
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const orderStatus = ref(null)
const keyword = ref('')

const statusMap = { 0: '未支付', 1: '已支付', 2: '待拿取', 3: '已拿取', 4: '已取消' }
const statusTypeMap = { 0: 'info', 1: 'warning', 2: '', 3: 'success', 4: 'danger' }

const statusText = (s) => statusMap[s] || '未知'
const statusType = (s) => statusTypeMap[s] || 'info'

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      skip: (page.value - 1) * pageSize.value,
      limit: pageSize.value,
    }
    if (orderStatus.value !== null) params.orderStatus = orderStatus.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/admin/orders', { params })
    orders.value = res.data.items
    total.value = res.data.total
  } catch {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  fetchOrders()
}

const changeStatus = async (row, newStatus) => {
  try {
    await Promise.all(
      row.items.map(item => api.put(`/api/orders/${item.orderId}`, { orderStatus: newStatus }))
    )
    ElMessage.success(`订单状态已更新为「${statusText(newStatus)}」`)
    fetchOrders()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该订单吗？', '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await Promise.all(
      row.items.map(item => api.delete(`/api/orders/${item.orderId}`))
    )
    ElMessage.success('订单已删除')
    fetchOrders()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '删除失败')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
