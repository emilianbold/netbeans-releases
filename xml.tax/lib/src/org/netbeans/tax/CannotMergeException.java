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
package org.netbeans.tax;

/**
 * This exception is thrown if particulat merge with particular
 * Object is not supported.
 *
 * @author  Petr Kuzel
 * @author  Libor Kramolis
 * @version 1.0
 */
public class CannotMergeException extends TreeException {
    
    /** Its serial version UID. */
    private static final long serialVersionUID = 2841154621509323456L;
    
    
    //
    // init
    //
    
    /**
     * Constructs an <code>CannotMergeException</code> with the specified detail message.
     * @param obj the object that cannot be merged.
     */
    public CannotMergeException (TreeObject treeObj) {
        super (treeObj == null ? Util.THIS.getString ("EXC_can_not_merge_null") : treeObj.getClass ().toString () + Util.THIS.getString ("EXC_can_not_be_merged"));
    }
    
    /**
     * Constructs an <code>CannotMergeException</code> with the specified detail message.
     * @param obj the object that cannot be merged.
     */
    public CannotMergeException (TreeObject treeObj, Exception exc) {
        super (treeObj == null ? Util.THIS.getString ("EXC_can_not_merge_null") : treeObj.getClass ().toString () + Util.THIS.getString ("EXC_can_not_be_merged"), exc);
    }
    
}
