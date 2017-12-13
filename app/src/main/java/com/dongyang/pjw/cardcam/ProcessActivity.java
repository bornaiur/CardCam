package com.dongyang.pjw.cardcam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "AIzaSyAcwY51x27djQPL6q8Ecjgp9tWmQAwffNs";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = "PROCESS";

    private CloudVision visionTask;
    Bitmap bm;
    long matAddr;
    Intent it;

    TextView tv;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        processMat();
    }


    @Override
    public void onResume() {

        super.onResume();
    }


    public void processMat(){
        try {
            Log.d(TAG, "processmat1");
            it = getIntent();
            matAddr = it.getLongExtra("addr", 0);
            Mat cardMat = new Mat(matAddr);
            Log.d(TAG, "processmat2");
            bm = Bitmap.createBitmap(cardMat.cols(), cardMat.rows(), Bitmap.Config.ARGB_8888);
            Log.d(TAG, "processmat3");
            Utils.matToBitmap(cardMat, bm);
            Log.d(TAG, "processmat4");
            iv = (ImageView)findViewById(R.id.imv);
            iv.setImageBitmap(bm);
            tv = (TextView)findViewById(R.id.tv);
            tv.setMovementMethod(new ScrollingMovementMethod());
            tv.setText("처리중");

            if(bm == null){
                tv.setText("bm is null");
            }
            visionTask = new CloudVision(bm);
            visionTask.execute();
//            callCloudVision(bm);
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    public void onDestroy() {
        super.onDestroy();
    }

//    private void callCloudVision(final Bitmap bitmap) throws IOException {
//
//        // Do the real work in an async task, because we need to use the network anyway
//        new AsyncTask<Object, Void, String>() {
//            @Override
//            protected String doInBackground(Object... params) {
//                try {
//                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
//                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
//
//                    VisionRequestInitializer requestInitializer =
//                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
//                                /**
//                                 * We override this so we can inject important identifying fields into the HTTP
//                                 * headers. This enables use of a restricted cloud platform API key.
//                                 */
//                                @Override
//                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
//                                        throws IOException {
//                                    super.initializeVisionRequest(visionRequest);
//
//                                    String packageName = getPackageName();
//                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);
//
//                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);
//
//                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
//                                }
//                            };
//
//                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
//                    builder.setVisionRequestInitializer(requestInitializer);
//
//                    Vision vision = builder.build();
//
//                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
//                            new BatchAnnotateImagesRequest();
//                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
//                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
//
//                        // Add the image
//                        Image base64EncodedImage = new Image();
//                        // Convert the bitmap to a JPEG
//                        // Just in case it's a format that Android understands but Cloud Vision
//                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
//                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
//
//                        // Base64 encode the JPEG
//                        base64EncodedImage.encodeContent(imageBytes);
//                        annotateImageRequest.setImage(base64EncodedImage);
//
//                        // add the features we want
//                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
//                            Feature labelDetection = new Feature();
//                            labelDetection.setType("TEXT_DETECTION");
//                            add(labelDetection);
//                        }});
//
//                        // Add the list of one thing to the request
//                        add(annotateImageRequest);
//                    }});
//
//                    Vision.Images.Annotate annotateRequest =
//                            vision.images().annotate(batchAnnotateImagesRequest);
//                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
//                    annotateRequest.setDisableGZipContent(true);
//                    Log.d("vision", "created Cloud Vision request object, sending request");
//
//                    BatchAnnotateImagesResponse response = annotateRequest.execute();
//                    return convertResponseToString(response);
//
//                } catch (GoogleJsonResponseException e) {
//                    Log.d("vision", "failed to make API request because " + e.getContent());
//                } catch (IOException e) {
//                    Log.d("vision", "failed to make API request because of other IOException " +
//                            e.getMessage());
//                }
//                return "Cloud Vision API request failed. Check logs for details.";
//            }
//
//            protected void onPostExecute(String result) {
//                tv.setText(result);
//            }
//        }.execute();
//    }

    public class CloudVision extends AsyncTask<Void, Void, String>{

        private final Bitmap bm;

        CloudVision(Bitmap bitmap){
            bm = bitmap;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                VisionRequestInitializer requestInitializer =
                        new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                            /**
                             * We override this so we can inject important identifying fields into the HTTP
                             * headers. This enables use of a restricted cloud platform API key.
                             */
                            @Override
                            protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                    throws IOException {
                                super.initializeVisionRequest(visionRequest);

                                String packageName = getPackageName();
                                visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                            }
                        };

                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                builder.setVisionRequestInitializer(requestInitializer);

                Vision vision = builder.build();

                BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                        new BatchAnnotateImagesRequest();
                batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                    // Add the image
                    Image base64EncodedImage = new Image();
                    // Convert the bitmap to a JPEG
                    // Just in case it's a format that Android understands but Cloud Vision
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Base64 encode the JPEG
                    base64EncodedImage.encodeContent(imageBytes);
                    annotateImageRequest.setImage(base64EncodedImage);

                    // add the features we want
                    annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                        Feature labelDetection = new Feature();
                        labelDetection.setType("TEXT_DETECTION");
                        labelDetection.setMaxResults(10);
                        add(labelDetection);
                    }});

                    // Add the list of one thing to the request
                    add(annotateImageRequest);
                }});

                Vision.Images.Annotate annotateRequest =
                        vision.images().annotate(batchAnnotateImagesRequest);
                // Due to a bug: requests to Vision API containing large images fail when GZipped.
                annotateRequest.setDisableGZipContent(true);
                Log.d("vision", "created Cloud Vision request object, sending request");

                BatchAnnotateImagesResponse response = annotateRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d("vision", "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d("vision", "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            tv.setText(result);
        }
    }


    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";


        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
//            for (EntityAnnotation label : labels) {
//                message += String.format("%s: %s", label.getBoundingPoly().toString(), label.getDescription());
//                message += "\n";
//            }

            message = labels.get(0).getDescription();
        } else {
            message += "nothing";
        }

        return message;
    }

}