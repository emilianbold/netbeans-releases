/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.cookies;

import java.io.IOException;
import java.beans.PropertyChangeListener;

import org.openide.nodes.Node;
import org.openide.util.Task;
import org.openide.loaders.XMLDataObject;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;

/**
 *
 * @author  Petr Kuzel
 * @version 
 */
public interface TreeEditorCookie extends Node.Cookie {
    
    /** property name of document property */
    public static final String PROP_DOCUMENT_ROOT = "documentRoot"; // NOI18N

    /** the result of parsing */
    public static final String PROP_STATUS        = "status"; // NOI18N
    

    /**
     * Detailed status of model not yet available (model not loaded).
     */
    public static final int STATUS_NOT     = XMLDataObject.STATUS_NOT;
    
    /**
     * Model is OK.
     */
    public static final int STATUS_OK      = XMLDataObject.STATUS_OK;
    
    /**
     * Model was constructed with some warnings.
     */
    public static final int STATUS_WARNING = XMLDataObject.STATUS_WARNING;
    
    /**
     * Model can not be constructed.
     */
    public static final int STATUS_ERROR   = XMLDataObject.STATUS_ERROR;
    

    /*
     * Wait until document is loaded/parsed.
     */
    public TreeDocumentRoot openDocumentRoot () throws IOException, TreeException;
    
    /*
     *
     */
    public Task prepareDocumentRoot ();
    
    /*
     * May return null.
     */
    public TreeDocumentRoot getDocumentRoot ();
    

    /**
     */
    public int getStatus();
    
    
    /**
     */
    public void addPropertyChangeListener (PropertyChangeListener listener);
    
    /**
     */
    public void removePropertyChangeListener (PropertyChangeListener listener);

}
