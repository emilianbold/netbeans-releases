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


import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class FacesConfigImpl extends JSFConfigComponentImpl implements FacesConfig{
    
    /** Creates a new instance of FacesConfigImpl */
    public FacesConfigImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public FacesConfigImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.FACES_CONFIG));
    }
    
    public List<ManagedBean> getManagedBeans() {
        return getChildren(ManagedBean.class);
    }
    
    public void addManagedBean(ManagedBean bean) {
        appendChild(MANAGED_BEAN, bean);
    }
    
    public void addManagedBean(int index, ManagedBean bean) {
        insertAtIndex(MANAGED_BEAN, bean, index, ManagedBean.class);
    }
    
    public void removeManagedBean(ManagedBean bean) {
        removeChild(MANAGED_BEAN, bean);
    }
    
    public List<NavigationRule> getNavigationRules() {
        return getChildren(NavigationRule.class);
    }
    
    public void addNavigationRule(NavigationRule rule) {
        appendChild(NAVIGATION_RULE, rule);
    }
    
    public void addNavigationRule(int index, NavigationRule rule) {
        insertAtIndex(NAVIGATION_RULE, rule, index, NavigationRule.class);
    }
    public void removeNavigationRule(NavigationRule rule) {
        removeChild(NAVIGATION_RULE, rule);
    }
    
    public List<Converter> getConverters() {
        return getChildren(Converter.class);
    }
    
    public void addConverter(Converter converter) {
        appendChild(CONVERTER, converter);
    }
    
    public void addConverter(int index, Converter converter) {
        insertAtIndex(CONVERTER, converter, index, NavigationRule.class);
    }
    
    public void removeConverter(Converter converter) {
        removeChild(CONVERTER, converter);
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
