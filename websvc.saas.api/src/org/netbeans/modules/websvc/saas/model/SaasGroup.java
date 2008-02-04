/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.model;

import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author nam
 */
public class SaasGroup {
    /**
     * Property change event names
     */
    public static final String PROP_CHILD_GROUP = "childGroup";
    public static final String PROP_CHILD_SERVICE = "childService";

    private final Group delegate;
    private final SaasGroup parent;
    private boolean userDefined = true; //once set to false, remain false.
    private SortedMap<String, Saas> services;
    private SortedMap<String, SaasGroup> children;
    
    public SaasGroup(SaasGroup parent, Group group) {
        this.parent = parent;
        this.delegate = group;
        services = Collections.synchronizedSortedMap(new TreeMap<String, Saas>());
    }

    public SaasGroup getParent() {
        return parent;
    }
    
    public Group getDelegate() {
        return delegate;
    }

    public List<Saas> getServices() {
        return Collections.unmodifiableList(new ArrayList<Saas>(services.values()));
    }

    /**
     * If this is part of model mutation, caller is responsible to ensure 
     * SaasServices persists with proper Group information.
     * 
     * @param service saas service
     */
    public void addService(Saas service) {
        services.put(service.getDisplayName(), service);
        SaasServicesModel.getInstance().fireChange(PROP_CHILD_SERVICE, this, null, service);
    }
    
    /**
     * If this is part of model mutation, caller is responsible to ensure 
     * SaasServices persists with proper Group information.
     * 
     * @param service saas service to remove
     */
    public boolean removeService(Saas service) {
        Saas removed = services.remove(service.getDisplayName());
        if (removed != null) {
            SaasServicesModel.getInstance().fireChange(PROP_CHILD_SERVICE, this, removed, null);
            return true;
        }
        return false;
    }

    public void setName(String value) {
        if (! isUserDefined()) {
            getParent().removeChildGroup(this);
            delegate.setName(value);
            getParent().addChildGroup(this);
        }
    }

    public String getName() {
        return delegate.getName();
    }
    
    public boolean isUserDefined() {
        return userDefined;
    }

    public void setUserDefined(boolean v) {
        if (userDefined) {
            userDefined = v;
        }
    }

    public List<SaasGroup> getChildrenGroups() {
        if (children == null) {
            children = Collections.synchronizedSortedMap(new TreeMap<String,SaasGroup>());
            for (Group g : delegate.getGroup()) {
                SaasGroup sg = new SaasGroup(this, g);
                children.put(sg.getName(), sg);
            }
        }
        return Collections.unmodifiableList(new ArrayList<SaasGroup>(children.values()));
    }

    public SaasGroup getChildGroup(String name) {
        getChildrenGroups();
        return children.get(name);
    }
    
    /**
     * All children services and children groups also removed.
     * Only group created by users could be removed.
     * Caller is responsible for persisting changes.
     * @param group saas group to remove
     */
    public boolean removeChildGroup(SaasGroup group) {
        if (! group.canRemove()) {
            return false;
        }
        
        _removeChildGroup(group);
        
        // only fire on top-level object
        SaasServicesModel.getInstance().fireChange(PROP_CHILD_GROUP, this, group, null);
        return true;
    }
    
    private void _removeChildGroup(SaasGroup child) {
        if (child != null) {
            for (Saas saas : child.getServices()) {
                _removeSaas(saas);
            }
            for (SaasGroup c : child.getChildrenGroups()) {
                _removeChildGroup(c);
            }
        }
    }
    
    private void _removeSaas(Saas childService) {
        services.remove(childService.getDisplayName());
        //TODO cleanup persistence and remove local files
    }
    
    public boolean canRemove() {
        if (isUserDefined()) {
            return false;
        }
        for (SaasGroup child : getChildrenGroups()) {
            if (! child.canRemove()) {
                return false;
            }
        }
        return true;
    }

    /**
     * If this is part of model mutation, caller is responsible to save
     * changes, whether the group is user-defined or pre-installed.
     * 
     * @param group saas group to add
     */
    public void addChildGroup(SaasGroup group) {
        children.put(group.getName(), group);
        SaasServicesModel.getInstance().fireChange(PROP_CHILD_GROUP, this, null, group);
    }
}
