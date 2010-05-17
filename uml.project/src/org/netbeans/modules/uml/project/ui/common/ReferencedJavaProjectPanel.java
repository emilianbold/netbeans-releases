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

/*
 * ReferencedJavaProjectPanel.java
 *
 * Created on September 12, 2006, 12:45 PM
 */

package org.netbeans.modules.uml.project.ui.common;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.modules.uml.project.ui.wizards.PanelConfigureProject;
import org.netbeans.modules.uml.project.ui.wizards.PanelOptionsVisual;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI.JavaSourceRootsModel;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.modules.uml.project.ui.wizards.NewUMLProjectWizardIterator;


/**
 *
 * @author Mike Frisino
 * @author Craig Conover, craig.conover@sun.com
 */
public class ReferencedJavaProjectPanel extends javax.swing.JPanel
    implements PropertyChangeListener, TableModelListener
{
    public ReferencedJavaProjectPanel(PanelConfigureProject panel)
    {
        this(panel, NewUMLProjectWizardIterator.TYPE_UML, null, null);
    }
    
    public ReferencedJavaProjectPanel(PanelConfigureProject panel, int type)
    {
        this(panel, type, null, null);
    }
    
    // TODO - I originally hoped to eliminate this constructor
    // and have the same code path for both wizard and customizer.
    // But as craig said, we have "visual" reuse, but not "model" reuse
    // It would be nice to unify, but if its not broken don't fix it
    // unless time permits.
    
    // this is the version invoked by the wizard.
    public ReferencedJavaProjectPanel(
        PanelConfigureProject projectConfigurePanel,
        int type, 
        Project currRefProj, 
        JavaSourceRootsUI.JavaSourceRootsModel rootsModel)
    {
        panel = projectConfigurePanel;
        isWizard = true;
        uiProperties = null;
        initComponents();
        
        if (rootsModel == null)
            sourceRoots.setModel(JavaSourceRootsUI.createEmptyModel());

        else
            sourceRoots.setModel(rootsModel);
        
        sourceRootsScrollPane.getViewport().setBackground(
            sourceRoots.getBackground());
        
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        sourceRoots.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        initValues(currRefProj);
    }
    
    
    // this is the version invoked by the customizer.
    public ReferencedJavaProjectPanel(UMLProjectProperties uiProperties)
    {
        this.uiProperties = uiProperties;
        initComponents();
        
        if (uiProperties == null )
        {
            // this means it was called from wizard before props are available
            isWizard = true;
            sourceRoots.setModel(JavaSourceRootsUI.createEmptyModel());
        }
        
        else
        {
            // this mean we are called from customizer
            isWizard = false;
            javaRefModel = uiProperties.referencedJavaProjectModel;
            javaRefSrcsModel = uiProperties.referencedJavaSourceRootsModel;
            setSelectedProject(javaRefModel.getProject());
            sourceRoots.setModel(javaRefSrcsModel);
        }
        
        sourceRoots.getModel().addTableModelListener(this);
        
        sourceRootsScrollPane.getViewport().setBackground(
            sourceRoots.getBackground());
        
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        
        // populate the combobox with the reference java project and 
        // disable the combbobox for read-only
        ProjectCellRenderer projectCellRenderer = new ProjectCellRenderer();
        projectsComboBox.setRenderer(projectCellRenderer);
        
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( 
            new Project[] {javaRefModel.getProject()});
        
        projectsComboBox.setModel(projectsModel);
        projectsComboBox.setEnabled(false);
    }
    
    private void initValues(Project prj)
    {
        ProjectCellRenderer projectCellRenderer = new ProjectCellRenderer();
        projectsComboBox.setRenderer(projectCellRenderer);
        
        // Populate the combo box with list of projects
        Project[] openProjects = ProjectUtil.getOpenJavaProjects();
        
        Project[] selectedProjects = 
            ProjectUtil.getSelectedProjects(Project.class);
        
        DefaultComboBoxModel projectsModel = 
            new DefaultComboBoxModel(openProjects);
        
        projectsComboBox.setModel(projectsModel);
        
        Project proj = null;
        if (prj!=null)
            proj = prj;
        
        else if (openProjects.length > 0 && selectedProjects.length > 0)
            proj = selectedProjects[0];
        
        this.selectProject(proj);
    }
    
    
    private void selectProject(Project prj)
    {
        if (prj != null)
        {
            projectsComboBox.setSelectedItem(prj);
        }
        
        else
        {
            if (ProjectUtil.getOpenJavaProjects().length > 0)
                projectsComboBox.setSelectedIndex(0);
        }
        
        updateState();
    }
    
    
    public void tableChanged(TableModelEvent e)
    {
        boolean enableButton = isAtLeastOneSrcFolderSelected();
        
        firePropertyChange(SOURCE_GROUP_CHANGED_PROP, 
            "dontCareOldValue", Boolean.valueOf(enableButton)); // NOI18N
    }
    
    
    // Check to see if at one src folder is selected; return true if at least one is selected.
    // if none is selected, return false
    private boolean isAtLeastOneSrcFolderSelected()
    {
        boolean selected = false;
        TableModel tModel = sourceRoots.getModel();
        
        if ( tModel !=  null)
        {
            int rowCount = tModel.getRowCount();
            for (int i = 0; i < rowCount; i++ )
            {
                selected  = selected || ((Boolean)tModel.getValueAt(
                    i,JavaSourceRootsUI.COL_INCLUDE_FLAG));
                
                if (selected)
                {  // if there's at least one src folder selected, exit the loop
                    break;
                }
            }
        }
        
        return selected;
    }
    
    
    public void propertyChange(PropertyChangeEvent event)
    {
        
        // Debug.out.println("MCF table propertyChange - " + event);
        /* MCF - change this to whatever prop we care about re bulletproofing
        if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
                String newProjectName = NewUMLProjectWizardIterator.getPackageName((String) event.getNewValue());
                this.mainClassTextField.setText (MessageFormat.format(
                        NbBundle.getMessage (ReferencedJavaProjectPanel.class,"TXT_ClassName"), new Object[] {newProjectName}
                ));
        }
         */
        
        if (PanelOptionsVisual.MODE_CHANGED_PROP.equals(event.getPropertyName()))
        {
            if (event.getNewValue().equals(UMLProject.PROJECT_MODE_IMPL_STR))
            {
                if (ProjectUtil.getOpenJavaProjects().length > 0)
                {
                    initValues(null);

                    // must be in wizard as well for the project combobox
                    // to be enabled. it should disabled in customizer
                    projectsComboBox.setEnabled(isWizard);
                }
            }

            else if (event.getNewValue()
                .equals(UMLProject.PROJECT_MODE_DESIGN_STR))
            {
                projectsComboBox.setSelectedItem(null);
                projectsComboBox.setEnabled(false);
            }
        }

        else if (ASSOCIATED_JAVA_PROJ_PROP.equals(event.getPropertyName()))
        {
            updateSourceRootsTable();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        modelTypeButtonGroup = new javax.swing.ButtonGroup();
        sourceRootsPanel = new javax.swing.JPanel();
        sourceRootsLabel = new javax.swing.JLabel();
        sourceRootsScrollPane = new javax.swing.JScrollPane();
        sourceRoots = new javax.swing.JTable();
        projectsComboBox = new javax.swing.JComboBox();
        javaProjectLabel = new javax.swing.JLabel();

        sourceRootsLabel.setLabelFor(sourceRoots);
        org.openide.awt.Mnemonics.setLocalizedText(sourceRootsLabel, bundle.getString("LBL_SourceGroupsLabel")); // NOI18N
        sourceRootsLabel.getAccessibleContext().setAccessibleName("");
        sourceRootsLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_SourceGroupsLabel")); // NOI18N

        sourceRootsScrollPane.setBorder(null);
        sourceRootsScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {new Boolean(true), null, null},
                {new Boolean(true), null, null}
            },
            new String [] {
                "Reverse Engineer", "Package Folder", "Package Folder Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sourceRootsScrollPane.setViewportView(sourceRoots);
        sourceRoots.getAccessibleContext().setAccessibleName("");
        sourceRoots.getAccessibleContext().setAccessibleDescription("");

        sourceRootsScrollPane.getAccessibleContext().setAccessibleName("");
        sourceRootsScrollPane.getAccessibleContext().setAccessibleDescription("");

        projectsComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                projectsComboBoxItemStateChanged(evt);
            }
        });

        projectsComboBox.getAccessibleContext().setAccessibleName("");
        projectsComboBox.getAccessibleContext().setAccessibleDescription("");

        javaProjectLabel.setLabelFor(projectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(javaProjectLabel, bundle.getString("LBL_ReferencedJavaProjectLabel")); // NOI18N
        javaProjectLabel.getAccessibleContext().setAccessibleName("");
        javaProjectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencedJavaProjectPanel.class, "ACSD_ReferencedJavaProjectLabel")); // NOI18N

        org.jdesktop.layout.GroupLayout sourceRootsPanelLayout = new org.jdesktop.layout.GroupLayout(sourceRootsPanel);
        sourceRootsPanel.setLayout(sourceRootsPanelLayout);
        sourceRootsPanelLayout.setHorizontalGroup(
            sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, sourceRootsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sourceRootsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, sourceRootsPanelLayout.createSequentialGroup()
                        .add(javaProjectLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(projectsComboBox, 0, 444, Short.MAX_VALUE))
                    .add(sourceRootsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE))
                .addContainerGap())
        );
        sourceRootsPanelLayout.setVerticalGroup(
            sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceRootsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(sourceRootsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(javaProjectLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceRootsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceRootsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addContainerGap())
        );
        sourceRootsPanel.getAccessibleContext().setAccessibleName("");
        sourceRootsPanel.getAccessibleContext().setAccessibleDescription("");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceRootsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(sourceRootsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void projectsComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_projectsComboBoxItemStateChanged
    {//GEN-HEADEREND:event_projectsComboBoxItemStateChanged
        setSelectedProject((Project)projectsComboBox.getSelectedItem());
        updateSourceRootsTable();
    }//GEN-LAST:event_projectsComboBoxItemStateChanged
    
    
    // only called by wizard
    public Project getSelectedProject()
    {
        return javaSrcProj;
    }
    
    
    // only called by wizard
    public void setSelectedProject(Project prj)
    {
        // MCF - i had to change this because Trey was not setting the
        // javaSrcProj before firing the event. And the wizard
        // listener was checking calling isValid, which was calling
        // getSelectedProject ... I hope I did not break any contract.
        Project oldVal = javaSrcProj;
        mIsImplementationMode = false;
        javaSrcProj = prj;
        firePropertyChange(ASSOCIATED_JAVA_PROJ_PROP, oldVal, javaSrcProj);
    }

    
    private void updateState()
    {
        if (projectsComboBox.getSelectedItem() == null)
        {
            sourceRoots.setModel(JavaSourceRootsUI.createEmptyModel());
            setSelectedProject(null);
            return;
        }
        
        setSelectedProject((Project)projectsComboBox.getSelectedItem());
        updateSourceRootsTable();
    }

    private void updateSourceRootsTable()
    {
        // conditionalized for wizard vs customizer
        if (uiProperties == null)
        {
            sourceRoots.setModel(
                JavaSourceRootsUI.createModel(getSelectedProject()));
            
            sourceRoots.getModel().addTableModelListener(this);
        }
        
        else
        {
            uiProperties.referencedJavaProjectModel =
                ReferencedJavaProjectModel.createMounted(
                    uiProperties.REFERENCED_JAVA_PROJECT,
                    getSelectedProject());
            
            uiProperties.referencedJavaSourceRootsModel =
                javaRefSrcsModel = JavaSourceRootsUI
                    .createModel(getSelectedProject());
            
            sourceRoots.setModel(
                uiProperties.referencedJavaSourceRootsModel);
            
            sourceRoots.getModel().addTableModelListener(this);
//            firePropertyChange(JAVA_PROJECT_CHANGED_PROP, 
//                oldVal, getSelectedProject());
        }
    }
    
    
    /*
     * returns a map where the key is a SourceGroup and the value is a Boolean
     * indicating whether it was a selected or deselected SourceGroup
     *
     */
    public JavaSourceRootsUI.JavaSourceRootsModel getJavaSourceRootsModel()
    {
        return (JavaSourceRootsUI.JavaSourceRootsModel)sourceRoots.getModel();
    }
    
    
    public boolean valid(WizardDescriptor settings)
    {
        // conditionalized for wizard vs customizer
        if (uiProperties == null)
        {
            if (getSelectedProject() == null)
            {
                settings.putProperty(
                    NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE,
                    NbBundle.getMessage(ReferencedJavaProjectPanel.class,
                    "ReverseEngineer_Warning")); //NOI18N

                return false;
            }

            else if (sourceRoots.getModel() == null || 
                !isAtLeastOneSrcFolderSelected())
            {
                settings.putProperty(
                    NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE,
                    NbBundle.getMessage(ReferencedJavaProjectPanel.class,
                    "SourceGroup_Warning")); //NOI18N

                return false;
            }
            
            settings.putProperty(
                NewUMLProjectWizardIterator.PROP_WIZARD_ERROR_MESSAGE, ""); // NOI18N

            return true;
        }
        
        return true;
    }

    public void itemStateChanged(ItemEvent e)
    {
    }
    
    private static class ProjectCellRenderer extends JLabel 
        implements ListCellRenderer
    {
        public ProjectCellRenderer()
        {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            
            if ( value instanceof Project )
            {
                ProjectInformation pi = 
                    ProjectUtils.getInformation((Project)value);
                
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }
            
            else
            {
                setText( value == null ? " " : value.toString() ); // NOI18N
                setIcon( null );
            }
            
            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel javaProjectLabel;
    private javax.swing.ButtonGroup modelTypeButtonGroup;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JLabel sourceRootsLabel;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JScrollPane sourceRootsScrollPane;
    // End of variables declaration//GEN-END:variables
    
    // Some of these would not be needed if we had unified code path.
    private Project javaSrcProj;
    private String javaSrcProjName;
    private boolean valid;
    private boolean isWizard = false;
    private final UMLProjectProperties uiProperties;
    private ReferencedJavaProjectModel javaRefModel;
    private JavaSourceRootsModel javaRefSrcsModel;
    private PanelConfigureProject panel;
    public static boolean mIsImplementationMode = true;

    private java.util.ResourceBundle bundle = 
        NbBundle.getBundle(ReferencedJavaProjectPanel.class);

    
    public static final String ASSOCIATED_JAVA_PROJ_PROP = "ReferencedProjectProperty"; //NOI18N
    public final static String SOURCE_GROUP_CHANGED_PROP = "SOURCE_GROUP_CHANGED";  //NOI18N
    public final static String JAVA_PROJECT_CHANGED_PROP = "JAVA_PROJ_CHANGED"; // NOI18N

}
