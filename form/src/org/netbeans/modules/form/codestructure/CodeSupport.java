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

package org.netbeans.modules.form.codestructure;

import java.util.*;
import java.lang.reflect.*;

/**
 * @author Tomas Pavek
 */

class CodeSupport {

    private CodeSupport() {
    }

    // ----------
    // implementation classes of CodeConnection interface

    static final class MethodConnection extends AbstractCodeConnection {
        private Method performMethod;
        private CodeElement[] parameters;

        public MethodConnection(CodeElement el, Method m, CodeElement[] params)
        {
            super(el);
            performMethod = m;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        public Object getConnectingObject() {
            return performMethod;
        }

        public CodeElement[] getConnectionParameters() {
            return parameters;
        }
        
        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            if (parentStr != null && !parentStr.equals("")) {
                buf.append(parentStr);
                buf.append("."); // NOI18N
            }

            buf.append(performMethod.getName());
            buf.append("("); // NOI18N

            for (int i=0; i < paramsStr.length; i++) {
                buf.append(paramsStr[i]);
                if (i+1 < parameters.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(");"); // NOI18N
            // we do add ; at the end

            return buf.toString();
        }
    }

    static final class FieldConnection extends AbstractCodeConnection {
        private Field assignField;
        private CodeElement[] parameters;

        public FieldConnection(CodeElement el, Field f, CodeElement assignedEl)
        {
            super(el);
            assignField = f;
            parameters = new CodeElement[] { assignedEl };
        }

        public Object getConnectingObject() {
            return assignField;
        }

        public CodeElement[] getConnectionParameters() {
            return parameters;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            if (parentStr != null && !parentStr.equals("")) {
                buf.append(parentStr);
                buf.append("."); // NOI18N
            }

            buf.append(assignField.getName());
            buf.append(" = "); // NOI18N
            buf.append(paramsStr[0]);
            buf.append(";"); // NOI18N
            // we do add ; at the end

            return buf.toString();
        }
    }

    static final class AssignVariableConnection extends AbstractCodeConnection {
        private CodeElementVariable variable;

        public AssignVariableConnection(CodeElementVariable var,
                                        CodeElement element)
        {
            super(element);
            variable = var;
        }

        public Object getConnectingObject() {
            return null;
        }

        public CodeElement[] getConnectionParameters() {
            return parentElement.getOrigin().getCreationParameters();
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
//            CodeElementVariable var = parentElement.getVariable();
//            if (var == null)
//                return null;
            StringBuffer buf = new StringBuffer();
            int varType = variable.getType();

            int declareMask = CodeElementVariable.LOCAL |
                              CodeElementVariable.EXPLICIT_DECLARATION;
            if ((varType & declareMask) == CodeElementVariable.LOCAL) {
                buf.append(variable.getDeclaredType().getName()
                                                       .replace('$','.'));
                buf.append(" "); // NOI18N
            }

            buf.append(variable.getName());
            buf.append(" = "); // NOI18N
            buf.append(parentElement.getOrigin().getJavaCodeString(
                                                     parentStr, paramsStr));
            buf.append(";");

            return buf.toString();
        }
    }

    static final class DeclareVariableConnection extends AbstractCodeConnection
    {
        private CodeStructure.Variable variable;

        public DeclareVariableConnection(CodeStructure.Variable var) {
            super(null);
            variable = var;
        }

        public Object getConnectingObject() {
            return null;
        }

        public CodeElement[] getConnectionParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            int type = variable.getType();
            if ((type & CodeElementVariable.SCOPE_MASK) == CodeElementVariable.FIELD) {
                if ((type & CodeElementVariable.TRANSIENT) == CodeElementVariable.TRANSIENT)
                    buf.append("transient "); // NOI18N
                if ((type & CodeElementVariable.STATIC) == CodeElementVariable.STATIC)
                    buf.append("static "); // NOI18N

                int access = type & CodeElementVariable.ACCESS_MASK;
                if (access == CodeElementVariable.DEFAULT_ACCESS)
                    access = variable.getDefaultAccessType();

                switch (access) {
                    case CodeElementVariable.PUBLIC:
                        buf.append("public "); // NOI18N
                        break;
                    case CodeElementVariable.PROTECTED:
                        buf.append("protected "); // NOI18N
                        break;
                    case CodeElementVariable.PACKAGE_PRIVATE:
                        break;
                    case CodeElementVariable.PRIVATE:
                    default:
                        buf.append("private "); // NOI18N
                        break;
                }

                if ((type & CodeElementVariable.FINAL) == CodeElementVariable.FINAL)
                    buf.append("final "); // NOI18N
            }

            buf.append(variable.getDeclaredType().getName().replace('$','.'));
            buf.append(" "); // NOI18N
            buf.append(variable.getName());
            buf.append(";"); // NOI18N

            return buf.toString();
        }
    }

    // ------------
    // implementation classes of CodeElementOrigin interface

    static final class ConstructorOrigin implements CodeElementOrigin {
        private Constructor constructor;
        private CodeElement[] parameters;

        public ConstructorOrigin(Constructor ctor, CodeElement[] params) {
            constructor = ctor;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        public Class getType() {
            return constructor.getDeclaringClass();
        }

        public CodeElement getParentElement() {
            return null;
        }

        public Object getCreatingObject() {
            return constructor;
        }

        public Object getValue() {
            Object[] params = new Object[parameters.length];
            for (int i=0; i < params.length; i++) {
                CodeElementOrigin paramOrigin = parameters[i].getOrigin();
                Object value = paramOrigin.getValue();
                Class type = paramOrigin.getType();
                if (value == null && type.isPrimitive())
                    return null;
                params[i] = value;
            }

            try {
                return constructor.newInstance(params);
            }
            catch (Exception ex) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ex.printStackTrace();
                return null;
            }
        }

        public CodeElement[] getCreationParameters() {
            return parameters;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            buf.append("new "); // NOI18N
            buf.append(constructor.getName());
            buf.append("(");

            for (int i=0; i < paramsStr.length; i++) {
                buf.append(paramsStr[i]);
                if (i+1 < parameters.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(")"); // NOI18N

            return buf.toString();
        }
    }

    static final class MethodOrigin implements CodeElementOrigin {
        private CodeElement parentElement;
        private Method creationMethod;
        private CodeElement[] parameters;

        public MethodOrigin(CodeElement parent, Method m, CodeElement[] params) {
            parentElement = parent;
            creationMethod = m;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        public Class getType() {
            return creationMethod.getReturnType();
        }

        public CodeElement getParentElement() {
            return parentElement;
        }

        public Object getCreatingObject() {
            return creationMethod;
        }

        public Object getValue() {
            return null;
        }

        public CodeElement[] getCreationParameters() {
            return parameters;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            if (parentElement != null) {
                if (parentStr != null && !parentStr.equals("")) {
                    buf.append(parentStr);
                    buf.append("."); // NOI18N
                }
            }
            else { // we suppose a static method
                buf.append(creationMethod.getDeclaringClass().getName()
                            .replace('$','.')); // NOI18N 
                buf.append("."); // NOI18N
            }

            buf.append(creationMethod.getName());
            buf.append("("); // NOI18N

            for (int i=0; i < paramsStr.length; i++) {
                buf.append(paramsStr[i]);
                if (i+1 < parameters.length)
                    buf.append(", "); // NOI18N
            }

            buf.append(")"); // NOI18N

            return buf.toString();
        }
    }

    static final class FieldOrigin implements CodeElementOrigin {
        private CodeElement parentElement;
        private Field originField;

        public FieldOrigin(CodeElement parent, Field f) {
            parentElement = parent;
            originField = f;
        }

        public Class getType() {
            return originField.getType();
        }

        public CodeElement getParentElement() {
            return parentElement;
        }

        public Object getCreatingObject() {
            return originField;
        }

        public Object getValue() {
            return null;
        }

        public CodeElement[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            if (parentElement != null) {
                if (parentStr != null && !parentStr.equals("")) {
                    buf.append(parentStr);
                    buf.append("."); // NOI18N
                }
            }
            else { // we suppose a static field
                buf.append(originField.getDeclaringClass().getName()
                            .replace('$','.')); // NOI18N 
                buf.append("."); // NOI18N
            }

            buf.append(originField.getName());

            return buf.toString();
        }
    }

    static final class ValueOrigin implements CodeElementOrigin {
        private Class elementType;
        private Object elementValue;
        private String javaString;

        public ValueOrigin(Class type, Object value, String javaStr) {
            elementType = type;
            elementValue = value;
            javaString = javaStr;
        }

        public Class getType() {
            return elementType;
        }

        public CodeElement getParentElement() {
            return null;
        }

        public Object getCreatingObject() {
            return null;
        }

        public Object getValue() {
            return elementValue;
        }

        public CodeElement[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return javaString;
        }
    }

    // --------
    // implementation of CodeConnectionGroup interface

    // temporary reduced implementation
    static final class DefaultConnectionGroup implements CodeConnectionGroup {

        private List connections = new ArrayList();

        public void addConnection(CodeConnection connection) {
            connections.add(connection);
        }

        public void addConnection(int index, CodeConnection connection) {
            connections.add(index, connection);
        }

        public void addGroup(CodeConnectionGroup group) {
            connections.add(group);
        }

        public void addGroup(int index, CodeConnectionGroup group) {
            connections.add(index, group);
        }

        public CodeConnection getConnection(int index) {
            Object obj = connections.get(index);
            if (obj instanceof CodeConnection)
                return (CodeConnection) obj;
            if (obj instanceof CodeConnectionGroup)
                return ((CodeConnectionGroup)obj).getConnection(0);
            return null;
        }

        public int indexOf(Object object) {
            return connections.indexOf(object);
        }

        public void remove(Object object) {
            connections.remove(object);
        }

        public void remove(int index) {
            connections.remove(index);
        }

        public void removeAll() {
            connections.clear();
        }

        public Iterator getConnectionsIterator() {
            return new ConnectionIterator();
        }

        class ConnectionIterator implements Iterator {
            int index = 0;
            int count = connections.size();
            Iterator subIter;

            public boolean hasNext() {
                if (subIter != null) {
                    if (subIter.hasNext())
                        return true;
                    subIter = null;
                    index++;
                }

                while (index < count) {
                    Object item = connections.get(index);
                    if (item instanceof CodeConnectionGroup) {
                        subIter = ((CodeConnectionGroup)item).getConnectionsIterator();
                        if (subIter.hasNext())
                            return true;
                        subIter = null;
                    }
                    else if (item instanceof CodeConnection)
                        return true; 
                    index++;
                }

                return false;
            }

            public Object next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return subIter != null ? subIter.next() :
                                         connections.get(index++);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
