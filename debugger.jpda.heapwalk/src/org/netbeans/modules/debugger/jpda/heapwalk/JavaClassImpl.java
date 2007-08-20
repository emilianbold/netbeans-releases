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
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.debugger.jpda.JPDAArrayType;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;

import org.openide.ErrorManager;

/**
 *
 * @author Martin Entlicher
 */
public class JavaClassImpl implements JavaClass {
    
    private JPDAClassType classType;
    private long instanceCount = -1L;
    private HeapImpl heap;
    private String className;
    
    /** Creates a new instance of JavaClassImpl */
    public JavaClassImpl(HeapImpl heap, JPDAClassType classType) {
        if (classType == null) {
            throw new NullPointerException("classType == null");
        }
        this.classType = classType;
        this.heap = heap;
    }
    
    /** For the case where the class type is not loaded yet. */
    public JavaClassImpl(String className) {
        this.className = className;
    }

    public JavaClassImpl(HeapImpl heap, JPDAClassType classType, long instanceCount) {
        this.classType = classType;
        this.instanceCount = instanceCount;
        this.heap = heap;
    }

    public long getJavaClassId() {
        // TODO ??
        if (classType != null) {
            return classType.hashCode();
        } else {
            return className.hashCode();
        }
    }

    public Instance getClassLoader() {
        if (classType != null) {
            return InstanceImpl.createInstance(heap, classType.getClassLoader(), -1);
        } else {
            return null;
        }
    }

    public JavaClass getSuperClass() {
        if (classType != null) {
            Super superClass = classType.getSuperClass();
            if (superClass != null) {
                return new JavaClassImpl(heap, superClass.getClassType());
            }
        }
        return null;
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
        if (classType == null) {
            return Collections.EMPTY_LIST;
        }
        List<org.netbeans.api.debugger.jpda.Field> refFields = classType.staticFields();
        List<FieldValue> fields = new ArrayList<FieldValue>(refFields.size());
        for (org.netbeans.api.debugger.jpda.Field field : refFields) {
            if (field.isStatic()) {
                if (field instanceof ObjectVariable) {
                    Instance instance;
                    if (((ObjectVariable) field).getUniqueID() == 0L) {
                        instance = null;
                    } else {
                        instance = InstanceImpl.createInstance(heap, (ObjectVariable) field);
                    }
                    fields.add(new ObjectFieldValueImpl(heap, null, field, instance));
                } else {
                    fields.add(new FieldValueImpl(heap, null, field));
                }
            }
        }
        return fields;
    }

    public Object getValueOfStaticField(String name) {
        // TODO
        return Collections.EMPTY_LIST;
    }

    public List<Instance> getInstances() {
        if (classType == null) {
            return Collections.EMPTY_LIST;
        }
        List<ObjectVariable> typeInstances = classType.getInstances(0);
        List<Instance> instances = new ArrayList<Instance>(typeInstances.size());
        int i = 1;
        for (ObjectVariable inst : typeInstances) {
            Instance instance = InstanceImpl.createInstance(heap, inst, i++);
            instances.add(instance);
        }
        return instances;
    }

    public int getInstancesCount() {
        if (instanceCount != -1L) {
            return (int) instanceCount;
        }
        //return (int) Java6Methods.instanceCounts(refType.virtualMachine(), Collections.singletonList(refType))[0];
        if (classType == null) {
            return 0;
        }
        return (int) classType.getInstanceCount();
    }

    public String getName() {
        if (classType != null) {
            return classType.getName();
        } else {
            return className;
        }
    }

    public boolean isArray() {
        return classType instanceof JPDAArrayType;
    }
    
    public List<JavaClass> getSubClasses() {
        if (classType != null) {
            try {
                java.lang.reflect.Method getSubClassesMethod = classType.getClass().getMethod("getSubClasses", new Class[0]);
                List<JPDAClassType> subclasses = (List<JPDAClassType>) getSubClassesMethod.invoke(classType, new Object[0]);
                if (subclasses.size() > 0) {
                    long[] counts = heap.getDebugger().getInstanceCounts(subclasses);
                    List<JavaClass> subClasses = new ArrayList<JavaClass>(subclasses.size());
                    int i = 0;
                    for (JPDAClassType subclass : subclasses) {
                        subClasses.add(new JavaClassImpl(heap, subclass, counts[i++]));
                    }
                    return Collections.unmodifiableList(subClasses);
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        /*
        if (classType != null) {
            List<JPDAClassType> subclasses = classType.getSubClasses();
            if (subclasses.size() > 0) {
                List<JavaClass> subClasses = new ArrayList<JavaClass>(subclasses.size());
                for (JPDAClassType subclass : subclasses) {
                    subClasses.add(new JavaClassImpl(heap, subclass));
                }
                return Collections.unmodifiableList(subClasses);
            }
        }
         */
        return Collections.EMPTY_LIST;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JavaClassImpl)) {
            return false;
        }
        JavaClassImpl jc = (JavaClassImpl) obj;
        if (classType != null && classType.equals(jc.classType)) {
            return true;
        }
        if (className != null && className.equals(jc.className)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (classType != null) {
            return classType.hashCode();
        }
        return className.hashCode() + 1024;
    }

}
