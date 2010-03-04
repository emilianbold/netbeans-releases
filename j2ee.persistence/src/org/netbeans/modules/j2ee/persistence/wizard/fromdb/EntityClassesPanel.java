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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class EntityClassesPanel extends javax.swing.JPanel {

    private final static Logger LOGGER = Logger.getLogger(EntityClassesPanel.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private JTextComponent packageComboBoxEditor;

    private PersistenceGenerator persistenceGen;
    private Project project;
    private boolean cmp;
    private String tableSourceName; //either Datasource or a connection

    private SelectedTables selectedTables;
    private final boolean puRequired;


    private EntityClassesPanel(boolean puRequired) {
        this.puRequired = puRequired;

        initComponents();

        classNamesTable.getParent().setBackground(classNamesTable.getBackground());
        classNamesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N

        packageComboBoxEditor = ((JTextComponent)packageComboBox.getEditor().getEditorComponent());
        Document packageComboBoxDocument = packageComboBoxEditor.getDocument();
        packageComboBoxDocument.addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                packageChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                packageChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                packageChanged();
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void initialize(PersistenceGenerator persistenceGen, Project project, boolean cmp, FileObject targetFolder) {
        this.persistenceGen = persistenceGen;
        this.project = project;
        this.cmp = cmp;

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        SourceGroupUISupport.connect(locationComboBox, sourceGroups);

        packageComboBox.setRenderer(PackageView.listRenderer());

        updatePackageComboBox();

        if (targetFolder != null) {
            // set default source group and package cf. targetFolder
            SourceGroup targetSourceGroup = SourceGroups.getFolderSourceGroup(sourceGroups, targetFolder);
            if (targetSourceGroup != null) {
                locationComboBox.setSelectedItem(targetSourceGroup);
                String targetPackage = SourceGroups.getPackageForFolder(targetSourceGroup, targetFolder);
                if (targetPackage != null) {
                    packageComboBoxEditor.setText(targetPackage);
                }
            }
        }

        if (!cmp) {
            // change text of named query/finder checkbox
            Mnemonics.setLocalizedText(generateFinderMethodsCheckBox,
                    NbBundle.getMessage(EntityClassesPanel.class, "TXT_GenerateNamedQueryAnnotations"));
            // hide local interface checkbox
            cmpFieldsInInterfaceCheckBox.setVisible(false);
        }

        if (cmp) {
            classNamesLabel.setVisible(false);
            classNamesScrollPane.setVisible(false);
            spacerPanel.setVisible(false);
            
            setName(org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_EntityBeansLocation"));

            Mnemonics.setLocalizedText(specifyNamesLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_SpecifyBeansLocation"));
        }

        updatePersistenceUnitButton(true);
    }

    public void update(TableClosure tableClosure, String tableSourceName) {
        try {
            if (selectedTables == null) {
                selectedTables = new SelectedTables(persistenceGen, tableClosure, getLocationValue(), getPackageName());
                selectedTables.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent event) {
                        changeSupport.fireChange();
                    }
                });
            } else {
                selectedTables.setTableClosureAndTargetFolder(tableClosure, getLocationValue(), getPackageName());
            }
//            selectedTables.ensureUniqueClassNames();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }

        TableUISupport.connectClassNames(classNamesTable, selectedTables);
        this.tableSourceName = tableSourceName;
    }

    public SelectedTables getSelectedTables() {
        return selectedTables;
    }

    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getPackageName() {
        return packageComboBoxEditor.getText();
    }

    public boolean getCmpFieldsInInterface() {
        return cmpFieldsInInterfaceCheckBox.isSelected();
    }

    public boolean getGenerateFinderMethods() {
        return generateFinderMethodsCheckBox.isSelected();
    }

    public boolean getCreatePersistenceUnit() {
        return createPUCheckbox.isVisible() && createPUCheckbox.isSelected();
    }

    private void locationChanged() {
        updatePackageComboBox();
        updateSelectedTables();
        changeSupport.fireChange();
    }

    private void packageChanged() {
        updateSelectedTables();
        changeSupport.fireChange();
    }

    private void updatePackageComboBox() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        if (sourceGroup != null) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            packageComboBox.setModel(model);
        }
    }

    private void updatePersistenceUnitButton(boolean initial) {
        String warning = " "; // NOI18N
        try{

            boolean showWarning = !cmp
                    && !ProviderUtil.persistenceExists(project);

            if(initial){
                createPUCheckbox.setVisible(showWarning);
                createPUCheckbox.setSelected(showWarning);
                createPUCheckbox.setEnabled(!puRequired);
            }


            if (showWarning && !createPUCheckbox.isSelected()) {
                warning = NbBundle.getMessage(EntityClassesPanel.class, "ERR_NoPersistenceUnit");
            }

        } catch (InvalidPersistenceXmlException ipx){
            createPUCheckbox.setVisible(false);
            warning = NbBundle.getMessage(EntityClassesPanel.class, "ERR_InvalidPersistenceUnit", ipx.getPath());
        }

        if (warning.trim().length() > 0) {
            Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/persistence/ui/resources/warning.gif", false);
            createPUWarningLabel.setIcon(icon);
            createPUWarningLabel.setText(warning);
            createPUWarningLabel.setToolTipText(warning);
        } else {
            createPUWarningLabel.setIcon(null);
            createPUWarningLabel.setText(" ");
            createPUWarningLabel.setToolTipText(null);
            
        }
    }

    private void updateSelectedTables() {
        if (selectedTables != null) {
            try {
                selectedTables.setTargetFolder(getLocationValue(), getPackageName());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
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

        specifyNamesLabel = new javax.swing.JLabel();
        classNamesLabel = new javax.swing.JLabel();
        classNamesScrollPane = new javax.swing.JScrollPane();
        classNamesTable = new TableUISupport.ClassNamesTable();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        generateFinderMethodsCheckBox = new javax.swing.JCheckBox();
        cmpFieldsInInterfaceCheckBox = new javax.swing.JCheckBox();
        spacerPanel = new javax.swing.JPanel();
        createPUWarningLabel = new ShyLabel();
        createPUCheckbox = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_EntityClasses")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(specifyNamesLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_SpecifyEntityClassNames")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(classNamesLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_ClassNames")); // NOI18N

        classNamesScrollPane.setViewportView(classNamesTable);

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_Package")); // NOI18N

        packageComboBox.setEditable(true);

        generateFinderMethodsCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateFinderMethodsCheckBox, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "TXT_GenerateFinderMethods")); // NOI18N
        generateFinderMethodsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        cmpFieldsInInterfaceCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cmpFieldsInInterfaceCheckBox, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "TXT_AddFieldsToInterface")); // NOI18N
        cmpFieldsInInterfaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        org.jdesktop.layout.GroupLayout spacerPanelLayout = new org.jdesktop.layout.GroupLayout(spacerPanel);
        spacerPanel.setLayout(spacerPanelLayout);
        spacerPanelLayout.setHorizontalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 383, Short.MAX_VALUE)
        );
        spacerPanelLayout.setVerticalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 9, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(createPUWarningLabel, "  ");
        createPUWarningLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPUWarningLabel.setMaximumSize(new java.awt.Dimension(1000, 29));

        createPUCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createPUCheckbox, org.openide.util.NbBundle.getMessage(EntityClassesPanel.class, "LBL_CreatePersistenceUnit")); // NOI18N
        createPUCheckbox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        createPUCheckbox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createPUCheckboxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(specifyNamesLabel)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classNamesLabel)
                    .add(projectLabel)
                    .add(locationLabel)
                    .add(packageLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spacerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(packageComboBox, 0, 383, Short.MAX_VALUE)
                    .add(locationComboBox, 0, 383, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, projectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .add(classNamesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)))
            .add(cmpFieldsInInterfaceCheckBox)
            .add(generateFinderMethodsCheckBox)
            .add(layout.createSequentialGroup()
                .add(createPUCheckbox)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(createPUWarningLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(specifyNamesLabel)
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(classNamesLabel)
                    .add(classNamesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spacerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(locationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(locationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(packageLabel))
                .add(21, 21, 21)
                .add(generateFinderMethodsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmpFieldsInInterfaceCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(createPUCheckbox)
                .add(11, 11, 11)
                .add(createPUWarningLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed

    private void createPUCheckboxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createPUCheckboxItemStateChanged
        if(createPUCheckbox.isVisible() && createPUCheckbox.isSelected()){
        } else {
        }
        updatePersistenceUnitButton(false);
    }//GEN-LAST:event_createPUCheckboxItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classNamesLabel;
    private javax.swing.JScrollPane classNamesScrollPane;
    private javax.swing.JTable classNamesTable;
    private javax.swing.JCheckBox cmpFieldsInInterfaceCheckBox;
    private javax.swing.JCheckBox createPUCheckbox;
    private javax.swing.JLabel createPUWarningLabel;
    private javax.swing.JCheckBox generateFinderMethodsCheckBox;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JPanel spacerPanel;
    private javax.swing.JLabel specifyNamesLabel;
    // End of variables declaration//GEN-END:variables

    public static final class WizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

        private final ChangeSupport changeSupport = new ChangeSupport(this);

        private EntityClassesPanel component;
        private boolean componentInitialized;

        private WizardDescriptor wizardDescriptor;
        private Project project;
        private boolean cmp;
        private boolean puRequired;

        private List<Provider> providers;

        public WizardPanel(){
            this(false);
        }

        public WizardPanel(boolean persistenceUnitRequired){
            puRequired = persistenceUnitRequired;
        }
        
        @Override
        public EntityClassesPanel getComponent() {
            if (component == null) {
                component = new EntityClassesPanel(puRequired);
                component.addChangeListener(this);
            }
            return component;
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public HelpCtx getHelp() {
            if (cmp) {
                return new HelpCtx("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp." + EntityClassesPanel.class.getSimpleName()); // NOI18N
            } else {
                return new HelpCtx(EntityClassesPanel.class);
            }
        }

        @Override
        public void readSettings(Object settings) {
            wizardDescriptor = (WizardDescriptor)settings;
            
            RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);

            if (!componentInitialized) {
                componentInitialized = true;

                PersistenceGenerator persistenceGen = helper.getPersistenceGenerator();
                project = Templates.getProject(wizardDescriptor);
                cmp = RelatedCMPWizard.isCMP(wizardDescriptor);
                FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);

                getComponent().initialize(persistenceGen, project, cmp, targetFolder);
            }

            TableSource tableSource = helper.getTableSource();
            String tableSourceName = null;
            if (tableSource != null) {
                // the name of the table source is only relevant if the source
                // was a data source of connection, since it will be sent to the
                // persistence unit panel, which only deals with data sources
                // or connections
                TableSource.Type tableSourceType = tableSource.getType();
                if (tableSourceType == TableSource.Type.DATA_SOURCE || tableSourceType == TableSource.Type.CONNECTION) {
                    tableSourceName = tableSource.getName();
                }
            }

            getComponent().update(helper.getTableClosure(), tableSourceName);
        }

        @Override
        public boolean isValid() {
            SourceGroup sourceGroup = getComponent().getLocationValue();
            if (sourceGroup == null) {
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_JavaTargetChooser_SelectSourceGroup"));
                return false;
            }

            String packageName = getComponent().getPackageName();
            if (packageName.trim().equals("")) { // NOI18N
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_JavaTargetChooser_CantUseDefaultPackage"));
                return false;
            }

            if (!JavaIdentifiers.isValidPackageName(packageName)) {
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class,"ERR_JavaTargetChooser_InvalidPackage")); //NOI18N
                return false;
            }

            if (!SourceGroups.isFolderWritable(sourceGroup, packageName)) {
                setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_JavaTargetChooser_UnwritablePackage")); //NOI18N
                return false;
            }

            // issue 92192: need to check that we will have a persistence provider
            // available to add to the classpath while generating entity classes (unless
            // the classpath already contains one)
            ClassPath classPath = null;
            try {
                FileObject packageFO = SourceGroups.getFolderForPackage(sourceGroup, packageName, false);
                if (packageFO == null) {
                    packageFO = sourceGroup.getRootFolder();
                }
                classPath = ClassPath.getClassPath(packageFO, ClassPath.COMPILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
            if (classPath != null) {
                if (classPath.findResource("javax/persistence/EntityManager.class") == null) { // NOI18N
                    // initialize the provider list lazily
                    if (providers == null) {
                        providers = PersistenceLibrarySupport.getProvidersFromLibraries();
                    }
                    if (providers.size() == 0) {
                        setErrorMessage(NbBundle.getMessage(EntityClassesPanel.class, "ERR_NoJavaPersistenceAPI")); // NOI18N
                        return false;
                    }
                }
            } else {
                LOGGER.warning("Cannot get a classpath for package " + packageName + " in " + sourceGroup); // NOI18N
            }

            SelectedTables selectedTables = getComponent().getSelectedTables();
            // check for null needed since isValid() can be called when
            // EntityClassesPanel.update() has not been called yet, e.g. from within
            // EntityClassesPanel.initialize()
            if (selectedTables != null) {
                String problem = selectedTables.getFirstProblemDisplayName();
                if (problem != null) {
                    setErrorMessage(problem);
                    return false;
                }
            }

            setErrorMessage(" "); // NOI18N
            return true;
        }

        @Override
        public void storeSettings(Object settings) {
            RelatedCMPHelper helper = RelatedCMPWizard.getHelper(wizardDescriptor);

            helper.setSelectedTables(getComponent().getSelectedTables());
            helper.setLocation(getComponent().getLocationValue());
            helper.setPackageName(getComponent().getPackageName());
            helper.setCmpFieldsInInterface(getComponent().getCmpFieldsInInterface());
            helper.setGenerateFinderMethods(getComponent().getGenerateFinderMethods());
            helper.setCreatePU(getComponent().getCreatePersistenceUnit());
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            changeSupport.fireChange();
        }

        private void setErrorMessage(String errorMessage) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N
        }

        @Override
        public boolean isFinishPanel() {
            return true;
        }
    }

    /**
     * A crude attempt at a label which doesn't expand its parent.
     */
    private static final class ShyLabel extends JLabel {

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width = 0;
            return size;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension size = super.getMinimumSize();
            size.width = 0;
            return size;
        }
    }
}
