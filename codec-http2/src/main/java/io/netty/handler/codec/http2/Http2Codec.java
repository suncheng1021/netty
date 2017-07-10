/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.UnstableApi;

/**
 * An HTTP/2 channel handler that adds a {@link Http2FrameCodec} and {@link Http2MultiplexCodec} to the pipeline before
 * removing itself.
 */
@UnstableApi
public final class Http2Codec extends ChannelDuplexHandler {
    private final Http2FrameCodec frameCodec;
    private final Http2MultiplexCodec multiplexCodec;

    Http2Codec(boolean server, Http2StreamChannelBootstrap bootstrap, Http2FrameWriter frameWriter,
               Http2FrameLogger frameLogger, Http2Settings initialSettings) {
        frameCodec = new Http2FrameCodec(server, frameWriter, frameLogger, initialSettings);
        multiplexCodec = new Http2MultiplexCodec(server, bootstrap);
    }

    Http2FrameCodec frameCodec() {
        return frameCodec;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // We use addAfter(...) because if we would use addBefore(...) we could end up with an incorrect
        // order of handlers in the pipeline when replace(...) is used with the Http2Codec.
        // See https://github.com/netty/netty/issues/6881.
        ctx.pipeline().addAfter(ctx.executor(), ctx.name(), null, multiplexCodec);
        ctx.pipeline().addAfter(ctx.executor(), ctx.name(), null, frameCodec);

        ctx.pipeline().remove(this);
    }
}
