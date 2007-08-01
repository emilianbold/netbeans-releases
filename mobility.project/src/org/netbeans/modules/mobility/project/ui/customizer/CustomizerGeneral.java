/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        jTextFieldDisplayName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldDisplayName, gridBagConstraints);
        jTextFieldDisplayName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_PrjName")); // NOI18N

        jLabel3.setLabelFor(jTextFieldProjectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel3, gridBagConstraints);

        jTextFieldProjectFolder.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldProjectFolder, gridBagConstraints);
        jTextFieldProjectFolder.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_PrjFolder")); // NOI18N

        jLabel4.setLabelFor(jTextFieldSrcRoot);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustomizeGeneral_SrcDir")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel4, gridBagConstraints);

        jTextFieldSrcRoot.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldSrcRoot, gridBagConstraints);
        jTextFieldSrcRoot.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_PrjSources")); // NOI18N

        jLabel5.setLabelFor(jTextFieldAppVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustMain_AppVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jTextFieldAppVersion, gridBagConstraints);

        jLabel6.setLabelFor(jSpinnerCounter);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(CustomizerGeneral.class, "LBL_CustMain_AppCounter")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 12);
        add(jLabel6, gridBagConstraints);

        jSpinnerCounter.setEditor(new JSpinner.NumberEditor(jSpinnerCounter, "#0"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jSpinnerCounter, gridBagConstraints);

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
        rebuildCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerGeneral.class, "ACSD_CustGeneral_RebuildProjects")); // NOI18N
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
