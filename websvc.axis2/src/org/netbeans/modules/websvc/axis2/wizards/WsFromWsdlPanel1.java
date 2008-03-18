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

package org.netbeans.modules.websvc.axis2.wizards;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

import org.netbeans.modules.websvc.axis2.WSDLUtils;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
public class WsFromWsdlPanel1 implements  WizardDescriptor.FinishablePanel<WizardDescriptor>, WizardProperties {
    
    private WsFromWsdlGUIPanel1 component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    /** Creates a new instance of WebServiceType */
    public WsFromWsdlPanel1(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }

    public Component getComponent() {
        if (component == null) {
            component = new WsFromWsdlGUIPanel1(this);
        }
        
        return component;
    }
    
    WizardDescriptor getWizardDescriptor() {
        return wizardDescriptor;
    }
    
    Project getProject() {
        return project;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(WsFromJavaPanel0.class);
    }

    public void readSettings(WizardDescriptor settings) {
        final File wsdlFile = (File)settings.getProperty(WizardProperties.PROP_WSDL_URL);
        component.setW2JOptions("");
        if (wsdlFile != null) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    FileObject wsdlFo = FileUtil.toFileObject(wsdlFile);
                    WSDLModel wsdlModel = WSDLUtils.getWSDLModel(wsdlFo, true);
                    if (wsdlModel != null) {
                        Collection<Service> services = WSDLUtils.getServices(wsdlModel);
                        component.setServices(services);
                        if (services != null && services.size()>0) {
                            component.setPorts(WSDLUtils.getPortsForService(services.iterator().next()));
                        }
                        String packageName = WSDLUtils.getPackageNameFromNamespace(wsdlModel.getDefinitions().getTargetNamespace());
                        component.setPackageName(packageName);
                        component.setW2JOptions("-sn "+component.getServiceName()+
                                    " -pn "+component.getPortName()+
                                    " -d adb\n"+
                                    " -p "+packageName+" -ssi"
                        );
                    }
                }
                
            });
        }        
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.PROP_DATABINDING_NAME, component.getDatabindingName());
        settings.putProperty(WizardProperties.PROP_SEI, Boolean.valueOf(component.isSEI()));
        settings.putProperty(WizardProperties.PROP_SERVICE_NAME, component.getServiceName());
        settings.putProperty(WizardProperties.PROP_PORT_NAME, component.getPortName());
        settings.putProperty(WizardProperties.PROP_PACKAGE_NAME, component.getPackageName());
        settings.putProperty(WizardProperties.PROP_WS_TO_JAVA_OPTIONS, component.getW2JMoreOptions());
    }

    public boolean isValid() {
        return component.dataIsValid();
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public boolean isFinishPanel() {
        return true;
    }

//    public void stateChanged(ChangeEvent e) {
//        fireChange();
//    }
    
    void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator<ChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().stateChanged(e);
        }
    }

}
