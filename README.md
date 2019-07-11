# VHYun_SDK_Android
微吼云Android SDK

集成和调用方式，参见官方文档：<http://yun.vhall.com/document/document/index>

## 版本更新纪录
### 版本 V1.8.0 更新时间2019.07.11
1. 新增录屏直播功能；
2. 发直播demo新增噪声消除开关；
3. 看直播播放器新增截图功能；
4. 聊天服务功能升级；
5. 文档模块新增是否展示上次文档功能(new VHOPS(channelId,roomId,token,loadLastDoc)):
6. 完善互动本地流渲染模式设置方式（localView.setScalingMode()）；

### 版本 V1.7.2 更新时间2019.05.24
1. 看直播播放器，新增添加水印功能；  
2. 文档模块，新增文档上传功能；
3. demo新增文档上传demo UploadDocumentActivity;  

#### 使用说明 
##### 添加水印方式：  
1. 控制台配置；  
2. VHVideoPlayerView代码设置（如果使用本地设置，后端配置将失效）：  

```Java
 	/**
     * 设置水印
     *
     * @param drawable
     */
    public void setWaterMark(Drawable drawable)
    /**
     * 设置水印的相对位置
     *
     * @param gravity Gravity.LEFT
     */
    public void setWaterMarkGravity(int gravity)

```
##### 文档上传
文档模块VHOPS新增上传接口：  
  
```Java
    /**
     * 文档上传
     * @param filePath 文件路径
     * @param rename 重命名
     * @param accessToken
     * @param callback
     */
    public static void upload(String filePath, String rename, String accessToken, final DocCallback callback)


    /**
     * 获取上传文件限制说明
     * @return
     */
    public static String getUploadConfig()
```
### 版本 V1.7.1 更新时间2019.04.26
1. 更新文档vhallops1.7.1.1.aar包，紧急修复文档加载异常问题；

### 版本 V1.7.1 更新时间2019.04.24
1. 文档演示新增绘制图形；
2. 文档功能优化；
3. 回放播放器完善缩放模式；
4. 直播相关底层优化；

### 版本 V1.7.0 更新时间：2019.03.18
1. 直播新增纯音频推流支持；
2. 互动新增1v15模式支持；

### 版本 v1.6.0 更新时间：2019.02.20
1. 新增回放水印功能
2. 新增回放Seek限制功能
3. 新增回放截图功能
4. 文档加载回调添加初始页面数据
5. 互动音频录制方案修改
6. 互动回声优化