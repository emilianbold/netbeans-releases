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
    // implementation classes of CodeStatement interface

    static final class MethodStatement extends AbstractCodeStatement {
        private Method performMethod;
        private CodeExpression[] parameters;

        public MethodStatement(CodeExpression exp,
                               Method m,
                               CodeExpression[] params)
        {
            super(exp);
            performMethod = m;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        public Object getMetaObject() {
            return performMethod;
        }

        public CodeExpression[] getStatementParameters() {
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

    static final class FieldStatement extends AbstractCodeStatement {
        private Field assignField;
        private CodeExpression[] parameters;

        public FieldStatement(CodeExpression exp,
                              Field f,
                              CodeExpression assignedExp)
        {
            super(exp);
            assignField = f;
            parameters = new CodeExpression[] { assignedExp };
        }

        public Object getMetaObject() {
            return assignField;
        }

        public CodeExpression[] getStatementParameters() {
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

    static final class AssignVariableStatement extends AbstractCodeStatement {
        private CodeVariable variable;

        public AssignVariableStatement(CodeVariable var, CodeExpression exp) {
            super(exp);
            variable = var;
        }

        public Object getMetaObject() {
            return null;
        }

        public CodeExpression[] getStatementParameters() {
            return parentExpression.getOrigin().getCreationParameters();
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();
            int varType = variable.getType();

            int declareMask = CodeVariable.SCOPE_MASK
                              | CodeVariable.DECLARATION_MASK;
            if ((varType & declareMask) == CodeVariable.LOCAL) {
                // no explicit local variable declaration, so we make the
                // declaration together with the assignment
                buf.append(variable.getDeclaredType().getName()
                                                       .replace('$','.'));
                buf.append(" "); // NOI18N
            }

            buf.append(variable.getName());
            buf.append(" = "); // NOI18N
            buf.append(parentExpression.getOrigin().getJavaCodeString(
                                                     parentStr, paramsStr));
            buf.append(";");

            return buf.toString();
        }
    }

    static final class DeclareVariableStatement extends AbstractCodeStatement {
        private CodeVariable variable;

        public DeclareVariableStatement(CodeVariable var) {
            super(null);
            variable = var;
        }

        public Object getMetaObject() {
            return null;
        }

        public CodeExpression[] getStatementParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();
            int type = variable.getType();

            if ((type & CodeVariable.SCOPE_MASK) == CodeVariable.FIELD) {
                switch (type & CodeVariable.ACCESS_MODIF_MASK) {
                    case CodeVariable.PUBLIC:
                        buf.append("public "); // NOI18N
                        break;
                    case CodeVariable.PRIVATE:
                        buf.append("private "); // NOI18N
                        break;
                    case CodeVariable.PROTECTED:
                        buf.append("protected "); // NOI18N
                        break;
                }

                if ((type & CodeVariable.STATIC) == CodeVariable.STATIC)
                    buf.append("static "); // NOI18N

                if ((type & CodeVariable.FINAL) == CodeVariable.FINAL)
                    buf.append("final "); // NOI18N

                if ((type & CodeVariable.TRANSIENT) == CodeVariable.TRANSIENT)
                    buf.append("transient "); // NOI18N

                if ((type & CodeVariable.VOLATILE) == CodeVariable.VOLATILE)
                    buf.append("volatile "); // NOI18N
            }
            else { // local variable
                if ((type & CodeVariable.FINAL) == CodeVariable.FINAL)
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
    // implementation classes of CodeExpressionOrigin interface

    static final class ConstructorOrigin implements CodeExpressionOrigin {
        private Constructor constructor;
        private CodeExpression[] parameters;

        public ConstructorOrigin(Constructor ctor, CodeExpression[] params) {
            constructor = ctor;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        public Class getType() {
            return constructor.getDeclaringClass();
        }

        public CodeExpression getParentExpression() {
            return null;
        }

        public Object getMetaObject() {
            return constructor;
        }

        public Object getValue() {
            Object[] params = new Object[parameters.length];
            for (int i=0; i < params.length; i++) {
                CodeExpressionOrigin paramOrigin = parameters[i].getOrigin();
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

        public CodeExpression[] getCreationParameters() {
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

    static final class MethodOrigin implements CodeExpressionOrigin {
        private CodeExpression parentExpression;
        private Method creationMethod;
        private CodeExpression[] parameters;

        public MethodOrigin(CodeExpression parent,
                            Method m,
                            CodeExpression[] params)
        {
            parentExpression = parent;
            creationMethod = m;
            parameters = params != null ? params : CodeStructure.EMPTY_PARAMS;
        }

        public Class getType() {
            return creationMethod.getReturnType();
        }

        public CodeExpression getParentExpression() {
            return parentExpression;
        }

        public Object getMetaObject() {
            return creationMethod;
        }

        public Object getValue() {
            return null;
        }

        public CodeExpression[] getCreationParameters() {
            return parameters;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            if (parentExpression != null) {
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

    static final class FieldOrigin implements CodeExpressionOrigin {
        private CodeExpression parentExpression;
        private Field originField;

        public FieldOrigin(CodeExpression parent, Field f) {
            parentExpression = parent;
            originField = f;
        }

        public Class getType() {
            return originField.getType();
        }

        public CodeExpression getParentExpression() {
            return parentExpression;
        }

        public Object getMetaObject() {
            return originField;
        }

        public Object getValue() {
            return null;
        }

        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            StringBuffer buf = new StringBuffer();

            if (parentExpression != null) {
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

    static final class ValueOrigin implements CodeExpressionOrigin {
        private Class expressionType;
        private Object expressionValue;
        private String javaString;

        public ValueOrigin(Class type, Object value, String javaStr) {
            expressionType = type;
            expressionValue = value;
            javaString = javaStr;
        }

        public Class getType() {
            return expressionType;
        }

        public CodeExpression getParentExpression() {
            return null;
        }

        public Object getMetaObject() {
            return null;
        }

        public Object getValue() {
            return expressionValue;
        }

        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return javaString;
        }
    }

    // --------
    // implementation of CodeGroup interface

    // temporary reduced implementation
    static final class DefaultCodeGroup implements CodeGroup {

        private List statements = new ArrayList();

        public void addStatement(CodeStatement statement) {
            statements.add(statement);
        }

        public void addStatement(int index, CodeStatement statement) {
            statements.add(index, statement);
        }

        public void addGroup(CodeGroup group) {
            statements.add(group);
        }

        public void addGroup(int index, CodeGroup group) {
            statements.add(index, group);
        }

        public CodeStatement getStatement(int index) {
            Object obj = statements.get(index);
            if (obj instanceof CodeStatement)
                return (CodeStatement) obj;
            if (obj instanceof CodeGroup)
                return ((CodeGroup)obj).getStatement(0);
            return null;
        }

        public int indexOf(Object object) {
            return statements.indexOf(object);
        }

        public void remove(Object object) {
            statements.remove(object);
        }

        public void remove(int index) {
            statements.remove(index);
        }

        public void removeAll() {
            statements.clear();
        }

        public Iterator getStatementsIterator() {
            return new StatementsIterator();
        }

        class StatementsIterator implements Iterator {
            int index = 0;
            int count = statements.size();
            Iterator subIter;

            public boolean hasNext() {
                if (subIter != null) {
                    if (subIter.hasNext())
                        return true;
                    subIter = null;
                    index++;
                }

                while (index < count) {
                    Object item = statements.get(index);
                    if (item instanceof CodeGroup) {
                        subIter = ((CodeGroup)item).getStatementsIterator();
                        if (subIter.hasNext())
                            return true;
                        subIter = null;
                    }
                    else if (item instanceof CodeStatement)
                        return true; 
                    index++;
                }

                return false;
            }

            public Object next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                return subIter != null ? subIter.next() :
                                         statements.get(index++);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
