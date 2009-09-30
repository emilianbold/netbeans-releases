/**************************************************************************
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Software is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2001.
 * All Rights Reserved.
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
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;


import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;


/**
 *	<p>InputStream for files read from the "HTTP Javadoc Viewer" file system.  This
 *	class merely wraps the InputStream object that comes from the HttpURLConnection
 *	object, and calls the HttpURLConnection.dicsonnect() method when the InputStream
 *	object is closed.</p>
 *
 *	@since 1.0
 */
class HTTPFileInputStream extends InputStream {
    
    // Connection object to the web server
    private HttpURLConnection	fileConnection;
    // Input stream that is wrapped by this class
    private InputStream fileInputStream;
        
    /**
     *	Constructs an <code>HTTPFileInputStream</code> object created from the
     *	connection object passed to the constructor.
     *
     *	@param FileConnection URLConnection object from which to obtain the wrapped
     *		InputStream.
     *
     *	@since 1.0
     */
    HTTPFileInputStream( HttpURLConnection fileConnection ) throws IOException {
        
        this.fileConnection = fileConnection;
        this.fileInputStream = this.fileConnection.getInputStream( );        
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#available()}.
     */
    public int available() throws IOException {
        
        return fileInputStream.available( );
    }
    
    
    /**
     *	Pass-through method for {@link InputStream#close()}.  It also disconnects from
     *	the web server.
     */
    public void close( ) throws IOException {
        
        // Disconnect from the web server when the InputStream is closed
        fileInputStream.close( );
        fileConnection.disconnect( );
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#mark(int)}.
     */
    public void mark( int param ) {
        
        fileInputStream.mark( param );
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#markSupported()}.
     */
    public boolean markSupported( ) {
        
        return fileInputStream.markSupported( );
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#read()}.
     */
    public int read( ) throws IOException {
        
        return fileInputStream.read( );        
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#read(byte[])}.
     */
    public int read( byte[] values ) throws IOException {
        
        return fileInputStream.read( values );        
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#read(byte[],int,int)}.
     */
    public int read( byte[] values, int off, int len ) throws IOException {
        
        return fileInputStream.read( values, off, len );        
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#reset()}.
     */
    public void reset( ) throws IOException {
        
        fileInputStream.reset( );        
    }
    
    
    /**
     *	Pass-through method for {@link java.io.InputStream#skip(long)}.
     */
    public long skip(long param ) throws IOException {
        
        return fileInputStream.skip( param );        
    }    
}
