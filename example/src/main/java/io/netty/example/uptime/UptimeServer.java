/*
 * Copyright 2017 The Netty Project
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
package io.netty.example.uptime;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Uptime server is served as a connection server.
 * So it simply discards all message received.
 */
public final class UptimeServer {
    private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    private static final UptimeServerHandler handler = new UptimeServerHandler();

    private UptimeServer() {
    }

    public static void main(String[] args) throws Exception {

        //用来接收连接，只需要一个线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //用来处理事件
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //服务端启动器，这里没有启动
            ServerBootstrap b = new ServerBootstrap();
            //将两个EventLoopGroup放入启动器中
            //bossGroup设置为父类AbstractBootstrap中的EventLoopGroup
            //workerGroup设置为ServerBootstrap中的EventLoopGroup childGroup
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) //生成一个ReflectiveChannelFactory
                    .handler(new LoggingHandler(LogLevel.INFO))  //设置成父类AbstractBootstrap中的handler
                    //设置成ServerBootstrap中的childHandler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(handler);
                        }
                    });

            // 绑定端口等待连接
            ChannelFuture f = b.bind(PORT).sync();

            // 一直等到socket关闭
            // 停止服务器.
            f.channel().closeFuture().sync();
        } finally {
            //优雅关闭
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
