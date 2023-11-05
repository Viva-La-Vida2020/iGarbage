from speech_2_text import speech_2_text
from text_2_speech import text_2_speech
from call_gpt_api import chat_with_gpt
import os
import pandas as pd
from conn_rubbish_db import display_rubbish_info
from text_2_speech import text_2_speech


def get_city_list():
    city_df = pd.read_csv('city.csv', header=None)
    city_df.columns = ['city_full_name', 'city_short_name']
    city_df['city_short_name'] = city_df['city_full_name'].str.slice(1, -2)
    city_df['city_full_name'] = city_df['city_full_name'].str.slice(1, -1)
    cities_list = list(city_df['city_full_name']) + list(city_df['city_short_name'])
    return(cities_list)

def get_rubbish_list():
    rubbish_df = pd.read_csv('rubbish.csv', header=None)
    rubbish_df.columns = ['rubbish_name', 'rubbish_short_name']
    rubbish_df['rubbish_name'] = rubbish_df['rubbish_name'].str.slice(1, -1)
    rubbish_lists = list(rubbish_df['rubbish_name'])
    return(rubbish_lists)

def mapping_city_and_rubbish(question,cities_lists,rubbish_lists):
    city_dic = {}
    # 城市找最短的，垃圾找最长的
    for city in cities_lists:
        if city in question:
            city_dic[city] = len(city)

    rubbish_dic = {}
    for rubbish in rubbish_lists:
        if rubbish in question:
            rubbish_dic[rubbish] = len(rubbish)
    city = sorted(city_dic.items(), key=lambda x: x[1])[0][0]
    rubbish = sorted(rubbish_dic.items(), key=lambda x: x[1], reverse=True)[0][0]
    return(city,rubbish)

if __name__ == "__main__":
    # 1.Start audio recording
    question = speech_2_text()
    # 2. 根据question解析用户提问关键词，包括垃圾种类和城市
    print(type(question))
    cities_list = get_city_list()
    rubbish_list = get_rubbish_list()
    city, rubbish = mapping_city_and_rubbish(question, cities_list, rubbish_list)
    # 3. 结合sql template从数据库中拿信息，作为prompt
    rubbish_name, city_name, rubbish_type, process_method, type_desc = display_rubbish_info(rubbish, city)
    prompt = rubbish_name+city_name+rubbish_type
    # 4. 将信息作为prompt传给gpt
    response = chat_with_gpt(prompt, question)
    # 5. 调用text2speech，播放回答
    print('Speech recognized: ', response)
    text_2_speech(response)


