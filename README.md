# meeting  
![logo](https://github.com/ddssingsong/webrtc_android/blob/master/image/logo.png)



## 概述

meeting 是基于webrtc开发的一套可以进行单路或者多路语音、视频的系统，这里仅仅提供demo，还有更多未知的东西等着大家来探索



## 实现功能

1. 支持一对一语音和视频
2. 支持多对多语音和视频会议
3. 灵活替换wss信令服务器和stun/turn转发穿透服务器
4. 动态权限申请
5. 模块独立，代码清晰



## 服务器搭建

服务器源码是基于skyrtc的代码进行重写实现的

源码地址：https://github.com/ddssingsong/webrtc_server.git  （github）

搭建过程请看博客

https://blog.csdn.net/u011077027/article/details/86225524  （csdn）

使用tips:

```
如果只使用移动端的话，服务器不配置https代理也是可以的哦
```





## 实现效果展示

1. 单人通话

![process](https://github.com/ddssingsong/webrtc_android/blob/master/image/image3.png)



2. 多人会话

   ![process](https://github.com/ddssingsong/webrtc_android/blob/master/image/image4.png)



## 实现过程探究

1. webrtc官方demo流程

![process](https://github.com/ddssingsong/webrtc_android/blob/master/image/image1.png)

2. 本demo流程

![process](https://github.com/ddssingsong/webrtc_android/blob/master/image/image2.png)

2. 自定义信令



#### 



## 借鉴

1. https://github.com/zengpeace/apprtcmobile （github）

   apprtc官方demo，这个更新的官方demo还是比较新的

2. https://github.com/LingyuCoder/SkyRTC

   服务端和网页端的实现基于此



## 共同探索

群名称：webrtc技术交流群

群   号：601332720

加入群一起探讨webrtc，分享好的开源项目

















