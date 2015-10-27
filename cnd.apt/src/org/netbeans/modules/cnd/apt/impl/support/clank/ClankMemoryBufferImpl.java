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
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vkvashin
 */
final class ClankMemoryBufferImpl extends MemoryBuffer {
    // for remote paths it is url; for locals it is normalized system absolute path
    private final char$ptr fileUrl;

    public static ClankMemoryBufferImpl create(FileObject fo, CharSequence foURL) throws IOException {
        InputStream is = null;
        try {
            if (fo.getSize() >= Integer.MAX_VALUE) {
                throw new IOException("Can't read file: " + fo + ". The file is too long: " + fo.getSize()); // NOI18N
            }
            String text = fo.asText();
            assert foURL.toString().contentEquals(CndFileSystemProvider.toUrl(FSPath.toFSPath(fo))) : foURL + " vs. " + CndFileSystemProvider.toUrl(FSPath.toFSPath(fo));
            return create(foURL, text.toCharArray()); // not optimal at all... but will dye quite soon
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    public static ClankMemoryBufferImpl create(CharSequence url, char[] chars) throws IOException {
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
        return new ClankMemoryBufferImpl(url, start, end, true);
    }    

    private ClankMemoryBufferImpl(CharSequence url, char$ptr start, char$ptr end, boolean RequiresNullTerminator) {
        super();
        this.fileUrl = NativePointer.create_char$ptr(url);
        init(start, end, RequiresNullTerminator);
    }

    @Override
    public char$ptr getBufferIdentifier() {
        return fileUrl;
    }

    @Override
    public BufferKind getBufferKind() {
        return BufferKind.MemoryBuffer_Malloc;
    }
}
