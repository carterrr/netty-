/*
 * Copyright 2012 The Netty Project
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
package io.netty.example.worldclock;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

public class WorldClockClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public WorldClockClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc(), WorldClockClient.HOST, WorldClockClient.PORT));
        }

        // 步骤1 一次解码器  转化protoBuf 得到byteBuf
        p.addLast(new ProtobufVarint32FrameDecoder());
        // 步骤2 二次解码器  从byteBuf  -> java对象
        p.addLast(new ProtobufDecoder(WorldClockProtocol.LocalTimes.getDefaultInstance()));

        // 步骤5  一次编码器  序列化的长度字段长度可变  修正读写指针等长度  处理对端粘包半包问题
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        // 步骤4  二次编码器  java对象 ->  byte[] 字节数组  直接序列化
        p.addLast(new ProtobufEncoder());

        // 步骤3  业务逻辑 返回一个结果 WorldClockProtocol.Locations对象
        // protobuf已经帮我们做了二次编码过程 因此无需单独二次编码器
        p.addLast(new WorldClockClientHandler());
    }
}
