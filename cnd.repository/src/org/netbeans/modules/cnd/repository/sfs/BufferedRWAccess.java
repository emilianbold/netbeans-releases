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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.UnitCodec;

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
	
        @Override
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
    
    private RandomAccessFile randomAccessFile;
    protected FileChannel channel;
    private ByteBuffer writeBuffer;
    private int bufSize;
    private final UnitCodec unitCodec;
    
    public BufferedRWAccess(File file, UnitCodec unitCodec) throws IOException {
        this.unitCodec = unitCodec;
	this.bufSize = Stats.bufSize > 0 ? Stats.bufSize : 32*1024;
        File parent = new File(file.getParent());
        
        if (!parent.exists()) {
            parent.mkdirs();
        }
        
	randomAccessFile = new RandomAccessFile(file, "rw"); // NOI18N
	channel = randomAccessFile.getChannel();
	ByteBuffer.allocateDirect(bufSize);
    }
        
    @Override
    public Persistent read(PersistentFactory factory, long offset, int size) throws IOException {
	try {
	    ByteBuffer buffer = getReadBuffer(size);
	    channel.read(buffer, offset);
	    buffer.flip();
	    RepositoryDataInput in = new BufferDataInput(buffer, unitCodec);
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
    
    @Override
    public int write(PersistentFactory factory, Persistent object, long offset) throws IOException {
	channel.position(offset);
	ByteBufferOutputStream bos = new ByteBufferOutputStream(getWriteBuffer());
	RepositoryDataOutput out = new RepositoryDataOutputStream(bos, unitCodec);
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
    
    @Override
    public long size() throws IOException {
	return channel.size();
    }
    
    @Override
    public void truncate(long size) throws IOException {
	channel.truncate(size);
	channel.position(size);
    }
    
    @Override
    public void move(long offset, int size, long newOffset) throws IOException {
	ByteBuffer buffer = getReadBuffer(size);
	channel.read(buffer, offset);
	buffer.flip();
	channel.write(buffer, newOffset);
    }
    
    @Override
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
    
    
    @Override
    public void close() throws IOException {
	channel.close();
    }

    @Override
    public FileDescriptor getFD() throws IOException {
	return randomAccessFile.getFD();
    }

}
