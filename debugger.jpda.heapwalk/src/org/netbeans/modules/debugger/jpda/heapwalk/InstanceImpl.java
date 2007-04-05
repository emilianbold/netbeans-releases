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

package org.netbeans.modules.debugger.jpda.heapwalk;

import com.sun.tools.profiler.heap.FieldValue;
import com.sun.tools.profiler.heap.Instance;
import com.sun.tools.profiler.heap.JavaClass;
import com.sun.tools.profiler.heap.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.debugger.jpda.JPDAArrayType;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin Entlicher
 */
public class InstanceImpl implements Instance {
    
    private ObjectVariable var;
    private int instanceNo;
    
    /** Creates a new instance of InstanceImpl */
    protected InstanceImpl(ObjectVariable var, int instanceNo) {
        this.var = var;
        this.instanceNo = instanceNo;
    }
    
    public static Instance createInstance(ObjectVariable var) {
        JPDAClassType classType = var.getClassType();
        if (classType == null) {
            return createInstance(var, 0);
        }
        List<ObjectVariable> vars = classType.getInstances(0);
        int i = 1;
        for (ObjectVariable obj: vars) {
            if (classType.equals(obj)) {
                break;
            }
            i++;
        }
        return createInstance(var, i);
    }
    
    public static Instance createInstance(ObjectVariable var, int instanceNo) {
        Instance instance;
        JPDAClassType type = var.getClassType();
        if (type instanceof JPDAArrayType) {
            boolean isPrimitiveArray = false;
            isPrimitiveArray = !(((JPDAArrayType) type).getComponentType() instanceof JPDAClassType);
            if (isPrimitiveArray) {
                instance = new PrimitiveArrayInstanceImpl(var, instanceNo);
            } else {
                instance = new ObjectArrayInstanceImpl(var, instanceNo);
            }
        } else {
            instance = new InstanceImpl(var, instanceNo);
        }
        return instance;
    }

    public JavaClass getJavaClass() {
        return new JavaClassImpl(var.getClassType());
    }

    public long getInstanceId() {
        return var.getUniqueID();
    }

    public int getInstanceNumber() {
        return instanceNo;
    }

    public int getSize() {
        return 0;
    }

    public List<FieldValue> getFieldValues() {
        int fieldsCount = var.getFieldsCount();
        org.netbeans.api.debugger.jpda.Field[] varFields = var.getFields(0, fieldsCount);
        List<FieldValue> fields = new ArrayList<FieldValue>(varFields.length);
        for (org.netbeans.api.debugger.jpda.Field field : varFields) {
            if (!field.isStatic()) {
                if (field instanceof ObjectVariable) {
                    fields.add(new ObjectFieldValueImpl(this, field, InstanceImpl.createInstance((ObjectVariable) field)));
                } else {
                    fields.add(new FieldValueImpl(this, field));
                }
            }
        }
        return fields;
    }

    public List<FieldValue> getStaticFieldValues() {
        return getJavaClass().getStaticFieldValues();
    }

    public List<Value> getReferences() {
        List<ObjectVariable> references = var.getReferringObjects(0);
        List<Value> values = new ArrayList<Value>(references.size());
        Set referencedFields = new HashSet();
        for (ObjectVariable obj : references) {
            JPDAClassType type = obj.getClassType();
            if (type instanceof JPDAArrayType) {
                int length = obj.getFieldsCount();
                int CHUNK = 1000;
                for (int i = 0; i < length; i += CHUNK) {
                    int to = Math.min(i + CHUNK, length);
                    Variable[] items = obj.getFields(i, to - i);
                    int j = i;
                    for (Variable item: items) {
                        if (var.equals(item)) {
                            Instance instance = createInstance(obj);
                            values.add(new ArrayItemValueImpl(instance, this, j));
                            break;
                        }
                        j++;
                    }
                    if (j < to) {
                        break;
                    }
                }
            } else {
                int count = obj.getFieldsCount();
                org.netbeans.api.debugger.jpda.Field[] allFields = obj.getFields(0, count);
                for (org.netbeans.api.debugger.jpda.Field field : allFields) {
                    if (field instanceof ObjectVariable &&
                        !referencedFields.contains(field) &&
                        var.getUniqueID() == ((ObjectVariable) field).getUniqueID()) {
                        
                        referencedFields.add(field);
                        Instance instance = createInstance(obj);
                        values.add(new ObjectFieldValueImpl(instance, field, this));
                        break;
                    }
                }
            }
        }
        return values;
    }

    public boolean isGCRoot() {
        return false;
    }

    public int getRetainedSize() {
        return 0;
    }

    public int getReachableSize() {
        return 0;
    }
    
    public Instance getNearestGCRootPointer() {
        return null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InstanceImpl)) {
            return false;
        }
        return var.equals(((InstanceImpl) obj).var);
    }

    public int hashCode() {
        return var.hashCode();
    }
    
}
