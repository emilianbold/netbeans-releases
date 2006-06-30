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
