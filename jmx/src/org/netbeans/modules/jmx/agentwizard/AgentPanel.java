/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.agentwizard;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.event.*;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.GenericWizardPanel;

import org.netbeans.modules.jmx.common.runtime.J2SEProjectType;
/**
 *
 * Class handling the graphical part of the standard Agent wizard panel
 *
 */
public class AgentPanel extends javax.swing.JPanel {
    
    private AgentWizardPanel wiz;
    private ResourceBundle bundle;
    private static WizardDescriptor wizDesc;
    private boolean agentNameSelected = false;
    private boolean updateNameRunning = false;
    private boolean mainSelected = false;
    private boolean mainClassSelected = false;
    private boolean codeExampleSelected = false;
    
    /**
     * Create the wizard panel component and set up some basic properties.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     */
    public AgentPanel (AgentWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(JMXAgentIterator.class);
        initComponents ();
        
        Mnemonics.setLocalizedText(mainJCheckBox,
                                   bundle.getString("LBL_chkMain.text"));//NOI18N
        Mnemonics.setLocalizedText(codeExampleJCheckBox,
                                   bundle.getString("LBL_chkCodeExample.text"));//NOI18N
        Mnemonics.setLocalizedText(mainClassJCheckBox,
                                   bundle.getString("LBL_chkMainClass.text"));//NOI18N
        
        // init flags
        codeExampleSelected = codeExampleJCheckBox.isSelected();
        mainSelected = mainJCheckBox.isSelected();
        mainClassSelected = mainClassJCheckBox.isSelected();
        
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(AgentPanel.class, "LBL_Agent_Panel"));  //NOI18N
        
         // Accessibility
        mainJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_CREATE_MAIN_METHOD"));// NOI18N
        mainJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_CREATE_MAIN_METHOD_DESCRIPTION"));// NOI18N
        
        mainClassJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SET_MAIN_CLASS"));// NOI18N
        mainClassJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SET_MAIN_CLASS_DESCRIPTION"));// NOI18N
        
        codeExampleJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_GENERATE_SAMPLE"));// NOI18N
        codeExampleJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_GENERATE_SAMPLE_DESCRIPTION"));// NOI18N
    }
    
    private static boolean shouldEnableMainProjectClass() {
        Project project = Templates.getProject(wizDesc);
        return J2SEProjectType.isProjectTypeSupported(project);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        agentOptionsPanel = new javax.swing.JPanel();
        mainJCheckBox = new javax.swing.JCheckBox();
        mainClassJCheckBox = new javax.swing.JCheckBox();
        codeExampleJCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        agentOptionsPanel.setLayout(new javax.swing.BoxLayout(agentOptionsPanel, javax.swing.BoxLayout.Y_AXIS));

        mainJCheckBox.setSelected(true);
        mainJCheckBox.setName("agentMainMethodCheckBox");
        mainJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainJCheckBoxActionPerformed(evt);
            }
        });

        agentOptionsPanel.add(mainJCheckBox);

        mainClassJCheckBox.setSelected(true);
        mainClassJCheckBox.setName("mainClass");
        agentOptionsPanel.add(mainClassJCheckBox);

        codeExampleJCheckBox.setSelected(true);
        codeExampleJCheckBox.setName("agentSampleCodeCheckBox");
        codeExampleJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeExampleJCheckBoxActionPerformed(evt);
            }
        });

        agentOptionsPanel.add(codeExampleJCheckBox);

        add(agentOptionsPanel, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents

    private void codeExampleJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeExampleJCheckBoxActionPerformed
        codeExampleSelected = codeExampleJCheckBox.isSelected();
    }//GEN-LAST:event_codeExampleJCheckBoxActionPerformed

    private void mainJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainJCheckBoxActionPerformed
        mainSelected = mainJCheckBox.isSelected();
        
        mainClassJCheckBox.setEnabled(mainSelected && shouldEnableMainProjectClass());
        
        if (!mainSelected) {
            mainClassSelected = mainClassJCheckBox.isSelected();
            mainClassJCheckBox.setSelected(false);
            
        } else {
            mainClassJCheckBox.setSelected(mainClassSelected);
        }
        
    }//GEN-LAST:event_mainJCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel agentOptionsPanel;
    private javax.swing.JCheckBox codeExampleJCheckBox;
    private javax.swing.JCheckBox mainClassJCheckBox;
    private javax.swing.JCheckBox mainJCheckBox;
    // End of variables declaration//GEN-END:variables
    
    /**
     *
     * Class handling the standard Agent wizard panel
     *
     */
    public static class AgentWizardPanel extends GenericWizardPanel 
    {    
        private AgentPanel panel = null;
        
        /**
         * Returns the agent panel.
         * @return <CODE>Component</CODE> the agent panel
         */
        public Component getComponent () { return getPanel(); }
        
        private AgentPanel getPanel() 
        {
            if (panel == null) {
                panel = new AgentPanel(this);
            }
            return panel;
        }

        /**
         * Returns if the main method generation is selected.
         * @return <CODE>boolean</CODE> true if main method generation is selected
         */
        public boolean isMainSelected() 
        { 
            return getPanel().mainSelected;
        }
        
        /**
         * Returns if the set as main class is selected.
         * @return <CODE>boolean</CODE> true if set as main class is selected
         */
        public boolean isMainClassSelected() 
        { 
            return getPanel().mainClassJCheckBox.isSelected();
        }
        
        /**
         * Returns if source code hints is selected.
         * @return <CODE>boolean</CODE> true if source code hints is selected.
         */
        public boolean isCodeExampleSelected() 
        {
            return getPanel().codeExampleSelected;
        }

        /**
         * This method is called when a step is loaded.
         * @param settings <CODE>Object</CODE> an object containing the wizard informations.
         */
        public void readSettings (Object settings) 
        {
            wizDesc = (WizardDescriptor) settings;
            if (!shouldEnableMainProjectClass())
                getPanel().mainClassJCheckBox.setEnabled(false);
        }
        
        /**
         * This method is called when the user quit a step.
         * @param settings <CODE>Object</CODE> an object containing the wizard informations.
         */
        public void storeSettings (Object settings) 
        {            
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            wiz.putProperty(WizardConstants.PROP_AGENT_MAIN_METHOD_SELECTED, 
                    new Boolean(isMainSelected()));
            wiz.putProperty(WizardConstants.PROP_AGENT_MAIN_CLASS_SELECTED, 
                    new Boolean(isMainClassSelected()));
            wiz.putProperty(WizardConstants.PROP_AGENT_SAMPLE_CODE_SELECTED, 
                    new Boolean(isCodeExampleSelected()));
        }
        
        /**
         * Returns the corresponding help context.
         * @return <CODE>HelpCtx</CODE> the corresponding help context.
         */
        public HelpCtx getHelp() {
           return new HelpCtx("tutorial"); //NOI18N
        }
      
    } 

}
