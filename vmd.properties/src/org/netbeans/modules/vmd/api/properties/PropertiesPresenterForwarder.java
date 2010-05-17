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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/**
 * This implemementation of PropertiesPresenter enables forwarding of particular 
 * DesignPropertyDEscriptors from one DesignComponent to another. For example using this class 
 * it is possible to attache DesignPropertyDescriptors from one DesignComponent to another so user
 * sees it in the Properties Window as single component's properties collection even though they dont belong
 * to the same DesignComponent.
 */
public class PropertiesPresenterForwarder extends PropertiesPresenter {
    
    private String propertyName;
    private String[] propertyNames;
    /**
     * Creates PropertiesPresenterForwarder based on the name of 
     * the PropertyDescriptor attached to the DesignComponent which owns this presenter
     * forwarder. In this case all DesignPropertyDescriptors are forwarded to this presenter and they are 
     * taken from the DesignComponent which is referenced through the DesignComponent property (PropertyValue).
     * @param propertyName property name of the DesignComponent PropertyDescriptor
     * @return PropertiesPresenter with forwarded DesignPropertyDescriptors
     */
    public static Presenter createByReference(String propertyName) {
        return new PropertiesPresenterForwarder(propertyName);
    }
    /**
     * Creates PropertiesPresenterForwarder based on the name of 
     * the PropertyDescriptor attached to the DesignComponent which owns this presenter
     * forwarder and the list of the properties names of the DesignComponent properties to forward. In this case chosen
     * DesignPropertyDescriptors are forwarded to this presenter and they are
     * taken from the DesignComponent which is referenced through the DesignComponent property (PropertyValue).
     * @param propertyName property name of the DesignComponent PropertyDescriptor with the DesignComponet references
     * @param propertyNames properties names of chosen propertires to forward
     * @return PropertiesPresenter with forwarded DesignPropertyDescriptors
     */
    public static Presenter createByNames(String propertyName, String... propertyNames) {
        return new PropertiesPresenterForwarder(propertyName, propertyNames);
    }
    
    private PropertiesPresenterForwarder(String propertyName, String... propertyNames) {        
        this.propertyName = propertyName;
        this.propertyNames = propertyNames;
    }
    @Override
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
    @Override
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
    @Override
    protected void notifyDetached(DesignComponent component) {
    }
    @Override
    protected DesignEventFilter getEventFilter() {
        return null;
    }
    @Override
    protected void designChanged(DesignEvent event) {
    }
    @Override
    protected void presenterChanged(PresenterEvent event) {
    }
    @Override
    protected void notifyAttached(DesignComponent component) {
    }
    
}
