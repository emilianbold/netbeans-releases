/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
