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
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.view.animation.LayoutAnimationController;

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
        //根据系统深色模式自动切换软件的深色/亮色主题
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);
        sp = getSharedPreferences("data", 0);
        //如果是初次开启，则展示help界面
        if (sp.getBoolean("first", true)) {
            showHelp();
            sp.edit().putBoolean("first", false).apply();
        }
        //读取用户设置“是否隐藏后台”，并进行隐藏后台
        ((ActivityManager) getSystemService(Service.ACTIVITY_SERVICE)).getAppTasks().get(0).setExcludeFromRecents(sp.getBoolean("hide", true));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        //限定一下横屏时的窗口宽度,让其不铺满屏幕。否则太丑
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            getWindow().getAttributes().width = (getWindowManager().getDefaultDisplay().getHeight());

        B = findViewById(R.id.b);
        C = findViewById(R.id.c);
        iv = findViewById(R.id.iv);

        //设置猫猫图案的长按事件为展示帮助界面
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
                        Switch S = v.findViewById(R.id.s);
                        S.setChecked(sp.getBoolean("hide", true));
                        S.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                sp.edit().putBoolean("hide", b).apply();
                                ((ActivityManager) getSystemService(Service.ACTIVITY_SERVICE)).getAppTasks().get(0).setExcludeFromRecents(b);
                            }
                        });
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
                                // 延时重启当前 Activity 以应用语言
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recreate();
                                    }
                                }, 500);
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
        boolean entering = d.getVisibility() == View.VISIBLE; // 当前列表可见 → 正要进入执行模式
        flipAnimation(view, entering); // 根据方向执行动画

        if (entering) {
            // 进入执行模式（隐藏列表，显示输入框）
            d.setVisibility(View.INVISIBLE);
            e.setVisibility(View.INVISIBLE);
            d.setAdapter(new adapter(this, new int[]{}));
            e.setAdapter(new adapter(this, new int[]{}));
            findViewById(R.id.l1).setVisibility(View.VISIBLE);
            e1 = findViewById(R.id.e);
            e1.setEnabled(true);
            e1.requestFocus();
            e1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(e1, 0);
                }
            }, 200);
            e1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        exe(v);
                    }
                    return false;
                }
            });
            e1.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                        exe(view);
                    return false;
                }
            });
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
        //flipAnimation是一个轻量级的翻转动画，很有趣哦
        //Fix: 增加了返回动画
        ObjectAnimator a2;
        if (enter) {
            a2 = ObjectAnimator.ofFloat(view, "rotationY", 0f, 180f); // 进入：0° → 180°（左右镜像）
        } else {
            a2 = ObjectAnimator.ofFloat(view, "rotationY", 180f, 0f); // 返回：180° → 0°
        }
        a2.setDuration(300).setInterpolator(new LinearInterpolator());
        a2.start();
    }

    public void exe(View view) {
        //EditText右边的执行按钮，点击后的事件
        if (e1.getText().length() > 0)
            startActivity(new Intent(this, Exec.class).putExtra("content", e1.getText().toString()));
    }

    public void initlist() {
        //根据用户设置，选择展示10个格子或者更多格子
        int[] e1 = sp.getBoolean("20", false) ? new int[]{5, 6, 7, 8, 9, 15, 16, 17, 18, 19, 25, 26, 27, 28, 29, 35, 36, 37, 38, 39, 45, 46, 47, 48, 49} : new int[]{5, 6, 7, 8, 9};
        int[] d1 = sp.getBoolean("20", false) ? new int[]{0, 1, 2, 3, 4, 10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34, 40, 41, 42, 43, 44} : new int[]{0, 1, 2, 3, 4};
        e.setAdapter(new adapter(this, e1));
        d.setAdapter(new adapter(this, d1));

        //加一点动画，非常的丝滑~~
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
}