# ShizukuRunner

## 一、声明

<img width="146" height="146" alt="Image" src="https://github.com/user-attachments/assets/ae4cee82-4810-45e4-a596-4d47fde46bfa" />

- 本项目为 [WuDi-ZhanShen/ShizukuRunner](https://github.com/WuDi-ZhanShen/ShizukuRunner) 的非官方衍生版本。
- **由于上游项目未提供 LICENSE 文件，本项目仅供学习交流使用，严禁用于商业用途。**
- 本仓库的核心业务代码版权归原作者 [WuDi-ZhanShen](https://github.com/WuDi-ZhanShen) 所有，本人（September-meteor）仅对本文档第二部分所提到的内容做出了修改。
- 如有侵犯原作者权益，请联系本人删除。

---

## 二、与原版的区别

本版本在保留原版全部功能的基础上，做了以下改进：

### 1. 双语支持与界面优化
- **增加中英双语切换**：将所有硬编码字符串抽取至资源文件，新增完整英文翻译，设置中可一键切换语言，并修复了相关的资源编译错误。
- **优化猫猫点击动画**：绕 Y 轴 180° 翻转，使其从单次命令模式返回时同样有翻转动画，视觉更连贯。
- **增加多行输入模式和自动换行功能**：在设置中开启对应选项后，输入框将支持多行命令输入或自动换行。

### 2. Bug 修复
- **修复 Shizuku 环境下重定向命令执行失败**：解决了在 Shizuku 中执行带 `>` 或 `>>` 重定向符的命令时，因 Binder 事务缓冲区限制导致的 `Failed transaction` 错误。通过解析重定向符、引入临时文件中转机制，确保了大输出量命令的文件写入成功。
- **修复执行命令时空消息导致的崩溃**：增加了 `null` 检查和 `activity` 判空，错误信息统一标红。
- **修复“将 root 降权至 shell”功能**：原版依赖可能不存在的 `libchid.so` 并试图直接执行它，现改用 `su shell -c` 实现，不会在运行后输出相关报错。
- **修复签名硬编码路径和不再兼容的语法**：避免在非 Windows 环境编译时报错，同时将 Kotlin DSL 语法修正为 Groovy DSL（AGP 7.2.2 要求）。
- **修复 drawable 资源类型引用错误**：将 `@string/xxx` 改为 `@drawable/xxx`，使 Release 构建正常通过，减小了安装包体积。

### 3. 构建与配置优化
- **添加 `.gitignore` 文件**：忽略构建产物（`.gradle/`、`app/build/` 等）、IDE 文件和临时文件，保持仓库整洁。
- **升级 Gradle 至 7.5.1，AGP 至 7.2.2**，移除废弃的 `jcenter()` 仓库，避免构建报错。
- **消除编译警告**：添加 `compileOnly annotation:1.3.0` 解决 Scope 枚举警告；用 `@SuppressWarnings("deprecation")` 抑制已弃用 API 警告。
- **为 Termux 环境编译添加配置注释**：在 `gradle.properties` 中提供了 aapt2 和 JDK 17 路径的示例，方便有需要的用户直接启用。

---

## 三、下载

你可以在 [Releases 页面](https://github.com/September-meteor/ShizukuRunner/releases) 下载预编译的 APK 文件。

但是这个版本因为本人能力有限，因此该版本**仅实验性构建，不保证一定可用，仅供测试**。

或者按照下面的方法自行编译。

---

## 四、如何编译

如果你使用桌面端（如 Android Studio），可参考另一个衍生版本 [delmsyap/ShizukuRunner](https://github.com/delmsyap/ShizukuRunner)，查看来自 [delmsyap](https://github.com/delmsyap) 的 [README.md](https://github.com/delmsyap/ShizukuRunner/blob/main/README.md)

如果你使用移动端（如 Termux），可参考以下编译流程：

### 环境要求
- Android SDK (API 23+)
- JDK 8 或更高版本
- 主要使用 Termux

### 步骤

1. 在 Termux 上下载必要的工具（如果你完全没有类似经验）：
```bash
pkg install git openjdk-17 aapt2 wget unzip 
```

2. 克隆本仓库：
```bash
git clone https://github.com/September-meteor/ShizukuRunner.git ShizukuRunner
cd ShizukuRunner
```

3. 设置环境变量：
```bash
nano gradle.properties
```

你会在文件末尾看到：
```bash
# Termux Android编译环境配置（默认注释，需要时取消）
# android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/opt/aapt2/aapt2
# org.gradle.java.home=/data/data/com.termux/files/usr/opt/openjdk-17

```

请移除最后两行的“# ”以使变量生效，使其最终呈现为：
```bash
# Termux Android编译环境配置（默认注释，需要时取消）
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/opt/aapt2/aapt2
org.gradle.java.home=/data/data/com.termux/files/usr/opt/openjdk-17

```

使用快捷键 “Ctrl + S”和“Ctrl + X”保存并退出。

4. 安装Android SDK：
请依次在 Termux 中执行以下命令：
```bash
# 创建 SDK 目录
mkdir -p ~/android-sdk
cd ~/android-sdk

# 下载命令行工具（若链接失效请前往 Google Android 官网获取最新 commandlinetools 下载链接，并替换以下命令中的相关链接和压缩包名）
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
# 解压
unzip commandlinetools-linux-11076708_latest.zip
rm commandlinetools-linux-11076708_latest.zip
# 按 Android 标准目录结构调整
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# 设置环境变量
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
# 如果下面这个步骤不生效，可以用 exit 退出 Termux 再重进
source ~/.bashrc

# 接受许可证
yes | sdkmanager --licenses
# 安装平台工具（包含 adb 等）和编译工具
sdkmanager "platform-tools" "platforms;android-32" "build-tools;32.0.0"

# 创建 local.properties 文件
cd ~/ShizukuRunner
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

5. 编译
```bash
./gradlew :app:assembleRelease && ls -lh app/build/outputs/apk/release/app-release-unsigned.apk
```

注意，第一条命令出现一些提醒是正常的，这不影响应用编译打包和正常功能，只要最终输出了 60k 左右的安装包，可以认为我们大致成功了。

6. apk安装

```bash
cp app/build/outputs/apk/release/app-release-unsigned.apk /sdcard/Download/ShizukuRunner.apk
```

打开你的文件管理器，打开下载（Downloads）文件夹，点击这个新出现的 ShizukuRunner.apk 安装包，即可唤起手机的包管理器自动安装。

---

## 五、使用方法

与原版完全一致：

<div align="center">
  <img width="240" height="101.111" alt="Image" src="https://github.com/user-attachments/assets/d69ed3fa-0a25-42ac-bad1-29eefd198789" />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <img width="109.286" height="99.857" alt="Image" src="https://github.com/user-attachments/assets/b68a60e8-e2f7-4bd2-a4a6-4fe9dcf01bf7" />
</div>

1. **确保你的 Shizuku 处于激活状态。**

2. 点击 ShizukuRunner 的应用图标，此时会弹出“要允许 ShizukuRunner 使用 Shizuku 吗？”的弹窗，请选择“始终允许”。

3. 你会看到一个使用帮助弹窗，选择右下角的“OK”或者选择左下角的“设置”打开设置界面：
  - “不在后台显示”，默认勾选。勾选它时，ShizukuRunner 将不会出现在你的后台中。如果你离开前台，它就会消失，下次你将需要重新点击应用图标来打开它。
  - “开启50格命令栏”，默认不勾选。勾选它时，你会发现原来的命令输入框从十个变成数十个，同时增加屏幕占用面积。
  - “English”，默认不勾选。勾选它后，应用语言将切换至英语。
  - “多行命令模式(实验)”，默认不勾选。勾选后，输入框将支持多行命令输入，这时点击回车键将换行而非直接执行命令，若想执行命令，应该点击输入框右侧的“>”。
  - “自动换行(实验)”，默认勾选。勾选后，各输入框都将支持自动换行，而非单行滚动。
  - 点击空白处你将退出设置。但是你可以长按主页上的猫猫图标来再次唤出使用帮助界面。

<div align="center">
<img width="48" height="48" alt="Image" src="https://github.com/user-attachments/assets/b56172fa-260e-421e-8a71-e82c646042c9" />
</div>

4. 点击 ShizukuRunner 界面上的“空/空”输入框或灰色加号：
  - 在弹出的弹窗内，第一个输入框输入你想命名的名称（可不填）。
  - 第二个输入框输入你想执行的命令。
  - **注意：**
    - 这个命令只能是 **Shell 命令**，因为 ShizukuRunner 运行在 adb shell 下，它默认调用的是 Android 原生自带的工具，非常精简，没有 Python 等工具。
    - **不要在命令前加 `adb shell`！** 因为它已经在 adb shell 下运行了，且由于 Android 原生没有 `adb` 命令，这会导致你的命令出错。
    - 可用命令示例：`whoami`（查看当前用户）、`wm size`（查看屏幕分辨率）等。
    - 警告：请**不要在 ShizukuRunner 中运行未知来源、结果不可控的命令！** ShizukuRunner 经 Shizuku 授权后至少会拥有 adb 权限，随意使用命令可能会破坏你的系统。
  - 如果你不是 root 用户，可以无视“将root权限降至shell”选项。
  - 点击“完成”退出编辑。

5. 这时你会发现输入框右边的灰色加号变成了运行按钮（“>”），点击它即可运行你刚刚写下的命令。长按输出结果可复制到剪切板。

6. 如果你想退出 ShizukuRunner，点击空白处，或是使用手机的返回键均可。

7. 补充说明（感谢原作者！）
  - **单击猫猫图案**：切换为一次性运行命令模式（隐藏列表，显示输入框）。  
  - **刷新 Shizuku 状态**：点击主界面标题上两个状态按钮中的任意一个即可刷新。  
  - **长按已保存的命令条目**：复制该条命令至剪贴板。  
  - **降权说明**：若 Shizuku 由 root 启动，勾选“将root权限降至shell”可仅以 shell 权限执行命令。  
  - **隐私承诺**：本应用不收集任何信息，无联网功能。  

---

## 六、许可证说明

- 由于上游项目 [WuDi-ZhanShen/ShizukuRunner](https://github.com/WuDi-ZhanShen/ShizukuRunner) 未提供 LICENSE 文件，其代码版权归原作者所有，使用请自行评估风险。
- **本仓库中由我（September-meteor）新增或修改的部分**（包括双语支持与界面优化、Bug 修复、构建与配置优化、本文档等），采用 **MIT 许可证**（详见 [LICENSE](./LICENSE) 文件）。
- 使用本仓库时，请务必：
  - **保留上游作者的版权声明**（如代码中已有）
  - 明确标注本项目为**非官方衍生版本**
  - 如用于二次分发，请**注明上游来源**

---

## 七、反馈

如果你在使用中遇到任何问题，或者有改进建议，欢迎提交 [Issue](https://github.com/September-meteor/ShizukuRunner/issues) 或 Pull Request。

感谢原作者的创意和基础工作，也希望这只可爱的小猫咪🐱能帮到更多人！🥰
