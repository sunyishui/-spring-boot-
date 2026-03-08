<template>
  <div>
    <el-card>
      <template #header>
        <span style="font-size: 18px; font-weight: bold">我的答卷记录</span>
      </template>

      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="id" label="答卷ID" width="90" />
        <el-table-column prop="surveyId" label="问卷ID" width="90" />
        <el-table-column prop="submitTime" label="提交时间" width="200" />
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="success">已提交</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="您还没有填写过问卷" />

      <el-pagination
        v-if="total > size"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20]"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyAnswers } from '@/api'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyAnswers({ page: page.value, size: size.value })
    tableData.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
