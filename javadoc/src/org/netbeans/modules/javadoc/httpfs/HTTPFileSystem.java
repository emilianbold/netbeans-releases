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
 * Portions created by Jeffrey A. Keyser are Copyright (C) 2000-2005.
 * All Rights Reserved.
 *
 * Contributor(s): Jeffrey A. Keyser.
 *
 **************************************************************************/


package org.netbeans.modules.javadoc.httpfs;

import java.io.*;
import java.beans.*;
import java.net.*;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.SystemAction;

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
    /**
     *	Property name for the refresh rate for the file system.
     *
     *	@since 3.4
     */
    public static final String  PROP_REFRESH_RATE = "RefreshRate";  // NOI18N
    /**
     *	Property name for the state of the file system.
     *
     *	@since 3.4
     */
    public static final String  PROP_STATE = "State";  // NOI18N
    /**
     *	Current state is not known.
     *
     *	@since 3.4
     */
    public static final int     STATE_UNKNOWN = 0;
    /**
     *	File system is reading its structure from the web site.
     *
     *	@since 3.4
     */
    public static final int     STATE_READING = 1;
    /**
     *	File system is done reading its structure.
     *
     *	@since 3.4
     */
    public static final int     STATE_COMPLETE = 2;

    private static final long   serialVersionUID = 200104;
    // Default URL to use for a new filesystem
    private static final String DEFAULT_URL = "http://www.netbeans.org/download/apis/"; //NOI18N
    
    
    // URL to the Javadocs
    transient URL                   baseURL;
    // Root file object for the mounted filesystem
    transient HTTPRootFileObject    rootFileObject;
    // Refresh rate in minutes
    transient int                   refreshRate;
    // Current state of the file system
    transient int                   currentState;
            
    /**
     *	Constructs a <code>HTTPFileSystem</code> file system bean.
     *
     *	@since 1.0
     */
    public HTTPFileSystem() {
        
        setHidden( true );
        addVetoableChangeListener( this );
        refreshRate = 0;
        currentState = STATE_UNKNOWN;
        
        try{
            
            // Set a known URL as the default
            setURL( DEFAULT_URL );  //NOI18N
            
        } catch( PropertyVetoException e ) {
            
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

        // Write the URL
        out.writeObject( baseURL.toString( ) );

        // Write the refresh rate
        out.writeInt( refreshRate );

    }
    
    
    /**
     *	Reads this object when it is unserialized.
     *
     *	@param in Serialization input stream.
     *
     *	@since 1.0
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {

        // Make sure this object listens to its own property changes
        addVetoableChangeListener( this );
        try {

            // Read the URL
            setURL( (String)in.readObject( ) );

            // Backward compatibility to old versions of this object
            try {

                // Read the refresh rate
                refreshRate = in.readInt( );

            // If the refresh rate could not be read,
            } catch( IOException e ) {

                // Default to not refreshing
                refreshRate = 0;

            }

        } catch( PropertyVetoException e ) {

            throw new IOException( e.getMessage( ) );

        }

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
     *  @throws PropertyVetoException If the URL doesn't point to a web site, or
     *      if some other property listener vetos this change.
     *
     *	@since 1.0
     *
     *	@see #getURL()
     */
    public synchronized void setURL( String url )
        throws PropertyVetoException {        

        // Original URL of this filesystem
        URL                 oldURL;
        // Original root file object of this filesystem
        HTTPRootFileObject  oldRootFileObject;


        // Save current state of the bean
        oldURL = baseURL;
        oldRootFileObject = rootFileObject;
        
        try {
            
            // Create the new root file object
            try {
                
                baseURL = new URL( url );
                
            }
            catch( java.net.MalformedURLException mlfEx ){
                
                throw new PropertyVetoException( mlfEx.toString( ), new PropertyChangeEvent( this, PROP_URL, oldURL != null ? oldURL.toExternalForm( ) : null, url ) );
                
            }
            rootFileObject = new HTTPRootFileObject( this );   //NOI18N

            // Give listeners a chance to reject the URL
            fireVetoableChange( PROP_URL, oldURL != null ? oldURL.toExternalForm( ) : null, url );
            
            // Set the new name of this file system (also fires display name property change event)
            setSystemName( this.getClass( ).getName( ) + "/" + baseURL.toExternalForm( ) ); //NOI18N
            
        } catch( PropertyVetoException e ) {
            
            // Set bean back to previous state and rethrow this exception
            baseURL = oldURL;
            rootFileObject = oldRootFileObject;
            throw e;

        }
        firePropertyChange( PROP_URL, oldURL != null ? oldURL.toExternalForm( ) : null, url );
        firePropertyChange( PROP_ROOT, oldRootFileObject, rootFileObject );        

    }


    /**
     *  Returns the current refresh rate of this file system.
     *
     *  @return Refresh rate for this filesystem.
     *
     *  @see #setRefreshRate(int)
     *
     *  @since 3.4
     */
    public int getRefreshRate(
    ) {

        return refreshRate;

    }


    /**
     *  Changes the current refresh rate of this file system.
     *
     *	@throws PropertyVetoException If the refresh rate is negative, or if
     *      some other property listener vetos this change.
     *
     *  @see #getRefreshRate()
     *
     *  @since 3.4
     */
    public void setRefreshRate(
        int newRefreshRate
    ) throws PropertyVetoException {

        int oldRefreshRate;


        oldRefreshRate = refreshRate;
        try {

            refreshRate = newRefreshRate;

            // Give listeners a chance to reject the new refresh rate
            fireVetoableChange( PROP_REFRESH_RATE, new Integer( oldRefreshRate ), new Integer( newRefreshRate ) );

        } catch( PropertyVetoException e ) {

            // Set bean back to previous state and rethrow this exception
            refreshRate = oldRefreshRate;
            throw e;

        }
        firePropertyChange( PROP_REFRESH_RATE, new Integer( oldRefreshRate ), new Integer( newRefreshRate ) );

    }


    /**
     *  Returns the current state of this file system.
     *
     *  @return State code for this filesystem.
     *
     *  @since 3.4
     */
    public int getState(
    ) {

        return currentState;

    }


    /**
     *  Changes the current state of this file system.
     *
     *  @see #getState()
     *
     *  @since 3.4
     */
    void setState(
        int newState
    ) {

        // Previous state
        int     oldState;
        // Previous display name
        String  oldDisplayName;


        // If the state is actually changing,
        if( newState != currentState ) {

            // Save the current display name
            oldDisplayName = getDisplayName( );

            // Change the state
            oldState = currentState;
            currentState = newState;
            firePropertyChange( PROP_STATE, new Integer( oldState ), new Integer( newState ) );

            // If the effective display name has changed,
            if( !oldDisplayName.equals( getDisplayName( ) ) ) {

                // Fire a property change event for that, too
                firePropertyChange( PROP_DISPLAY_NAME, oldDisplayName, getDisplayName( ) );

            }

        }

    }


    /**
     *  Verifies that the URL or refresh rate given to this filesystem is valid.
     *
     *  @param propertyChangeEvent Change request for this file system's
     *  properties.
     *
     *  @since 1.0
     */
    public void vetoableChange(
        PropertyChangeEvent propertyChangeEvent
    ) throws PropertyVetoException {

        // New URL
        URL newURL;
        // New refresh rate
        int newRefreshRate;


        // If the property change event is this object's URL property,
        if( propertyChangeEvent.getSource( ) == this && propertyChangeEvent.getPropertyName( ).equals( PROP_URL ) ) {

            // Test the URL format
            try {

                newURL = new URL( (String)propertyChangeEvent.getNewValue( ) );

            }
            catch( MalformedURLException mlfEx ){

                throw new PropertyVetoException( mlfEx.toString( ), propertyChangeEvent );

            }
        
            // If this URL doesn't point to an HTTP server,
            if( !newURL.getProtocol( ).equals( "http" ) && !newURL.getProtocol( ).equals( "https" ) ) { //NOI18N
                
                // Reject this URL
                throw new PropertyVetoException( NbBundle.getMessage(HTTPFileSystem.class, "MSG_NotHTTPProtocol" ), propertyChangeEvent );    //NOI18N
                
            }
            // If this URL doesn't point to a directory,
            if( !newURL.toExternalForm( ).endsWith( "/" ) ){    //NOI18N

                // Reject this URL
                throw new PropertyVetoException( NbBundle.getMessage(HTTPFileSystem.class, "MSG_NotDirectory" ), propertyChangeEvent );    //NOI18N

            }
            
        // If the property change event is this object's refresh rate property,
        } else if( propertyChangeEvent.getSource( ) == this && propertyChangeEvent.getPropertyName( ).equals( PROP_REFRESH_RATE ) ) {

            newRefreshRate = ( (Integer)propertyChangeEvent.getNewValue( ) ).intValue( );

            // If the new refresh rate is negative,
            if( newRefreshRate < 0 ) {

                // Reject this refresh rate
                throw new PropertyVetoException( NbBundle.getMessage(HTTPFileSystem.class, "MSG_RefreshRateCannotBeNegative" ), propertyChangeEvent );    //NOI18N

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

        // Message key to use
        String  messageKey;
        // Values to pass to the message formatter
        Object  replacementValues[];

        // If the web server is being scanned,
        if( getState( ) == STATE_READING ) {

            // Use the "scanning" message
            messageKey = "DisplayName_Scanning"; // NOI18N
        // If the web server is not being scanned,
        } else {

            // Use the "normal" message
            messageKey = "DisplayName_Normal"; // NOI18N
        }

        return NbBundle.getMessage(HTTPFileSystem.class, messageKey, baseURL.toExternalForm( ) );

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
     *
     *  @see RefeshAction
     */
    public org.openide.util.actions.SystemAction[] getActions(
    ) {

        // Class object for RefreshAction
        Class           refreshActionClass;
        // Cached instance of RefreshAction
        RefreshAction   refreshAction;


        // Get a cached copy of the RefreshAction object
        refreshActionClass = RefreshAction.class;
        refreshAction = (RefreshAction)SharedClassObject.findObject( refreshActionClass, true );

        // Return the array of actions
        return new SystemAction[ ] { refreshAction };

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
