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
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

// XXX I18N

/** If you are looking for the non-GUI part of the panel please look
 * into new file wizard
 */

/**
 * Provides the GUI for the template chooser panel.
 * @author Jesse Glick
 */
final class TemplateChooserPanelGUI extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener {
    
    private static final ListCellRenderer PROJECT_CELL_RENDERER = new ProjectCellRenderer();
    
    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (560, 350);
    
    // private final String[] recommendedTypes = null;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private final ExplorerManager manager;
    
    public TemplateChooserPanelGUI(Project p /* , String[] recommendedTypes */ ) {
        /* this.recommendedTypes = recommendedTypes; */
        manager = new ExplorerManager();
        DataFolder templates = DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().findResource("Templates")); // NOI18N
        manager.setRootContext(new FilterNode(templates.getNodeDelegate(), new TemplateChildren(templates)));
        try {
            manager.setSelectedNodes(new Node[] {manager.getRootContext()});
        } catch (PropertyVetoException e) {
            throw new AssertionError(e);
        }
        manager.addPropertyChangeListener(this);
        initComponents();        
        initValues( p );
        
        
        setName( "Choose Template");
        projectsComboBox.setRenderer( PROJECT_CELL_RENDERER );
        
        
        // Create the templates view
        BeanTreeView btv = new TemplatesTreeView();
        btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );        
        btv.setRootVisible( false );
        btv.setBorder( descriptionScrollPane.getBorder() );
        btv.setDefaultActionAllowed(false);
        btv.setPopupAllowed(false);       
        templatesPanel.add( btv, java.awt.BorderLayout.CENTER );
                
     }
    
    /** Called from readSettings, to initialize the GUI with proper components
     */
    public void initValues( Project p ) {
        // Populate the combo box with list of projects
        Project openProjects[] = OpenProjectList.getDefault().getOpenProjects();
        Arrays.sort( openProjects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
        DefaultComboBoxModel projectsModel = new DefaultComboBoxModel( openProjects );
        projectsComboBox.setModel( projectsModel );                
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
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }    
    
    public Project getProject() {
        return (Project)projectsComboBox.getSelectedItem();
    }
    
    public FileObject getTemplate() {
        Node[] sel = manager.getSelectedNodes();
        if (sel.length == 1) {
            
            if ( sel[0].getParentNode() == null ) {
                return null;
            }
            
            DataObject d = (DataObject)sel[0].getLookup().lookup(DataObject.class);
                        
            if (d != null && d.isTemplate() ) {
                
                FileObject pf = d.getPrimaryFile();
                return pf;
            }
        }
        return null;
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
        jLabel2 = new javax.swing.JLabel();
        templatesPanel = new javax.swing.JPanel();
        showRecommendedTemplatesCheckBox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        descriptionScrollPane = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Project:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 13, 0);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(projectsComboBox, gridBagConstraints);

        jLabel2.setText("Templates:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabel2, gridBagConstraints);

        templatesPanel.setLayout(new java.awt.BorderLayout());

        templatesPanel.setFocusable(false);
        templatesPanel.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(templatesPanel, gridBagConstraints);

        showRecommendedTemplatesCheckBox.setMnemonic('A');
        showRecommendedTemplatesCheckBox.setText("Only Show File Types Supported in Selected Project");
        showRecommendedTemplatesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 12, 0);
        add(showRecommendedTemplatesCheckBox, gridBagConstraints);

        jLabel3.setText("Description:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabel3, gridBagConstraints);

        descriptionScrollPane.setFocusable(false);
        descriptionScrollPane.setRequestFocusEnabled(false);
        descriptionScrollPane.setEnabled(false);
        jTextArea1.setEditable(false);
        jTextArea1.setText("No description");
        jTextArea1.setFocusable(false);
        descriptionScrollPane.setViewportView(jTextArea1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(descriptionScrollPane, gridBagConstraints);

    }//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JComboBox projectsComboBox;
    private javax.swing.JCheckBox showRecommendedTemplatesCheckBox;
    private javax.swing.JPanel templatesPanel;
    // End of variables declaration//GEN-END:variables
    
    // private static final Comparator NATURAL_NAME_SORT = Collator.getInstance();
    
    private final class TemplateChildren extends Children.Keys/*<DataObject>*/ implements ChangeListener, ActionListener /*, Comparator/*<DataObject>*/ {
        
        private final DataFolder folder;
        
        TemplateChildren(DataFolder folder) {
            this.folder = folder;
        }
        
        protected void addNotify() {
            super.addNotify();
            showRecommendedTemplatesCheckBox.addChangeListener(this);
            projectsComboBox.addActionListener( this );
            updateKeys();
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            showRecommendedTemplatesCheckBox.removeChangeListener(this);
            projectsComboBox.removeActionListener( this );
            super.removeNotify();
        }
        
        private void updateKeys() {
            // SortedSet/*<DataObject>*/ l = new TreeSet(this);
            List l = new ArrayList();
            DataObject[] kids = folder.getChildren();
            String[] recommendedTypes = getRecommendedTypes();
            for (int i = 0; i < kids.length; i++) {
                DataObject d = kids[i];
                FileObject prim = d.getPrimaryFile();
                if ( acceptTemplate( d, prim ) ) { 
                    if ( showRecommendedTemplatesCheckBox.isSelected() && recommendedTypes != null) {
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
                    l.add(d);
                }
            }
            setKeys(l);
        }
        
        protected Node[] createNodes(Object key) {
            DataObject d = (DataObject)key;
            if (d instanceof DataFolder && !d.isTemplate() ) {
                return new Node[] {new FilterNode(d.getNodeDelegate(), new TemplateChildren((DataFolder)d))};
            } else {
                return new Node[] {new FilterNode(d.getNodeDelegate(), Children.LEAF ) };
            }
        }
        
        // State listener ------------------------------------------------------
        
        public void stateChanged( ChangeEvent e ) {
            updateKeys();
        }
        
        // ActionListener ------------------------------------------------------
        
        public void actionPerformed( ActionEvent e ) {
            updateKeys();
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
        
        private String[] getRecommendedTypes() {
            Project project = (Project)projectsComboBox.getSelectedItem();
            RecommendedTemplates rt = (RecommendedTemplates)project.getLookup().lookup( RecommendedTemplates.class );
            return rt == null ? null :rt.getRecommendedTypes();
        }
        
        private boolean acceptTemplate( DataObject d, FileObject primaryFile ) {
            
            if (d instanceof DataFolder )  {
                Object o = primaryFile.getAttribute ("simple"); // NOI18N
                return o == null || Boolean.TRUE.equals (o);
            }
            else {
                return Boolean.TRUE.equals(primaryFile.getAttribute("template")); // NOI18N
            }
        }
    }
    
    // Private innerclasses ----------------------------------------------------

    // Just to make the tree non-editable 
    private static final class TemplatesTreeView extends BeanTreeView {
        
        TemplatesTreeView() {
            tree.setEditable(false);
        }
                
    }
    
    
    private static class ProjectCellRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer implements ListCellRenderer {
        
        /*
        public ProjectCellRenderer() {
            setOpaque(true);
        }
        */
        
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
            javax.swing.plaf.basic.BasicComboBoxRenderer cbr = (javax.swing.plaf.basic.BasicComboBoxRenderer)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );   
            
            if ( value != null ) {
                ProjectInformation pi = ProjectUtils.getInformation((Project)value);
                cbr.setText(pi.getDisplayName());
                cbr.setIcon(pi.getIcon());
            }
            return cbr;
        }
        
    }
    
    
}
