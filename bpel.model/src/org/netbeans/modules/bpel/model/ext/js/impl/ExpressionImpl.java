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
package org.netbeans.modules.bpel.model.ext.js.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.js.api.Expression;
import org.netbeans.modules.bpel.model.ext.js.xam.JsElements;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionAssignOperation;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.js.api.ExpressionLanguage;
import org.netbeans.modules.bpel.model.ext.js.xam.JsAttributes;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
/**
 *
 * @author Vitaly Bychkov
 */
public class ExpressionImpl extends JsEntityImpl implements Expression {

    ExpressionImpl(JsEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    ExpressionImpl(JsEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, JsElements.EXPRESSION);
    }

    @Override
    protected BpelEntity create( Element element ) {
        return null;
    }

    @Override
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] {
                JsAttributes.INPUT_VARS,
                JsAttributes.OUTPUT_VARS,
                JsAttributes.EXPRESSION_LANGUAGE
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return Expression.class;
    }

    public EntityUpdater getEntityUpdater() {
        return JsEntityUpdater.getInstance();
    }

    public ExpressionLanguage getExpressionLanguage() {
        readLock();
        try {
            String str = getAttribute(JsAttributes.EXPRESSION_LANGUAGE);
            return ExpressionLanguage.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    public void setExpressionLanguage(ExpressionLanguage expressionLang) {
        setBpelAttribute(JsAttributes.EXPRESSION_LANGUAGE, expressionLang);
    }

    public void removeExpressionLanguage() {
        removeAttribute(JsAttributes.EXPRESSION_LANGUAGE);
    }

    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

    public List<String> getInputVariables() {
        String inVarsString = getAttribute(JsAttributes.INPUT_VARS);
        StringTokenizer stk = new StringTokenizer(inVarsString, ","); // NOI18N
        List<String> list = new ArrayList<String>();

        while (stk.hasMoreTokens()) {
            String next = stk.nextToken();
            next = next.trim();
            list.add(next);
        }
        return list;
    }

    public void setInputVariables(List<String> varList) throws VetoException {
        if (varList == null || varList.size() < 1) {
            removeAttribute(JsAttributes.INPUT_VARS);
        }
        StringBuilder builder = new StringBuilder();

        for (String var : varList) {
            builder.append(var);
            builder.append(","); // NOI18N
        }
        if (builder.length() > 0) {
            setBpelAttribute(JsAttributes.INPUT_VARS, builder.toString());
        }
    }

    public String getInputVariablesList() {
        List<String> varList = getInputVariables();

        if (varList == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        for (String var : varList) {
            builder.append(var);
            builder.append(","); // NOI18N
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        }
        return builder.toString();
    }

    public void setInputVariablesList(String value) throws VetoException{
        if (value == null) {
            removeOutputVariablesList();
        }
        setBpelAttribute(JsAttributes.INPUT_VARS, value);
    }

    public void removeInputVariablesList() {
        removeAttribute(JsAttributes.INPUT_VARS);
    }

    public List<String> getOutputVariables() {
        String inVarsString = getAttribute(JsAttributes.OUTPUT_VARS);
        StringTokenizer stk = new StringTokenizer(inVarsString, ","); // NOI18N
        List<String> list = new ArrayList<String>();

        while (stk.hasMoreTokens()) {
            String next = stk.nextToken();
            next = next.trim();
            list.add(next);
        }
        return list;
    }

    public void setOutputVariables(List<String> varList) throws VetoException {
        if (varList == null || varList.size() < 1) {
            removeAttribute(JsAttributes.OUTPUT_VARS);
        }
        StringBuilder builder = new StringBuilder();

        for (String var : varList) {
            builder.append(var);
            builder.append(","); // NOI18N
        }
        if (builder.length() > 0) {
            setBpelAttribute(JsAttributes.OUTPUT_VARS, builder.toString());
        }
    }

    public String getOutputVariablesList() {
        List<String> varList = getOutputVariables();

        if (varList == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        for (String var : varList) {
            builder.append(var);
            builder.append(","); // NOI18N
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        }
        return builder.toString();
    }

    public void setOutputVariablesList(String value) throws VetoException {
        if (value == null) {
            removeOutputVariablesList();
        }
        setBpelAttribute(JsAttributes.OUTPUT_VARS, value);
    }

    public void removeOutputVariablesList() {
        removeAttribute(JsAttributes.OUTPUT_VARS);
    }

    public String getCDataContent() {
        return getCorrectedCDataContent();
    }

    public void setCDataContent(String content) throws VetoException, IOException {
        getAttributeAccess().setCDataContent(content);
    }

    private static class JsEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new JsEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private JsEntityUpdater() {

        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof ExtensionAssignOperation) {
                ExtensionAssignOperation extAssignOp = (ExtensionAssignOperation)target;
                Expression expression = (Expression)child;
                switch (operation) {
                case ADD:
                    extAssignOp.addExtensionEntity(Expression.class, expression);
                    break;
                case REMOVE:
                    extAssignOp.remove(expression);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index,
                Operation operation)
        {
            update(target, child, operation);
        }
    }
}
