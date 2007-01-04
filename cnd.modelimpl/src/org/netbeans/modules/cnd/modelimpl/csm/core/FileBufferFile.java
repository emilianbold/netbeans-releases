/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.*;
import java.lang.ref.SoftReference;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileBufferFile implements FileBuffer {

    private File file;
    private SoftReference bytes;

    public FileBufferFile(File file) {
        this.file = file;
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    public String getText() throws IOException {
        return new String(getBytes());
    }

    public String getText(int start, int end) {
        try {
            byte[] b = getBytes();
            if( end > b.length ) {
                new IllegalArgumentException("").printStackTrace(System.err);
                end = b.length;
            }
            return new String(b, start, end-start);
        }
        catch( IOException e ) {
            e.printStackTrace(System.err);
            return "";
        }
    }
    
    public synchronized byte[] getBytes() throws IOException {
        byte[] b;
        if( bytes != null  ) {
            Object o = bytes.get();
            if( o != null ) {
                return (byte[]) o;
            }
        }
        // either bytes == null or bytes.get() == null
        b = doGetBytes();
        bytes = new SoftReference(b);
        return b;
    }

    private byte[] doGetBytes() throws IOException {
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            new IllegalArgumentException("File is too large: " + file.getAbsolutePath()).printStackTrace(System.err);
        }
        byte[] bytes = new byte[(int)length];
        InputStream is = new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);
        try {
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }
        return bytes;
    }
    

    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(file), TraceFlags.BUF_SIZE);
    }

    public File getFile() {
        return file;
    }
    
    public int getLength() {
        return (int) file.length();
    }    

    public boolean isFileBased() {
        return true;
    }
}
