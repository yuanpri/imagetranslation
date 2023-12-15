package com.gpnu.yuan.livewords.ui;


import static com.gpnu.yuan.livewords.util.Constant.baidu_Image_apiKey;
import static com.gpnu.yuan.livewords.util.Constant.baidu_Image_apiSecret;
import static com.gpnu.yuan.livewords.util.Constant.baidu_Image_grantType;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gpnu.yuan.livewords.R;
import com.gpnu.yuan.livewords.adapter.DiscernResultAdapter;
import com.gpnu.yuan.livewords.model.GetDiscernResultResponse;
import com.gpnu.yuan.livewords.model.GetTokenResponse;
import com.gpnu.yuan.livewords.network.ApiService;
import com.gpnu.yuan.livewords.network.NetCallBack;
import com.gpnu.yuan.livewords.network.ServiceGenerator;
import com.gpnu.yuan.livewords.util.Base64Util;
import com.gpnu.yuan.livewords.util.Constant;
import com.gpnu.yuan.livewords.util.FileUtil;
import com.gpnu.yuan.livewords.util.SPUtils;
import com.gpnu.yuan.livewords.util.TranslationUtil;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class ImageTranslationActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    /**
     * 打开相册
     */
    private static final int OPEN_ALBUM_CODE = 100;
    /**
     * 打开相机
     */
    private static final int TAKE_PHOTO_CODE = 101;
    /**
     * Api服务
     */
    private ApiService service;
    /**
     * 鉴权Toeken
     */
    private String accessToken;
    /**
     * 显示图片
     */
    private ImageView ivPicture;
    /**
     * 进度条
     */
    private ProgressBar pbLoading;

    private RxPermissions rxPermissions;

    private File outputImage;
    private NiceSpinner spLanguage;//语言选择下拉框
    private String toLanguage = "auto";//翻译语言

    //配置初始数据
    private List<String> data = new LinkedList<>(Arrays.asList(
            "中文 → 英文", "中文 → 繁体中文", "中文 → 粤语", "中文 → 日语",
            "中文 → 韩语", "中文 → 法语", "中文 → 俄语",
            "中文 → 阿拉伯语", "中文 → 西班牙语 ", "中文 → 意大利语"));


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_translation);

        //初始化控件视图
        initView();

        service = ServiceGenerator.createService(ApiService.class);
        rxPermissions = new RxPermissions(this);
        //获取Token
        getAccessToken();
    }
    /**
     * 初始化控件视图
     */
    private void initView() {

        //设置亮色状态栏模式 systemUiVisibility在Android11中弃用了，可以尝试一下。
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setFullScreenImmersion();
        //控件初始化
        spLanguage = findViewById(R.id.sp_language);
        ivPicture = findViewById(R.id.iv_picture);
        pbLoading = findViewById(R.id.pb_loading);
        //设置下拉数据
        spLanguage.attachDataSource(data);
        spinnerListener();//下拉框选择监听

    }
    protected void setFullScreenImmersion() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }


    /**
     * 语言类型选择
     */
    private void spinnerListener() {
        spLanguage.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {

            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                switch (position) {
                    case 0://中文 → 英文
                        toLanguage = "en";
                        break;
                    case 1://中文 → 繁体中文
                        toLanguage = "cht";
                        break;
                    case 2://中文 → 粤语
                        toLanguage = "yue";
                        break;
                    case 3://中文 → 日语
                        toLanguage = "jp";
                        break;
                    case 4://中文 → 韩语
                        toLanguage = "kor";
                        break;
                    case 5://中文 → 法语
                        toLanguage = "fra";
                        break;
                    case 6://中文 → 俄语
                        toLanguage = "ru";
                        break;
                    case 7://中文 → 阿拉伯语
                        toLanguage = "ara";
                        break;
                    case 8://中文 → 西班牙语
                        toLanguage = "spa";
                        break;
                    case 9://中文 → 意大利语
                        toLanguage = "it";
                        break;
                    default:
                        break;
                }
            }
        });
    }


    /**
     * 获取鉴权Token
     */
    private String getAccessToken() {
        String token = SPUtils.getString(Constant.TOKEN, null, this);
        if (token == null) {
            //访问API获取接口
            requestApiGetToken();
        } else {
            //则判断Token是否过期
            if (isTokenExpired()) {
                //过期
                requestApiGetToken();
            } else {
                accessToken = token;
            }
        }
        return accessToken;
    }

    /**
     * 访问API获取接口
     */
    private void requestApiGetToken() {
        service.getToken(baidu_Image_grantType, baidu_Image_apiKey, baidu_Image_apiSecret)
                .enqueue(new NetCallBack<GetTokenResponse>() {
                    @Override
                    public void onSuccess(Call<GetTokenResponse> call, Response<GetTokenResponse> response) {
                        if (response.body() != null) {
                            //鉴权Token
                            accessToken = response.body().getAccess_token();
                            //过期时间 秒
                            long expiresIn = response.body().getExpires_in();
                            //当前时间 秒
                            long currentTimeMillis = System.currentTimeMillis() / 1000;
                            //放入缓存
                            Log.e(TAG, "onSuccess: " + accessToken);
                            SPUtils.putString(Constant.TOKEN, accessToken, ImageTranslationActivity.this);
                            SPUtils.putLong(Constant.GET_TOKEN_TIME, currentTimeMillis, ImageTranslationActivity.this);
                            SPUtils.putLong(Constant.TOKEN_VALID_PERIOD, expiresIn, ImageTranslationActivity.this);
                        }
                    }

                    @Override
                    public void onFailed(String errorStr) {
                        Log.e(TAG, "获取Token失败，失败原因：" + errorStr);
                        accessToken = null;
                    }
                });
    }

    /**
     * Token是否过期
     *
     * @return
     */
    private boolean isTokenExpired() {
        //获取Token的时间
        long getTokenTime = SPUtils.getLong(Constant.GET_TOKEN_TIME, 0, this);
        //获取Token的有效时间
        long effectiveTime = SPUtils.getLong(Constant.TOKEN_VALID_PERIOD, 0, this);
        //获取当前系统时间
        long currentTime = System.currentTimeMillis() / 1000;

        return (currentTime - getTokenTime) >= effectiveTime;
    }


    /**
     * 识别网络图片
     *
     * @param view
     */
    public void IdentifyWebPictures(View view) {
        pbLoading.setVisibility(View.VISIBLE);
        if (accessToken == null) {
            showMsg("获取AccessToken到null");
            return;
        }
        String imgUrl = "https://bce-baiyu.cdn.bcebos.com/14ce36d3d539b6004ef2e45fe050352ac65cb71e.jpeg";
        //显示图片
        Glide.with(this).load(imgUrl).into(ivPicture);
        showMsg("图像识别中");
        ImageDiscern(accessToken, null, imgUrl);
    }

    /**
     * 图像识别请求
     *
     * @param token       token
     * @param imageBase64 图片Base64
     * @param imgUrl      网络图片Url
     */
    private void ImageDiscern(String token, String imageBase64, String imgUrl) {
        service.getDiscernResult(token, imageBase64, imgUrl).enqueue(new NetCallBack<GetDiscernResultResponse>() {
            @Override
            public void onSuccess(Call<GetDiscernResultResponse> call, Response<GetDiscernResultResponse> response) {
                if(response.body() == null){
                    showMsg("未获得相应的识别结果");
                    return;
                }
                List<GetDiscernResultResponse.ResultBean> result = response.body().getResult();
                if (result != null && result.size() > 0) {
                    //显示识别结果
                    translate(result);
                } else {
                    pbLoading.setVisibility(View.GONE);
                    showMsg("未获得相应的识别结果");
                }
            }

            @Override
            public void onFailed(String errorStr) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "图像识别失败，失败原因：" + errorStr);
            }
        });
    }

    /**
     * 显示识别的结果列表
     *
     * @param result
     */
    private void showDiscernResult(List<GetDiscernResultResponse.ResultBean> result) {
        RecyclerView rvResult = findViewById(R.id.rv_translate);
        DiscernResultAdapter adapter = new DiscernResultAdapter(R.layout.item_result_rv, result);
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        rvResult.setAdapter(adapter);
        //隐藏加载
        pbLoading.setVisibility(View.GONE);

    }
    public void translate(List<GetDiscernResultResponse.ResultBean> resultBeans) {
        // 创建一个字符串列表来存储全部的关键词，然后将这些关键词连接成一个字符串并进行翻译。
        List<String> keywordList = new ArrayList<>();
        for (GetDiscernResultResponse.ResultBean result : resultBeans) {
            keywordList.add(result.getKeyword());
        }
        String keywords = String.join(";", keywordList);

        // 翻译关键字
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    return TranslationUtil.translateText(keywords,"zh",toLanguage);
                } catch (IOException e) {
                    Log.e("TranslationUtil", "Translation failed", e);
                    return null;
                }
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(String translation) {
                if (translation != null) {
                    // 将翻译后的字符串分割，并逐一将它们设置为item.getRoot()。
                    Log.d("translation",translation);
                    String[] translationArray = translation.split(";");
                    for (int i = 0; i < resultBeans.size(); i++) {
                        if (i < translationArray.length) {
                            Log.d("translate",translationArray[i]);
                            resultBeans.get(i).setTranslate(translationArray[i]);
                            Log.d("translate",resultBeans.get(i).getTranslate());
                        }
                    }
                    showDiscernResult(resultBeans);
                } else {
                    Log.e("Translation failed","ddddddd");
                }
            }
        }.execute();
    }


    /**
     * 识别相册图片
     *
     * @param view
     */
    @SuppressLint("CheckResult")
    public void IdentifyAlbumPictures(View view) {
        Log.d("PermissionDebug", "IdentifyTakePhotoImage: Start");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions.request(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(grant -> {
                        Log.d("PermissionDebug", "Permission Granted: " + grant);
                        if (grant) {
                            Log.d("PermissionDebug", "Permission Granted: " + grant);
                            //获得权限
                            openAlbum();
                        } else {
                            Log.d("PermissionDebug", "Permission Granted: " + grant);
                            showMsg("未获取到权限");
                        }
                    });
        } else {
            Log.d("PermissionDebug", "SDK_INT < M - Turn On Camera");
            openAlbum();
        }
    }

    /**
     * 识别拍照图片
     *
     * @param view
     */
    @SuppressLint("CheckResult")
    public void IdentifyTakePhotoImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions.request(
                            Manifest.permission.CAMERA)
                    .subscribe(grant -> {
                        if (grant) {
                            //获得权限
                            turnOnCamera();
                        } else {
                            showMsg("未获取到权限");
                        }
                    });
        } else {
            turnOnCamera();
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, OPEN_ALBUM_CODE);
    }

    /**
     * 打开相机
     */
    private void turnOnCamera() {
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("HH_mm_ss");
        String filename = timeStampFormat.format(new Date());
        //创建File对象
        outputImage = new File(getExternalCacheDir(), "takePhoto" + filename + ".jpg");
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(this,
                    "com.gpnu.yuan.livewords.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //打开相机
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO_CODE);
    }

    /**
     * Toast提示
     *
     * @param msg 内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            pbLoading.setVisibility(View.VISIBLE);
            if (requestCode == OPEN_ALBUM_CODE) {
                //打开相册返回
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                final Uri imageUri = Objects.requireNonNull(data).getData();
                Cursor cursor = getContentResolver().query(imageUri, filePathColumns, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumns[0]);
                //获取图片路径
                String imagePath = cursor.getString(columnIndex);
                cursor.close();
                //识别
                localImageDiscern(imagePath);

            } else if (requestCode == TAKE_PHOTO_CODE) {
                //拍照返回
                String imagePath = outputImage.getAbsolutePath();
                //识别
                localImageDiscern(imagePath);
            }
        } else {
            showMsg("什么都没有");
        }
    }

    /**
     * 本地图片识别
     */
    private void localImageDiscern(String imagePath) {
        try {
            if (accessToken == null) {
                showMsg("获取AccessToken到null");
                return;
            }
            //通过图片路径显示图片
            Glide.with(this).load(imagePath).into(ivPicture);
            //按字节读取文件
            byte[] imgData = FileUtil.readFileByBytes(imagePath);
            //字节转Base64
            String imageBase64 = Base64Util.encode(imgData);
            //图像识别
            ImageDiscern(accessToken, imageBase64, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
