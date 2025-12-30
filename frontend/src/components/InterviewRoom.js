import React, { useState, useEffect, useRef } from 'react';
import { Card, Button, Spinner, Badge } from 'react-bootstrap';
import { interviewService } from '../services/api';

const InterviewRoom = ({ sessionId, onComplete }) => {
    const [phase, setPhase] = useState('INIT'); // INIT, FETCHING, PLAYING, RECORDING, PROCESSING, REPEAT_PLAYING
    const [currentQuestion, setCurrentQuestion] = useState(null);
    const [statusMessage, setStatusMessage] = useState('Preparing interview...');
    const [error, setError] = useState('');

    const mediaRecorderRef = useRef(null);
    const chunksRef = useRef([]);

    useEffect(() => {
        fetchNextQuestion();
    }, []);

    const fetchNextQuestion = async () => {
        setPhase('FETCHING');
        setStatusMessage('Fetching next question...');
        try {
            const response = await interviewService.getNextQuestion(sessionId);
            if (response.data.status === 'COMPLETED') {
                // Call End Interview Endpoint to trigger background processing
                await interviewService.endInterview(sessionId);
                onComplete();
                return;
            }
            setCurrentQuestion(response.data);
            playQuestionAudio(response.data.id);
        } catch (err) {
            setError('Failed to load question. Please try again.');
        }
    };

    const playQuestionAudio = (questionId) => {
        setPhase('PLAYING');
        setStatusMessage('Interviewer is speaking...');

        // Use Backend Audio
        const audioUrl = `http://localhost:8080/api/interview/${sessionId}/question-audio?nocache=${Date.now()}`;
        const audio = new Audio(audioUrl);
        audio.onended = () => {
            handleAudioEnded();
        };
        audio.onerror = () => {
            console.error("Failed to play audio from backend");
            // Fallback or error state
            setError('Failed to play audio question.');
        };
        audio.play().catch(e => {
            console.error("Audio play error", e);
            setError('Click to play audio');
        });
    };

    const handleAudioEnded = () => {
        setPhase('RECORDING');
        setStatusMessage('Listening... (Click "Done" when finished)');
        startRecording();
    };

    const startRecording = async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaRecorderRef.current = new MediaRecorder(stream);
            chunksRef.current = [];

            mediaRecorderRef.current.ondataavailable = (e) => {
                if (e.data.size > 0) chunksRef.current.push(e.data);
            };

            mediaRecorderRef.current.start();
        } catch (err) {
            setError('Microphone access denied. Please enable microphone.');
        }
    };

    const stopRecordingAndSubmit = () => {
        if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
            mediaRecorderRef.current.stop();
            mediaRecorderRef.current.onstop = async () => {
                const blob = new Blob(chunksRef.current, { type: 'audio/wav' });
                submitAnswer(blob);
            };
        }
    };

    const submitAnswer = async (audioBlob) => {
        setPhase('PROCESSING');
        setStatusMessage('Processing your answer...');

        try {
            const response = await interviewService.submitAnswer(sessionId, audioBlob);
            const result = response.data;

            if (result.status === 'NEXT') {
                if (result.feedbackAudioUrl) {
                    // Play feedback audio
                    setStatusMessage(result.feedbackText || "Interviewer is replying...");
                    setPhase('PLAYING'); // Re-use playing phase to show spinner/status

                    // Ensure absolute URL if backend returns relative
                    let audioSource = result.feedbackAudioUrl;
                    if (!audioSource.startsWith('http')) {
                        audioSource = `http://localhost:8080${audioSource}`;
                    }

                    const audio = new Audio(audioSource);
                    audio.onended = () => {
                        fetchNextQuestion();
                    };
                    audio.onerror = () => {
                        console.error("Failed to play feedback audio. Moving to next.");
                        fetchNextQuestion();
                    };
                    audio.play().catch(e => {
                        console.error("Feedback audio play error", e);
                        fetchNextQuestion();
                    });
                } else {
                    fetchNextQuestion();
                }
            } else if (result.status === 'REPEAT') {
                // ... same logic for repeat
                if (result.audioUrl) {
                    // Play rephrase audio
                    let audioSource = result.audioUrl;
                    if (!audioSource.startsWith('http')) {
                        audioSource = `http://localhost:8080${audioSource}`;
                    }
                    const audio = new Audio(audioSource);
                    audio.onended = () => handleAudioEnded();
                    audio.play();
                } else {
                    // Fallback TTS
                    setStatusMessage(result.text || "Could you please clarify?");
                    setTimeout(handleAudioEnded, 2000);
                }
            } else if (result.status === 'SKIPPED') {
                setStatusMessage('Question skipped. Moving next...');
                setTimeout(fetchNextQuestion, 2000);
            } else {
                // Fallback
                fetchNextQuestion();
            }
        } catch (err) {
            setError('Failed to submit answer.');
        }
    };

    const handleEndInterview = async () => {
        if (window.confirm("Are you sure you want to end the interview now?")) {
            setPhase('PROCESSING');
            setStatusMessage('Ending interview and processing results...');
            try {
                // Terminate recording if active
                if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
                    mediaRecorderRef.current.stop();
                }

                await interviewService.endInterview(sessionId);
                onComplete();
            } catch (err) {
                setError('Failed to end interview.');
            }
        }
    };

    return (
        <div className="d-flex flex-column align-items-center justify-content-center h-100 p-4" style={{ minHeight: '100vh', background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)' }}>
            <Card className="glass-panel text-center shadow-lg border-0" style={{ width: '100%', maxWidth: '800px', borderRadius: '20px', overflow: 'hidden' }}>
                <div className="card-header bg-transparent border-0 pt-4">
                    <h3 className="fw-bold text-dark">AI Interview Session</h3>
                    <Badge bg="secondary" className="mt-2">Session ID: {sessionId}</Badge>
                </div>

                <div className="card-body p-5">
                    {error && <div className="alert alert-danger fade-in">{error}</div>}

                    {/* Dynamic Status / Visualizer */}
                    <div className="mb-5 d-flex justify-content-center align-items-center" style={{ minHeight: '150px' }}>
                        {phase === 'PLAYING' || phase === 'REPEAT_PLAYING' ? (
                            <div className="text-center fade-in">
                                <div className="spinner-grow text-primary" style={{ width: '3rem', height: '3rem' }} role="status"></div>
                                <div className="mt-3 text-primary fw-bold text-uppercase">{statusMessage}</div>
                            </div>
                        ) : phase === 'RECORDING' ? (
                            <div className="text-center fade-in">
                                <div className="recording-pulse mb-3" style={{ fontSize: '3rem' }}>🎙️</div>
                                <div className="text-danger fw-bold text-uppercase tracking-wider">{statusMessage}</div>
                            </div>
                        ) : phase === 'PROCESSING' || phase === 'FETCHING' ? (
                            <div className="text-secondary fade-in">
                                <Spinner animation="border" variant="primary" />
                                <div className="mt-3 text-muted">{statusMessage}</div>
                            </div>
                        ) : (
                            <div className="text-muted">{statusMessage}</div>
                        )}
                    </div>

                    {/* LIVE CAPTION AREA */}
                    {currentQuestion && (
                        <div className="mb-5 fade-in">
                            <h5 className="text-muted mb-3 text-uppercase small ls-1">Current Question</h5>
                            <div className="p-4 bg-light rounded-3 shadow-sm border-start border-4 border-primary text-start">
                                <p className="mb-0 fs-5 fw-medium text-dark" style={{ lineHeight: '1.6' }}>
                                    {currentQuestion.text || currentQuestion.question || "..."}
                                </p>
                            </div>
                            <div className="mt-2 text-end">
                                <Badge bg={currentQuestion.level === 'Hard' ? 'danger' : currentQuestion.level === 'Medium' ? 'warning' : 'success'}>
                                    {currentQuestion.level} Difficulty
                                </Badge>
                                {currentQuestion.category && <Badge bg="dark" className="ms-2">{currentQuestion.category}</Badge>}
                            </div>
                        </div>
                    )}

                    {/* ACTION TEXT */}


                    <div className="mt-5 d-flex gap-3 justify-content-center">
                        {phase === 'RECORDING' && (
                            <Button variant="danger" size="lg" className="px-5 py-3 rounded-pill shadow btn-animate" onClick={stopRecordingAndSubmit}>
                                Stop & Submit Answer
                            </Button>
                        )}

                        <Button variant="outline-danger" size="lg" className="px-4 py-3 rounded-pill shadow-sm" onClick={handleEndInterview}>
                            End Interview
                        </Button>
                    </div>
                </div>
            </Card>
        </div>
    );
};

export default InterviewRoom;
