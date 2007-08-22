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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class ManagedBeanImpl extends DescriptionGroupImpl implements ManagedBean {
    
    // caching properties
    private String beanName;
    private String beanClass;
    private ManagedBean.Scope beanScope;
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>();
    {
        SORTED_ELEMENTS.addAll(DescriptionGroupImpl.SORTED_ELEMENTS);
        SORTED_ELEMENTS.add(JSFConfigQNames.MANAGED_BEAN_NAME.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.MANAGED_BEAN_CLASS.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.MANAGED_BEAN_SCOPE.getLocalName());
    }
    
    /** Creates a new instance of ManagedBeanImpl */
    public ManagedBeanImpl(JSFConfigModelImpl model,Element element) {
        super(model, element);
        beanName = null;
        beanClass = null;
        beanScope = null;
        
        this.addPropertyChangeListener(new PropertyChangeListener () {
            
            public void propertyChange(PropertyChangeEvent event) {
                // The managed bean was changed -> reset all cache fields
                // When user modifies the source file by hand, then the property name
                // is "textContent", so it's easier to reset all fields, then 
                // parse the new value.
                beanName = null;
                beanClass = null;
                beanScope = null;
            }
            
        });
        
    }
    
    public ManagedBeanImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.MANAGED_BEAN));
    }
    
    public String getManagedBeanName() {
        if (beanName == null) {
            beanName = getChildElementText(JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getModel().getVersion()));
        }
        return beanName;
    }
    
    public void setManagedBeanName(String name) {
        setChildElementText(MANAGED_BEAN_NAME, name, JSFConfigQNames.MANAGED_BEAN_NAME.getQName(getModel().getVersion()));
    }
    
    public String getManagedBeanClass() {
        if (beanClass ==  null) {
            beanClass = getChildElementText(JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getModel().getVersion()));
        }
        return beanClass;
    }
    
    public void setManagedBeanClass(String beanClass) {
        setChildElementText(MANAGED_BEAN_CLASS, beanClass, JSFConfigQNames.MANAGED_BEAN_CLASS.getQName(getModel().getVersion()));
    }
    
    public ManagedBean.Scope getManagedBeanScope() {
        if (beanScope == null) {
            String scopeText = getChildElementText(JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getModel().getVersion()));
            scopeText = scopeText.trim().toUpperCase();
            try{
                beanScope = ManagedBean.Scope.valueOf(scopeText);
            }
            catch (IllegalArgumentException exception){
                // do nothing. The value is wrong and the method should return null. 
            }
        }
        return beanScope;
    }
    
    public void setManagedBeanScope(ManagedBean.Scope scope) {
        setChildElementText(MANAGED_BEAN_SCOPE, scope.toString(), JSFConfigQNames.MANAGED_BEAN_SCOPE.getQName(getModel().getVersion()));
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
