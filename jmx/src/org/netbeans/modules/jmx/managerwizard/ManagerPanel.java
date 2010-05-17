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
package org.netbeans.modules.jmx.managerwizard;

import java.awt.Component;
import java.util.ResourceBundle;

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
 * Class handling the graphical part of the standard Manager wizard panel
 *
 */
public class ManagerPanel extends javax.swing.JPanel {
    
    private ManagerWizardPanel wiz;
    private ResourceBundle bundle;
    private static WizardDescriptor wizDesc;
    private boolean mainMethodSelected = true;
    private boolean mainClassSelected = true;
    private boolean sampleCodeSelected = true;
    
    /**
     * Create the wizard panel component and set up some basic properties.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     */
    public ManagerPanel (ManagerWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(ManagerPanel.class);
        initComponents ();
        
        Mnemonics.setLocalizedText(generateMainMethodJCheckBox,
                bundle.getString("LBL_chkMainMethod.text"));// NOI18N
        Mnemonics.setLocalizedText(setAsMainClassJCheckBox,
                bundle.getString("LBL_chkMainClass.text"));// NOI18N
        Mnemonics.setLocalizedText(generateSampleCodeJCheckBox,
                bundle.getString("LBL_chkSampleCode.text"));// NOI18N
        
        // Accessibility
        generateMainMethodJCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_CREATE_MAIN_METHOD"));// NOI18N
        generateMainMethodJCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_CREATE_MAIN_METHOD_DESCRIPTION"));// NOI18N
        setAsMainClassJCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_SET_MAIN_CLASS"));// NOI18N
        setAsMainClassJCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_SET_MAIN_CLASS_DESCRIPTION"));// NOI18N
        generateSampleCodeJCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_GENERATE_SAMPLE"));// NOI18N
        generateSampleCodeJCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_GENERATE_SAMPLE_DESCRIPTION"));// NOI18N

        
        // init flags
        mainMethodSelected = generateMainMethodJCheckBox.isSelected();
        mainClassSelected = setAsMainClassJCheckBox.isSelected();
        sampleCodeSelected = generateSampleCodeJCheckBox.isSelected();
        
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_Manager_Panel"));// NOI18N 
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
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
        managerOptionsPanel = new javax.swing.JPanel();
        generateMainMethodJCheckBox = new javax.swing.JCheckBox();
        setAsMainClassJCheckBox = new javax.swing.JCheckBox();
        generateSampleCodeJCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.BorderLayout());

        managerOptionsPanel.setLayout(new javax.swing.BoxLayout(managerOptionsPanel, javax.swing.BoxLayout.Y_AXIS));

        generateMainMethodJCheckBox.setSelected(true);
        generateMainMethodJCheckBox.setName("managerGenerateMainMethodCheckBox");
        generateMainMethodJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateMainMethodJCheckBoxActionPerformed(evt);
            }
        });

        managerOptionsPanel.add(generateMainMethodJCheckBox);

        setAsMainClassJCheckBox.setSelected(true);
        setAsMainClassJCheckBox.setName("managerSetAsMainClassCheckBox");
        setAsMainClassJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setAsMainClassJCheckBoxActionPerformed(evt);
            }
        });

        managerOptionsPanel.add(setAsMainClassJCheckBox);

        generateSampleCodeJCheckBox.setSelected(true);
        generateSampleCodeJCheckBox.setName("generateSampleCodeCheckBox");
        generateSampleCodeJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateSampleCodeJCheckBoxActionPerformed(evt);
            }
        });

        managerOptionsPanel.add(generateSampleCodeJCheckBox);

        add(managerOptionsPanel, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents

    private void generateSampleCodeJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateSampleCodeJCheckBoxActionPerformed
        sampleCodeSelected = generateSampleCodeJCheckBox.isSelected();
    }//GEN-LAST:event_generateSampleCodeJCheckBoxActionPerformed

    private void setAsMainClassJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setAsMainClassJCheckBoxActionPerformed
        mainClassSelected = setAsMainClassJCheckBox.isSelected();
    }//GEN-LAST:event_setAsMainClassJCheckBoxActionPerformed

    private void generateMainMethodJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateMainMethodJCheckBoxActionPerformed
        mainMethodSelected = generateMainMethodJCheckBox.isSelected();
        
        //mainClassJCheckBox.setEnabled(mainSelected && shouldEnableMainProjectClass());
        setAsMainClassJCheckBox.setEnabled(mainMethodSelected && shouldEnableMainProjectClass());
        generateSampleCodeJCheckBox.setEnabled(mainMethodSelected);
        
        if (!mainMethodSelected) {
                mainClassSelected= setAsMainClassJCheckBox.isSelected();
                sampleCodeSelected = generateSampleCodeJCheckBox.isSelected();
                setAsMainClassJCheckBox.setSelected(false);
                generateSampleCodeJCheckBox.setSelected(false);
                
            } else {
                setAsMainClassJCheckBox.setSelected(mainClassSelected);
                generateSampleCodeJCheckBox.setSelected(sampleCodeSelected);
            }
       
    }//GEN-LAST:event_generateMainMethodJCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox generateMainMethodJCheckBox;
    private javax.swing.JCheckBox generateSampleCodeJCheckBox;
    private javax.swing.JPanel managerOptionsPanel;
    private javax.swing.JCheckBox setAsMainClassJCheckBox;
    // End of variables declaration//GEN-END:variables
    
    /**
     *
     * Class handling the standard Manager wizard panel
     *
     */
    public static class ManagerWizardPanel extends GenericWizardPanel 
    {    
        private ManagerPanel panel = null;
        
        /**
         * Returns the Manager panel.
         * @return <CODE>Component</CODE> the manager panel
         */
        public Component getComponent () { return getPanel(); }
        
        private ManagerPanel getPanel() 
        {
            if (panel == null) {
                panel = new ManagerPanel(this);
            }
            return panel;
        }

        /**
         * Returns if the main method is to be generated
         * @return <CODE>boolean</CODE> true if the main method is to be 
         * generated
         */
        public boolean isMainMethodSelected() 
        { 
            return getPanel().mainMethodSelected;
        }
        
        /**
         * Returns if the set as main class is selected.
         * @return <CODE>boolean</CODE> true if set as main class is selected
         */
        public boolean isMainClassSelected() 
        { 
            return getPanel().mainClassSelected;
        }
        
        /**
         * Returns if the sample code checkbox is selected
         * @return <CODE>boolean</CODE> true if generate sample code is selected
         */
        public boolean isSampleCodeSelected() {
            return getPanel().sampleCodeSelected;
        }
        
        /**
         * This method is called when a step is loaded.
         * @param settings <CODE>Object</CODE> an object containing the wizard informations.
         */
        public void readSettings (Object settings) 
        {
            wizDesc = (WizardDescriptor) settings;
            if (!shouldEnableMainProjectClass())
                getPanel().setAsMainClassJCheckBox.setEnabled(false);
        }
        
        /**
         * This method is called when the user quit a step.
         * @param settings <CODE>Object</CODE> an object containing the wizard informations.
         */
        public void storeSettings (Object settings) 
        {            
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            wiz.putProperty(WizardConstants.PROP_MANAGER_MAIN_METHOD_SELECTED,  
                    new Boolean(isMainMethodSelected()));
            wiz.putProperty(WizardConstants.PROP_MANAGER_MAIN_CLASS_SELECTED, 
                    new Boolean(isMainClassSelected()));
            wiz.putProperty(WizardConstants.PROP_MANAGER_SAMPLE_CODE_SELECTED, 
                    new Boolean(isSampleCodeSelected()));
        } 
        
        /**
         * Returns the corresponding help context.
         * @return <CODE>HelpCtx</CODE> the corresponding help context.
         */
        public HelpCtx getHelp() {
           return new HelpCtx("jmx_manager_app");// NOI18N
        }
      
    } 

}
