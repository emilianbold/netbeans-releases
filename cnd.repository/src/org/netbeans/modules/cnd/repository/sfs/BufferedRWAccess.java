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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * 
 * @author Vladimir Kvashin
 */
public class BufferedRWAccess implements FileRWAccess {
    
    private class ByteBufferOutputStream extends OutputStream {
	
	private int oldPosition;
	private int flushed = 0;
	private ByteBuffer buffer;
	
	public ByteBufferOutputStream(ByteBuffer buffer) {
	    this.buffer = buffer;
	    oldPosition = buffer.position();
	}
	
	public void write(int b) throws IOException {
	    if( buffer.remaining() <= 0 ) {
		flushed += buffer.position();
		writeBuffer();
	    }
	    buffer.put((byte) b);
	}
	
	private int count() {
	    return flushed + buffer.position() - oldPosition;
	}
    }
    
    private File file;
    private RandomAccessFile randomAccessFile;
    protected FileChannel channel;
    private ByteBuffer writeBuffer;
    private int bufSize;
    
    public BufferedRWAccess(File file) throws IOException {
	this.file = file;
	this.bufSize = Stats.bufSize > 0 ? Stats.bufSize : 32*1024;
	randomAccessFile = new RandomAccessFile(file, "rw"); // NOI18N
	channel = randomAccessFile.getChannel();
	writeBuffer.allocateDirect(bufSize);
    }
        
    public Persistent read(PersistentFactory factory, long offset, int size) throws IOException {
	try {
	    ByteBuffer buffer = getReadBuffer(size);
	    channel.read(buffer, offset);
	    buffer.flip();
	    DataInput in = new BufferDataInput(buffer);
	    return factory.read(in);
	}
	catch( BufferOverflowException e ) {
	    e.printStackTrace(System.err);
	    throw e;
	}
	catch( BufferUnderflowException e ) {
	    e.printStackTrace(System.err);
	    throw e;
	}
    }
    
    public int write(PersistentFactory factory, Persistent object, long offset) throws IOException {
	channel.position(offset);
	ByteBufferOutputStream bos = new ByteBufferOutputStream(getWriteBuffer());
	DataOutput out = new DataOutputStream(bos);
	factory.write(out, object);
	int count = bos.count();
	writeBuffer();
	return count;
    }
    
    // TODO: handle possible buffer overflow 
    // (for now we just allocate large buffer and hope that it will never ovrflow
    protected ByteBuffer getWriteBuffer() {
	if( writeBuffer == null ) {
	    writeBuffer = ByteBuffer.allocateDirect(bufSize);
	}
	return writeBuffer;
    }
    
    // TODO: optimize buffer allocation
    protected ByteBuffer getReadBuffer(int size) {
	ByteBuffer buffer = ByteBuffer.allocate(size);
	return buffer;
    }
    
    protected void writeBuffer() throws IOException {
	writeBuffer.flip();
	channel.write(writeBuffer);
	writeBuffer.clear();
    }
    
    public long size() throws IOException {
	return channel.size();
    }
    
    public void truncate(long size) throws IOException {
	channel.truncate(size);
	channel.position(size);
    }
    
    public void move(long offset, int size, long newOffset) throws IOException {
	ByteBuffer buffer = getReadBuffer(size);
	channel.read(buffer, offset);
	buffer.flip();
	channel.write(buffer, newOffset);
    }
    
    public void move(FileRWAccess from, long offset, int size, long newOffset) throws IOException {
	if( ! (from instanceof  BufferedRWAccess) ) {
	    throw new IllegalArgumentException("Illegal class to move from: " + from.getClass().getName()); // NOI18N
	}
	BufferedRWAccess from2 = (BufferedRWAccess) from;
	ByteBuffer buffer = getReadBuffer(size);
	from2.channel.read(buffer, offset);
	buffer.flip();
	channel.write(buffer, newOffset);
    }
    
    
    public void close() throws IOException {
	channel.close();
    }

    public FileDescriptor getFD() throws IOException {
	return randomAccessFile.getFD();
    }

}
