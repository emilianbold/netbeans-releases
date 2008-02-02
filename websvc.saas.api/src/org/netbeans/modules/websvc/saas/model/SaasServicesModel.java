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

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class SaasServicesModel {

    public static final String PROP_GROUPS = "groups";
    public static final String PROP_SERVICES = "services";
    public static final String ROOT_GROUP = "root";
    public static final String WEBSVC_HOME = WebServiceDescriptor.WEBSVC_HOME;
    private SaasGroup rootGroup;
    private State state = State.UNINITIALIZED;
    private PropertyChangeSupport pps = new PropertyChangeSupport(this);

    public static enum State {

        UNINITIALIZED, INITIALIZING, READY
    }
    private static SaasServicesModel instance;

    public static SaasServicesModel getInstance() {
        if (instance == null) {
            instance = new SaasServicesModel();
        }
        return instance;
    }

    private SaasServicesModel_1() {
    }

    private void init() {
        if (state == State.READY) {
            return;
        }
        synchronized (state) {
            if (state == State.READY) {
                return;
            }
            setState(State.INITIALIZING);
            loadUserDefinedGroups();
            loadFromDefaultFileSystem();
            setState(State.READY);
        }
    }

    private void loadUserDefinedGroups() {
        FileObject input = FileUtil.toFileObject(new File(WEBSVC_HOME));
        try {
            rootGroup = SaasUtil.loadSaasGroup(input);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        if (rootGroup == null) {
            Group g = new Group();
            g.setName(ROOT_GROUP);
            rootGroup = new SaasGroup(null, g);
        }
    }

    private void loadFromDefaultFileSystem() {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject f = sfs.findResource("SaasServices"); // NOI18N
        if (f != null && f.isFolder()) {
            Enumeration<? extends FileObject> en = f.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject groupFolder = en.nextElement();
                loadGroupFromDefaultFileSystemFolder(groupFolder);
            }
        }
    }

    private void loadGroupFromDefaultFileSystemFolder(FileObject folder) {
        List<Exception> exs = new ArrayList<Exception>();
        for (FileObject fo : folder.getChildren()) {
            try {
                SaasServices ss = SaasUtil.loadSaasServices(fo);
                Group g = ss.getSaasMetadata().getGroup();
                SaasGroup parent = rootGroup;
                while (g != null) {
                    SaasGroup child = parent.getChildGroup(g.getName());
                    if (child == null) {
                        child = new SaasGroup(parent, g);
                        parent.addChildGroup(child);
                    }

                    if (child.getChildrenGroups().size() == 0) {
                        child.addService(new Saas(parent, ss));
                        break;
                    } else {
                        g = g.getGroup().get(0);
                    }
                    parent = child;
                }
            } catch (Exception ex) {
                exs.add(ex);
            }
        }
        if (exs.size() > 0) {
            StringBuffer messages = new StringBuffer();
            for (Exception ex : exs) {
                messages.append(ex.getLocalizedMessage());
                messages.append(System.getProperties().getProperty("line.separator"));
            }
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, messages.toString());
        }
    }

    protected SaasGroup getRootGroup() {
        init();
        return rootGroup;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        synchronized (state) {
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
        PropertyChangeEvent pce = new PropertyChangeEvent(source, propertyName, old, neu);
        pps.firePropertyChange(pce);
    }

    List<SaasGroup> getGroups() {
        return getRootGroup().getChildrenGroups();
    }

    /**
     * Model mutation: add group from UI
     * 
     * @param parent
     * @param child
     */
    public void addGroup(SaasGroup child) {
        addGroup(rootGroup, child);
    }

    public void addGroup(SaasGroup parent, SaasGroup child) {
        init();
        parent.addChildGroup(child);
    //TODO save
    }

    /**
     * Model mutation: remove group from UI
     * 
     * @param group group to remove
     */
    public void removeGroup(SaasGroup child) {
        removeGroup(rootGroup, child);
    }

    public void removeGroup(SaasGroup parent, SaasGroup group) {
        init();
        parent.removeChildGroup(group);
    //TODO save
    }

    List<Saas> getServices() {
        return getRootGroup().getServices();
    }

    /**
     * Model mutation: add saas service from UI
     * 
     * @param parent group
     * @param displayName name
     * @param url URL pointing to a WSDL or WADL
     * @param packageName package name used in codegen; if null, value will be derived.
     */
    public void addWsdlService(SaasGroup parent, String displayName, String url, String packageName) {
        init();
        WsdlSaas service = new WsdlSaas(parent, displayName, url, packageName);
        parent.addService(service);
    //TODO save
    }

    /**
     * Model mutation: add group from UI
     * 
     * @param parent
     * @param child
     */
    public void removeService(SaasGroup parent, Saas service) {
        init();
        parent.removeService(service);
    //TODO save
    }
}
