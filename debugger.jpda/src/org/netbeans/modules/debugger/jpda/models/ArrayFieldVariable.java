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

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;


/**
 * @author   Jan Jancura
 */
public class ArrayFieldVariable extends AbstractVariable implements 
org.netbeans.api.debugger.jpda.Field {
        
    private int index;
    private String declaredType;
    private String className;

    ArrayFieldVariable (
        LocalsTreeModel model, 
        Value value,
        String className,
        String declaredType,
        int index, 
        String parentID
    ) {
        super (
            model, 
            value, 
            parentID + '.' + index +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.index = index;
        this.declaredType = declaredType;
        this.className = className;
    }

    
    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return "[" + index + "]";
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return className;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return declaredType;
    }

    
    // other methods ...........................................................

    public String toString () {
        return "FieldVariable " + getName ();
    }
}

