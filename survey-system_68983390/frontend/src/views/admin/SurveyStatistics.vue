<template>
  <div>
    <el-card style="margin-bottom: 16px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-button @click="$router.push('/admin/surveys')" icon="ArrowLeft">返回</el-button>
          <span style="margin-left: 16px; font-size: 18px; font-weight: bold">{{ stats.surveyTitle }}</span>
        </div>
        <div style="display: flex; align-items: center; gap: 20px">
          <el-statistic title="总参与人数" :value="stats.totalAnswers || 0" />
          <el-button type="warning" :loading="reportLoading" @click="handleAiReport">AI分析报告</el-button>
        </div>
      </div>
    </el-card>

    <!-- AI分析报告卡片 -->
    <el-card v-if="aiReport" style="margin-bottom: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold; color: #e6a23c">AI 智能分析报告</span>
          <el-button text type="danger" @click="aiReport = ''">关闭</el-button>
        </div>
      </template>
      <div class="ai-report" v-html="renderMarkdown(aiReport)"></div>
    </el-card>

    <div v-for="(q, idx) in stats.questions || []" :key="q.id">
      <el-card style="margin-bottom: 16px">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <div>
              <span>{{ idx + 1 }}. {{ q.title }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ typeLabel(q.type) }}</el-tag>
            </div>
            <el-button v-if="q.type === 'INPUT'" type="warning" size="small" :loading="summaryLoading[q.id]" @click="handleSummarize(q.id)">
              AI摘要
            </el-button>
          </div>
        </template>
        <!-- AI摘要结果 -->
        <div v-if="summaryResults[q.id]" style="margin-bottom: 16px; padding: 12px; background: #fdf6ec; border-radius: 6px; border-left: 4px solid #e6a23c">
          <div style="font-weight: bold; color: #e6a23c; margin-bottom: 8px">AI 智能摘要</div>
          <div class="ai-report" v-html="renderMarkdown(summaryResults[q.id])"></div>
        </div>
        <div v-if="q.type === 'INPUT'" :ref="el => setChartRef(el, 'wordcloud_' + q.id)" style="height: 300px"></div>
        <div v-else-if="q.type === 'RADIO'" :ref="el => setChartRef(el, 'bar_' + q.id)" style="height: 350px"></div>
        <div v-else :ref="el => setChartRef(el, 'pie_' + q.id)" style="height: 350px"></div>
      </el-card>
    </div>

    <el-empty v-if="!loading && (!stats.questions || stats.questions.length === 0)" description="暂无统计数据" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getStatistics, aiAnalyzeReport, aiSummarizeText } from '@/api'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import 'echarts-wordcloud'

const route = useRoute()
const surveyId = Number(route.params.id)
const loading = ref(true)
const stats = ref({})
const chartRefs = {}

// AI Report
const reportLoading = ref(false)
const aiReport = ref('')

// AI Summarize
const summaryLoading = reactive({})
const summaryResults = reactive({})

const typeLabel = (t) => ({ RADIO: '单选题', CHECKBOX: '多选题', INPUT: '填空题' }[t] || t)

const setChartRef = (el, key) => {
  if (el) chartRefs[key] = el
}

// Simple markdown to HTML renderer (handles bold, headers, lists)
const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/### (.*)/g, '<h4 style="margin:12px 0 6px">$1</h4>')
    .replace(/## (.*)/g, '<h3 style="margin:14px 0 8px">$1</h3>')
    .replace(/# (.*)/g, '<h2 style="margin:16px 0 8px">$1</h2>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/^\- (.*)/gm, '<li style="margin-left:16px">$1</li>')
    .replace(/^\d+\. (.*)/gm, '<li style="margin-left:16px">$1</li>')
    .replace(/\n/g, '<br>')
}

const handleAiReport = async () => {
  reportLoading.value = true
  try {
    const res = await aiAnalyzeReport(surveyId)
    aiReport.value = res.data
    ElMessage.success('分析报告已生成')
  } catch {
    ElMessage.error('生成报告失败，请重试')
  } finally {
    reportLoading.value = false
  }
}

const handleSummarize = async (questionId) => {
  summaryLoading[questionId] = true
  try {
    const res = await aiSummarizeText(questionId)
    summaryResults[questionId] = res.data
    ElMessage.success('摘要已生成')
  } catch {
    ElMessage.error('摘要生成失败，请重试')
  } finally {
    summaryLoading[questionId] = false
  }
}

const renderCharts = () => {
  if (!stats.value.questions) return
  stats.value.questions.forEach(q => {
    if (q.type === 'RADIO' && q.optionStats) {
      const el = chartRefs['bar_' + q.id]
      if (!el) return
      const chart = echarts.init(el)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: q.optionStats.map(o => o.content), axisLabel: { interval: 0, rotate: q.optionStats.length > 5 ? 30 : 0 } },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{ type: 'bar', data: q.optionStats.map(o => o.count), itemStyle: { color: '#409eff' }, barMaxWidth: 50 }],
        grid: { left: 60, right: 30, bottom: 60, top: 20 }
      })
    } else if (q.type === 'CHECKBOX' && q.optionStats) {
      const el = chartRefs['pie_' + q.id]
      if (!el) return
      const chart = echarts.init(el)
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{ type: 'pie', radius: ['40%', '70%'], data: q.optionStats.map(o => ({ name: o.content, value: o.count })), emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } } }]
      })
    } else if (q.type === 'INPUT' && q.textAnswers) {
      const el = chartRefs['wordcloud_' + q.id]
      if (!el) return
      const freqMap = {}
      q.textAnswers.forEach(text => {
        const t = text.trim()
        if (t) freqMap[t] = (freqMap[t] || 0) + 1
      })
      const words = Object.entries(freqMap).map(([name, value]) => ({ name, value }))
      if (words.length === 0) return
      const chart = echarts.init(el)
      chart.setOption({
        tooltip: { show: true },
        series: [{
          type: 'wordCloud',
          shape: 'circle',
          sizeRange: [16, 60],
          rotationRange: [-45, 45],
          gridSize: 8,
          textStyle: { fontFamily: 'sans-serif', color: () => 'rgb(' + [Math.round(Math.random() * 160), Math.round(Math.random() * 160), Math.round(Math.random() * 160)].join(',') + ')' },
          data: words
        }]
      })
    }
  })
}

onMounted(async () => {
  try {
    const res = await getStatistics(surveyId)
    stats.value = res.data
    await nextTick()
    setTimeout(renderCharts, 100)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.ai-report {
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}
.ai-report :deep(h2), .ai-report :deep(h3), .ai-report :deep(h4) {
  color: #303133;
}
.ai-report :deep(li) {
  list-style: disc;
}
.ai-report :deep(strong) {
  color: #409eff;
}
</style>
