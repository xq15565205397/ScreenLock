
# 定时锁屏 App

适配vivo最新安卓系统版本的定时锁屏应用。

## 功能特性

- 可设置1-180分钟的锁屏倒计时
- 实时显示剩余时间
- 使用设备管理器权限安全锁屏
- 简洁直观的用户界面

## 构建APK

### 前置条件

1. 安装 [Android Studio](https://developer.android.com/studio)
2. 安装 JDK 17 或更高版本

### 构建步骤

1. 使用 Android Studio 打开此项目
2. 等待 Gradle 同步完成
3. 点击菜单 `Build` -&gt; `Build Bundle(s) / APK(s)` -&gt; `Build APK(s)`
4. 构建完成后，APK 文件位于：`app/build/outputs/apk/debug/app-debug.apk`

### 发布版本构建

如需构建发布版本APK：

1. 在 `app/build.gradle` 中配置签名
2. 点击 `Build` -&gt; `Generate Signed Bundle / APK`
3. 选择 `APK`，按照向导完成签名配置

## 使用说明

1. 安装APK到vivo手机
2. 首次打开时，点击"授予权限"按钮
3. 在系统设置中启用设备管理器权限
4. 选择想要的锁屏时间（1-180分钟）
5. 点击"开始计时"按钮
6. 倒计时结束后将自动锁屏

## 注意事项

- 必须授予设备管理器权限才能使用锁屏功能
- 如需卸载应用，需先在系统设置中取消设备管理器权限
