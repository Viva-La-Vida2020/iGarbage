import mysql.connector
def display_rubbish_info(rubbish_name,city):
    #print(university,program)
    db_connection = mysql.connector.connect(
        host="localhost",  # MySQL服务器地址
        user="root",   # MySQL用户名
        password="root",  # MySQL密码
        database="mysql"  # 数据库名称
    )
    cursor = db_connection.cursor()
    #template1 = ['Australian Catholic University','Master of Public Health']
    template1 = [rubbish_name,'%'+city+'%']

    # 执行SQL查询
    query = "select r.name,c.name,t.type,t.des,t.intro from rubbish r inner join type_rubbishes tr on r.id = tr.rubbishes_id inner join type t on tr.types_id = t.id inner join city c on t.city_id = c.id where r.name = %s and c.name like %s "
    cursor.execute(query,template1)

    # print('项目简介................')
    # 获取查询结果
    results = cursor.fetchall()
    info = list(results[0])
    rubbish_name = info[0]
    city_name = info[1]
    rubbish_type = info[2]
    process_method = info[3]
    type_desc = rubbish_type+info[4]
    # for row in results:
    #     print(row)
    return rubbish_name,city_name,rubbish_type,process_method,type_desc
