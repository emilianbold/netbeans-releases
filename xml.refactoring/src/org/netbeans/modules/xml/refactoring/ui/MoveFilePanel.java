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

package org.netbeans.modules.xml.refactoring.ui;

import org.netbeans.modules.xml.refactoring.ui.views.PackageView;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;

/**
 * Asks where to move a class to.
 * @author Jan Becicka, Jesse Glick
 */
public class MoveFilePanel extends JPanel implements ActionListener, DocumentListener,CustomRefactoringPanel {
  
    private static final ListCellRenderer GROUP_CELL_RENDERER = new GroupCellRenderer();
    private static final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    
    private Project project;
    private ChangeListener parent;
    private FileObject fo;
    private SourceGroup[] groups;
    private String startPackage;
    
    public MoveFilePanel(final ChangeListener parent, String startPackage, String headLine, FileObject f) {
        this.fo = f;
        this.parent = parent;
        initComponents();
        setCombosEnabled(true);
        labelHeadLine.setText(headLine);
        rootComboBox.setRenderer(GROUP_CELL_RENDERER);
        packageComboBox.setRenderer(PackageView.listRenderer());
        projectsComboBox.setRenderer( PROJECT_CELL_RENDERER );
                
        rootComboBox.addActionListener( this );
        packageComboBox.addActionListener( this );
        projectsComboBox.addActionListener( this );
        
        Object textField = packageComboBox.getEditor().getEditorComponent();
        if (textField instanceof JTextField) {
            ((JTextField) textField).getDocument().addDocumentListener(this); 
        }
        
        project = fo != null ? FileOwnerQuery.getOwner(fo):OpenProjects.getDefault().getOpenProjects()[0];
        this.startPackage = startPackage;
        
    }
           
    private boolean initialized = false;
    public void initialize() {
        if (initialized)
            return ;
        //put initialization code here
        initValues(startPackage);
        initialized = true;
    }
    
    public void initValues(String preselectedFolder ) {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
/* # 155199
        List<Project> op = Arrays.asList(openProjects);
        java.util.List projectRoots = new java.util.ArrayList();
        SubprojectProvider provider = (SubprojectProvider)project.getLookup().lookup(SubprojectProvider.class);
        Set refProjects= provider.getSubprojects();
        Iterator it = refProjects.iterator();
        projectRoots.add(project);
        
        while(it.hasNext()){
             Object o = it.next();
             Project refPrj = (Project) o;
             if(op.contains(refPrj))
                 projectRoots.add(refPrj);
        }
        Project[] roots = (Project[])projectRoots.toArray(new Project[projectRoots.size()]);
*/
        Arrays.sort( openProjects, new ProjectByDisplayNameComparator());
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );                
        projectsComboBox.setSelectedItem( project );
        
        updateRoots();
        updatePackages(); 
        if (preselectedFolder != null) {
//          packageComboBox.setSelectedItem(preselectedFolder);
        }
        // Determine the extension
    }
    
    public void requestFocus() {
        setCombosEnabled(true);
        packageComboBox.requestFocus();
    }
    
    public FileObject getRootFolder() {
        return ((SourceGroup) rootComboBox.getSelectedItem()).getRootFolder();
    }
    
    public String getPackageName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName; // NOI18N
    }
    
    private void fireChange() {
        parent.stateChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelProject = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        labelLocation = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        labelPackage = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        bottomPanel = new javax.swing.JPanel();
        labelHeadLine = new javax.swing.JLabel();
        updateReferencesCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        labelProject.setLabelFor(projectsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelProject, org.openide.util.NbBundle.getMessage(MoveFilePanel.class, "LBL_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelProject, gridBagConstraints);
        labelProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveFilePanel.class, "DSC_Project")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectsComboBox, gridBagConstraints);
        projectsComboBox.getAccessibleContext().setAccessibleDescription("null");

        labelLocation.setLabelFor(rootComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelLocation, org.openide.util.NbBundle.getMessage(MoveFilePanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelLocation, gridBagConstraints);
        labelLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MoveFilePanel.class, "DSC_Location")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(rootComboBox, gridBagConstraints);
        rootComboBox.getAccessibleContext().setAccessibleDescription("null");

        labelPackage.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(labelPackage, org.openide.util.NbBundle.getMessage(MoveFilePanel.class, "LBL_ToPackage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(labelPackage, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(packageComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanel, gridBagConstraints);

        labelHeadLine.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 6, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(labelHeadLine, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(updateReferencesCheckBox, org.openide.util.NbBundle.getBundle(MoveFilePanel.class).getString("LBL_MoveWithoutReferences")); // NOI18N
        updateReferencesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 0, 4));
        updateReferencesCheckBox.setMargin(new java.awt.Insets(2, 2, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(updateReferencesCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JPanel bottomPanel;
    private javax.swing.JLabel labelHeadLine;
    private javax.swing.JLabel labelLocation;
    private javax.swing.JLabel labelPackage;
    private javax.swing.JLabel labelProject;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JCheckBox updateReferencesCheckBox;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    public void actionPerformed(ActionEvent e) {
        if (projectsComboBox == e.getSource()) {
            project = (Project) projectsComboBox.getSelectedItem();
            updateRoots();
            updatePackages();
        } else 
        if ( rootComboBox == e.getSource() ) {            
            updatePackages();
        }
        else if ( packageComboBox == e.getSource() ) {
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(DocumentEvent e) {                
        fireChange();        
    }    
    
    public void insertUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    public void removeUpdate(DocumentEvent e) {
        fireChange();        
    }
    
    // Private methods ---------------------------------------------------------
        
    private void updatePackages() {
        SourceGroup g = (SourceGroup) rootComboBox.getSelectedItem();
        packageComboBox.setModel(PackageView.createListView(g));
    }
    
    void setCombosEnabled(boolean enabled) {
        packageComboBox.setEnabled(enabled);
        rootComboBox.setEnabled(enabled);
        projectsComboBox.setEnabled(enabled);
        updateReferencesCheckBox.setVisible(!enabled);
    }

    public boolean isUpdateReferences() {
        if (updateReferencesCheckBox.isVisible() && updateReferencesCheckBox.isSelected())
            return false;
        return true;
    }
    
    
    private void updateRoots() {
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<String> sourceGroupList = SharedUtils.getSourceGroups();
        for(String type: sourceGroupList){
            SourceGroup[] srcGrps = sources.getSourceGroups(type);
            if(srcGrps != null){
                for(SourceGroup srcGrp : srcGrps )
                    result.add(srcGrp);
            }
        }
        //groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        groups = result.toArray(new SourceGroup[result.size()]);
        int preselectedItem = 0;
        for( int i = 0; i < groups.length; i++ ) {
            if (fo!=null) {
                try {
                    if (groups[i].contains(fo)) {
                        preselectedItem = i;
                    }
                } catch (IllegalArgumentException e) {
                    // XXX this is a poor abuse of exception handling
                }
            }
        }
                
        // Setup comboboxes 
        rootComboBox.setModel(new DefaultComboBoxModel(groups));
        rootComboBox.setSelectedIndex(preselectedItem);
    }
    
    private abstract static class BaseCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public BaseCellRenderer () {
            setOpaque(true);
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    }
    
    /** Groups combo renderer, used also in CopyClassPanel */
    static class GroupCellRenderer extends BaseCellRenderer {
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            SourceGroup g = (SourceGroup) value;
            setText(g.getDisplayName());
            setIcon(g.getIcon(false));
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    
    /** Projects combo renderer, used also in CopyClassPanel */
    static class ProjectCellRenderer extends BaseCellRenderer {
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if ( value != null ) {
                ProjectInformation pi = ProjectUtils.getInformation((Project)value);
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
    }
    //Copy/pasted from OpenProjectList
    //remove this code as soon as #68827 is fixed.
    static class ProjectByDisplayNameComparator implements Comparator {
        
        private static Comparator COLLATOR = Collator.getInstance();
        
        public int compare(Object o1, Object o2) {
            
            if ( !( o1 instanceof Project ) ) {
                return 1;
            }
            if ( !( o2 instanceof Project ) ) {
                return -1;
            }
            
            Project p1 = (Project)o1;
            Project p2 = (Project)o2;
            
            return COLLATOR.compare(ProjectUtils.getInformation(p1).getDisplayName(), ProjectUtils.getInformation(p2).getDisplayName());
        }
    }    

    public Component getComponent() {
        return this;
    }
}
