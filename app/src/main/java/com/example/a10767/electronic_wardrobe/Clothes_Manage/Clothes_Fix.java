package com.example.a10767.electronic_wardrobe.Clothes_Manage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a10767.electronic_wardrobe.ActivityCollector;
import com.example.a10767.electronic_wardrobe.Clothes_Message.Clothe_Jacket;
import com.example.a10767.electronic_wardrobe.OkHttpUtil;
import com.example.a10767.electronic_wardrobe.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.a10767.electronic_wardrobe.StaticVariable.clo_id;
import static com.example.a10767.electronic_wardrobe.StaticVariable.clo_name;
import static com.example.a10767.electronic_wardrobe.StaticVariable.login_account_Ed;
import static com.example.a10767.electronic_wardrobe.Main_Fragment.MyselfMine.CHOOSE_PHOTO;
import static com.example.a10767.electronic_wardrobe.Main_Fragment.MyselfMine.TAKE_PHOTO;
import static com.example.a10767.electronic_wardrobe.StaticVariable.url;
/**
 * Created by zwp on 2021/7/30.
 */

public class Clothes_Fix extends AppCompatActivity implements View.OnClickListener {
    /*??????*/
    private Map<String, String> map = new HashMap<String, String>();
    private File outputImage;
    private Uri imageUri;
    private Dialog dialog;
    private ViewGroup viewGroup;
    private ProgressDialog progressDialog; //??????
    private TextView start_head_portrait;//????????????
    private TextView start_photos;//????????????
    private TextView start_cancel;//????????????
    /*===================================================================================*/
    private static final String BOUNDARY = UUID.randomUUID().toString(); // ???????????? ????????????
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // ????????????
    private static final String CHARSET = "utf-8"; // ????????????
    private int readTimeOut = 10 * 1000; // ????????????
    private int connectTimeout = 10 * 1000; // ????????????
    /***
     * ????????????????????????
     */
    private static int requestTime = 0;
    /*??????*/
    private static final String TAG = "Clothes_Fix";
    private ImageView clothes_fix_IMG; //??????
    private String clothes_fix_IMG_S;
    private Bitmap bitmap;
    private EditText clothes_fix_color; //??????
    private String clothes_fix_color_Ed = "";
    private EditText clothes_fix_style; //??????
    private String clothes_fix_style_Ed = "";
    private EditText clothes_fix_season;
    private String clothes_fix_season_Ed = "";
    private EditText clothes_fix_weather;
    private String clothes_fix_weather_Ed = "";
    private EditText clothes_fix_text;
    private String clothes_fix_text_Ed = "";
    private LinearLayout clothes_fix_return; //??????
    private Button clothes_fix_save; //??????


    private final int SHOW_ERROR = -1;
    private final int UPLOAD_FAIL = -2;
    private final int SHOW_SUCCESS = 1;
    private final int UPLOAD_SUCCESS = 2;
    private final int FILE_ERROR = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_ERROR:
                    Toast.makeText(Clothes_Fix.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_SUCCESS:
                    clothes_fix_color.setText(clothes_fix_color_Ed);
                    clothes_fix_style.setText(clothes_fix_style_Ed);
                    clothes_fix_season.setText(clothes_fix_season_Ed);
                    clothes_fix_weather.setText(clothes_fix_weather_Ed);
                    clothes_fix_text.setText(clothes_fix_text_Ed);
                    clothes_fix_IMG.setImageBitmap(bitmap);
                    progressDialog.dismiss();
                    break;
                case FILE_ERROR:
                    progressDialog.dismiss();
                    Toast.makeText(Clothes_Fix.this, "???????????????", Toast.LENGTH_SHORT).show();
                    break;
                case UPLOAD_SUCCESS:
                    progressDialog.dismiss();
                    Toast.makeText(Clothes_Fix.this, "????????????", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Clothes_Fix.this, Clothes_Check.class));
                    overridePendingTransition(R.anim.enter2, R.anim.quit2);
                    break;
                case UPLOAD_FAIL:
                    progressDialog.dismiss();
                    Toast.makeText(Clothes_Fix.this, "??????????????????,????????????", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clothe_fix);
        initUI();
        initView();
        uploadMessage();//????????????
        ActivityCollector.addActivity(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Clothes_Fix.this, Clothes_Check.class));
        overridePendingTransition(R.anim.enter2, R.anim.quit2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    private void initView() {
        progressDialog.setCancelable(false);
        clothes_fix_save.setOnClickListener(this);
        clothes_fix_return.setOnClickListener(this);
        start_head_portrait.setOnClickListener(this);
        start_photos.setOnClickListener(this);
        start_cancel.setOnClickListener(this);
        clothes_fix_IMG.setOnClickListener(this);
    }

    private void initUI() {
        progressDialog = new ProgressDialog(Clothes_Fix.this);
        progressDialog.setMessage("?????????....");
        clothes_fix_IMG = findViewById(R.id.clothes_fix_IMG);
        clothes_fix_color = findViewById(R.id.clothes_fix_color);
        clothes_fix_style = findViewById(R.id.clothes_fix_style);
        clothes_fix_season = findViewById(R.id.clothes_fix_season);
        clothes_fix_weather = findViewById(R.id.clothes_fix_weather);
        clothes_fix_text = findViewById(R.id.clothes_fix_text);
        clothes_fix_return = findViewById(R.id.clothes_fix_return);
        clothes_fix_save = findViewById(R.id.clothes_fix_save);
        viewGroup = (ViewGroup) LayoutInflater.from(Clothes_Fix.this).inflate(R.layout.head_portrait_dialog, null);
        start_head_portrait = viewGroup.findViewById(R.id.start_head_portrait);//??????
        start_photos = viewGroup.findViewById(R.id.start_photos);//????????????
        start_cancel = viewGroup.findViewById(R.id.start_cancel);//????????????
        dialog = new AlertDialog.Builder(Clothes_Fix.this).create();//??????
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clothes_fix_IMG: //??????
                startDialog();
                break;
            case R.id.start_photos: //??????
                choosePicture();//????????????
                dialog.dismiss();
                break;
            case R.id.start_head_portrait: //??????
                recentFile();//???????????????
                dialog.dismiss();
                break;
            case R.id.start_cancel: //??????
                dialog.dismiss();
                break;
            case R.id.clothes_fix_save:
                progressDialog.show();
                receiveMessage();//???????????????
                break;
            case R.id.clothes_fix_return:
                startActivity(new Intent(Clothes_Fix.this, Clothes_Check.class));
                overridePendingTransition(R.anim.enter2, R.anim.quit2);
            break;
        }
    }

    /**
     * ????????????
     */
    private void receiveMessage() {
        clothes_fix_color_Ed = clothes_fix_color.getText().toString();
        clothes_fix_season_Ed = clothes_fix_season.getText().toString();
        clothes_fix_style_Ed = clothes_fix_style.getText().toString();
        clothes_fix_weather_Ed = clothes_fix_weather.getText().toString();
        clothes_fix_text_Ed = clothes_fix_text.getText().toString();
        map.put("username", login_account_Ed);
        map.put("cloId", String.valueOf(clo_id));
        map.put("cloColor", clothes_fix_color_Ed);
        map.put("cloSeason", clothes_fix_season_Ed);
        map.put("cloStyle", clothes_fix_style_Ed);
        map.put("cloWeather", clothes_fix_weather_Ed);
        map.put("cloName", clo_name);
        map.put("cloLabel", clothes_fix_text_Ed);
        Log.d(TAG, "????????????" + login_account_Ed
                + "CloId:" + clo_id
                + "??????:" + clothes_fix_color_Ed
                + "??????:" + clo_name
                + "??????:" + clothes_fix_season_Ed
                + "??????:" + clothes_fix_style_Ed
                + "??????:" + clothes_fix_weather_Ed
                + "??????:" + clothes_fix_text_Ed
                + "??????:" + outputImage);
        uploadClothes(outputImage, url + "LoginTest2/updateUserclothes.action", map); //????????????
    }

    /**
     * ????????????
     *
     * @param file
     * @param RequestURL
     * @param map
     */
    private void uploadClothes(final File file, final String RequestURL, final Map<String, String> map) {
        if (file == null || (!file.exists())) {
            Log.d(TAG, "???????????????");
            handler.sendEmptyMessage(FILE_ERROR);
            return;
        }
        Log.i(TAG, "?????????URL=" + RequestURL);
        Log.i(TAG, "?????????fileName=" + file.getName());
        new Thread(new Runnable() { //????????????????????????
            @Override
            public void run() {
                toUploadFile(file, RequestURL, map);
            }
        }).start();
    }

    /**
     * ????????????
     *
     * @param file
     * @param RequestURL
     * @param param
     */
    private void toUploadFile(File file, String RequestURL, Map<String, String> param) {
        String result = null;
        requestTime = 0;
        long requestTime = System.currentTimeMillis();
        long responseTime = 0;
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(readTimeOut);
            conn.setConnectTimeout(connectTimeout);
            conn.setDoInput(true); // ???????????????
            conn.setDoOutput(true); // ???????????????
            conn.setUseCaches(false); // ?????????????????????
            conn.setRequestMethod("POST"); // ????????????
            conn.setRequestProperty("Charset", CHARSET); // ????????????
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            /**
             * ????????????????????????????????????????????????
             */
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            StringBuffer sb = null;
            String params = "";

            /***
             * ???????????????????????????
             */
            if (param != null && param.size() > 0) {
                Iterator<String> it = param.keySet().iterator();
                while (it.hasNext()) {
                    sb = null;
                    sb = new StringBuffer();
                    String key = it.next();
                    String value = param.get(key);
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END);
                    sb.append(value).append(LINE_END);
                    params = sb.toString();
                    Log.i(TAG, key + "=" + params + "##");
                    dos.write(params.getBytes());
                    // dos.flush();
                }
            }
            sb = null;
            params = null;
            sb = new StringBuffer();
            /**
             * ????????????????????? name?????????????????????????????????key ????????????key ??????????????????????????????
             * filename??????????????????????????????????????? ??????:abc.png
             */
            sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
            sb.append("Content-Disposition:form-data; name=\"" + "cloPicture"
                    + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
            sb.append("Content-Type:image/pjpeg" + LINE_END); // ???????????????Content-type???????????? ?????????????????????????????????????????????
            sb.append(LINE_END);
            params = sb.toString();
            sb = null;
            Log.i(TAG, file.getName() + "=" + params + "##");
            dos.write(params.getBytes());
                         /*????????????*/
            InputStream is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            int curLen = 0;
            while ((len = is.read(bytes)) != -1) {
                curLen += len;
                dos.write(bytes, 0, len);
            }
            is.close();

            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dos.write(end_data);
            dos.flush();
            /**
             * ??????????????? 200=?????? ????????????????????????????????????
             */
            int res = conn.getResponseCode();
            responseTime = System.currentTimeMillis();
            this.requestTime = (int) ((responseTime - requestTime) / 1000);
            Log.e(TAG, "response code:" + res);
            if (res == 200) {
                Log.e(TAG, "request success");
                handler.sendEmptyMessage(UPLOAD_SUCCESS);
                return;
            } else {
                handler.sendEmptyMessage(UPLOAD_FAIL);
                Log.e(TAG, "request error" + res);
                Log.d(TAG, "???????????????code=" + res);
                return;
            }
        } catch (MalformedURLException e) {
            handler.sendEmptyMessage(UPLOAD_FAIL);
            Log.d(TAG, "???????????????error=" + e.getMessage());
            e.printStackTrace();
            return;
        } catch (IOException e) {
            handler.sendEmptyMessage(UPLOAD_FAIL);
            Log.d(TAG, "???????????????error=" + e.getMessage());
            e.printStackTrace();
            return;
        }
    }


    /**
     * ????????????
     */
    private void uploadMessage() {
        progressDialog.show();
        OkHttpUtil.postJson_DeleteClothes(url + "LoginTest2/findOneUserClothes.action", login_account_Ed, String.valueOf(clo_id), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(SHOW_ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String show_message = response.body().string();
                Log.d(TAG, show_message);
                try {
                    JSONObject jsonObject = new JSONObject(show_message);
                    clothes_fix_color_Ed = jsonObject.getString("cloColor");
                    clothes_fix_style_Ed = jsonObject.getString("cloStyle");
                    clothes_fix_season_Ed = jsonObject.getString("cloSeason");
                    clothes_fix_weather_Ed = jsonObject.getString("cloWeather");
                    clothes_fix_text_Ed = jsonObject.getString("cloLabel");
                    clothes_fix_IMG_S = jsonObject.getString("cloPicture");
                    bitmap = getBitmap(clothes_fix_IMG_S);
                    saveBitmapFile(bitmap);
                    handler.sendEmptyMessage(SHOW_SUCCESS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ????????????????????????
     */
    private void choosePicture() {
        if (ContextCompat.checkSelfPermission(Clothes_Fix.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Clothes_Fix.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    /**
     * bitmap???????????????file??????
     *
     * @param bitmap
     */
    public void saveBitmapFile(Bitmap bitmap) {
        outputImage = new File("/mnt/sdcard/01.jpg");
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputImage));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("broken");
        }
    }


    /**
     * //????????????
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//????????????
    }

    /**
     * ???????????????
     */
    private void recentFile() {
        outputImage = new File(getExternalCacheDir(), "head_portrait.jpg");
        Log.d(TAG, String.valueOf(getExternalCacheDir()));
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(Clothes_Fix.this, "myself_mine_head_portrait", outputImage);

        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //??????????????????
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * ??????????????????
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //??????????????????????????????
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        clothes_fix_IMG.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK)
                    //??????????????????
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4??????????????????????????????????????????
                        handleImageOnKitkat(data);
                    } else {
                        //4.4??????????????????????????????????????????
                        handleImageBeforeKitKat(data);
                    }
                break;
            default:
                break;
        }
    }

    /**
     * ??????dialog??????
     */
    private void startDialog() {
        Window window = dialog.getWindow();
        //??????????????????
        window.setWindowAnimations(R.style.AppTheme);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // ??????????????????????????????????????????????????????
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.onWindowAttributesChanged(wl);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        dialog.getWindow().setContentView(viewGroup);
    }

    /**
     * ????????????
     *
     * @param path
     * @return
     * @throws IOException
     */
    private Bitmap getBitmap(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }
        return null;
    }

    /**
     * 4.4????????????????????????
     *
     * @param data
     */
    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //?????????document?????????Uri
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];   //????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //?????????content?????????uri,???????????????????????????
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //?????????file?????????Uri,??????????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//??????????????????????????????
    }

    /**
     * ??????????????????
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * ??????????????????????????????
     *
     * @param imagePath
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            clothes_fix_IMG.setImageBitmap(bitmap);
            String temp[] = imagePath.replaceAll("\\\\", "/").split("/");
            String fileName = "";
            if (temp.length > 1) {
                fileName = temp[temp.length - 1];
                Log.d(TAG, fileName);
            }
            Log.d(TAG, imagePath);
            //??????????????????
            try {
                outputImage = saveFile(bitmap, Environment.getExternalStorageDirectory().toString(), fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ???????????????????????????Bitmap???????????????
     * ????????????
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public File saveFile(Bitmap bm, String path, String fileName) throws IOException {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path, fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
        return myCaptureFile;
    }

    /**
     * 4.4????????????????????????
     *
     * @param data
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }
}
