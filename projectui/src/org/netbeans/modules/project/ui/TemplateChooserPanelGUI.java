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

package org.netbeans.modules.project.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.AsyncGUIJob;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** If you are looking for the non-GUI part of the panel please look
 * into new file wizard
 */

/**
 * Provides the GUI for the template chooser panel.
 * @author Jesse Glick
 */
final class TemplateChooserPanelGUI extends javax.swing.JPanel implements PropertyChangeListener, AsyncGUIJob {
    
    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (500, 340);
    
    // private final String[] recommendedTypes = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    //Templates folder root
    private FileObject templatesFolder;

    //GUI Builder
    private TemplatesPanelGUI.Builder builder;
    private Project project;
    private String category;
    private String template;
    private boolean isWarmUp = true;
    private ListCellRenderer projectCellRenderer;
    private boolean firstTime = true;
    
    public TemplateChooserPanelGUI() {
        this.builder = new FileChooserBuilder ();
        initComponents();
        setPreferredSize( PREF_DIM );
        setName (org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "LBL_TemplateChooserPanelGUI_Name")); // NOI18N
        projectCellRenderer = new ProjectCellRenderer ();
        projectsComboBox.setRenderer (projectCellRenderer);
     }
    
    public void readValues (Project p, String category, String template) {
        assert p != null : "Project can not be null";   //NOI18N
        boolean wf;
        synchronized (this) {
            this.project = p;
            this.category = category;
            this.template = template;
            wf = this.isWarmUp;
        }
        if (!wf) {
            this.selectProject ( project );
            ((TemplatesPanelGUI)this.templatesPanel).setSelectedCategoryByName (this.category);
            ((TemplatesPanelGUI)this.templatesPanel).setSelectedTemplateByName (this.template);
        }
    }

    /** Called from readSettings, to initialize the GUI with proper components
     */
    private void initValues( Project p ) {
        // Populate the combo box with list of projects
        Project openProjects[] = OpenProjectList.getDefault().getOpenProjects();
        Arrays.sort( openProjects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );
        this.selectProject (p);
    }

    private void selectProject (Project p) {
        if (p != null) {
            DefaultComboBoxModel projectsModel = (DefaultComboBoxModel) projectsComboBox.getModel ();
            if ( projectsModel.getIndexOf( p ) == -1 ) {
                projectsModel.insertElementAt( p, 0 );
            }
            projectsComboBox.setSelectedItem( p );
        }
    }


    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
	for (ChangeListener l: templist) {
            l.stateChanged(e);
        }
    }
    
    
    public Project getProject() {
        boolean wf;
        synchronized (this) {
            wf = isWarmUp;
        }
        if (wf) {
            return this.project;
        }
        else {
            return (Project)projectsComboBox.getSelectedItem();
        }
    }
    
    public FileObject getTemplate() {
        return ((TemplatesPanelGUI)this.templatesPanel).getSelectedTemplate ();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireChange();
    }
    
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    public String getCategoryName () {
        return ((TemplatesPanelGUI)this.templatesPanel).getSelectedCategoryName ();
    }

    public String getTemplateName () {
        return ((TemplatesPanelGUI)this.templatesPanel).getSelectedTemplateName ();
    }

    public void setCategory (String category) {
        ((TemplatesPanelGUI)this.templatesPanel).setSelectedCategoryByName (category);
    }
    
    public void addNotify () {
        if (firstTime) {
            //77244 prevent multiple initializations..
            Utilities.attachInitJob (this, this);
            firstTime = false;
        }
        super.addNotify ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectsComboBox = new javax.swing.JComboBox();
        templatesPanel = new TemplatesPanelGUI (this.builder);

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "MNE_TemplateChooserPanelGUI_jLabel1").charAt(0));
        jLabel1.setLabelFor(projectsComboBox);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "LBL_TemplateChooserPanelGUI_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 13, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "ACSN_jLabel1"));
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "ACSD_jLabel1"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(projectsComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(templatesPanel, gridBagConstraints);

    }//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JPanel templatesPanel;
    // End of variables declaration//GEN-END:variables
    
    // private static final Comparator NATURAL_NAME_SORT = Collator.getInstance();
    
    private final class TemplateChildren extends Children.Keys<DataFolder> implements ActionListener {
        
        private final DataFolder folder;
        
        TemplateChildren(DataFolder folder) {
            this.folder = folder;
        }
        
        protected void addNotify() {
            super.addNotify();
            projectsComboBox.addActionListener( this );
            updateKeys();
        }
        
        protected void removeNotify() {
            setKeys(Collections.<DataFolder>emptySet());
            projectsComboBox.removeActionListener( this );
            super.removeNotify();
        }
        
        private void updateKeys() {
            List<DataFolder> l = new ArrayList<DataFolder>();
            for (DataObject d : folder.getChildren()) {
                FileObject prim = d.getPrimaryFile();
                if ( acceptTemplate( d, prim ) ) {
                    // has children?
                    if (hasChildren ((Project)projectsComboBox.getSelectedItem (), d)) {
                        l.add((DataFolder) d);
                    }
                }
            }
            setKeys(l);
        }
        
        protected Node[] createNodes(DataFolder d) {
            boolean haveChildren = false;
            for (DataObject child : d.getChildren()) {
                if ((child instanceof DataFolder) && !isTemplate(child)) {
                    haveChildren = true;
                    break;
                }
            }
            if (!haveChildren) {
                return new Node[] {new FilterNode(d.getNodeDelegate(), Children.LEAF )};
            } else {
                return new Node[] {new FilterNode(d.getNodeDelegate(), new TemplateChildren((DataFolder)d))};
            }
        }
        
        public void actionPerformed (ActionEvent event) {
            final String cat = getCategoryName ();
            String template =  ((TemplatesPanelGUI)TemplateChooserPanelGUI.this.templatesPanel).getSelectedTemplateName();
            this.setKeys(Collections.<DataFolder>emptySet());
            this.updateKeys ();
            setCategory (cat);
            ((TemplatesPanelGUI)TemplateChooserPanelGUI.this.templatesPanel).setSelectedTemplateByName(template);
        }
                
        
        /** Uncoment if you want to have the templates sorted alphabeticaly
         
        // Comparator ---------------------------------------------------------- 
         
        public int compare(Object o1, Object o2) {
            DataObject d1 = (DataObject)o1;
            DataObject d2 = (DataObject)o2;
            if ((d1 instanceof DataFolder) && !(d2 instanceof DataFolder)) {
                return 1;
            } else if (!(d1 instanceof DataFolder) && (d2 instanceof DataFolder)) {
                return -1;
            } else {
                return NATURAL_NAME_SORT.compare(d1.getNodeDelegate().getDisplayName(), d2.getNodeDelegate().getDisplayName());
            }
        }
        */

        // Private methods -----------------------------------------------------
        
        private boolean acceptTemplate( DataObject d, FileObject primaryFile ) {            
            if (d instanceof DataFolder && !isTemplate((DataFolder)d))  {
                Object o = primaryFile.getAttribute ("simple"); // NOI18N
                return o == null || Boolean.TRUE.equals (o);
            }
            return false;
        }
        
    }
    
    
    private final class FileChildren extends Children.Keys<DataObject> {
        
        private DataFolder root;
                
        public FileChildren (DataFolder folder) {
            this.root = folder;
            assert this.root != null : "Root can not be null";  //NOI18N
        }
        
        protected void addNotify () {
            this.setKeys (this.root.getChildren());
        }
        
        protected void removeNotify () {
            this.setKeys (new DataObject[0]);
        }
        
        protected Node[] createNodes(DataObject dobj) {
            if (isTemplate(dobj) && OpenProjectList.isRecommended(getProject(), dobj.getPrimaryFile())) {
                if (dobj instanceof DataShadow) {
                    dobj = ((DataShadow)dobj).getOriginal();
                }
                return new Node[] { new FilterNode(dobj.getNodeDelegate(), Children.LEAF) };
            } else {
                return new Node[0];
            }
        }        
        
    }
    
  
    private final class FileChooserBuilder implements TemplatesPanelGUI.Builder {
        
        public Children createCategoriesChildren(DataFolder folder) {
            return new TemplateChildren (folder);
        }
        
        public Children createTemplatesChildren(DataFolder folder) {
            return new FileChildren (folder);
        }
        
        public void fireChange() {
            TemplateChooserPanelGUI.this.fireChange();
        }
        
        public char getCategoriesMnemonic() {
            return NbBundle.getMessage (TemplateChooserPanelGUI.class,"MNE_Categories").charAt(0);
        }
        
        public String getCategoriesName() {
            return NbBundle.getMessage (TemplateChooserPanelGUI.class,"CTL_Categories");
        }
        
        public char getTemplatesMnemonic() {
            return NbBundle.getMessage (TemplateChooserPanelGUI.class,"MNE_Files").charAt(0);
        }
        
        public String getTemplatesName() {
            return NbBundle.getMessage (TemplateChooserPanelGUI.class,"CTL_Files");
        }
        
    }
    
    // #89393: GTK needs cell renderer to implement UIResource to look "natively"
    private static class ProjectCellRenderer extends JLabel implements ListCellRenderer, UIResource  {
        
        
        public ProjectCellRenderer() {
            setOpaque(true);
        }
        
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
                    
            if ( value instanceof Project ) {
                ProjectInformation pi = ProjectUtils.getInformation((Project)value);
                setText(pi.getDisplayName());
                setIcon(pi.getIcon());
            }
            else {
                setText( value == null ? "" : value.toString () ); // NOI18N
                setIcon( null );
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

        // #89393: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
    
    private static boolean isTemplate (DataObject dobj) {
        if (dobj.isTemplate())
            return true;
        if (dobj instanceof DataShadow) {
            return ((DataShadow)dobj).getOriginal().isTemplate();
        }
        return false;
    }
    
    private boolean hasChildren (Project p, DataObject folder) { 
        if (!(folder instanceof DataFolder)) {
            return false;
        }
        
        DataFolder f = (DataFolder) folder;
        DataObject[] ch = f.getChildren ();
        boolean ok = false;
        for (int i = 0; i < ch.length; i++) {
            if (isTemplate (ch[i]) && OpenProjectList.isRecommended(p, ch[i].getPrimaryFile ())) {
                // XXX: how to filter link to Package template in each java types folder?
                if (!(ch[i] instanceof DataShadow)) {
                    ok = true;
                    break;
                }
            } else if (ch[i] instanceof DataFolder && hasChildren (p, ch[i])) {
                    ok = true;
                    break;
            }
        }
        return ok;
        
        // simplied but more counts
        //return new FileChildren (p, (DataFolder) folder).getNodesCount () > 0;
        
    }
    
    public void construct () {
        this.templatesFolder = Repository.getDefault().getDefaultFileSystem().findResource("Templates");
        ((TemplatesPanelGUI)this.templatesPanel).warmUp(this.templatesFolder);
    }
    
    public void finished () {
        //In the awt
        Cursor cursor = null;
        try {
            Project p;
            String c,t;
            synchronized (this) {
                p = this.project;
                c = this.category;
                t = this.template;
            }
            cursor = TemplateChooserPanelGUI.this.getCursor();
            TemplateChooserPanelGUI.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            initValues( p );
            ((TemplatesPanelGUI)this.templatesPanel).doFinished (this.templatesFolder, c, t);
        } finally {
            synchronized (this) {
                isWarmUp = false;
            }
            if (cursor != null) {
                this.setCursor (cursor);
            }
        }
    }
    
}
