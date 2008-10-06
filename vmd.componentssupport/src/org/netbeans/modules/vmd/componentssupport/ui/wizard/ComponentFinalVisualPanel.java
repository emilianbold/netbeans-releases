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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.componentssupport.ui.UIUtils;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.BaseHelper;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.CustomComponentHelper;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Name and Location</em> panel in J2ME Library Descriptor Wizard.
 *
 * @author ads
 */
final class ComponentFinalVisualPanel extends JPanel {
    
    /** Creates new NameAndLocationPanel */
    ComponentFinalVisualPanel(ComponentFinalWizardPanel panel) {
        myPanel = panel;
        initComponents();

    }
    

    void storeData(WizardDescriptor descriptor) {
        // nothing to do on this step
    }
    
    void readData( WizardDescriptor descriptor) {
        mySettings = descriptor;
        
        projectNameValue.setText( getProjectName() );
        
        setFilesInfoIntoTextAreas();
    }

    private String getProjectName(){
        return getHelper().getProjectName();
    }
    
    private CustomComponentHelper getHelper(){
        return (CustomComponentHelper)mySettings.getProperty( 
                NewComponentDescriptor.HELPER);
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(ComponentFinalVisualPanel.class);
    }
    
    private String getCodeNameBase(){
        return getHelper().getCodeNameBase();
    }

    private String getCDClassName() {
        return (String) mySettings.getProperty(
                NewComponentDescriptor.CD_CLASS_NAME);
    }

    private String getProducerClassName() {
        return (String) mySettings.getProperty(
                NewComponentDescriptor.CP_CLASS_NAME);
    }

    private String getSmallIconPath() {
        String path = (String) mySettings.getProperty(
                NewComponentDescriptor.CP_SMALL_ICON);
        if (path == null || path.length() == 0){
            return null;
        }
        return path;
    }

    private String getLargeIconPath() {
        String path = (String) mySettings.getProperty(
                NewComponentDescriptor.CP_LARGE_ICON);
        if (path == null || path.length() == 0){
            return null;
        }
        return path;
    }

    private void setFilesInfoIntoTextAreas() {
        List<String> created = new ArrayList<String>();
        List<String> modified = new ArrayList<String>();

        addCDToList(created, modified);
        addProducerToList(created, modified);
        addLayerXmlToList(created, modified);
        addBundleToList(created, modified);
        addIconsToList(created, modified);

        // publish
            createdFilesValue.setText(UIUtils.generateTextAreaContent(
                    created.toArray(new String[]{})));
            modifiedFilesValue.setText(UIUtils.generateTextAreaContent(
                    modified.toArray(new String[]{})));

        
    }

    // TODO get path from helper
    private void addCDToList(List<String> created, List<String> modified){
        String dotCodeNameBase = getCodeNameBase();
        String name = getCDClassName();

        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        
        created.add(
                codeNameBase + "/" + BaseHelper.DESCRIPTORS + "/" + 
                name + BaseHelper.JAVA_EXTENSION); // NOI18N
    }
    // TODO get path from helper
    private void addProducerToList(List<String> created, List<String> modified){
        String dotCodeNameBase = getCodeNameBase();
        String name = getProducerClassName();

        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        
        created.add(
                codeNameBase + "/" + BaseHelper.PRODUCERS + "/" +
                name + BaseHelper.JAVA_EXTENSION); // NOI18N
    }
    
    private void addLayerXmlToList(List<String> created, List<String> modified){
        
        String dotCodeNameBase = getCodeNameBase();
        
        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        modified.add(
                codeNameBase + "/" + CustomComponentWizardIterator.LAYER_XML); // NOI18N
    }
    
    private void addBundleToList(List<String> created, List<String> modified){
        String dotCodeNameBase = getCodeNameBase();
        
        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        String bundle = codeNameBase + "/" +                                    // NOI18N
                CustomComponentWizardIterator.BUNDLE_PROPERTIES;
        // simply add to modified, while for the first producer it will be created.
        modified.add(bundle);
    }
    
    private void addIconsToList(List<String> created, List<String> modified){
        String small = getSmallIconPath();
        if (small != null){
            created.add( getFinalIconPath(small) );
        }
        
        String large = getLargeIconPath();
        if (large != null){
            created.add( getFinalIconPath(large) );
        }
    }
    
    private String getFinalIconPath(String iconPath){
        String dotCodeNameBase = getCodeNameBase();
        String codeNameBase = dotCodeNameBase.replace('.', '/'); // NOI18N
        
        File icon = new File(iconPath);
        String name = icon.getName();
        
        return codeNameBase + "/" + BaseHelper.RESOURCES + "/" + name;
    }
    
    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(ComponentFinalVisualPanel.class, key, args);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        createdFilesValueS = new javax.swing.JScrollPane();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValueS = new javax.swing.JScrollPane();
        modifiedFilesValue = new javax.swing.JTextArea();
        projectName = new javax.swing.JLabel();
        projectNameValue = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        createdFiles.setLabelFor(createdFilesValue);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/vmd/componentssupport/ui/wizard/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, bundle.getString("LBL_F_CreatedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);
        createdFiles.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSN_F_CreatedFiles")); // NOI18N
        createdFiles.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_CreatedFiles")); // NOI18N

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, bundle.getString("LBL_F_ModifiedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);
        modifiedFiles.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSN_F_ModifiedFiles")); // NOI18N
        modifiedFiles.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_ModifiedFiles")); // NOI18N

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_CreatedFiles")); // NOI18N
        createdFilesValue.setBorder(null);
        createdFilesValueS.setViewportView(createdFilesValue);
        createdFilesValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSN_F_CreatedFiles")); // NOI18N
        createdFilesValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_CreatedFiles")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFilesValueS, gridBagConstraints);

        modifiedFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setEditable(false);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_ModifiedFiles")); // NOI18N
        modifiedFilesValue.setBorder(null);
        modifiedFilesValueS.setViewportView(modifiedFilesValue);
        modifiedFilesValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSN_F_ModifiedFiles")); // NOI18N
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_ModifiedFiles")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(modifiedFilesValueS, gridBagConstraints);

        projectName.setLabelFor(projectNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(projectName, bundle.getString("LBL_F_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(projectName, gridBagConstraints);
        projectName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSN_F_ProjectName")); // NOI18N
        projectName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_ProjectName")); // NOI18N

        projectNameValue.setEditable(false);
        projectNameValue.setToolTipText(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_F_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(projectNameValue, gridBagConstraints);
        projectNameValue.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSN_ProjectName")); // NOI18N
        projectNameValue.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentFinalVisualPanel.class, "ACSD_ProjectName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JScrollPane createdFilesValueS;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JScrollPane modifiedFilesValueS;
    private javax.swing.JLabel projectName;
    private javax.swing.JTextField projectNameValue;
    // End of variables declaration//GEN-END:variables
    
    private WizardDescriptor mySettings;
    private ComponentFinalWizardPanel myPanel;
    
}
