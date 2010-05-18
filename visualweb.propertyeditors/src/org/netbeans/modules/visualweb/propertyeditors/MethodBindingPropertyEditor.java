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

import com.sun.faces.util.ConstantMethodBinding;
import com.sun.rave.designtime.ContextMethod;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.designtime.faces.ResolveResult;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import java.awt.Component;
import java.lang.reflect.Method;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ReferenceSyntaxException;

/**
 * An editor for properties on JSF components that take a binding to a method. The
 * property type must be {@link javax.faces.el.MethodBinding}. Note: this editor was
 * brought over, modus modendum, from the deprecated jsfcl module.
 *
 * @author eric
 */

public class MethodBindingPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.MethodBindingPropertyEditor {

    private static final Bundle bundle = Bundle.getBundle(MethodBindingPropertyEditor.class);

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(getAsMethodBinding(text));
    }

    public String getAsText() {
        Object value = getValue();
        if (value instanceof MethodBinding) {
            if (value instanceof ConstantMethodBinding) {
                return (String) ((MethodBinding) value).invoke(null, null); // Hack to get the constant value
            } else {
                return ((MethodBinding) value).getExpressionString();
            }
        } else if (value instanceof MethodExpression) {
            return ((MethodExpression) value).getExpressionString();
        }
        return (value == null) ? "" : value.toString(); //NOI18N
    }

    public String getJavaInitializationString() {
        return "\"" + getAsText() + "\""; //NOI18N
    }

    /**
     * Attempts to convert the expression specified into a method binding or method
     * expression.
     */
    Object getAsMethodBinding(String string) throws IllegalArgumentException {
        if (string == null)
            return null;
        string = string.trim();
        if (string.startsWith("#{") && string.endsWith("}")) { //NOI18N
            String expr = string.substring(2, string.length() - 1);
            FacesDesignProject facesDesignProject = (FacesDesignProject)designProperty.getDesignBean().getDesignContext().getProject();
 	    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                DesignProperty designProperty = this.getDesignProperty();
                FacesDesignContext pcontext = (FacesDesignContext) designProperty.getDesignBean().getDesignContext();
                Application application = pcontext.getFacesContext().getApplication();
                Class propertyType = designProperty.getPropertyDescriptor().getPropertyType();
                if (MethodExpression.class.isAssignableFrom(propertyType) || MethodBinding.class.isAssignableFrom(propertyType)) {
                    DesignContext[] contextsTmp = facesDesignProject.findDesignContexts(new String[]{"session", "application"});
                    FacesDesignContext[] contexts = new FacesDesignContext[contextsTmp.length + 1];
                    System.arraycopy(contextsTmp, 0, contexts, 1, contextsTmp.length);
                    contexts[0] = pcontext;
                    int firstDotIndex = expr.indexOf('.');
                    boolean twoOrMoreDots = firstDotIndex != -1 && firstDotIndex != expr.lastIndexOf('.');
                    FacesContext facesContext = null;
                    Class returnType = null;
                    Class[] parameterTypes = null;
                    if (firstDotIndex != -1) { //if we have at least one dot
                        for (FacesDesignContext context : contexts) {
                            //try to discover returnType and parameterTypes for this expression
                            if (twoOrMoreDots) {
                                ResolveResult result = context.resolveBindingExprToBean(string);
                                DesignBean resultDesignBean = result.getDesignBean();
                                if (resultDesignBean != null) { //will be null if "context" is Page1 and string begins with "#{SessionBean1", for instance
                                    //we have the right context
                                    Object beanInstance = resultDesignBean.getInstance();
                                    if (beanInstance != null) { //will be null if, say, foo can't be resolved in the string "#{SessionBean1.foo.bar}"
                                        Class beanClass = beanInstance.getClass();
                                        String methodName = result.getRemainder();
                                        for (Method method : beanClass.getMethods()) {
                                            if (method.getName().equals(methodName)) {
                                                facesContext = context.getFacesContext();
                                                returnType = method.getReturnType();
                                                parameterTypes = method.getParameterTypes();
                                                break;
                                            }
                                        }
                                    }
                                    break; //we've examined the right context. none of the remaining contexts will match.
                                }
                            }
                            else {  //one dot
                                String exprPrefix = expr.substring(0, firstDotIndex);
                                if (exprPrefix.equals(context.getReferenceName())) {
                                    //we have the right context
                                    //get exprSuffix, keeping in mind that the dot could be the last character
                                    String exprSuffix = (firstDotIndex == expr.length() - 1) ? "" : expr.substring(firstDotIndex + 1);
                                    ContextMethod[] contextMethods = context.getContextMethods();
                                    for (ContextMethod contextMethod : contextMethods) {
                                        if (exprSuffix.equals(contextMethod.getName())) {
                                            facesContext = context.getFacesContext();
                                            returnType = contextMethod.getReturnType();
                                            parameterTypes = contextMethod.getParameterTypes();
                                            break;
                                        }
                                    }
                                    break;  //we've examined the right context. none of the remaining contexts will match.
                                }
                            }
                        }
                    }
                    if (facesContext != null && returnType != null && parameterTypes != null) {
                        if (MethodExpression.class.isAssignableFrom(propertyType)) {
                            return application.getExpressionFactory().createMethodExpression(
                                facesContext.getELContext(), string, returnType, parameterTypes);
                        }
                        if (MethodBinding.class.isAssignableFrom(propertyType)) {
                            return application.createMethodBinding(string,parameterTypes);
                        }
                    }
                }
                return string;
            } catch (ReferenceSyntaxException e) {
                throw new IllegalTextArgumentException(
                        bundle.getMessage("MethodBindingPropertyEditor.formatErrorMessage", string), e); //NOI18N
            }finally{
                Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            }
        } else if (string.length() > 0) {
            return new ConstantMethodBinding(string);
        } else {
            return null;
        }
    }
    
    public Component getCustomEditor() {
        return new MethodBindingPropertyPanel(this);
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
}
