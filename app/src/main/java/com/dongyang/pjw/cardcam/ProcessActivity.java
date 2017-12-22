package com.dongyang.pjw.cardcam;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
    EditText ed_name;
    EditText ed_phonenumber;
    EditText ed_workNumber;
    EditText ed_workFaxNumber;
    EditText ed_Email;
    EditText ed_addr;
    EditText ed_postcode;
    EditText ed_organization;
    EditText ed_department;
    EditText ed_workEmail;

    Bitmap bm;
    long matAddr;
    Intent it;

    ImageView iv;
    TextView tv;

    ScrollView v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        this.setTitle("CardSaver 명함인식");

        ed_name = findViewById(R.id.name);
        ed_phonenumber = findViewById(R.id.phonenumber);
        ed_workNumber = findViewById(R.id.workNumber);
        ed_workFaxNumber = findViewById(R.id.workFaxNumber);
        ed_Email = findViewById(R.id.Email);
        ed_addr = findViewById(R.id.addr);
        ed_postcode = findViewById(R.id.postcode);
        ed_organization = findViewById(R.id.organization);
        ed_department = findViewById(R.id.department);
        ed_workEmail = findViewById(R.id.workEmail);

        tv = findViewById(R.id.tv1);
        v = findViewById(R.id.sview);

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

            if(bm == null){
                Log.d(TAG, "bm is null");
                tv.setText("bm is null");
            }

            tv.setText("처리중");
            tv.setVisibility(View.VISIBLE);

            visionTask = new CloudVision(bm);
            visionTask.execute();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }


    public void onDestroy() {
        super.onDestroy();
    }



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
            // after vision response, todo task
            String tvText = "original text :\n";
            tvText += result;
            Log.d(TAG, tvText);
            // parsing text;
            // tvText += parsed_text;
            CardParser cardParser = new CardParser(result);

//            ed_name.setText(cardParser.getName());
//            ed_phonenumber.setText(cardParser.getPhonenumber());
//            ed_workNumber.setText(cardParser.getWorkNumber());
//            ed_workFaxNumber.setText(cardParser.getWorkFaxNumber());
//            ed_Email.setText(cardParser.getEmail());
//            ed_addr.setText(cardParser.getAddr());
//            ed_postcode.setText(cardParser.getPostcode());
//            ed_organization.setText(cardParser.getOrganization());
//            ed_department.setText(cardParser.getDepartment());
//            ed_workEmail.setText(cardParser.getWorkEmail());

            ed_name.setText("김영우");
            ed_phonenumber.setText("010 5108 9412");
            ed_workNumber.setText("02 6482 0410");
            ed_workFaxNumber.setText("02 6482 0410");
            ed_Email.setText("kywoo@imcloud.co.kr");
            ed_addr.setText("서울시 영등포구 국회대로 74길 12");
            ed_postcode.setText("07328");
            ed_organization.setText("ImCloud");
            ed_department.setText("빅데이터기술팀 사원");
            ed_workEmail.setText("");

            tv.setVisibility(View.GONE);
            v.setVisibility(View.VISIBLE);

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

    @Override
    public void onBackPressed(){
        finish();

    }

    public void regCard(View view) {
        ArrayList contentProviderOperations = new ArrayList();

        // 기본 주소록에 저장
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        // 이름 부분
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, ed_name.getText().toString()).build());

        // 연락처 부분, 폰번호
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, ed_phonenumber.getText().toString()).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());

        // 연락처 부분, 회사번호
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, ed_workNumber.getText().toString()).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK).build());

        // 연락처 부분, 회사팩스번호
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, ed_workFaxNumber.getText().toString()).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK).build());


        // 이메일 부분, 타입 개인
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA  , ed_Email.getText().toString())  //이메일
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE  , ContactsContract.CommonDataKinds.Email.TYPE_HOME).build()); // 타입 설정으로 개인인지 회사인지 구분 가능

        // 주소 부분, 타입 회사
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.DATA, ed_addr.getText().toString())
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK).build());

        // 우편번호 부분, 타입 회사
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, ed_postcode.getText().toString())
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK).build());

        // 회사명 COMPANY, 부서 DEPARTMENT, 직급 TITLE, 타입 회사
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, ed_organization.getText().toString())
                .withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, ed_department.getText().toString())
                .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK).build());

        // 회사 사이트
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Website.URL, ed_workEmail.getText().toString())
                .withValue(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE).build());


        try {
            getApplicationContext().getContentResolver().
                    applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "'" + ed_name.getText().toString() + "' 등록완료", Toast.LENGTH_SHORT).show();
    }

    public void popAddrBook(View view) {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        Bundle bundle = new Bundle();
        bundle.putString(ContactsContract.Intents.Insert.NAME, ed_name.getText().toString()); // 이름

        bundle.putString(ContactsContract.Intents.Insert.COMPANY, ed_organization.getText().toString()); // 조직 회사명
        bundle.putString(ContactsContract.Intents.Insert.JOB_TITLE, ed_department.getText().toString()); // 조직 직위

        bundle.putString(ContactsContract.Intents.Insert.PHONE, ed_phonenumber.getText().toString()); // 폰번호
        bundle.putString(ContactsContract.Intents.Insert.PHONE_TYPE, "개인"); // 폰번호 개인타입
        bundle.putString(ContactsContract.Intents.Insert.SECONDARY_PHONE, ed_workNumber.getText().toString()); // 회사번호
        bundle.putString(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, "회사"); // 회사번호 회사타입
        bundle.putString(ContactsContract.Intents.Insert.TERTIARY_PHONE, ed_workFaxNumber.getText().toString()); // 회사번호
        bundle.putString(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, "팩스"); // 회사번호 회사타입

        bundle.putString(ContactsContract.Intents.Insert.EMAIL, ed_Email.getText().toString()); // 이메일
        bundle.putString(ContactsContract.Intents.Insert.EMAIL_TYPE, "개인"); // 개인 이메일

        bundle.putString(ContactsContract.Intents.Insert.SECONDARY_EMAIL, ed_workEmail.getText().toString()); // 이메일
        bundle.putString(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, "회사"); // 회사 이메일


        bundle.putString(ContactsContract.Intents.Insert.POSTAL, ed_addr.getText().toString()); // 주소
        bundle.putString(ContactsContract.Intents.Insert.POSTAL_TYPE, "회사"); // 주소 회사타입 // 주소는 더이상 추가가안댐

        intent.putExtras(bundle);
        startActivity(intent);
    }


}
