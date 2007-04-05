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

import com.sun.tools.profiler.heap.Field;
import com.sun.tools.profiler.heap.FieldValue;
import com.sun.tools.profiler.heap.Instance;
import com.sun.tools.profiler.heap.JavaClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAArrayType;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;

/**
 *
 * @author Martin Entlicher
 */
public class JavaClassImpl implements JavaClass {
    
    private JPDAClassType classType;
    private long instanceCount = -1L;
    
    /** Creates a new instance of JavaClassImpl */
    public JavaClassImpl(JPDAClassType classType) {
        this.classType = classType;
    }

    public JavaClassImpl(JPDAClassType classType, long instanceCount) {
        this.classType = classType;
        this.instanceCount = instanceCount;
    }

    public long getJavaClassId() {
        // TODO ??
        return classType.hashCode();
    }

    public Instance getClassLoader() {
        return InstanceImpl.createInstance(classType.getClassLoader(), -1);
    }

    public JavaClass getSuperClass() {
        Super superClass = classType.getSuperClass();
        if (superClass != null) {
            return new JavaClassImpl(superClass.getClassType());
        } else {
            return null;
        }
    }

    public int getInstanceSize() {
        return -1;
    }

    public int getAllInstancesSize() {
        return -1;
    }

    public List<Field> getFields() {
        return Collections.emptyList();
        // TODO
        /*List<com.sun.jdi.Field> refFields = classType.fields();
        List<Field> fields = new ArrayList<Field>(refFields.size());
        for (com.sun.jdi.Field field : refFields) {
            fields.add(new FieldImpl(field));
        }
        return fields;*/
    }

    public List<FieldValue> getStaticFieldValues() {
        List<org.netbeans.api.debugger.jpda.Field> refFields = classType.staticFields();
        List<FieldValue> fields = new ArrayList<FieldValue>(refFields.size());
        for (org.netbeans.api.debugger.jpda.Field field : refFields) {
            if (field.isStatic()) {
                if (field instanceof ObjectVariable) {
                    fields.add(new ObjectFieldValueImpl(null, field, InstanceImpl.createInstance((ObjectVariable) field)));
                } else {
                    fields.add(new FieldValueImpl(null, field));
                }
            }
        }
        return fields;
    }

    public List<Instance> getInstances() {
        List<ObjectVariable> typeInstances = classType.getInstances(0);
        List<Instance> instances = new ArrayList<Instance>(typeInstances.size());
        int i = 1;
        for (ObjectVariable inst : typeInstances) {
            Instance instance = InstanceImpl.createInstance(inst, i++);
            instances.add(instance);
        }
        return instances;
    }

    public int getInstancesCount() {
        if (instanceCount != -1L) {
            return (int) instanceCount;
        }
        //return (int) Java6Methods.instanceCounts(refType.virtualMachine(), Collections.singletonList(refType))[0];
        return (int) classType.getInstanceCount();
    }

    public String getName() {
        return classType.getName();
    }

    public boolean isArray() {
        return classType instanceof JPDAArrayType;
    }
    
    public List<JavaClass> getSubClasses() {
        return null;
    }

}
