/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
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
import org.netbeans.modules.debugger.jpda.expr.EvaluatorVisitor;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InterfaceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;

/**
 *
 * @author Martin Entlicher
 */
public class JPDAClassTypeImpl implements JPDAClassType {
    
    private static final Logger loggerValue = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N
    
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
        ClassObjectReference co;
        try {
            co = ReferenceTypeWrapper.classObject(classType);
        } catch (InternalExceptionWrapper ex) {
            co = null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            co = null;
        } catch (UnsupportedOperationExceptionWrapper ex) {
            co = null; // J2ME does not support this.
        }
        return new ClassVariableImpl(debugger, co, "");
    }
    
    public ObjectVariable getClassLoader() {
        ClassLoaderReference cl;
        try {
            cl = ReferenceTypeWrapper.classLoader(classType);
        } catch (InternalExceptionWrapper ex) {
            cl = null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            cl = null;
        }
        return new AbstractObjectVariable(debugger, cl, "Loader "+getName());
    }
    
    public SuperVariable getSuperClass() {
        if (classType instanceof ClassType) {
            try {
                return new SuperVariable(debugger, null, ClassTypeWrapper.superclass((ClassType) classType), getName());
            } catch (InternalExceptionWrapper ex) {
                return null;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public List<JPDAClassType> getSubClasses() {
        if (classType instanceof ClassType) {
            List<ClassType> subclasses = ClassTypeWrapper.subclasses0((ClassType) classType);
            if (subclasses.size() > 0) {
                List<JPDAClassType> subClasses = new ArrayList(subclasses.size());
                for (ClassType subclass : subclasses) {
                    subClasses.add(new JPDAClassTypeImpl(debugger, subclass));
                }
                return Collections.unmodifiableList(subClasses);
            }
        }
        if (classType instanceof InterfaceType) {
            List<InterfaceType> subinterfaces = InterfaceTypeWrapper.subinterfaces0((InterfaceType) classType);
            List<ClassType> implementors = InterfaceTypeWrapper.implementors0((InterfaceType) classType);
            int ss = subinterfaces.size();
            int is = implementors.size();
            if (ss > 0 || is > 0) {
                List<JPDAClassType> subClasses = new ArrayList(ss + is);
                for (InterfaceType subclass : subinterfaces) {
                    subClasses.add(new JPDAClassTypeImpl(debugger, subclass));
                }
                for (ClassType subclass : implementors) {
                    subClasses.add(new JPDAClassTypeImpl(debugger, subclass));
                }
                return Collections.unmodifiableList(subClasses);
            }
        }
        return Collections.EMPTY_LIST;
    }

    public boolean isInstanceOf(String className) {
        List<ReferenceType> classTypes;
        try {
            classTypes = VirtualMachineWrapper.classesByName(MirrorWrapper.virtualMachine(classType), className);
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        for (ReferenceType rt : classTypes) {
            if (EvaluatorVisitor.instanceOf(classType, rt)) {
                return true;
            }
        }
        return false;
    }

    public List<Field> staticFields() {
        List<com.sun.jdi.Field> allFieldsOrig;
        try {
            allFieldsOrig = ReferenceTypeWrapper.allFields0(classType);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return Collections.emptyList();
        }
        List<Field> staticFields = new ArrayList<Field>();
        for (int i = 0; i < allFieldsOrig.size(); i++) {
            Value value = null;
            com.sun.jdi.Field origField = allFieldsOrig.get(i);
            try {
                if (TypeComponentWrapper.isStatic(origField)) {
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("STARTED : " + classType + ".getValue(" + origField + ")");
                    }
                    value = ReferenceTypeWrapper.getValue(classType, origField);
                    if (loggerValue.isLoggable(Level.FINE)) {
                        loggerValue.fine("FINISHED: " + classType + ".getValue(" + origField + ") = " + value);
                    }
                    if (value instanceof PrimitiveValue) {
                        staticFields.add(new FieldVariable(debugger, (PrimitiveValue) value, origField, "", (ObjectReference) null));
                    } else {
                        staticFields.add(new ObjectFieldVariable(debugger, (ObjectReference) value, origField, "", (ObjectReference) null));
                    }
                }
            } catch (InternalExceptionWrapper ex) {
            } catch (VMDisconnectedExceptionWrapper ex) {
                return Collections.emptyList();
            }
        }
        return staticFields;
    }
    
    public long getInstanceCount() {//boolean refresh) {
        if (JPDAUtils.IS_JDK_16) {
            /*synchronized (this) {
                if (!refresh && cachedInstanceCount > -1L) {
                    return cachedInstanceCount;
                }
            }*/
            try {
                long[] counts = VirtualMachineWrapper.instanceCounts(MirrorWrapper.virtualMachine(classType), Collections.singletonList(classType));
                return counts[0];
            } catch (InternalExceptionWrapper ex) {
                return 0L;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return 0L;
            }
            /*synchronized (this) {
                cachedInstanceCount = counts[0];
            }*/
        } else {
            return 0L;
        }
    }
    
    public List<ObjectVariable> getInstances(long maxInstances) {
        if (JPDAUtils.IS_JDK_16) {
            final List<ObjectReference> instances;
            try {
                instances = ReferenceTypeWrapper.instances(classType, maxInstances);
            } catch (VMDisconnectedExceptionWrapper ex) {
                return Collections.emptyList();
            } catch (InternalExceptionWrapper ex) {
                return Collections.emptyList();
            }
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
