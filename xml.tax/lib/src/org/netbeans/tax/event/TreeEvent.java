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
package org.netbeans.tax.event;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeObject;
import org.netbeans.tax.TreeNode;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeEvent extends PropertyChangeEvent {
    // toDo:
    // + implement *_PHASE type of event.
    
    /** Serial Version UID */
    private static final long serialVersionUID =-4899604850035092773L;
    
    /** */
    private boolean bubbling;
    
    /** */
    private TreeObject originalSource; // make sense in bubbling phase
    
    /** */
    private String originalPropertyName;
    
    
    // make sense in bubbling phase
    
    
    //
    // init
    //
    
    /** Creates new TreeEvent. */
    private TreeEvent (TreeObject source, String propertyName, Object oldValue, Object newValue, TreeObject originalSource, String originalPropertyName) {
        super (source, propertyName, oldValue, newValue);
        
        this.bubbling             = ( originalSource != null );
        this.originalSource       = originalSource;
        this.originalPropertyName = originalPropertyName;
    }
    
    /** Creates new TreeEvent. */
    public TreeEvent (TreeObject source, String propertyName, Object oldValue, Object newValue) {
        this (source, propertyName, oldValue, newValue, null, null);
    }
    
    
    //
    // itself
    //
    
    /**
     * Used to indicate whether or not an event is a bubbling event. If the
     * event can bubble the value is true, else the value is false.
     */
    public final boolean isBubbling () {
        return bubbling;
    }
    
    /**
     */
    public final TreeObject getOriginalSource () {
        return originalSource;
    }
    
    /**
     */
    public final String getOriginalPropertyName () {
        return originalPropertyName;
    }
    
    /**
     */
    public final TreeEvent createBubbling (TreeNode currentNode) {
        return new TreeEvent (currentNode, TreeNode.PROP_NODE, null, null, (TreeObject)this.getSource (), this.getPropertyName ());
    }
    
}
