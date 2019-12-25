package com.example.ds.final_project;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.ds.final_project.db.DTO.Size;
import com.example.ds.final_project.db.DTO.SizeSkirt;
import com.example.ds.final_project.db.DTO.User;
import com.example.ds.final_project.db.DTO.WishProduct;
import com.example.ds.final_project.db.DAO.DeleteWishProduct;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductInfo extends AppCompatActivity {
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_SHARE_MSG_SPEECH_INPUT = 101;
    Intent reviewIntent;
    private String mJsonString;
    int error=0;


    private TextView product_info; //상세정보 표시
    ImageView productImg; //상품 이미지 표시
    private Button wishCheck; //관심상품 등록
    private boolean infoBool=false; //관심상품 등록 여부
    private int check=0;
    //상품 정보
//    private Product product;
    private User user;
    private String productAlias="";
    private String uuid=" ";
    private String productId=" ";
//    private String optionNum="";
//    private String productURL=" ";
    private String info=" ";
    private String image=" ";
    private String size=" ";
    private String sizeTable=" ";
    //수신자 정보
    String phoneName = "";
    String phoneNo = "";

    Button sizeTableDetail;
    LinearLayout layout ;
    LinearLayout btnLayout;

    //통신사 정보
    String ret_operator = null;
    String MMSCenterUrl = null;
    String MMSProxy = null;
    int MMSPort = 0;

//    private String wishProductName=" ";
    WishProductDialog wishProductDialog;
    SendMsgDialog sendMsgDialog;
    private String Url="https://store.musinsa.com/app/product/detail/";

    //Layout 추가
    Context context;
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(

            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=getApplicationContext();
        wishProductDialog=new WishProductDialog(this);
        sendMsgDialog= new SendMsgDialog(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//뒤로가기 버튼

        sizeTableDetail=findViewById(R.id.sizeTableDetail);
        layout = findViewById(R.id.sizesLayout);
        btnLayout=findViewById(R.id.btnLayout);

        Gson gson = new GsonBuilder().create();
        if(!(getPreferences("USER")==null||getPreferences("USER")=="")){
            String strContact=getPreferences("USER");
            user=gson.fromJson(strContact,User.class);
            Log.d("uuid 정보",user.getName()+user.getAddress()+user.getPhoneNum());
        }
        uuid = getPreferences("uuid");
        product_info=(TextView)findViewById(R.id.product_info);
        product_info.setMovementMethod(new ScrollingMovementMethod());
        Intent intent = getIntent();

        productId=intent.getStringExtra("productId");
        Url+=productId;
        info=intent.getStringExtra("info");
        image=intent.getStringExtra("image");
        size=intent.getStringExtra("size");
        sizeTable=intent.getStringExtra("sizeTable");
        Log.d("sizeTable",sizeTable);


        productAlias=intent.getStringExtra("alias");
        if(productAlias==""||productAlias==null)
            getSupportActionBar().setTitle("상품 상세 정보");
        else
            getSupportActionBar().setTitle(productAlias+" 상세 정보");
        reviewIntent=new Intent(getApplicationContext(),ReviewActivity.class); //리뷰

        productImg=(ImageView)findViewById(R.id.productImg);
//        Log.i("이미지",""+image);
        Glide.with(this).load(image).into(productImg);

        product_info.setText(info);
        Url = Url + productId;
//        info = info + "\n" + Url;

        wishCheck=(Button) findViewById(R.id.wishCheck);
        wishCheck.setContentDescription("관심상품등록");


        TextView msgTextView = findViewById(R.id.msgTextView);
        String msg = "";

        //사이즈 가져오기
//        String str_size=produ
        Log.d("사이즈1",size);
        size.trim();
        if(size == null || size == ""|| size.equals("null")){
            Log.d("사이즈 ","업ㅅ");
            msg = "사이즈 정보가 존재하지 않습니다.";
            msgTextView.setText(msg);
        }
        else {
            Log.d("사이즈 ","있음 ");
            String[] sizes = size.split(",");
            if (sizes.length <= 0||sizes==null) {
                msg = "사이즈 정보가 존재하지 않습니다.";
                msgTextView.setText(msg);
            } else {
                Log.d("사이즈",sizes.toString());
                msg = "사이즈를 선택해주세요.";
                msgTextView.setText(msg);

                int id = 0;
                for (String size : sizes) {
                    Button btn = new Button(this);

                    // setId 버튼에 대한 키값

                    btn.setId(id);

                    btn.setText(size);

                    btn.setLayoutParams(params);

                    btn.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View v) {
                            GetSizeTable task=new GetSizeTable();
                            Log.d("sizeTable: ",sizeTable+","+productId+", "+size);

                            task.execute(sizeTable,productId,size);

                        }

                    });

                    //버튼 add
                    btnLayout.addView(btn);

                    id++;

                }
            }

        }
//        layout.addView(btnLayout);
        //등록된 상품인지 확인
        CheckWishProduct task = new CheckWishProduct();
        task.execute( "CheckWishProduct",uuid,productId);

        wishProductDialog.setDialogListener(new DialogListener() {
            @Override
            public void onPositiveClicked(String name) {
                productAlias=name;
                infoBool=true;
                Log.i("관심상품등록",uuid+productAlias);
                InsertWishProduct task = new InsertWishProduct();
                task.execute("InsertWishProduct",uuid,productAlias,productId,image,info,size,sizeTable);
                Toast.makeText(ProductInfo.this, "관심 상품으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                Log.i("관심2",productAlias);
                WishBtnChanged(infoBool);
            }

            @Override
            public void onNegativeClicked() {
                Log.d("dialog","취소");
            }


//            public void onSTTClicked(){
//                promptSpeechInput();
//            }
        });

    }

//    private void promptSpeechInput() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
//                context.getString(R.string.speech_prompt));
//        try {
//            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
//        } catch (ActivityNotFoundException a) {
//            Toast.makeText(context,
//                    context.getString(R.string.speech_not_supported),
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    wishProductDialog.setEditText(result.get(0)); //다이얼로그 editText 수정
                }
                break;
            }
            case REQ_CODE_SHARE_MSG_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    sendMsgDialog.setEditText(result.get(0)); //다이얼로그 editText 수정
                }
                break;
            }
        }
    }


    // 사이즈 상세보기 버튼 클릭

    public void onSizeDetailClicked(View view) {
        String btnText = sizeTableDetail.getText().toString();
        if(btnText.equals("사이즈표 상세보기")){
            sizeTableDetail.setText("사이즈표 닫기");
            layout.setVisibility(View.VISIBLE);
        }
        else{
            sizeTableDetail.setText("사이즈표 상세보기");
            layout.setVisibility(View.GONE);
        }



    }
    // 관심상품버튼 클릭
    public void onWishBtnClicked(View view){
        // 관심상품 등록
        if(infoBool == false){
            wishProductDialog.show();

        }
        // 관심상품 취소
        else{
            infoBool=false;
            DeleteWishProduct task = new DeleteWishProduct();
            Log.d("delete",uuid+", "+productAlias);
            task.execute( "DeleteWishProduct",uuid,productAlias);
            Toast.makeText(ProductInfo.this,"관심 상품 등록 취소되었습니다.",Toast.LENGTH_SHORT).show();
            WishBtnChanged(infoBool);
        }
       WishBtnChanged(infoBool);
    }
    // 관심상품버튼 상태 변경
    private void WishBtnChanged(Boolean infoBool){
        // 관심상품일 경우 : 관심상품취소버튼
        if(infoBool == false){
            wishCheck.setContentDescription("관심상품취소");
            wishCheck.setBackgroundResource(R.drawable.off);
        }
        // 관심상품아닐 경우 : 관심상품등록버튼
        else{
            wishCheck.setContentDescription("관심상품등록");
            wishCheck.setBackgroundResource(R.drawable.on);
        }
    }

    public void onReviewClicked(View view){
        // 리뷰 보기 버튼 
        reviewIntent.putExtra("product", productId);
        startActivity(reviewIntent);
    }
    //공유 다이얼로그
    public void onShareClicked(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("공유 방식을 선택해주세요.");

        builder.setItems(R.array.Messenger, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int pos)
            {
                String[] items = getResources().getStringArray(R.array.Messenger);
                //Toast.makeText(getApplicationContext(),items[pos],Toast.LENGTH_LONG).show();
                //문자공유
                if(items[pos].equals("문자")){
                    ShareMessage();
                }
                //카톡공유
                else{
                    ShareKakao();
                    getAppKeyHash();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void ShareMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("연락처 입력 방식을 선택해주세요");

        builder.setItems(R.array.Input, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int pos)
            {
                String[] items = getResources().getStringArray(R.array.Input);
                //Toast.makeText(getApplicationContext(),items[pos],Toast.LENGTH_LONG).show();
                // 주소록 검색
                if(items[pos].equals("주소록 검색")){
//                    searchPhone();

                    sendMsgDialog.setDialogListener(new DialogListener() {
                        @Override
                        public void onPositiveClicked(String name) {
                        }

                        @Override
                        public void onNegativeClicked() {
                            Log.d("dialog","취소");
                        }

                    });

                    sendMsgDialog.show();
                    sendMsgDialog.setText_guide("주소록에서 검색하실 이름을 입력해주세요");
                    sendMsgDialog.setEditTextHint("이름 입력");
                }
                // 직접 입력
                else{
                    inputPhonNo();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //연락처 검색_연락처 입력
    private void searchPhone(){
        AlertDialog.Builder ad = new AlertDialog.Builder(ProductInfo.this);

        // 제목 설정
        ad.setTitle("주소록에서 검색하실 연락처를 입력해주세요.");
        // 내용 설정
        //ad.setMessage("Message");

        // EditText 삽입하기
        final EditText et = new EditText(ProductInfo.this);
        ad.setView(et);

        // 전송 버튼 설정
        ad.setNegativeButton("검색", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.v(TAG, "Yes Btn Click");

                // Text 값 받아서 로그 남기기
                phoneName = et.getText().toString();
                //Log.v(TAG, value);
                findNum(phoneName);
                dialog.dismiss();     //닫기
                // Event
            }
        });

        // 취소 버튼 설정
        ad.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.v(TAG,"No Btn Click");
                dialog.dismiss();     //닫기
                // Event
            }
        });

        // 창 띄우기
        ad.show();
    }
    // 번호 직접 입력
    private void inputPhonNo(){
        AlertDialog.Builder ad = new AlertDialog.Builder(ProductInfo.this);

        // 제목 설정
        ad.setTitle("메시지를 받으실 분의 번호를 정확히 입력해주세요.");
        // 내용 설정
        //ad.setMessage("Message");

        // EditText 삽입하기
        final EditText et = new EditText(ProductInfo.this);
        ad.setView(et);

        // 전송 버튼 설정
        ad.setNegativeButton("보내기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.v(TAG, "Yes Btn Click");

                // Text 값 받아서 수신자로 지정
                phoneNo = et.getText().toString();
                sendMMS(phoneNo, "이 상품 구매 부탁드립니다!!");
                sendMMS(phoneNo, Url);
                sendMMS(phoneNo, "주소: "+user.getAddress());

                Toast.makeText(getApplicationContext(),"해당번호로 상품을 공유했습니다.",Toast.LENGTH_LONG).show();
                dialog.dismiss();     //닫기
                // Event
            }
        });

        // 취소 버튼 설정
        ad.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Log.v(TAG,"No Btn Click");
                dialog.dismiss();     //닫기
                // Event
            }
        });

        // 창 띄우기
        ad.show();
    }
    //주소록에서 번호 가져오기
    private void findNum(String fname){
        String number=null;
        Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc");
        int i=0;
        while (c.moveToNext()) {

            // 연락처 id 값
            String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
            // 연락처 대표 이름
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
//            Log.d("name",name);
            if(name.trim().equals(fname)) {

                // ID로 전화 정보 조회
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);

                // 데이터가 있는 경우
                if (phoneCursor.moveToFirst()) {
                    Log.d("name","찾");
                    number = phoneCursor.getString(phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));

                }
                phoneCursor.close();

            }
        }// end while
        c.close();
        if(number!=null) {
//            phoneNo = number;

            sendMMS(number, "이 상품 구매 부탁드립니다!!");
            sendMMS(number, Url);
            sendMMS(number, "주소: "+user.getAddress());
            Toast.makeText(getApplicationContext(),phoneName+"님께 해당 상품을 공유했습니다.",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(),phoneName+"님의 연락처는 없습니다.",Toast.LENGTH_LONG).show();
        }
    }
    private void sendMMS(String phoneNo,String msg) {
        //String sms = "http://deal.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=1708920758&cls=3791&trTypeCd=102";

        try {
            //전송
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }
    private void ShareKakao(){
        TextTemplate params = TextTemplate.newBuilder(info, LinkObject.newBuilder().setWebUrl("https://developers.kakao.com").setMobileWebUrl("https://developers.kakao.com").build()).build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Logger.e(errorResult.toString());
                Log.d("kakao",errorResult.toString());
            }
            @Override
            public void onSuccess(KakaoLinkResponse result) {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
            }
        });
    }
    // 값 불러오기
    private String  getPreferences(String key){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    private void savePreferences(String key, String s){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, s);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) { //뒤로가기 버튼 실행
        Intent homeIntent=new Intent(this,ChatbotActivity.class);
        switch (item.getItemId()){
            case android.R.id.home: //toolbar의 back키 눌렀을 때 동작
//                startActivity(homeIntent);
                finish();
                return true;

            case R.id.showme:
                startActivity(homeIntent);
                finish();
                return true;
            case R.id.help:
                AlertDialog.Builder oDialog = new AlertDialog.Builder(ProductInfo.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);

                oDialog.setTitle("도움말")
                        .setMessage("")
                        .setPositiveButton("닫기", null)
                        .setCancelable(true)
                        .show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //관심상품 등록 확인 클래스
    private class CheckWishProduct extends AsyncTask<String, Void,String> {
        String LoadData;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            String project = (String)params[0];
            String uid = (String)params[1];
            String id = (String)params[2];
            for(String p:params){
                Log.d("hi",p);
            }
            try {
                HttpParams httpParameters = new BasicHttpParams();
                HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);

                HttpClient client = new DefaultHttpClient(httpParameters);

                HttpConnectionParams.setConnectionTimeout(httpParameters, 7000);
                HttpConnectionParams.setSoTimeout(httpParameters, 7000);
                HttpConnectionParams.setTcpNoDelay(httpParameters, true);

                // 주소 : aws서버
                String postURL = "http://52.78.143.125:8080/showme/";

                HttpPost post = new HttpPost(postURL+project);
                //서버에 보낼 파라미터
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                //파라미터 추가하기

                postParameters.add(new BasicNameValuePair("uid", uid));
                postParameters.add(new BasicNameValuePair("id", id));

                //파라미터 보내기
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(postParameters, HTTP.UTF_8);
                post.setEntity(ent);

                long startTime = System.currentTimeMillis();

                HttpResponse responsePOST = client.execute(post);

                long elapsedTime = System.currentTimeMillis() - startTime;
                Log.v("debugging", elapsedTime + " ");


                HttpEntity resEntity = responsePOST.getEntity();
                if (resEntity != null) {
                    LoadData = EntityUtils.toString(resEntity, HTTP.UTF_8);

                    Log.d("가져온 데이터", LoadData);
                    return LoadData;
                }
                if(responsePOST.getStatusLine().getStatusCode()==200){
                    Log.d("오류없음","굳");
                }
                else{
                    Log.d("error","오류");
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

//            pDialog.dismiss();
            if (result == null||result==""){
                Log.d("wishList","아님 ");
                infoBool=false;
//                check=1;
            }
            else {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    // json객체.get("변수명")
                    JSONArray jArray = (JSONArray) jsonObj.get("getData");
//                    items = new ArrayList<Product>();
                    if(jArray.length()==0){
                        Log.d("wishList","아님");
                        infoBool=false;
//                        check=1;
//                        removePreferences("USER");
                    }else {
                        Log.d("wishList","맞음");
                        WishProduct wishProduct=new WishProduct();
                        for (int i = 0; i < jArray.length(); i++) {
                            // json배열.getJSONObject(인덱스)
                            JSONObject row = jArray.getJSONObject(i);
                            String id=row.getString("ID");
                            String alias=row.getString("ALIAS");

                            wishProduct.setId(id);
                            wishProduct.setAlias(alias);

                            productAlias=alias;
                            Log.d("가져온 데이터",id+", "+alias);
                        }
                        infoBool=true;
//                        check=1;
                    }

                } catch (JSONException e) {
                    Log.d("관심상품 등록 여부 확인 : ", e.getMessage());
                }
            }
            WishBtnChanged(infoBool);
        }
    }

    //관심상품 등록 클래스
    public class InsertWishProduct extends AsyncTask<String, Void, String> {
        String LoadData;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String project = (String)params[0];
            String uid = (String)params[1];
            String alias = (String)params[2];
            String id = (String)params[3];
            String image = (String)params[4];
            String info = (String)params[5];
            String size= (String)params[6];
            String sizeTable= (String)params[7];

            try {
                HttpParams httpParameters = new BasicHttpParams();
                HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);

                HttpClient client = new DefaultHttpClient(httpParameters);

                HttpConnectionParams.setConnectionTimeout(httpParameters, 7000);
                HttpConnectionParams.setSoTimeout(httpParameters, 7000);
                HttpConnectionParams.setTcpNoDelay(httpParameters, true);

                // 주소 : aws서버
                String postURL = "http://52.78.143.125:8080/showme/";

                // 로컬서버
//            String postURL = "http://10.0.2.2:8080/showme/InsertUser";

                HttpPost post = new HttpPost(postURL+project);
                //서버에 보낼 파라미터
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
//            파라미터 추가하기
                postParameters.add(new BasicNameValuePair("uid", uid));
                postParameters.add(new BasicNameValuePair("alias", alias));
                postParameters.add(new BasicNameValuePair("id", id));
                postParameters.add(new BasicNameValuePair("image", image));
                postParameters.add(new BasicNameValuePair("info", info));
                postParameters.add(new BasicNameValuePair("size", size));
                postParameters.add(new BasicNameValuePair("sizeTable", sizeTable));

                //파라미터 보내기
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(postParameters, HTTP.UTF_8);
                post.setEntity(ent);

                long startTime = System.currentTimeMillis();

                HttpResponse responsePOST = client.execute(post);

                long elapsedTime = System.currentTimeMillis() - startTime;
                Log.v("debugging", elapsedTime + " ");


                HttpEntity resEntity = responsePOST.getEntity();
                if (resEntity != null) {
                    LoadData = EntityUtils.toString(resEntity, HTTP.UTF_8);
                    Log.d("성공",LoadData);
                }
                if(responsePOST.getStatusLine().getStatusCode()==200){
//                    Log.d("디비 ","성공 이지만 중복이라 실패 ");

//                    return "fail";
                }
                else{

                    Log.d("실패 ","떙 ");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (LoadData == null){
                insertFail();
//            Toast.makeText(getApplicationContext(),"실패",Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    JSONObject jsonObj = new JSONObject(LoadData);
                    // json객체.get("변수명")
                    JSONArray jArray = (JSONArray) jsonObj.get("count");
//                    items = new ArrayList<Product>();
                    if(jArray.length()==0){
                        Log.d("검색"," 실패");
                        insertFail();
                    }else {
                        Log.i("검색","성공"+result);
                        infoBool=true;

                    }

                } catch (JSONException e) {
                    insertFail();
                    Log.d("검색 오류 : ", e.getMessage());
                }

            }
            WishBtnChanged(infoBool);
        }
    }
    private void insertFail(){
        infoBool=false;
        WishBtnChanged(infoBool);
        Toast.makeText(getApplicationContext(),"별칭이 중복되어 관심상품 등록에 실패했습니다.",Toast.LENGTH_LONG).show();
    }

    //사이즈표 가져오는 클래스
    private class GetSizeTable extends AsyncTask<String, Void,String> {
        String LoadData;
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ProductInfo.this);
            pDialog.setMessage("검색중입니다..");
            pDialog.setCancelable(false);
//            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub

            String project = (String) params[0];
            String id = (String) params[1];
            String size = (String) params[2];
            switch (project){
                case "SIZE_BOTTOM":
                    project="GetSizeBottom";
                    break;
                case "SIZE_DRESS_JUMPSUIT":
                    project="GetSizeDressJumpsuit";
                    break;
                case "SIZE_DRESS_ORIGINAL":
                    project="GetSizeDressOriginal";
                    break;
                case "SIZE_DRESS_SLEEVELESS":
                    project="GetSizeDressSleeveless";
                    break;
                case "SIZE_OUTER":
                    project="GetSizeOuter";
                    break;
                case "SIZE_TOP":
                    project="GetSizeTop";
                    break;
                case "SIZE_TOP_SLEEVELESS":
                    project="GetSizeTopSleeveless";
                    break;
                case "SIZE_TOP_VEST":
                    project="GetSizeTopVest";
                    break;
                case "SIZE_SKIRT":
                    project="GetSizeSkirt";
                    break;
            }
            try {
                HttpParams httpParameters = new BasicHttpParams();
                HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);

                HttpClient client = new DefaultHttpClient(httpParameters);

                HttpConnectionParams.setConnectionTimeout(httpParameters, 7000);
                HttpConnectionParams.setSoTimeout(httpParameters, 7000);
                HttpConnectionParams.setTcpNoDelay(httpParameters, true);

                // 주소 : aws서버
                String postURL = "http://52.78.143.125:8080/showme/";

                // 로컬서버
//            String postURL = "http://10.0.2.2:8080/showme/InsertUser";

                HttpPost post = new HttpPost(postURL + project);
                //서버에 보낼 파라미터
                ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                //파라미터 추가하기
                postParameters.add(new BasicNameValuePair("id", id));
                postParameters.add(new BasicNameValuePair("size", size));


                //파라미터 보내기
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(postParameters, HTTP.UTF_8);
                post.setEntity(ent);

                long startTime = System.currentTimeMillis();

                HttpResponse responsePOST = client.execute(post);

                long elapsedTime = System.currentTimeMillis() - startTime;
                Log.v("debugging", elapsedTime + " ");


                HttpEntity resEntity = responsePOST.getEntity();
                if (resEntity != null) {
//                ToastMessage("로그인 성공");

                    LoadData = EntityUtils.toString(resEntity, HTTP.UTF_8);
                    Log.i("chat가져온 데이터", LoadData);
                    return LoadData;

                }
                if (responsePOST.getStatusLine().getStatusCode() == 200) {
                    Log.d("search","오류없음");

                } else {
                    Log.d("search","오류있음");
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            if (result == null){
                Log.i("로긴","실패");
//            Toast.makeText(getApplicationContext(),"실패",Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    JSONObject jsonObj = new JSONObject(LoadData);
                    // json객체.get("변수명")
                    JSONArray jArray = (JSONArray) jsonObj.get("getData");
//                    items = new ArrayList<Product>();
                    if(jArray.length()==0){
                        Log.d("검색"," 실패");
                        Toast.makeText(getApplicationContext(),"사이즈표가 없습니다.",Toast.LENGTH_LONG).show();


                    }else {
                        for (int i = 0; i < jArray.length(); i++) {
                            // json배열.getJSONObject(인덱스)
                            JSONObject row = jArray.getJSONObject(i);

                            String st=row.getString("Size");

                            Log.i("sizeTable: ", st);

                            AlertDialog.Builder oDialog = new AlertDialog.Builder(ProductInfo.this,
                                    android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);



                            oDialog.setTitle("사이즈표 상세 정보")
                                    .setMessage(st)
                                    .setPositiveButton("닫기", null)
                                    .setCancelable(true)
                                    .show();


                        }

                    }

                } catch (JSONException e) {
                    Log.d("검색 오류 : ", e.getMessage());
                }

            }
        }

    }

}

