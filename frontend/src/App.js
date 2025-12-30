import React, { useState, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Button } from 'react-bootstrap';
import HrDashboard from './pages/HrDashboard';
import CandidateDashboard from './pages/CandidateDashboard';

function App() {
  const [currentView, setCurrentView] = useState('home');

  const renderView = () => {
    switch(currentView) {
      case 'hr':
        return <HrDashboard onBack={() => setCurrentView('home')} />;
      case 'candidate':
        return <CandidateDashboard onBack={() => setCurrentView('home')} />;
      default:
        return (
          <Container className="d-flex justify-content-center align-items-center min-vh-100">
            <div className="text-center">
              <h1 className="mb-4">AI Interview Agent</h1>
              <div className="d-grid gap-3" style={{width: '300px'}}>
                <Button 
                  variant="primary" 
                  size="lg"
                  onClick={() => setCurrentView('hr')}
                >
                  HR Dashboard
                </Button>
                <Button 
                  variant="success" 
                  size="lg"
                  onClick={() => setCurrentView('candidate')}
                >
                  Candidate Dashboard
                </Button>
              </div>
            </div>
          </Container>
        );
    }
  };

  return <div className="App">{renderView()}</div>;
}

export default App;