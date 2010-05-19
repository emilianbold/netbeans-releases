/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOMessageExchangePattern;
import org.netbeans.modules.soa.pojo.util.POJOSupportedDataTypes;
import org.netbeans.modules.soa.pojo.util.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author sgenipudi
 */
public class OperationMethodChooserPanelWizardDescriptor implements WizardDescriptor.FinishablePanel {

    private Component mComp = null;
    private WizardDescriptor wizard = null;
    private final List listeners = new ArrayList();
    
    public OperationMethodChooserPanelWizardDescriptor(Component comp) {
        mComp = comp;
        final OperationMethodChooserPanelWizardDescriptor thisInstance = this;
        ((OperationMethodChooserPanel)mComp).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
              thisInstance.fireChange();
            }
        });
    }

    public boolean isFinishPanel() {
        //Revalidate this!.
        return true;
    }
    
    
    public void setWizard(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    public Component getComponent() {
        return this.mComp;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(Object settings) {
        OperationMethodChooserPanel panel = ((OperationMethodChooserPanel) this.mComp);
        POJOProviderAdvancedPanel adwPanel = panel.getAdvancedPanel();
        WizardDescriptor wizDesc = (WizardDescriptor) settings;
        wizard = wizDesc;
        panel.setWizardDescriptor(wizDesc);
        if (adwPanel.useDefaultValues()) {
            String className = Templates.getTargetName(wizDesc);
            if ( className != null) {
                FileObject folder = Templates.getTargetFolder(wizDesc);
                String pkgName = Util.getSelectedPackageName(folder);

                String defaultNS = GeneratorUtil.getNamespace(pkgName, className);
                String defIntfName = className + GeneratorUtil.POJO_INTERFACE_SUFFIX;
                String defServName = className + GeneratorUtil.POJO_SERVICE_SUFFIX;
                
                adwPanel.setEndpointName(className);
                adwPanel.setInterfaceName(defIntfName);
                adwPanel.setInterfaceNameNS(defaultNS);
                adwPanel.setOutMessageType(className + GeneratorUtil.POJO_OUT_MESSAGE_SUFFIX);
                adwPanel.setOutMessageTypeNS(defaultNS);
                adwPanel.setServiceName(defServName);
                adwPanel.setServiceNameNS(defaultNS);

            }
        }
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wizDesc = (WizardDescriptor) settings;
        wizDesc.putProperty(GeneratorUtil.POJO_OPERATION_METHOD_NAME, ((OperationMethodChooserPanel) mComp).getMethodName());

        OperationMethodChooserPanel panel = ((OperationMethodChooserPanel) this.mComp);
        WizardDescriptor wizProps = wizDesc;
        if (panel.isInOnly()) {
            wizProps.putProperty(GeneratorUtil.POJO_OPERATION_PATTERN, POJOMessageExchangePattern.InOnly);
        } else {
            wizProps.putProperty(GeneratorUtil.POJO_OPERATION_PATTERN, POJOMessageExchangePattern.InOut);
        }
        wizProps.putProperty(GeneratorUtil.POJO_OUTPUT_TYPE, panel.getOutputType());        
        wizProps.putProperty(GeneratorUtil.POJO_INPUT_TYPE, panel.getInputType());
        wizProps.putProperty(GeneratorUtil.POJO_GENERATE_WSDL,Boolean.valueOf(panel.generateWSDL()));
        String className = Templates.getTargetName(wizDesc);
        POJOProviderAdvancedPanel adwPanel = panel.getAdvancedPanel();
        MultiTargetChooserPanelGUI multiPanelGUI = (MultiTargetChooserPanelGUI) wizProps.getProperty(MultiTargetChooserPanel.MULTI_TARGET_GUI_INSTANCE);


        String cname= (String)wizProps.getProperty(GeneratorUtil.POJO_DEST_NAME);
        if ( cname != null) {
            className = cname;
        }
        if ( className != null) {
            wizProps.putProperty(GeneratorUtil.POJO_CLASS_NAME, className);
        }
        if ( multiPanelGUI != null) {
            wizProps.putProperty(GeneratorUtil.POJO_PACKAGE_NAME,multiPanelGUI.getPackageName());
        }
        if (! adwPanel.useDefaultValues()) {
            wizProps.putProperty(GeneratorUtil.POJO_ENDPOINT_NAME, adwPanel.getEndpointName());
            wizProps.putProperty(GeneratorUtil.POJO_INTERFACE_NAME, adwPanel.getInterfaceName());
            wizProps.putProperty(GeneratorUtil.POJO_SERVICE_NAME, adwPanel.getServiceName());
            wizProps.putProperty(GeneratorUtil.POJO_INTERFACE_NS, adwPanel.getInterfaceNameNS());
            wizProps.putProperty(GeneratorUtil.POJO_SERVICE_NS, adwPanel.getServiceNameNS());
            wizProps.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NAME, adwPanel.getOutMessageType());
            wizProps.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS, adwPanel.getOutMessageTypeNS());
            wizProps.putProperty(GeneratorUtil.POJO_ADVANCED_SAVED, GeneratorUtil.POJO_ADVANCED_SAVED);
        } else {
//                    wizProps.putProperty(GeneratorUtil.POJO_ADVANCED_SAVED, GeneratorUtil.POJO_ADVANCED_SAVED);
                className = multiPanelGUI.getTargetName();
                if ( className != null) {
                    String pkgName = multiPanelGUI.getPackageName();
                    String defaultNS = GeneratorUtil.getNamespace(pkgName, className);

                    wizProps.putProperty(GeneratorUtil.POJO_ENDPOINT_NAME, className);
                    wizProps.putProperty(GeneratorUtil.POJO_INTERFACE_NAME,className + GeneratorUtil.POJO_INTERFACE_SUFFIX);
                    wizProps.putProperty(GeneratorUtil.POJO_SERVICE_NAME, className + GeneratorUtil.POJO_SERVICE_SUFFIX);
                    wizProps.putProperty(GeneratorUtil.POJO_INTERFACE_NS, defaultNS);
                    wizProps.putProperty(GeneratorUtil.POJO_SERVICE_NS, defaultNS);
                    wizProps.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NAME, className+ GeneratorUtil.POJO_OUT_MESSAGE_SUFFIX);
                    wizProps.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS, defaultNS);
                }
        }
    //throw new UnsupportedOperationException("Not supported yet.");
    }

    private void setErrorMessage( String key ) {
        if ( key == null ) {
            setLocalizedErrorMessage ( "" ); // NOI18N
        }
        else {
            setLocalizedErrorMessage ( NbBundle.getMessage( MultiTargetChooserPanelGUI.class, key)  ); // NOI18N
        }
    }
    
    private void setLocalizedErrorMessage (String message) {
        if ( wizard != null) {
            wizard.putProperty("WizardPanel_errorMessage", message);//NOI18N
        }
    }
    public boolean isValid() {
        //throw new UnsupportedOperationException("Not supported yet.");
      //  setLocalizedErrorMessage ( "" );
        OperationMethodChooserPanel panel = (OperationMethodChooserPanel)this.mComp;
        if (! MultiTargetChooserPanel.isValidTypeIdentifier(panel.getMethodName())) {
            setErrorMessage("MSG_Invalid_Method_Name"); //NOI18N
            return false;
        } 
        if ( panel.isAdvancedPanelLaunched()) {
            if (! panel.getAdvancedPanel().useDefaultValues()) {
                if (! MultiTargetChooserPanel.isValidTypeIdentifier(panel.getAdvancedPanel().getEndpointName())) {
                    setErrorMessage("MSG_Invalid_Endpoint_Name"); //NOI18N
                    return false;
                } 

                if (! MultiTargetChooserPanel.isValidTypeIdentifier(panel.getAdvancedPanel().getInterfaceName())) {
                    setErrorMessage("MSG_Invalid_Interface_Name"); //NOI18N
                    return false;
                } 

                if (! MultiTargetChooserPanel.isValidTypeIdentifier(panel.getAdvancedPanel().getServiceName())) {
                    setErrorMessage("MSG_Invalid_Service_Name"); //NOI18N
                    return false;
                } 
                String msg = GeneratorUtil.isValidNamespace(panel.getAdvancedPanel().getInterfaceNameNS(), "MSG_Invalid_Interface_Namespace");//NOI18N
                if ( msg != null) {
                    setLocalizedErrorMessage(msg);
                    return false;
                }
                msg = GeneratorUtil.isValidNamespace(panel.getAdvancedPanel().getServiceNameNS(), "MSG_Invalid_Service_Namespace");//NOI18N
                if ( msg != null) {
                    setLocalizedErrorMessage(msg);
                    return false;
                }

                if (!POJOSupportedDataTypes.Void.equals(panel.getOutputType())){
                    msg = panel.getAdvancedPanel().getOutMessageType();
                    if ( msg != null && !msg.equals("")) {
                        if (! MultiTargetChooserPanel.isValidTypeIdentifier(msg)) {
                            setErrorMessage("MSG_Invalid_OutMsg_Name"); //NOI18N
                            return false;
                        }
                    }

                     msg = GeneratorUtil.isValidNamespace(panel.getAdvancedPanel().getOutMessageTypeNS(), "MSG_Invalid_OutMsg_Namespace");//NOI18N
                    if ( msg != null) {
                        setLocalizedErrorMessage(msg);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    public void addChangeListener(ChangeListener arg0) {
        listeners.add(arg0);
    }

    public void removeChangeListener(ChangeListener arg0) {
        listeners.remove(arg0);
    }
    
    private void fireChange()
    {
        ChangeEvent e = new ChangeEvent(this);
        for(Iterator it = listeners.iterator(); it.hasNext(); ((ChangeListener)it.next()).stateChanged(e));
    }    
}
