/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import org.clank.support.NativePointer;
import org.clank.support.aliases.char$ptr;
import org.llvm.support.MemoryBuffer;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vkvashin
 */
class ClankMemoryBufferImpl extends MemoryBuffer {

    public static ClankMemoryBufferImpl create(FileObject fo) throws IOException {
        InputStream is = null;
        try {
            if (fo.getSize() >= Integer.MAX_VALUE) {
                throw new IOException("Can't read file: " + fo.getPath() + ". The file is too long: " + fo.getSize());
            }
            int sz = (int) fo.getSize();
            byte[] array = new byte[sz + 1]; // reserve 1 byte for trailing zero
            is = fo.getInputStream();
            for (int i = 0; i < sz; i++) {
                int read = is.read();
                if (read < 0) {
                    break; // TODO: what should we do here?
                } else {
                    array[i] = (byte) read;
                }
            }
            char$ptr start = NativePointer.create_char$ptr(array);
            char$ptr end = start.$add(sz);
            return new ClankMemoryBufferImpl(start, end, true);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static ClankMemoryBufferImpl create(APTFileBuffer aptBuf) throws IOException {
        char[] chars = aptBuf.getCharBuffer();
        CharBuffer cb = CharBuffer.wrap(chars);
        ByteBuffer bb = getUTF8Charset().encode(cb);
        // we need to add a trailing zero
        byte[] array;
        if (bb.limit() < bb.capacity()) {
            int pos = bb.limit();
            bb.limit(bb.limit() + 1);
            bb.position(pos);
            bb.put((byte) 0);
            array = bb.array();
        } else {
            array = new byte[bb.limit() + 1];
            System.arraycopy(bb.array(), bb.position(), array, 0, bb.limit());
            array[bb.limit()] = 0;
        }
        char$ptr start = NativePointer.create_char$ptr(array);
        char$ptr end = start.$add(bb.limit());
        return new ClankMemoryBufferImpl(start, end, true);
    }

    private ClankMemoryBufferImpl(char$ptr start, char$ptr end, boolean RequiresNullTerminator) {
        super();
        init(start, end, RequiresNullTerminator);
    }

    @Override
    public BufferKind getBufferKind() {
        return BufferKind.MemoryBuffer_Malloc;
    }
    private static volatile Charset UTF8Charset = null;

    private static Charset getUTF8Charset() {
        if (UTF8Charset == null) {
            UTF8Charset = Charset.forName("UTF-8"); //NOI18N
        }
        return UTF8Charset;
    }
}
