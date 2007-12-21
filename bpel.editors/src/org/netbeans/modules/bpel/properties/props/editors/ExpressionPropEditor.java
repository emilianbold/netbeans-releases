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
package org.netbeans.modules.bpel.properties.props.editors;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Expression;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.soa.ui.form.ReusablePropertyCustomizer;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author nk160297
 */
public abstract class ExpressionPropEditor<T extends Expression>
        extends PropertyEditorSupport implements ExPropertyEditor {
    
    protected Component customizer = null;
    
    protected PropertyEnv myPropertyEnv;
    
    protected abstract T createNewExpression(BpelModel model);
    
    protected ExpressionPropEditor() {
    }
    
    public String getAsText() {
        Expression val = (Expression)getValue();
        if (val == null) {
            return "";
        } else {
            return val.getContent().trim();
        }
    }
    
    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if (text == null || text.length() == 0) {
            FeatureDescriptor fd = myPropertyEnv.getFeatureDescriptor();
            if (fd instanceof PropertyUtils.Reflection) {
                PropertyUtils.Reflection property = (PropertyUtils.Reflection)fd;
                if (!property.canRemove()) {
                    try {
                        Expression oldValue = (Expression)property.getValue();
                        if (oldValue == null) {
                            // Skip assignment if the old value hasn't been specified
                            return;
                        }
                        String oldContent = oldValue.getContent();
                        if(oldContent == null || oldContent.length() == 0) {
                            // Skip assignment if the old value has been empty as well.
                            return;
                        }
                    } catch (IllegalAccessException ex) {
                        ErrorManager.getDefault().notify(ex);
                    } catch (InvocationTargetException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
            //
            setValue(null);
        } else {
            Expression currVal = (Expression)getValue();
            try {
                if (currVal != null) {
                    currVal.setContent(text);
                } else {
                    BpelNode node = getNode();
                    BpelEntity bpelEntity = (BpelEntity)node.getReference();
                    if (bpelEntity != null) {
                        BpelModel bpelModel = bpelEntity.getBpelModel();
                        if (bpelModel != null) {
                            T newExpression = createNewExpression(bpelModel);
                            newExpression.setContent(text);
                            setValue(newExpression);
                        }
                    }
                }
            } catch (VetoException ex) {
                IllegalArgumentException newEx = new IllegalArgumentException(ex);
                throw newEx;
            }
        }
    }
    
    public void attachEnv(PropertyEnv propertyEnv) {
        myPropertyEnv = propertyEnv;
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        // return new TextCustomEditorNew(env, this);
        if (customizer == null) {
            Class customizerClass = getCustomizerClass();
            if (customizerClass != null &&
                    ReusablePropertyCustomizer.class.
                    isAssignableFrom(customizerClass)) {
                customizer = PropertyUtils.propertyCustomizerPool.
                        getObjectByClass(customizerClass);
            } else {
                customizer = createCustomizer();
            }
        }
        if (customizer instanceof ReusablePropertyCustomizer) {
            ((ReusablePropertyCustomizer)customizer).init(myPropertyEnv, this);
        }
        return customizer;
    }
    
    protected Class getCustomizerClass() {
        return StringPropertyCustomizer.class;
    }
    
    protected Component createCustomizer() {
        return new StringPropertyCustomizer();
    }
    
    public BpelNode getNode() {
        Object[] beans = myPropertyEnv.getBeans();
        BpelNode node = (BpelNode)beans[0];
        return node;
    }
}
