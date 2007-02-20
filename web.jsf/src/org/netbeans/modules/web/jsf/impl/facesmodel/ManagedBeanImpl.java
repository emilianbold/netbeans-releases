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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class ManagedBeanImpl extends JSFConfigComponentImpl.ComponentInfoImpl implements ManagedBean {
    
    /** Creates a new instance of ManagedBeanImpl */
    public ManagedBeanImpl(JSFConfigModelImpl model,Element element) {
        super(model, element);
    }
    
    public ManagedBeanImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.MANAGED_BEAN));
    }
    
    public String getManagedBeanName() {
        return getChildElementText(JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getModel().getVersion()));
    }
    
    public void setManagedBeanName(String name) {
        setChildElementText(MANAGED_BEAN_NAME, name, JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getModel().getVersion()));
    }
    
    public String getManagedBeanClass() {
        return getChildElementText(JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getModel().getVersion()));
    }
    
    public void setManagedBeanClass(String beanClass) {
        setChildElementText(MANAGED_BEAN_CLASS, beanClass, JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getModel().getVersion()));
    }
    
    public String getManagedBeanScope() {
        return getChildElementText(JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getModel().getVersion()));
    }
    
    public void setManagedBeanScope(String scope) {
        setChildElementText(MANAGED_BEAN_SCOPE, scope, JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getModel().getVersion()));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
