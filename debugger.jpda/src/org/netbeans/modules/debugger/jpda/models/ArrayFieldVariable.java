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

import com.sun.jdi.*;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;


/**
 * @author   Jan Jancura
 */
public class ArrayFieldVariable extends AbstractVariable implements 
org.netbeans.api.debugger.jpda.Field {

    private final ArrayReference array;
    private int index;
    private String declaredType;

    ArrayFieldVariable (
        LocalsTreeModel model, 
        Value value,
        String declaredType,
        ArrayReference array,
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
        this.array = array;
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
        return getType ();
    }

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return false;
    }
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return declaredType;
    }

    /**
     * Sets new value of this variable.
     * 
     * @param value ne value
     * @throws InvalidExpressionException if the value is invalid
     */ 
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            array.setValue(index, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    // other methods ...........................................................

    public String toString () {
        return "FieldVariable " + getName ();
    }
}

