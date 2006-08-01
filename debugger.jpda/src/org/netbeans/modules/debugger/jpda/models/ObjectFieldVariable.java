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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ObjectFieldVariable extends FieldVariable
implements ObjectVariable {


    private ObjectReference objectReference;
    private String genericSignature;
    
    ObjectFieldVariable (
        JPDADebuggerImpl debugger, 
        ObjectReference value, 
        //String className,
        Field field,
        String parentID,
        String genericSignature,
        ObjectReference objectReference
    ) {
        super (
            debugger,
            value,
           // className,
            field,
            parentID,
            genericSignature
        );
        this.objectReference = objectReference;
        this.genericSignature = genericSignature;
    }

    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            objectReference.setValue (field, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    public ObjectFieldVariable clone() {
        return new ObjectFieldVariable(getDebugger(), (ObjectReference) getJDIValue(),
                field, getID(), genericSignature, objectReference);
    }

    
    // other methods ...........................................................

    public String toString () {
        return "ObjectFieldVariable " + field.name ();
    }
}
