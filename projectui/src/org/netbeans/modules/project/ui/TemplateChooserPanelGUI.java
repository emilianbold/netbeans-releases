/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.api.project.ProjectInformation;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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
    
    private static final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    
    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (500, 340);
    
    // private final String[] recommendedTypes = null;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    //GUI Builder
    private TemplatesPanelGUI.Builder builder;
    private Project project;
    private boolean isWarmingUp = false;
    
    public TemplateChooserPanelGUI(Project p /* , String[] recommendedTypes */ ) {
        assert p != null : "Project cannot be null.";  // NOI18N
        project = p;
        setPreferredSize( PREF_DIM );
        setName (org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "LBL_TemplateChooserPanelGUI_Name")); // NOI18N
        isWarmingUp = true;
        Utilities.attachInitJob (this, this);
     }
    
    /** Called from readSettings, to initialize the GUI with proper components
     */
    public void initValues( Project p ) {
        if (isWarmingUp) {
            return ;
        }
        // Populate the combo box with list of projects
        Project openProjects[] = OpenProjectList.getDefault().getOpenProjects();
        Arrays.sort( openProjects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );
        if ( projectsModel.getIndexOf( p ) == -1 ) {
            projectsModel.insertElementAt( p, 0 );
        }
        projectsComboBox.setSelectedItem( p );
    }
    
    
    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
    
    
    public Project getProject() {
        if (isWarmingUp) {
            // during warming up the form then return then project given from constructor
            return project;
        }
        return (Project)projectsComboBox.getSelectedItem();
    }
    
    public FileObject getTemplate() {
        if (isWarmingUp) {
            return null;
        }
        return ((TemplatesPanelGUI)this.templatesPanel).getSelectedTemplate ();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireChange();
    }
    
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    private String getCategory () {
        if (isWarmingUp) {
            return null;
        }
        return ((TemplatesPanelGUI)this.templatesPanel).getSelectedCategoryName ();
    }

    public void setCategory (String category) {
        ((TemplatesPanelGUI)this.templatesPanel).setSelectedCategoryByName (category);
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
    
    private final class TemplateChildren extends Children.Keys/*<DataObject>*/ implements ActionListener {
        
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
            setKeys(Collections.EMPTY_SET);
            projectsComboBox.removeActionListener( this );
            super.removeNotify();
        }
        
        private void updateKeys() {
            List l = new ArrayList();
            DataObject[] kids = folder.getChildren();
            for (int i = 0; i < kids.length; i++) {
                DataObject d = kids[i];
                FileObject prim = d.getPrimaryFile();
                if ( acceptTemplate( d, prim ) ) {
                    // has children?
                    if (hasChildren ((Project)projectsComboBox.getSelectedItem (), d)) {
                        l.add(d);
                    }
                }
            }
            setKeys(l);
        }
        
        protected Node[] createNodes(Object key) {
            DataFolder d = (DataFolder)key;
            DataObject[] chlds = d.getChildren();
            int state = 0;
            for (int i=0; i<chlds.length; i++) {
                if ((chlds[i] instanceof DataFolder) && !isTemplate(chlds[i])) {
                    state = 1;
                    break;
                }
            }
            if (state == 0) {
                return new Node[] {new FilterNode(d.getNodeDelegate(), Children.LEAF )};
            } else {
                return new Node[] {new FilterNode(d.getNodeDelegate(), new TemplateChildren((DataFolder)d))};
            }
        }
        
        public void actionPerformed (ActionEvent event) {
            String cat = getCategory ();
            String template =  ((TemplatesPanelGUI)TemplateChooserPanelGUI.this.templatesPanel).getSelectedTemplateName();
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
    
    
    private final class FileChildren extends Children.Keys {
        
        private DataFolder root;
                
        public FileChildren (DataFolder folder) {
            this.root = folder;
            assert this.root != null : "Root can not be null";  //NOI18N
        }
        
        protected void addNotify () {
            this.setKeys (this.root.getChildren());
        }
        
        protected void removeNotify () {
            this.setKeys (new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof DataObject) {
                DataObject dobj = (DataObject)key;
                if (isTemplate(dobj) && OpenProjectList.isRecommended (getProject (), dobj.getPrimaryFile ())) {
                    return new Node[] {
                        new FilterNode (dobj.getNodeDelegate(),Children.LEAF)
                    };
                }
            }
            return new Node[0];
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
    
    
    private static class ProjectCellRenderer extends JLabel implements ListCellRenderer  {
        
        
        public ProjectCellRenderer() {
            setOpaque(true);
        }
        
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
                    
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
            }
        }
        return ok;
        
        // simplied but more counts
        //return new FileChildren (p, (DataFolder) folder).getNodesCount () > 0;
        
    }
    
    public void construct () {
        this.builder = new FileChooserBuilder ();
        initComponents();
        isWarmingUp = false;
        projectsComboBox.setRenderer( PROJECT_CELL_RENDERER );                
        initValues( project );        
    }
    
    public void finished () {
        final TemplatesPanelGUI tempPanel = (TemplatesPanelGUI)this.templatesPanel;
        ((TemplatesPanelGUI)this.templatesPanel).setTemplatesFolder (Repository.getDefault().getDefaultFileSystem().findResource("Templates"));  //NOI18N
        // select first category
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                tempPanel.setSelectedCategoryByName (null);
            }
        });
        ((TemplatesPanelGUI)this.templatesPanel).doFinished ();
    }
    
}
