<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
        <div>
          <el-button @click="$router.push('/admin/surveys')" icon="ArrowLeft">返回</el-button>
          <span style="margin-left: 16px; font-size: 18px; font-weight: bold">问卷数据管理</span>
        </div>
        <el-button type="success" @click="handleExport" :loading="exportLoading">导出Excel</el-button>
      </div>

      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="id" label="答卷ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="submitTime" label="提交时间" width="200" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row.id)">查看详情</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="答卷详情" width="650px">
      <div v-for="(item, idx) in detailData" :key="idx" style="margin-bottom: 16px; padding: 12px; background: #f5f7fa; border-radius: 6px">
        <div style="font-weight: bold; margin-bottom: 6px">
          {{ idx + 1 }}. {{ item.questionTitle }}
          <el-tag size="small" style="margin-left: 8px">{{ typeLabel(item.questionType) }}</el-tag>
        </div>
        <div v-if="item.selectedOptions && item.selectedOptions.length" style="color: #409eff">
          {{ item.selectedOptions.join('、') }}
        </div>
        <div v-else-if="item.content" style="color: #409eff">{{ item.content }}</div>
        <div v-else style="color: #999">未作答</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAnswerSheets, getAnswerDetail, deleteAnswerSheet } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const surveyId = Number(route.params.id)
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const exportLoading = ref(false)

const detailVisible = ref(false)
const detailData = ref([])

const typeLabel = (t) => ({ RADIO: '单选题', CHECKBOX: '多选题', INPUT: '填空题' }[t] || t)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAnswerSheets(surveyId, { page: page.value, size: size.value })
    tableData.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

const viewDetail = async (sheetId) => {
  const res = await getAnswerDetail(sheetId)
  detailData.value = res.data
  detailVisible.value = true
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该答卷？', '提示', { type: 'warning' })
  await deleteAnswerSheet(id)
  ElMessage.success('删除成功')
  loadData()
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/answers/export/${surveyId}`, {
      responseType: 'blob',
      headers: { Authorization: `Bearer ${token}` }
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `survey_${surveyId}_data.xlsx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(loadData)
</script>
