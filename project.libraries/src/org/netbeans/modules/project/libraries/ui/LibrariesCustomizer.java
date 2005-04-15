/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  tom
 */
public final class LibrariesCustomizer extends JPanel implements ExplorerManager.Provider, HelpCtx.Provider {
    
    private static final Dimension PREFERRED_SIZE = new Dimension (720,400);
    
    private ExplorerManager manager;
    private LibrariesModel model;
    private BeanTreeView libraries;

    /** Creates new form LibrariesCustomizer */
    public LibrariesCustomizer () {
        this.model = new LibrariesModel ();
        initComponents();
        postInitComponents ();
    }


    public void setSelectedLibrary (LibraryImplementation library) {
        if (library == null)
            return;
        ExplorerManager manager = this.getExplorerManager();
        Node root = manager.getRootContext();        
        String[] path = new String[2];
        path[0]=library.getType();
        path[1]=library.getName();
        try {
            Node node = NodeOp.findPath(root, path);
            if (node != null) {
                manager.setSelectedNodes(new Node[] {node});
            }
        } catch (NodeNotFoundException e) {
            //Ignore it
        }
        catch (PropertyVetoException e) {
            //Ignore it
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( LibrariesCustomizer.class );
    }
    
    public boolean apply () {
        try {
            this.model.apply();
            return true;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return false;
        }
    }

    public void cancel () {
        this.model.cancel();
    }

    public void addNotify() {
        super.addNotify();
        expandAllNodes(this.libraries,this.getExplorerManager().getRootContext());
        //Select first library if nothing selected
        if (this.getExplorerManager().getSelectedNodes().length == 0) {
        Node root = this.getExplorerManager().getRootContext();
            Node[] nodes = root.getChildren().getNodes (true);
            for (int i = 0; i< nodes.length; i++) {
                Node[] lnodes = nodes[i].getChildren().getNodes(true);
                if (lnodes.length > 0) {
                    try {
                        this.getExplorerManager().setSelectedNodes(new Node[] {lnodes[0]});
                    } catch (PropertyVetoException e) {
                        //Ignore it
                    }
                    break;
                }
            }
        }
        this.libraries.requestFocus();
    }
    
    public Dimension getPreferredSize () {
        return PREFERRED_SIZE;
    }
    
    
    public ExplorerManager getExplorerManager () {
        if (this.manager == null) {
            this.manager = new ExplorerManager ();
            this.manager.addPropertyChangeListener (new PropertyChangeListener() {
                public void propertyChange (PropertyChangeEvent event) {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue ();
                        selectLibrary(nodes);                            
                        libraries.requestFocus();
                    }                    
                }
            });
            this.manager.addVetoableChangeListener(new VetoableChangeListener() {
                public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
                    if (ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
                        Node[] nodes = (Node[]) event.getNewValue();
                        if (nodes.length <=1) {
                            return;
                        }
                        else {
                            throw new PropertyVetoException ("Invalid length", event);  //NOI18N
                        }
                    }
                }
            });            
            this.manager.setRootContext (buildTree(this.model));
        }
        return this.manager;
    }


    private void postInitComponents () {
        this.libraries = new LibrariesView ();        
        GridBagConstraints c = new GridBagConstraints ();
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;        
        ((GridBagLayout)this.libsPanel.getLayout()).setConstraints(this.libraries,c);
        this.libsPanel.add(this.libraries);
        this.libraryName.setColumns(25);
        this.libraryName.setEnabled(false);
        this.libraryName.addActionListener(
                new ActionListener () {
                    public void actionPerformed(ActionEvent e) {
                        nameChanged();
                    }
                });                        
    }


    private void nameChanged () {
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1 && (nodes[0] instanceof LibraryNode)) {
            LibraryNode node = (LibraryNode) nodes[0];
            String newName = this.libraryName.getText();
            if (newName.length () == 0) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        NbBundle.getMessage(LibrariesCustomizer.class, "ERR_InvalidName"),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
            else if (isValidName (this.model, newName)) {
                node.getLibrary().setName(newName);
            }
            else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message (
                        MessageFormat.format(NbBundle.getMessage(LibrariesCustomizer.class, "ERR_ExistingName"),
                                new Object[] {newName}),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        }                        
    }


    private void selectLibrary (Node[] nodes) {
        int tabCount = this.properties.getTabCount();
        for (int i=0; i<tabCount; i++) {
            this.properties.removeTabAt(0);
        }
        this.libraryName.setEnabled(false);
        this.libraryName.setText("");   //NOI18N
        this.jLabel1.setVisible(false);
        this.libraryName.setVisible(false);
        this.properties.setVisible(false);
        this.deleteButton.setEnabled(false);        
        if (nodes.length != 1 || !(nodes[0] instanceof LibraryNode)) {            
            return;
        }
        this.jLabel1.setVisible(true);
        this.libraryName.setVisible(true);
        this.properties.setVisible(true);
        LibraryNode lnode = (LibraryNode) nodes[0];
        LibraryImplementation impl = lnode.getLibrary ();
        boolean editable = model.isLibraryEditable (impl);
        this.libraryName.setEnabled(editable);
        this.deleteButton.setEnabled(editable);
        this.libraryName.setText (getLocalizedString(impl.getLocalizingBundle(),impl.getName()));
        String libraryType = impl.getType();
        LibraryTypeProvider provider = lnode.getProvider ();
        if (provider == null)
            return;
        String[] volumeTypes = provider.getSupportedVolumeTypes();
        for (int i=0; i< volumeTypes.length; i++) {
            Customizer c = provider.getCustomizer (volumeTypes[i]);
            if (c instanceof JComponent) {
                c.setObject (impl);
                JComponent component = (JComponent) c;
                component.setEnabled (editable);
                String tabName = component.getName();
                if (tabName == null) {
                    tabName = volumeTypes[i];
                }
                this.properties.addTab(tabName, component);
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
        libraryName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        properties = new javax.swing.JTabbedPane();
        createButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        libsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AD_LibrariesCustomizer"));
        jLabel1.setLabelFor(libraryName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("CTL_CustomizerLibraryName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jLabel1, gridBagConstraints);

        libraryName.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.6;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(libraryName, gridBagConstraints);
        libraryName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AD_LibraryName"));

        jPanel1.setLayout(new java.awt.BorderLayout());

        properties.setPreferredSize(new java.awt.Dimension(400, 300));
        jPanel1.add(properties, java.awt.BorderLayout.CENTER);
        properties.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AN_LibrariesCustomizerProperties"));
        properties.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AD_LibrariesCustomizerProperties"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 12);
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("CTL_NewLibrary"));
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLibrary(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(createButton, gridBagConstraints);
        createButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AD_NewLibrary"));

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("CTL_DeleteLibrary"));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLibrary(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(deleteButton, gridBagConstraints);
        deleteButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AD_DeleteLibrary"));

        libsPanel.setLayout(new java.awt.GridBagLayout());

        libsPanel.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 6);
        add(libsPanel, gridBagConstraints);
        libsPanel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("AD_libsPanel"));

        jLabel2.setLabelFor(libsPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/project/libraries/ui/Bundle").getString("TXT_LibrariesPanel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 2, 12);
        add(jLabel2, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void deleteLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibrary
        Node[] nodes = this.getExplorerManager().getSelectedNodes();
        if (nodes.length == 1 && (nodes[0] instanceof LibraryNode)) {            
            Node[] sib = nodes[0].getParentNode().getChildren().getNodes(true);            
            Node selNode = null;
            for (int i=0; i < sib.length; i++) {
                if (nodes[0].equals(sib[i])) {
                    if (i>0) {
                        selNode = sib[i-1];
                    }
                    else if (i<sib.length-1){
                        selNode = sib[i+1];
                    }
                }
            }            
            model.removeLibrary (((LibraryNode)nodes[0]).getLibrary());
            try {
                if (selNode != null) {
                    this.getExplorerManager().setSelectedNodes(new Node[] {selNode});            
                }
            } catch (PropertyVetoException e) {
                //Ignore it
            }
            this.libraries.requestFocus();
        }
    }//GEN-LAST:event_deleteLibrary

    private void createLibrary(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLibrary
        Dialog dlg = null;
        try {
            String preselectedLibraryType = null;
            Node[] preselectedNodes = this.getExplorerManager().getSelectedNodes();
            if (preselectedNodes.length == 1) {
                LibraryCategory lc = (LibraryCategory) preselectedNodes[0].getLookup().lookup(LibraryCategory.class);
                if (lc != null) {
                    preselectedLibraryType = lc.getCategoryType();
                }
            }
            NewLibraryPanel p = new NewLibraryPanel (this.model, preselectedLibraryType);
            DialogDescriptor dd = new DialogDescriptor (p, NbBundle.getMessage(LibrariesCustomizer.class,"CTL_CreateLibrary"),
                    true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
            p.setDialogDescriptor(dd);
            dlg = DialogDisplayer.getDefault().createDialog (dd);
            dlg.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                String libraryType = p.getLibraryType();
                String libraryName = p.getLibraryName();
                LibraryTypeProvider provider = LibraryTypeRegistry.getDefault().getLibraryTypeProvider (libraryType);
                if (provider == null) {
                    return;
                }
                LibraryImplementation impl = provider.createLibrary();
                impl.setName (libraryName);
                model.addLibrary (impl);                
                String[] path = new String[2];
                path[0] = impl.getType();
                path[1] = impl.getName();
                ExplorerManager mgr = this.getExplorerManager();
                try {
                    Node node = NodeOp.findPath(mgr.getRootContext(),path);
                    if (node != null) {
                        mgr.setSelectedNodes(new Node[] {node});
                    }
                } catch (PropertyVetoException e) {
                    //Ignore it
                }
                catch (NodeNotFoundException e) {
                    //Ignore it
                }
                this.libraryName.requestFocus();
                this.libraryName.selectAll();
            }
            else {
                this.libraries.requestFocus();
            }
        }
        finally {
            if (dlg != null)
                dlg.dispose();
        }
    }//GEN-LAST:event_createLibrary


    static boolean isValidName (LibrariesModel model, String name) {
        int count = model.getSize();
        for (int i=0; i<count; i++) {
            LibraryImplementation lib = (LibraryImplementation) model.getElementAt (i);
            if (lib != null && lib.getName().equals(name))
                return false;
        }
        return true;
    }


    static String getLocalizedString (String bundleResourceName, String key) {
        if (key == null) {
            return null;
        }
        if (bundleResourceName == null) {
            return key;
        }
        ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleResourceName);
        } catch (MissingResourceException mre) {
            // Bundle should have existed.
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mre);
            return key;
        }
        try {
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            // No problem, not specified.
            return key;
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField libraryName;
    private javax.swing.JPanel libsPanel;
    private javax.swing.JTabbedPane properties;
    // End of variables declaration//GEN-END:variables
            

    private static void expandAllNodes (BeanTreeView btv, Node node) {
        btv.expandNode (node);
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {            
            return;
        }
        Node nodes[] = ch.getNodes( true );
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i]);
        }

    }
    
    private static class LibrariesView extends BeanTreeView {
        
        public LibrariesView () {
            super ();
            this.setRootVisible(false);
            this.setPopupAllowed(false);
            this.setDefaultActionAllowed(false);
            this.tree.setEditable (false);
            this.tree.setShowsRootHandles (false);
        }
        
    }
    
    
    private static class RootChildren extends Children.Keys {        
        
        private LibrariesModel model;
        
        public RootChildren (LibrariesModel model) {
            this.model = model;
        }
        
        public void addNotify () {
            this.setKeys(LibraryTypeRegistry.getDefault().getLibraryTypeProviders());
        }
        
        public void removeNotify () {
            this.setKeys (new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof LibraryTypeProvider) {
                LibraryTypeProvider provider = (LibraryTypeProvider) key;
                return new Node[] {
                    new CategoryNode (provider, this.model)
                };
            }
            return new Node[0];
        }
        
    }
    
    
    private static final class LibraryCategory {
        
        private final String name;
        
        LibraryCategory (String name) {
            this.name = name;
        }
        
        public String getCategoryType () {
            return this.name;
        }
        
    }
    
    private static class CategoryNode extends AbstractNode {
                
        
        private LibraryTypeProvider provider;
        private Node iconDelegate;
                
        public CategoryNode (LibraryTypeProvider provider, LibrariesModel model) {
            super (new CategoryChildren(provider, model), Lookups.singleton(new LibraryCategory (provider.getLibraryType())));
            this.provider = provider;       
            this.iconDelegate = DataFolder.findFolder (Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        }
        
        public String getName () {
            return provider.getLibraryType ();
        }
        
        public String getDisplayName() {
            return this.provider.getDisplayName();
        }
        
        public Image getIcon(int type) {            
            return this.iconDelegate.getIcon (type);
        }        
        
        public Image getOpenedIcon(int type) {
            return this.iconDelegate.getOpenedIcon (type);
        }        
                        
    }    
    
    private static class CategoryChildren extends Children.Keys implements ListDataListener {
        
        private LibraryTypeProvider provider;
        private LibrariesModel model;
        
        public CategoryChildren (LibraryTypeProvider provider, LibrariesModel model) {
            this.provider = provider;
            this.model = model;
            this.model.addListDataListener(this);
        }
        
        public void addNotify () {
            Collection keys = new ArrayList ();
            for (int i=0; i<model.getSize(); i++) {
                LibraryImplementation impl = (LibraryImplementation) model.getElementAt(i);
                if (this.provider.getLibraryType().equals(impl.getType())) {
                    keys.add (impl);
                }
            }
            this.setKeys(keys);
        }
        
        public void removeNotify () {
            this.setKeys(new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof LibraryImplementation) {
                LibraryImplementation impl = (LibraryImplementation) key;                
                return new Node[] {
                    new LibraryNode (impl, this.provider)
                };
            }
            return new Node[0];
        }
        
        public void contentsChanged(ListDataEvent e) {
            //Todo: Optimize it
            this.addNotify();
        }
        
        public void intervalAdded(ListDataEvent e) {
            //Todo: Optimize it
            this.addNotify();
        }
        
        public void intervalRemoved(ListDataEvent e) {
            //Todo: Optimize it
            this.addNotify();
        }
        
    }
    
    private static class LibraryNode extends AbstractNode {
        
        private static final String ICON = "org/netbeans/modules/project/libraries/resources/libraries";  //NOI18N
        
        private LibraryImplementation lib;
        private LibraryTypeProvider provider;
        
        public LibraryNode (LibraryImplementation lib, LibraryTypeProvider provider) {            
            super (Children.LEAF);
            this.lib = lib;
            this.provider = provider;
            this.setIconBase(ICON);
        }
        
        public String getName () {            
            return this.lib.getName ();
        }
        
        public String getDisplayName () {
            return getLocalizedString(this.lib.getLocalizingBundle(), this.lib.getName());
        }
        
        public LibraryImplementation getLibrary () {
            return this.lib;            
        }
        
        public LibraryTypeProvider getProvider () {
            return this.provider;
        }
        
        public boolean equals (Object other) {
            if (other instanceof LibraryNode) {
                LibraryNode ol = (LibraryNode) other;
                return (this.lib == null ? ol.lib == null : this.lib.equals(ol.lib))
                    && (this.provider == null ? ol.provider == null : this.provider.equals(ol.provider));
            }
            return false;
        }
    }
    
    private static Node buildTree (LibrariesModel model) {
        return new AbstractNode (new RootChildren (model));
    }
    
    
}
