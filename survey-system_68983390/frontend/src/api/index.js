import request from './request'

// Auth
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)

// User management (admin)
export const getUserList = (params) => request.get('/users', { params })
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/users/${id}`)
export const toggleUserStatus = (id) => request.put(`/users/${id}/toggle-status`)

// Survey management
export const createSurvey = (data) => request.post('/surveys', data)
export const updateSurvey = (data) => request.put('/surveys', data)
export const deleteSurvey = (id) => request.delete(`/surveys/${id}`)
export const publishSurvey = (id) => request.put(`/surveys/${id}/publish`)
export const endSurvey = (id) => request.put(`/surveys/${id}/end`)
export const getSurveyList = (params) => request.get('/surveys', { params })
export const getPublishedSurveys = (params) => request.get('/surveys/published', { params })
export const getSurveyDetail = (id) => request.get(`/surveys/${id}`)

// Question management
export const addQuestion = (data) => request.post('/questions', data)
export const updateQuestion = (data) => request.put('/questions', data)
export const deleteQuestion = (id) => request.delete(`/questions/${id}`)
export const reorderQuestions = (surveyId, ids) => request.put(`/questions/reorder/${surveyId}`, ids)

// Answer management
export const submitAnswer = (data) => request.post('/answers/submit', data)
export const getAnswerSheets = (surveyId, params) => request.get(`/answers/sheets/${surveyId}`, { params })
export const getAnswerDetail = (sheetId) => request.get(`/answers/detail/${sheetId}`)
export const deleteAnswerSheet = (id) => request.delete(`/answers/sheets/${id}`)
export const getStatistics = (surveyId) => request.get(`/answers/statistics/${surveyId}`)
export const getMyAnswers = (params) => request.get('/answers/my', { params })

// Export (returns blob)
export const exportExcel = (surveyId) => request.get(`/answers/export/${surveyId}`, { responseType: 'blob' })

// AI功能
export const aiGenerateSurvey = (description) => request.post('/ai/generate-survey', { description })
export const aiAnalyzeReport = (surveyId) => request.get(`/ai/analyze/${surveyId}`)
export const aiSummarizeText = (questionId) => request.get(`/ai/summarize/${questionId}`)
