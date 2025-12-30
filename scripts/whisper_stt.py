import sys
import os
import static_ffmpeg
import whisper

# Ensure ffmpeg is in the path
static_ffmpeg.add_paths()

def transcribe_audio(file_path):
    if not os.path.exists(file_path):
        print("Error: File not found")
        return

    try:
        # Initialize model
        # Note: openai-whisper doesn't use compute_type="int8" arg in load_model
        model = whisper.load_model("tiny.en", device="cpu")

        # Transcribe
        result = model.transcribe(
            file_path,
            language="en",
            beam_size=1
        )

        print(result["text"].strip())
    
    except Exception as e:
        # Print error to stderr so Java can capture it in logs
        sys.stderr.write(f"Error during transcription: {e}\n")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python whisper_stt.py <audio_file_path>")
        sys.exit(1)
    
    audio_file = sys.argv[1]
    transcribe_audio(audio_file)
