package com.dongyang.pjw.cardcam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import gun0912.tedbottompicker.TedBottomPicker;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    private long lastBackTime;
    TedBottomPicker tedBottomPicker;

    Mat matResult;

    public native boolean findCardFromImage(long matAddrInput, long matAddrOutput);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("CardSaver 명함인식");

        //////
        if ( matResult != null ) matResult.release();

        tedBottomPicker = new TedBottomPicker.Builder(MainActivity.this)
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

        /////
        if ( matResult != null ) matResult.release();
    }

    public void goCam(View view) {
        Intent it = new Intent(this, CamPreview.class);
        startActivity(it);
    }

    public void goGal(View view) {
        tedBottomPicker.show(getSupportFragmentManager());
    }

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis() - lastBackTime < 2000){
            finish();
        }
        Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastBackTime = System.currentTimeMillis();
    }
}
