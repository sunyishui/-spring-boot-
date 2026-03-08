<template>
  <div>
    <div style="margin-bottom: 16px; display: flex; align-items: center">
      <el-input v-model="keyword" placeholder="搜索问卷" style="width: 300px; margin-right: 10px" clearable @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in surveyList" :key="item.id" style="margin-bottom: 16px">
        <el-card shadow="hover" style="cursor: pointer; height: 100%" @click="goFill(item.id)">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold; font-size: 16px">{{ item.title }}</span>
              <el-tag type="success" size="small">进行中</el-tag>
            </div>
          </template>
          <p style="color: #666; margin-bottom: 12px; min-height: 40px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical">
            {{ item.description || '暂无说明' }}
          </p>
          <div style="color: #999; font-size: 13px">
            <div v-if="item.startTime">开始: {{ item.startTime }}</div>
            <div v-if="item.endTime">截止: {{ item.endTime }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && surveyList.length === 0" description="暂无可填写的问卷" />

    <el-pagination
      v-if="total > size"
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="prev, pager, next"
      style="margin-top: 16px; justify-content: center"
      @current-change="loadData"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublishedSurveys } from '@/api'

const router = useRouter()
const loading = ref(false)
const surveyList = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)
const keyword = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPublishedSurveys({ page: page.value, size: size.value, keyword: keyword.value })
    surveyList.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

const goFill = (id) => router.push(`/surveys/${id}/fill`)

onMounted(loadData)
</script>
