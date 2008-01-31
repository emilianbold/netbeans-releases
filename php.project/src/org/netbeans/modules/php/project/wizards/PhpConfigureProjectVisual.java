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

package org.netbeans.modules.php.project.wizards;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

class PhpConfigureProjectVisual extends JPanel {

    private static final long serialVersionUID = 6001805042236340989L;
    
    private static final String ACS_NAME_PANEL           
                                        = "ACS_NamePanel_A11YDesc";  // NOI18N
    private static final String NEW_PROJECT              
                                        = "LBL_NewProject"; // NOI18N
    public static final String NEW_PROJECT_WIZARD_TITLE  
                                        = "NewProjectWizard_Title"; // NOI18N
    
    PhpConfigureProjectVisual( PhpProjectConfigurePanel panel ) {
        myPanel = panel;
        
        initComponents();
        
        init(panel); 
    }

    boolean dataIsValid( WizardDescriptor wizardDescriptor) {
        boolean valid =  getLocationPanel().dataIsValid( wizardDescriptor );
        if ( !valid ){
            return false;
        }
        valid = getSourcesPanel().dataIsValid( wizardDescriptor );
        if (!valid){
            return false;
        }
        valid = getOptionsPanel().dataIsValid( wizardDescriptor );
        if (!valid){
            return false;
        }
        wizardDescriptor.putProperty( 
                NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, "");
        return true;
    }

    void read (WizardDescriptor descriptor) {
        getLocationPanel().read(descriptor);
        getSourcesPanel().read(descriptor);
        getOptionsPanel().read(descriptor);
    }

    void store(WizardDescriptor descriptor) {
        getLocationPanel().store(descriptor);
        getSourcesPanel().store(descriptor);
        if ( getOptionsPanel() != null ) {
            getOptionsPanel().store(descriptor);
        }
    }
    
    private PhpProjectConfigurePanel getPanel(){
        return myPanel;
    }
    
    private PanelProjectLocationVisual getLocationPanel() {
        return myProjectLocationPanel;
    }
    
    private PanelOptionsVisual getOptionsPanel() {
        return myOptionsPanel;
    }
    
    private ExistingSourcesPanel getSourcesPanel() {
        return mySourcesPanel;
    }

    private void init( PhpProjectConfigurePanel panel ) {
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getBundle( NewPhpProjectWizardIterator.class ).
                    getString(ACS_NAME_PANEL));  
        
        configureProjectLocationPanel(panel);
        
        configureSourcesPanel(panel);
                
        configureOptionsPanel(panel);
        
        myProjectLocationPanel.addPropertyChangeListener(mySourcesPanel);
        mySourcesPanel.addPropertyChangeListener(myOptionsPanel);
        
        // Provide a name in the title bar.
        setName(NbBundle.getBundle( NewPhpProjectWizardIterator.class).
                getString( NewPhpProjectWizardIterator.STEP_PROJECT  ));
        String title = panel.getTitle();
        if ( title == null ) {
            title = NbBundle.getBundle(NewPhpProjectWizardIterator.class).
                getString(NEW_PROJECT);
        }
        putClientProperty (NEW_PROJECT_WIZARD_TITLE, title );
        
    }
    
    
    private void configureProjectLocationPanel(PhpProjectConfigurePanel panel ){
        myProjectLocationPanel = new PanelProjectLocationVisual( panel );
        myLocationContainer.add( BorderLayout.NORTH, myProjectLocationPanel );

    }
    
    private void configureSourcesPanel(PhpProjectConfigurePanel panel) {
        mySourcesPanel = new ExistingSourcesPanel(panel);
        mySourcesContainer.add(BorderLayout.NORTH, mySourcesPanel);
    }

    private void configureOptionsPanel(PhpProjectConfigurePanel panel) {
        if ( panel.isFull() ) {
            myOptionsPanel = new PanelOptionsVisual(panel);
            myOptionsContainer.add( BorderLayout.NORTH, myOptionsPanel );
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        myLocationContainer = new javax.swing.JPanel();
        mySourcesContainer = new javax.swing.JPanel();
        mySeparator = new javax.swing.JSeparator();
        myOptionsContainer = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(0, 52));
        setRequestFocusEnabled(false);
        setLayout(new java.awt.GridBagLayout());

        myLocationContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(myLocationContainer, gridBagConstraints);

        mySourcesContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(mySourcesContainer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        add(mySeparator, gridBagConstraints);

        myOptionsContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(myOptionsContainer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel myLocationContainer;
    private javax.swing.JPanel myOptionsContainer;
    private javax.swing.JSeparator mySeparator;
    private javax.swing.JPanel mySourcesContainer;
    // End of variables declaration//GEN-END:variables
    
    private PanelProjectLocationVisual myProjectLocationPanel;
    private ExistingSourcesPanel mySourcesPanel;
    private PanelOptionsVisual myOptionsPanel;
    private PhpProjectConfigurePanel myPanel;
    
}
