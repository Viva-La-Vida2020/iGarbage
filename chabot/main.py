from speech_2_text import speech_2_text
from text_2_speech import text_2_speech
from call_gpt_api import chat_with_gpt
import os


if __name__ == "__main__":
    # 1.Start audio recording
    question = speech_2_text()
    # 2. 根据question解析用户提问关键词，包括垃圾种类和城市

    # 3. 结合sql template从数据库中拿信息，作为prompt
    prompt = ''
    # 4. 将信息作为prompt传给gpt
    response = chat_with_gpt(prompt, question)
    # 5. 调用text2speech，播放回答
    print('Speech recognized: ', response)
    text_2_speech(response)


