package cn.wildfirechat.avenginekit;

import android.util.Log;

import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.SendMessageCallback;

/**
 * Created by dds on 2019/7/24.
 * android_shuai@163.com
 */
public class h implements SendMessageCallback {
    h(AVEngineKit var1, MessageContent var2, String var3, Message var4, boolean var5) {
        this.e = var1;
        this.a = var2;
        this.b = var3;
        this.c = var4;
        this.d = var5;
    }

    public void onSuccess(long var1, long var3) {
        Log.d("CallRTCClient", "send message success");
        if (this.a instanceof CallStartMessageContent && AVEngineKit.a(this.e) != null && this.b.equals(AVEngineKit.CallSession.e(AVEngineKit.a(this.e)))) {
            AVEngineKit.CallSession.b(AVEngineKit.a(this.e), this.c.messageId);
        }

    }

    public void onFail(int var1) {
        if (this.d) {
            AVEngineKit.b(this.e).submit(() -> {
                if (AVEngineKit.a(this.e) != null && AVEngineKit.a(this.e).getState() != AVEngineKit.CallState.Idle) {
                    AVEngineKit.CallSession.b(AVEngineKit.a(this.e)).didError("Signal error");
                    AVEngineKit.CallSession.a(AVEngineKit.a(this.e), AVEngineKit.CallEndReason.SignalError);
                }

            });
        } else {
            try {
                ChatManager.Instance().sendMessage(this.c, new g(this));
            } catch (NotInitializedExecption var2) {
                var2.printStackTrace();
            }
        }

    }

    public void onPrepare(long var1, long var3) {
        Log.d("CallRTCClient", "send message prepared");
    }

    public void onMediaUpload(String var1) {
    }
}

