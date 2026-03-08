<template>
  <div>
    <el-card>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px">
        <div>
          <el-button @click="$router.push('/admin/surveys')" icon="ArrowLeft">返回</el-button>
          <span style="margin-left: 16px; font-size: 18px; font-weight: bold">{{ surveyTitle }}</span>
        </div>
        <el-button type="success" @click="openDialog()">添加题目</el-button>
      </div>

      <el-table :data="questions" stripe border v-loading="loading">
        <el-table-column prop="sortOrder" label="序号" width="70" />
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="题目标题" min-width="250" />
        <el-table-column label="必填" width="80">
          <template #default="{ row }">
            <el-tag :type="row.required ? 'danger' : 'info'" size="small">{{ row.required ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="选项数" width="80">
          <template #default="{ row }">{{ row.options ? row.options.length : '-' }}</template>
        </el-table-column>
        <el-table-column label="排序" width="120">
          <template #default="{ row, $index }">
            <el-button size="small" :disabled="$index === 0" @click="moveUp($index)">上</el-button>
            <el-button size="small" :disabled="$index === questions.length - 1" @click="moveDown($index)">下</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editId ? '编辑题目' : '添加题目'" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="form.type" @change="onTypeChange">
            <el-option label="单选题" value="RADIO" />
            <el-option label="多选题" value="CHECKBOX" />
            <el-option label="填空题" value="INPUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目标题" />
        </el-form-item>
        <el-form-item label="是否必填">
          <el-switch v-model="form.required" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="选项" v-if="form.type !== 'INPUT'">
          <div style="width: 100%">
            <div v-for="(opt, idx) in form.options" :key="idx" style="display: flex; margin-bottom: 8px; align-items: center">
              <el-input v-model="opt.content" :placeholder="'选项' + (idx + 1)" style="flex: 1" />
              <el-button type="danger" text @click="removeOption(idx)" :disabled="form.options.length <= 2" style="margin-left: 8px">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" text @click="addOption">+ 添加选项</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSurveyDetail, addQuestion, updateQuestion, deleteQuestion, reorderQuestions } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const surveyId = Number(route.params.id)
const surveyTitle = ref('')
const loading = ref(false)
const questions = ref([])

const dialogVisible = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ type: 'RADIO', title: '', required: 1, options: [{ content: '', sortOrder: 0 }, { content: '', sortOrder: 1 }] })
const formRules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  title: [{ required: true, message: '请输入题目标题', trigger: 'blur' }]
}

const typeLabel = (t) => ({ RADIO: '单选题', CHECKBOX: '多选题', INPUT: '填空题' }[t] || t)
const typeTag = (t) => ({ RADIO: '', CHECKBOX: 'warning', INPUT: 'success' }[t] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSurveyDetail(surveyId)
    surveyTitle.value = res.data.survey.title
    questions.value = res.data.questions
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    editId.value = row.id
    form.type = row.type
    form.title = row.title
    form.required = row.required
    form.options = row.options ? row.options.map(o => ({ ...o })) : [{ content: '', sortOrder: 0 }, { content: '', sortOrder: 1 }]
  } else {
    editId.value = null
    form.type = 'RADIO'
    form.title = ''
    form.required = 1
    form.options = [{ content: '', sortOrder: 0 }, { content: '', sortOrder: 1 }]
  }
  dialogVisible.value = true
}

const onTypeChange = () => {
  if (form.type === 'INPUT') {
    form.options = []
  } else if (form.options.length < 2) {
    form.options = [{ content: '', sortOrder: 0 }, { content: '', sortOrder: 1 }]
  }
}

const addOption = () => form.options.push({ content: '', sortOrder: form.options.length })
const removeOption = (idx) => form.options.splice(idx, 1)

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.type !== 'INPUT') {
    const empty = form.options.some(o => !o.content.trim())
    if (empty) { ElMessage.warning('请填写所有选项内容'); return }
  }
  submitLoading.value = true
  try {
    const data = { surveyId, type: form.type, title: form.title, required: form.required, sortOrder: questions.value.length, options: form.type !== 'INPUT' ? form.options.map((o, i) => ({ ...o, sortOrder: i })) : [] }
    if (editId.value) {
      data.id = editId.value
      await updateQuestion(data)
    } else {
      await addQuestion(data)
    }
    ElMessage.success(editId.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该题目？', '提示', { type: 'warning' })
  await deleteQuestion(id)
  ElMessage.success('删除成功')
  loadData()
}

const moveUp = async (idx) => {
  const ids = questions.value.map(q => q.id);
  [ids[idx], ids[idx - 1]] = [ids[idx - 1], ids[idx]]
  await reorderQuestions(surveyId, ids)
  loadData()
}

const moveDown = async (idx) => {
  const ids = questions.value.map(q => q.id);
  [ids[idx], ids[idx + 1]] = [ids[idx + 1], ids[idx]]
  await reorderQuestions(surveyId, ids)
  loadData()
}

onMounted(loadData)
</script>
