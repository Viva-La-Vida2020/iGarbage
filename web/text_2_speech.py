from gtts import gTTS
from playsound import playsound
import os

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))


def text_2_speech(text):
    tts = gTTS(text=text, lang='zh-cn')
    audio_file = f"{CURRENT_DIR}/speech.mp3"

    tts.save(audio_file)
    playsound(audio_file)

    # 将其删除
    os.remove(audio_file)


if __name__ == "__main__":
    text = "你好，这是xx垃圾"
    text_2_speech(text)
