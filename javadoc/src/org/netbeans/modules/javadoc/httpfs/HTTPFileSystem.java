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

import java.io.*;
import java.beans.*;
import java.net.*;
import java.util.StringTokenizer;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem


/**
 *	<p>Implemets the "HTTP Javadoc Filesystem" bean.</p>
 *
 *	@since 1.0
 */
public class HTTPFileSystem extends FileSystem implements VetoableChangeListener {

    /**
     *	Property name for the URL of the file system.
     *
     *	@since 1.0
     */
    public static final String  PROP_URL = "URL";   //NOI18N
    private static final long   serialVersionUID = 200104;
    // Default URL to use for a new filesystem
    private static final String DEFAULT_URL = "http://www.netbeans.org/download/apis/"; // NO I18N
    
    
    // URL to the Javadocs
    transient URL               baseURL;
    // Root file object for the mounted filesystem
    transient HTTPFileObject    rootFileObject;
            
    /**
     *	Constructs a <code>HTTPFileSystem</code> file system bean.
     *
     *	@since 1.0
     */
    public HTTPFileSystem() {
        
        // The default capabilities of this file system
        FileSystemCapability.Bean   httpFileSystemCapabilities;
        
        
        // Set the capabilies to "Documentation" only
        httpFileSystemCapabilities = new FileSystemCapability.Bean( );
        httpFileSystemCapabilities.setDoc( true );
        httpFileSystemCapabilities.setExecute( false );
        httpFileSystemCapabilities.setCompile( false );
        httpFileSystemCapabilities.setDebug( false );        
        setCapability( httpFileSystemCapabilities );
        setHidden( true );
        addVetoableChangeListener( this );
        
        try{
            
            // Set a known URL as the default
            setURL( DEFAULT_URL );  //NOI18N
            
        } catch( IOException e ) {
            
            // I have no idea what else to do if this happens!
            e.printStackTrace( );                

        }
        
    }
    
    
    /**
     *	Writes this object when it is serialized.
     *
     *	@param out Serialization output stream.
     *
     *	@since 1.0
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        
        out.writeObject( baseURL.toString( ) );
        
    }
    
    
    /**
     *	Reads this object when it is unserialized.
     *
     *	@param in Serialization input stream.
     *
     *	@since 1.0
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        
        setURL( (String)in.readObject( ) );

    }
    
    
    /**
     *	Returns the current URL of this file system.
     *
     *	@since 1.0
     *
     *	@return URL name for this filesystem.
     *
     *	@see #setURL(URL)
     */
    public String getURL( ) {
        
        return baseURL.toString();
        
    }
    
    
    /**
     *	Sets a new URL for this file system.
     *
     *	@param newURL The URL this file system should use.
     *
     *	@throws IOException If the URL doesn't point to a web site, or if some
     *      other property listener vetos this change.
     *
     *	@since 1.0
     *
     *	@see #getURL()
     */
    public synchronized void setURL( String url ) throws IOException {        

        // Original URL of this filesystem
        URL             oldURL;
        // Original root file object of this filesystem
        HTTPFileObject  oldRootFileObject;
        
        // Save current state of the bean
        oldURL = baseURL;
        oldRootFileObject = rootFileObject;
        
        try {
            
            // Create the new root file object
            try {
                
                baseURL = new URL( url );
                
            }
            catch( java.net.MalformedURLException mlfEx ){
                
                throw new IOException( mlfEx.toString( ) );
                
            }
            rootFileObject = new HTTPFileObject( "/", this );   //NOI18N

            // Give listeners a chance to reject the URL
            fireVetoableChange( PROP_URL, oldURL != null ? oldURL.toExternalForm( ) : null, url );
            
            // Set the new name of this file system
            setSystemName( this.getClass( ).getName( ) + "/" + baseURL.toExternalForm( ) ); //NOI18N
            
        } catch( PropertyVetoException e ) {
            
            // Set bean back to previous state and rethrow this exception
            baseURL = oldURL;
            rootFileObject = oldRootFileObject;
            throw new IOException( e.getMessage( ) );

        }
        firePropertyChange( PROP_URL, oldURL != null ? oldURL.toExternalForm( ) : null, url );
        firePropertyChange( PROP_ROOT, oldRootFileObject, rootFileObject );        

    }
    
    
    /**
     *	Verifies that the URL given to this filesystem is valid.
     *
     *	@param urlChangeEvent Change request for the URL property.
     *
     *	@since 1.0
     */
    public void vetoableChange( PropertyChangeEvent urlChangeEvent ) throws PropertyVetoException {

        // New URL
        URL newURL;


        // If the property change event is this object's URL property,
        if( urlChangeEvent.getSource( ) == this && urlChangeEvent.getPropertyName( ).equals( PROP_URL ) ) {

            // Test the URL format
            try {

                newURL = new URL( (String)urlChangeEvent.getNewValue( ) );

            }
            catch( MalformedURLException mlfEx ){

                throw new PropertyVetoException( mlfEx.toString( ), urlChangeEvent );

            }
        
            // If this URL doesn't point to an HTTP server,
            if( !newURL.getProtocol( ).equals( "http" ) && !newURL.getProtocol( ).equals( "https" ) ) { //NOI18N
                
                // Reject this URL
                throw new PropertyVetoException( ResourceUtils.getBundledString( "MSG_NotHTTPProtocol" ), urlChangeEvent );    //NOI18N
                
            }
            // If this URL doesn't point to a directory,
            if( !newURL.toExternalForm( ).endsWith( "/" ) ){    // NO I18N

                // Reject this URL
                throw new PropertyVetoException( ResourceUtils.getBundledString( "MSG_NotDirectory" ), urlChangeEvent );    //NOI18N

            }
            
        }
    }
    
    
    /**
     *	Returns the root directory for the Javadocs.
     *
     *	@return Root file object of this filesystem.
     *
     *	@since 1.0
     */
    public FileObject getRoot() {
        
        return rootFileObject;

    }
    
    
    /**
     *	Provides the name of this file system to be displayed to the user, which is the
     *	URL of the Javadocs.
     *
     *	@return Name to display in the IDE for this filesystem.
     *
     *	@since 1.0
     */
    public String getDisplayName( ) {
        
        // Return the name of the URL
        return baseURL.toExternalForm( );        
    }
    
    
    /**
     *	Returns a file object by its resource name, or "null" if the file was not
     *	found.
     *
     *	@param resourceName The path of the file under the URL to return.
     *
     *	@return File object in this filesystem, or null if not found.
     *
     *	@since 1.0
     */
    public FileObject findResource(String resourceName) {
        
        // Parser to break up the path to the file
        StringTokenizer	pathParser;
        // File object to return
        HTTPFileObject	foundFileObject;
        
        
        // Pull apart the directory structure
        pathParser = new StringTokenizer( resourceName, "/" );  //NOI18N
        foundFileObject = (HTTPFileObject)getRoot( );
        
        // Walk down the path to find the requested file
        while( foundFileObject != null && pathParser.hasMoreElements( ) ) {
            
            foundFileObject = foundFileObject.child( (String)pathParser.nextElement( ) );
            
        }
        return foundFileObject;
    }
    
    
    /**
     *	Always returns "true" for this read-only file system.
     *
     *	@return True.
     *
     *	@since 1.0
     */
    public boolean isReadOnly( ) {
        
        return true;
    }
    
    
    /**
     *	Returns the list of actions that can be performed against the files in this
     *	file system.
     *
     *	@return Array of SystemActions that can be performed on this filesystem.
     *
     *	@since 1.0
     */
    public org.openide.util.actions.SystemAction[] getActions() {
        
        return new org.openide.util.actions.SystemAction[ 0 ];        
    }
    
    
    /**
     *	Add this file system to the CLASSPATH of the environment.  Always throws
     *  an exception, because this file system cannot be used in a CLASSPATH.
     *
     *	@since 1.0
     */
    public void prepareEnvironment(FileSystem.Environment env) throws EnvironmentNotSupportedException {

        throw new EnvironmentNotSupportedException(this);

    }
    
    
    /**
     *	Cleans up this object.
     *
     *	@since 1.0
     */
    protected void finalize( ) throws Throwable {

        removeVetoableChangeListener(this);
        rootFileObject = null;
        baseURL = null;        

    }

}
