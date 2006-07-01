/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.core.output2;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Heap based implementation of the Storage interface, over a byte array.
 *
 */
class HeapStorage implements Storage {
    private boolean closed = true;
    private byte[] bytes = new byte[2048];
    private int size = 0;

    public Storage toFileMapStorage() throws IOException {
        FileMapStorage result = new FileMapStorage();
        result.write(getReadBuffer(0, size), false);
        return result;
    }

    public ByteBuffer getReadBuffer(int start, int length) throws IOException {
        return ByteBuffer.wrap(bytes, start, Math.min(length, bytes.length - start));
    }

    public ByteBuffer getWriteBuffer(int length) throws IOException {
        return ByteBuffer.allocate(length);
    }

    public synchronized int write(ByteBuffer buf, boolean addNewLine) throws IOException {
        closed = false;
        int oldSize = size;
        size += buf.limit() + ( addNewLine ? OutWriter.lineSepBytes.length : 0);
        if (size > bytes.length) {
            byte[] oldBytes = bytes;
            bytes = new byte[Math.max (oldSize * 2, (buf.limit() * 2) + oldSize)]; 
            System.arraycopy (oldBytes, 0, bytes, 0, oldSize);
        }
        buf.flip();
        buf.get(bytes, oldSize, buf.limit());
        if (addNewLine) {
            System.arraycopy (OutWriter.lineSepBytes, 0, bytes, size - OutWriter.lineSepBytes.length, OutWriter.lineSepBytes.length);
        }
        return oldSize;
    }

    public synchronized void dispose() {
        bytes = new byte[0];
        size = 0;
    }

    public synchronized int size() {
        return size;
    }

    public void flush() throws IOException {
        //N/A
    }

    public void close() throws IOException {
        closed = true;
    }

    public boolean isClosed() {
        return closed;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
