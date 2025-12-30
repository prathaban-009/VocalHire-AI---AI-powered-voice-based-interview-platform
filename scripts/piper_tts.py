import sys
import os
import subprocess

# Paths based on your structure
# BASE_DIR is the root "ai-interview-agent - Copy"
# This script is in "scripts/" so we go up one level
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PIPER_DIR = os.path.join(BASE_DIR, "piper")
PIPER_EXE = os.path.join(PIPER_DIR, "piper.exe")
MODEL_PATH = os.path.join(PIPER_DIR, "models", "en_US-joe-medium.onnx")

def text_to_speech(text, output_path):
    print(f"DEBUG: Starting Piper TTS script...")
    print(f"DEBUG: Piper Exe: {PIPER_EXE}")
    print(f"DEBUG: Model: {MODEL_PATH}")
    print(f"DEBUG: Output: {output_path}")

    if not os.path.exists(PIPER_EXE):
        print(f"Error: Piper executable not found at {PIPER_EXE}")
        sys.exit(1)
    
    if not os.path.exists(MODEL_PATH):
        print(f"Error: Piper model not found at {MODEL_PATH}")
        sys.exit(1)

    print(f"Generating audio for: '{text}'")
    
    # Piper Command: echo "text" | piper.exe --model model.onnx --output_file output.wav
    command = [
        PIPER_EXE,
        "--model", MODEL_PATH,
        "--output_file", output_path
    ]

    try:
        # Pass text via stdin
        process = subprocess.Popen(
            command, 
            stdin=subprocess.PIPE, 
            stdout=subprocess.PIPE, 
            stderr=subprocess.PIPE,
            text=True
        )
        
        stdout, stderr = process.communicate(input=text)
        
        if process.returncode != 0:
            print(f"Piper failed: {stderr}")
            sys.exit(process.returncode)
            
        print(f"Audio saved to {output_path}")

    except Exception as e:
        print(f"Error executing Piper: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python piper_tts.py <text> <output_file_path>")
        sys.exit(1)
    
    text_input = sys.argv[1]
    output_file = sys.argv[2]
    
    text_to_speech(text_input, output_file)
