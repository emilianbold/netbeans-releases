/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * WSDLWizardConstants.java
 *
 * Created on August 31, 2006, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import java.awt.Component;
import java.beans.PropertyChangeListener;

import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author radval
 */
public class WSDLWizardBindingConfigurationWrapperStep implements WizardDescriptor.FinishablePanel {
    
    
    private WSDLWizardDescriptorPanel mWPanel;
    
    //private Logger logger = Logger.getLogger(WSDLWizardBindingConfigurationWrapperStep.class.getName());
    
    private WSDLWizardContext context;
    
    private String mErrorMessage;
    private String mWarningMessage;
    private String mInfoMessage;
    private WizardDescriptor wiz;
    private PropertyChangeListener propertyChangeListener = new WizardPanelPropertyChangeListener();
    
    public WSDLWizardBindingConfigurationWrapperStep(WSDLWizardContext context) {
        this.context = context;
    }

    public void setWizardDescriptorPanel(WSDLWizardDescriptorPanel panel) {
        if (mWPanel == panel) return;
        mWPanel = panel;
        mErrorMessage = null;
        mWarningMessage = null;
        mInfoMessage = null;
    }

    public void addChangeListener(ChangeListener l) {
//        logger.info("addChangeListener called..........");
        mWPanel.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
//        logger.info("removeChangeListener called..........");
        mWPanel.removeChangeListener(l);
    }
    
    public String getName() {
        return mWPanel.getName();
    }

    public Component getComponent() {
//        logger.info("getComponent called..........");
        Component component = mWPanel.getComponent();
        component.setName(getName());
        component.getAccessibleContext().setAccessibleDescription(getName());
        if (component instanceof JComponent) {
            JComponent jc = (JComponent) component;
            jc.putClientProperty("WizardPanel_contentData", context.getSteps());
            jc.putClientProperty("WizardPanel_contentSelectedIndex", context.getStepIndex());
            component.removePropertyChangeListener(propertyChangeListener);
            component.addPropertyChangeListener(propertyChangeListener);
            
        }
        return component;
    }

//    private String[] getSteps() {
//        return context.getSteps();
//    }
//    
//    private Integer getStepIndex() {
//        return context.getStepIndex();
//    }
    public HelpCtx getHelp() {
        return mWPanel.getHelp();
    }

    public boolean isValid() {
//        logger.info("isValid called..........");
        return mErrorMessage == null && mWPanel.isValid();
    }

    public void readSettings(Object settings) {
//        logger.info("readSettings called..........");
        mWPanel.readSettings(settings);
        wiz = (WizardDescriptor) settings;
        if (mErrorMessage != null) {
            wiz.putProperty("WizardPanel_errorMessage", mErrorMessage);
        } else if (mWarningMessage != null) {
            wiz.putProperty("WizardPanel_warningMessage", mWarningMessage); // NOI18N
        }
    }

    public void storeSettings(Object settings) {
//        logger.info("storeSettings called..........");
        mWPanel.storeSettings(settings);
    }
    
    public boolean isFinishPanel() {
//        logger.info("isFinishPanel called..........");
        return mWPanel.isFinishPanel();
    }
    
    
    class WizardPanelPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof String) {
                String message = (String) evt.getNewValue();
                if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT)) {
                    mErrorMessage = message;
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, mErrorMessage); // NOI18N
                } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_WARNING_EVT)) {
                    mWarningMessage = message;
                    wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, mWarningMessage); // NOI18N
                } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_NORMAL_MESSAGE_EVT)) {
                    mInfoMessage = message;
                    wiz.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, mInfoMessage); // NOI18N
                } else if (evt.getPropertyName().equals(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT)) {
                    mErrorMessage = null;
                    mWarningMessage = null;
                    mInfoMessage = null;
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
                    wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message); // NOI18N
                    wiz.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message); // NOI18N
                }
                mWPanel.fireChange();
            }
        }
    }
    
}

