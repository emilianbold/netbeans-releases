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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Date;
import org.netbeans.installer.sandbox.download.DownloadOptions;
import org.netbeans.installer.utils.exceptions.InitializationException;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class Connection {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int DEFAULT_CONNECTION_TIMEOUT = 3000;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    protected static int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    
    public static Connection getConnection(URI uri, long offset, long length, 
            DownloadOptions options) throws InitializationException {
        try {
            if (uri.getScheme().equals("http")) {
                return new HTTPConnection(uri.toURL(), offset, length, options);
            }
            if (uri.getScheme().equals("file")) {
                return new FileConnection(new File(uri));
            }
            if (uri.getScheme().equals("resource")) {
                return new ResourceConnection(uri.getSchemeSpecificPart(), 
                        (ClassLoader) options.get(DownloadOptions.CLASSLOADER));
            }
        } catch (MalformedURLException e) {
            throw new InitializationException("Cannot convert the " +
                    "supplied URI to an URL, which is required for connections " +
                    "of this type.", e);
        }
        
        throw new InitializationException("Cannot initialize " +
                "connection, unknown URI scheme - " + uri.getScheme());
    }
    
    public static int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public static void setConnectionTimeout(int aConnectionTimeout) {
        connectionTimeout = aConnectionTimeout;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract void open() throws IOException;
    
    public abstract void close() throws IOException;
    
    public abstract int read(byte[] buffer) throws IOException;
    
    public abstract int available() throws IOException;
    
    public abstract boolean supportsRanges();
    
    public abstract long getContentLength();
    
    public abstract Date getModificationDate();
}
