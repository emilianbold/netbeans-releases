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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAClassTypeImpl implements JPDAClassType {
    
    private JPDADebuggerImpl debugger;
    private ReferenceType classType;
    
    /**
     * Creates a new instance of JPDAClassTypeImpl
     */
    public JPDAClassTypeImpl(JPDADebuggerImpl debugger, ReferenceType classType) {
        this.debugger = debugger;
        this.classType = classType;
    }

    public String getName() {
        return classType.name();
    }

    public String getSourceName() throws AbsentInformationException {
        return classType.sourceName();
    }

    public ClassVariable classObject() {
        return new ClassVariableImpl(debugger, classType.classObject(), "");
    }

    public List<Field> staticFields() {
        List<com.sun.jdi.Field> allFieldsOrig = classType.allFields();
        List<Field> staticFields = new ArrayList<Field>();
        for (int i = 0; i < allFieldsOrig.size(); i++) {
            Value value = null;
            com.sun.jdi.Field origField = allFieldsOrig.get(i);
            if (origField.isStatic()) {
                value = classType.getValue(origField);
                staticFields.add(new FieldVariable(debugger, value, origField, "", (ObjectReference) null));
            }
        }
        return staticFields;
    }
    
}
