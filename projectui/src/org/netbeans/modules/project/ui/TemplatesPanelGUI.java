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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;

/**
 *
 * @author  tom
 */
public class TemplatesPanelGUI extends javax.swing.JPanel implements PropertyChangeListener {
    
    public static final String TEMPLATES_FOLDER = "templatesFolder";        //NOI18N
    public static final String TARGET_TEMPLATE = "targetTemplate";          //NOI18N
    private static final String ATTR_INSTANTIATING_DESC = "instantiatingWizardURL"; //NOI18N
    
    private TemplatesPanel firer;
    private TemplateWizard wiz;
    
    /** Creates new form TemplatesPanelGUI */
    public TemplatesPanelGUI (TemplatesPanel firer) {
        this.firer = firer;
        initComponents();
        postInitComponents ();
        setName (NbBundle.getMessage(TemplatesPanelGUI.class,"TXT_SelectTemplate"));
    }
    
    public void propertyChange (PropertyChangeEvent event) {
        if (event.getSource() == this.categoriesPanel) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (event.getPropertyName())) {
                try {
                    ((ExplorerProviderPanel)this.projectsPanel).setSelectedNodes(new Node[0]);
                } catch (PropertyVetoException e) {
                    /*Ignore it*/
                }
                Node[] selectedNodes = (Node[]) event.getNewValue();
                if (selectedNodes != null && selectedNodes.length == 1) {
                    DataObject template = (DataObject) selectedNodes[0].getCookie(DataObject.class);
                    if (template != null) {
                        FileObject fo = template.getPrimaryFile();
                        ((ExplorerProviderPanel)this.projectsPanel).setRootNode(
                            new FilterNode (selectedNodes[0], new TemplateChildren (fo)));
                        URL descURL = getDescription (template);
                        if (descURL != null) {
                            try {
                                this.description.setPage (descURL);                                                                
                                return;
                            } catch (IOException e) {
                                //Ignore it
                            }
                        }                        
                    }
                    this.description.setText (ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("TXT_NoDescription"));                        
                }
            }
        }
        else if (event.getSource() == this.projectsPanel) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (event.getPropertyName())) {
                Node[] selectedNodes = (Node[]) event.getNewValue ();
                if (selectedNodes != null && selectedNodes.length == 1) {
                    DataObject template = (DataObject) selectedNodes[0].getCookie(DataObject.class);
                    if (template != null) {
                        FileObject fo = template.getPrimaryFile();
                        URL descURL = getDescription (template);
                        if (descURL != null) {
                            try {
                                this.description.setPage (descURL);                                
                            } catch (IOException e) {
                                this.description.setText (ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("TXT_NoDescription"));
                            }
                        }
                        else {
                            this.description.setText (ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("TXT_NoDescription"));
                        }
                    }                    
                }
                this.firer.fireChange ();
            }
        }
    }
    
    
    void store (TemplateWizard settings) {
        Node[] nodes = (Node[]) ((ExplorerProviderPanel)this.projectsPanel).getSelectedNodes();
        if (nodes != null && nodes.length == 1) {
            DataObject dobj = (DataObject) nodes[0].getCookie (DataObject.class);
            settings.setTemplate (dobj);
        }
        String path = ((ExplorerProviderPanel)this.categoriesPanel).getSelectionPath ();
        if (path != null) {
            OpenProjectListSettings.getInstance().setLastSelectedProjectCategory(path);
        }
        path = ((ExplorerProviderPanel)this.projectsPanel).getSelectionPath ();
        if (path != null) {
            OpenProjectListSettings.getInstance().setLastSelectedProjectType (path);
        }
    }
    
    void read (TemplateWizard settings) {        
        this.wiz = settings;
        FileObject templatesFolder = (FileObject) settings.getProperty (TEMPLATES_FOLDER);        
        if (templatesFolder != null && templatesFolder.isFolder()) {
            DataFolder dobj = DataFolder.findFolder (templatesFolder);
            ((ExplorerProviderPanel)this.categoriesPanel).setRootNode(new FilterNode (
                    dobj.getNodeDelegate(), new CategoriesChildren (dobj)));
            if (settings.getProperty(TARGET_TEMPLATE) == null) {
                //First run
                String selectedCategory = OpenProjectListSettings.getInstance().getLastSelectedProjectCategory ();
                String selectedTemplate = OpenProjectListSettings.getInstance().getLastSelectedProjectType ();
                ((ExplorerProviderPanel)this.categoriesPanel).setSelectedNode (selectedCategory);
                ((ExplorerProviderPanel)this.projectsPanel).setSelectedNode (selectedTemplate);
            }
        }
    }
    
    boolean valid () {
        Node[] nodes = ((ExplorerProviderPanel)this.projectsPanel).getSelectedNodes ();
        return nodes != null && nodes.length == 1;
    }
    
    
    private void postInitComponents () {        
        BeanTreeView btv = new BeanTreeView ();
        btv.setRootVisible(false);
        btv.setPopupAllowed(false);
        btv.setDefaultActionAllowed(false);
        GridBagConstraints c = new GridBagConstraints ();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        ((GridBagLayout)this.categoriesPanel.getLayout()).setConstraints(btv, c);
        this.categoriesPanel.add (btv);
        this.categoriesPanel.addPropertyChangeListener(this);
        ListView lv = new ListView ();
        lv.setPopupAllowed(false);
        c = new GridBagConstraints ();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        ((GridBagLayout)this.projectsPanel.getLayout()).setConstraints(lv, c);
        this.projectsPanel.add (lv);
        this.projectsPanel.addPropertyChangeListener(this);
        this.description.setEditorKit(new HTMLEditorKit());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        categoriesPanel = new ExplorerProviderPanel ();
        projectsPanel = new ExplorerProviderPanel ();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        description = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 230));
        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("MNE_Categories").charAt(0));
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("CTL_Categories"));
        jLabel1.setLabelFor(categoriesPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        add(jLabel1, gridBagConstraints);

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("MNE_Projects").charAt(0));
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("CTL_Projects"));
        jLabel2.setLabelFor(projectsPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        add(jLabel2, gridBagConstraints);

        categoriesPanel.setLayout(new java.awt.GridBagLayout());

        categoriesPanel.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.7;
        add(categoriesPanel, gridBagConstraints);

        projectsPanel.setLayout(new java.awt.GridBagLayout());

        projectsPanel.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.7;
        add(projectsPanel, gridBagConstraints);

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("CTL_DescriptionMnemonic").charAt(0));
        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("CTL_Description"));
        jLabel3.setLabelFor(description);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel3, gridBagConstraints);

        description.setEditable(false);
        description.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/ui/Bundle").getString("TXT_NoDescription"));
        description.setPreferredSize(new java.awt.Dimension(100, 60));
        jScrollPane1.setViewportView(description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        add(jScrollPane1, gridBagConstraints);

    }//GEN-END:initComponents
    
    private URL getDescription (DataObject dobj) {
        //XXX: Some templates are using templateWizardURL others instantiatingWizardURL. What is correct?        
        FileObject fo = dobj.getPrimaryFile();
        URL desc = (URL) fo.getAttribute(ATTR_INSTANTIATING_DESC);
        if (desc != null) {
            return desc;
        }
        desc = wiz.getDescription (dobj);
        return desc;
    }
    
    private static class ExplorerProviderPanel extends JPanel implements ExplorerManager.Provider, PropertyChangeListener, VetoableChangeListener {
        
        private ExplorerManager manager;
        
        public ExplorerProviderPanel () {           
            this.manager = new ExplorerManager ();
            this.manager.addPropertyChangeListener(this);
            this.manager.addVetoableChangeListener(this);
        }
        
        public ExplorerProviderPanel (Node rootNode) {
            this ();
            this.manager.setRootContext(rootNode);            
        }
        
        public void setRootNode (Node node) {
            this.manager.setRootContext(node);
        }
        
        public Node getRootNode () {
            return this.manager.getRootContext();
        }
        
        public Node[] getSelectedNodes () {
            return this.manager.getSelectedNodes();
        }
        
        public void setSelectedNodes (Node[] nodes) throws PropertyVetoException {
            this.manager.setSelectedNodes(nodes);
        }
        
        public void setSelectedNode (String path) {
            if (path == null) {
                return;
            }
            StringTokenizer tk = new StringTokenizer (path,"/");    //NOI18N
            String[] names = new String[tk.countTokens()];
            for (int i=0;tk.hasMoreTokens();i++) {
                names[i] = tk.nextToken();
            }
            try {
                Node node = NodeOp.findPath(this.manager.getRootContext(),names);
                if (node != null) {
                    this.manager.setSelectedNodes(new Node[] {node});
                }
            } catch (PropertyVetoException e) {
                //Skeep it, not important
            }
            catch (NodeNotFoundException e) {
                //Skeep it, not important
            }
        }
        
        public String getSelectionPath () {
            Node[] selectedNodes = this.manager.getSelectedNodes();
            if (selectedNodes == null || selectedNodes.length != 1) {
                return null;
            }
            Node rootNode = this.manager.getRootContext();
            String[] path = NodeOp.createPath(selectedNodes[0],rootNode);
            StringBuffer builder = new StringBuffer ();
            for (int i=0; i< path.length; i++) {
                builder.append('/');        //NOI18N
                builder.append(path[i]);
            }
            return builder.substring(1);
        }
        
        public ExplorerManager getExplorerManager() {
            return this.manager;
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            this.firePropertyChange(event.getPropertyName(), 
                event.getOldValue(), event.getNewValue());
        }                        
        
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName())) {
                Node[] newValue = (Node[]) evt.getNewValue();
                if (newValue == null || (newValue.length != 1 && newValue.length != 0)) {
                    throw new PropertyVetoException ("Invalid length",evt);      //NOI18N
                }
            }
        }
        
    }
    
    
    private static class CategoriesChildren extends Children.Keys {
        
        private DataFolder root;
        
        public CategoriesChildren (DataFolder folder) {
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
                DataObject dobj = (DataObject) key;
                if (dobj instanceof DataFolder) {
                    return new Node[] {
                        new FilterNode (dobj.getNodeDelegate(), new CategoriesChildren ((DataFolder)dobj))
                    };
                }
            }
            return new Node[0];
        }                
    }
    
    private static class TemplateChildren extends Children.Keys {
        
        private FileObject root;
                
        public TemplateChildren (FileObject folder) {
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
            if (key instanceof FileObject) {
                FileObject fo = (FileObject) key;
                if (fo.isData()) {
                    try {
                        DataObject dobj = DataObject.find (fo);
                        return new Node[] {
                            new FilterNode (dobj.getNodeDelegate(),Children.LEAF)
                        };
                    } catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
            return new Node[0];
        }        
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoriesPanel;
    private javax.swing.JEditorPane description;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel projectsPanel;
    // End of variables declaration//GEN-END:variables
    
}
