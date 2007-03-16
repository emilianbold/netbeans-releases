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

/**
 * ByteBuffer based DataInput implementation
 * @author Vladimir Kvashin
 */
public class BufferDataInput implements DataInput {
    
    public static class NotImplementedException extends Error {
	public NotImplementedException() {
	    super("Not implemented"); // NOI18N
	}
    }
    
    private ByteBuffer buffer;
    
    public BufferDataInput(ByteBuffer buffer) {
	this.buffer = buffer;
    }

    public byte readByte() throws IOException {
	return buffer.get();
    }
    
    public char readChar() throws IOException {
	return buffer.getChar();
    }
    
    public int readInt() throws IOException {
	return buffer.getInt();
    }
    
    public int readUnsignedShort() throws IOException {
	short result = buffer.getShort();
	return ((int) result) & 0x0000FFFF;
    }
    
    public boolean readBoolean() throws IOException {
	return buffer.get() != 0;
    }
    
    
    public double readDouble() throws IOException {
	return buffer.getDouble();
    }
    
    public float readFloat() throws IOException {
	return buffer.getFloat();
    }
    
    
    public String readLine() throws IOException {
	StringBuilder sb = new StringBuilder();
	byte b;
	read: while( buffer.hasRemaining() ) {
	    switch( b = buffer.get() ) {
		case '\r':
		    if( buffer.hasRemaining() ) {
			byte next = buffer.get();
			if( next != '\n') {
			    buffer.position(buffer.position()-1);
			}
		    }
		    break read;
		case '\n':
		    break read;
		default:
		    sb.append((char) b);
		    break;
	    }
	}
	return sb.toString();
    }
    
    public long readLong() throws IOException {
	return buffer.getLong();
    }
    
    public short readShort() throws IOException {
	return buffer.getShort();
    }
    
    public String readUTF() throws IOException {
	return UTF.readUTF(this);
    }
    
    public int readUnsignedByte() throws IOException {
	byte b = buffer.get();
	return ((short) b) & 0x00FF;
    }
    
    public int skipBytes(int n) throws IOException {
	int skip = Math.min(n, buffer.remaining());
	buffer.position(buffer.position() + skip);
	return skip;
    }
    
    public void readFully(byte[] b, int off, int len) throws IOException {
	buffer.get(b, off, len);
    }
    
    public void readFully(byte[] b) throws IOException {
	buffer.get(b);
    }
}
