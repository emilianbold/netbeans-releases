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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "managed-bean" element represents a JavaBean, of a
 * particular class, that will be dynamically instantiated
 * at runtime (by the default VariableResolver implementation)
 * if it is referenced as the first element of a value binding
 * expression, and no corresponding bean can be identified in
 * any scope.  In addition to the creation of the managed bean,
 * and the optional storing of it into the specified scope,
 * the nested managed-property elements can be used to
 * initialize the contents of settable JavaBeans properties of
 * the created instance.
 * @author Petr Pisl
 */
public interface ManagedBean extends JSFConfigComponent, ComponentInfo{
    
    public static final String MANAGED_BEAN_NAME = JSFConfigQNames.MANAGED_BEAN_NAME.getLocalName();
    public static final String MANAGED_BEAN_CLASS = JSFConfigQNames.MANAGED_BEAN_CLASS.getLocalName();
    public static final String MANAGED_BEAN_SCOPE = JSFConfigQNames.MANAGED_BEAN_SCOPE.getLocalName();
    
    String getManagedBeanName();
    void setManagedBeanName(String name);
    
    String getManagedBeanClass();
    void setManagedBeanClass(String beanClass);
    
    String getManagedBeanScope();
    void setManagedBeanScope(String scope);
    
}
