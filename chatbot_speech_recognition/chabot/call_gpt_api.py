import openai


api_key = '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'
openai.api_key = api_key

def chat_with_gpt(prompt, question):
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "user", "content": prompt},
            {"role": "user", "content": question}
        ]
    )

    message = response['choices'][0]['message']['content']
    return message

# background = "塑料瓶在上海属于可回收垃圾的一种."
# question = "我在上海，矿泉水瓶是什么垃圾？"
#
# response = chat_with_gpt(background, question)
# print(response)
