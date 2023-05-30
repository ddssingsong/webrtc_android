/*
 *  Copyright 2017 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import android.media.MediaCodecInfo;


import java.util.Arrays;

/**
 * Factory for Android hardware VideoDecoders.
 */
public class HardwareVideoDecoderFactory extends MediaCodecVideoDecoderFactory {
    private final static Predicate<MediaCodecInfo> defaultAllowedPredicate =
            new Predicate<MediaCodecInfo>() {
                private String[] prefixBlacklist =
                        Arrays.copyOf(MediaCodecUtils.SOFTWARE_IMPLEMENTATION_PREFIXES,
                                MediaCodecUtils.SOFTWARE_IMPLEMENTATION_PREFIXES.length);

                @Override
                public boolean test(MediaCodecInfo arg) {
                    final String name = arg.getName();
                    for (String prefix : prefixBlacklist) {
                        if (name.startsWith(prefix)) {
                            return false;
                        }
                    }
                    return true;
                }
            };

    /**
     * Creates a HardwareVideoDecoderFactory that does not use surface textures.
     */
    @Deprecated // Not removed yet to avoid breaking callers.
    public HardwareVideoDecoderFactory() {
        this(null);
    }

    /**
     * Creates a HardwareVideoDecoderFactory that supports surface texture rendering.
     *
     * @param sharedContext The textures generated will be accessible from this context. May be null,
     *                      this disables texture support.
     */
    public HardwareVideoDecoderFactory(  EglBase.Context sharedContext) {
        this(sharedContext, /* codecAllowedPredicate= */ null);
    }

    /**
     * Creates a HardwareVideoDecoderFactory that supports surface texture rendering.
     *
     * @param sharedContext         The textures generated will be accessible from this context. May be null,
     *                              this disables texture support.
     * @param codecAllowedPredicate predicate to filter codecs. It is combined with the default
     *                              predicate that only allows hardware codecs.
     */
    public HardwareVideoDecoderFactory(  EglBase.Context sharedContext,
                                         Predicate<MediaCodecInfo> codecAllowedPredicate) {
        super(sharedContext,
                (codecAllowedPredicate == null ? defaultAllowedPredicate
                        : codecAllowedPredicate.and(defaultAllowedPredicate)));
    }
}
