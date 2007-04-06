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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.Java6Methods;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAClassTypeImpl implements JPDAClassType {
    
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI8N
    
    private JPDADebuggerImpl debugger;
    private ReferenceType classType;
//    private long cachedInstanceCount = -1L;
    
    /**
     * Creates a new instance of JPDAClassTypeImpl
     */
    public JPDAClassTypeImpl(JPDADebuggerImpl debugger, ReferenceType classType) {
        this.debugger = debugger;
        this.classType = classType;
    }
    
    public ReferenceType getType() {
        return classType;
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
    
    public ObjectVariable getClassLoader() {
        return new AbstractObjectVariable(debugger, classType.classLoader(), "Loader "+getName());
    }
    
    public SuperVariable getSuperClass() {
        if (classType instanceof ClassType) {
            return new SuperVariable(debugger, null, ((ClassType) classType).superclass(), getName());
        } else {
            return null;
        }
    }

    public List<Field> staticFields() {
        List<com.sun.jdi.Field> allFieldsOrig = classType.allFields();
        List<Field> staticFields = new ArrayList<Field>();
        for (int i = 0; i < allFieldsOrig.size(); i++) {
            Value value = null;
            com.sun.jdi.Field origField = allFieldsOrig.get(i);
            if (origField.isStatic()) {
                if (loggerValue.isLoggable(Level.FINE)) {
                    loggerValue.fine("STARTED : "+classType+".getValue("+origField+")");
                }
                value = classType.getValue(origField);
                if (loggerValue.isLoggable(Level.FINE)) {
                    loggerValue.fine("FINISHED: "+classType+".getValue("+origField+") = "+value);
                }
                if (value instanceof PrimitiveValue) {
                    staticFields.add(new FieldVariable(debugger, (PrimitiveValue) value, origField, "", (ObjectReference) null));
                } else {
                    staticFields.add(new ObjectFieldVariable(debugger, (ObjectReference) value, origField, "", (ObjectReference) null));
                }
            }
        }
        return staticFields;
    }
    
    public long getInstanceCount() {//boolean refresh) {
        if (Java6Methods.isJDK6()) {
            /*synchronized (this) {
                if (!refresh && cachedInstanceCount > -1L) {
                    return cachedInstanceCount;
                }
            }*/
            long[] counts = Java6Methods.instanceCounts(classType.virtualMachine(),
                                                        Collections.singletonList(classType));
            /*synchronized (this) {
                cachedInstanceCount = counts[0];
            }*/
            return counts[0];
        } else {
            return 0L;
        }
    }
    
    public List<ObjectVariable> getInstances(long maxInstances) {
        if (Java6Methods.isJDK6()) {
            final List<ObjectReference> instances = Java6Methods.instances(classType, maxInstances);
            return new AbstractList<ObjectVariable>() {
                public ObjectVariable get(int i) {
                    ObjectReference obj = instances.get(i);
                    return new AbstractObjectVariable(debugger, obj, classType.name()+" instance "+i);
                }

                public int size() {
                    return instances.size();
                }
            };
        } else {
            return Collections.emptyList();
        }
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof JPDAClassTypeImpl)) {
            return false;
        }
        return classType.equals(((JPDAClassTypeImpl) o).classType);
    }
    
    public int hashCode() {
        return classType.hashCode() + 1000;
    }
}
