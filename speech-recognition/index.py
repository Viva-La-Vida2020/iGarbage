import pyaudio  # 录音程序
import wave
import openai
import os
from model.predict import predict

# 一次读取数据流的数据量，避免一次性的数据量大大
CHUNK = 1024
# 采样精度
FORMAT = pyaudio.paInt16
# 声道数
CHANNELS = 1
# 采样频率
RATE = 11025
# 录音时长，单位秒
RECORD_SECONDS = 3

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_FILENAME = f'{CURRENT_DIR}/output.wav'

openai.api_key = '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'


def transcribe(file: str):
    audio_file = open(file, "rb")
    transcript = openai.Audio.transcribe("whisper-1", audio_file)
    return transcript['text']


def recorder(output_file: str, time=3):
    # 初始化PyAudio
    audio = pyaudio.PyAudio()

    # 开始录制
    print("Recording...")
    stream = audio.open(format=FORMAT,
                        channels=CHANNELS,
                        rate=RATE,
                        input=True,
                        frames_per_buffer=CHUNK)

    frames = []

    for _ in range(0, int(RATE / CHUNK * time)):
        data = stream.read(CHUNK)
        frames.append(data)

    print("Finished recording")
    # 停止录制
    stream.stop_stream()
    stream.close()
    audio.terminate()

    # 保存为WAV文件
    with wave.open(output_file, 'wb') as wf:
        wf.setnchannels(CHANNELS)
        wf.setsampwidth(audio.get_sample_size(FORMAT))
        wf.setframerate(RATE)
        wf.writeframes(b''.join(frames))

    print(f"Saved as {output_file}")


def recognition(file):
    try:
        print('call model')
        result = predict(file)
    except any:
        print('call whisper')
        result = transcribe(file)
    return result


if __name__ == "__main__":
    recorder(OUTPUT_FILENAME, RECORD_SECONDS)
    result = transcribe(OUTPUT_FILENAME)
    print(result)
