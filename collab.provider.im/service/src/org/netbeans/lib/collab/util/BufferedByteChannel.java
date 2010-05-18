/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.util;


import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.channels.*;
import java.nio.*;

import org.apache.log4j.*;

/**
 * This class provides a way to buffer the writes.  This allows code
 * that calls it to more or less assume that the write is complete
 * and not block at the same time.
 *
 * Assumes that the selection key does not change.
 *
 * @author Jacques Belissent
 * @author Vijayakumar Palaniappan
 *
 */
public class BufferedByteChannel implements ByteChannel
{

    private ByteBuffer _OutBuffer;

    private Object lockObject = new Object();
    private ByteChannel sc;
    private SelectWorker _selector;
    private Object _selection = null;

    // Bytes read from the sc
    private int bytesRead = 0;
    // Bytes written to the _OutBuffer
    private int bytesWritten = 0;


    private static Logger logger = SelectWorker.getLogger();

    public BufferedByteChannel(ByteChannel sc,
                               SelectWorker selector)
    {
        this(sc, selector, 16386);
    }

    public BufferedByteChannel(ByteChannel sc,
                               SelectWorker selector, 
                               int capacity)
    {
        this.sc = sc;
        _selector = selector;
        _OutBuffer = ByteBuffer.allocateDirect(capacity);
    }

    protected void setSelectionKey(Object key)
    {
        _selection = key;
    }

    // Returns the number of bytes read from the ByteChannel since the read count is last reset.
    public int getReadCount()
    {
        return bytesRead;
    }

    // Resets the read count to zero.
    public void resetReadCount()
    {
        bytesRead = 0;
    }

    // Returns the number of bytes written to the ByteBuffer since the written count is last reset.
    public int getWrittenCount() 
    {
        return bytesWritten;
    }

    // Resets the writtenCount to zero.
    public void resetWrittenCount()
    {
        bytesWritten = 0;
    }


    public int read(ByteBuffer dst) throws IOException {
        if (!sc.isOpen()) throw new EOFException("Channel already closed");
    
        int len = sc.read(dst); 
        if (logger != null) {
            logger.debug("BufferedByteChannel[" + sc +"]: nread=" + len + " space=" + dst.remaining());
        }

        if (len >= 0) {
	    // this has no effect if another read is in progress.
	    //VIJAY: This is needed when read is called by a non-worker thread 
	    //specifically JSO sendAndWatch case.
	    _selector.interestOps(_selection, SelectionKey.OP_READ);

	    if (len > 0) bytesRead += len;

	} else {
	    throw new IOException("BufferedByteChannel[" + sc +"] read failed");
	}

        return len;
    }

    public void close() throws IOException { 
        // this will be done by first cancelling the key
        // sc.close();
        _selector.cancel(_selection);
    }

    public boolean isOpen() { return sc.isOpen(); }

    public int write(ByteBuffer src) throws IOException 
    { 
        if (!sc.isOpen()) throw new EOFException("Channel already closed");
        
        int len = 0;
        synchronized (lockObject) {
            //boolean queue = _OutBuffer.position() ==  0;
            _OutBuffer.limit(_OutBuffer.capacity());
            len = src.remaining();
            int destRemaining = _OutBuffer.remaining();
            if (len > destRemaining) {
                if(destRemaining > 0) {
                    len = destRemaining;
                    _OutBuffer.put(src.array(), src.position(), len);
                    src.position(src.position() + len);
                } else {
                    System.out.println("BufferedByteChannel[" + sc +"] : OVERFLOW: remaining=" + _OutBuffer.remaining() + " requested=" + len + " position=" + _OutBuffer.position() + " limit=" + _OutBuffer.limit());
                    
                    // Not sleeping - we will wait for the caller to do the
                    // lazy work (aka sleep)
                    //long start = System.currentTimeMillis();
                    try {
                        //To handle to thread starvation case
                        lockObject.wait(2000);
                    } catch(InterruptedException e) {
                    }
                    len = 0;
                    //System.out.println("Wait time " + (System.currentTimeMillis() - start));
                }
            } else {
                _OutBuffer.put(src);
            }
            //if(queue) {
                if (writeNow() > 0) {
                    _selector.interestOps(_selection, SelectionKey.OP_WRITE);
                }
            //}
        }

        //System.out.println("BufferedByteChannel: after put len=" + len + " , buffer size=" + _OutBuffer.position());

        bytesWritten += len;
        return len;
    }

    /**
     * @return number of bytes pending write
     */
    public int writeNow() throws IOException
    {
        synchronized (lockObject){
            //System.out.println("BufferedByteChannel: before flip buffer size=" + _OutBuffer.position());
            if (_OutBuffer.position() == 0) return 0;

            // flip buffer for writing
            _OutBuffer.flip();
            try {
                int amt = sc.write(_OutBuffer);
            } finally {
                lockObject.notifyAll();
            }
            //System.out.println("BufferedByteChannel: wrote " + amt + " bytes to " + sc + " remaining=" + _OutBuffer.remaining());

            // flip back for appending
            _OutBuffer.compact();

            //System.out.println("BufferedByteChannel: after flip buffer size=" + _OutBuffer.position() + " limit=" + _OutBuffer.limit());

            return _OutBuffer.position();
        }
    }

    public String toString() {
        return sc.toString();
    }

}
