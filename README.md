# ShizukuRunner

<div align="center">
<img width="48" height="48" alt="Image" src="https://github.com/user-attachments/assets/b56172fa-260e-421e-8a71-e82c646042c9" />
</div>

If your primary language is not Chinese, you can use the [English Version](./README_en.md).

一个轻量级的 Android Shell 命令执行工具，通过 [Shizuku](https://shizuku.rikka.app/) 调用系统 shell，无需 root 即可执行 adb 权限级别的命令。

- 获取已激活 Shizuku 给予的权限
- 将常用命令保存到快捷格子，一键运行
- 支持 root 降权至 shell，降低权限风险
- 应用内中英双语切换
- 完全离线，不收集任何隐私数据

## 截图

首次进入弹窗↓

<img src="https://github.com/user-attachments/assets/98238403-614c-4938-8b67-3271d3c54499" width="160" />

主界面↓

<img src="https://github.com/user-attachments/assets/36b39ce7-17fa-423d-a696-db844ec82900" width="160" />

单次命令输入界面↓

<img src="https://github.com/user-attachments/assets/cbc0aa95-d005-4deb-9a4f-40e2beac1a8e" width="160" />

命令输出结果界面↓

<img src="https://github.com/user-attachments/assets/dbebe678-62b5-4608-8f31-535f2a4065ec" width="160" />

设置界面↓

<img src="https://github.com/user-attachments/assets/6eb4c589-9f95-411a-9371-9aca7b944de0" width="160" />

## 下载

[![Release](https://img.shields.io/github/v/release/September-meteor/ShizukuRunner?label=最新版本)](https://github.com/September-meteor/ShizukuRunner/releases/latest)

- 在 [Releases](https://github.com/September-meteor/ShizukuRunner/releases) 页面下载最新 APK
- 当前最低支持 Android 6.0（API 23）

### 使用须知

1. **本应用需要 Shizuku 环境**：请确保设备已安装并激活 [Shizuku](https://shizuku.rikka.app/)，且成功授权给本软件，否则无法运行
2. **测试设备有限**：主要在自己设备上测试通过，不同 ROM 或 Android 版本可能存在兼容性问题
3. **建议优先使用 Release 版本**：自行编译时若修改了 Gradle/AGP 版本，可能引入未预期的问题

如果安装后无法运行，排查上述须知后仍有问题，请携带设备型号、Android 版本、Shizuku 版本、详细问题描述 [提交 Issue](https://github.com/September-meteor/ShizukuRunner/issues)

## 使用方法

1. 确保设备已安装并激活 [Shizuku](https://shizuku.rikka.app/)
2. 首次打开授予 Shizuku 权限，按提示完成设置
3. 点击格子或「+」编辑命令
4. 编辑完成后点击「>」运行；长按输出结果可复制全部内容
5. 单击猫猫头像切换为单次命令模式，再次点击猫猫头像可返回主界面
6. 点击空白处退出软件

**常用操作**
- 长按猫猫头像：打开帮助与设置，点击 OK 可返回主界面
- 长按已保存的命令条目：复制命令到剪贴板
- 点击顶部 Shizuku 状态按钮：刷新授权状态

**注意事项**
- 命令直接写想执行的命令即可，**不要加 `adb shell` 前缀**
- 非 root 用户可忽略"降权"选项
- 请勿运行来源不明、可能破坏系统的命令

**扩展功能**
- **环境变量**：设置 → 编辑环境变量，可预设 KEY=VALUE 变量（如 PATH、LANG 等）
- **配置备份**：设置 → 导入/导出命令配置，支持一键备份和恢复

## 与原版的区别

本项目基于 [WuDi-ZhanShen/ShizukuRunner](https://github.com/WuDi-ZhanShen/ShizukuRunner) 的 ShizukuRunner 修改，主要改动包括：

**功能增强**
- 执行结果界面支持长按复制全部输出
- 补全猫猫头像翻转动画
- 添加应用内中英双语切换
- 添加多行命令输入与自动换行功能（三种模式：多行/仅换行/不换行）
- 添加命令配置导入/导出功能
- 添加自定义环境变量注入功能

**Bug 修复**
- 修复日志输出时的空指针崩溃
- 修复命令输出重定向（`>` / `>>`）在 Shizuku 环境下因 Binder 缓冲区限制导致的事务失败
- 修复 `root 降权至 shell` 功能异常
- 修复包含 `&&` 等符号的命令因 `\r` 导致的解析错误
- 修复 Shizuku 权限未声明导致的授权检测失效
- 修复主题跟随系统切换时部分界面颜色显示异常

**构建与维护**
- 升级 Gradle / AGP 工具链，移除废弃的 jcenter 仓库
- 修复资源类型引用与编译警告，消除 Release 构建错误
- 删除硬编码签名配置，解除 Windows 环境绑定
- 添加 Termux 编译支持，补充 .gitignore 与 LICENSE

## 自行编译

**桌面端（Android Studio）**：可参考 [delmsyap/ShizukuRunner](https://github.com/delmsyap/ShizukuRunner) 的 README。

**移动端（Termux）**：

本项目编译主要使用 Termux。若你尚未配置 Termux 编译环境（Android SDK、JDK 等），请先参考 [BUILD_TERMUX.md](./BUILD_TERMUX.md) 完成前置准备。

环境就绪后，按以下步骤编译：

1. 克隆仓库

```bash
git clone https://github.com/September-meteor/ShizukuRunner.git
cd ShizukuRunner
```

2. 取消 Termux 配置注释

```bash
sed -i 's/^# android.aapt2FromMavenOverride=/android.aapt2FromMavenOverride=/' gradle.properties
sed -i 's/^# org.gradle.java.home=/org.gradle.java.home=/' gradle.properties
```

3. 确保项目根目录存在 `local.properties`：

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

4. 编译

```bash
./gradlew :app:assembleRelease
```

5. 安装

```bash
cp app/build/outputs/apk/release/app-release-unsigned.apk /sdcard/Download/
```

在文件管理器中，选择下载文件夹，找到 app-release-unsigned.apk 点击安装。

## 许可证

原项目由 [WuDi-ZhanShen](https://github.com/WuDi-ZhanShen) 创建，未声明开源许可证。

本 fork 中由我（September-meteor/z） **新增及修改的代码** 以 [MIT](./LICENSE) 许可证发布。  
原项目代码的版权仍归原作者所有。

若原权利方对本项目的使用有任何异议，请 [提交 Issue](https://github.com/September-meteor/ShizukuRunner/issues) 或邮箱联系。