/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/**
 * An implementation of FileRWAccess
 * @author Vladimir Kvashin
 */
public class SimpleRWAccess implements FileRWAccess {
    
    private RandomAccessFile randomAccessFile;
    private static final class Lock {}
    private final Object lock = new Lock();

    public SimpleRWAccess(File file) throws IOException {
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
