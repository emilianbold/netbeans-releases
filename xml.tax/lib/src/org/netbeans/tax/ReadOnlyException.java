/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

/**
 * This exception is thrown if context read-only property is manipulated.
 * For instance content of external entity.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class ReadOnlyException extends TreeException {
        
    /** Serial Version UID */
    private static final long serialVersionUID =8498263913386691552L;
    
    //
    // init
    //

    /**
     */
    public ReadOnlyException (TreeObject treeObj) {
        super (treeObj.toString() + Util.getString ("PROP_is_read_only_now"));
    }

}
