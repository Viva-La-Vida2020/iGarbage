## TO RUN THIS PROJECT - USE IDEA



**1. Add opencv Java dll file**

![image-20231104103235652](D:\NUS\5002\rubbish-classification-master\img\image-20231104103235652.png)

find path:

```java
.\src\main\resources\opencv_java480.dll
```

find Java file:

```java
src/main/java/com/rubbishclassification/RubbishClassificationApplication.java
```

replace the **line 39** with your path:

```java
System.load(" ");
```



**2. Add jar**

![image-20231104103128558](D:\NUS\5002\rubbish-classification-master\img\image-20231104103128558.png)

find path:

```java
.\src\main\resources\opencv-480.jar
```

add the jar to **IDEA -> File -> Project Structure -> Project Settings -> Libraries**



**3. Modify the application properties**

![image-20231104103946954](D:\NUS\5002\rubbish-classification-master\img\image-20231104103946954.png)

find Java file:

```java
.\src\main\resources\application.properties
```

modify the database info with your MySQL username and password

modify the model path with your model path



**4. Create MySQL database**

run file in path:

```
.\rc\rc.sql
```



**4. Run file**

run Java file in IDEA:

```java
.\src\main\java\com\rubbishclassification\RubbishClassificationApplication.java
```
