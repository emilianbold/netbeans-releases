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
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.StringTokenizer;

import org.openide.filesystems.*;


/**
 *	<p>Implemets the "HTTP Javadoc Filesystem" bean.</p>
 *
 *	@since 1.0
 */
public class HTTPFileSystem extends FileSystem {
    /**
     *	Property name for the URL of the file system.
     *
     *	@since 1.0
     */
    public static final String	PROP_URL = "URL";   //NOI18N
    private static final long serialVersionUID = 200104;
        
    
    // URL to the Javadocs, do not keep it
    transient URL	baseURL;
    // Root file object for the mounted filesystem
    transient HTTPFileObject rootFileObject;
    // Flags whether this filesystem's contents has been walked or not
    transient boolean isInitialized;
            
    /**
     *	Constructs a <code>HTTPFileSystem</code> file system bean.
     *
     *	@since 1.0
     */
    public HTTPFileSystem() {
        
        // The default capabilities of this file system
        FileSystemCapability.Bean httpFileSystemCapabilities;
        
        
        // Set the capabilies to "Documentation" only
        httpFileSystemCapabilities = new FileSystemCapability.Bean( );
        httpFileSystemCapabilities.setDoc( true );
        httpFileSystemCapabilities.setExecute( false );
        httpFileSystemCapabilities.setCompile( false );
        httpFileSystemCapabilities.setDebug( false );        
        setCapability( httpFileSystemCapabilities );
        setHidden(true);
        
        // TODO: I'd like to use this as the default, but there is no package-list file!
        //			setURL( new URL( "http://www.netbeans.org/download/apis/" ) );
        try{
            setURL( "http://java.sun.com/j2se/1.3/docs/api/" );  //NOI18N        
        } catch( PropertyVetoException e ) {
            
            // I have no idea what to do if this happens!
            
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
        
        out.writeObject( baseURL.toString() );        
    }
    
    
    /**
     *	Reads this object when it is unserialized.
     *
     *	@param in Serialization input stream.
     *
     *	@since 1.0
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        
        // URL for the filesystem
        String fileSystemURL = (String)in.readObject( );
        try {            
            setURL( fileSystemURL );            
        } catch( PropertyVetoException e ) {            
            throw new IOException( e.getMessage( ) );            
        }        
    }
    
    
    /**
     *	Returns the current URL of this file system.
     *
     *	@since 1.0
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
     *	@throws PropertyVetoException If the bean cannot find the "package-list" file
     *			at this URL, if the URL doesn't point to a web site, or if some other
     *			property listener vetos this change.
     *
     *	@since 1.0
     *
     *	@see #getURL()
     */
    public synchronized void setURL( String url ) throws PropertyVetoException {        
        // Change event for this property
        
        java.beans.PropertyChangeEvent	newURLEvent;
        //new URL
        URL newURL;
        // Original URL of this filesystem
        URL oldURL;
        // Original root file object of this filesystem
        HTTPFileObject oldRootFileObject;
        
        try {
            newURL = new URL(url);
        }
        catch( java.net.MalformedURLException mlfEx){
            return; //can not continue
        }
                        
        // Save current state of the bean
        oldURL = baseURL;
        oldRootFileObject = rootFileObject;
        
        fireVetoableChange( PROP_URL, oldURL, newURL );
        
        try {
            
            // Create a change event for this property
            newURLEvent = new java.beans.PropertyChangeEvent( this, PROP_URL, oldURL, newURL );
            
            // If this URL doesn't point to an HTTP server,
            if( !newURL.getProtocol( ).equals( "http" ) && !newURL.getProtocol( ).equals( "https" ) ) { //NOI18N
                
                // Reject this URL
                throw new PropertyVetoException( ResourceUtils.getBundledString( "MSG_NotHTTPProtocol" ), newURLEvent );    //NOI18N
                
            }
            
            // Create the new root file object
            baseURL = newURL;
            rootFileObject = new HTTPFileObject( "/", this );   //NOI18N
            
            // Flag this file sytem as needing its contents scanned
            isInitialized = false;
            
            // If a Javadoc package list doesn't exists at this URL,
            if( !rootFileObject.addOptionalChild( "/package-list" ) ) { //NOI18N
                
                // Reject this URL
                throw new PropertyVetoException( ResourceUtils.getBundledString( "MSG_JavadocsNotFound" ), newURLEvent ); //NOI18N
                
            }
            
            // Set the new name of this file system
            setSystemName( this.getClass( ).getName( ) + "/" + baseURL.toExternalForm( ) ); //NOI18N
            
        } catch( PropertyVetoException e ) {
            
            // Set bean back to previous state and rethrow this exception
            baseURL = oldURL;
            rootFileObject = oldRootFileObject;
            throw e;            
        }
        firePropertyChange( PROP_URL, oldURL, newURL );
        firePropertyChange( PROP_ROOT, oldRootFileObject, rootFileObject );        
    }
    
    
    /**
     *	Returns the root directory for the Javadocs.  If this filesystem has not
     *	yet been used, it reads the base files available at the URL to build the
     *	file tree.
     *
     *	@since 1.0
     */
    public FileObject getRoot() {
        
        // File object for /package-list
        HTTPFileObject	packageFile;
        // File object for /index-files/ directory
        HTTPFileObject	indexDirectory;
        // Reader of package names in /package-list
        BufferedReader packageReader;
        // Package name read from /package-list
        String packageName;
        // File number for the next split index file
        int indexFileNumber;
        
        
        if( !isInitialized ) {
            
            // Add the standard files for a Javadoc directory structre
            packageFile = rootFileObject.child( "package-list" );       //NOI18N
            rootFileObject.addChild( "/allclasses-frame.html" );        //NOI18N
            rootFileObject.addOptionalChild( "/deprecated-list.html" ); //NOI18N
            rootFileObject.addOptionalChild( "/help-doc.html" );        //NOI18N
            rootFileObject.addOptionalChild( "/index.html" );           //NOI18N
            rootFileObject.addChild( "/overview-frame.html" );          //NOI18N
            rootFileObject.addChild( "/overview-summary.html" );        //NOI18N
            rootFileObject.addOptionalChild( "/overview-tree.html" );   //NOI18N
            rootFileObject.addChild( "/packages.html" );                //NOI18N
            rootFileObject.addChild( "/serialized-form.html" );         //NOI18N
            rootFileObject.addChild( "/stylesheet.css" );               //NOI18N
            
            // Add the full index file
            if( !rootFileObject.addOptionalChild( "/index-all.html" ) ) {   //NOI18N
                
                // If there was no full index, search for split index files
                indexFileNumber = 1;
                while( rootFileObject.addOptionalChild( "/index-" + indexFileNumber + ".html" ) ) { //NOI18N
                    
                    indexFileNumber++;                    
                }
                
                // If no index were found in the root,
                if( indexFileNumber == 1 ) {
                    // Look in /index-files/
                    indexDirectory = new HTTPFileObject( "/index-files/", this );   //NOI18N
                    // Add the full index file
                    if( !indexDirectory.addOptionalChild( "/index-files/index-all.html" ) ) {   //NOI18N
                        // If there was no full index, search for split index files
                        indexFileNumber = 1;
                        while( indexDirectory.addOptionalChild( "/index-files/index-" + indexFileNumber + ".html" ) ) { //NOI18N
                            indexFileNumber++;
                        }
                        // If index file were found in this directory,
                        if( indexFileNumber != 1 ) {
                            rootFileObject.addChild( indexDirectory );
                        }
                        
                        // If there was an index file found in this directory,
                    } else {
                        rootFileObject.addChild( indexDirectory );
                    }
                }
            }
            try {
                // Read all of the package names from the /package-list file
                packageReader = new BufferedReader( new InputStreamReader( packageFile.getInputStream( ) ) );
                packageName = packageReader.readLine( );
                while( packageName != null ) {
                    // Add each package to this file system
                    addPackage( packageName );
                    packageName = packageReader.readLine( );
                }
                packageReader.close( );
                
            } catch( IOException e ) {
                // Ignore packages
            } finally {
                // File system is done mounting
                isInitialized = true;
            }
        }
        return rootFileObject;
    }
    
    
    /**
     *	Adds the named package to this file sytem.
     *
     *	@param packageName Package name to add to this file system.
     *
     *	@since 1.0
     */
    private void addPackage( String packageName ) {
        
        // Parser to break up the package heirarchy
        StringTokenizer		packageParser;
        // One level of this package heirarchy
        String				packagePart;
        // The diretory that belongs to the selected package
        HTTPFileObject		packageDirectory;
        
        
        // Pull apart the package heirarchy
        packageParser = new StringTokenizer( packageName, "." );    //NOI18N
        packageDirectory = rootFileObject;
        
        // With each level of the package,
        while( packageParser.hasMoreElements( ) ) {
            
            packagePart = (String)packageParser.nextElement( );
            
            // Find its directory object
            if( packageDirectory.child( packagePart ) == null ) {
                
                packageDirectory.addChild( packageDirectory.uriStem + packagePart + "/" );  //NOI18N
            }
            packageDirectory = packageDirectory.child( packagePart );            
        }
        // flag this directory as containing class files
        packageDirectory.makePackage( );
    }
    
    
    /**
     *	Provides the name of this file system to be displayed to the user, which is the
     *	URL of the Javadocs.
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
     *	@since 1.0
     */
    public boolean isReadOnly( ) {
        
        return true;
    }
    
    
    /**
     *	Returns the list of actions that can be performed against the files in this
     *	file system.
     *
     *	@since 1.0
     */
    public org.openide.util.actions.SystemAction[] getActions() {
        
        return new org.openide.util.actions.SystemAction[ 0 ];        
    }
    
    
    /**
     *	Cleans up this object.
     *
     *	@since 1.0
     */
    protected void finalize( ) throws Throwable {
        
        rootFileObject = null;
        baseURL = null;        
    }    
}
