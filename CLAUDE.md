# Android 项目编译、修错、安装、运行流程

本项目基于 Gradle + Kotlin + Android SDK，使用 API 29 及 AndroidX Compose。

## 环境路径

- Android SDK: `D:\android\sdk`
- Gradle 使用项目内 `gradlew` (v9.4.1)
- adb: `D:\android\sdk\platform-tools\adb.exe`

> **注意**: adb 在某些 Git Bash 环境中不在 PATH 中，需使用绝对路径调用。

## 编译项目

```bash
cd D:\Projects\gptImage2 && ./gradlew assembleDebug --no-daemon
```

> 如果在 Gradle daemon 模式下遇到 AAPT2 启动失败（CreateProcess error=740），加 `--no-daemon` 可绕过。

## 查看编译错误

```bash
cd D:/Projects/gptImage2 && ./gradlew assembleDebug 2>&1 | tail -80
```

重点关注 `e:` 开头的 Kotlin 编译错误和 `FAILURE` 部分。

### 常见修复模式

#### 1. Unresolved reference 'AUTO'（ImageSize 枚举变化）
`ImageSize` 还原为旧版时没有 `AUTO` 值，需要：
- 默认值从 `ImageSize.AUTO` 改为 `ImageSize.SQUARE_1024`
- 移除 `if (selectedSize == ImageSize.AUTO) null else` 的判断，直接使用 `selectedSize.apiValue`

#### 2. No parameter with name 'quality'/'outputFormat' found（Repository 函数签名变化）
还原后 `imageToImage()` 和 `textToImage()` 没有 `quality` / `outputFormat` 参数，移除调用处的这些参数即可。

#### 3. Unresolved reference 'cancelFullscreen'（ViewModel 方法缺失）
还原后 ViewModel 没有 `cancelFullscreen` 方法，改为 `exitFullscreen`。

#### 4. No parameter with name 'interactive' found（MaskCanvas 参数变化）
还原后 `MaskCanvas` 没有 `interactive` 参数，移除即可。

#### 5. AAPT2 Daemon startup failed (error 740)
Gradle daemon 模式下 AAPT2 可能因权限问题无法启动，用 `--no-daemon` 解决。

## 安装到模拟器

```bash
# adb 设备列表
/d/android/sdk/platform-tools/adb.exe devices

# 安装 APK（如果已编译）
/d/android/sdk/platform-tools/adb.exe install -r app/build/outputs/apk/debug/app-debug.apk
```

## 启动应用

```bash
/d/android/sdk/platform-tools/adb.exe shell am start -n "com.example.gptimage2/.MainActivity"
```

## 一键全流程

```bash
cd D:/Projects/gptImage2 && \
./gradlew assembleDebug --no-daemon && \
/d/android/sdk/platform-tools/adb.exe install -r app/build/outputs/apk/debug/app-debug.apk && \
/d/android/sdk/platform-tools/adb.exe shell am start -n "com.example.gptimage2/.MainActivity"
```
