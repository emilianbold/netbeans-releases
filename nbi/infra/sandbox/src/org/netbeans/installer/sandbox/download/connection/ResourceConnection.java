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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.netbeans.installer.sandbox.download.Download;
import org.netbeans.installer.utils.exceptions.InitializationException;

/**
 *
 * @author Kirill Sorokin
 */
public class ResourceConnection extends Connection {
    private String name;
    private ClassLoader classLoader;
    
    private InputStream inputStream;
    
    public ResourceConnection(String aName, ClassLoader aClassLoader) throws InitializationException {
        if (aName != null) {
            name = aName;
        } else {
            throw new InitializationException("The supplied resource name cannot be null.");
        }
        
        if (aClassLoader != null) {
            classLoader = aClassLoader;
        } else {
            classLoader = getClass().getClassLoader();
        }
    }
    
    public void open() throws IOException {
        assert inputStream == null;
        
        try {
            inputStream = classLoader.getResource(name).openStream();
        } catch (NullPointerException e) {
            throw new FileNotFoundException();
        }
    }
    
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
    
    public int read(byte[] buffer) throws IOException {
        assert inputStream != null;
        
        return inputStream.read(buffer);
    }
    
    public int available() throws IOException {
        return inputStream.available();
    }
    
    public boolean supportsRanges() {
        return false;
    }
    
    public long getContentLength() {
        return Download.UNDEFINED_LENGTH;
    }
    
    public Date getModificationDate() {
        return new Date();
    }
}
