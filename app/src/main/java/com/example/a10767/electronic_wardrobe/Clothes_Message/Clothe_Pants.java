package com.example.a10767.electronic_wardrobe.Clothes_Message;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.a10767.electronic_wardrobe.ActivityCollector;
import com.example.a10767.electronic_wardrobe.Clothes_Manage.Clothes_Check;
import com.example.a10767.electronic_wardrobe.Clothes_Manage.Clothes_Delete;
import com.example.a10767.electronic_wardrobe.Clothes_Manage.Clothes_Search;
import com.example.a10767.electronic_wardrobe.Clothes_Manage.Pants_Add;
import com.example.a10767.electronic_wardrobe.Main_Fragment.TheMain;
import com.example.a10767.electronic_wardrobe.OkHttpUtil;
import com.example.a10767.electronic_wardrobe.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.a10767.electronic_wardrobe.StaticVariable.checkOrSearch;
import static com.example.a10767.electronic_wardrobe.StaticVariable.clo_id;
import static com.example.a10767.electronic_wardrobe.StaticVariable.login_account_Ed;
import static com.example.a10767.electronic_wardrobe.StaticVariable.url;
import static com.example.a10767.electronic_wardrobe.StaticVariable.themain;
import static com.example.a10767.electronic_wardrobe.StaticVariable.clo_name;

/**
 * Created by zwp on 2021/7/19.
 */

public class Clothe_Pants extends AppCompatActivity implements View.OnClickListener {
    private ListView mListView;
    private String jacket_url; //??????
    private LinearLayout pants_return; //??????
    private Button pants_add; //??????
    private Button pants_manage; //??????
    private Button pants_search; //??????
    private static final String TAG = "Clothe_Jacket";
    private Clothes clothes;
    private ProgressDialog progressDialog; //??????
    private List<Clothes> newList = new ArrayList<>();
    private final int SUCCESS_SHOW = 1;
    private final int ERROR_SHOW = -1;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_SHOW:
                    new NewsAsyncTask().execute(jacket_url);
                    break;
                case ERROR_SHOW:
                    progressDialog.dismiss();
                    Toast.makeText(Clothe_Pants.this, "??????????????????", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clothe_pants);
        mListView = findViewById(R.id.pants_listView);
        initUI();
        initView();
        receiveClothes(); //??????????????????
        ActivityCollector.addActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        themain = 1;
        startActivity(new Intent(Clothe_Pants.this, TheMain.class));
        overridePendingTransition(R.anim.enter2, R.anim.quit2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private void initView() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkOrSearch = "check";
                clothes = newList.get(i);
                clo_id = clothes.getCloId();
                Log.d(TAG, String.valueOf(clo_id));
                startActivity(new Intent(Clothe_Pants.this, Clothes_Check.class));
                overridePendingTransition(R.anim.enter, R.anim.quit);
            }
        });
        pants_search.setOnClickListener(this);
        pants_manage.setOnClickListener(this);
        pants_add.setOnClickListener(this);
        pants_return.setOnClickListener(this);
    }

    private void initUI() {
        clo_name = "??????";
        progressDialog = new ProgressDialog(Clothe_Pants.this);
        progressDialog.setMessage("?????????....");
        progressDialog.setCancelable(false);
        pants_search=findViewById(R.id.pants_search);
        pants_manage = findViewById(R.id.pants_manage);
        pants_add = findViewById(R.id.pants_add);
        pants_return = findViewById(R.id.pants_return);
    }

    /**
     * ???url?????????JSON????????????????????????????????????NewsBean
     *
     * @param strings
     * @return
     */
    private List<Clothes> getJasonDate(String strings) {
        try {
            String jsonString = readStream(new URL(strings).openStream());
            //?????????????????????????????????????????????InputStream
            JSONObject jsonObject;
            jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("data");//???????????????
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                clothes = new Clothes();
/*===============================????????????======================================================*/
                clothes.setCloId(jsonObject.getInt("cloId"));
                clothes.setPicture(jsonObject.getString("cloPicture"));
                clothes.setClo_label(jsonObject.getString("cloLabel"));
                clothes.setClo_color(jsonObject.getString("cloColor"));
                clothes.setSeason(jsonObject.getString("cloSeason"));
                clothes.setStyle(jsonObject.getString("cloStyle"));
                clothes.setClo_collect(jsonObject.getBoolean("othercollect"));
                clothes.setWeather(jsonObject.getString("cloWeather"));
                Log.d(TAG, "CloId" + jsonObject.getInt("cloId")
                        +"????????????:" + jsonObject.getString("cloPicture")
                        + "??????:" + jsonObject.getString("cloLabel")
                        + "??????:" + jsonObject.getString("cloColor")
                        + "??????:" + jsonObject.getString("cloSeason")
                        + "??????:" + jsonObject.getString("cloStyle")
                        + "??????:" + jsonObject.getString("cloWeather"));
                newList.add(clothes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newList;
    }

    /**
     * ????????????
     *
     * @param is
     * @return
     */
    private String readStream(InputStream is) {
        InputStreamReader isr;
        String result = "";
        try {
            String line = "";
            isr = new InputStreamReader(is, "utf-8");  //?????????d??????????????????
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                result += line;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pants_add:
                startActivity(new Intent(Clothe_Pants.this, Pants_Add.class));
                overridePendingTransition(R.anim.enter, R.anim.quit);
                break;
            case R.id.pants_return:
                themain = 1;
                startActivity(new Intent(Clothe_Pants.this, TheMain.class));
                overridePendingTransition(R.anim.enter2, R.anim.quit2);
                break;
            case R.id.pants_manage:
                startActivity(new Intent(Clothe_Pants.this, Clothes_Delete.class));
                overridePendingTransition(R.anim.enter, R.anim.quit);
                break;
            case R.id.pants_search:
                startActivity(new Intent(Clothe_Pants.this, Clothes_Search.class));
                overridePendingTransition(R.anim.enter, R.anim.quit);
                break;
        }

    }

    /**
     * ???????????????????????????
     */
    class NewsAsyncTask extends AsyncTask<String, Void, List<Clothes>> {
        /**
         * ?????????List?????????????????????
         *
         * @param strings ???????????????
         * @return
         */
        @Override
        protected List<Clothes> doInBackground(String... strings) {
            return getJasonDate(strings[0]);
        }

        @Override
        protected void onPostExecute(List<Clothes> clothes) {
            super.onPostExecute(clothes);
            ClothesAdapter clothesAdapter = new ClothesAdapter(Clothe_Pants.this, clothes);
            progressDialog.dismiss();
            mListView.setAdapter(clothesAdapter);
        }
    }


    /**
     * ????????????
     */
    private void receiveClothes() {
        progressDialog.show();
          /*????????????*/
        OkHttpUtil.postJson_showClothes(url + "LoginTest2/findUserClothesBegin.action", login_account_Ed, "??????", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(ERROR_SHOW);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                jacket_url = response.body().string();
                Log.d(TAG, "??????:" + jacket_url);
                handler.sendEmptyMessage(SUCCESS_SHOW);
            }
        });

    }

}
