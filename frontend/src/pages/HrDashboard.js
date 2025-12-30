import React, { useState, useEffect } from 'react';
import { Container, Form, Button, Alert, Badge, Row, Col } from 'react-bootstrap';
import { hrService } from '../services/api';

const HrDashboard = ({ onBack }) => {
  const [requirement, setRequirement] = useState({
    role: '',
    requiredSkills: [],
    difficultyPolicy: 'EASY_MEDIUM_HARD'
  });
  const [skillInput, setSkillInput] = useState('');
  const [requirements, setRequirements] = useState([]);
  const [message, setMessage] = useState('');
  const [sessions, setSessions] = useState([]);

  useEffect(() => {
    loadRequirements();
    loadSessions();
  }, []);

  const loadRequirements = async () => {
    try {
      const response = await hrService.getRequirements();
      setRequirements(response.data);
    } catch (error) {
      console.error('Error loading requirements:', error);
    }
  };

  const handleAddSkill = () => {
    if (skillInput.trim() && !requirement.requiredSkills.includes(skillInput.trim())) {
      setRequirement({
        ...requirement,
        requiredSkills: [...requirement.requiredSkills, skillInput.trim()]
      });
      setSkillInput('');
    }
  };

  const handleRemoveSkill = (skill) => {
    setRequirement({
      ...requirement,
      requiredSkills: requirement.requiredSkills.filter(s => s !== skill)
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await hrService.createRequirement(requirement);
      setMessage('Requirement created successfully!');
      setRequirement({ role: '', requiredSkills: [], difficultyPolicy: 'EASY_MEDIUM_HARD' });
      loadRequirements();
    } catch (error) {
      setMessage('Error creating requirement');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const loadSessions = async () => {
    try {
      const res = await hrService.getAllSessions();
      const sorted = res.data.sort((a, b) => b.id - a.id);
      setSessions(sorted);
    } catch (e) {
      console.error("Failed to load sessions", e);
    }
  }

  return (
    <Container className="py-5" style={{ maxWidth: '1200px' }}>
      <div className="d-flex justify-content-between align-items-center mb-5">
        <h2 className="fw-bold" style={{ color: '#2d3748', letterSpacing: '-1px' }}>HR Dashboard</h2>
        <Button variant="link" onClick={onBack} style={{ color: '#4a5568', textDecoration: 'none', fontWeight: 600 }}>&larr; Back to Home</Button>
      </div>

      <Row className="gy-4">
        {/* CREATE REQUIREMENT COL */}
        <Col lg={4}>
          <div className="glass-panel h-100">
            <h4 className="mb-4 text-primary fw-bold">Create Job Profile</h4>
            {message && <Alert variant="success" className="shadow-sm border-0">{message}</Alert>}

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label className="fw-bold small text-uppercase text-secondary">Role Title</Form.Label>
                <Form.Control
                  type="text"
                  value={requirement.role}
                  onChange={(e) => setRequirement({ ...requirement, role: e.target.value })}
                  placeholder="e.g., Senior Java Developer"
                  required
                  className="border-0 shadow-sm bg-light"
                  style={{ borderRadius: '10px', padding: '12px' }}
                />
              </Form.Group>

              <Form.Group className="mb-4">
                <Form.Label className="fw-bold small text-uppercase text-secondary">Required Skills</Form.Label>
                <div className="d-flex mb-2">
                  <Form.Control
                    type="text"
                    value={skillInput}
                    onChange={(e) => setSkillInput(e.target.value)}
                    placeholder="Add a skill..."
                    onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddSkill())}
                    className="border-0 shadow-sm bg-light"
                    style={{ borderRadius: '10px 0 0 10px', padding: '12px' }}
                  />
                  <Button variant="primary" onClick={handleAddSkill} style={{ borderRadius: '0 10px 10px 0', padding: '0 20px' }} className="fw-bold">
                    +
                  </Button>
                </div>
                <div className="d-flex flex-wrap gap-2">
                  {requirement.requiredSkills.map((skill, index) => (
                    <Badge key={index} bg="white" text="primary" className="p-2 d-flex align-items-center shadow-sm border" style={{ borderRadius: '8px' }}>
                      {skill}
                      <span className="ms-2 text-danger" style={{ cursor: 'pointer' }} onClick={() => handleRemoveSkill(skill)}>&times;</span>
                    </Badge>
                  ))}
                </div>
              </Form.Group>

              <Button className="btn-modern w-100 mt-2" type="submit">
                Create Profile
              </Button>
            </Form>
          </div>
        </Col>

        {/* LISTINGS COL */}
        <Col lg={8}>
          <div className="d-flex flex-column gap-4 h-100">
            {/* STATUS CARD */}
            <div className="glass-panel">
              <h4 className="mb-4 text-primary fw-bold">Recent Interview Results</h4>
              <div className="table-responsive" style={{ maxHeight: '400px', overflowY: 'auto' }}>
                <table className="table table-borderless align-middle">
                  <thead className="text-secondary small text-uppercase sticky-top bg-white">
                    <tr>
                      <th>Candidate</th>
                      <th>Email</th>
                      <th>Date</th>
                      <th>Score</th>
                      <th>Stats</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sessions.length === 0 ? (
                      <tr><td colSpan="6" className="text-center text-muted py-4">No interviews yet.</td></tr>
                    ) : (
                      sessions.map(session => (
                        <tr key={session.id} className="bg-white rounded-3 shadow-sm mb-2" style={{ borderBottom: '10px solid transparent' }}>
                          <td className="p-3 fw-bold">{session.candidateName || `User ${session.userId}`}</td>
                          <td className="text-muted small">{session.candidateEmail || '-'}</td>
                          <td className="text-muted small">{formatDate(session.startTime)}</td>
                          <td>
                            {session.totalScore ? (
                              <Badge bg={session.totalScore >= 35 ? "success" : session.totalScore >= 20 ? "warning" : "danger"} className="px-3 py-2 rounded-pill">
                                {session.totalScore.toFixed(0)}/50
                              </Badge>
                            ) : (
                              <Badge bg="secondary" className="px-3 py-2 rounded-pill">-</Badge>
                            )}
                          </td>
                          <td>
                            <small className="text-muted">
                              {session.askedQuestionIds ? session.askedQuestionIds.length : 0} Qs
                            </small>
                          </td>
                          <td>
                            <span className={`fw-bold small ${session.status === 'COMPLETED' ? 'text-success' : 'text-warning'}`}>
                              {session.status}
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="glass-panel flex-grow-1">
              <h4 className="mb-4 text-primary fw-bold">Active Job Profiles</h4>
              <div className="d-flex  gap-3 overflow-auto pb-2" style={{ scrollbarWidth: 'thin' }}>
                {requirements.map((req) => (
                  <div key={req.id} className="glass-card p-4" style={{ minWidth: '250px' }}>
                    <h5 className="fw-bold text-dark">{req.role}</h5>
                    <div className="d-flex flex-wrap gap-1 mb-3 mt-2">
                      {req.requiredSkills.slice(0, 3).map(skill => (
                        <Badge bg="light" text="dark" className="border" key={skill}>{skill}</Badge>
                      ))}
                      {req.requiredSkills.length > 3 && <Badge bg="light" text="dark" className="border">+{req.requiredSkills.length - 3}</Badge>}
                    </div>
                    <small className="text-muted d-block mt-3">Policy: {req.difficultyPolicy}</small>
                  </div>
                ))}
                {requirements.length === 0 && <div className="text-muted w-100 text-center py-4">No active profiles</div>}
              </div>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default HrDashboard;