<template>
  <div style="max-width: 800px; margin: 0 auto">
    <el-card v-if="survey" style="margin-bottom: 20px">
      <h2 style="margin-bottom: 8px">{{ survey.title }}</h2>
      <p style="color: #666">{{ survey.description }}</p>
    </el-card>

    <el-card v-for="(q, idx) in questions" :key="q.id" style="margin-bottom: 16px">
      <div style="margin-bottom: 12px">
        <span style="font-weight: bold; font-size: 15px">{{ idx + 1 }}. {{ q.title }}</span>
        <el-tag v-if="q.required" type="danger" size="small" style="margin-left: 8px">必填</el-tag>
        <el-tag size="small" style="margin-left: 4px">{{ typeLabel(q.type) }}</el-tag>
      </div>

      <!-- Radio -->
      <el-radio-group v-if="q.type === 'RADIO'" v-model="answers[q.id].optionIds[0]" style="display: flex; flex-direction: column; gap: 8px">
        <el-radio v-for="opt in q.options" :key="opt.id" :value="opt.id">{{ opt.content }}</el-radio>
      </el-radio-group>

      <!-- Checkbox -->
      <el-checkbox-group v-else-if="q.type === 'CHECKBOX'" v-model="answers[q.id].optionIds" style="display: flex; flex-direction: column; gap: 8px">
        <el-checkbox v-for="opt in q.options" :key="opt.id" :value="opt.id" :label="opt.content" />
      </el-checkbox-group>

      <!-- Input -->
      <el-input v-else v-model="answers[q.id].content" type="textarea" :rows="3" placeholder="请输入您的回答" />
    </el-card>

    <div style="text-align: center; margin: 20px 0" v-if="questions.length > 0">
      <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" style="width: 200px">提交问卷</el-button>
    </div>

    <el-empty v-if="!loading && questions.length === 0" description="问卷暂无题目" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSurveyDetail, submitAnswer } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const surveyId = Number(route.params.id)
const loading = ref(true)
const submitting = ref(false)
const survey = ref(null)
const questions = ref([])
const answers = reactive({})

const typeLabel = (t) => ({ RADIO: '单选题', CHECKBOX: '多选题', INPUT: '填空题' }[t] || t)

const loadData = async () => {
  try {
    const res = await getSurveyDetail(surveyId)
    survey.value = res.data.survey
    questions.value = res.data.questions
    // Initialize answer model
    questions.value.forEach(q => {
      answers[q.id] = { questionId: q.id, optionIds: [], content: '' }
    })
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  // Validate required questions
  for (const q of questions.value) {
    const a = answers[q.id]
    if (q.required) {
      if (q.type === 'INPUT' && !a.content.trim()) {
        ElMessage.warning(`请填写第${questions.value.indexOf(q) + 1}题: ${q.title}`)
        return
      }
      if ((q.type === 'RADIO' || q.type === 'CHECKBOX') && a.optionIds.length === 0) {
        ElMessage.warning(`请完成第${questions.value.indexOf(q) + 1}题: ${q.title}`)
        return
      }
    }
  }

  submitting.value = true
  try {
    const answerList = questions.value.map(q => {
      const a = answers[q.id]
      return {
        questionId: q.id,
        optionIds: q.type === 'RADIO' ? (a.optionIds[0] ? [a.optionIds[0]] : []) : a.optionIds,
        content: a.content
      }
    })
    await submitAnswer({ surveyId, answers: answerList })
    ElMessage.success('提交成功，感谢您的参与！')
    router.push('/surveys')
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
