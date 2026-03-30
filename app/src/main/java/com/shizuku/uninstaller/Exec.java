package com.shizuku.uninstaller;

import android.app.Activity;
import android.app.Service;
import android.app.UiModeManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import rikka.shizuku.Shizuku;

public class Exec extends Activity {

    TextView t1, t2;
    Process p;
    Thread h1, h2, h3;
    boolean br = false;

    //mHandler用于弱引用和主线程更新UI
    protected MyHandler mHandler = new MyHandler(this);

    public static class MyHandler extends Handler {
        private final WeakReference<Exec> mOuter;

        public MyHandler(Exec activity) {
            mOuter = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Exec activity = mOuter.get();
            if (activity == null || activity.t2 == null) return;

            CharSequence text;
            if (msg.what == 1) {
                // 错误消息：红色 SpannableString
                if (msg.obj instanceof SpannableString) {
                    text = (SpannableString) msg.obj;
                } else {
                    String str = msg.obj != null ? msg.obj.toString() : "null";
                    SpannableString ss = new SpannableString(str);
                    ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text = ss;
                }
            } else {
                // 普通消息
                text = msg.obj != null ? String.valueOf(msg.obj) : "";
            }
            activity.t2.append(text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.exec_title_running));

        //根据系统深色模式动态改变深色主题
        if (((UiModeManager) getSystemService(Service.UI_MODE_SERVICE)).getNightMode() == UiModeManager.MODE_NIGHT_NO)
            setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog);

        //半透明背景
        getWindow().getAttributes().alpha = 0.85f;
        setContentView(R.layout.exec);
        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        
        // ===== 设置输出文本自动换行 =====
        t2.setHorizontallyScrolling(false);
        
        t2.requestFocus();
        t2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                    finish();
                return false;
            }
        });

        // 添加长按复制功能
        t2.setOnLongClickListener(v -> {
            String text = t2.getText().toString();
            if (!text.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Command Output", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(Exec.this, R.string.copied_output_to_clipboard, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Exec.this, R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        //子线程执行命令
        h1 = new Thread(new Runnable() {
            @Override
            public void run() {
                ShizukuExec(getIntent().getStringExtra("content"));
            }
        });
        h1.start();
    }

    public void ShizukuExec(String cmd) {
        try {
            // 解析重定向
            String actualCmd = cmd;
            String redirectPath = null;
            boolean append = false;

            if (cmd.contains(">>")) {
                int index = cmd.indexOf(">>");
                actualCmd = cmd.substring(0, index).trim();
                redirectPath = cmd.substring(index + 2).trim();
                append = true;
            } else if (cmd.contains(">")) {
                int index = cmd.indexOf('>');
                actualCmd = cmd.substring(0, index).trim();
                redirectPath = cmd.substring(index + 1).trim();
                append = false;
            }

            // 清理路径引号
            if (redirectPath != null) {
                if (redirectPath.startsWith("\"") && redirectPath.endsWith("\"")) {
                    redirectPath = redirectPath.substring(1, redirectPath.length() - 1);
                } else if (redirectPath.startsWith("'") && redirectPath.endsWith("'")) {
                    redirectPath = redirectPath.substring(1, redirectPath.length() - 1);
                }
            }

            long time = System.currentTimeMillis();

            // 有重定向时，用临时文件中转
            final String tempFilePath = redirectPath != null ?
                    "/data/local/tmp/shizuku_" + System.currentTimeMillis() + ".tmp" : null;
            final String finalRedirectPath = redirectPath;
            final boolean finalAppend = append;

            String shCmd = actualCmd;
            if (redirectPath != null) {
                shCmd = actualCmd + " > " + tempFilePath;
            }

            p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((shCmd + "\nexit\n").getBytes());
            out.flush();
            out.close();

            h2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String inline;
                        while ((inline = mReader.readLine()) != null) {
                            if (t2.length() > 2000 || br) break;
                            Message msg = new Message();
                            msg.what = 0;
                            msg.obj = inline.equals("") ? "\n" : inline + "\n";
                            mHandler.sendMessage(msg);
                        }
                        mReader.close();
                    } catch (Exception ignored) {
                    }
                }
            });
            h2.start();

            h3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader mReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String inline;
                        while ((inline = mReader.readLine()) != null) {
                            if (t2.length() > 2000 || br) break;
                            Message msg = new Message();
                            msg.what = 1;
                            if (inline.equals(""))
                                msg.obj = null;
                            else {
                                SpannableString ss = new SpannableString(inline + "\n");
                                ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                msg.obj = ss;
                            }
                            mHandler.sendMessage(msg);
                        }
                        mReader.close();
                    } catch (Exception ignored) {
                    }
                }
            });
            h3.start();

            p.waitFor();

            // 有重定向时，处理文件复制
            if (p.exitValue() == 0 && finalRedirectPath != null && tempFilePath != null) {
                Process checkProcess = Shizuku.newProcess(new String[]{"sh"}, null, null);
                OutputStream checkOut = checkProcess.getOutputStream();
                checkOut.write(("[ -f " + tempFilePath + " ] && echo 1 || echo 0\nexit\n").getBytes());
                checkOut.flush();
                checkOut.close();

                BufferedReader checkReader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
                String checkResult = checkReader.readLine();
                checkReader.close();
                checkProcess.waitFor();

                if ("1".equals(checkResult)) {
                    // 复制文件
                    Process copyProcess = Shizuku.newProcess(new String[]{"sh"}, null, null);
                    OutputStream copyOut = copyProcess.getOutputStream();

                    String dirPath = finalRedirectPath.substring(0, finalRedirectPath.lastIndexOf('/'));
                    String copyCmd = "mkdir -p '" + dirPath + "' && cat '" + tempFilePath + "' ";
                    copyCmd += finalAppend ? ">> '" : "> '";
                    copyCmd += finalRedirectPath + "'";

                    copyOut.write((copyCmd + "\nexit\n").getBytes());
                    copyOut.flush();
                    copyOut.close();
                    copyProcess.waitFor();
                }

                // 清理临时文件
                Process cleanupProcess = Shizuku.newProcess(new String[]{"sh"}, null, null);
                OutputStream cleanupOut = cleanupProcess.getOutputStream();
                cleanupOut.write(("rm -f '" + tempFilePath + "'\nexit\n").getBytes());
                cleanupOut.flush();
                cleanupOut.close();
                cleanupProcess.waitFor();
            }

            String exitValue = String.valueOf(p.exitValue());
            t1.post(new Runnable() {
                @Override
                public void run() {
                    t1.setText(String.format(getString(R.string.return_value_format), exitValue, (System.currentTimeMillis() - time) / 1000f));
                    setTitle(getString(R.string.exec_title_finished));
                }
            });

        } catch (Exception e) {
            Message msg = new Message();
            msg.what = 1;
            SpannableString ss = new SpannableString("执行异常: " + e.getMessage() + "\n");
            ss.setSpan(new ForegroundColorSpan(Color.RED), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            msg.obj = ss;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        //关闭所有输入输出流，销毁进程，防止内存泄漏等问题
        br = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        p.destroyForcibly();
                    } else {
                        p.destroy();
                    }
                    h1.interrupt();
                    h2.interrupt();
                    h3.interrupt();
                } catch (Exception ignored) {
                }
            }
        }, 1000);
        super.onDestroy();
    }
}