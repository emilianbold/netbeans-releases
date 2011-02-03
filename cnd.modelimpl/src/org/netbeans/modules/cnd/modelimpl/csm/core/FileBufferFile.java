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


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.*;
import java.lang.ref.SoftReference;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileBufferFile extends AbstractFileBuffer {
    
    private volatile SoftReference<char[]> cachedArray;
    private volatile long lastModifiedWhenCachedString;

    public FileBufferFile(FileObject fileObject) {
        super(fileObject);
    }
    
    @Override
    public String getText() throws IOException {
        char[] buf = doGetChar();
        return new String(buf, 0, buf.length);
    }
    
    @Override
    public String getText(int start, int end) {
        try {
            char[] buf = doGetChar();
            if( end > buf.length ) {
                new IllegalArgumentException("").printStackTrace(System.err); // NOI18N
                end = buf.length;
            }
            return new String(buf, start, end - start);
        } catch( IOException e ) {
            DiagnosticExceptoins.register(e);
            return ""; // NOI18N
        }
    }

    private synchronized char[] doGetChar() throws IOException {
        SoftReference<char[]> aCachedArray = cachedArray;
        if (aCachedArray != null) {
            char[] res = aCachedArray.get();
            if (res != null) {
                if (lastModifiedWhenCachedString == lastModified()) {
                    return res;
                }
            }
        }
        FileObject fo = getFileObject();
        long length = fo.getSize();
        if (length > Integer.MAX_VALUE) {
            new IllegalArgumentException("File is too large: " + fo.getPath()).printStackTrace(System.err); // NOI18N
        }
        char[] readChars = new char[(int)length+1];
        InputStream is = getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, getEncoding()));
        try {
            String line;
            int position = 0;
            while((line = reader.readLine())!= null) {
                for(int i = 0; i < line.length(); i++) {
                    readChars[position++] = line.charAt(i);
                }
                readChars[position++] = '\n'; // NOI18N
            }
            char[] copyChars = new char[position];
            System.arraycopy(readChars, 0, copyChars, 0, position);
            readChars = copyChars;
        } finally {
            reader.close();
            is.close();
        }
        cachedArray = new SoftReference<char[]>(readChars);
        lastModifiedWhenCachedString = lastModified();
        return readChars;
    }

    private InputStream getInputStream() throws IOException {
        InputStream is;
        FileObject fo = getFileObject();
        if (fo != null && fo.isValid()) {
            is = fo.getInputStream();
        } else {
            throw new FileNotFoundException("Null file object for " + this.getAbsolutePath()); // NOI18N
        }
        return new BufferedInputStream(is, TraceFlags.BUF_SIZE);
    }
    
    @Override
    public boolean isFileBased() {
        return true;
    }
    
    @Override
    public long lastModified() {
	return getFileObject().lastModified().getTime();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public FileBufferFile(DataInput input) throws IOException {
        super(input);
    }

    @Override
    public char[] getCharBuffer() throws IOException {
        return doGetChar();
    }
}
