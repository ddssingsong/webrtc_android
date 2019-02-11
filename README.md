# Webrtc的Android端多人视频实现
群名称：webrtc技术交流群

群   号：601332720

#### 一、开源借鉴

ios端的实现：https://www.jianshu.com/p/c49da1d93df4（简书）

android端的实现：https://github.com/ddssingsong/WebRtcDemo （github）

服务器端的实现：https://github.com/ddssingsong/webrtc_server.git （github）

#### 二、本demo实现的功能

1. 支持多人视频
2. 支持一对一视频和语音
3. 可配置服务器，包括wss和stun服务器
4. 移动到library，供项目调用
5. 优化界面
6. 动态权限申请
7. 分离Websocket逻辑，可替换和自定义信令

#### 三、实现效果 



#### 四、具体实现过程



#### 五、自定义信令



```json
推送格式信息
{
    "type":"1",//1 语音 2 视频
    "room":"roomId",
    "fromID":"",  // 邀请人
    "otherID":""  // 其他人的id,逗号分隔
}
====================================================================

tips:注意事项 

fromID , toID  代表用户ID
socketID ,you  代表房间里通话成员的标识Id

1. 登录
{
       "action":"user_login",
       "data":{
           "sessionID":"sessionId"
       } 

   }
处理返回结果
 {
       "action":"user_login_success",
       "data":{
           "socketID":"socketId", // socketID 代表人房间里这个人ID ,区别于userID
           "iceServers":[{
                   "urls":"",
                   "username":"trust",
                   "credential":"trust"
           }]
       }
   }
=========================================================================   
   
2. 创建房间
  {
       "action":"room_create",
       "data":{
           "ids":"100001,100000",//通话被邀请人用户ID
           "type":1//类型 1=语音，2=视频
       } 
   }
处理返回结果
 {
       "action":"room_create_success",
       "data":{
           "room":"sexsaa23s"
       } 

   }
==========================================================================
3. 加入房间
   {
       "action":"room_join",
       "data":{
           "room":"5tkls0012ld"
       } 
   }
 处理返回结果（下行数据）
   {
       "action":"room_join_success",
       "data":{
           "connections":["123","345"],
           "you":"myId"
       } 

   }

 有人进入房间（下行数据）
  {
       "action":"new_user_join",
       "data":{
           "socketID":"socketId"
       } 

   }

============================================================================
4. 发送回应信息
{
       "action":"user_ack",
       "data":{
           "toID":"100002"
       } 
   }
对方收到回应
{
       "action":"user_ack",
       "data":{
           "fromID":"fromId"
       }
   }



5. 邀请通话
  {
       "action":"user_invite",
       "data":{
           "toID":"100001"
       } 
   }
对方收到邀请 并开始响铃
  {
       "action":"user_invite",
       "data":{
           "socketID":"32434344",
           "room":"room123"
       } 
   }
=================================================================================
6. 发送offer 
 {
       "action":"user_sendOffer",
       "data":{
           "sdp":"sdp",
           "socketID":"socketId"
          }
       } 
   }

对方收到数据

  {
       "action":"user_sendOffer",
       "data":{
           "sdp":"sdp",
           "socketID":"other socketId"
          }
       } 
   }
7. 发送answer
   {
       "action":"user_sendAnswer",
       "data":{
           "sdp":"sdp",
           "socketID":"other socketid"
       } 
   }
对方收到的数据
  {
       "action":"user_sendAnswer",
       "data":{
           "sdp":"sdp",
           "socketID":"other socketId"
          }
       } 
   }

8.发送IceCandidate
 {
       "action":"user_sendIceCandidate",
       "data":{
           "socketID":"socketId",
           "id":"sdpMid",
           "label":"sdpMLineIndex信息",
           "candidate":"sdp信息"
       } 
   }
对方收到的数据
 {
       "action":"user_sendIceCandidate",
       "data":{
           "id":"sdpMid",
           "socketID":"socketId",
           "label":"sdpMLineIndex信息",
           "candidate":"sdp信息"
       } 
   }
======================================================================================
 8.有人退出房间
{
       "action":"user_leave",
       "data":{
           "socketID":"socketId"
       } 

   }

9.拒绝接受
    {
       "action":"room_refuse",
       "data":{
            "room":"roomid",
            "toID":"toID"
       } 

   }
  {
       "action":"decline_reason",
       "data":{
           "reason":"cancel"  //refuse=对方挂断，busy = 对方正在通话中 cancel=未接通时，对方已取消
       } 

   }
   
   

10.发生错误
 {
       "action":"show_error",
       "data":{
           "errMessage":"message"
       } 

   }

```




















