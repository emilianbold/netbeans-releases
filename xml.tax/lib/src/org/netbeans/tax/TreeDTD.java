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
 */
package org.netbeans.tax;

import org.netbeans.tax.event.TreeEventManager;

import org.netbeans.tax.spec.DTD;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeDTD extends AbstractTreeDTD implements TreeDocumentRoot, TreeDTDRoot {

    /** */
    public static final String PROP_VERSION  = "version"; // NOI18N
    /** */
    public static final String PROP_ENCODING = "encoding"; // NOI18N

    /** Own event manager. */
    private TreeEventManager eventManager;

    /** -- can be null. */
    private String version;
    
    /** -- can be null. */
    private String encoding;
    
    
    //
    // init
    //
    
    /**
     * Creates new TreeDTD.
     * @throws InvalidArgumentException
     */
    public TreeDTD (String version, String encoding) throws InvalidArgumentException {
        super ();
        
        checkVersion (version);
        checkEncoding (encoding);
        checkHeader (version, encoding);
        
        this.version      = version;
        this.encoding     = encoding;
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeDTD::init: encoding = " + this.encoding); // NOI18N
        
        this.eventManager = new TreeEventManager ();
    }
    
    /** Creates new TreeDTD.
     * @throws InvalidArgumentException
     */
    public TreeDTD () throws InvalidArgumentException {
        this (null, null);
    }
    
    /** Creates new TreeDTD -- copy constructor. */
    protected TreeDTD (TreeDTD dtd, boolean deep) {
        super (dtd, deep);
        
        this.version      = dtd.version;
        this.encoding     = dtd.encoding;
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeDTD::init [copy]: encoding = " + this.encoding); // NOI18N
        
        this.eventManager = new TreeEventManager (dtd.eventManager);
    }
    
    
    //
    // from TreeObject
    //
    
    /**
     */
    public Object clone (boolean deep) {
        return new TreeDTD (this, deep);
    }
    
    /**
     */
    public boolean equals (Object object, boolean deep) {
        if (!!! super.equals (object, deep))
            return false;
        
        TreeDTD peer = (TreeDTD) object;
        if (!!! Util.equals (this.getVersion (), peer.getVersion ()))
            return false;
        if (!!! Util.equals (this.getEncoding (), peer.getEncoding ()))
            return false;
        
        return true;
    }
    
    /*
     * Merges version and encoding properties.
     */
    public void merge (TreeObject treeObject) throws CannotMergeException {
        super.merge (treeObject);
        
        TreeDTD peer = (TreeDTD) treeObject;
        
        try {
            setVersionImpl (peer.getVersion ());
            setEncodingImpl (peer.getEncoding ());
        } catch (Exception exc) {
            throw new CannotMergeException (treeObject, exc);
        }
    }
    
    //
    // from TreeDocumentRoot
    //
    
    /**
     */
    public String getVersion () {
        return version;
    }
    
    /**
     */
    private final void setVersionImpl (String newVersion) {
        String oldVersion = this.version;
        
        this.version = newVersion;
        
        firePropertyChange (PROP_VERSION, oldVersion, newVersion);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setVersion (String newVersion) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.version, newVersion) )
            return;
        checkReadOnly ();
        checkVersion (newVersion);
        checkHeader (newVersion, this.encoding);
        
        //
        // set new value
        //
        setVersionImpl (newVersion);
    }
    
    /**
     */
    protected final void checkVersion (String version) throws InvalidArgumentException {
        TreeUtilities.checkDTDVersion (version);
    }
    
    
    /**
     */
    public String getEncoding () {
        return encoding;
    }
    
    /**
     */
    private final void setEncodingImpl (String newEncoding) {
        String oldEncoding = this.encoding;
        
        this.encoding = newEncoding;
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeDTD::setEncodingImpl: encoding = " + this.encoding); // NOI18N
        
        firePropertyChange (PROP_ENCODING, oldEncoding, newEncoding);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setEncoding (String newEncoding) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        if ( Util.equals (this.encoding, newEncoding) )
            return;
        checkReadOnly ();
        checkEncoding (newEncoding);
        checkHeader (this.version, newEncoding);
        
        //
        // set new value
        //
        setEncodingImpl (newEncoding);
    }
    
    /**
     */
    protected final void checkEncoding (String encoding) throws InvalidArgumentException {
        TreeUtilities.checkDTDEncoding (encoding);
    }
    
    /**
     * @throws ReadOnlyException
     * @throws InvalidArgumentException
     */
    public final void setHeader (String newVersion, String newEncoding) throws ReadOnlyException, InvalidArgumentException {
        //
        // check new value
        //
        boolean setVersion  = !!! Util.equals (this.version, newVersion);
        boolean setEncoding = !!! Util.equals (this.encoding, newEncoding);
        if ( !!! setVersion &&
             !!! setEncoding ) {
            return;
        }
        checkReadOnly ();
        if ( setVersion ) {
            checkVersion (newVersion);
        }
        if ( setEncoding ) {
            checkEncoding (newEncoding);
        }
        checkHeader (newVersion, newEncoding);
        
        //
        // set new value
        //
        if ( setVersion ) {
            setVersionImpl (newVersion);
        }
        if ( setEncoding ) {
            setEncodingImpl (newEncoding);
        }
    }
    
    /**
     */
    protected final void checkHeader (String version, String encoding) throws InvalidArgumentException {
        if ((version != null) && (encoding == null)) {
            throw new InvalidArgumentException
            (Util.THIS.getString ("EXC_invalid_dtd_header"),
            new NullPointerException ());
        }
    }
    
    
    //
    // event model
    //
    
    /**
     */
    public TreeEventManager getRootEventManager () {
        return eventManager;
    }
    
    
    //
    // TreeObjectList.ContentManager
    //
    
    /**
     */
    protected TreeObjectList.ContentManager createChildListContentManager () {
        return new ChildListContentManager ();
    }
    
    
    /**
     *
     */
    protected class ChildListContentManager extends AbstractTreeDTD.ChildListContentManager {
        
        /**
         */
        public TreeNode getOwnerNode () {
            return TreeDTD.this;
        }
        
        /**
         */
        public void checkAssignableObject (Object obj) {
            super.checkAssignableObject (obj);
            checkAssignableClass (DTD.Child.class, obj);
        }
        
    } // end: class ChildListContentManager
    
}
