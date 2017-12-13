# CamSaver



## 1. 환경

- 안드로이드 스튜디오 3.0.1

- OpenCV 3.3.1 [download](https://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.3.1/opencv-3.3.1-android-sdk.zip/download)

- targetSdkVersion : 26

- minSdkVersion : 21

  ​

## 2. 설정 및 설치

1. `TOOLS > ANDROID > SDK MANAGER > SDK TOOLS` 에서 LLDB, CMAKE, NDK 를 설치

2. opencv 라이브러리를 다운받아 적당한 곳에 푼다 (예 : C:\SDK\OpenCV-android-sdk )

3. git 으로부터 프로젝트를 clone 하거나 zip으로 다운받아 압축을 푼다(branch = master)

4. `CMakeLists.txt` 를 수정한다 (보이지 않는다면 네비게이션창을 Project로 바꾸어 본다)

   ```c
   # Sets the minimum version of CMake required to build the native library.

   cmake_minimum_required(VERSION 3.4.1)




   set(pathOPENCV C:/SDK/OpenCV-android-sdk) //이 부분을 설치한 openCV 위치로
   set(pathPROJECT C:/Users/borna/AndroidStudioProjects/CardCam) //이 부분을 프로젝트 위치로
   set(pathLIBOPENCV_JAVA ${pathPROJECT}/app/src/main/JniLibs/${ANDROID_ABI}/libopencv_java3.so)

   ```

   ​	

5. Gradle Sync

6. 혹시 sync error가 뜬다면 `gradle(openCVLibrary331)`을 수정하여 sdk 버전을 수정한다

   ```c
   android {
       compileSdkVersion 26
       buildToolsVersion "26.0.2"

       defaultConfig {
           minSdkVersion 21
           targetSdkVersion 26
       }

   ```





