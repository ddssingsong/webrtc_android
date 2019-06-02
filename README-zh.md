# video conference
![logo](https://github.com/ddssingsong/webrtc_android/blob/master/art/logo1.png)



## 概述

**sky p2p metting**是基于webrtc开发的一套可以进行单路或者多路语音、视频的系统。高仿微信九宫格显示，最多可支持 **9** 路视频。



**文末有Server端搭建教程！**

tips：这只是个demo，学习使用，需要产品化的朋友们请绕道。

## 服务器搭建

请看：https://blog.csdn.net/u011077027/article/details/86225524  （csdn）

## 实现功能

1. 支持一对一语音和视频
2. 支持多对多语音和视频会议
3. 灵活替换wss信令服务器和stun/turn转发穿透服务器
4. 动态权限申请
5. 模块独立，代码清晰
6. 使用最新的webrtc源码
7. 切换摄像头、免提、开启静音、监听耳机插拔、系统来电时断开、关闭视频保留声音

## 实现效果展示

1. 单人通话

   ![process](https://github.com/ddssingsong/webrtc_android/blob/master/art/image3.png)



2. 多人会话

   ![process](https://github.com/ddssingsong/webrtc_android/blob/master/art/image5.jpg)



## 更新日志

v1.1.2 

 新增功能：

1. 仿微信九宫格
2. 会议添加扬声器和关闭摄像头功能

v1.1.1 保存代码，便于后续的开发

v1.1.0  完成基本视频会议



## 实现过程探究

自定义信令

![process](https://github.com/ddssingsong/webrtc_android/blob/master/art/image2.jpg)



## 服务器搭建

服务器源码是基于skyrtc的代码进行重写实现的

源码地址：https://github.com/ddssingsong/webrtc_server.git  （github）

搭建过程请看博客

https://blog.csdn.net/u011077027/article/details/86225524  （csdn）

使用tips:

```
如果只使用移动端的话，服务器不配置https代理也是可以的哦
```





## 借鉴

1. https://github.com/zengpeace/apprtcmobile （github）

   apprtc官方demo，这个更新的官方demo还是比较新的

2. https://github.com/LingyuCoder/SkyRTC

   服务端和网页端的实现基于此



## 共同探索

QQ群名称：webrtc技术交流群

QQ群   号：601332720

加入群一起探讨webrtc，分享好的开源项目