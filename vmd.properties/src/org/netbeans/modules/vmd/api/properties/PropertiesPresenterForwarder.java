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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.api.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public class PropertiesPresenterForwarder extends PropertiesPresenter {
    
    private String propertyName;
    private String[] propertyNames;
    
    public static Presenter createByReference(String propertyName) {
        return new PropertiesPresenterForwarder(propertyName);
    }
    
    public static Presenter createByNames(String propertyName, String... propertyNames) {
        return new PropertiesPresenterForwarder(propertyName, propertyNames);
    }
    
    private PropertiesPresenterForwarder(String propertyName, String... propertyNames) {
        this.propertyName = propertyName;
        this.propertyNames = propertyNames;
    }
    
    public List<DesignPropertyDescriptor> getDesignPropertyDescriptors() {
        DesignComponent component = getComponent().readProperty(propertyName).getComponent();
        
        if (component == null) 
            return Collections.<DesignPropertyDescriptor>emptyList();
        
        List<DesignPropertyDescriptor> descriptors = new ArrayList<DesignPropertyDescriptor>();
        Collection<? extends PropertiesPresenter> propertiesPresenters  = component.getPresenters(PropertiesPresenter.class);
        if (propertiesPresenters == null)
            return Collections.<DesignPropertyDescriptor>emptyList(); 
                
        for (PropertiesPresenter presenter : propertiesPresenters) {
            descriptors.addAll(filterDescriptors(presenter.getDesignPropertyDescriptors()));
        }
        
        return descriptors;
    }
    
    public List<String> getPropertiesCategories() {
        DesignComponent component = getComponent().readProperty(propertyName).getComponent();
        
        if (component == null) 
            return Collections.<String>emptyList();
        
        List<String> categories = new ArrayList<String>();
        for (PropertiesPresenter presenter : component.getPresenters(PropertiesPresenter.class)) {
            categories.addAll(presenter.getPropertiesCategories());
        }
        
        return categories;
    }
    
    private List<DesignPropertyDescriptor> filterDescriptors(List<DesignPropertyDescriptor> descriptors) {
        if (propertyNames == null || propertyNames.length == 0) {
            return descriptors;
        }
        List<DesignPropertyDescriptor> list = new ArrayList<DesignPropertyDescriptor>(descriptors.size());
        for (DesignPropertyDescriptor designPropertyDescriptor : descriptors) {
            for (String name : propertyNames) {
                if (designPropertyDescriptor.getPropertyNames().contains(name)) {
                    list.add(designPropertyDescriptor);
                }
            }
        }
        return list;
    }
    
    protected void notifyDetached(DesignComponent component) {
    }
    
    protected DesignEventFilter getEventFilter() {
        return null;
    }
    
    protected void designChanged(DesignEvent event) {
    }
    
    protected void presenterChanged(PresenterEvent event) {
    }
    
    protected void notifyAttached(DesignComponent component) {
    }
    
}
