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

package org.netbeans.modules.wsdlextensions.ldap;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.openide.WizardDescriptor;


/**
 * Wizard Iterator used to plug-in from the WSDL Wizard
 * 
 * @author jalmero
 */
public class LDAPBrowserWizardIterator extends WSDLWizardExtensionIterator  {

    private WizardDescriptor wizard;
    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] wsdlPanels;  
    private WSDLWizardContext wsdlContext = null;
        private ResourceBundle mBundle = ResourceBundle.getBundle(
            "org.netbeans.modules.wsdlextensions.ldap.template.Bundle");    
    private LDAPServerBrowserWizardPanel2 mLDAPSettingsPanel = null;
    private LDAPServerBrowserWizardPanel3 mLDAPOptionsPanel = null;
    
    public LDAPBrowserWizardIterator(WSDLWizardContext context) {
        super(context);
        wsdlContext = context;
        wsdlPanels = getPanels();
    }
    
    public LDAPBrowserWizardIterator() {
        this(null);
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WSDLWizardDescriptorPanel[] getPanels() {
        if (wsdlPanels == null) {
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                mLDAPSettingsPanel = new LDAPServerBrowserWizardPanel2(wsdlContext),
                mLDAPOptionsPanel = new LDAPServerBrowserWizardPanel3(wsdlContext)
            };
            String[] steps = new String[wsdlPanels.length];
            for (int i = 0; i < wsdlPanels.length; i++) {
                Component c = wsdlPanels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return wsdlPanels;
    }

    public Set instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;

    }

    public void uninitialize(WizardDescriptor wizard) {
        wsdlPanels = null;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;     
        
        // since only one template, no need to recreate if already exists
        if (wsdlPanels == null) {
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        mLDAPSettingsPanel = new LDAPServerBrowserWizardPanel2(wsdlContext),
                        mLDAPOptionsPanel = new LDAPServerBrowserWizardPanel3(wsdlContext)
                    };
        }
                
        if (templateName.equals("FromLDAPServer")) { // NOI18N
            // From LDAP Server mode
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        mLDAPSettingsPanel, mLDAPOptionsPanel
            };  
            mLDAPSettingsPanel.setLDAPServerMode(true);
            if (wizard != null) {
                wizard.putProperty("FROM_LDAP_SERVER", new Boolean(true));
                wizard.putProperty("FROM_LDIF_FILE", new Boolean(false));                
            }
        } else if (templateName.equals("FromLdifFile")) { // NOI18N
            // From LDIF File mode
            wsdlPanels = new WSDLWizardDescriptorPanel[]{
                        mLDAPSettingsPanel, mLDAPOptionsPanel
            };         
            mLDAPSettingsPanel.setLDAPServerMode(false);
            if (wizard != null) {
                wizard.putProperty("FROM_LDAP_SERVER", new Boolean(false));
                wizard.putProperty("FROM_LDIF_FILE", new Boolean(true));                
            }
        }     
        
        steps = new String[wsdlPanels.length];
        int i = 0;
        for (WSDLWizardDescriptorPanel panel : wsdlPanels) {
            steps[i++] = panel.getName();
        }
        
    }

    public WSDLWizardDescriptorPanel current() {
        WSDLWizardDescriptorPanel ldapPanels[] = getPanels();      
        return ldapPanels[currentStepIndex];
    }

    public String[] getSteps() {
        assert templateName != null : "template is not set";
        return steps;
    }

    public boolean hasNext() {
        return currentStepIndex < wsdlPanels.length - 1;
    }

    public boolean hasPrevious() {
        return true;
    }

    public void nextPanel() {
        
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
     /*   if (currentStepIndex > -1) {
            WSDLWizardDescriptorPanel wp = current();
            if(wp instanceof LDAPServerBrowserWizardPanel2){
                LDAPServerBrowserWizardPanel2 wp2 = 
                        (LDAPServerBrowserWizardPanel2)wp;
                String str2=wp2.validate();
                if(!"".equals(str2)){
                    JOptionPane.showMessageDialog(null, str2, "Warning", 
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }  
        }*/
        currentStepIndex++;     
    }

    public void previousPanel() {
        currentStepIndex--;
    }

    @Override
    public boolean commit() {
        boolean status = true;
        // ldap creates their own wsdl file.  instead at the end
        return status;
    } 
}

