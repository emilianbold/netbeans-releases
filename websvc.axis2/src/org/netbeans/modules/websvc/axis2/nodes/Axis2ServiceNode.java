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
package org.netbeans.modules.websvc.axis2.nodes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.Axis2ModelProvider;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.actions.DeployAction;
import org.netbeans.modules.websvc.axis2.actions.EditWsdlAction;
import org.netbeans.modules.websvc.axis2.actions.GenerateWsdlAction;
import org.netbeans.modules.websvc.axis2.actions.RefreshServiceAction;
import org.netbeans.modules.websvc.axis2.actions.ServiceConfigurationAction;
import org.netbeans.modules.websvc.axis2.config.model.Axis2;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.modules.websvc.axis2.services.model.ServiceGroup;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class Axis2ServiceNode extends AbstractNode implements OpenCookie {
    private static final String WSDL_URL_PROP = "wsdl-url"; //NOI18N
    private static final String AXIS_ICON = "org/netbeans/modules/websvc/axis2/resources/axis_node_16.png"; // NOI18N
    
    Service service;
    FileObject srcRoot;
    
    public Axis2ServiceNode(Service service, FileObject srcRoot) {
        this(service, srcRoot, new InstanceContent());
    }
    
    private Axis2ServiceNode(Service service, FileObject srcRoot, InstanceContent content) {
        super(new Axis2ServiceChildren(service, srcRoot),new AbstractLookup(content));
        this.service=service;
        this.srcRoot = srcRoot;
        content.add(service);
        content.add(srcRoot);
        content.add(this);
        setIconBaseWithExtension(AXIS_ICON);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Axis2ServiceNode.this.setValue(WSDL_URL_PROP, getWsdlUrl());
            }
        });     
    }
    
    public String getName() {
        return service.getNameAttr();
    }
    
    public String getDisplayName() {
        return service.getNameAttr();
    }
    
    @Override
    public String getShortDescription() {
        return getWsdlUrl();
    }
    
    private String getWsdlUrl() {
        Preferences preferences = NbPreferences.forModule(Axis2ServiceNode.class);
        String axisURL = preferences.get("AXIS_URL",null); //NOI18N
        if (axisURL!=null) {
            String serviceName = service.getNameAttr();
            try {
                serviceName = URLEncoder.encode(serviceName, "UTF-8"); //NOI18N
            } catch (UnsupportedEncodingException ex) {}            
            return axisURL+"/services/"+serviceName+"?wsdl"; //NOI18N
        } else {
            return service.getServiceClass();
        }       
    }
    
    public void open() {
        FileObject fo = srcRoot.getFileObject(service.getServiceClass().replace('.', '/')+".java"); //NOI18N
        try {
            DataObject dObj = DataObject.find(fo);
            if (dObj!=null) {
                EditCookie ec = dObj.getCookie(EditCookie.class);
                if (ec != null) {
                    ec.edit();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>(Arrays.asList(
            SystemAction.get(OpenAction.class),
            SystemAction.get(DeployAction.class),
            null,
            SystemAction.get(ServiceConfigurationAction.class),
            SystemAction.get(GenerateWsdlAction.class),
            SystemAction.get(EditWsdlAction.class),
            SystemAction.get(RefreshServiceAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)));
        addFromLayers(actions, "WebServices/Services/Actions"); // NOI18N
        return actions.toArray(new Action[actions.size()]);
    }
    
    private void addFromLayers(List<Action> actions, String path) {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                actions.add((Action) next);
            } else if (next instanceof javax.swing.JSeparator) {
                actions.add(null);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws java.io.IOException {
        Project prj = FileOwnerQuery.getOwner(srcRoot);
        
        Axis2ModelProvider axis2ModelProvider = prj.getLookup().lookup(Axis2ModelProvider.class);
        if (axis2ModelProvider != null) {
            ServicesModel servicesModel = axis2ModelProvider.getServicesModel();
            String serviceName = service.getNameAttr();
            
            // remove entry from services.xml 
            org.netbeans.modules.websvc.axis2.services.model.Service serviceToRemove = null;
            ServiceGroup serviceGroup = (ServiceGroup)servicesModel.getRootComponent();
            List<org.netbeans.modules.websvc.axis2.services.model.Service> services = serviceGroup.getServices();
            for (org.netbeans.modules.websvc.axis2.services.model.Service s:services) {
                if (serviceName.equals(s.getNameAttr())) {
                    serviceToRemove = s;
                    break;
                }
            }
            if (serviceToRemove != null) {
                servicesModel.startTransaction();
                serviceGroup.removeService(serviceToRemove);
                servicesModel.endTransaction();
            }
            
            // removing implementation class
            FileObject fo = srcRoot.getFileObject(service.getServiceClass().replace('.', '/')+".java"); //NOI18N
            if (fo != null) fo.delete();
            
            // call clean targets
            if (service.getGenerateWsdl() != null) {
                String targets[] = new String[] {"java2wsdl-clean-"+serviceName}; // NOI18N
                AxisUtils.runTargets(prj.getProjectDirectory(), targets);
            } else if (service.getWsdlUrl() != null) {
                String targets[] = new String[] {"wsdl2java-clean-"+serviceName}; // NOI18N
                AxisUtils.runTargets(prj.getProjectDirectory(), targets);
            }
            
            // remove entry from axis2.xml
            Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
            Axis2 axis2 = axis2Model.getRootComponent();
            axis2Model.startTransaction();
            axis2.removeService(service);
            axis2Model.endTransaction();
            FileObject axis2Folder = AxisUtils.getNbprojectFolder(prj.getProjectDirectory());
            FileObject axis2Fo = axis2Folder.getFileObject("axis2", "xml"); //NOI18N
            if (axis2Fo != null) {
                DataObject dObj = DataObject.find(axis2Fo);
                if (dObj != null) {
                    SaveCookie save = dObj.getCookie(SaveCookie.class);
                    if (save != null) save.save();
                }
            }
        }
    }
    
    public void nameChanged(String oldName, String newName) {
        fireNameChange(oldName, newName);
        fireDisplayNameChange(oldName, newName);
        setValue(WSDL_URL_PROP, getWsdlUrl());
    }
    
}
