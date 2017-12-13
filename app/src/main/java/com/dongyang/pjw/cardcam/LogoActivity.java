package com.dongyang.pjw.cardcam;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class LogoActivity extends AppCompatActivity {
    private static final String TAG = "LOGO";
    Handler handler;

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    // afetr loading library , some tasks here


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                if (!OpenCVLoader.initDebug()) {
                    Log.d(TAG, "Internal OpenCV library not found.");
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getApplicationContext(), mLoaderCallback);
                } else {
                    Log.d(TAG, "OpenCV library found inside package. Using it!");
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }

                Intent it = new Intent(LogoActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        }, 2000);   // 2500 = 2.5초 후 메인 액티비티 도출

    }
}
