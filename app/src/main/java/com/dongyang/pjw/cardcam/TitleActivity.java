package com.dongyang.pjw.cardcam;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.IOException;

import gun0912.tedbottompicker.TedBottomPicker;

public class TitleActivity extends AppCompatActivity {

    private static final String TAG = "TITLE";
    TedBottomPicker tedBottomPicker;
    // opencv library load
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    Mat matResult;
    public native boolean findCardFromImage(long matAddrInput, long matAddrOutput);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    // afetr loading library , some tasks here
                    if ( matResult != null ) matResult.release();

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
        setContentView(R.layout.activity_title);

        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        tedBottomPicker = new TedBottomPicker.Builder(TitleActivity.this)
                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        // here is selected uri
                        try {
                            Log.d(TAG, "uri=" + uri);
                            Log.d(TAG, "uri=" + uri.getPath());

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 4;
                            Bitmap bm = BitmapFactory.decodeFile(uri.getPath(), options);

                            Mat imgMAT = new Mat(bm.getHeight(), bm.getWidth(), CvType.CV_8UC4);
                            Utils.bitmapToMat(bm, imgMAT);
                            matResult = new Mat();

                            if (findCardFromImage(imgMAT.getNativeObjAddr(), matResult.getNativeObjAddr())) {
                                long matAddr = matResult.getNativeObjAddr();

                                Mat cardMat = new Mat(matAddr);
                                Bitmap bm2 = Bitmap.createBitmap(cardMat.cols(), cardMat.rows(), Bitmap.Config.ARGB_8888);
                                Utils.matToBitmap(cardMat, bm2);

                                Intent it = new Intent(getApplicationContext(), ProcessActivity.class);
                                it.putExtra("addr", matAddr);
                                startActivity(it);
                            } else {
                                Log.d(TAG, "fail");
                                //fail
                                Toast.makeText(getApplicationContext(), "다른사진넣어", Toast.LENGTH_SHORT).show();
                            }

                            imgMAT.release();

//                        }catch (FileNotFoundException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
                        }catch(Exception e){

                            Log.d(TAG, ""+e);
                        }


                    }
                })
                .create();


    }
    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void goCam(View view) {
        Intent it = new Intent(this, CamPreview.class);
        startActivity(it);
    }

    public void goGal(View view) {
        tedBottomPicker.show(getSupportFragmentManager());
    }
}
