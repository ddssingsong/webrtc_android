![logo](art/logo1.png)

# Sky p2p metting (p2p视频会议)

> [中文文档](<https://github.com/ddssingsong/webrtc_android/blob/master/README-zh.md>)



A set of voice and video systems based on  [webrtc](https://webrtc.googlesource.com/) can be developed for single or multiple channels.



Through this project, you can get a clearer understanding of the whole call process of webrtc. This project is just for learning webrtc. If you feel it meets your needs, just download the source code and start your performance.



## Server 

You can find the server-side code from here. 

NodeJs    https://github.com/ddssingsong/webrtc_server_node   for branch:branch_nodejs

java      https://github.com/ddssingsong/webrtc_server_java         for branch:master



## Features

- Completed
  1. Support one-to-one voice and video
  2. Support for many-to-many voice and video conferencing
  3. Flexible configuration of signaling server and stun/turn forwarding penetration server
  4. Dynamic permission application
  5. Encapsulating core code into SDK
  6. The whole process of calling, ringing and dialing

- In progress
  1. Various optimization
  2. 2.0.0_preview in dev



## Effect

1. Single call

   ![process](art/image3.png)



2. Multi-person conversation

   ![process](art/image5.jpg)





## CHANGELOG

**2.0.0**

Various optimization

**v1.2.0:** 

 move to androidx

**v1.1.2:**   

release base on android support

**v1.1.1:**   

This version has implemented the basic functions.

**v1.1.0**  

Complete Basic Video Conferences


# Android demo

apk for master : [AndroidDemo.apk](app/release/app-release.apk)

apk for branch_meeting : [AndroidDemo.apk](https://github.com/ddssingsong/webrtc_android/blob/branch_meeting/app/release/app-release.apk)



## Links

1. https://github.com/LingyuCoder/SkyRTC

   server side,base on this

2. https://webrtc.org/

   webrtc网站

3. https://webrtc.googlesource.com/src/+/master/examples

   google git demo

   

## License

MIT License 
Copyright (c) 2019 哒哒瑟 



## QQ

QQ Group：webrtc技术交流群

QQ 群   号：601332720
            619413989  （2群）


加入群一起探讨webrtc，分享好的开源项目







