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
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
public class SaasServicesModel {

    public static final String PROP_GROUPS = "groups";
    public static final String PROP_SERVICES = "services";
    public static final String ROOT_GROUP = "root";
    public static final String WEBSVC_HOME = WsdlServiceProxyDescriptor.WEBSVC_HOME;
    public static final String SERVICE_GROUP_XML = "service-groups.xml";
    
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

    private SaasServicesModel() {
    }

    public void initRootGroup() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                init();
            }
        });
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
            loadFromWebServicesHome();
            setState(State.READY);
        }
    }

    private void loadUserDefinedGroups() {
        FileObject input = FileUtil.toFileObject(new File(WEBSVC_HOME, SERVICE_GROUP_XML));
        try {
            if (input != null) {
                rootGroup = SaasUtil.loadSaasGroup(input);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        if (rootGroup == null) {
            Group g = new Group();
            g.setName(ROOT_GROUP);
            rootGroup = new SaasGroup((SaasGroup)null, g);
        }
    }

    private void loadFromDefaultFileSystem() {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject f = sfs.findResource("SaasServices"); // NOI18N
        if (f != null && f.isFolder()) {
            Enumeration<? extends FileObject> en = f.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject groupFolder = en.nextElement();
                for (FileObject fo : groupFolder.getChildren()) {
                    if (fo.isFolder()) {
                        continue;
                    }
                    loadSaasServiceFile(fo);
                }
            }
        }
    }

    public void saveRootGroup() {
        try {
            SaasUtil.saveSaasGroup(rootGroup, new File(WEBSVC_HOME, SERVICE_GROUP_XML));
        } catch(Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static FileObject getWebServiceHome() {
        File websvcDir = new File(WEBSVC_HOME);
        if (! websvcDir.isFile()) {
            websvcDir.mkdirs();
        }
        return FileUtil.toFileObject(websvcDir);
    }
    
    private void loadFromWebServicesHome() {
        for (FileObject fo : getWebServiceHome().getChildren()) {
            if (! fo.isFolder()) {
                continue;
            }
            for (FileObject file : fo.getChildren()) {
                if (! file.getNameExt().endsWith("-saas.xml")) { //NOI18N
                    continue;
                }
                try {
                    loadSaasServiceFile(file);
                } catch(Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private void loadSaasServiceFile(FileObject saasFile) {
            try {
                SaasServices ss = SaasUtil.loadSaasServices(saasFile);
                Group g = ss.getSaasMetadata().getGroup();
                SaasGroup parent = rootGroup;
                while (g != null) {
                    SaasGroup child = parent.getChildGroup(g.getName());
                    if (child == null) {
                        child = new SaasGroup(parent, g);
                        child.setUserDefined(false);
                        parent.addChildGroup(child);
                    }

                    if (child.getChildrenGroups().size() == 0) {
                        Saas service;
                        if (Saas.NS_WADL.equals(ss.getType())) {
                            service = new WadlSaas(parent, ss);
                        } else if (Saas.NS_WSDL.equals(ss.getType())) {
                            service = new WsdlSaas(parent, ss);
                        } else {
                            service = new CustomSaas(parent, ss);
                        }
                        child.addService(service);
                        break;
                    } else {
                        g = g.getGroup().get(0);
                    }
                    parent = child;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
    }
    
    public SaasGroup getRootGroup() {
        init();
        return rootGroup;
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        synchronized (state) {
            this.state = state;
            if (state == State.READY) {
                fireChange(PROP_GROUPS, rootGroup, null, rootGroup.getChildrenGroups());
                fireChange(PROP_SERVICES, rootGroup, null, rootGroup.getServices());
            }
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

    public void addGroup(SaasGroup parent, SaasGroup group) {
        init();
        parent.addChildGroup(group);
        saveRootGroup();
        fireChange(PROP_GROUPS, parent, null, group);
    }

    /**
     * Model mutation: remove group from UI
     * 
     * @param group group to remove
     */
    public void removeGroup(SaasGroup child) {
        removeGroup(rootGroup, child);
        saveRootGroup();
    }

    public void removeGroup(SaasGroup parent, SaasGroup group) {
        init();
        parent.removeChildGroup(group);
        saveRootGroup();
        fireChange(PROP_GROUPS, parent, group, null);
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
        service.getSaasFolder();
        WsdlData data = WsdlUtil.addWsdlData(url, packageName);
        if (data != null) {
            service.setWsdlData(data);
            data.addPropertyChangeListener(service);
        }
        parent.addService(service);
        service.save();
        fireChange(PROP_SERVICES, parent, null, service);
    }
    
    public void addWsdlService(SaasGroup parent, String url, String packageName) {
        addWsdlService(parent, WsdlUtil.getServiceDirName(url), url, packageName);
    }
    
    /**
     * Model mutation: remve service from parent group, delete file, fire event
     * @param service to remove.
     */
    public void removeService(Saas service) {
        init();
        SaasGroup parent = service.getParentGroup();
        parent.removeService(service);
        try {
            FileObject saasFolder = service.getSaasFolder();
            if (saasFolder != null) {
                saasFolder.delete();
            }
            if (service instanceof WsdlSaas) {
                WsdlSaas saas = (WsdlSaas) service;
                WsdlUtil.removeWsdlData(saas.getWsdlData());
            }
            fireChange(PROP_SERVICES, parent, service, null);
        } catch(IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
}
