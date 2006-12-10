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
 *  
 * $Id$
 */
package org.netbeans.installer.sandbox.download.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author Kirill Sorokin
 */
public class FileConnection extends Connection {
    private File file;
    
    private InputStream inputStream;
    
    public FileConnection(File aFile) {
        file   = aFile;
    }
    
    public void open() throws IOException {
        inputStream = new FileInputStream(file);
    }
    
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
    
    public int read(byte[] buffer) throws IOException {
        return inputStream.read(buffer);
    }
    
    public int available() throws IOException {
        return inputStream.available();
    }
    
    public boolean supportsRanges() {
        return false;
    }
    
    public long getContentLength() {
        return file.length();
    }
    
    public Date getModificationDate() {
        return new Date(file.lastModified());
    }
}
