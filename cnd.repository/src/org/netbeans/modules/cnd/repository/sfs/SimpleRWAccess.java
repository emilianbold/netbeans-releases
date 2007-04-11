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

/**
 * An implementation of FileRWAccess
 * @author Vladimir Kvashin
 */
public class SimpleRWAccess implements FileRWAccess {
    
    private File file;
    private RandomAccessFile randomAccessFile;
    private Object lock = new Object();

    public SimpleRWAccess(File file) throws IOException {
	this.file = file;
	randomAccessFile = new RandomAccessFile(file, "rw"); // NOI18N
    }

    public long size() throws IOException {
	return randomAccessFile.length();
    }

    public void truncate(long size) throws IOException {
	randomAccessFile.setLength(size);
    }
    
    public void move(long offset, int size, long newOffset) throws IOException {
	byte[] buffer = new byte[size];
	synchronized( lock ) {
	    randomAccessFile.seek(offset);
	    randomAccessFile.read(buffer);
	    randomAccessFile.seek(newOffset);
	    randomAccessFile.write(buffer);
	}
    }
    
    public void move(FileRWAccess from, long offset, int size, long newOffset) throws IOException {
	if( ! (from instanceof  SimpleRWAccess) ) {
	    throw new IllegalArgumentException("Illegal class to move from: " + from.getClass().getName()); // NOI18N
	}
	SimpleRWAccess from2 = (SimpleRWAccess) from;
	byte[] buffer = new byte[size];
	from2.randomAccessFile.seek(offset);
	from2.randomAccessFile.read(buffer);
	randomAccessFile.seek(newOffset);
	randomAccessFile.write(buffer);
    }
    
    
    public void close() throws IOException {
	randomAccessFile.close();
    }
    
    public int write(PersistentFactory factory, Persistent object, long offset) throws IOException {
	synchronized( lock ) {
	    randomAccessFile.seek(offset);
	    factory.write(randomAccessFile, object);
	    return (int) (randomAccessFile.getFilePointer() - offset);
	}
    }
    
    
    public Persistent read(PersistentFactory factory, long offset, int size) throws IOException {
	synchronized( lock ) {
	    randomAccessFile.seek(offset);
	    return factory.read(randomAccessFile);
	}
    }
    
    public FileDescriptor getFD() throws IOException {
	return randomAccessFile.getFD();
    }
}
