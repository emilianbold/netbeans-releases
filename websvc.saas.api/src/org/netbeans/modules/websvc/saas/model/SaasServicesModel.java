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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 *
 * @author nam
 */
public class SaasServicesModel {
    public static final String PROP_GROUPS = "groups";
    public static final String PROP_SERVICES = "services";
    private final SaasGroup rootGroup;
    private State state = State.INITIALIZING;
    private PropertyChangeSupport pps = new PropertyChangeSupport(this);
    
    public static enum State {
        INITIALIZING, READY
    }
    
    private static SaasServicesModel instance;
    public static SaasServicesModel getInstance() {
        synchronized(instance) {
            if (instance == null) {
                instance = new SaasServicesModel();
            }
        }
        return instance;
    }

    public SaasServicesModel() {
        rootGroup = new SaasGroup(null, new Group());
        init();
    }
    
    private void init() {
        //TODO: read layer file system and <userdir>/config/saas persistence
        setState(State.READY);
    }
    
    public SaasGroup getRootGroup() {
        if (state != State.READY) {
            throw new IllegalStateException("SaaS Model is not ready!");
        }
        return rootGroup;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        synchronized(state) {
            this.state = state;
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pps.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pps.removePropertyChangeListener(l);
    }
    
    protected void fireChange(String propertyName, Object source, Object old, Object neu) {
        pps.firePropertyChange(propertyName, old, neu);
    }
    
    /**
     * Model mutation: add group from UI
     * 
     * @param parent
     * @param child
     */
    public SaasGroup addGroup(SaasGroup parent, String childName) {
       //TODO 
        return null;
    }

    /**
     * Model mutation: remove group from UI
     * 
     * @param group group to remove
     */
    public void removeGroup(SaasGroup group) {
       //TODO 
    }

    /**
     * Model mutation: add group from UI
     * 
     * @param groups list of group to remove
     */
    public void removeGroups(List<SaasGroup> groups) {
       //TODO 
    }

    /**
     * Model mutation: add saas service from UI
     * 
     * @param parent group
     * @param displayName name
     * @param url URL pointing to a WSDL or WADL
     * @param packageName package name used in codegen; if null, value will be derived.
     */
    public Saas addSaasService(SaasGroup parent, String displayName, String url, String packageName) {
        //TODO 
        return null;
    }

    /**
     * Model mutation: add group from UI
     * 
     * @param parent
     * @param child
     */
    public void removeSaasService(Saas target) {
       //TODO 
    }
}
