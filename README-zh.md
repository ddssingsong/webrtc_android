
![logo](https://github.com/ddssingsong/webrtc_android/blob/master/art/logo1.png)

# RTC 视频通话

> [English Documents](<https://github.com/ddssingsong/webrtc_android/blob/master/README.md>)


基于 WebRTC 开发的一套可以进行**单路**或者**多路**语音、视频的系统。仿微信九宫格显示，最多可支持 **9** 路视频。

这是一个很棒的WebRTC入门项目，帮助你深刻理解webRTC通话的整个流程，仅仅作为学习使用
 

## 服务器搭建

java： https://github.com/ddssingsong/webrtc_server_java     for branch:master


如果需要详细的部署流程可参考博客

https://blog.csdn.net/u011077027/article/details/86225524  （csdn）



## 实现功能

- 已实现功能：
  1. 支持一对一语音和视频
  2. 支持多对多语音和视频会议
  3. 灵活替换wss信令服务器和stun/turn转发穿透服务器
  4. 动态权限申请
  5. 切换摄像头、免提、开启静音、关闭视频保留声音
  6. 呼叫、响铃、拨打电话的整个流程
  7. 将信令模块和UI提取出来，将核心代码封装成SDK

- 正在开发的功能：
  1. 断线重连的能力
  2. 美颜功能
  
     

## 实现效果展示

1. 单人通话

   ![process](art/image3.png)



2. 多人会话

   ![process](/art/image5.jpg)


# demo测试

apk:[Android端测试](app/release/app-release.apk)


## 借鉴

2. https://github.com/LingyuCoder/SkyRTC

   服务端和网页端的实现基于此修改
   
2. https://webrtc.org/

   webrtc网站

3. https://webrtc.googlesource.com/src/+/master/examples

   google git demo

webrtc源码编译时间：2019年4月

## License

MIT License 
Copyright (c) 2019 哒哒瑟 



## 共同探索

QQ群名称：webrtc技术交流群

QQ群   号：601332720
           619413989 （2群）

Now:请加微信号好友：ddssingsong007

email: ddssingsong@163.com