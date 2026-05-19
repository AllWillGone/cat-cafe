<template>
  <div>
    <h3>商品管理</h3>

    <div class="toolbar">
      <el-select v-model="category" style="width: 130px" @change="search">
        <el-option label="全部分类" :value="null" />
        <el-option label="服务" :value="0" />
        <el-option label="餐饮" :value="1" />
        <el-option label="猫咪用品" :value="2" />
      </el-select>
      <el-select v-model="status" style="width: 130px; margin-left: 12px" @change="search">
        <el-option label="全部状态" :value="null" />
        <el-option label="在售" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索商品名"
        clearable
        style="width: 200px; margin-left: 12px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" style="margin-left: 12px" @click="search">搜索</el-button>
      <el-button type="success" style="margin-left: auto" @click="openAdd">新增商品</el-button>
    </div>

    <el-table :data="products" border v-loading="loading" style="margin-top: 16px">
      <el-table-column prop="productId" label="ID" width="60" />
      <el-table-column prop="productName" label="商品名" min-width="160" />
      <el-table-column label="分类" width="100">
        <template #default="{ row }">
          {{ categoryMap[row.category] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column label="价格" width="90">
        <template #default="{ row }">&yen;{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stockQuantity" label="库存" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '在售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
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
        @current-change="fetchProducts"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名">
          <el-input v-model="form.productName" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option :value="0" label="服务" />
            <el-option :value="1" label="餐饮" />
            <el-option :value="2" label="猫咪用品" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stockQuantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" maxlength="255" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" maxlength="500" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在售"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api/index'
import { ElMessage, ElMessageBox } from 'element-plus'

const products = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const category = ref(null)
const status = ref(null)
const keyword = ref('')

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const form = reactive({
  productName: '',
  category: 1,
  price: 0.01,
  stockQuantity: 0,
  imageUrl: '',
  description: '',
  status: 1,
})

const categoryMap = { 0: '服务', 1: '餐饮', 2: '猫咪用品' }

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      skip: (page.value - 1) * pageSize.value,
      limit: pageSize.value,
    }
    if (category.value !== null) params.category = category.value
    if (status.value !== null) params.status = status.value
    if (keyword.value) params.keyword = keyword.value
    const res = await api.get('/api/admin/products', { params })
    products.value = res.data.items
    total.value = res.data.total
  } catch {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  fetchProducts()
}

const resetForm = () => {
  form.productName = ''
  form.category = 1
  form.price = 0.01
  form.stockQuantity = 0
  form.imageUrl = ''
  form.description = ''
  form.status = 1
}

const openAdd = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  editingId.value = row.productId
  form.productName = row.productName
  form.category = row.category
  form.price = Number(row.price)
  form.stockQuantity = row.stockQuantity
  form.imageUrl = row.imageUrl || ''
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

const saveProduct = async () => {
  saving.value = true
  try {
    const body = {
      productName: form.productName,
      category: form.category,
      price: form.price,
      stockQuantity: form.stockQuantity,
      imageUrl: form.imageUrl,
      description: form.description || null,
      status: form.status,
    }
    if (isEdit.value) {
      await api.put(`/api/admin/products/${editingId.value}`, body)
      ElMessage.success('商品已更新')
    } else {
      await api.post('/api/admin/products', body)
      ElMessage.success('商品已添加')
    }
    dialogVisible.value = false
    fetchProducts()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '保存失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  if (row.status === 1) {
    try {
      await api.put(`/api/admin/products/${row.productId}/off`)
      ElMessage.success('已下架')
      fetchProducts()
    } catch (err) {
      ElMessage.error(err.response?.data?.detail || '下架失败')
    }
  } else {
    try {
      await api.put(`/api/admin/products/${row.productId}`, { status: 1 })
      ElMessage.success('已上架')
      fetchProducts()
    } catch (err) {
      ElMessage.error(err.response?.data?.detail || '上架失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.productName}」吗？`, '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await api.delete(`/api/admin/products/${row.productId}`)
    ElMessage.success('商品已删除')
    fetchProducts()
  } catch (err) {
    ElMessage.error(err.response?.data?.detail || '删除失败')
  }
}

onMounted(() => {
  fetchProducts()
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
