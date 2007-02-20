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

import java.util.List;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The roor element of the faces configuration file.
 * @author Petr Pisl
 */

public interface FacesConfig extends JSFConfigComponent {
    
    /**
     * Property for &lt;managed-bean&gt; element
     */
    public static final String MANAGED_BEAN = JSFConfigQNames.MANAGED_BEAN.getLocalName();
    /**
     * Property of &lt;navigation-rule&gt; element
     */
    public static final String NAVIGATION_RULE = JSFConfigQNames.NAVIGATION_RULE.getLocalName();
    /**
     * Property of &lt;converter&gt; element
     */
    public static final String CONVERTER = JSFConfigQNames.CONVERTER.getLocalName();
    
    
    List<Converter> getConverters();
    void addConverter(Converter converter);
    void addConverter(int index, Converter converter);
    void removeConverter(Converter converter);
    
    List <ManagedBean> getManagedBeans();
    void addManagedBean(ManagedBean bean);
    void addManagedBean(int index, ManagedBean bean);
    void removeManagedBean(ManagedBean bean);
    
    List<NavigationRule> getNavigationRules();
    void addNavigationRule(NavigationRule rule);
    void addNavigationRule(int index, NavigationRule rule);
    void removeNavigationRule(NavigationRule rule);
}
