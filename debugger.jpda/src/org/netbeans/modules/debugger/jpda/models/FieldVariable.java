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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class FieldVariable extends AbstractVariable implements
org.netbeans.api.debugger.jpda.Field {

    protected Field field;
    private ObjectReference objectReference;
    

    FieldVariable (
        JPDADebuggerImpl debugger,
        PrimitiveValue value,
    //    String className,
        Field field,
        String parentID,
        ObjectReference objectReference
    ) {
        super (
            debugger, 
            value, 
            parentID + '.' + field.name ()
        );
        this.field = field;
        //this.className = className;
        this.objectReference = objectReference;
    }

    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return field.name ();
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return field.declaringType ().name (); //className;
    }
    
    public JPDAClassType getDeclaringClass() {
        return new JPDAClassTypeImpl(getDebugger(), (ReferenceType) objectReference.type());
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return field.typeName ();
    }

    /**
     * Returns <code>true</code> for static fields.
     *
     * @return <code>true</code> for static fields
     */
    public boolean isStatic () {
        return field.isStatic ();
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            boolean set = false;
            if (objectReference != null) {
                objectReference.setValue (field, value);
                set = true;
            } else {
                ReferenceType rt = field.declaringType();
                if (rt instanceof ClassType) {
                    ClassType ct = (ClassType) rt;
                    ct.setValue(field, value);
                    set = true;
                }
            }
            if (!set) {
                throw new InvalidExpressionException(field.toString());
            }
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    public FieldVariable clone() {
        FieldVariable clon;
        clon = new FieldVariable(getDebugger(), (PrimitiveValue) getJDIValue(), field,
                getID().substring(0, getID().length() - ("." + field.name() + (getJDIValue() instanceof ObjectReference ? "^" : "")).length()),
                objectReference);
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        return "FieldVariable " + field.name ();
    }
}

