package com.gpnu.yuan.livewords.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gpnu.yuan.livewords.R;
import com.gpnu.yuan.livewords.model.TranslateResult;
import com.gpnu.yuan.livewords.util.TranslationUtil;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 在线翻译
 *
 * @author llw
 */
public class TranslationActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout beforeLay;//翻译之前的布局
    private NiceSpinner spLanguage;//语言选择下拉框
    private LinearLayout afterLay;//翻译之后的布局
    private TextView tvFrom;//翻译源语言
    private TextView tvTo;//翻译目标语言

    private EditText edContent;//输入框（要翻译的内容）
    private ImageView ivClearTx;//清空输入框按钮
    private TextView tvTranslation;//翻译

    private LinearLayout resultLay;//翻译结果布局
    private TextView tvResult;//翻译的结果
    private ImageView ivCopyTx;//复制翻译的结果

    private String fromLanguage = "auto";//目标语言
    private String toLanguage = "auto";//翻译语言

    private ClipboardManager myClipboard;//复制文本
    private ClipData myClip; //剪辑数据


    //配置初始数据
    private List<String> data = new LinkedList<>(Arrays.asList(
            "自动检测语言", "中文 → 英文", "英文 → 中文",
            "中文 → 繁体中文", "中文 → 粤语", "中文 → 日语",
            "中文 → 韩语", "中文 → 法语", "中文 → 俄语",
            "中文 → 阿拉伯语", "中文 → 西班牙语 ", "中文 → 意大利语"));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        //初始化控件视图
        initView();
    }

    /**
     * 初始化控件视图
     */
    private void initView() {

        //设置亮色状态栏模式 systemUiVisibility在Android11中弃用了，可以尝试一下。
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setFullScreenImmersion();
        //控件初始化
        beforeLay = findViewById(R.id.before_lay);
        spLanguage = findViewById(R.id.sp_language);
        afterLay = findViewById(R.id.after_lay);
        tvFrom = findViewById(R.id.tv_from);
        tvTo = findViewById(R.id.tv_to);
        edContent = findViewById(R.id.ed_content);
        ivClearTx = findViewById(R.id.iv_clear_tx);
        tvTranslation = findViewById(R.id.tv_translation);
        resultLay = findViewById(R.id.result_lay);
        tvResult = findViewById(R.id.tv_result);
        ivCopyTx = findViewById(R.id.iv_copy_tx);

        //点击时间
        ivClearTx.setOnClickListener(this);
        ivCopyTx.setOnClickListener(this);
        tvTranslation.setOnClickListener(this);

        //设置下拉数据
        spLanguage.attachDataSource(data);
        editTextListener();//输入框监听
        spinnerListener();//下拉框选择监听
        //获取系统粘贴板服务
        myClipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);

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
     * 输入监听
     */
    private void editTextListener() {
        edContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮
            }

            @Override
            public void afterTextChanged(Editable s) {
                ivClearTx.setVisibility(View.VISIBLE);//显示清除按钮

                String content = edContent.getText().toString().trim();
                if (content.isEmpty()) {//为空
                    resultLay.setVisibility(View.GONE);
                    tvTranslation.setVisibility(View.VISIBLE);
                    beforeLay.setVisibility(View.VISIBLE);
                    afterLay.setVisibility(View.GONE);
                    ivClearTx.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 语言类型选择
     */
    private void spinnerListener() {
        spLanguage.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {

            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                switch (position) {
                    case 0://自动检测
                        fromLanguage = "auto";
                        toLanguage = fromLanguage;
                        break;
                    case 1://中文 → 英文
                        fromLanguage = "zh";
                        toLanguage = "en";
                        break;
                    case 2://英文 → 中文
                        fromLanguage = "en";
                        toLanguage = "zh";
                        break;
                    case 3://中文 → 繁体中文
                        fromLanguage = "zh";
                        toLanguage = "cht";
                        break;
                    case 4://中文 → 粤语
                        fromLanguage = "zh";
                        toLanguage = "yue";
                        break;
                    case 5://中文 → 日语
                        fromLanguage = "zh";
                        toLanguage = "jp";
                        break;
                    case 6://中文 → 韩语
                        fromLanguage = "zh";
                        toLanguage = "kor";
                        break;
                    case 7://中文 → 法语
                        fromLanguage = "zh";
                        toLanguage = "fra";
                        break;
                    case 8://中文 → 俄语
                        fromLanguage = "zh";
                        toLanguage = "ru";
                        break;
                    case 9://中文 → 阿拉伯语
                        fromLanguage = "zh";
                        toLanguage = "ara";
                        break;
                    case 10://中文 → 西班牙语
                        fromLanguage = "zh";
                        toLanguage = "spa";
                        break;
                    case 11://中文 → 意大利语
                        fromLanguage = "zh";
                        toLanguage = "it";
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 页面点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        //清空输入框
        if (v.getId()==R.id.iv_clear_tx) {
            edContent.setText("");//清除文本
            ivClearTx.setVisibility(View.GONE);//清除数据之后隐藏按钮
        }
        //复制翻译后的结果
        else if (v.getId()==R.id.iv_copy_tx) {
            String inviteCode = tvResult.getText().toString();
            myClip = ClipData.newPlainText("text", inviteCode);
            myClipboard.setPrimaryClip(myClip);
            showMsg("已复制");
        }
        //翻译
        else if (v.getId()==R.id.tv_translation) {
            translation();//翻译
        }

    }

    /**
     * 翻译
     */
    private void translation() {
        //获取输入的内容
        String inputTx = edContent.getText().toString().trim();
        //判断输入内容是否为空
        if (!inputTx.isEmpty() || !"".equals(inputTx)) {//不为空
            tvTranslation.setText("翻译中...");
            tvTranslation.setEnabled(false);//不可更改，同样就无法点击

            //异步Get请求访问网络
            new AsyncTask<Void, Void, TranslateResult>() {
                @Override
                protected TranslateResult doInBackground(Void... voids) {
                    try {
                        return TranslationUtil.translateTextSync(inputTx,fromLanguage,toLanguage);
                    } catch (IOException e) {
                        Log.e("TranslationUtil", "Translation failed", e);
                        return null;
                    }
                }

                @SuppressLint("StaticFieldLeak")
                @Override
                protected void onPostExecute(TranslateResult result) {
                    if (result != null) {
                        tvTranslation.setText("翻译");
                        tvTranslation.setEnabled(true);

                        tvTranslation.setVisibility(View.GONE);
                        //显示翻译的结果
                        if(result.getTrans_result().get(0).getDst() == null){
                            showMsg("数据为空");
                        }
                        tvResult.setText(result.getTrans_result().get(0).getDst());
                        resultLay.setVisibility(View.VISIBLE);
                        beforeLay.setVisibility(View.GONE);
                        afterLay.setVisibility(View.VISIBLE);
                        //翻译成功后的语言判断显示
                        initAfter(result.getFrom(), result.getTo());


                    } else {
                        showMsg("数据为空");
                        Log.e("Translation failed","ddddddd");
                    }
                }
            }.execute();


        } else {//为空
            showMsg("请输入要翻译的内容！");
        }
    }



    /**
     * 翻译成功后的语言判断显示
     */
    private void initAfter(String from, String to) {
        if (("zh").equals(from)) {
            tvFrom.setText("中文");
        } else if (("en").equals(from)) {
            tvFrom.setText("英文");
        } else if (("yue").equals(from)) {
            tvFrom.setText("粤语");
        } else if (("cht").equals(from)) {
            tvFrom.setText("繁体中文");
        } else if (("jp").equals(from)) {
            tvFrom.setText("日语");
        } else if (("kor").equals(from)) {
            tvFrom.setText("韩语");
        } else if (("fra").equals(from)) {
            tvFrom.setText("法语");
        } else if (("ru").equals(from)) {
            tvFrom.setText("俄语");
        } else if (("ara").equals(from)) {
            tvFrom.setText("阿拉伯语");
        } else if (("spa").equals(from)) {
            tvFrom.setText("西班牙语");
        } else if (("it").equals(from)) {
            tvFrom.setText("意大利语");
        }
        if (("zh").equals(to)) {
            tvTo.setText("中文");
        } else if (("en").equals(to)) {
            tvTo.setText("英文");
        } else if (("yue").equals(to)) {
            tvTo.setText("粤语");
        } else if (("cht").equals(to)) {
            tvTo.setText("繁体中文");
        } else if (("jp").equals(to)) {
            tvTo.setText("日语");
        } else if (("kor").equals(to)) {
            tvTo.setText("韩语");
        } else if (("fra").equals(to)) {
            tvTo.setText("法语");
        } else if (("ru").equals(to)) {
            tvTo.setText("俄语");
        } else if (("ara").equals(to)) {
            tvTo.setText("阿拉伯语");
        } else if (("spa").equals(to)) {
            tvTo.setText("西班牙语");
        } else if (("it").equals(to)) {
            tvTo.setText("意大利语");
        }
    }

    /**
     * Toast提示
     *
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
