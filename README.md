# GPT Image 2 Android Drawing App

基于 Jetpack Compose 的 Android 图片生成应用，调用 Dreamfield 提供的 OpenAI 兼容 API（模型 `gpt-image-2`），支持三种生图方式。

## 功能概览

| 功能 | 说明 |
|------|------|
| 文生图 | 输入提示词，生成全新图片 |
| 图生图 | 上传参考图片 + 提示词，基于参考图生成新图片 |
| 局部重绘 | 上传原图 + 手绘蒙版 + 提示词，仅重绘蒙版覆盖区域 |

## API 信息

- **Base URL**: `https://www.dreamfield.top/v1`
- **模型**: `gpt-image-2`
- **认证**: Bearer Token（在设置页面配置 API Key）
- **支持的尺寸**: `1024x1024`、`1792x1024`、`1024x1792`

---

## 三种生图方式的请求与实现

### 1. 文生图（Text-to-Image）

**原理**: 用户输入提示词，API 根据文本描述从零生成图片。

**API 请求**:

```
POST https://www.dreamfield.top/v1/images/generations
Content-Type: application/json
Authorization: Bearer <API_KEY>

{
  "model": "gpt-image-2",
  "prompt": "用户输入的提示词",
  "size": "1792x1024",
  "n": 1,
  "response_format": "b64_json"
}
```

**响应格式**:

```json
{
  "created": 1700000000,
  "data": [
    {
      "b64_json": "<Base64 编码的 PNG 图片数据>",
      "revised_prompt": "模型可能修改后的提示词"
    }
  ]
}
```

**实现流程**:

1. 用户在「文生图」页面输入提示词、选择尺寸
2. `HomeViewModel.generateImage()` 调用 `ImageRepository.textToImage(prompt, size)`
3. Repository 构建 JSON 请求体，通过 OkHttp 发送 POST 请求
4. 解析响应中的 `b64_json` 字段，Base64 解码为 Bitmap，保存为 PNG 文件到 `filesDir/generated/` 目录
5. UI 显示生成的图片缩略图，点击可查看大图

**关键代码**: `ImageRepository.textToImage()` → `HomeViewModel.generateImage()`

---

### 2. 图生图（Image-to-Image）

**原理**: 用户上传一张或多张参考图片，配合提示词，API 基于参考图片的风格/内容生成新图片。

**API 请求**:

```
POST https://www.dreamfield.top/v1/images/edits
Authorization: Bearer <API_KEY>
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="model"

gpt-image-2
--boundary
Content-Disposition: form-data; name="prompt"

用户输入的提示词
--boundary
Content-Disposition: form-data; name="size"

1792x1024
--boundary
Content-Disposition: form-data; name="n"

1
--boundary
Content-Disposition: form-data; name="response_format"

b64_json
--boundary
Content-Disposition: form-data; name="image[]"; filename="ref_image_0.png"
Content-Type: image/png

<二进制图片数据>
--boundary
Content-Disposition: form-data; name="image[]"; filename="ref_image_1.png"
Content-Type: image/png

<二进制图片数据>
--boundary--
```

> 支持上传多张参考图片，每张作为 `image[]` 字段发送。

**实现流程**:

1. 用户在「图生图」页面通过系统图片选择器选取一张或多张参考图片
2. `ImageEditViewModel.generateImage()` 将选中的 Uri 复制到缓存目录作为临时文件
3. 调用 `ImageRepository.imageToImage(prompt, size, imageFiles)`
4. Repository 使用 `MultipartBody.Builder` 构建 multipart/form-data 请求体，每张图片作为 `image[]` 字段上传
5. 响应处理与文生图相同：解码 `b64_json`，保存文件，显示结果

**关键代码**: `ImageRepository.imageToImage()` → `ImageEditViewModel.generateImage()`

---

### 3. 局部重绘（Inpainting / Mask Editing）

**原理**: 用户上传原图，在画布上用手指涂抹需要重绘的区域（生成蒙版），API 只修改蒙版覆盖的区域，保留其余部分不变。

**API 请求**:

```
POST https://www.dreamfield.top/v1/images/edits
Authorization: Bearer <API_KEY>
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="model"

gpt-image-2
--boundary
Content-Disposition: form-data; name="prompt"

仅描述重绘区域想要的内容
--boundary
Content-Disposition: form-data; name="size"

1792x1024
--boundary
Content-Disposition: form-data; name="n"

1
--boundary
Content-Disposition: form-data; name="response_format"

b64_json
--boundary
Content-Disposition: form-data; name="image[]"; filename="mask_source.png"
Content-Type: image/png

<原图二进制数据>
--boundary
Content-Disposition: form-data; name="mask"; filename="mask.png"
Content-Type: image/png

<蒙版二进制数据>
--boundary--
```

> 与图生图使用同一 `/images/edits` 端点，额外附带 `mask` 字段。

**蒙版说明**:
- 蒙版为与原图同尺寸的 PNG 图片
- 白色像素（ARGB `#FFFFFFFF`）= 需要重绘的区域
- 透明像素（ARGB `#00000000`）= 保持不变的区域
- 用户通过画笔（涂抹）和橡皮擦（擦除）模式绘制蒙版

**实现流程**:

1. 用户在「局部重绘」页面选择原图，图片加载到 Canvas 中显示
2. 创建与原图同尺寸的空蒙版 Bitmap（全透明）
3. 用户在 `MaskCanvas` 上手指涂抹，`MaskEditorViewModel.onDraw()` / `onDrawLine()` 调用 `BitmapUtils.drawOnMask()` 在蒙版上绘制白色圆形/线条
4. 支持切换画笔/橡皮擦模式、调整画笔大小、重置蒙版
5. 点击生成时，`MaskEditorViewModel.generateInpainting()` 将蒙版 Bitmap 保存为临时 PNG 文件
6. 调用 `ImageRepository.imageToImage(prompt, size, imageFiles, maskFile)`，其中 `imageFiles` 包含原图，`maskFile` 为蒙版
7. Repository 在 multipart 请求中附带 `mask` 字段上传
8. 响应处理与其他方式相同

**关键代码**: `MaskCanvas`（绘制交互）→ `MaskEditorViewModel`（蒙版管理）→ `ImageRepository.imageToImage()`（带 mask 参数）

---

## 项目结构

```
app/src/main/java/com/example/gptimage2/
├── GptImage2App.kt              # Application 类
├── MainActivity.kt              # 入口 Activity
├── data/
│   ├── local/
│   │   ├── ApiKeyStore.kt       # API Key 加密存储（EncryptedSharedPreferences）
│   │   └── ImageStorage.kt      # 生成图片文件管理
│   ├── remote/
│   │   ├── api/ImageApiService.kt
│   │   ├── dto/                 # 请求/响应数据类
│   │   └── interceptor/AuthInterceptor.kt
│   └── repository/
│       └── ImageRepository.kt   # 核心网络请求（OkHttp 直连）
├── di/AppModule.kt              # 手动依赖注入
├── domain/model/                # 领域模型
├── ui/
│   ├── components/              # 通用 UI 组件
│   ├── navigation/AppNavigation.kt
│   └── screens/
│       ├── home/                # 文生图
│       ├── imageedit/           # 图生图
│       ├── mask/                # 局部重绘（含 MaskCanvas 画布）
│       ├── gallery/             # 图片画廊 + 详情
│       └── settings/            # API Key 设置
└── util/
    ├── ApiErrorParser.kt        # 错误信息解析
    └── BitmapUtils.kt           # Bitmap 工具（Base64 解码、蒙版绘制）
```

## 技术栈

- **UI**: Jetpack Compose + Material 3
- **网络**: OkHttp（直接构建请求）+ Moshi（JSON 解析）
- **图片加载**: Coil
- **存储**: EncryptedSharedPreferences（API Key）、内部存储（生成图片）
- **架构**: MVVM（ViewModel + StateFlow）
- **依赖注入**: 手动单例（AppModule）
