# video conference
![logo](https://github.com/ddssingsong/webrtc_android/blob/master/art/logo1.png)

## 概述

> [English Documents](<https://github.com/ddssingsong/webrtc_android/blob/master/README.md>)

基于 [webrtc](https://webrtc.googlesource.com/) 开发的一套可以进行**单路**或者**多路**语音、视频的系统。高仿微信九宫格显示，最多可支持 **9** 路视频。



通过这个项目，你可以深刻学习并理解webrtc通话的整个流程。这个项目只是作为学习使用，是一个很好的webrtc入门项目，如果你感兴趣的话，赶紧开始吧



## 服务器搭建


nodejs：https://github.com/ddssingsong/webrtc_server_node    for branch:branch_nodejs(会议)

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
  1. 各种优化，使用体验啥的
  2. 2.0.0_preview 正在开发中
  
     

## 实现效果展示

1. 单人通话

   ![process](art/image3.png)



2. 多人会话

   ![process](/art/image5.jpg)



## 更新日志

**2.0.0**

重构代码

**v1.2.0**

使用androidx

**v1.1.2** 

 新增功能：

1. 仿微信九宫格
2. 会议添加扬声器和关闭摄像头功能

**v1.1.1** 

保存代码，便于后续的开发

**v1.1.0**  

完成基本视频会议

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


加入群一起探讨webrtc，分享好的开源项目