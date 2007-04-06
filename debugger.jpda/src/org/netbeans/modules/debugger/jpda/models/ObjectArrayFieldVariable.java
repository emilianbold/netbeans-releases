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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ObjectArrayFieldVariable extends AbstractObjectVariable implements
org.netbeans.api.debugger.jpda.Field {

    private final ArrayReference array;
    private int index;
    private int maxIndexLog;
    private String declaredType;

    ObjectArrayFieldVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String declaredType,
        ArrayReference array,
        int index,
        int maxIndex,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            parentID + '.' + index +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.index = index;
        this.maxIndexLog = ArrayFieldVariable.log10(maxIndex);
        this.declaredType = declaredType;
        this.array = array;
    }

    public String getName () {
        return ArrayFieldVariable.getName(maxIndexLog, index);
    }
    
    public String getClassName () {
        return getType ();
    }
    
    public JPDAClassType getDeclaringClass() {
        return new JPDAClassTypeImpl(getDebugger(), (ReferenceType) array.type());
    }

    public boolean isStatic () {
        return false;
    }
    
    public String getDeclaredType () {
        return declaredType;
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            array.setValue(index, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    public ObjectArrayFieldVariable clone() {
        ObjectArrayFieldVariable clon = new ObjectArrayFieldVariable(
                getDebugger(),
                (ObjectReference) getJDIValue(),
                getDeclaredType(),
                array,
                index,
                0,
                getID());
        clon.maxIndexLog = this.maxIndexLog;
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        return "ObjectArrayFieldVariable " + getName ();
    }
}
