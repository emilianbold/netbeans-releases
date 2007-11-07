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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form.codestructure;

import java.util.*;
import java.lang.reflect.*;

/**
 * @author Tomas Pavek
 */

class CodeSupport {

    private CodeSupport() {
    }

    static String getSourceClassName(Class cls) {
        return cls.getName().replace('$', '.').replace('+', '.').replace('/', '.'); // NOI18N
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
            return parentExpression;
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
                if ((varType & CodeVariable.FINAL) == CodeVariable.FINAL)
                    buf.append("final "); // NOI18N

                buf.append(getSourceClassName(variable.getDeclaredType()));
                buf.append(" "); // NOI18N
            }

            buf.append(variable.getName());
            buf.append(" = "); // NOI18N
            buf.append(parentExpression.getOrigin().getJavaCodeString(
                                                     parentStr, paramsStr));
            buf.append(";"); // NOI18N

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
            return variable;
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

            buf.append(getSourceClassName(variable.getDeclaredType()));
            
            String typeParameters = variable.getDeclaredTypeParameters();
            if ((typeParameters != null) && !"".equals(typeParameters)) { // NOI18N
                buf.append(typeParameters);
            }

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
                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
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
                buf.append(getSourceClassName(creationMethod.getDeclaringClass()));
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
                buf.append(getSourceClassName(originField.getDeclaringClass()));
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

        private List<Object/*CodeStatement or CodeGroup*/> statements = new ArrayList<Object>();

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
