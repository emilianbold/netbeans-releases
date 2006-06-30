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

package org.netbeans.modules.form.codestructure;

import java.beans.PropertyEditor;
import java.lang.reflect.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/**
 * @author Tomas Pavek
 */

public class FormCodeSupport {

    public static CodeExpressionOrigin createOrigin(Node.Property property) {
        if (property instanceof FormProperty)
            return new FormPropertyValueOrigin((FormProperty)property);
        else
            return new PropertyValueOrigin(property);
    }

    public static CodeExpressionOrigin createOrigin(Class type,
                                                    PropertyEditor prEd)
    {
        return new PropertyEditorOrigin(type, prEd);
    }

    public static CodeExpressionOrigin createOrigin(RADComponent component) {
        return new RADComponentOrigin(component);
    }

    public static void readPropertyExpression(CodeExpression expression,
                                              Node.Property property,
                                              boolean allowChangeFiring)
    {
        FormProperty fProperty = property instanceof FormProperty ?
                                 (FormProperty) property : null;

        if (fProperty != null) {
            if (!allowChangeFiring) {
                if (fProperty.isChangeFiring())
                    fProperty.setChangeFiring(false);
                else
                    allowChangeFiring = true; // just not to set firing back
            }

            Object metaOrigin = expression.getOrigin().getMetaObject();
            if (metaOrigin instanceof PropertyEditor)
                fProperty.setCurrentEditor((PropertyEditor)metaOrigin);
        }

        try {
            property.setValue(expression.getOrigin().getValue());
        }
        catch (Exception ex) { // ignore
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        expression.setOrigin(createOrigin(property));

        if (fProperty != null && !allowChangeFiring)
            fProperty.setChangeFiring(true);
    }

    public static void readPropertyStatement(CodeStatement statement,
                                             Node.Property property,
                                             boolean allowChangeFiring)
    {
        // expecting statement with one expression parameter
        CodeExpression[] params = statement.getStatementParameters();
        if (params.length != 1)
            throw new IllegalArgumentException();

        readPropertyExpression(params[0], property, allowChangeFiring);
    }

    // --------

    static final class PropertyValueOrigin implements CodeExpressionOrigin {
        private Node.Property property;

        public PropertyValueOrigin(Node.Property property) {
            this.property = property;
        }

        public Class getType() {
            return property.getValueType();
        }

        public CodeExpression getParentExpression() {
            return null;
        }

        public Object getValue() {
            try {
                return property.getValue();
            }
            catch (Exception ex) {} // should no happen

            return null;
        }

        public Object getMetaObject() {
            return property;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            try {
                PropertyEditor pred = property.getPropertyEditor();
                pred.setValue(property.getValue());
                return pred.getJavaInitializationString();
            }
            catch (Exception ex) {} // should not happen
            return null;
        }

        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }
    }

    static final class FormPropertyValueOrigin implements CodeExpressionOrigin {
        private FormProperty property;

        public FormPropertyValueOrigin(FormProperty property) {
            this.property = property;
        }

        public Class getType() {
            return property.getValueType();
        }

        public CodeExpression getParentExpression() {
            return null;
        }

        public Object getValue() {
            try {
                return property.getRealValue();
                // [or getValue() ??]
            }
            catch (Exception ex) {} // should no happen

            return null;
        }

        public Object getMetaObject() {
            return property;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return property.getJavaInitializationString();
        }

        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }
    }

    static final class PropertyEditorOrigin implements CodeExpressionOrigin {
        private Class type;
        private PropertyEditor propertyEditor;

        public PropertyEditorOrigin(Class type, PropertyEditor prEd) {
            this.type = type;
            this.propertyEditor = prEd;
        }

        public Class getType() {
            return type;
        }

        public CodeExpression getParentExpression() {
            return null;
        }

        public Object getValue() {
            return propertyEditor.getValue();
        }

        public Object getMetaObject() {
            return propertyEditor;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            return propertyEditor.getJavaInitializationString();
        }

        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }
    }

    static final class RADComponentOrigin implements CodeExpressionOrigin {
        private RADComponent component;

        public RADComponentOrigin(RADComponent component) {
            this.component = component;
        }

        public Class getType() {
            return component.getBeanClass();
        }

        public CodeExpression getParentExpression() {
            return null;
        }

        public Object getMetaObject() {
            return component;
        }

        public Object getValue() {
            return component.getBeanInstance();
        }

        public CodeExpression[] getCreationParameters() {
            return CodeStructure.EMPTY_PARAMS;
        }

        public String getJavaCodeString(String parentStr, String[] paramsStr) {
            if (component == component.getFormModel().getTopRADComponent())
                return "this"; // NOI18N

            StringBuffer buf = new StringBuffer();

            buf.append("new "); // NOI18N
            buf.append(component.getBeanClass().getName().replace('&','.')); // NOI18N
            buf.append("()"); // NOI18N

            return buf.toString();
        }
    }
}
