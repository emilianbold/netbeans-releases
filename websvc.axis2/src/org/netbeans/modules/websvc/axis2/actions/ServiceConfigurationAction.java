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
package org.netbeans.modules.websvc.axis2.actions;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.TransformerUtils;
import org.netbeans.modules.websvc.axis2.config.model.Axis2ComponentFactory;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.GenerateWsdl;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.modules.websvc.axis2.nodes.Axis2ServiceNode;
import org.netbeans.modules.websvc.axis2.services.model.Parameter;
import org.netbeans.modules.websvc.axis2.services.model.ServiceGroup;
import org.netbeans.modules.websvc.axis2.services.model.ServicesModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class ServiceConfigurationAction extends NodeAction  {
    
    public String getName() {
        return NbBundle.getMessage(ServiceConfigurationAction.class, "LBL_ServiceConfigAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes==null || activatedNodes.length != 1) return false;
        Service service = activatedNodes[0].getLookup().lookup(Service.class);
        if (service != null && service.getWsdlUrl() == null) return true;
        return false;
    }
    
    protected void performAction(Node[] activatedNodes) {

        Service service = activatedNodes[0].getLookup().lookup(Service.class);
        ServiceConfigurationPanel configPanel = new ServiceConfigurationPanel(service);
        DialogDescriptor dialog = new DialogDescriptor(configPanel, "Configuration...");
        DialogDisplayer.getDefault().notify(dialog);
        if (dialog.getValue() == DialogDescriptor.OK_OPTION) {
            Axis2Model model = service.getModel();
            FileObject srcRoot = activatedNodes[0].getLookup().lookup(FileObject.class);
            boolean modelChanged = false;
            boolean serviceNameChanged = false;
            boolean serviceClassChanged = false;
            
            model.startTransaction();
            String newServiceName = configPanel.getServiceName();
            String oldServiceName = service.getNameAttr();
            if (newServiceName.length() > 0 && !newServiceName.equals(oldServiceName)) {
                service.setNameAttr(newServiceName);
                modelChanged = true;
                serviceNameChanged = true;
                Axis2ServiceNode serviceNode = activatedNodes[0].getLookup().lookup(Axis2ServiceNode.class);
                if (serviceNode != null) serviceNode.nameChanged(oldServiceName, newServiceName);
            }
            String newServiceClass = configPanel.getServiceClass();
            String oldServiceClass = service.getServiceClass();
            if (newServiceClass.length() > 0 && !newServiceClass.equals(oldServiceClass)) {
                service.setServiceClass(newServiceClass);
                modelChanged = true;
                serviceClassChanged = true;
            }
            boolean generateWsdl = configPanel.generateWsdl();
            if (service.getGenerateWsdl() == null && generateWsdl) {
                Axis2ComponentFactory factory = model.getFactory();
                GenerateWsdl genWsdl = factory.createGenerateWsdl();
                String ns = configPanel.getTargetNamespace();
                if (ns.length() > 0) genWsdl.setTargetNamespaceAttr(ns);
                ns = configPanel.getSchemaNamespace();
                if (ns.length() > 0) genWsdl.setSchemaNamespaceAttr(ns);
                service.setGenerateWsdl(genWsdl);
                modelChanged = true;
            } else if (service.getGenerateWsdl() != null) {
                if (!generateWsdl) {
                    service.setGenerateWsdl(null);
                    modelChanged = true;
                } else {
                    GenerateWsdl genWsdl = service.getGenerateWsdl();
                    String ns = configPanel.getTargetNamespace();
                    if (!ns.equals(genWsdl.getTargetNamespaceAttr())) {
                        genWsdl.setTargetNamespaceAttr(ns);
                        modelChanged = true;
                    }
                    ns = configPanel.getSchemaNamespace();
                    if (!ns.equals(genWsdl.getSchemaNamespaceAttr())) {
                        genWsdl.setSchemaNamespaceAttr(ns);
                        modelChanged = true;
                    }                   
                }
            }
            model.endTransaction();

            if (modelChanged) {
                Project prj = FileOwnerQuery.getOwner(srcRoot);
                if (prj!= null) {
                    try {
                        saveConfigFile(prj);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                if (serviceNameChanged || serviceClassChanged) {
                   ServicesModel servicesModel = AxisUtils.getServicesModelForProject(prj);
                   if (servicesModel != null) {
                       servicesModel.startTransaction();
                       ServiceGroup serviceGroup = (ServiceGroup) servicesModel.getRootComponent();
                       for (org.netbeans.modules.websvc.axis2.services.model.Service serv:serviceGroup.getServices()) {
                           if (oldServiceName.equals(serv.getNameAttr())) {
                               if (serviceNameChanged) {
                                   serv.setNameAttr(newServiceName);
                               }
                               if (serviceClassChanged) {
                                   for (Parameter param:serv.getParameters()) {
                                       if ("ServiceClass".equals(param.getNameAttr())) {
                                           param.setValue(newServiceClass);
                                           break;
                                       }
                                   }
                               }
                               break;
                           }
                       }
                       servicesModel.endTransaction();
                   }
                }
            

                try {
                    TransformerUtils.transform(prj.getProjectDirectory());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private void saveConfigFile(Project prj) throws java.io.IOException {
        FileObject axis2Fo = prj.getProjectDirectory().getFileObject(TransformerUtils.AXIS2_XML_PATH);
        DataObject dObj = DataObject.find(axis2Fo);
        if (dObj != null) {
            SaveCookie save = dObj.getCookie(SaveCookie.class);
            if (save != null) save.save();
        }
    }

}

