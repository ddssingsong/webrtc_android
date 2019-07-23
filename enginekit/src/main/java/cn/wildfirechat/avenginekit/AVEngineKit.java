//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.wildfirechat.avenginekit;

import android.content.Context;
import android.os.AsyncTask;
import android.telecom.VideoProfile;
import android.util.Log;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CameraVideoCapturer.CameraEventsHandler;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.RendererCommon.RendererEvents;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.wildfirechat.avenginekit.a.b;
import cn.wildfirechat.avenginekit.a.d;
import cn.wildfirechat.avenginekit.a.h;
import cn.wildfirechat.avenginekit.a.j;
import cn.wildfirechat.avenginekit.w.c;
import cn.wildfirechat.client.NotInitializedExecption;
import cn.wildfirechat.message.CallStartMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.message.MessageContent;
import cn.wildfirechat.message.core.MessageDirection;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.Conversation.ConversationType;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.OnReceiveMessageListener;

public class AVEngineKit implements OnReceiveMessageListener {
    private static final String a = "CallRTCClient";
    private static AVEngineKit b;
    private c c = new f(this);
    private VideoCapturer d;
    private final ExecutorService e = Executors.newSingleThreadExecutor();
    private AVEngineKit.CallSession f;
    private AVEngineKit.AVEngineCallback g;
    private Context h;
    private cn.wildfirechat.avenginekit.c i = null;
    private boolean j;
    private w k;
    private VideoTrack l;
    private VideoTrack m;
    private List<IceServer> n = new ArrayList();
    private int o = 30;
    private boolean p = false;

    private static void b(JSONObject var0, String var1, Object var2) {
        try {
            var0.put(var1, var2);
        } catch (JSONException var3) {
            throw new RuntimeException(var3);
        }
    }

    private AVEngineKit.CallSession a(String var1, boolean var2, String var3) {
        AVEngineKit.CallSession var4;
        AVEngineKit.CallSession var10000 = var4 = new AVEngineKit.CallSession;
        var4.<init>((f)null);
        var4.c = var1;
        var4.a = var3;
        var10000.d = var2;
        return var10000;
    }

    private AVEngineKit() {
    }

    public static AVEngineKit Instance() {
        AVEngineKit var0;
        if ((var0 = b) != null) {
            return var0;
        } else {
            throw new NotInitializedExecption();
        }
    }

    public static void init(Context var0, AVEngineKit.AVEngineCallback var1) {
        if (b == null) {
            b = new AVEngineKit();
            AVEngineKit var10000 = b;
            var10000.h = var0;
            var10000.g = var1;

            try {
                ChatManager.Instance().registerMessageContent(b.class);
                ChatManager.Instance().registerMessageContent(d.class);
                ChatManager.Instance().registerMessageContent(f.class);
                ChatManager.Instance().registerMessageContent(j.class);
                ChatManager.Instance().registerMessageContent(h.class);
            } catch (NotInitializedExecption var2) {
                var2.printStackTrace();
            }

            ChatManager.Instance().addOnReceiveMessageListener(b);
        }
    }

    private void a(MessageContent var1, String var2, boolean var3) {
        Message var4;
        Message var10000 = var4 = new Message;
        var4.<init>();
        var10000.content = var1;
        var10000.conversation = new Conversation(ConversationType.Single, var2);

        try {
            ChatManager.Instance().sendMessage(var4, new cn.wildfirechat.avenginekit.h(this, var1, var2, var4, var3));
        } catch (NotInitializedExecption var5) {
            var5.printStackTrace();
        }

    }

    private void b() {
        if (this.k == null) {
            this.k = new w();
            VideoProfile var1 = VideoProfile.getVideoProfile(this.o, this.p);
            this.k.a(this.h, this.c, this.f.d ^ true, var1.width, var1.height, var1.fps, var1.bitrate);
        }

    }

    private void a(String var1, String var2) {
        this.a(new f(var1), var2, false);
    }

    private void a(byte[] var1) {
        String var2;
        var2 = new String.<init>(var1);
        JSONObject var4;
        JSONObject var10000 = var4 = new JSONObject;
        var10000.<init>(var2);
        if ((var2 = var10000.optString("type")).equals("candidate")) {
            this.c(this.a(var4));
        } else if (var2.equals("remove-candidates")) {
            JSONArray var5;
            IceCandidate[] var6 = new IceCandidate[(var5 = var4.getJSONArray("candidates")).length()];

            for(int var3 = 0; var3 < var5.length(); ++var3) {
                var6[var3] = this.a(var5.getJSONObject(var3));
            }

            this.b(var6);
        } else if (var2.equals("answer")) {
            if (this.j) {
                this.b(new SessionDescription(Type.fromCanonicalForm(var2), var4.getString("sdp")));
            }
        } else if (var2.equals("offer") && !this.j) {
            this.b(new SessionDescription(Type.fromCanonicalForm(var2), var4.getString("sdp")));
        }

    }

    private void b(SessionDescription var1) {
        this.e.submit(() -> {
            w var2;
            if ((var2 = this.k) == null) {
                Log.e("CallRTCClient", "Received remote SDP for non-initilized peer connection.");
            } else {
                var2.a(var1);
                if (!this.j) {
                    this.k.c();
                }

            }
        });
    }

    private void c(IceCandidate var1) {
        this.e.submit(() -> {
            w var2;
            if ((var2 = this.k) == null) {
                Log.e("CallRTCClient", "Received ICE candidate for a non-initialized peer connection.");
            } else {
                var2.a(var1);
            }
        });
    }

    private void b(IceCandidate[] var1) {
        this.e.submit(() -> {
            w var2;
            if ((var2 = this.k) == null) {
                Log.e("CallRTCClient", "Received ICE candidate removals for a non-initialized peer connection.");
            } else {
                var2.a(var1);
            }
        });
    }

    private void g() {
        w var1;
        if ((var1 = this.k) != null) {
            var1.b();
            this.k = null;
        }

        Log.d("CallRTCClient", "Stopping capture.");
        VideoCapturer var3;
        if ((var3 = this.d) != null) {
            AVEngineKit var10000;
            AVEngineKit var10001;
            try {
                var10000 = this;
                var10001 = this;
                var3.stopCapture();
            } catch (InterruptedException var2) {
                throw new RuntimeException(var2);
            }

            var10001.d.dispose();
            var10000.d = null;
        }

        this.l = null;
        this.m = null;
        if (this.i != null) {
            (new AVEngineKit.a((Runnable)null, () -> {
                this.i.c();
                this.i = null;
            })).execute(new Void[0]);
        }

    }

    private void a(boolean var1) {
        this.j = var1;
        this.f.a(AVEngineKit.CallState.Connecting);
        this.b();
        (new AVEngineKit.a((Runnable)null, () -> {
            this.i = cn.wildfirechat.avenginekit.c.a(this.h);
            this.i.a((var1, var2) -> {
                this.b(var1, var2);
            });
        })).execute(new Void[0]);
        if (this.d == null && !this.f.isAudioOnly()) {
            this.d = this.c();
            VideoCapturer var10000 = this.d;
        }

        VideoCapturer var2;
        if (this.f.isAudioOnly() && (var2 = this.d) != null) {
            AVEngineKit var10001;
            AVEngineKit var10002;
            AVEngineKit var4;
            try {
                var4 = this;
                var10001 = this;
                var10002 = this;
                var2.stopCapture();
            } catch (InterruptedException var3) {
                throw new RuntimeException(var3);
            }

            var10002.d.dispose();
            var10001.d = null;
            var4.k.h();
        }

        this.k.a(this.d, this.n);
        if (var1) {
            this.k.d();
        }

    }

    private void b(cn.wildfirechat.avenginekit.c.a var1, Set<cn.wildfirechat.avenginekit.c.a> var2) {
        Log.d("ddd", "onAudioManagerDevicesChanged: " + var2 + ", selected: " + var1);
    }

    private VideoCapturer c() {
        Logging.d("CallRTCClient", "Creating capturer using camera1 API.");
        VideoCapturer var1;
        if ((var1 = this.a((CameraEnumerator)(new Camera1Enumerator(this.a())))) == null) {
            this.e.submit(() -> {
                AVEngineKit.CallSession var1;
                if ((var1 = this.f) != null) {
                    var1.e.didError("Failure open camera");
                    this.f.a(AVEngineKit.CallEndReason.OpenCameraFailure);
                }

            });
            return null;
        } else {
            return var1;
        }
    }

    private boolean a() {
        return false;
    }

    private VideoCapturer a(CameraEnumerator var1) {
        String[] var5;
        String[] var10000 = var5 = var1.getDeviceNames();
        Logging.d("CallRTCClient", "Looking for front facing cameras.");
        int var2 = var10000.length;

        int var3;
        String var4;
        CameraVideoCapturer var6;
        for(var3 = 0; var3 < var2; ++var3) {
            if (var1.isFrontFacing(var4 = var5[var3])) {
                Logging.d("CallRTCClient", "Creating front facing camera capturer.");
                if ((var6 = var1.createCapturer(var4, (CameraEventsHandler)null)) != null) {
                    return var6;
                }
            }
        }

        Logging.d("CallRTCClient", "Looking for other cameras.");
        var2 = var5.length;

        for(var3 = 0; var3 < var2; ++var3) {
            if (!var1.isFrontFacing(var4 = var5[var3])) {
                Logging.d("CallRTCClient", "Creating other camera capturer.");
                if ((var6 = var1.createCapturer(var4, (CameraEventsHandler)null)) != null) {
                    return var6;
                }
            }
        }

        return null;
    }

    private static JSONObject d(IceCandidate var0) {
        JSONObject var1;
        JSONObject var10000 = var1 = new JSONObject;
        var1.<init>();
        b(var1, "label", var0.sdpMLineIndex);
        b(var1, "id", var0.sdpMid);
        b(var10000, "candidate", var0.sdp);
        return var10000;
    }

    public AVEngineKit.CallSession getCurrentSession() {
        return this.f;
    }

    public void onReceiveMessage(List<Message> var1, boolean var2) {
        Iterator var3 = var1.iterator();

        while(var3.hasNext()) {
            this.onReceiveCallMessage((Message)var3.next());
        }

    }

    public AVEngineKit.CallSession startCall(String var1, boolean var2, AVEngineKit.CallSessionCallback var3) {
        CountDownLatch var4;
        CountDownLatch var10000 = var4 = new CountDownLatch;
        var4.<init>(1);
        Future var7 = this.e.submit(() -> {
            Throwable var10000;
            label246: {
                AVEngineKit.CallSession var36;
                AVEngineKit.CallSession var5;
                boolean var10001;
                try {
                    (var5 = this.a(var1, var2, var1 + System.currentTimeMillis())).e = var3;
                    var36 = this.f;
                } catch (Throwable var35) {
                    var10000 = var35;
                    var10001 = false;
                    break label246;
                }

                CountDownLatch var39;
                if (var36 != null) {
                    AVEngineKit.CallState var37;
                    AVEngineKit.CallState var38;
                    try {
                        var37 = this.f.b;
                        var38 = AVEngineKit.CallState.Idle;
                    } catch (Throwable var34) {
                        var10000 = var34;
                        var10001 = false;
                        break label246;
                    }

                    if (var37 != var38) {
                        try {
                            var36 = var5;
                            var39 = var4;
                            var5.e.didCallEndWithReason(AVEngineKit.CallEndReason.Busy);
                        } catch (Throwable var31) {
                            var10000 = var31;
                            var10001 = false;
                            break label246;
                        }

                        var39.countDown();
                        return var36;
                    }
                }

                AVEngineKit var10002;
                CallStartMessageContent var10003;
                String var10004;
                try {
                    var36 = var5;
                    var39 = var4;
                    var10002 = this;
                    this.f = var5;
                    this.f.a(AVEngineKit.CallState.Outgoing);
                    var10004 = var1;
                    var10003 = new CallStartMessageContent(var5.a, var1, var2);
                } catch (Throwable var33) {
                    var10000 = var33;
                    var10001 = false;
                    break label246;
                }

                boolean var10005 = true;

                try {
                    var10002.a(var10003, var10004, var10005);
                } catch (Throwable var32) {
                    var10000 = var32;
                    var10001 = false;
                    break label246;
                }

                var39.countDown();
                return var36;
            }

            var4.countDown();
            throw var10000;
        });

        try {
            var10000.await();
            return (AVEngineKit.CallSession)var7.get();
        } catch (InterruptedException var5) {
            var5.printStackTrace();
        } catch (ExecutionException var6) {
            var6.printStackTrace();
        }

        return null;
    }

    public boolean onReceiveCallMessage(Message var1) {
        long var2 = 0L;

        long var4;
        try {
            var4 = ChatManager.Instance().getServerDeltaTime();
        } catch (NotInitializedExecption var6) {
            var6.printStackTrace();
            var4 = var2;
        }

        MessageContent var8;
        if (!((var8 = var1.content) instanceof j) && !(var8 instanceof CallStartMessageContent) && !(var8 instanceof b) && !(var8 instanceof f) && !(var8 instanceof h)) {
            return false;
        } else {
            if (System.currentTimeMillis() - (var1.serverTime - var4) < 90000L && (var1.direction == MessageDirection.Receive || var1.content instanceof b)) {
                this.e.submit(() -> {
                    MessageContent var2;
                    if ((var2 = var1.content) instanceof j) {
                        AVEngineKit.CallSession var10;
                        if ((var10 = this.f) != null && var10.b != AVEngineKit.CallState.Idle) {
                            j var11 = (j)var1.content;
                            if (var1.sender.equals(this.f.c) && var11.a().equals(this.f.a)) {
                                if (this.f.b == AVEngineKit.CallState.Connected || this.f.b == AVEngineKit.CallState.Connecting) {
                                    try {
                                        this.a(var11.b());
                                    } catch (JSONException var4) {
                                        var4.printStackTrace();
                                    }
                                }

                            } else {
                                this.a(var11.a(), var1.sender);
                            }
                        }
                    } else {
                        AVEngineKit.CallSession var3;
                        if (var2 instanceof CallStartMessageContent) {
                            CallStartMessageContent var9 = (CallStartMessageContent)var2;
                            if ((var3 = this.f) != null && var3.b != AVEngineKit.CallState.Idle) {
                                this.a(var9.getCallId(), var1.sender);
                            } else {
                                AVEngineKit var10000 = this;
                                AVEngineKit var10001 = this;
                                AVEngineKit.CallSession var5;
                                AVEngineKit.CallSession var10002 = var5 = this.a(var1.sender, var9.isAudioOnly(), var9.getCallId());
                                var5.a(AVEngineKit.CallState.Incoming);
                                var10002.m = var1.messageId;
                                var10001.f = var10002;
                                var10000.g.onReceiveCall(var5);
                            }
                        } else {
                            if (var2 instanceof b) {
                                b var8 = (b)var2;
                                if ((var3 = this.f) != null && var3.b != AVEngineKit.CallState.Idle) {
                                    if (!var1.sender.equals(this.f.c) || !var8.a().equals(this.f.a)) {
                                        if (var1.direction == MessageDirection.Receive) {
                                            this.a(var8.a(), var1.sender);
                                        } else if (this.f.b == AVEngineKit.CallState.Incoming) {
                                            this.f.a(AVEngineKit.CallEndReason.AcceptByOtherClient);
                                        }

                                        return;
                                    }

                                    if (this.f.b == AVEngineKit.CallState.Connecting || this.f.b == AVEngineKit.CallState.Connected) {
                                        return;
                                    }

                                    if (this.f.b != AVEngineKit.CallState.Outgoing) {
                                        this.a(var8.a(), var1.sender);
                                        return;
                                    }

                                    this.f.a(AVEngineKit.CallState.Connecting);
                                    this.f.setAudioOnly(var8.b());
                                    this.a(false);
                                }
                            } else if (var2 instanceof f) {
                                f var7 = (f)var2;
                                if ((var3 = this.f) == null || var3.b == AVEngineKit.CallState.Idle || !this.f.a.equals(var7.a()) || !this.f.c.equals(var1.sender)) {
                                    return;
                                }

                                this.f.a(AVEngineKit.CallEndReason.RemoteHangup);
                            } else if (var2 instanceof h) {
                                h var6 = (h)var2;
                                if ((var3 = this.f) != null && var3.b == AVEngineKit.CallState.Connected && this.f.a.equals(var6.a()) && this.f.c.equals(var1.sender)) {
                                    this.f.setAudioOnly(var6.b());
                                }
                            }

                        }
                    }
                });
            }

            MessageContent var7;
            return (var7 = var1.content) instanceof j || var7 instanceof b || var7 instanceof f;
        }
    }

    public void startPreview() {
        if (!this.f.isAudioOnly()) {
            if (this.d == null) {
                this.d = this.c();
            }

            this.b();
            this.k.a(this.d);
        }
    }

    IceCandidate a(JSONObject var1) {
        return new IceCandidate(var1.getString("id"), var1.getInt("label"), var1.getString("candidate"));
    }

    public void addIceServer(String var1, String var2, String var3) {
        AVEngineKit var10000 = this;
        IceServer var4 = IceServer.builder(var1).setUsername(var2).setPassword(var3).createIceServer();
        var10000.n.add(var4);
    }

    public void setVideoProfile(int var1, boolean var2) {
        this.o = var1;
        this.p = var2;
    }

    private static class a extends AsyncTask<Void, Void, Void> {
        private final Runnable a;
        private final Runnable b;

        public a(Runnable var1, Runnable var2) {
            this.a = var1;
            this.b = var2;
        }

        protected void a(Void var1) {
            Runnable var2;
            if ((var2 = this.b) != null) {
                var2.run();
            }

        }

        protected Void a(Void... var1) {
            Runnable var2;
            if ((var2 = this.a) != null) {
                var2.run();
            }

            return null;
        }
    }

    public class CallSession {
        private String a;
        private AVEngineKit.CallState b;
        private String c;
        private boolean d;
        private AVEngineKit.CallSessionCallback e;
        private List<SurfaceViewRenderer> f;
        private VideoRenderer g;
        private VideoRenderer h;
        private long i;
        private long j;
        private long k;
        private Timer l;
        private long m;

        private void a(AVEngineKit.CallState var1) {
            AVEngineKit.CallState var2;
            if ((var2 = this.b) != var1) {
                this.b = var1;
                Timer var12;
                if (var1 != AVEngineKit.CallState.Incoming && var1 != AVEngineKit.CallState.Outgoing) {
                    if (var1 != AVEngineKit.CallState.Idle && var1 != AVEngineKit.CallState.Connected) {
                        if (var1 == AVEngineKit.CallState.Connecting) {
                            AVEngineKit.this.g.shouldSopRing();
                            if ((var12 = this.l) != null) {
                                var12.cancel();
                            }

                            this.l = new Timer();
                            this.l.schedule(new l(this), 60000L);
                        }
                    } else {
                        if (var1 == AVEngineKit.CallState.Idle && (var2 == AVEngineKit.CallState.Incoming || var2 == AVEngineKit.CallState.Outgoing)) {
                            AVEngineKit.this.g.shouldSopRing();
                        }

                        if ((var12 = this.l) != null) {
                            var12.cancel();
                        }

                        this.l = null;
                        if (this.m > 0L) {
                            label87: {
                                NotInitializedExecption var10000;
                                label111: {
                                    Message var13;
                                    boolean var15;
                                    boolean var10001;
                                    try {
                                        var15 = (var13 = ChatManager.Instance().getMessage(this.m)).content instanceof CallStartMessageContent;
                                    } catch (NotInitializedExecption var9) {
                                        var10000 = var9;
                                        var10001 = false;
                                        break label111;
                                    }

                                    if (!var15) {
                                        break label87;
                                    }

                                    CallStartMessageContent var14;
                                    AVEngineKit.CallState var16;
                                    AVEngineKit.CallState var17;
                                    try {
                                        var16 = var1;
                                        var14 = (CallStartMessageContent)var13.content;
                                        var17 = AVEngineKit.CallState.Connected;
                                    } catch (NotInitializedExecption var8) {
                                        var10000 = var8;
                                        var10001 = false;
                                        break label111;
                                    }

                                    CallStartMessageContent var18;
                                    byte var19;
                                    if (var16 == var17) {
                                        try {
                                            var18 = var14;
                                            var14.setConnectTime(System.currentTimeMillis());
                                        } catch (NotInitializedExecption var7) {
                                            var10000 = var7;
                                            var10001 = false;
                                            break label111;
                                        }

                                        var19 = 1;
                                    } else {
                                        try {
                                            var18 = var14;
                                            var14.setEndTime(System.currentTimeMillis());
                                        } catch (NotInitializedExecption var6) {
                                            var10000 = var6;
                                            var10001 = false;
                                            break label111;
                                        }

                                        var19 = 2;
                                    }

                                    try {
                                        var18.setStatus(var19);
                                    } catch (NotInitializedExecption var5) {
                                        var10000 = var5;
                                        var10001 = false;
                                        break label111;
                                    }

                                    try {
                                        ChatManager.Instance().updateMessage(this.m, var14);
                                        break label87;
                                    } catch (NotInitializedExecption var4) {
                                        var10000 = var4;
                                        var10001 = false;
                                    }
                                }

                                var10000.printStackTrace();
                            }
                        }
                    }
                } else {
                    AVEngineKit.AVEngineCallback var11 = AVEngineKit.this.g;
                    boolean var3;
                    if (var1 == AVEngineKit.CallState.Incoming) {
                        var3 = true;
                    } else {
                        var3 = false;
                    }

                    var11.shouldStartRing(var3);
                    if ((var12 = this.l) != null) {
                        var12.cancel();
                    }

                    this.l = new Timer();
                    this.l.schedule(new k(this), 60000L);
                }

                AVEngineKit.CallSessionCallback var10;
                if ((var10 = this.e) != null) {
                    var10.didChangeState(var1);
                }

            }
        }

        private void a(AVEngineKit.CallEndReason var1) {
            AVEngineKit.this.e.submit(() -> {
                if (this.b != AVEngineKit.CallState.Idle) {
                    this.k = System.currentTimeMillis();
                    if (var1 != AVEngineKit.CallEndReason.AcceptByOtherClient) {
                        f var2;
                        var2 = new f.<init>(this.a);
                        AVEngineKit.this.a(var2, this.c, false);
                    }

                    this.c = null;
                    this.a = null;
                    this.a(AVEngineKit.CallState.Idle);
                    this.g = null;
                    this.h = null;
                    Iterator var3 = this.f.iterator();

                    while(var3.hasNext()) {
                        ((SurfaceViewRenderer)var3.next()).release();
                    }

                    this.f.clear();
                    this.e.didCallEndWithReason(var1);
                    AVEngineKit.this.f = null;
                    AVEngineKit.this.g();
                }
            });
        }

        private CallSession() {
            this.f = new ArrayList();
            this.i = System.currentTimeMillis();
        }

        public boolean isAudioOnly() {
            return this.d;
        }

        public void setAudioOnly(boolean var1) {
            if (this.d != var1) {
                this.d = var1;
                this.e.didChangeMode(var1);
                AVEngineKit.this.e.submit(() -> {
                    if (AVEngineKit.this.f.getState() == AVEngineKit.CallState.Connected) {
                        h var2;
                        var2 = new h.<init>(this.a, var1);
                        AVEngineKit.this.a(var2, this.c, true);
                    }

                    if (var1 && AVEngineKit.this.k != null) {
                        AVEngineKit.this.k.h();
                    }

                });
            }
        }

        public void switchCamera() {
            AVEngineKit.this.e.submit(() -> {
                if (AVEngineKit.this.k != null) {
                    AVEngineKit.this.k.k();
                }

            });
        }

        public boolean muteAudio(boolean var1) {
            Future var10000 = AVEngineKit.this.e.submit(new i(this, var1));

            try {
                return (Boolean)var10000.get();
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            } catch (ExecutionException var3) {
                var3.printStackTrace();
            }

            return false;
        }

        public boolean muteVideo(boolean var1) {
            Future var10000 = AVEngineKit.this.e.submit(new cn.wildfirechat.avenginekit.j(this, var1));

            try {
                return (Boolean)var10000.get();
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            } catch (ExecutionException var3) {
                var3.printStackTrace();
            }

            return false;
        }

        public AVEngineKit.CallState getState() {
            return this.b;
        }

        public SurfaceView createRendererView() {
            if (AVEngineKit.this.k != null) {
                Exception var10000;
                label26: {
                    SurfaceViewRenderer var1;
                    boolean var10001;
                    SurfaceViewRenderer var10002;
                    org.webrtc.EglBase.Context var10003;
                    SurfaceViewRenderer var4;
                    AVEngineKit.CallSession var5;
                    try {
                        var4 = var1 = new SurfaceViewRenderer;
                        var5 = this;
                        var10002 = var1;
                        var1.<init>(AVEngineKit.this.h);
                        var10003 = AVEngineKit.this.k.e();
                    } catch (Exception var3) {
                        var10000 = var3;
                        var10001 = false;
                        break label26;
                    }

                    Object var10004 = null;

                    try {
                        var10002.init(var10003, (RendererEvents)var10004);
                        var5.f.add(var1);
                        return var4;
                    } catch (Exception var2) {
                        var10000 = var2;
                        var10001 = false;
                    }
                }

                var10000.printStackTrace();
                return null;
            } else {
                return null;
            }
        }

        public void setVideoScalingType(SurfaceView var1, ScalingType var2) {
            if (var1 instanceof SurfaceViewRenderer) {
                ((SurfaceViewRenderer)var1).setScalingType(var2);
            }

        }

        public void setupLocalVideo(SurfaceView var1, ScalingType var2) {
            if (var1 != null) {
                ((SurfaceViewRenderer)var1).setScalingType(var2);
            }

            AVEngineKit.this.e.submit(() -> {
                if (this.g != null) {
                    AVEngineKit.this.l.removeRenderer(this.g);
                }

                if (var1 != null && AVEngineKit.this.l != null) {
                    this.g = new VideoRenderer((SurfaceViewRenderer)var1);
                    AVEngineKit.this.l.addRenderer(this.g);
                }

            });
        }

        public void setupRemoteVideo(SurfaceView var1, ScalingType var2) {
            if (var1 != null) {
                ((SurfaceViewRenderer)var1).setScalingType(var2);
            }

            AVEngineKit.this.e.submit(() -> {
                if (this.h != null) {
                    AVEngineKit.this.m.removeRenderer(this.h);
                }

                if (var1 != null && AVEngineKit.this.m != null) {
                    this.h = new VideoRenderer((SurfaceViewRenderer)var1);
                    AVEngineKit.this.m.addRenderer(this.h);
                }

            });
        }

        public void answerCall(boolean var1) {
            AVEngineKit.this.e.submit(() -> {
                if (this.b != AVEngineKit.CallState.Incoming) {
                    Log.d("CallRTCClient", "can not answer call in state " + this.b);
                } else {
                    this.a(AVEngineKit.CallState.Connecting);
                    if (this.isAudioOnly()) {
                        var1 = true;
                    }

                    this.setAudioOnly(var1);
                    d var2;
                    var2 = new d.<init>(this.a, var1);
                    AVEngineKit.this.a(var2, this.c, false);
                    b var3;
                    var3 = new b.<init>(this.a, var1);
                    AVEngineKit.this.a(var3, this.c, true);
                    AVEngineKit.this.a(true);
                }
            });
        }

        public void endCall() {
            this.a(AVEngineKit.CallEndReason.Hangup);
        }

        public void setCallback(AVEngineKit.CallSessionCallback var1) {
            this.e = var1;
        }

        public String getClientId() {
            return this.c;
        }

        public void stopVideoSource() {
            AVEngineKit.this.e.submit(() -> {
                AVEngineKit.CallState var1;
                if ((var1 = this.b) != AVEngineKit.CallState.Idle && var1 != AVEngineKit.CallState.Incoming && AVEngineKit.this.k != null) {
                    AVEngineKit.this.k.j();
                }

            });
        }

        public void startVideoSource() {
            AVEngineKit.this.e.submit(() -> {
                AVEngineKit.CallState var1;
                if ((var1 = this.b) != AVEngineKit.CallState.Idle && var1 != AVEngineKit.CallState.Incoming && AVEngineKit.this.k != null) {
                    AVEngineKit.this.k.i();
                }

            });
        }

        public long getStartTime() {
            return this.i;
        }

        public long getConnectedTime() {
            return this.j;
        }

        public long getEndTime() {
            return this.k;
        }
    }

    public interface CallSessionCallback {
        void didCallEndWithReason(AVEngineKit.CallEndReason var1);

        void didChangeState(AVEngineKit.CallState var1);

        void didChangeMode(boolean var1);

        void didCreateLocalVideoTrack();

        void didReceiveRemoteVideoTrack();

        void didError(String var1);

        void didGetStats(StatsReport[] var1);
    }

    public interface AVEngineCallback {
        void onReceiveCall(AVEngineKit.CallSession var1);

        void shouldStartRing(boolean var1);

        void shouldSopRing();
    }

    public static enum CallEndReason {
        Busy,
        SignalError,
        Hangup,
        MediaError,
        RemoteHangup,
        OpenCameraFailure,
        Timeout,
        AcceptByOtherClient;

        private CallEndReason() {
        }
    }

    public static enum CallState {
        Idle,
        Outgoing,
        Incoming,
        Connecting,
        Connected;

        private CallState() {
        }
    }
}
