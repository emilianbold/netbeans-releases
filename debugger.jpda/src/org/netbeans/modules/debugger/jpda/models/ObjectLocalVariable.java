/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;


/**
 * @author   Jan Jancura
 */
public class ObjectLocalVariable extends Local implements 
org.netbeans.api.debugger.jpda.ObjectVariable {
        
    ObjectLocalVariable (
        LocalsTreeModel model, 
        ObjectReference value,
        String className,
        LocalVariable local
    ) {
        super (model, value, className, local);
    }

    
    // LocalVariable impl.......................................................
    
    // other methods ...........................................................
    
    public String toString () {
        return "ObjectLocalVariable " + local.name ();
    }
}
