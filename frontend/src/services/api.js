import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
});

export const hrService = {
  createRequirement: (requirement) => api.post('/hr/requirements', requirement),
  getRequirements: () => api.get('/hr/requirements'),
  getRequirement: (id) => api.get(`/hr/requirements/${id}`),
  getAllSessions: () => api.get('/interview/all'),
};

export const interviewService = {
  // Now using the Start Interview endpoint from Controller
  startInterview: (userId, name, email, resumeFile, requirementId) => {
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('name', name);
    formData.append('email', email);
    formData.append('resume', resumeFile);
    if (requirementId) formData.append('requirementId', requirementId);
    return api.post('/interview/start', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  getNextQuestion: (sessionId) => api.get(`/interview/${sessionId}/next-question`),

  getQuestionAudioUrl: (sessionId) => `${API_BASE_URL}/interview/${sessionId}/question-audio?t=${new Date().getTime()}`, // Prevent caching

  submitAnswer: (sessionId, audioBlob) => {
    const formData = new FormData();
    formData.append('audio', audioBlob, 'answer.wav');
    return api.post(`/interview/${sessionId}/answer`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  getResult: (sessionId) => api.get(`/interview/${sessionId}/result`),

  startTestInterview: (userId) => {
    return api.post('/interview/test-start', null, {
      params: { userId }
    });
  },

  endInterview: (sessionId) => api.post(`/interview/${sessionId}/end`),
};

export default api;