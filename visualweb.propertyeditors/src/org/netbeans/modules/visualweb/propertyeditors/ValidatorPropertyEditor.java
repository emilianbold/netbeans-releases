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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.faces.FacesDesignProject;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.LengthValidator;
import javax.faces.validator.LongRangeValidator;
import javax.faces.validator.Validator;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.ResolveResult;
import java.util.Iterator;
import java.util.TreeSet;
import javax.el.MethodExpression;

/**
 * An editor for properties on JSF components that take a binding to a
 * validation method. The property may be of type {@link javax.faces.el.MethodBinding},
 * {@link javax.el.MethodExpression}, or {@link java.lang.String}. The editor allows
 * the user to choose from among existing validation beans in the project, validation
 * methods defined on the current page, session or application beans. Also, a new
 * validation bean may be created and bound to.
 *
 * @author gjmurphy
 */
public class ValidatorPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.ValidatorPropertyEditor {

    private static final Class[] VALIDATE_PARAMS = new Class[] {
        FacesContext.class, UIComponent.class, Object.class
    };

    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.propertyeditors.Bundle"); //NOI18N

    // Some default JSF validators, used in the event that the project context cannot
    // be scanned for converters
    private static final Class[] defaultFacesValidatorClasses = new Class[] {
        DoubleRangeValidator.class,
        LengthValidator.class,
        LongRangeValidator.class
    };

    public String[] getTags() {
        DesignBean[] validatorBeans = this.getValidatorBeans();
        String[] validatorLabels = this.getValidatorLabels();
        String[] tags = new String[validatorBeans.length + validatorLabels.length + 1];
        int index = 0;
        tags[index++] = "";
        for (int i = 0; i < validatorBeans.length; i++) {
            tags[index++] = validatorBeans[i].getInstanceName();
        }
        for (int i = 0; i < validatorLabels.length; i++) {
            tags[index++] = validatorLabels[i];
        }
        return tags;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty != null) {
            FacesDesignContext context =
                    (FacesDesignContext) designProperty.getDesignBean().getDesignContext();
            // A hack to prevent the user from overwriting a binding that is persisting
            // the validate event handler
            if (text == null || text.trim().length() == 0) {
                Object value = this.getValue();
                if (value != null && (value instanceof MethodBinding || value instanceof MethodExpression)) {
                    String expressionString = null;
                    if (value instanceof MethodBinding)
                        expressionString = ((MethodBinding) value).getExpressionString();
                    else
                        expressionString = ((MethodExpression) value).getExpressionString();
                    ResolveResult result = context.resolveBindingExprToBean(expressionString);
                    DesignBean designBean = result.getDesignBean();
                    if (!(designBean.getInstance() instanceof Validator))
                        return;
                }
                setValue(null);
                return;
            }
            
            if (text.startsWith("#{")) { //NOI18N
                setValidatorMethodBinding(text);
                return;
            }
            
            DesignBean[] validatorBeans = this.getValidatorBeans();
            for (int i = 0; i < validatorBeans.length; i++) {
                if (validatorBeans[i].getInstanceName().equals(text)) {
                    setValidatorBeanBinding(validatorBeans[i]);
                    return;
                }
            }
            
            String[] validatorLabels = this.getValidatorLabels();
            Class[] validatorClasses = this.getValidatorClasses();
            for (int i = 0; i < validatorLabels.length; i++) {
                if (validatorLabels[i].equals(text)) {
                    DesignBean bean = context.createBean(validatorClasses[i].getName(), null, null);
                    if (bean != null) {
                        setValidatorBeanBinding(bean);
                        return;
                    }
                }
            }
        }
    }
    
    public String getAsText() {
        Object value = getValue();
        if (value == null)
            return "";
        if (MethodExpression.class.isAssignableFrom(value.getClass()) || MethodBinding.class.isAssignableFrom(value.getClass())) {
            DesignProperty designProperty = this.getDesignProperty();
            String expression;
            if (MethodExpression.class.isAssignableFrom(value.getClass()))
                expression = ((MethodExpression) value).getExpressionString();
            else
                expression = ((MethodBinding) value).getExpressionString();
            // If the method binding expression refers to a method on a validator
            // bean, return just the bean's instance name. Otherwise, the method
            // binding must refer to a validate method defined on a page, request,
            // session, or application bean. In this case return the expression.
            if (designProperty != null) {
                FacesDesignContext designContext = (FacesDesignContext) designProperty.getDesignBean().getDesignContext();
                ResolveResult result = designContext.resolveBindingExprToBean(expression);
                DesignBean lbean = result.getDesignBean();
                if (lbean != null && lbean.getInstance() instanceof Validator)
                    return lbean.getInstanceName();
                return expression;
            }
        }
        return value.toString();
    }
    
    public String getJavaInitializationString() {
        return "null"; //NOI18N
    }
    
    private void setValidatorMethodBinding(String binding) {
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty == null) {
            setValue(null);
        } else {
            Class propertyType = designProperty.getPropertyDescriptor().getPropertyType();
            FacesDesignContext context =
                    (FacesDesignContext) designProperty.getDesignBean().getDesignContext();
            Application application = context.getFacesContext().getApplication();
            if (MethodBinding.class.isAssignableFrom(propertyType)) {
                MethodBinding methodBinding = application.createMethodBinding(binding, VALIDATE_PARAMS);
                setValue(methodBinding);
            } else if (MethodExpression.class.isAssignableFrom(propertyType)) {
                MethodExpression methodExpr = application.getExpressionFactory().createMethodExpression(
                        context.getFacesContext().getELContext(), binding, null, VALIDATE_PARAMS);
                setValue(methodExpr);
            } else {
                setValue(binding);
            }
        }
    }
    
    private void setValidatorBeanBinding(DesignBean sourceBean) {
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty != null) {
            FacesDesignContext context =
                    (FacesDesignContext) designProperty.getDesignBean().getDesignContext();
            String binding = context.getBindingExpr(sourceBean, ".validate"); //NOI18N
            setValidatorMethodBinding(binding);
        }
    }
    
    private DesignBean[] getValidatorBeans() {
        DesignProperty designProperty = this.getDesignProperty();
        return (designProperty == null) ? new DesignBean[0] :
            designProperty.getDesignBean().getDesignContext().getBeansOfType(Validator.class);
    }
    
    private static Comparator validatorComparator = new Comparator() {
        public int compare(Object obj1, Object obj2) {
            String name1 = ((Class) obj1).getName();
            String name2 = ((Class) obj2).getName();
            return name1.substring(name1.lastIndexOf('.') + 1).compareTo(name2.substring(name2.lastIndexOf('.') + 1));
        }
    };
    
    // A global map of validator IDs to classes, to avoid expensive repetitive lookups.
    // If a new component library is imported into the IDE, any new validators will
    // be discovered and added to the map.
    private static HashMap validatorIdMap = new HashMap();
    
    private Class[] validatorClasses;
    
    /**
     * Generates an array of classes for all validator components registered with the
     * design-time JSF application.
     */
    protected Class[] getValidatorClasses() {
        if (validatorClasses != null)
            return validatorClasses;
        DesignProperty designProperty = this.getDesignProperty();
        if (designProperty == null)
            return defaultFacesValidatorClasses;
        FacesDesignProject facesDesignProject = (FacesDesignProject)designProperty.getDesignBean().getDesignContext().getProject();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(facesDesignProject.getContextClassLoader());
            FacesContext facesContext =
                    ((FacesDesignContext) designProperty.getDesignBean().getDesignContext()).getFacesContext();
            Application application = facesContext.getApplication();
            Iterator iter = application.getValidatorIds();
            TreeSet set = new TreeSet(validatorComparator);
            while (iter.hasNext()) {
                String id = (String) iter.next();
                if (!validatorIdMap.containsKey(id)) {
                    Validator validator = application.createValidator(id);
                    validatorIdMap.put(id, validator.getClass());
                }
                set.add(validatorIdMap.get(id));
            }
            validatorClasses = new Class[set.size()];
            iter = set.iterator();
            for (int i = 0; i < validatorClasses.length; i++)
                validatorClasses[i] = (Class) iter.next();
        } finally{
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
        return validatorClasses;
    }
    
    private String[] validatorLabels;
    
    /**
     * Generates an array of display labels for all converter classes.
     */
    protected String[] getValidatorLabels() {
        if (validatorLabels != null)
            return validatorLabels;
        Class[] validatorClasses = getValidatorClasses();
        validatorLabels = new String[validatorClasses.length];
        MessageFormat labelFormat =
                new MessageFormat(bundle.getString("ValidatorPropertyEditor.newValidatorLabel")); //NOI18N
        Object[] args = new Object[1];
        for (int i = 0; i < validatorClasses.length; i++) {
            String name = validatorClasses[i].getName();
            args[0] = name.substring(name.lastIndexOf('.') + 1);
            validatorLabels[i] = labelFormat.format(args);
        }
        return validatorLabels;
    }
    
}
