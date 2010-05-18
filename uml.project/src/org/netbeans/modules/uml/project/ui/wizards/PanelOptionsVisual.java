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

package org.netbeans.modules.uml.project.ui.wizards;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.common.ReferencedJavaProjectPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 *
 * @author  Mike Frisino
 */
public class PanelOptionsVisual extends SettingsPanel
{
    private static boolean lastMainClassCheck = true; // XXX Store somewhere
    private PanelConfigureProject panelConfigureProject;
    private boolean valid;
    private int wizardType = NewUMLProjectWizardIterator.TYPE_UML;
    private ReferencedJavaProjectPanel javaProjectPanel;
//    private RoseImportProjectPanel roseImportProjectPanel;

    public final static String MODE_CHANGED_PROP = "MODE_CHANGED"; // NOI18N
    
    public PanelOptionsVisual(PanelConfigureProject panel, int type)
    {
        initComponents();
        this.panelConfigureProject = panel;
        this.wizardType = type;
        
        switch (type)
        {
            case NewUMLProjectWizardIterator.TYPE_UML:
            case NewUMLProjectWizardIterator.TYPE_UML_JAVA:
                hideJavaProjectPanel();
//                hideRoseImportProjectPanel();
                break;
                
            case NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER:
                createJavaProjectPanel();
                showJavaProjectPanel();
//                hideRoseImportProjectPanel();
                break;

//            case NewUMLProjectWizardIterator.TYPE_ROSE_IMPORT:
//                createRoseImportProjectPanel();
//                showRoseImportProjectPanel();
//                hideJavaProjectPanel();
//                break;
        }
    }
    

    private void createJavaProjectPanel()
    {
        if (javaProjectPanel == null)
        {
            javaProjectPanel = new ReferencedJavaProjectPanel(panelConfigureProject, wizardType);
            add(javaProjectPanel, BorderLayout.CENTER);
        }
    }
    
//    private void createRoseImportProjectPanel()
//    {
//        if (roseImportProjectPanel == null)
//        {
//            // roseImportProjectPanel = new PanelRoseImport(panelConfigureProject, wizardType);
//            roseImportProjectPanel = new RoseImportProjectPanel();
//            add(roseImportProjectPanel, BorderLayout.CENTER);
//        }
//    }
    
    private void showJavaProjectPanel()
    {
        javaProjectPanel.setVisible(true);

        javaProjectPanel.addPropertyChangeListener(
            ReferencedJavaProjectPanel.ASSOCIATED_JAVA_PROJ_PROP, 
            panelConfigureProject);
        
        javaProjectPanel.addPropertyChangeListener(
            ReferencedJavaProjectPanel.SOURCE_GROUP_CHANGED_PROP, 
            panelConfigureProject);
    }

//    private void showRoseImportProjectPanel()
//    {
//        roseImportProjectPanel.setVisible(true);
//
//        roseImportProjectPanel.addPropertyChangeListener(
//            RoseImportProjectPanel.ROSE_MODEL_PROP, 
//            panelConfigureProject);
//    }

    private void hideJavaProjectPanel()
    {
        // if never instatiated, then it doesn't need to be hidden
        // this is just defensive code and shouldn't happen
        if (javaProjectPanel == null)
            return;
        
        javaProjectPanel.setVisible(false);
        
        javaProjectPanel.removePropertyChangeListener(
            ReferencedJavaProjectPanel.ASSOCIATED_JAVA_PROJ_PROP, 
            panelConfigureProject);

        javaProjectPanel.removePropertyChangeListener(
            ReferencedJavaProjectPanel.SOURCE_GROUP_CHANGED_PROP, 
            panelConfigureProject);
    }
    
//    private void hideRoseImportProjectPanel()
//    {
//        // if never instatiated, then it doesn't need to be hidden
//        // this is just defensive code and shouldn't happen
//        if (roseImportProjectPanel == null)
//            return;
//        
//        roseImportProjectPanel.setVisible(false);
//        
//        roseImportProjectPanel.removePropertyChangeListener(
//            RoseImportProjectPanel.ROSE_MODEL_PROP, 
//            panelConfigureProject);
//    }
    

    boolean valid(WizardDescriptor settings)
    {
        /* MCF - TODO - this is sample of bulletproofing code from the
         * j2se project or wherever i copied it from. We may need to do similar
         * here
         *
        if (mainClassTextField.isVisible () && mainClassTextField.isEnabled ()) 
         {
            if (!valid) 
            {
                settings.putProperty(
                    NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelOptionsVisual.class,
                        "ERROR_IllegalMainClassName")); //NOI18N
            }

            return this.valid;
        }
        
        else 
            return true;
         */

        if (javaProjectPanel != null)
            return javaProjectPanel.valid(settings);
        
//        else if (roseImportProjectPanel != null)
//            return roseImportProjectPanel.valid(settings);

        else
            return true;
    }
    
    void read(WizardDescriptor wizDesc)
    {
        // TODO: do we need to do anything?
    }
    
    void validate(WizardDescriptor wizDesc) 
        throws WizardValidationException
    {
        // nothing to validate
        if (wizardType == NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER)
        {
            // TODO - make sure they have selected a target
            // otherwise there is nothing to rev engineer
            if (javaProjectPanel.getSelectedProject() == null)
            {
                // TODO set message and throw except
            }
        }
    }
    
    void store(WizardDescriptor wizDesc)
    {
        wizDesc.putProperty(
            NewUMLProjectWizardIterator.PROP_SET_AS_MAIN, Boolean.FALSE);
        
        if (wizardType == NewUMLProjectWizardIterator.TYPE_UML)
        {
            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_MODELING_MODE,
                UMLProject.PROJECT_MODE_ANALYSIS_STR);
        }
        
        else if (wizardType == NewUMLProjectWizardIterator.TYPE_UML_JAVA)
        {
            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_MODELING_MODE,
                UMLProject.PROJECT_MODE_DESIGN_STR);
        }
        
        if (wizardType == NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER)
        {
            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_MODELING_MODE,
                UMLProject.PROJECT_MODE_IMPL_STR);

            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_JAVA_SOURCE_PROJECT,
                javaProjectPanel.getSelectedProject());
            
            wizDesc.putProperty(
                NewUMLProjectWizardIterator.PROP_JAVA_SOURCE_ROOTS_MODEL,
                javaProjectPanel.getJavaSourceRootsModel());
        }
        
//        if (wizardType == NewUMLProjectWizardIterator.TYPE_ROSE_IMPORT)
//        {
//            wizDesc.putProperty(
//                NewUMLProjectWizardIterator.PROP_ROSE_FILE,
//                roseImportProjectPanel.getRoseFile());
//            
//            wizDesc.putProperty(
//                NewUMLProjectWizardIterator.PROP_MODELING_MODE,
//                roseImportProjectPanel.getModelingMode());
//        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new BorderLayout());

        getAccessibleContext().setAccessibleName("");
        getAccessibleContext().setAccessibleDescription("");
    }// </editor-fold>//GEN-END:initComponents

    
    
//    public void propertyChange(PropertyChangeEvent evt)
//    {
//        if (evt.getPropertyName().equals(
//            NewUMLProjectWizardIterator.PROP_WIZARD_TYPE))
//        {
//            if (((Integer)evt.getNewValue()).intValue() == 
//                NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER)
//            {
//                showJavaProjectPanel();
//            }
//            
//            else
//                hideJavaProjectPanel();
//        }
//    }
    
    // This panelConfigureProject is added at the bottom of the container to
    // just pushes the other items up so the layout is as intended.
    // MCF - this was needed when we were using the grid bag layout
    // but is not needed if we stick with border
//    private void addBottomPanel() {
//     
//        jPanel1 = new javax.swing.JPanel();
//        //jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 255, 0)));
//        java.awt.GridBagConstraints gridBagConstraints;
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.weighty = 1.0;
//        add(jPanel1, gridBagConstraints);
//        jPanel1.getAccessibleContext().setAccessibleName(
//            java.util.ResourceBundle.getBundle(
//            "org/netbeans/modules/uml/project/ui/wizards/Bundle").getString("ACSN_jPanel1"));
//        jPanel1.getAccessibleContext().setAccessibleDescription(
//            java.util.ResourceBundle.getBundle(
//            "org/netbeans/modules/uml/project/ui/wizards/Bundle").getString("ASCD_jPanel1"));
//    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
