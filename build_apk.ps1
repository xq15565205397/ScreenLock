
# APK构建脚本
Write-Host "=== 定时锁屏APK构建向导 ===" -ForegroundColor Cyan

Write-Host "`n请按以下步骤操作：" -ForegroundColor Yellow

Write-Host "`n1. 安装 Android Studio"
Write-Host "   下载地址: https://developer.android.com/studio"
Write-Host "   或者使用命令: winget install Google.AndroidStudio"

Write-Host "`n2. 安装完成后，打开 Android Studio 并:"
Write-Host "   - 等待首次启动配置完成"
Write-Host "   - 打开此项目目录: $PWD"
Write-Host "   - 等待 Gradle 同步完成"

Write-Host "`n3. 在 Android Studio 中构建 APK:"
Write-Host "   - 点击菜单: Build -&gt; Build Bundle(s) / APK(s) -&gt; Build APK(s)"
Write-Host "   - APK 位置: app/build/outputs/apk/debug/app-debug.apk"

Write-Host "`n或者使用命令行（需要先配置好 Android SDK）:"
Write-Host "   .\gradlew.bat assembleDebug"
Write-Host "`n"
