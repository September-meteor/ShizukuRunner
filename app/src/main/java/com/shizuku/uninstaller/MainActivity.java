package com.shizuku.uninstaller;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.util.Locale;

import rikka.shizuku.Shizuku;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    boolean b, c;
    Button B, C;
    int m;
    ListView d, e;
    EditText e1;
    ImageView iv;
    SharedPreferences sp;
    // 多行模式相关（现在由设置直接控制，不需要切换按钮）
    private ImageButton toggleButton;
    // 自定义 Toast 相关
    private Handler toastHandler;
    private PopupWindow customPopup;
    // 静态实例供 adapter 使用
    private static MainActivity instance;

    //shizuku监听授权结果
    private final Shizuku.OnRequestPermissionResultListener RL = this::onRequestPermissionsResult;

    private void onRequestPermissionsResult(int i, int i1) {
        check();
    }

    // 应用语言设置
    private void applyLanguage() {
        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        String lang = prefs.getString("language", "zh");
        Locale locale;
        if ("en".equals(lang)) {
            locale = Locale.ENGLISH;
        } else {
            locale = Locale.CHINESE;
        }
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, res.getDisplayMetrics());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先应用语言设置
        applyLanguage();

        super.onCreate(savedInstanceState);
        instance = this;
        
        //根据系统深色模式自动切换软件的深色/亮色主题
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);
        sp = getSharedPreferences("data", 0);
        //如果是初次开启，则展示help界面
        if (sp.getBoolean("first", true)) {
            showHelp();
            sp.edit().putBoolean("first", false).apply();
        }
        //读取用户设置"是否隐藏后台"，并进行隐藏后台
        ((ActivityManager) getSystemService(Service.ACTIVITY_SERVICE)).getAppTasks().get(0).setExcludeFromRecents(sp.getBoolean("hide", true));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //限定一下横屏时的窗口宽度,让其不铺满屏幕。否则太丑
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());

        B = findViewById(R.id.b);
        C = findViewById(R.id.c);
        iv = findViewById(R.id.iv);

        //设置猫猫图案的长按事件为展示帮助界面（保持不变）
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showHelp();
                return false;
            }
        });

        //shizuku返回授权结果时将执行RL函数
        Shizuku.addRequestPermissionResultListener(RL);

        //m用于保存shizuku状态显示按钮的初始颜色（int类型哦），为的是适配安卓12的莫奈取色，方便以后恢复颜色时用
        m = B.getCurrentTextColor();

        //检查Shizuk是否运行，并申请Shizuku权限
        check();
        d = findViewById(R.id.list);
        e = findViewById(R.id.lista);

        //为两列listView适配每个item的具体样式和总item数
        initlist();
    }

    // 供外部调用的静态方法：获取自动换行设置
    public static boolean isAutoWrapEnabled() {
        if (instance == null || instance.sp == null) return true;
        return instance.sp.getBoolean("auto_wrap", true);
    }

    // 供外部调用的静态方法：应用自动换行到 EditText
    public static void applyAutoWrapToEditText(EditText editText) {
        if (editText == null) return;
        boolean autoWrap = isAutoWrapEnabled();
        if (autoWrap) {
            editText.setHorizontallyScrolling(false);
            editText.setSingleLine(false);
            editText.setMaxLines(5);
            editText.setMinLines(1);
        } else {
            editText.setSingleLine(true);
            editText.setHorizontallyScrolling(true);
            editText.setMaxLines(1);
        }
    }

    private void showHelp() {
        //展示帮助界面
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.help, null);
        ((TextView) v.findViewById(R.id.t3)).setText(Html.fromHtml(getString(R.string.help_text_part1)));
        ((TextView) v.findViewById(R.id.t4)).setText(Html.fromHtml(getString(R.string.help_text_part2)));
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.help_title)
                .setView(v)
                .setNegativeButton(R.string.button_ok, null)
                .setNeutralButton(R.string.button_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                        dialog.getWindow().getAttributes().alpha = 0.85f;
                        dialog.getWindow().setGravity(Gravity.BOTTOM);

                        View v = View.inflate(MainActivity.this, R.layout.set, null);
                        
                        // 隐藏后台开关
                        Switch S = v.findViewById(R.id.s);
                        S.setChecked(sp.getBoolean("hide", true));
                        S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                sp.edit().putBoolean("hide", b).apply();
                                ((ActivityManager) getSystemService(Service.ACTIVITY_SERVICE)).getAppTasks().get(0).setExcludeFromRecents(b);
                            }
                        });
                        
                        // 20格开关
                        Switch S1 = v.findViewById(R.id.s1);
                        S1.setChecked(sp.getBoolean("20", false));
                        S1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                sp.edit().putBoolean("20", b).apply();
                                Toast.makeText(MainActivity.this, R.string.restart_toast, Toast.LENGTH_SHORT).show();
                            }
                        });
                        
                        // 语言切换开关
                        Switch langSwitch = v.findViewById(R.id.lang_switch);
                        langSwitch.setChecked("en".equals(sp.getString("language", "zh")));
                        langSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                sp.edit().putString("language", isChecked ? "en" : "zh").apply();
                                Toast.makeText(MainActivity.this, R.string.restart_toast, Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recreate();
                                    }
                                }, 500);
                            }
                        });

                        // 多行命令模式开关（替代原来的实验性功能）
                        Switch multiLineSwitch = v.findViewById(R.id.multi_line_switch);
                        multiLineSwitch.setChecked(sp.getBoolean("multi_line_mode", false));
                        multiLineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                sp.edit().putBoolean("multi_line_mode", isChecked).apply();
                                Toast.makeText(MainActivity.this, R.string.restart_toast, Toast.LENGTH_SHORT).show();
                            }
                        });

                        // 自动换行开关
                        Switch autoWrapSwitch = v.findViewById(R.id.auto_wrap_switch);
                        autoWrapSwitch.setChecked(sp.getBoolean("auto_wrap", true));
                        autoWrapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                sp.edit().putBoolean("auto_wrap", isChecked).apply();
                                // 如果当前在命令输入模式，立即应用更改
                                if (e1 != null && findViewById(R.id.l1).getVisibility() == View.VISIBLE) {
                                    applyAutoWrapToEditText(e1);
                                }
                            }
                        });

                        dialog.setView(v);
                        dialog.show();
                    }
                })
                .create().show();
    }

    private void check() {
        //本函数用于检查shizuku状态，b代表shizuk是否运行，c代表shizuku是否授权
        b = true;
        c = false;
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                Shizuku.requestPermission(0);
            else c = true;
        } catch (Exception e) {
            if (checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED)
                c = true;
            if (e.getClass() == IllegalStateException.class) {
                b = false;
                Toast.makeText(this, R.string.shizuku_not_running_toast, Toast.LENGTH_SHORT).show();
            }
        }
        B.setText(b ? getString(R.string.shizuku_running) : getString(R.string.shizuku_not_running));
        B.setTextColor(b ? m : 0x77ff0000);
        C.setText(c ? getString(R.string.shizuku_granted) : getString(R.string.shizuku_not_granted));
        C.setTextColor(c ? m : 0x77ff0000);
    }

    @Override
    protected void onDestroy() {
        //在APP退出时，取消注册Shizuku授权结果监听，这是Shizuku的要求
        Shizuku.removeRequestPermissionResultListener(RL);
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //在点击返回键时直接退出APP，因为APP比较轻量，没必要双击返回退出或者设置什么退出限制
        finish();
    }

    public void ch(View view) {
        //本函数绑定了主界面两个显示Shizuk状态的按钮的点击事件
        check();
    }

    public void ex(View view) {
        //单击猫猫头像的点击事件，让list变不可见，让EditText可见。
        boolean entering = d.getVisibility() == View.VISIBLE;
        flipAnimation(view, entering);

        if (entering) {
            // 进入执行模式（隐藏列表，显示输入框）
            d.setVisibility(View.INVISIBLE);
            e.setVisibility(View.INVISIBLE);
            d.setAdapter(new adapter(this, new int[]{}));
            e.setAdapter(new adapter(this, new int[]{}));
            findViewById(R.id.l1).setVisibility(View.VISIBLE);
            e1 = findViewById(R.id.e);
            toggleButton = findViewById(R.id.toggle_mode);

            // 隐藏切换按钮（现在由设置直接控制，不需要切换按钮了）
            toggleButton.setVisibility(View.GONE);

            // 根据设置决定是单行还是多行模式
            boolean multiLineMode = sp.getBoolean("multi_line_mode", false);
            if (multiLineMode) {
                setupMultiLineMode();
            } else {
                setupSingleLineMode();
            }

            e1.setEnabled(true);
            e1.requestFocus();
            e1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(e1, 0);
                }
            }, 200);
        } else {
            // 返回主界面（显示列表，隐藏输入框）
            d.setVisibility(View.VISIBLE);
            e.setVisibility(View.VISIBLE);
            e1.setEnabled(false);
            initlist();
            findViewById(R.id.l1).setVisibility(View.GONE);
        }

        // 重置点击监听，保证下次点击仍然进入此方法
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ex(view);
            }
        });
    }

    private void flipAnimation(View view, boolean enter) {
        ObjectAnimator a2;
        if (enter) {
            a2 = ObjectAnimator.ofFloat(view, "rotationY", 0f, 180f);
        } else {
            a2 = ObjectAnimator.ofFloat(view, "rotationY", 180f, 0f);
        }
        a2.setDuration(300).setInterpolator(new LinearInterpolator());
        a2.start();
    }

    public void exe(View view) {
        if (e1.getText().length() > 0) {
            String raw = e1.getText().toString();
            String clean = raw.replace("\r", "");
            startActivity(new Intent(this, Exec.class).putExtra("content", clean));
        }
    }

    public void initlist() {
        int[] e1 = sp.getBoolean("20", false) ? new int[]{5, 6, 7, 8, 9, 15, 16, 17, 18, 19, 25, 26, 27, 28, 29, 35, 36, 37, 38, 39, 45, 46, 47, 48, 49} : new int[]{5, 6, 7, 8, 9};
        int[] d1 = sp.getBoolean("20", false) ? new int[]{0, 1, 2, 3, 4, 10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44} : new int[]{0, 1, 2, 3, 4};
        e.setAdapter(new adapter(this, e1));
        d.setAdapter(new adapter(this, d1));

        TranslateAnimation animation = new TranslateAnimation(-50f, 0f, -30f, 0f);
        animation.setDuration(500);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        d.setLayoutAnimation(controller);
        animation = new TranslateAnimation(50f, 0f, -30f, 0f);
        animation.setDuration(500);
        controller = new LayoutAnimationController(animation, 0.1f);
        e.setLayoutAnimation(controller);
    }

    // ========== 输入模式设置 ==========

    private void setupSingleLineMode() {
        // 单行模式：回车直接执行，不换行
        e1.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        
        // 应用自动换行设置
        applyAutoWrapToEditText(e1);
        
        // 设置回车执行监听
        e1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    exe(v);
                    return true;
                }
                return false;
            }
        });
        e1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    exe(v);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupMultiLineMode() {
        // 多行模式：回车换行，需点击运行按钮执行
        e1.setSingleLine(false);
        e1.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        e1.setHorizontallyScrolling(false);
        e1.setMinLines(3);   // 多行模式下最少显示3行
        e1.setMaxLines(10);  // 最多10行
        
        // 移除单行模式的监听器，让回车默认换行
        e1.setOnEditorActionListener(null);
        e1.setOnKeyListener(null);
    }

    // ========== 自定义 Toast ==========
    private void showShortToast(String msg) {
        if (toastHandler == null) toastHandler = new Handler();
        if (customPopup != null && customPopup.isShowing()) {
            customPopup.dismiss();
            toastHandler.removeCallbacksAndMessages(null);
        }

        TextView textView = new TextView(this);
        textView.setText(msg);
        textView.setBackgroundColor(0xCC000000);
        textView.setTextColor(0xFFFFFFFF);
        textView.setPadding(32, 16, 32, 16);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(10);
        textView.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.5));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setElevation(6);
            textView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            textView.setClipToOutline(true);
        }

        customPopup = new PopupWindow(textView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        customPopup.setClippingEnabled(false);
        customPopup.setFocusable(false);
        customPopup.setTouchable(false);
        customPopup.setOutsideTouchable(false);
        customPopup.setBackgroundDrawable(null);

        View rootView = getWindow().getDecorView().getRootView();
        customPopup.showAtLocation(rootView, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);

        toastHandler.postDelayed(() -> {
            if (customPopup != null && customPopup.isShowing()) {
                customPopup.dismiss();
                customPopup = null;
            }
        }, 800);
    }
}