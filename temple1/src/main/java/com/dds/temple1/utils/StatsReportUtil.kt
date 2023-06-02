package com.dds.temple1.utils

import android.util.Log
import org.webrtc.RTCStatsReport
import java.math.BigInteger

class StatsReportUtil {
    private var lastBytesReceivedVideo = BigInteger.ZERO
    private var lastBytesSentVideo = BigInteger.ZERO
    private var lastBytesReceivedAudio = BigInteger.ZERO
    private var lastBytesSentAudio = BigInteger.ZERO
    private var lastFrameDecodedOut = 0L
    private var lastFrameDecodedIn = 0L

    fun getStatsReport(report: RTCStatsReport?): String {
        if (report == null) {
            return ""
        }
        var audioInFound = false
        var videoInFound = false
        var audioOutFound = false
        var videoOutFound = false

        var audioInCodec = "unknown"
        var videoInCodec = "unknown"
        var audioOutCodec = "unknown"
        var videoOutCodec = "unknown"

        var audioInBytesDelta = 0L
        var audioOutBytesDelta = 0L
        var videoInBytesDelta = 0L
        var videoOutBytesDelta = 0L

        var videoInWidth = 0L
        var videoInHeight = 0L
        var videoInFrameRate = 0L

        var videoOutWidth = 0L
        var videoOutHeight = 0L
        var videoOutFrameRate = 0L

        val statsMap = report.statsMap
        for (stats in statsMap.values) {
            if (stats.type == "inbound-rtp") {
                val members = stats.members
                val mediaType = members["kind"]

                if (mediaType == "video") {
                    if (videoInFound) {
                        Log.w(TAG, "Already found inbound video track")
                        continue
                    }

                    val codecId = members["codecId"] as String?
                    val trackId = members["trackId"] as String?

                    if (codecId != null) {
                        val vmap = statsMap[codecId]!!
                        videoInCodec = (vmap.members["mimeType"] as String?) ?: videoInCodec
                    }

                    if (trackId != null) {
                        val vmap = statsMap[trackId]!!
                        videoInWidth = (vmap.members["frameWidth"] as Long?) ?: 0L
                        videoInHeight = (vmap.members["frameHeight"] as Long?) ?: 0L
                    }

                    val bytes = members["bytesReceived"] as BigInteger
                    videoInBytesDelta =
                        (bytes.toLong() - lastBytesReceivedVideo.toLong()) * 8 / STATS_INTERVAL_MS
                    lastBytesReceivedVideo = bytes

                    val framesDecoded = members["framesDecoded"] as Long
                    val lastFrame = lastFrameDecodedIn
                    videoInFrameRate = ((framesDecoded - lastFrame) * 1000L / STATS_INTERVAL_MS)
                    lastFrameDecodedIn = framesDecoded
                    videoInFound = true
                }

                if (mediaType == "audio") {
                    if (audioInFound) {
                        Log.w(TAG, "Already found inbound audio track")
                        continue
                    }

                    val codecId = members["codecId"] as String?
                    if (codecId != null) {
                        val vmap = statsMap[codecId]!!
                        audioInCodec = (vmap.members["mimeType"] as String?) ?: audioInCodec
                    }

                    val bytes = members["bytesReceived"] as BigInteger
                    audioInBytesDelta =
                        (bytes.toLong() - lastBytesReceivedAudio.toLong()) * 8 / STATS_INTERVAL_MS
                    lastBytesReceivedAudio = bytes
                    audioInFound = true
                }
            } else if (stats.type == "outbound-rtp") {
                val map = stats.members
                val mediaType = map["kind"]

                if (mediaType == "video") {
                    if (videoOutFound) {
                        Log.w(TAG, "Already found outbound video track")
                        continue
                    }

                    val trackId = map["trackId"] as String?
                    val codecId = map["codecId"] as String?

                    if (trackId != null) {
                        val vmap = statsMap[trackId]!!
                        videoOutWidth = (vmap.members["frameWidth"] as Long?) ?: 0L
                        videoOutHeight = (vmap.members["frameHeight"] as Long?) ?: 0L
                    }

                    if (codecId != null) {
                        val vmap = statsMap[codecId]!!
                        videoOutCodec = (vmap.members["mimeType"] as String?) ?: videoOutCodec
                    }

                    val bytes = map["bytesSent"] as BigInteger
                    videoOutBytesDelta =
                        (bytes.toLong() - lastBytesSentVideo.toLong()) * 8 / STATS_INTERVAL_MS
                    lastBytesSentVideo = bytes

                    val framesEncoded = map["framesEncoded"] as Long
                    val lastFrame = lastFrameDecodedOut
                    videoOutFrameRate = ((framesEncoded - lastFrame) * 1000L / STATS_INTERVAL_MS)
                    lastFrameDecodedOut = framesEncoded
                    videoOutFound = true
                }

                if (mediaType == "audio") {
                    if (audioOutFound) {
                        Log.w(TAG, "Already found outbound audio track")
                        continue
                    }

                    val codecId = map["codecId"] as String?
                    if (codecId != null) {
                        val vmap = statsMap[codecId]!!
                        audioOutCodec = (vmap.members["mimeType"] as String?) ?: audioOutCodec
                    }

                    val bytes = map["bytesSent"] as BigInteger
                    audioOutBytesDelta =
                        (bytes.toLong() - lastBytesSentAudio.toLong()) * 8 / STATS_INTERVAL_MS
                    lastBytesSentAudio = bytes
                    audioOutFound = true
                }
            }
        }

        if (!audioInFound) {
            lastBytesReceivedAudio = BigInteger.ZERO
        }

        if (!audioOutFound) {
            lastBytesSentAudio = BigInteger.ZERO
        }

        if (!videoInFound) {
            lastFrameDecodedIn = 0
            lastBytesReceivedVideo = BigInteger.ZERO
        }

        if (!videoOutFound) {
            lastFrameDecodedOut = 0
            lastBytesSentVideo = BigInteger.ZERO
        }

        return " Receiving\n" +
                "  Video Codec: $videoInCodec\n" +
                "   Quality: ${videoInWidth}x$videoInHeight @ $videoInFrameRate fps\n" +
                "   Bitrate: $videoInBytesDelta kbps\n" +
                "  Audio Codec: $audioInCodec\n" +
                "   Bitrate: $audioInBytesDelta kbps\n" +
                " Sending\n" +
                "  Video Codec: $videoOutCodec\n" +
                "   Quality: ${videoOutWidth}x$videoOutHeight @ $videoOutFrameRate fps\n" +
                "   Bitrate: $videoOutBytesDelta kbps\n" +
                "  Audio Codec: $audioOutCodec\n" +
                "   Bitrate: $audioOutBytesDelta kbps\n"
    }

    companion object {
        const val STATS_INTERVAL_MS = 1000L
        private const val TAG = "StatsReportUtil"
    }
}