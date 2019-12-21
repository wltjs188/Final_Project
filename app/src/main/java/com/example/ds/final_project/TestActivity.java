package com.example.ds.final_project;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.ds.final_project.db.DTO.Product;
import com.example.ds.final_project.db.DTO.SizeSkirt;

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

import java.util.ArrayList;
import java.math.BigDecimal;
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        GetSizeSkirt task=new GetSizeSkirt();
        task.execute("GetSizeSkirt","710130","FREE");
    }
    private class GetSizeSkirt extends AsyncTask<String, Void,String> {
        String LoadData;
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(TestActivity.this);
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
                        Toast.makeText(getApplicationContext(),"검색된 상품이 없습니다.",Toast.LENGTH_LONG).show();


                    }else {
//                    Log.i("검색","성공"+result);
                        SizeSkirt sizeSkirt=new SizeSkirt();
                        for (int i = 0; i < jArray.length(); i++) {
                            // json배열.getJSONObject(인덱스)
                            JSONObject row = jArray.getJSONObject(i);

                            float total = BigDecimal.valueOf(row.getDouble("TOTAL")).floatValue();
                            float tail = BigDecimal.valueOf(row.getDouble("TAIL")).floatValue();
                            float waist = BigDecimal.valueOf(row.getDouble("WAIST")).floatValue();

                            sizeSkirt.setTotal(total);
                            sizeSkirt.setTail(tail);
                            sizeSkirt.setWaist(waist);
                            Log.i("shop가져온 데이터><", sizeSkirt.toString());

                        }

                    }

                } catch (JSONException e) {
                    Log.d("검색 오류 : ", e.getMessage());
                }

            }
        }

    }
}
