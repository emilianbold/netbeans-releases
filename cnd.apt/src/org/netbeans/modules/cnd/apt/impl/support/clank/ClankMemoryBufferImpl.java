/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.io.InputStream;
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
    private final char$ptr fileName;

    public static ClankMemoryBufferImpl create(FileObject fo) throws IOException {
        InputStream is = null;
        try {
            if (fo.getSize() >= Integer.MAX_VALUE) {
                throw new IOException("Can't read file: " + fo.getPath() + ". The file is too long: " + fo.getSize()); // NOI18N
            }
            String text = fo.asText();
            return create(fo.getPath(), text.toCharArray()); // not optimal at all... but will dye quite soon
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static ClankMemoryBufferImpl create(APTFileBuffer aptBuf) throws IOException {
        char[] chars = aptBuf.getCharBuffer();
        return create(aptBuf.getAbsolutePath(), chars);
    }
    
    public static ClankMemoryBufferImpl create(CharSequence fileName, char[] chars) throws IOException {
        int nullTermIndex = chars.length;
        byte[] array = new byte[nullTermIndex+1];
        for (int i = 0; i < nullTermIndex; i++) {
            char c = chars[i];
            if (c > 127) {
                // convert all non ascii to spaces
                array[i] = ' ';
            } else {
                array[i] = (byte)c;
            }
        }
        array[nullTermIndex] = 0;
        char$ptr start = NativePointer.create_char$ptr(array);
        char$ptr end = start.$add(nullTermIndex);
        return new ClankMemoryBufferImpl(fileName, start, end, true);
    }    

    private ClankMemoryBufferImpl(CharSequence fileName, char$ptr start, char$ptr end, boolean RequiresNullTerminator) {
        super();
        this.fileName = NativePointer.create_char$ptr(fileName);
        init(start, end, RequiresNullTerminator);
    }

    @Override
    public char$ptr getBufferIdentifier() {
        return fileName;
    }

    @Override
    public BufferKind getBufferKind() {
        return BufferKind.MemoryBuffer_Malloc;
    }
}
