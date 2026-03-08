<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
        <div>
          <el-input v-model="keyword" placeholder="搜索问卷标题" style="width: 250px; margin-right: 10px" clearable @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 130px; margin-right: 10px" @change="loadData">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </div>
        <el-button type="success" @click="openDialog()">新建问卷</el-button>
        <el-button type="warning" @click="aiDialogVisible = true">AI智能生成</el-button>
      </div>

      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="问卷标题" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status).type">{{ statusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)" :disabled="row.status === 1">编辑</el-button>
            <el-button size="small" type="primary" @click="goQuestions(row.id)">题目</el-button>
            <el-button size="small" type="success" v-if="row.status === 0" @click="handlePublish(row.id)">发布</el-button>
            <el-button size="small" type="warning" v-if="row.status === 1" @click="handleEnd(row.id)">结束</el-button>
            <el-button size="small" type="info" v-if="row.status !== 0" @click="goData(row.id)">数据</el-button>
            <el-button size="small" type="primary" v-if="row.status !== 0" @click="goStats(row.id)">统计</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑问卷' : '新建问卷'" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="问卷标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入问卷标题" />
        </el-form-item>
        <el-form-item label="问卷说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入问卷说明" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI生成问卷对话框 -->
    <el-dialog v-model="aiDialogVisible" title="AI智能生成问卷" width="600px">
      <el-form label-width="90px">
        <el-form-item label="问卷描述">
          <el-input v-model="aiDescription" type="textarea" :rows="4" placeholder="请描述你想创建的问卷，例如：大学生消费习惯调查、员工满意度调研、产品用户体验反馈..." />
        </el-form-item>
      </el-form>
      <div v-if="aiResult" style="margin-top: 16px">
        <el-alert title="AI已生成问卷，请确认" type="success" :closable="false" style="margin-bottom: 12px" />
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="问卷标题">{{ aiResult.title }}</el-descriptions-item>
          <el-descriptions-item label="问卷说明">{{ aiResult.description }}</el-descriptions-item>
          <el-descriptions-item label="题目数量">{{ aiResult.questions ? aiResult.questions.length : 0 }} 道</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 12px; max-height: 300px; overflow-y: auto">
          <div v-for="(q, idx) in (aiResult.questions || [])" :key="idx" style="padding: 8px; margin-bottom: 6px; background: #f5f7fa; border-radius: 4px; font-size: 13px">
            <strong>{{ idx + 1 }}. [{{ {RADIO:'单选',CHECKBOX:'多选',INPUT:'填空'}[q.type] }}] {{ q.title }}</strong>
            <div v-if="q.options && q.options.length" style="color: #666; margin-top: 4px">
              选项: {{ q.options.map(o => o.content).join(' | ') }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false; aiResult = null">取消</el-button>
        <el-button type="warning" :loading="aiLoading" @click="handleAiGenerate" :disabled="!!aiResult">生成</el-button>
        <el-button v-if="aiResult" type="primary" :loading="aiSaving" @click="handleAiSave">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSurveyList, createSurvey, updateSurvey, deleteSurvey, publishSurvey, endSurvey, aiGenerateSurvey, addQuestion } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const statusFilter = ref(null)

const dialogVisible = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ title: '', description: '', startTime: null, endTime: null })
const formRules = { title: [{ required: true, message: '请输入问卷标题', trigger: 'blur' }] }

const statusTag = (s) => {
  const map = { 0: { label: '草稿', type: 'info' }, 1: { label: '已发布', type: 'success' }, 2: { label: '已结束', type: 'danger' } }
  return map[s] || { label: '未知', type: '' }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSurveyList({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value })
    tableData.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    editId.value = row.id
    Object.assign(form, { title: row.title, description: row.description, startTime: row.startTime, endTime: row.endTime })
  } else {
    editId.value = null
    Object.assign(form, { title: '', description: '', startTime: null, endTime: null })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await updateSurvey({ id: editId.value, ...form })
    } else {
      await createSurvey(form)
    }
    ElMessage.success(editId.value ? '修改成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该问卷？', '提示', { type: 'warning' })
  await deleteSurvey(id)
  ElMessage.success('删除成功')
  loadData()
}

const handlePublish = async (id) => {
  await ElMessageBox.confirm('确定发布该问卷？发布后不可修改', '提示', { type: 'warning' })
  await publishSurvey(id)
  ElMessage.success('发布成功')
  loadData()
}

const handleEnd = async (id) => {
  await ElMessageBox.confirm('确定结束该问卷？', '提示', { type: 'warning' })
  await endSurvey(id)
  ElMessage.success('已结束')
  loadData()
}

const goQuestions = (id) => router.push(`/admin/surveys/${id}/questions`)
const goData = (id) => router.push(`/admin/surveys/${id}/data`)
const goStats = (id) => router.push(`/admin/surveys/${id}/statistics`)

onMounted(loadData)

// ==================== AI生成问卷 ====================
const aiDialogVisible = ref(false)
const aiDescription = ref('')
const aiLoading = ref(false)
const aiSaving = ref(false)
const aiResult = ref(null)

const handleAiGenerate = async () => {
  if (!aiDescription.value.trim()) {
    ElMessage.warning('请输入问卷描述')
    return
  }
  aiLoading.value = true
  aiResult.value = null
  try {
    const res = await aiGenerateSurvey(aiDescription.value)
    aiResult.value = res.data
    ElMessage.success('AI问卷生成成功，请确认后创建')
  } catch {
    ElMessage.error('AI生成失败，请重试')
  } finally {
    aiLoading.value = false
  }
}

const handleAiSave = async () => {
  if (!aiResult.value) return
  aiSaving.value = true
  try {
    // 1. Create survey
    const surveyRes = await createSurvey({
      title: aiResult.value.title,
      description: aiResult.value.description
    })
    const surveyId = surveyRes.data.id

    // 2. Create questions
    const questions = aiResult.value.questions || []
    for (let i = 0; i < questions.length; i++) {
      const q = questions[i]
      await addQuestion({
        surveyId,
        type: q.type,
        title: q.title,
        required: q.required ?? 1,
        sortOrder: i,
        options: (q.options || []).map((o, j) => ({ content: o.content, sortOrder: j }))
      })
    }

    ElMessage.success(`问卷创建成功，共${questions.length}道题目`)
    aiDialogVisible.value = false
    aiResult.value = null
    aiDescription.value = ''
    loadData()
  } catch {
    ElMessage.error('创建失败，请重试')
  } finally {
    aiSaving.value = false
  }
}
</script>
