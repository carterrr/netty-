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
package io.netty.handler.codec.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * compact  压缩序列化 占用空间比ObjectOutputStream小
 */
class CompactObjectOutputStream extends ObjectOutputStream {

    static final int TYPE_FAT_DESCRIPTOR = 0;
    static final int TYPE_THIN_DESCRIPTOR = 1;

    CompactObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // 重写该方法
        // 对比JDK的 ObjectOutputStream() 方法
        // protected void writeStreamHeader() throws IOException {
        //        bout.writeShort(STREAM_MAGIC);
        //        bout.writeShort(STREAM_VERSION);
        //    }
        // 少了一个STREAM_MAGIC  节省了一个魔数的空间
        writeByte(STREAM_VERSION);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class<?> clazz = desc.forClass();
        if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() ||
            desc.getSerialVersionUID() == 0) {
            write(TYPE_FAT_DESCRIPTOR);
            super.writeClassDescriptor(desc);
        } else {
            // 比较jdk  去掉了元信息  类属性信息
            //  jdk元信息：
            // out.writeShort(fields.length);
            //        for (int i = 0; i < fields.length; i++) {
            //            ObjectStreamField f = fields[i];
            //            out.writeByte(f.getTypeCode());
            //            out.writeUTF(f.getName());
            //            if (!f.isPrimitive()) {
            //                out.writeTypeString(f.getTypeString());
            //            }
            //        }
            write(TYPE_THIN_DESCRIPTOR);
            // 类名用于反序列化  必须要  因为已经没有类属性信息  这里可以反射读取
            writeUTF(desc.getName());
        }
    }
}
