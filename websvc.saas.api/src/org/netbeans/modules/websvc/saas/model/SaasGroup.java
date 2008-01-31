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

/**
 *
 * @author nam
 */
public class SaasGroup {
    private final Group delegate;
    private final SaasGroup parent;
    private boolean userDefined;
    private List<Saas> services;
    private List<SaasGroup> children;
    
    public SaasGroup(SaasGroup parent, Group group) {
        this.parent = parent;
        this.delegate = group;
        services = Collections.synchronizedList(new ArrayList<Saas>());
        children = Collections.synchronizedList(new ArrayList<SaasGroup>());
    }

    public Group getDelegate() {
        return delegate;
    }

    public List<Saas> getServices() {
        return Collections.unmodifiableList(services);
    }

    /**
     * If this is part of model mutation, caller is responsible to ensure 
     * SaasServices persists with proper Group information.
     * 
     * @param service saas service
     */
    public void addService(Saas service) {
        synchronized(services) {
            services.add(service);
        }
    }
    /**
     * If this is part of model mutation, caller is responsible to ensure 
     * SaasServices persists with proper Group information.
     * 
     * @param service saas service to remove
     */
    public boolean removeService(Saas service) {
        synchronized(services) {
            return services.remove(service);
        }
    }

    public void setName(String value) {
        delegate.setName(value);
    }

    public String getName() {
        return delegate.getName();
    }
    
    public boolean isUserDefined() {
        return userDefined;
    }

    public void setUserDefined(boolean v) {
        userDefined = v;
    }

    public List<SaasGroup> getChildrenGroups() {
        return Collections.unmodifiableList(children);
    }

    /**
     * If this is part of model mutation, caller is responsible to ensure 
     * SaasServices persists with proper Group information.
     * 
     * @param group saas group to remove
     */
    public boolean removeChildGroup(SaasGroup group) {
        synchronized(children) {
            return children.remove(group);
        }
    }

    /**
     * If this is part of model mutation, caller is responsible to persist 
     * group removal, whether the group is user-defined or installed.
     * 
     * @param group saas group to add
     */
    public void addChildGroup(SaasGroup group) {
        synchronized(children) {
            children.add(group);
        }
    }
}
