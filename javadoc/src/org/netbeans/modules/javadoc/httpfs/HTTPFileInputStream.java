/**************************************************************************
 *
 *                          Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the HTTP Javadoc Filesystem.
 * The Initial Developer of the Original Code is Jeffrey A. Keyser.
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2001.
 * All Rights Reserved.
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
