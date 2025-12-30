import React, { useState } from 'react';
import { Container, Form, Button, Alert, Row, Col } from 'react-bootstrap';
import { interviewService, hrService } from '../services/api';
import InterviewRoom from '../components/InterviewRoom';

const CandidateDashboard = ({ onBack }) => {
  const [view, setView] = useState('SETUP'); // SETUP, INTERVIEW, RESULT
  const [sessionId, setSessionId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [score, setScore] = useState(null);

  // Registration Fields
  const [candidateName, setCandidateName] = useState('');
  const [candidateEmail, setCandidateEmail] = useState('');
  const [resume, setResume] = useState(null);

  const [requirements, setRequirements] = useState([]);
  const [selectedReqId, setSelectedReqId] = useState('');

  const userId = 1; // Default for now, ideally managed by auth

  React.useEffect(() => {
    // Fetch requirements on load
    hrService.getRequirements()
      .then(res => setRequirements(res.data))
      .catch(err => console.error("Failed to fetch requirements", err));
  }, []);

  const startInterview = async () => {
    if (!candidateName || !candidateEmail || !resume) {
      setError('Please provide your name, email, and resume to proceed.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const response = await interviewService.startInterview(userId, candidateName, candidateEmail, resume, selectedReqId);
      setSessionId(response.data.id);
      setView('INTERVIEW');
    } catch (err) {
      setError('Failed to start interview session. Ensure backend is running.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async () => {
    setView('RESULT');
    try {
      const response = await interviewService.getResult(sessionId);
      setScore(response.data); // InterviewSession object
    } catch (err) {
      setError('Failed to fetch results.');
    }
  };

  return (
    <Container className="py-5" style={{ maxWidth: '1000px' }}>
      <div className="d-flex justify-content-between align-items-center mb-5">
        <h2 className="fw-bold" style={{ color: '#2d3748', letterSpacing: '-1px' }}>Candidate Portal</h2>
        {view === 'SETUP' && (
          <Button variant="link" onClick={onBack} style={{ color: '#4a5568', textDecoration: 'none', fontWeight: 600 }}>&larr; Back to Home</Button>
        )}
      </div>

      {view === 'SETUP' && (
        <div className="row justify-content-center">
          <div className="col-lg-7">
            <div className="glass-panel text-center p-5">
              <h3 className="mb-2 fw-bold text-dark">Welcome, Candidate!</h3>
              <p className="text-secondary mb-4">
                Please register to begin your AI-powered technical interview.
              </p>

              {error && <Alert variant="danger" className="mb-4 text-start">{error}</Alert>}

              <div className="text-start d-flex flex-column gap-3">
                <Form.Group>
                  <Form.Label className="small text-uppercase fw-bold text-muted">Full Name</Form.Label>
                  <Form.Control
                    type="text"
                    value={candidateName}
                    onChange={(e) => setCandidateName(e.target.value)}
                    placeholder="John Doe"
                    className="bg-light border-0 shadow-sm p-3 rounded-3"
                  />
                </Form.Group>

                <Form.Group>
                  <Form.Label className="small text-uppercase fw-bold text-muted">Email Address</Form.Label>
                  <Form.Control
                    type="email"
                    value={candidateEmail}
                    onChange={(e) => setCandidateEmail(e.target.value)}
                    placeholder="john@example.com"
                    className="bg-light border-0 shadow-sm p-3 rounded-3"
                  />
                </Form.Group>

                <Row>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label className="small text-uppercase fw-bold text-muted">Interview Role</Form.Label>
                      <Form.Select
                        value={selectedReqId}
                        onChange={(e) => setSelectedReqId(e.target.value)}
                        className="bg-light border-0 shadow-sm p-3 rounded-3"
                      >
                        <option value="">General Engineer</option>
                        {requirements.map(req => (
                          <option key={req.id} value={req.id}>
                            {req.role} ({req.difficultyPolicy || 'Standard'})
                          </option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label className="small text-uppercase fw-bold text-muted">Resume (PDF/DOC)</Form.Label>
                      <Form.Control
                        type="file"
                        accept=".pdf,.doc,.docx,.txt"
                        onChange={(e) => setResume(e.target.files[0])}
                        className="bg-light border-0 shadow-sm p-3 rounded-3"
                      />
                    </Form.Group>
                  </Col>
                </Row>
              </div>

              <div className="mt-5">
                <Button
                  className="btn-modern w-100 py-3 fs-5 shadow-lg"
                  onClick={startInterview}
                  disabled={loading}
                >
                  {loading ? 'Initializing Interface...' : 'Start Interview Now'}
                </Button>
              </div>

              <p className="mt-3 text-muted small">
                Ensure your microphone is enabled. The session usually lasts 15-30 minutes.
              </p>
            </div>
          </div>
        </div>
      )}

      {view === 'INTERVIEW' && (
        <div style={{ minHeight: '600px' }}>
          <InterviewRoom sessionId={sessionId} onComplete={handleComplete} />
        </div>
      )}

      {view === 'RESULT' && score && (
        <div className="row justify-content-center">
          <div className="col-md-6">
            <div className="glass-panel text-center p-5">
              <div className="mb-4">
                <span className="display-1 fw-bold text-primary">{score.totalScore ? score.totalScore.toFixed(0) : 0}</span>
                <span className="text-muted fs-4">/100</span>
              </div>

              <h4 className="mb-3 fw-bold">Interview Completed</h4>
              <p className="text-secondary mb-5">
                Thank you, <strong>{score.candidateName || candidateName}</strong>! Your responses have been recorded and sent to our HR team for review. You will receive an email shortly directly to <strong>{score.candidateEmail || candidateEmail}</strong>.
              </p>

              <Button variant="outline-primary" size="lg" className="px-5 rounded-pill" onClick={() => setView('SETUP')}>
                Back to Home
              </Button>
            </div>
          </div>
        </div>
      )}
    </Container>
  );
};

export default CandidateDashboard;