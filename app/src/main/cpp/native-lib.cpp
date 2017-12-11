#include <jni.h>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <vector>

using namespace cv;
using namespace std;

static int maintain = 0;
static double pre_size = 0.0;

vector<Point> getRectPoint(vector<vector<Point>>);

extern "C"
JNIEXPORT void JNICALL
Java_com_dongyang_pjw_cardcam_CamPreview_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                          jlong matAddrInput, jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;


//    cvtColor(matInput, matResult, CV_RGBA2GRAY);
    Canny(matInput, matResult, 100, 200);
}

extern "C"
JNIEXPORT bool JNICALL
Java_com_dongyang_pjw_cardcam_CamPreview_FindEdgeAndDraw(JNIEnv *env, jobject instance,
                                                         jlong matAddrInput, jlong matAddrResult) {

    // TODO
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;
    Mat original;
    matInput.copyTo(original);

    matResult = matInput.clone();

    // gray image
    Mat imgray;
    cvtColor(matInput, imgray, CV_RGBA2GRAY);

    // blur
    Mat blur;
    GaussianBlur(imgray, blur, Size(3, 3), 0);

    // canny edge
    Mat edge;
    Canny(blur, edge, 75, 200);

    // contours
    vector<vector<Point> > contours;
    findContours(edge, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

    // sort
    sort(contours.begin(), contours.end(), [](const vector<Point> &c1, const vector<Point> &c2) {
        return contourArea(c1, false) > contourArea(c2, false);
    });

    vector<vector<Point> > approx;
    approx.resize(1);

    for (int i = 0; i < contours.size(); i++) {

        if (i >= 4) break;
//        __android_log_print(ANDROID_LOG_DEBUG, (char *) "contourarea :: ", (char *) "size %f ", contourArea(contours[i]));
        double peri = arcLength(Mat(contours[i]), true);
//        __android_log_print(ANDROID_LOG_DEBUG, (char *) "peri :: ", (char *) "peri %f ", peri);
        approxPolyDP(Mat(contours[i]), approx[0], 0.02 * peri, true);
//        __android_log_print(ANDROID_LOG_DEBUG, (char *) "approxarea :: ", (char *) "size %f ", contourArea(approx[0]));
//        for(int k=0; k < approx[0].size(); k++){
//            __android_log_print(ANDROID_LOG_DEBUG, (char *) "\t\tx : %s ", (char *)approx[0][k].x ,(char *) ",,,, y :  %s ", (char *)approx[0][k].y);
//        }

        if (approx[0].size() == 4) {
            double camsize = matInput.cols * matInput.rows;
            double approxsize = contourArea(approx[0]);
            double ratio = approxsize / camsize;


            __android_log_print(ANDROID_LOG_DEBUG, (char *) "camsize :: ", (char *) "size %f ",
                                camsize);
            __android_log_print(ANDROID_LOG_DEBUG, (char *) "contourarea :: ", (char *) "size %f ",
                                approxsize);
            __android_log_print(ANDROID_LOG_DEBUG, (char *) "ratio :: ", (char *) "size %.2f ",
                                ratio);

            double ratio_pre = (approxsize > pre_size) ? approxsize/pre_size : pre_size / approxsize;
            pre_size = approxsize;

            if (ratio > 0.1 && ratio_pre <1.03) {
                maintain++;
                __android_log_print(ANDROID_LOG_DEBUG, (char *) "maintain :: ",
                                    (char *) "size %d\n ", maintain);
                drawContours(matResult, approx, -1, Scalar(0, 255, 0), 2);
                break;
            } else {
                maintain = 0;
            }
        }
    }

    if (maintain > 10) {
        maintain = 0;

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "before getrect :: ",
                            (char *) "maintain= %d\n ", maintain);
        vector<Point> rect = getRectPoint(approx);

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "after getrect :: ",
                            (char *) "maintain= %d\n ", maintain);

//        for(int j=0; j< rect.size(); j++) {
//            Point temp = rect[j];
//            int xx = temp.x;
//            int yy = temp.y;
//            __android_log_print(ANDROID_LOG_DEBUG, (char *) "xxx :: ", (char *) "size %d ", xx);
//            __android_log_print(ANDROID_LOG_DEBUG, (char *) "yyy :: ", (char *) "size %d ", yy);
//        }




        double w1 = sqrt( pow(rect[2].x - rect[3].x, 2) + pow(rect[2].x - rect[3].x, 2) );
        double w2 = sqrt( pow(rect[1].x - rect[0].x, 2) + pow(rect[1].x - rect[0].x, 2) );
        double h1 = sqrt( pow(rect[1].y - rect[2].y, 2) + pow(rect[1].y - rect[2].y, 2) );
        double h2 = sqrt( pow(rect[0].y - rect[3].y, 2) + pow(rect[0].y - rect[4].y, 2) );

        double maxWidth = (w1 > w2) ? w1 : w2;
        double maxHeight = (h1 > h2) ? h1 : h2;

        Point2f dst[4], src[4];

        src[0] = Point2f(rect[0].x, rect[0].y);
        src[1] = Point2f(rect[1].x, rect[1].y);
        src[2] = Point2f(rect[2].x, rect[2].y);
        src[3] = Point2f(rect[3].x, rect[3].y);
        dst[0] = Point2f(0, 0);
        dst[1] = Point2f(maxWidth-1, 0);
        dst[2] = Point2f(maxWidth-1, maxHeight-1);
        dst[3] = Point2f(0, maxHeight-1);

//        dst[3] = Point2f(0, 0);
//        dst[0] = Point2f(maxWidth-1, 0);
//        dst[1] = Point2f(maxWidth-1, maxHeight-1);
//        dst[2] = Point2f(0, maxHeight-1);

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "before transmat :: ", (char *) "maintain= %d\n ", maintain);
        Mat transMat = getPerspectiveTransform(src, dst);

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "before warped :: ", (char *) "maintain= %d\n ", maintain);
        Mat warped;
        warpPerspective(original, warped, transMat, Size(maxWidth, maxHeight));

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "after warped :: ", (char *) "maintain= %d\n ", maintain);

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "warped :: ", (char *) "cols %d ", warped.cols);
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "warped :: ", (char *) "rows %d ", warped.rows);
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "warped :: ", (char *) "type %d ", warped.type());

        bool rot = true;
        if(rot){
            transpose(warped, warped);
            flip(warped, warped, 1);
        }
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "warped2 :: ", (char *) "cols %d ", warped.cols);
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "warped2 :: ", (char *) "rows %d ", warped.rows);
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "warped2 :: ", (char *) "type %d ", warped.type());

        matResult = warped.clone();
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "matResult :: ", (char *) "cols %d ", matResult.cols);
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "matResult :: ", (char *) "rows %d ", matResult.rows);
        __android_log_print(ANDROID_LOG_DEBUG, (char *) "matResult :: ", (char *) "type %d ", matResult.type());

        __android_log_print(ANDROID_LOG_DEBUG, (char *) "return true:: ", (char *) "maintain= %d\n ", maintain);
        return true;
    }
    __android_log_print(ANDROID_LOG_DEBUG, (char *) "return false :: ", (char *) "maintain= %d\n ", maintain);
    return false;
}

vector<Point> getRectPoint(vector<vector<Point>> approx){
    Point LT, RT, LB, RB;
    vector<Point> rect(4), pre(4);

//    rect.resize(4);

    pre = approx[0];

    int sumMax = pre[0].x + pre[0].y;
    int sumMin = pre[0].x + pre[0].y;
    int diffMax = pre[0].y - pre[0].x;
    int diffMin = pre[0].y - pre[0].x;;

    LT = pre[0];
    RT = pre[0];
    LB = pre[0];
    RB = pre[0];

    int calc = 0;


    for(int i=0; i<4; i++){
        calc = pre[i].x + pre[i].y;
        if (calc > sumMax){
            sumMax = calc;
            RB = pre[i];
        }
        else if (calc < sumMin){
            sumMin = calc;
            LT = pre[i];
        }

        calc = pre[i].y - pre[i].x;
        if (calc > diffMax){
            diffMax = calc;
            LB = pre[i];
        }
        else if (calc < diffMin){
            diffMin = calc;
            RT = pre[i];
        }
    }

    rect[0] = LT;
    rect[1] = RT;
    rect[2] = RB;
    rect[3] = LB;

    return rect;
}

//__android_log_print(ANDROID_LOG_DEBUG, (char *) "native-lib :: ", (char *) "face %d found ", faces.size());

//    for (int i=0; i<5; i++){
//        double peri = arcLength(Mat(contours[i]), true);
//        approxPolyDP(contours[i], approx[0], 0.02*peri, true);
//
//        if (approx[0].size() == 4){
//            break;
//        }
//    }

//    drawContours(edge, approx, -1, Scalar(0, 255, 0), 2);

//    // contours
//    vector<vector<Point> > contours;
//    vector<vector<Point> > contours2;
//    vector<Vec4i> hierarchy;
//
//    std::vector<Rect> faces;
//
//    findContours(edge, contours, hierarchy, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);
//    findContours(edge, contours2, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
//    cout << "\nContours found = " << contours.size() << "\n"; cout.flush();
//    cout << "\nContours found2 = " << contours2.size() << "\n"; cout.flush();
//    drawContours(matResult, contours, -1, (0, 255, 0), 1, 8, hierarchy);
//
//    Mat test;
//    drawContours(test, contours, -1, (0, 255, 0), 1, 8, hierarchy);
////    imshow("test", test);

//}

//extern "C"
//JNIEXPORT jlong JNICALL
//Java_com_dongyang_pjw_cardcam_CamPreview_loadCascade(JNIEnv *env, jclass type,
//                                                     jstring cascadeFileName_) {
//    const char *nativeFileNameString  = env->GetStringUTFChars(cascadeFileName_, 0);
//
//    // TODO
//
//    string baseDir("/storage/emulated/0/");
//    baseDir.append(nativeFileNameString);
//    const char *pathDir = baseDir.c_str();
//
//    jlong ret = 0;
//    ret = (jlong) new CascadeClassifier(pathDir);
//    if (((CascadeClassifier *) ret)->empty()) {
//        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
//                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
//    }
//    else
//        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
//                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);
//
//
//    env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);
//
//    return ret;
//}
//
//
//
//float resize(Mat img_src, Mat &img_resize, int resize_width){
//
//    float scale = resize_width / (float)img_src.cols ;
//    if (img_src.cols > resize_width) {
//        int new_height = cvRound(img_src.rows * scale);
//        resize(img_src, img_resize, Size(resize_width, new_height));
//    }
//    else {
//        img_resize = img_src;
//    }
//    return scale;
//}
//
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_dongyang_pjw_cardcam_CamPreview_detect(JNIEnv *env, jclass type,
//                                                jlong cascadeClassifier_face,
//                                                jlong cascadeClassifier_eye, jlong matAddrInput,
//                                                jlong matAddrResult) {
//
//    // TODO
//    Mat &img_input = *(Mat *) matAddrInput;
//    Mat &img_result = *(Mat *) matAddrResult;
//
//    img_result = img_input.clone();
//
//    std::vector<Rect> faces;
//    Mat img_gray;
//
//    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
//    equalizeHist(img_gray, img_gray);
//
//    Mat img_resize;
//    float resizeRatio = resize(img_gray, img_resize, 640);
//
//    //-- Detect faces
//    ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );
//
//
//    __android_log_print(ANDROID_LOG_DEBUG, (char *) "native-lib :: ",
//                        (char *) "face %d found ", faces.size());
//
//    for (int i = 0; i < faces.size(); i++) {
//        double real_facesize_x = faces[i].x / resizeRatio;
//        double real_facesize_y = faces[i].y / resizeRatio;
//        double real_facesize_width = faces[i].width / resizeRatio;
//        double real_facesize_height = faces[i].height / resizeRatio;
//
//        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);
//        ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
//                Scalar(255, 0, 255), 30, 8, 0);
//
//
//        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
//        Mat faceROI = img_gray( face_area );
//        std::vector<Rect> eyes;
//
//        //-- In each face, detect eyes
//        ((CascadeClassifier *) cascadeClassifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
//
//        for ( size_t j = 0; j < eyes.size(); j++ )
//        {
//            Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
//            int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
//            circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 30, 8, 0 );
//        }
//    }
//}