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
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
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
import org.openide.util.NbBundle;

/** If you are looking for the non-GUI part of the panel please look
 * into new file wizard
 */

/**
 * Provides the GUI for the template chooser panel.
 * @author Jesse Glick
 */
final class TemplateChooserPanelGUI extends javax.swing.JPanel implements PropertyChangeListener {
    
    private static final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    
    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (560, 350);
    
    // private final String[] recommendedTypes = null;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    
    private Project project;
    
    //GUI Builder
    private TemplatesPanelGUI.Builder builder;
    
    public TemplateChooserPanelGUI(Project p /* , String[] recommendedTypes */ ) {
        this.builder = new FileChooserBuilder ();
        initComponents();        
        ((TemplatesPanelGUI)this.templatesPanel).setTemplatesFolder (Repository.getDefault().getDefaultFileSystem().findResource("Templates"));  //NOI18N
        initValues( p );        
        setName (org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "LBL_TemplateChooserPanelGUI_Name")); // NOI18N
        projectsComboBox.setRenderer( PROJECT_CELL_RENDERER );                
     }
    
    /** Called from readSettings, to initialize the GUI with proper components
     */
    public void initValues( Project p ) {
        // Populate the combo box with list of projects
        Project openProjects[] = OpenProjectList.getDefault().getOpenProjects();
        Arrays.sort( openProjects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );
        if ( projectsModel.getIndexOf( p ) == -1 ) {
            projectsModel.insertElementAt( p, 0 );
        }
        projectsComboBox.setSelectedItem( p );
        project = p;
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
        return (Project)projectsComboBox.getSelectedItem();
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

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TemplateChooserPanelGUI.class, "LBL_TemplateChooserPanelGUI_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 13, 0);
        add(jLabel1, gridBagConstraints);

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
            // SortedSet/*<DataObject>*/ l = new TreeSet(this);
            List l = new ArrayList();
            DataObject[] kids = folder.getChildren();
            String[] recommendedTypes = getRecommendedTypes ( (Project)projectsComboBox.getSelectedItem() );
            for (int i = 0; i < kids.length; i++) {
                DataObject d = kids[i];
                FileObject prim = d.getPrimaryFile();
                if ( acceptTemplate( d, prim ) ) {
                    // issue 43958, if attr 'templateCategorized' is not set => all is ok
                    if (isCategorized (prim)) {
                        if ( recommendedTypes != null) {
                            // XXX assert recommendedTypes != null;
                            boolean ok = false;
                            for (int j = 0; j < recommendedTypes.length; j++) {
                                if (Boolean.TRUE.equals(prim.getAttribute(recommendedTypes[j]))) {
                                    ok = true;
                                    break;
                                }
                            }
                            if (!ok) {
                                continue;
                            }
                        }
                    }
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
            this.updateKeys ();
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
    
    
    private static class FileChildren extends Children.Keys {
        
        private DataFolder root;
        private Project project;
                
        public FileChildren (Project p, DataFolder folder) {
            this.root = folder;
            this.project = p;
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
                if (isTemplate(dobj) && isRightCategory (project, dobj.getPrimaryFile ())) {
                    return new Node[] {
                        new FilterNode (dobj.getNodeDelegate(),Children.LEAF)
                    };
                }
            }
            return new Node[0];
        }        
        
    }
    
  
    private final class FileChooserBuilder implements TemplatesPanelGUI.Builder {
        
        public Children createCategoriesChildren(FileObject fo) {
            return new TemplateChildren (DataFolder.findFolder(fo));
        }
        
        public Children createTemplatesChildren(FileObject fo) {
            return new FileChildren (project, DataFolder.findFolder(fo));
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
                setText( value.toString () );
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
    
    private static boolean isCategorized (FileObject primaryFile) {
        Object o = primaryFile.getAttribute ("templateCategorized"); // NOI18N
        if (o != null) {
            assert o instanceof Boolean : primaryFile + ", attr templateCategorized = " + o;
            return ((Boolean)o).booleanValue ();
        } else {
            return false;
        }
    }

    private static boolean isRightCategory (Project p, FileObject primaryFile) {
        if (getRecommendedTypes (p) == null || getRecommendedTypes (p).length == 0) {
            // if no recommendedTypes are supported (i.e. freeform) -> disaply all templates
            return true;
        }
        
        if (isCategorized (primaryFile)) {
            Object o = primaryFile.getAttribute ("templateCategory"); // NOI18N
            if (o != null) {
                assert o instanceof String : primaryFile + " attr templateCategory = " + o;
                String category = (String)o;
                return Arrays.asList (getRecommendedTypes (p)).contains (category);
            } else {
                return false;
            }
        }
        
        // no category set, ok display it
        return true;
    }

    private static String[] getRecommendedTypes (Project project) {
        RecommendedTemplates rt = (RecommendedTemplates)project.getLookup().lookup( RecommendedTemplates.class );
        return rt == null ? null :rt.getRecommendedTypes();
    }
    
    private boolean hasChildren (Project p, DataObject folder) { 
        if (!(folder instanceof DataFolder)) {
            return false;
        }
        
        DataFolder f = (DataFolder) folder;
        DataObject[] ch = f.getChildren ();
        boolean ok = false;
        for (int i = 0; i < ch.length; i++) {
            if (isTemplate (ch[i]) && isRightCategory (p, ch[i].getPrimaryFile ())) {
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

}
