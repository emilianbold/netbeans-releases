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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Hashtable;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;



/**
 *	<p>Represents an individual file found on the file sysetm.</p>
 *
 *	@since 1.0
 */
class HTTPFileObject extends FileObject {
    
    private static final long serialVersionUID = 200104;

	// File system that owns this file
    transient private HTTPFileSystem		parentFileSystem;
    // Path of this file under the URL of the file system.
    String									uriStem;
    // Directory object that contains this file
    transient private HTTPFileObject		parentFileObject;
    // Child file objects of this file if it is a directory
    transient private Hashtable				childFileObjects;
    // URL to this file
    private transient java.net.URL fileURL;
    // The file name part of this file
    transient private String				fullFileName;
    // The first part of this file's file name
    transient private String				fileName;
    // The extension of this file
    transient private String				fileExtension;
    // Flags whether the HTTP header of this file has been read
    transient private boolean				wasFileHeaderRead;
    // The size of this file
    transient private long					fileSize;
    // The MIME type of this file
    transient private String				fileMIMEType;
    // The last modified date of this file
    transient private Date					fileDate;
    // All file attributes for this file, as read from the HTTP headers
    transient private Hashtable				fileAttributes;
    // Flags whether this package's contents were read yet
    transient private	boolean				arePackageContentsKnown;
        
    /**
     *	Constructs a <code>HTTPFileObject</code> with the path and file systems
     *	passed.
     *
     *	@param uriStem The path to this file in the file system.
     *	@param parentFileSystem The file system that contains this file.
     *
     *	@since 1.0
     */
    HTTPFileObject( String uriStem, HTTPFileSystem parentFileSystem ) {

        initialize( uriStem, parentFileSystem );
    }
    
    
    /**
     *	Constructs an empty <code>HTTPFileObject</code>.  This constructor is only
	 *	expected to be used during deserialization.
     *
     *	@since 1.0
     */
    protected HTTPFileObject(
    ) {

	}


    /**
     *	Initializes this object with the path and file systems passed.
     *
     *	@param uriStem The path to this file in the file system.
     *	@param parentFileSystem The file system that contains this file.
     *
     *	@since 1.0
     */
	private void initialize(
		String			uriStem,
		HTTPFileSystem	parentFileSystem
	) {

        try {
            
            // initialize this file object
            this.parentFileSystem = parentFileSystem;
            this.parentFileObject = null;
            this.childFileObjects = new Hashtable( );
            this.uriStem = uriStem;
            this.fileURL = new java.net.URL( parentFileSystem.baseURL, "." + uriStem );//NOI18N
            this.fileAttributes = new Hashtable( 0 );
            this.arePackageContentsKnown = true;
            
            // If this is not a root file object,
            if( !isRoot( ) ) {

                // If this is a directory,
                if( isFolder( ) ) {

                    // Flag the header as read (there is no header for this file)
                    this.wasFileHeaderRead = true;
                    
                    // Trim the trailing slash from the file name
                    this.fullFileName = uriStem.substring( 0, uriStem.length( ) - 1 );
                    
                // If this is a file object (not a directory),
                } else {
                    
                    // Create default values for items read from the header
                    this.wasFileHeaderRead = false;
                    this.fileSize = -1;
                    this.fileMIMEType = ""; //NOI18N
                    this.fileDate = new Date( );
                    this.fullFileName = uriStem;
                    
                }
                // Trim everything after the last slash as the file name
                this.fullFileName = this.fullFileName.substring( this.fullFileName.lastIndexOf( '/' ) + 1 );
                
                // If the full file name contains a period,
                if( this.fullFileName.lastIndexOf( '.' ) != -1 ) {
                    
                    // Split the file name into its two parts
                    this.fileName = this.fullFileName.substring( 0, this.fullFileName.lastIndexOf( '.' ) );
                    this.fileExtension = this.fullFileName.substring( this.fullFileName.lastIndexOf( '.' ) + 1 );
                    
                } else {
                    
                    this.fileName = this.fullFileName;
                    this.fileExtension = "";//NOI18N
                    
                }
                
            // If this is the root file object,
            } else {
                
                this.fullFileName = "";//NOI18N
                this.fileName = "";//NOI18N
                this.fileExtension = "";//NOI18N
                
            }
            
        } catch( java.net.MalformedURLException e ) {
            
            // Ignore - should never occur
            
        }
        
	}


    /**
     *	Writes this object when it is serialized.
     *
     *	@param out Serialization output stream.
     *
     *	@since 1.0
     */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {

		// Write out the name of the filesystem and this file
		out.writeObject( parentFileSystem.getSystemName( ) );
		out.writeObject( uriStem );

	}


    /**
     *	Reads this object when it is unserialized.
     *
     *	@param in Serialization input stream.
     *
     *	@since 1.0
     */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		// Name of the parent filesystem when it was saved
		String fileSystemName;
		// Mounted filesystem with the above name
		HTTPFileSystem newParentFileSystem;
		// Name of the file
		String newURIStem;

		// Read the name of the parent filesystem and find it if mounted
		fileSystemName = (String)in.readObject( );
		newParentFileSystem = (HTTPFileSystem)org.openide.TopManager.getDefault( ).getRepository( ).findFileSystem( fileSystemName );
		if( newParentFileSystem == null ) {

			// TODO: Add an error message to this exception
			throw new IOException( ResourceUtils.getBundledString( "MSG_FilesystemNotFound" ) ); //NOI18N
		}

		// Read the name of this file and initialize it
		newURIStem = (String)in.readObject( );
		initialize( newURIStem, newParentFileSystem );
	}


	/**
     *	This method reads the information about this file that is found in the HTTP header.
     *
     *	@since 1.0
     */
    private void readFileHeader( ) {
        
        try {
            // Open a connection to the web server to read this file's header, which
            // has the side effect of reading the headers for this file
            getFileConnection( "HEAD" ).disconnect( );  //NOI18N
            
        } catch( IOException e ) {
            
            // Ignore errors
        }
    }
    
    
    /**
     *	Obtains a connection to the web server for this file with the selected request
     *	method.
     *
     *	@param requestMethod The request method to use when opening this connection.
     *
     *	@throws IOException If there was an error opening the connection
     *
     *	@since 1.0
     */
    private HttpURLConnection getFileConnection( String requestMethod ) throws IOException {
        
        // Connection to the web server for this file
        HttpURLConnection	fileConnection;
                
        // Open the connection
        fileConnection = (HttpURLConnection)fileURL.openConnection( );
        fileConnection.setUseCaches( true );
        fileConnection.setRequestMethod( requestMethod );
        
        // If the headers have not yet been read for this file,
        if( !wasFileHeaderRead ) {
            
            // Read the headers now
            readFileHeadersFromConnection( fileConnection );
            
        }
        return fileConnection;
    }
    
    
    /**
     *	Obtains a connection to the web server for this file with the "GET" method.
     *
     *	@throws IOException If there was an error opening the connection
     *
     *	@since 1.0
     */
    private HttpURLConnection getFileConnection( ) throws IOException {
        
        return getFileConnection( "GET" );    //NOI18N    
    }
    
    
    /**
     *	Given a connection to a file on a web server, reads the attributes for this
     *	file that can be found in its HTTP headers.
     *
     *	@param fileConnection Connection to the web server
     *
     *	@since 1.0
     */
    private void readFileHeadersFromConnection( HttpURLConnection fileConnection ) {
        
        // Index of the header being added to the attributes of this file
//        int	headerIndex;        
        
        fileSize = fileConnection.getContentLength( );
        fileMIMEType = fileConnection.getContentType( );
        fileDate = new Date( fileConnection.getLastModified( ) );
/*		headerIndex = 0;
		while( fileConnection.getHeaderField( headerIndex ) != null ) {

				fileAttributes.put( fileConnection.getHeaderFieldKey( headerIndex ), fileConnection.getHeaderField( fileConnection.getHeaderFieldKey( headerIndex ) ) );
				headerIndex++;

		}*/
        wasFileHeaderRead = true;
        
    }
    
    
    /**
     *	Returns the file system that owns this file.
     *
     *	@since 1.0
     */
    public org.openide.filesystems.FileSystem getFileSystem( ) {
        
        return parentFileSystem;        
    }
    
    
    /**
     *	Returns the name of this file.
     *
     *	@since 1.0
     */
    public String getName( ) {
        
        return fileName;        
    }
    
    
    /**
     *	Returns the extension of this file.
     *
     *	@since 1.0
     */
    public String getExt(
    ) {
        
        return fileExtension;
        
    }
    
    
    /**
     *	Returns the size of this file in bytes.
     *
     *	@since 1.0
     */
    public long getSize( ) {  
        
        if( !wasFileHeaderRead ) {            
            readFileHeader( );            
        }
        return fileSize;        
    }
    
    
    /**
     *	Returns the MIME type of this file.
     *
     *	@since 1.0
     */
    public String getMIMEType( ) {
        if( !wasFileHeaderRead ) {
            readFileHeader( );
        }
        return fileMIMEType;
    }
    
    
    /**
     *	Returns the parent dircetory object of this file.
     *
     *	@since 1.0
     */
    public FileObject getParent( ) {
        return parentFileObject;
    }
    
    
    /**
     *	Returns the date this file was last modified.
     *
     *	@since 1.0
     */
    public Date lastModified( ) {
        if( !wasFileHeaderRead ) {
            readFileHeader( );
        }
        return fileDate;
    }
    
    
    /**
     *	Always returns "true" for this read-only filesystem.
     *
     *	@since 1.0
     */
    public boolean isReadOnly( ) {
        return true;
    }
    
    
    /**
     *	Returns a flag specifying whether this file is valid or not.
     *
     *	@since 1.0
     */
    public boolean isValid( ) {
        return fileURL != null;
    }
    
    
    /**
     *	Returns "true" if this is the root file object of this file system.
     *
     *	@since 1.0
     */
    public boolean isRoot( ) {        
        return uriStem.equals( "/" ); //NOI18N        
    }
    
    
    /**
     *	Returns "true" if this is a directory.
     *
     *	@since 1.0
     */
    public boolean isFolder(
    ) {
        
        return uriStem.endsWith( "/" ); //NOI18N
        
    }
    
    
    /**
     *	Returns "true" if this is a file, and not a folder.
     *
     *	@since 1.0
     */
    public boolean isData(
    ) {
        
        return !isFolder( );
        
    }
    
    
    /**
     *	Called by NetBeans to lock this file.  Always returns FileLock.NONE for this
     *	read-only file system.
     *
     *	@throws IOException If there was an error locking this file.
     *
     *	@since 1.0
     */
    public FileLock lock(
    ) throws IOException {
        
        return FileLock.NONE;
        
    }
    
    
    /**
     *	Called by NetBeans to create a new file in this file system.  Always throws an
     *	{@link IOException} for this read-only file system.
     *
     *	@param fileName The file name of the file to create.
     *	@param extension The extension of the file to create.
     *
     *	@throws IOException If there was an error creating this file.
     *
     *	@since 1.0
     */
    public FileObject createData(
		String	fileName,
		String	extension
    ) throws IOException {
        
        throw new IOException( );        
    }
    
    
    /**
     *	Called by NetBeans to create a new directory in this file system.  Always
     *	throws an {@link IOException} for this read-only file system.
     *
     *	@param fileName The name of the directory to create.
     *
     *	@throws IOException If there was an error creating this directory.
     *
     *	@since 1.0
     */
    public FileObject createFolder(
	    String	fileName
    ) throws IOException {
        
        throw new IOException( );        
    }
    
    
    /**
     *	Called by NetBeans to create rename a file.  Always throws an
     *	{@link IOException} for this read-only file system.
     *
     *	@param lock The lock on this file.
     *	@param fileName The new file name for this file.
     *	@param extension The new extension for this file.
     *
     *	@throws IOException If there was an error renaming this file.
     *
     *	@since 1.0
     */
    public void rename(
		FileLock	lock,
		String		fileName,
		String		extension
    ) throws IOException {
        
        throw new IOException( );        
    }
    
    
    /**
     *	Called by NetBeans to create delete this file.  Always throws an
     *	{@link IOException} for this read-only file system.
     *
     *	@param lock The lock on this file.
     *
     *	@throws IOException If there was an error deleting this file.
     *
     *	@since 1.0
     */
    public void delete( FileLock lock ) throws IOException {
        throw new IOException( );
    }
    
    
    /**
     *	Called by NetBeans to set the importance of this file.  Does tothing for this
     *	read-only file system.
     *
     *	@param isImportant Flags whether this file is important or not.
     *
     *	@since 1.0
    */
    public void setImportant( boolean isImportant ) {
        // File system is read-only - ignore this call
    }
    
    
    /**
     *	Returns the list of attributes available for this file.
     *
     *	@since 1.0
     */
    public java.util.Enumeration getAttributes() {
        
        if( !wasFileHeaderRead ) {
            
            readFileHeader( );
            
        }
        return fileAttributes.keys( );        
    }
    
    
    /**
     *	Returns the value of the named attribute for this file.
     *
     *	@param attributeName The name of the attribute whose value should be returned.
     *
     *	@since 1.0
     */
    public Object getAttribute( String attributeName ) {
        
        if( !wasFileHeaderRead ) {
            readFileHeader( );
        }
        return fileAttributes.get( attributeName );
    }
    
    
    /**
     *	Changes the specified attribute for this file.  Always throws an
     *	{@link IOException} for this read-only file system.
     *
     *	@param attributeName The name of the attribute to change.
     *	@param newValue the new value for this attribute
     *
     *	@throws IOException If there was an error setting this attribute.
     *
     *	@since 1.0
     */
    public void setAttribute(
		String	attributeName,
		Object	newValue
    ) throws IOException {
        
        throw new IOException( );        
    }
    
    
    /**
     *	Returns a list of files that belong to this directory.
     *
     *	@since 1.0
     */
    synchronized public FileObject[] getChildren( ) {
        
        // If this is a package directory that has not been read yet,
        if( !arePackageContentsKnown ) {
            
            // Read the list of files in this directory
            readPackageContents( );
            
        }
        return (FileObject[])childFileObjects.values( ).toArray( new FileObject[ 0 ] );        
    }
    
    
    /**
     *	Returns a file within this directory with the passed name, or "null" if the
     *	file doesn't exist.
     *
     *	@param fileName The name of the file to return.
     *	@param extension The extension of the file to return.
     *
     *	@since 1.0
     */
    public FileObject getFileObject( String fileName, String extension ) {
        
        if( !extension.equals( "" ) ) { //NOI18N
            return child( fileName + "." + extension ); //NOI18N
        } else {
            return child( fileName );
        }
    }
    
    
    /**
     *	Returns an {@link HTTPFileInputStream} object to read the contents of this
     *	file.
     *
     *	@throws FileNotFoundException If the file doesn't exist.
     *
     *	@since 1.0
     */
    public java.io.InputStream getInputStream() throws java.io.FileNotFoundException {
        
        try {
            return new HTTPFileInputStream( getFileConnection( ) );
        } catch( IOException e ) {
            throw new java.io.FileNotFoundException( e.getMessage( ) );
        }
    }
    
    
    /**
     *	Intended to open an OutputStream to change the contents of this file.  Always
     *	throws an {@link IOException} for this read-only file system.
     *
     *	@param lock The lock on this file.
     *
     *	@since 1.0
     */
    public java.io.OutputStream getOutputStream(FileLock lock) throws IOException {        
        throw new IOException( );        
    }
    
    
    /**
     *	Adds an object to the list of objects that will receive notifications of file
     *	changes.  Does nothing for this read-only file system, since the files will
     *	not have change events.
     *
     *	@param listener The listener object to add to this file's list.
     *
     *	@since 1.0
     */
    public void addFileChangeListener(org.openide.filesystems.FileChangeListener listener) {        
        // File system is read-only - ignore this call        
    }
    
    
    /**
     *	Removes an object from the list of objects that will receive notifications of
     *	file changes.  Does nothing for this read-only file system, since the files
     *	will not have change events.
     *
     *	@param listener The listener object to remove from this file's list.
     *
     *	@since 1.0
     */
    public void removeFileChangeListener(org.openide.filesystems.FileChangeListener listener) {
        
        // File system is read-only - ignore this call        
    }
    
    
    /**
     *	Adds a file object to the list of files of this directory object.
     *
     *	@param newChildFileObject The file object to add.
     *
     *	@since 1.0
     */
    synchronized void addChild( HTTPFileObject newChildFileObject ) {        
        
        childFileObjects.put( newChildFileObject.fullFileName, newChildFileObject );
        newChildFileObject.parentFileObject = this;        
    }
    
    
    /**
     *	Adds a file object with the passed name to the list of files of this directory
     *	object.
     *
     *	@param newChildFileName The name of the new file object to add.
     *
     *	@since 1.0
     */
    synchronized void addChild( String newChildFileName ) {        
        addChild( new HTTPFileObject( newChildFileName, parentFileSystem ) );        
    }
    
    
    /**
     *	Adds a new file object with the passed name to the list of file objects for
     *	this directory, if the file exists.  Returns a flag specifying whether the
     *	file existed and was added or not.
     *
     *
     */
    synchronized boolean addOptionalChild( String newChildFileName ) {        
        // Connection to the web server for this file
        HttpURLConnection	fileConnection;
        // New file object
        HTTPFileObject		childFileObject;
        // Flags whether the file was added or not
        boolean				wasFileAdded;
        
        
        fileConnection = null;
        try {
            
            // Create the new file object
            childFileObject = new HTTPFileObject( newChildFileName, parentFileSystem );
            fileConnection = childFileObject.getFileConnection( "HEAD" );   //NOI18N
            
            // If the file exists,
            if( fileConnection.getResponseCode( ) < 400 ) {
                
                // Add the new file
                addChild( childFileObject );
                wasFileAdded = true;
                
            } else {
                
                wasFileAdded = false;
            }
            
        } catch( Exception e ) {
            
            wasFileAdded = false;
            
        } finally {
            
            // Always close the connection to the web server once it has been opened
            if( fileConnection != null ) {
                
                fileConnection.disconnect( );                
            }
            
        }
        return wasFileAdded;
    }
    
    
    /**
     *	Returns a file within this directory with the passed name, or "null" if the
     *	file doesn't exist.
     *
     *	@param fullFileName The full name of the file to return.
     *
     *	@since 1.0
     */
    synchronized HTTPFileObject child( String fullFileName ) {        
        // If this is a package that hasn't been read yet and the file system is not mounting,
        if( parentFileSystem.isInitialized && !arePackageContentsKnown ) {            
            readPackageContents( );            
        }
        return (HTTPFileObject)childFileObjects.get( fullFileName );        
    }
    
    
    /**
     *	Called to mark this directory as one that contains package files.
     *
     *	@since 1.0
     */
    void makePackage() {        
        arePackageContentsKnown = false;        
    }
    
    
    /**
     *	Called to read all of the contents of this package directory into memory.
     *
     *	@since 1.0
     */
    private void readPackageContents( ) {
        
        // File object for this package's "package-summary.html" file
        HTTPFileObject	packageSummaryFile;
        // Object for this package's "class-use/" directory
        HTTPFileObject	classUseDirectory;
        // InputStream to read the "package-summary.html" file
        java.io.InputStream packageFileInputStream;
        // Kit to read and parse an HTML file
        javax.swing.text.html.HTMLEditorKit editorKit;
        // HTML document representation of "package-summary.html" file
        javax.swing.text.html.HTMLDocument htmlDoc;
        // Iterator through "A" tags of the above file
        javax.swing.text.html.HTMLDocument.Iterator tagIterator;
        // The file name for the class file in this directory
        String classFileName;
        
        
        // Find ths standard files found in a package directory
        packageSummaryFile = new HTTPFileObject( uriStem + "package-summary.html", parentFileSystem );  //NOI18N
        addChild( packageSummaryFile );
        addOptionalChild( uriStem + "package-frame.html" ); //NOI18N
        addOptionalChild( uriStem + "package-tree.html" );  //NOI18N
        if( addOptionalChild( uriStem + "package-use.html" ) ) {    //NOI18N
            
            classUseDirectory = new HTTPFileObject( uriStem + "class-use/", parentFileSystem ); //NOI18N
            addChild( classUseDirectory );
            
        } else {
            
            classUseDirectory = null;
            
        }
        
        try {
            
            // Read the "package-summary.html" file into memory
            packageFileInputStream = packageSummaryFile.getInputStream( );
            editorKit = new javax.swing.text.html.HTMLEditorKit();
            htmlDoc = (javax.swing.text.html.HTMLDocument)editorKit.createDefaultDocument();
            editorKit.read( new java.io.InputStreamReader( packageFileInputStream ), htmlDoc, 0);
            
            // Find all of the "A" tags in the file
            tagIterator = htmlDoc.getIterator( javax.swing.text.html.HTML.Tag.A );
            while( tagIterator.isValid( ) ) {
                
                // Find the target of the link tag
                classFileName = (String)tagIterator.getAttributes( ).getAttribute( javax.swing.text.html.HTML.Attribute.HREF );
                if( classFileName != null ) {
                    
                    // If the link points to  file in this directory that is also not a standard package file,
                    if( classFileName.indexOf( '/' ) == -1 && !classFileName.startsWith( "." )  //NOI18N
                    && !classFileName.startsWith( "#" ) && classFileName.indexOf( ':' ) == -1   //NOI18N
                    && !classFileName.startsWith( "package-" ) ) {  //NOI18N
                        
                        // Add the file to this package directory
                        addChild( uriStem + classFileName );
                        
                        // If there is a "class-use/" dircetory,
                        if( classUseDirectory != null ) {                            
                            // Add the corresponding file in that directory
                            classUseDirectory.addChild( classUseDirectory.uriStem + classFileName );                            
                        }                        
                    }                    
                }
                tagIterator.next( );                
            }
            packageFileInputStream.close( );
            
        } catch( javax.swing.text.BadLocationException e ) {            
            // Ignore the classes of this package            
        } catch( IOException e ) {            
            // Ignore the classes of this package            
        } finally {            
            arePackageContentsKnown = true;            
        }        
    }    
}
