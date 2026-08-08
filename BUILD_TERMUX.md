# 📱 Termux 编译环境配置教程

## 环境要求
- Android SDK (API 23+)
- JDK 8 或更高版本
- Termux 应用（从 F-Droid 或 GitHub 下载，**不要从 Google Play 下载**）

---

## 步骤 1：安装基础工具

```bash
pkg install git openjdk-17 aapt2 wget unzip -y
```

---

## 步骤 2：创建 SDK 目录

```bash
mkdir -p ~/android-sdk
cd ~/android-sdk
```

---

## 步骤 3：下载 Android 命令行工具

```bash
# 下载（链接可能过期，如失效请前往 Google 官网获取最新链接）
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

# 验证下载是否完整
ls -lh commandlinetools-linux-11076708_latest.zip
```

---

## 步骤 4：解压并整理目录结构

```bash
# 解压
unzip commandlinetools-linux-11076708_latest.zip

# 删除压缩包
rm commandlinetools-linux-11076708_latest.zip

# 按 Android 标准目录结构调整
mkdir -p cmdline-tools/latest
mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# 验证目录结构
ls cmdline-tools/latest/
# 应看到：bin  lib  NOTICE.txt  source.properties
```

---

## 步骤 5：配置环境变量

```bash
# 添加到 ~/.bashrc
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# 立即生效（若不生效可重启 Termux）
source ~/.bashrc

# 验证环境变量
echo $ANDROID_HOME
# 应输出：/data/data/com.termux/files/home/android-sdk
```

---

## 步骤 6：接受 Android SDK 许可证

```bash
yes | sdkmanager --licenses
```

---

## 步骤 7：安装 SDK 组件

```bash
# 安装平台工具、Android 平台和构建工具
sdkmanager "platform-tools" "platforms;android-32" "build-tools;32.0.0"

# 验证安装
ls platform-tools platforms build-tools
# 期望输出
# build-tools:
# 32.0.0
#
# platform-tools:
# NOTICE.txt ……
# 
# platforms:
# android-32
```