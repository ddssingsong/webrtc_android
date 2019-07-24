//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.wildfirechat.avenginekit;

import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpParameters;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.CameraVideoCapturer.CameraSwitchHandler;
import org.webrtc.DataChannel.Init;
import org.webrtc.Logging.Severity;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.PeerConnection.BundlePolicy;
import org.webrtc.PeerConnection.ContinualGatheringPolicy;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnection.KeyType;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.RTCConfiguration;
import org.webrtc.PeerConnection.RtcpMuxPolicy;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.PeerConnection.TcpCandidatePolicy;
import org.webrtc.PeerConnectionFactory.InitializationOptions;
import org.webrtc.PeerConnectionFactory.Options;
import org.webrtc.RtpParameters.Encoding;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioUtils;

public class w {
    public static final String a = "ARDAMSv0";
    public static final String b = "ARDAMSa0";
    public static final String c = "video";
    private static final String d = "PCRTCClient";
    private static final String e = "VP8";
    private static final String f = "VP9";
    private static final String g = "H264";
    private static final String h = "H264 Baseline";
    private static final String i = "H264 High";
    private static final String j = "opus";
    private static final String k = "ISAC";
    private static final String l = "x-google-start-bitrate";
    private static final String m = "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String n = "WebRTC-IntelVP8/Enabled/";
    private static final String o = "WebRTC-H264HighProfile/Enabled/";
    private static final String p = "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    private static final String q = "VideoFrameEmit/Enabled/";
    private static final String r = "maxaveragebitrate";
    private static final String s = "googEchoCancellation";
    private static final String t = "googAutoGainControl";
    private static final String u = "googHighpassFilter";
    private static final String v = "googNoiseSuppression";
    private static final String w = "levelControl";
    private static final String x = "DtlsSrtpKeyAgreement";
    private static final int y = 1280;
    private static final int z = 720;
    private static final int A = 1000;
    private static final ExecutorService B = Executors.newSingleThreadExecutor();
    private final w.b C = new w.b((o)null);
    private final w.e D = new w.e((o)null);
    private final EglBase E = EglBase.create();
    private PeerConnectionFactory F;
    private PeerConnection G;
    Options H = null;
    private AudioSource I;
    private VideoSource J;
    private boolean K;
    private boolean L;
    private String M;
    private boolean N;
    private boolean O;
    private Timer P;
    public List<IceServer> Q;
    private int R;
    private int S;
    private int T;
    private MediaConstraints U;
    private MediaConstraints V;
    private w.d W;
    private List<IceCandidate> X;
    private w.c Y;
    private boolean Z;
    private SessionDescription aa;
    private MediaStream ba;
    private VideoCapturer ca;
    private boolean da;
    private VideoTrack ea;
    private VideoTrack fa;
    private RtpSender ga;
    private boolean ha;
    private AudioTrack ia;
    private DataChannel ja;
    private boolean ka;

    public w() {
    }

    private void a(Context var1) {
        this.O = false;
        String var2 = "";
        if (this.W.i) {
            var2 = var2 + "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
            Log.d("PCRTCClient", "Enable FlexFEC field trial.");
        }

        var2 = var2 + "WebRTC-IntelVP8/Enabled/";
        if (this.W.s) {
            var2 = var2 + "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
            Log.d("PCRTCClient", "Disable WebRTC AGC field trial.");
        }

        var2 = var2 + "VideoFrameEmit/Enabled/";
        this.M = "VP8";
        String var3;
        if (this.K && (var3 = this.W.g) != null) {
            byte var4 = -1;
            int var5;
            if ((var5 = var3.hashCode()) != -2140422726) {
                if (var5 != -1031013795) {
                    if (var5 != 85182) {
                        if (var5 == 85183 && var3.equals("VP9")) {
                            var4 = 1;
                        }
                    } else if (var3.equals("VP8")) {
                        var4 = 0;
                    }
                } else if (var3.equals("H264 Baseline")) {
                    var4 = 2;
                }
            } else if (var3.equals("H264 High")) {
                var4 = 3;
            }

            switch(var4) {
                case 0:
                default:
                    this.M = "VP8";
                    break;
                case 1:
                    this.M = "VP9";
                    break;
                case 2:
                    this.M = "H264";
                    break;
                case 3:
                    var2 = var2 + "WebRTC-H264HighProfile/Enabled/";
                    this.M = "H264";
            }
        }

        Log.d("PCRTCClient", "Preferred video codec: " + this.M);
        Log.d("PCRTCClient", "Initialize WebRTC. Field trials: " + var2 + " Enable video HW acceleration: " + this.W.h);
        PeerConnectionFactory.initialize(InitializationOptions.builder(var1).setFieldTrials(var2).setEnableVideoHwAcceleration(this.W.h).setEnableInternalTracer(true).createInitializationOptions());
        if (this.W.b) {
            PeerConnectionFactory.startInternalTracingCapture(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "webrtc-trace.txt");
        }

        String var6;
        boolean var7;
        if ((var6 = this.W.k) != null && var6.equals("ISAC")) {
            var7 = true;
        } else {
            var7 = false;
        }

        this.L = var7;
        if (!this.W.n) {
            Log.d("PCRTCClient", "Disable OpenSL ES audio even if device supports it");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true);
        } else {
            Log.d("PCRTCClient", "Allow OpenSL ES audio if device supports it");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
        }

        if (this.W.o) {
            Log.d("PCRTCClient", "Disable built-in AEC even if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        } else {
            Log.d("PCRTCClient", "Enable built-in AEC if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
        }

        if (this.W.p) {
            Log.d("PCRTCClient", "Disable built-in AGC even if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);
        } else {
            Log.d("PCRTCClient", "Enable built-in AGC if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(false);
        }

        if (this.W.q) {
            Log.d("PCRTCClient", "Disable built-in NS even if device supports it");
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        } else {
            Log.d("PCRTCClient", "Enable built-in NS if device supports it");
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(false);
        }

        WebRtcAudioRecord.setErrorCallback(new p(this));
        WebRtcAudioTrack.setErrorCallback(new q(this));
        if (this.H != null) {
            Log.d("PCRTCClient", "Factory networkIgnoreMask option: " + this.H.networkIgnoreMask);
        }

        var7 = "H264 High".equals(this.W.g);
        Object var8;
        Object var9;
        if (this.W.h) {
            var9 = new DefaultVideoEncoderFactory.<init>(this.E.getEglBaseContext(), true, var7);
            var8 = new DefaultVideoDecoderFactory.<init>(this.E.getEglBaseContext());
        } else {
            var9 = new SoftwareVideoEncoderFactory.<init>();
            var8 = new SoftwareVideoDecoderFactory.<init>();
        }

        this.F = new PeerConnectionFactory(this.H, (VideoEncoderFactory)var9, (VideoDecoderFactory)var8);
        Log.d("PCRTCClient", "Peer connection factory created.");
    }

    private void n() {
        if (this.ca == null) {
            Log.w("PCRTCClient", "No camera on device. Switch to audio only call.");
            this.K = false;
        }

        if (this.K) {
            w.d var1;
            this.R = (var1 = this.W).c;
            this.S = var1.d;
            this.T = var1.e;
            if (this.R == 0 || this.S == 0) {
                this.R = 1280;
                this.S = 720;
            }

            if (this.T == 0) {
                this.T = 30;
            }

            Logging.d("PCRTCClient", "Capturing format: " + this.R + "x" + this.S + "@" + this.T);
        }

        this.U = new MediaConstraints();
        if (this.W.l) {
            Log.d("PCRTCClient", "Disabling audio processing");
            this.U.mandatory.add(new KeyValuePair("googEchoCancellation", "false"));
            this.U.mandatory.add(new KeyValuePair("googAutoGainControl", "false"));
            this.U.mandatory.add(new KeyValuePair("googHighpassFilter", "false"));
            this.U.mandatory.add(new KeyValuePair("googNoiseSuppression", "false"));
        }

        if (this.W.r) {
            Log.d("PCRTCClient", "Enabling level control.");
            this.U.mandatory.add(new KeyValuePair("levelControl", "true"));
        }

        this.V = new MediaConstraints();
        this.V.mandatory.add(new KeyValuePair("OfferToReceiveAudio", "true"));
        if (this.K) {
            this.V.mandatory.add(new KeyValuePair("OfferToReceiveVideo", "true"));
        } else {
            this.V.mandatory.add(new KeyValuePair("OfferToReceiveVideo", "false"));
        }

    }

    private void o() {
        if (this.F != null && !this.O) {
            Log.d("PCRTCClient", "Create peer connection.");
            this.X = new ArrayList();
            if (this.K) {
                this.F.setVideoHwAccelerationOptions(this.E.getEglBaseContext(), this.E.getEglBaseContext());
            }

            RTCConfiguration var1;
            RTCConfiguration var10003 = var1 = new RTCConfiguration;
            var10003.<init>(this.Q);
            var10003.tcpCandidatePolicy = TcpCandidatePolicy.DISABLED;
            var10003.bundlePolicy = BundlePolicy.MAXBUNDLE;
            var10003.rtcpMuxPolicy = RtcpMuxPolicy.REQUIRE;
            var10003.continualGatheringPolicy = ContinualGatheringPolicy.GATHER_CONTINUALLY;
            var10003.keyType = KeyType.ECDSA;
            var10003.enableDtlsSrtp = true;
            this.G = this.F.createPeerConnection(var1, this.C);
            if (this.ka) {
                Init var8;
                Init var10002 = var8 = new Init;
                var8.<init>();
                var8.ordered = this.W.t.a;
                var8.negotiated = this.W.t.e;
                var8.maxRetransmits = this.W.t.c;
                var8.maxRetransmitTimeMs = this.W.t.b;
                var8.id = this.W.t.f;
                var10002.protocol = this.W.t.d;
                this.ja = this.G.createDataChannel("AVEngineKit data", var8);
            }

            this.Z = false;
            Logging.enableLogToDebugOutput(Severity.LS_INFO);
            this.ba = this.F.createLocalMediaStream("ARDAMS");
            if (this.K) {
                this.ba.addTrack(this.a(this.ca));
            }

            this.ba.addTrack(this.m());
            this.G.addStream(this.ba);
            if (this.K) {
                this.q();
            }

            if (this.W.m) {
                label52: {
                    IOException var10000;
                    label65: {
                        w var9;
                        File var10;
                        File var12;
                        StringBuilder var16;
                        boolean var10001;
                        try {
                            var9 = this;
                            var10 = new File;
                            var12 = var10;
                            var16 = (new StringBuilder()).append(Environment.getExternalStorageDirectory().getPath()).append(File.separator);
                        } catch (IOException var5) {
                            var10000 = var5;
                            var10001 = false;
                            break label65;
                        }

                        String var10004 = "Download/audio.aecdump";

                        try {
                            var12.<init>(var16.append(var10004).toString());
                        } catch (IOException var4) {
                            var10000 = var4;
                            var10001 = false;
                            break label65;
                        }

                        int var14 = 1006632960;

                        PeerConnectionFactory var11;
                        int var13;
                        try {
                            ParcelFileDescriptor var6 = ParcelFileDescriptor.open(var10, var14);
                            var11 = var9.F;
                            var13 = var6.getFd();
                        } catch (IOException var3) {
                            var10000 = var3;
                            var10001 = false;
                            break label65;
                        }

                        byte var15 = -1;

                        try {
                            var11.startAecDump(var13, var15);
                            break label52;
                        } catch (IOException var2) {
                            var10000 = var2;
                            var10001 = false;
                        }
                    }

                    IOException var7 = var10000;
                    Log.e("PCRTCClient", "Can not open aecdump file", var7);
                }
            }

            Log.d("PCRTCClient", "Peer connection created.");
        } else {
            Log.e("PCRTCClient", "Peerconnection factory is not created");
        }
    }

    private void l() {
        PeerConnectionFactory var1;
        if ((var1 = this.F) != null && this.W.m) {
            var1.stopAecDump();
        }

        Log.d("PCRTCClient", "Closing peer connection.");
        this.P.cancel();
        DataChannel var2;
        if ((var2 = this.ja) != null) {
            var2.dispose();
            this.ja = null;
        }

        PeerConnection var3;
        if ((var3 = this.G) != null) {
            var3.dispose();
            this.G = null;
        }

        Log.d("PCRTCClient", "Closing audio source.");
        AudioSource var4;
        if ((var4 = this.I) != null) {
            var4.dispose();
            this.I = null;
        }

        Log.d("PCRTCClient", "Stopping capture.");
        if (this.ca != null) {
            this.N = true;
            this.ca = null;
        }

        Log.d("PCRTCClient", "Closing video source.");
        VideoSource var5;
        if ((var5 = this.J) != null) {
            var5.dispose();
            this.J = null;
        }

        Log.d("PCRTCClient", "Closing peer connection factory.");
        if ((var1 = this.F) != null) {
            var1.dispose();
            this.F = null;
        }

        this.H = null;
        this.E.release();
        Log.d("PCRTCClient", "Closing peer connection done.");
        this.Y.c();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        this.Y = null;
    }

    private void r() {
        PeerConnection var1;
        if ((var1 = this.G) != null && !this.O) {
            if (!var1.getStats(new r(this), (MediaStreamTrack)null)) {
                Log.e("PCRTCClient", "getStats() returns false!");
            }

        }
    }

    private void b(String var1) {
        Log.e("PCRTCClient", "Peerconnection error: " + var1);
        B.execute(() -> {
            if (!this.O) {
                this.Y.a(var1);
                this.O = true;
            }

        });
    }

    private AudioTrack m() {
        this.I = this.F.createAudioSource(this.U);
        this.ia = this.F.createAudioTrack("ARDAMSa0", this.I);
        this.ia.setEnabled(this.ha);
        return this.ia;
    }

    private void q() {
        Iterator var1 = this.G.getSenders().iterator();

        while(var1.hasNext()) {
            RtpSender var2;
            if ((var2 = (RtpSender)var1.next()).track() != null && var2.track().kind().equals("video")) {
                Log.d("PCRTCClient", "Found video sender.");
                this.ga = var2;
            }
        }

    }

    private static String a(String var0, boolean var1, String var2, int var3) {
        String[] var4 = var2.split("\r\n");
        byte var5 = -1;
        boolean var6 = false;
        String var7 = null;
        Pattern var8 = Pattern.compile("^a=rtpmap:(\\d+) " + var0 + "(/\\d+)+[\r]?$");
        int var9 = 0;

        while(true) {
            if (var9 >= var4.length) {
                var9 = var5;
                break;
            }

            Matcher var10;
            if ((var10 = var8.matcher(var4[var9])).matches()) {
                var7 = var10.group(1);
                break;
            }

            ++var9;
        }

        if (var7 == null) {
            Log.w("PCRTCClient", "No rtpmap for " + var0 + " codec");
            return var2;
        } else {
            Log.d("PCRTCClient", "Found " + var0 + " rtpmap " + var7 + " at " + var4[var9]);
            Pattern var12 = Pattern.compile("^a=fmtp:" + var7 + " \\w+=\\d+.*[\r]?$");

            StringBuilder var11;
            for(int var14 = 0; var14 < var4.length; ++var14) {
                if (var12.matcher(var4[var14]).matches()) {
                    Log.d("PCRTCClient", "Found " + var0 + " " + var4[var14]);
                    StringBuilder var10002;
                    if (var1) {
                        var10002 = var11 = new StringBuilder;
                        var11.<init>();
                        var4[var14] = var10002.append(var4[var14]).append("; x-google-start-bitrate=").append(var3).toString();
                    } else {
                        var10002 = var11 = new StringBuilder;
                        var11.<init>();
                        var4[var14] = var10002.append(var4[var14]).append("; maxaveragebitrate=").append(var3 * 1000).toString();
                    }

                    Log.d("PCRTCClient", "Update remote SDP line: " + var4[var14]);
                    var6 = true;
                    break;
                }
            }

            var11 = new StringBuilder.<init>();

            for(int var13 = 0; var13 < var4.length; ++var13) {
                var11.append(var4[var13]).append("\r\n");
                if (!var6 && var13 == var9) {
                    String var15;
                    if (var1) {
                        var15 = "a=fmtp:" + var7 + " " + "x-google-start-bitrate" + "=" + var3;
                    } else {
                        var15 = "a=fmtp:" + var7 + " " + "maxaveragebitrate" + "=" + var3 * 1000;
                    }

                    Log.d("PCRTCClient", "Add remote SDP line: " + var15);
                    var11.append(var15).append("\r\n");
                }
            }

            return var11.toString();
        }
    }

    private static int a(boolean var0, String[] var1) {
        String var3;
        if (var0) {
            var3 = "m=audio ";
        } else {
            var3 = "m=video ";
        }

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if (var1[var2].startsWith(var3)) {
                return var2;
            }
        }

        return -1;
    }

    private static String a(Iterable<? extends CharSequence> var0, String var1, boolean var2) {
        Iterator var4;
        if (!(var4 = var0.iterator()).hasNext()) {
            return "";
        } else {
            StringBuilder var3;
            var3 = new StringBuilder.<init>((CharSequence)var4.next());

            while(var4.hasNext()) {
                var3.append(var1).append((CharSequence)var4.next());
            }

            if (var2) {
                var3.append(var1);
            }

            return var3.toString();
        }
    }

    private static String a(List<String> var0, String var1) {
        List var2;
        if ((var2 = Arrays.asList(var1.split(" "))).size() <= 3) {
            Log.e("PCRTCClient", "Wrong SDP media description format: " + var1);
            return null;
        } else {
            List var4 = var2.subList(0, 3);
            ArrayList var3;
            ArrayList var10000 = var3 = new ArrayList;
            var3.<init>(var2.subList(3, var2.size()));
            var10000.removeAll(var0);
            ArrayList var5;
            var10000 = var5 = new ArrayList;
            var5.<init>();
            var5.addAll(var4);
            var5.addAll(var0);
            var10000.addAll(var3);
            return a((Iterable)var10000, " ", false);
        }
    }

    private static String b(String var0, String var1, boolean var2) {
        int var3;
        String[] var9;
        if ((var3 = a(var2, var9 = var0.split("\r\n"))) == -1) {
            Log.w("PCRTCClient", "No mediaDescription line, so can't prefer " + var1);
            return var0;
        } else {
            ArrayList var4;
            var4 = new ArrayList.<init>();
            Pattern var5 = Pattern.compile("^a=rtpmap:(\\d+) " + var1 + "(/\\d+)+[\r]?$");
            int var6 = var9.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Matcher var8;
                if ((var8 = var5.matcher(var9[var7])).matches()) {
                    var4.add(var8.group(1));
                }
            }

            if (var4.isEmpty()) {
                Log.w("PCRTCClient", "No payload types with name " + var1);
                return var0;
            } else if ((var1 = a((List)var4, (String)var9[var3])) == null) {
                return var0;
            } else {
                Log.d("PCRTCClient", "Change media description from: " + var9[var3] + " to " + var1);
                var9[var3] = var1;
                return a((Iterable)Arrays.asList(var9), "\r\n", true);
            }
        }
    }

    private void p() {
        if (this.X != null) {
            Log.d("PCRTCClient", "Add " + this.X.size() + " remote candidates");
            Iterator var1 = this.X.iterator();

            while(var1.hasNext()) {
                IceCandidate var2 = (IceCandidate)var1.next();
                this.G.addIceCandidate(var2);
            }

            this.X = null;
        }

    }

    private void y() {
        if (this.ca instanceof CameraVideoCapturer) {
            if (!this.K || this.O) {
                Log.e("PCRTCClient", "Failed to switch camera. Video: " + this.K + ". Error : " + this.O);
                return;
            }

            Log.d("PCRTCClient", "Switch camera");
            ((CameraVideoCapturer)this.ca).switchCamera((CameraSwitchHandler)null);
        } else {
            Log.d("PCRTCClient", "Will not switch camera, video caputurer is not a camera");
        }

    }

    private void b(int var1, int var2, int var3) {
        if (this.K && !this.O && this.ca != null) {
            Log.d("PCRTCClient", "changeCaptureFormat: " + var1 + "x" + var2 + "@" + var3);
            this.J.adaptOutputFormat(var1, var2, var3);
        } else {
            Log.e("PCRTCClient", "Failed to change capture format. Video: " + this.K + ". Error : " + this.O);
        }
    }

    public void a(Options var1) {
        this.H = var1;
    }

    public void a(Context var1, w.c var2, boolean var3, int var4, int var5, int var6, int var7) {
        this.W = new w.d(var3, false, var4, var5, var6, var7, "H264 Baseline", true, true, 0, "OPUS", false, false, false, true, true, true, false, false, (w.a)null);
        this.Y = var2;
        w.d var8;
        this.K = (var8 = this.W).a;
        boolean var9;
        if (var8.t != null) {
            var9 = true;
        } else {
            var9 = false;
        }

        this.ka = var9;
        this.F = null;
        this.G = null;
        this.L = false;
        this.N = false;
        this.O = false;
        this.X = null;
        this.aa = null;
        this.ba = null;
        this.ca = null;
        this.da = true;
        this.ea = null;
        this.fa = null;
        this.ga = null;
        this.ha = true;
        this.ia = null;
        this.P = new Timer();
        if ((var8 = this.W).a) {
            this.R = var8.c;
            this.S = var8.d;
            this.T = var8.e;
            if (var4 == 0 || var5 == 0) {
                this.R = 1280;
                this.S = 720;
            }

            if (this.T == 0) {
                this.T = 30;
            }

            Logging.d("PCRTCClient", "Capturing format: " + var4 + "x" + var5 + "@" + var6);
        }

        this.a(var1);
    }

    public void h() {
        MediaStream var1;
        VideoTrack var2;
        if ((var1 = this.ba) != null && (var2 = this.ea) != null) {
            var1.removeTrack(var2);
            this.ea.dispose();
            this.ea = null;
            this.K = false;
            this.Y.a();
        }

    }

    public void a(VideoCapturer var1, List<IceServer> var2) {
        if (this.W == null) {
            Log.e("PCRTCClient", "Creating peer connection without initializing factory.");
        } else {
            this.ca = var1;
            this.Q = var2;
            B.execute(new o(this));
        }
    }

    public void b() {
        B.execute(() -> {
            this.l();
        });
    }

    public boolean g() {
        return this.K;
    }

    public boolean f() {
        return this.K && this.R * this.S >= 921600;
    }

    public org.webrtc.EglBase.Context e() {
        return this.E.getEglBaseContext();
    }

    public void a(boolean var1, int var2) {
        if (var1) {
            Exception var10000;
            label26: {
                boolean var10001;
                Timer var6;
                t var7;
                try {
                    var6 = this.P;
                    var7 = new t(this);
                } catch (Exception var4) {
                    var10000 = var4;
                    var10001 = false;
                    break label26;
                }

                long var10002 = 0L;
                long var10003 = (long)var2;

                try {
                    var6.schedule(var7, var10002, var10003);
                    return;
                } catch (Exception var3) {
                    var10000 = var3;
                    var10001 = false;
                }
            }

            Exception var5 = var10000;
            Log.e("PCRTCClient", "Can not schedule statistics timer", var5);
        } else {
            this.P.cancel();
        }

    }

    public void a(boolean var1) {
        B.execute(new u(this, var1));
    }

    public void b(boolean var1) {
        B.execute(new v(this, var1));
    }

    public void d() {
        B.execute(() -> {
            if (this.G != null && !this.O) {
                Log.d("PCRTCClient", "PC Create OFFER");
                this.Z = true;
                this.G.createOffer(this.D, this.V);
            }

        });
    }

    public void c() {
        B.execute(() -> {
            if (this.G != null && !this.O) {
                Log.d("PCRTCClient", "PC create ANSWER");
                this.Z = false;
                this.G.createAnswer(this.D, this.V);
            }

        });
    }

    public void a(IceCandidate var1) {
        B.execute(() -> {
            PeerConnection var2;
            if ((var2 = this.G) != null && !this.O) {
                List var3;
                if ((var3 = this.X) != null) {
                    var3.add(var1);
                } else {
                    var2.addIceCandidate(var1);
                }
            }

        });
    }

    public void a(IceCandidate[] var1) {
        B.execute(() -> {
            if (this.G != null && !this.O) {
                this.p();
                this.G.removeIceCandidates(var1);
            }
        });
    }

    public void a(SessionDescription var1) {
        B.execute(() -> {
            if (this.G != null && !this.O) {
                String var2 = var1.description;
                if (this.L) {
                    var2 = b(var2, "ISAC", true);
                }

                if (this.K) {
                    var2 = b(var2, this.M, false);
                }

                int var3;
                if ((var3 = this.W.j) > 0) {
                    var2 = a("opus", false, var2, var3);
                }

                Log.d("PCRTCClient", "Set remote SDP.");
                SessionDescription var4;
                var4 = new SessionDescription.<init>(var1.type, var2);
                this.G.setRemoteDescription(this.D, var4);
            }
        });
    }

    public void j() {
        B.execute(() -> {
            if (this.ca != null && !this.N) {
                w var10000 = this;
                Log.d("PCRTCClient", "Stop video source.");

                try {
                    var10000.ca.stopCapture();
                } catch (InterruptedException var1) {
                }

                this.N = true;
            }

        });
    }

    public void i() {
        B.execute(() -> {
            if (this.ca != null && this.N) {
                Log.d("PCRTCClient", "Restart video source.");
                this.ca.startCapture(this.R, this.S, this.T);
                this.N = false;
            }

        });
    }

    public void a(Integer var1) {
        B.execute(() -> {
            if (this.G != null && this.ga != null && !this.O) {
                Log.d("PCRTCClient", "Requested max video bitrate: " + var1);
                RtpSender var2;
                if ((var2 = this.ga) == null) {
                    Log.w("PCRTCClient", "Sender is not ready.");
                } else {
                    RtpParameters var6;
                    if ((var6 = var2.getParameters()).encodings.size() == 0) {
                        Log.w("PCRTCClient", "RtpParameters are not ready.");
                    } else {
                        Encoding var4;
                        Integer var5;
                        for(Iterator var3 = var6.encodings.iterator(); var3.hasNext(); var4.maxBitrateBps = var5) {
                            var4 = (Encoding)var3.next();
                            if (var1 == null) {
                                var5 = null;
                            } else {
                                var5 = var1 * 1000;
                            }
                        }

                        if (!this.ga.setParameters(var6)) {
                            Log.e("PCRTCClient", "RtpSender.setParameters failed.");
                        }

                        Log.d("PCRTCClient", "Configured max video bitrate to: " + var1);
                    }
                }
            }
        });
    }

    public VideoTrack a(VideoCapturer var1) {
        this.ca = var1;
        VideoTrack var2;
        if ((var2 = this.ea) != null) {
            return var2;
        } else {
            this.J = this.F.createVideoSource(var1);
            var1.startCapture(this.R, this.S, this.T);
            this.ea = this.F.createVideoTrack("ARDAMSv0", this.J);
            this.ea.setEnabled(this.da);
            this.Y.a(this.ea);
            return this.ea;
        }
    }

    public void k() {
        B.execute(() -> {
            this.y();
        });
    }

    public void a(int var1, int var2, int var3) {
        B.execute(() -> {
            this.b(var1, var2, var3);
        });
    }

    private class e implements SdpObserver {
        private e() {
        }

        public void onCreateSuccess(SessionDescription var1) {
            if (w.this.aa != null) {
                w.this.b("Multiple SDP create.");
            } else {
                String var2 = var1.description;
                if (w.this.L) {
                    var2 = cn.wildfirechat.avenginekit.w.b(var2, "ISAC", true);
                }

                if (w.this.K) {
                    var2 = cn.wildfirechat.avenginekit.w.b(var2, w.this.M, false);
                }

                SessionDescription var3;
                var3 = new SessionDescription.<init>(var1.type, var2);
                w.this.aa = var3;
                cn.wildfirechat.avenginekit.w.B.execute(new y(this, var3));
            }
        }

        public void onSetSuccess() {
            cn.wildfirechat.avenginekit.w.B.execute(new z(this));
        }

        public void onCreateFailure(String var1) {
            w.this.b("createSDP error: " + var1);
        }

        public void onSetFailure(String var1) {
            w.this.b("setSDP error: " + var1);
        }
    }

    private class b implements Observer {
        private b() {
        }

        public void onIceCandidate(IceCandidate var1) {
            cn.wildfirechat.avenginekit.w.B.execute(() -> {
                w.this.Y.onIceCandidate(var1);
            });
        }

        public void onIceCandidatesRemoved(IceCandidate[] var1) {
            cn.wildfirechat.avenginekit.w.B.execute(() -> {
                w.this.Y.onIceCandidatesRemoved(var1);
            });
        }

        public void onSignalingChange(SignalingState var1) {
            Log.d("PCRTCClient", "SignalingState: " + var1);
        }

        public void onIceConnectionChange(IceConnectionState var1) {
            cn.wildfirechat.avenginekit.w.B.execute(() -> {
                Log.d("PCRTCClient", "IceConnectionState: " + var1);
                if (var1 == IceConnectionState.CONNECTED) {
                    if (w.this.Y != null) {
                        w.this.Y.d();
                    }
                } else if (var1 == IceConnectionState.DISCONNECTED) {
                    if (w.this.Y != null) {
                        w.this.Y.b();
                    }
                } else if (var1 == IceConnectionState.FAILED) {
                    w.this.b("ICE connection failed.");
                }

            });
        }

        public void onIceGatheringChange(IceGatheringState var1) {
            Log.d("PCRTCClient", "IceGatheringState: " + var1);
        }

        public void onIceConnectionReceivingChange(boolean var1) {
            Log.d("PCRTCClient", "IceConnectionReceiving changed to " + var1);
        }

        public void onAddStream(MediaStream var1) {
            cn.wildfirechat.avenginekit.w.B.execute(() -> {
                if (w.this.G != null && !w.this.O) {
                    if (var1.audioTracks.size() <= 1 && var1.videoTracks.size() <= 1) {
                        if (var1.videoTracks.size() == 1) {
                            w.this.fa = (VideoTrack)var1.videoTracks.get(0);
                            w.this.fa.setEnabled(w.this.da);
                            w.this.Y.c(w.this.fa);
                        }

                    } else {
                        w.this.b("Weird-looking stream: " + var1);
                    }
                }
            });
        }

        public void onRemoveStream(MediaStream var1) {
            cn.wildfirechat.avenginekit.w.B.execute(() -> {
                if (w.this.Y != null) {
                    w.this.Y.b(w.this.fa);
                }

                w.this.fa = null;
            });
        }

        public void onDataChannel(DataChannel var1) {
            Log.d("PCRTCClient", "New Data channel " + var1.label());
            if (w.this.ka) {
                var1.registerObserver(new x(this, var1));
            }
        }

        public void onRenegotiationNeeded() {
        }

        public void onAddTrack(RtpReceiver var1, MediaStream[] var2) {
        }
    }

    public interface c {
        void a(SessionDescription var1);

        void onIceCandidate(IceCandidate var1);

        void onIceCandidatesRemoved(IceCandidate[] var1);

        void d();

        void b();

        void c(VideoTrack var1);

        void b(VideoTrack var1);

        void a(VideoTrack var1);

        void a();

        void c();

        void a(StatsReport[] var1);

        void a(String var1);
    }

    private static class d {
        public final boolean a;
        public final boolean b;
        public final int c;
        public final int d;
        public final int e;
        public final int f;
        public final String g;
        public final boolean h;
        public final boolean i;
        public final int j;
        public final String k;
        public final boolean l;
        public final boolean m;
        public final boolean n;
        public final boolean o;
        public final boolean p;
        public final boolean q;
        public final boolean r;
        public final boolean s;
        private final w.a t;

        public d(boolean var1, boolean var2, int var3, int var4, int var5, int var6, String var7, boolean var8, boolean var9, int var10, String var11, boolean var12, boolean var13, boolean var14, boolean var15, boolean var16, boolean var17, boolean var18, boolean var19) {
            this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, var15, var16, var17, var18, var19, (w.a)null);
        }

        public d(boolean var1, boolean var2, int var3, int var4, int var5, int var6, String var7, boolean var8, boolean var9, int var10, String var11, boolean var12, boolean var13, boolean var14, boolean var15, boolean var16, boolean var17, boolean var18, boolean var19, w.a var20) {
            this.a = var1;
            this.b = var2;
            this.c = var3;
            this.d = var4;
            this.e = var5;
            this.f = var6;
            this.g = var7;
            this.i = var9;
            this.h = var8;
            this.j = var10;
            this.k = var11;
            this.l = var12;
            this.m = var13;
            this.n = var14;
            this.o = var15;
            this.p = var16;
            this.q = var17;
            this.r = var18;
            this.s = var19;
            this.t = var20;
        }
    }

    private static class a {
        public final boolean a;
        public final int b;
        public final int c;
        public final String d;
        public final boolean e;
        public final int f;

        public a(boolean var1, int var2, int var3, String var4, boolean var5, int var6) {
            this.a = var1;
            this.b = var2;
            this.c = var3;
            this.d = var4;
            this.e = var5;
            this.f = var6;
        }
    }
}
