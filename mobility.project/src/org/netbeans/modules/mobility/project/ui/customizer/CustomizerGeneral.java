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

package org.netbeans.modules.mobility.project.ui.customizer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.CyclicDependencyWarningPanel;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk, Adam Sotona
 */
public class CustomizerGeneral extends JPanel implements CustomizerPanel, ActionListener {
    
    private ProjectProperties props;
    
    /** Creates new form CustomizerCompile */
    public CustomizerGeneral() {
        initComponents();
        initAccessibility();
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        this.props = props;
        final VisualPropertySupport vps = VisualPropertySupport.getDefault(props);
        
        vps.register(jTextFieldDisplayName, J2MEProjectProperties.J2ME_PROJECT_NAME);
        vps.register(rebuildCheckBox, DefaultPropertiesDescriptor.NO_DEPENDENCIES);
        vps.register(jTextFieldSrcRoot, DefaultPropertiesDescriptor.SRC_DIR);
        vps.register(jTextFieldAppVersion, DefaultPropertiesDescriptor.APP_VERSION_NUMBER);
        vps.register(jSpinnerCounter, DefaultPropertiesDescriptor.APP_VERSION_COUNTER);
        vps.register(jCheckBoxAutoIncrement, DefaultPropertiesDescriptor.APP_VERSION_AUTOINCREMENT);
        vps.register(jCheckBoxUsePreprocessor, DefaultPropertiesDescriptor.USE_PREPROCESSOR);
        jCheckBoxAutoIncrement.addActionListener(this);
        actionPerformed(null);
        
        final FileObject fo = props.getProjectDirectory();
        File f = null;
        if (fo != null)
            f = FileUtil.toFile(fo);
        jTextFieldProjectFolder.setText((f != null) ? f.getAbsolutePath() : ""); //NOI18N
        
        final DefaultListModel lm = new DefaultListModel();
        for( String st : getSortedSubprojectsList() ) {
            lm.addElement( st );
        }
        projectList.setModel(lm);
    }
    
    public void actionPerformed(ActionEvent e) {
        boolean auto = jCheckBoxAutoIncrement.isSelected();
        jSpinnerCounter.setEnabled(auto);
        jTextFieldAppVersion.setEditable(!auto);
    }
   
    List<String> getSortedSubprojectsList() {
        final ArrayList<Project> subprojects = new ArrayList<Project>( 5 );
        
        addSubprojects(DefaultPropertiesDescriptor.LIBS_CLASSPATH, subprojects);
        final ProjectConfiguration cfg[] = props.getConfigurations();
        for (int i= 0; i<cfg.length; i++) {
            addSubprojects("configs." + cfg[i].getDisplayName() + '.' + DefaultPropertiesDescriptor.LIBS_CLASSPATH, subprojects);
        }
        
        // Replace projects in the list with formated names
        final ArrayList<String> strSubprojects = new ArrayList<String>( subprojects.size() );
        for ( int i = 0; i < subprojects.size(); i++ ) {
            final Project p = subprojects.get( i );
            strSubprojects.add( ProjectUtils.getInformation(p).getDisplayName());
        }
        
        // Sort the list
        Collections.sort( strSubprojects, Collator.getInstance() );
        
        return strSubprojects;
    }

        
    private void addSubprojects(final String cpProperty, final List<Project> result) {
        final List<VisualClassPathItem> l = (List<VisualClassPathItem>)props.get(cpProperty);
        if (l != null) {
            for ( final VisualClassPathItem vcpi : l ) {
                if (VisualClassPathItem.TYPE_ARTIFACT == vcpi.getType()) {
                    final AntArtifact antArtifact = (AntArtifact)vcpi.getElement();
                    if (antArtifact == null)
                        continue;
                    final Project sp = antArtifact.getProject();
                    if (!result.contains(sp)) {
                        result.add(sp);
                        addSubprojects(sp, result);
                    }
                }
            }
        }
    }
    
    /** Gets all subprojects recursively
     */
    private void addSubprojects( final Project p, final List<Project> result ) {
        
        final SubprojectProvider spp = p.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Project sp: spp.getSubprojects()) {
            if (sp.getProjectDirectory().equals(props.getProjectDirectory())) {
                CyclicDependencyWarningPanel.showWarning(ProjectUtils.getInformation(sp).getDisplayName());
                return;
            }
            if ( !result.contains( sp ) ) {
                result.add( sp );
                addSubprojects( sp, result );
            }
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

        jLabel1 = new javax.swing.JLabel();
        jTextFieldDisplayName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldProjectFolder = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldSrcRoot = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldAppVersion = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jSpinnerCounter = new javax.swing.JSpinner();
        jCheckBoxAutoIncrement = new javax.swing.JCheckBox();
        jCheckBoxUsePreprocessor = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        rebuildCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jTextFieldDisplayName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustGeneral_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral_ProjectName")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_ProjectName")); // NOI18N

        jTextFieldDisplayName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldDisplayName, gridBagConstraints);
        jTextFieldDisplayName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral_PrjName")); // NOI18N
        jTextFieldDisplayName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_PrjName")); // NOI18N

        jLabel3.setLabelFor(jTextFieldProjectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustomizeGeneral_ProjectFolder")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustomizeGeneral_ProjectFolder")); // NOI18N

        jTextFieldProjectFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldProjectFolder, gridBagConstraints);
        jTextFieldProjectFolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral_PrjFolder")); // NOI18N
        jTextFieldProjectFolder.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_PrjFolder")); // NOI18N

        jLabel4.setLabelFor(jTextFieldSrcRoot);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_SrcDir")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustomizeGeneral_SrcDir")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustomizeGeneral_SrcDir")); // NOI18N

        jTextFieldSrcRoot.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldSrcRoot, gridBagConstraints);
        jTextFieldSrcRoot.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral_PrjSources")); // NOI18N
        jTextFieldSrcRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_PrjSources")); // NOI18N

        jLabel5.setLabelFor(jTextFieldAppVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustMain_AppVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustMain_AppVersion")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustMain_AppVersion")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldAppVersion, gridBagConstraints);
        jTextFieldAppVersion.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_AppVersionNumber")); // NOI18N
        jTextFieldAppVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_AppVersionNumber")); // NOI18N

        jLabel6.setLabelFor(jSpinnerCounter);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustMain_AppCounter")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustMain_AppCounter")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustMain_AppCounter")); // NOI18N

        jSpinnerCounter.setEditor(new JSpinner.NumberEditor(jSpinnerCounter, "#0"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jSpinnerCounter, gridBagConstraints);
        jSpinnerCounter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_AppVersionCounter")); // NOI18N
        jSpinnerCounter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_AppVersionCounter")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxAutoIncrement, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustMain_AutoIncrement")); // NOI18N
        jCheckBoxAutoIncrement.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxAutoIncrement.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jCheckBoxAutoIncrement, gridBagConstraints);
        jCheckBoxAutoIncrement.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustMain_AutoIncrement")); // NOI18N
        jCheckBoxAutoIncrement.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustMain_AutoIncrement")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxUsePreprocessor, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_UsePreprocessor")); // NOI18N
        jCheckBoxUsePreprocessor.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBoxUsePreprocessor.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jCheckBoxUsePreprocessor, gridBagConstraints);
        jCheckBoxUsePreprocessor.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustomizeGeneral_UsePreprocessor")); // NOI18N
        jCheckBoxUsePreprocessor.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustomizeGeneral_UsePreprocessor")); // NOI18N

        jLabel2.setLabelFor(projectList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustGeneral_RequiredProjects")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral_RequiredProjects")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_RequiredProjects")); // NOI18N

        jScrollPane1.setViewportView(projectList);
        projectList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_Projects")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rebuildCheckBox, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustGeneral_RebuildProjects")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(rebuildCheckBox, gridBagConstraints);
        rebuildCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral_RebuildProjects")); // NOI18N
        rebuildCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_RebuildProjects")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerGeneral.class, "ACSN_CustGeneral"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxAutoIncrement;
    private javax.swing.JCheckBox jCheckBoxUsePreprocessor;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinnerCounter;
    private javax.swing.JTextField jTextFieldAppVersion;
    private javax.swing.JTextField jTextFieldDisplayName;
    private javax.swing.JTextField jTextFieldProjectFolder;
    private javax.swing.JTextField jTextFieldSrcRoot;
    private javax.swing.JList projectList;
    private javax.swing.JCheckBox rebuildCheckBox;
    // End of variables declaration//GEN-END:variables
    
    
    // Private methods for classpath data manipulation -------------------------
    
}
