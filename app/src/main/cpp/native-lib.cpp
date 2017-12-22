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
JNIEXPORT bool JNICALL
Java_com_dongyang_pjw_cardcam_CamPreview_FindEdgeAndDraw(JNIEnv *env, jobject instance,
                                                         jlong matAddrInput, jlong matAddrResult, jboolean rot) {

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
    // Mat blur;
    // GaussianBlur(imgray, blur, Size(3, 3), 0);
    // imgray.release();

    // canny edge
    Mat edge;
    Canny(imgray, edge, 75, 200);
    // Canny(blur, edge, 75, 200);
    // blur.release();

    // contours
    vector<vector<Point>> contours;
    findContours(edge, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
    edge.release();

    // sort
    sort(contours.begin(), contours.end(), [](const vector<Point> &c1, const vector<Point> &c2) {
        return contourArea(c1, false) > contourArea(c2, false);
    });

    vector<vector<Point>> approx;
    approx.resize(1);

    for (int i = 0; i < contours.size(); i++) {

        if (i >= 4) break;

        double peri = arcLength(Mat(contours[i]), true);
        approxPolyDP(Mat(contours[i]), approx[0], 0.02 * peri, true);  /// 0.02 to 0.05

        if (approx[0].size() == 4) {
            double camsize = matInput.cols * matInput.rows;
            double approxsize = contourArea(approx[0]);
            double ratio = approxsize / camsize;


//            __android_log_print(ANDROID_LOG_DEBUG, (char *) "camsize :: ", (char *) "size %f ",
//                                camsize);
//            __android_log_print(ANDROID_LOG_DEBUG, (char *) "contourarea :: ", (char *) "size %f ",
//                                approxsize);
//            __android_log_print(ANDROID_LOG_DEBUG, (char *) "ratio :: ", (char *) "size %.2f ",
//                                ratio);

            double ratio_pre = (approxsize > pre_size) ? approxsize/pre_size : pre_size / approxsize;
            pre_size = approxsize;

            if (ratio > 0.1 && ratio_pre <1.03) {
                maintain++;
                __android_log_print(ANDROID_LOG_DEBUG, (char *) "maintain :: ", (char *) "num =  %d\n ", maintain);
                drawContours(matResult, approx, -1, Scalar(0, 255, 0), 2);
                break;
            } else {
                maintain = 0;
            }
        }
    }

    if (maintain > 10) {
        maintain = 0;

        vector<Point> rect = getRectPoint(approx);

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
        double h2 = sqrt( pow(rect[0].y - rect[3].y, 2) + pow(rect[0].y - rect[3].y, 2) );

        double maxWidth = (w1 > w2) ? w1 : w2;
        double maxHeight = (h1 > h2) ? h1 : h2;

        Point2f dst[4], src[4];

        src[0] = Point2f(rect[0].x, rect[0].y);
        src[1] = Point2f(rect[1].x, rect[1].y);
        src[2] = Point2f(rect[2].x, rect[2].y);
        src[3] = Point2f(rect[3].x, rect[3].y);
        dst[0] = Point2f(0, 0);
        dst[1] = Point2f((float) (maxWidth - 1), 0);
        dst[2] = Point2f((float) (maxWidth - 1), (float) (maxHeight - 1));
        dst[3] = Point2f(0, (float) (maxHeight - 1));

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


        if(maxHeight > maxWidth) {
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

extern "C"
JNIEXPORT bool JNICALL
Java_com_dongyang_pjw_cardcam_MainActivity_findCardFromImage(JNIEnv *env, jobject instance,
                                                              jlong matAddrInput, jlong matAddrOutput) {
    // TODO
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrOutput;
    Mat original;
    matInput.copyTo(original);

    matResult = matInput.clone();

    // gray image
    Mat imgray;
    cvtColor(matInput, imgray, CV_RGBA2GRAY);

    // blur
    // Mat blur;
    // GaussianBlur(imgray, blur, Size(3, 3), 0);
    // imgray.release();

    // canny edge
    Mat edge;
    Canny(imgray, edge, 75, 200);
    // Canny(blur, edge, 75, 200);
    // blur.release();

    // contours
    vector<vector<Point>> contours;
    findContours(edge, contours, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
    edge.release();

    // sort
    sort(contours.begin(), contours.end(), [](const vector<Point> &c1, const vector<Point> &c2) {
        return contourArea(c1, false) > contourArea(c2, false);
    });

    vector<vector<Point>> approx;
    approx.resize(1);


    for (int i = 0; i < contours.size(); i++) {

        if (i >= 4) {
            return false;
        }

        double peri = arcLength(Mat(contours[i]), true);
        approxPolyDP(Mat(contours[i]), approx[0], 0.02 * peri, true); //0.02 to 0.05

        if (approx[0].size() == 4) {
            double camsize = matInput.cols * matInput.rows;
            double approxsize = contourArea(approx[0]);
            double ratio = approxsize / camsize;

            if (ratio > 0.1 ) {
                break;
            }
        }
    }

    vector<Point> rect = getRectPoint(approx);


    double w1 = sqrt( pow(rect[2].x - rect[3].x, 2) + pow(rect[2].x - rect[3].x, 2) );
    double w2 = sqrt( pow(rect[1].x - rect[0].x, 2) + pow(rect[1].x - rect[0].x, 2) );
    double h1 = sqrt( pow(rect[1].y - rect[2].y, 2) + pow(rect[1].y - rect[2].y, 2) );
    double h2 = sqrt( pow(rect[0].y - rect[3].y, 2) + pow(rect[0].y - rect[3].y, 2) );

    double maxWidth = (w1 > w2) ? w1 : w2;
    double maxHeight = (h1 > h2) ? h1 : h2;

    Point2f dst[4], src[4];

    src[0] = Point2f(rect[0].x, rect[0].y);
    src[1] = Point2f(rect[1].x, rect[1].y);
    src[2] = Point2f(rect[2].x, rect[2].y);
    src[3] = Point2f(rect[3].x, rect[3].y);
    dst[0] = Point2f(0, 0);
    dst[1] = Point2f((float) (maxWidth - 1), 0);
    dst[2] = Point2f((float) (maxWidth - 1), (float) (maxHeight - 1));
    dst[3] = Point2f(0, (float) (maxHeight - 1));

    Mat transMat = getPerspectiveTransform(src, dst);
    Mat warped;
    warpPerspective(original, warped, transMat, Size(maxWidth, maxHeight));

    if(maxHeight > maxWidth) {
        transpose(warped, warped);
        flip(warped, warped, 1);
    }
    matResult = warped.clone();
    return true;
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

