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
package com.sun.jsfcl.std;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.LongRangeValidator;
import javax.faces.validator.Validator;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.ResolveResult;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * @deprecated
 */
public class ValidatorRefPropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {
    
    private static final ComponentBundle bundle = ComponentBundle.getBundle(
            ValidatorRefPropertyEditor.class);
    
    
    private DesignProperty liveProperty;
    private PropertyEnv propertyEnv;
    
    /* (non-Javadoc)
     * @see java.beans.PropertyEditor#getValue()
     */
    public Object getValue() {
        Object value = super.getValue();
        return value;
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyEditor#setValue(java.lang.Object)
     */
    public void setValue(Object v) {
        super.setValue(v);
    }
    
    private static final Class[] classes = new Class[] {
        DoubleRangeValidator.class,
        LengthValidator.class,
        LongRangeValidator.class
    };
    
    private static final String[] prettyClasses = new String[] {
        bundle.getMessage("parenNewDRV"), //NOI18N
        bundle.getMessage("parenNewLV"), //NOI18N
        bundle.getMessage("parenNewLRV") //NOI18N
    };
    
    public String[] getTags() {
        DesignBean[] lbeans = getValidatorBeans();
        String[] tags = new String[lbeans.length + prettyClasses.length + 1];
        
        int index = 0;
        tags[index++] = "";
        for (int i = 0; i < lbeans.length; i++) {
            tags[index++] = lbeans[i].getInstanceName();
        }
        for (int i = 0; i < prettyClasses.length; i++) {
            tags[index++] = prettyClasses[i];
        }
        return tags;
    }
    
    private void setValidatorMethodBinding(String binding) {
        FacesDesignContext fctx = (FacesDesignContext)liveProperty.getDesignBean().getDesignContext();
        Application app = fctx.getFacesContext().getApplication();
        MethodBinding mb = app.createMethodBinding(binding,
                new Class[] {
            FacesContext.class, UIComponent.class, Object.class});
            setValue(mb);
    }
    
    private void setValidatorBeanBinding(DesignBean sourceBean) {
        FacesDesignContext fctx = (FacesDesignContext)liveProperty.getDesignBean().getDesignContext();
        String binding = fctx.getBindingExpr(sourceBean, ".validate"); //NOI18N
        setValidatorMethodBinding(binding);
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        
        if (text == null || text.trim().length() == 0) {
            // A hack to prevent user from overwriting a binding that is persisting
            // the validate event handler
            Object value = this.getValue();
            if (liveProperty != null && value != null && value instanceof MethodBinding) {
                FacesDesignContext designContext =
                        (FacesDesignContext) liveProperty.getDesignBean().getDesignContext();
                ResolveResult result =
                        designContext.resolveBindingExprToBean(((MethodBinding) value).getExpressionString());
                DesignBean lbean = result.getDesignBean();
                if (!(lbean.getInstance() instanceof Validator))
                    return;
            }
            setValue(null);
            return;
        }
        
        if (text.startsWith("#{")) { //NOI18N
            setValidatorMethodBinding(text);
        } else {
            DesignBean[] lbeans = getValidatorBeans();
            for (int i = 0; i < lbeans.length; i++) {
                if (lbeans[i].getInstanceName().equals(text)) {
                    setValidatorBeanBinding(lbeans[i]);
                    return;
                }
            }
            for (int i = 0; i < prettyClasses.length; i++) {
                if (prettyClasses[i].equals(text)) {
                    DesignBean lbean = liveProperty.getDesignBean().getDesignContext().createBean(
                            classes[i].getName(), null, null);
                    if (lbean != null) {
                        setValidatorBeanBinding(lbean);
                        return;
                    }
                }
            }
        }
    }
    
    public String getAsText() {
        Object value = getValue();
        if (value instanceof MethodBinding) {
            String expression = ((MethodBinding) value).getExpressionString();
            // If the method binding expression refers to a method on a validator
            // bean, return just the bean's instance name. Otherwise, the method
            // binding must refer to a validate method defined on a page, request,
            // session, or application bean. In this case return the expression.
            if (liveProperty != null) {
                FacesDesignContext designContext = (FacesDesignContext) liveProperty.getDesignBean().getDesignContext();
                ResolveResult result = designContext.resolveBindingExprToBean(expression);
                DesignBean lbean = result.getDesignBean();
                if (lbean.getInstance() instanceof Validator)
                    return lbean.getInstanceName();
                return expression;
            }
        }
        return (value == null) ? "" : value.toString(); //NOI18N
    }
    
    public String getJavaInitializationString() {
        return "null"; //NOI18N
    }
    
    public boolean supportsCustomEditor() {
        return false;
    }
    
    public Component getCustomEditor() {
        return null;
    }
    
    public void setDesignProperty(DesignProperty liveProperty) {
        this.liveProperty = liveProperty;
    }
    
    private DesignBean[] getValidatorBeans() {
        return (liveProperty == null) ? new DesignBean[0] :
            liveProperty.getDesignBean().getDesignContext().getBeansOfType(Validator.class);
    }
    
}
