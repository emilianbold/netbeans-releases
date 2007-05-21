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
package org.netbeans.modules.xml.catalog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.modules.xml.catalog.spi.CatalogWriter;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.RenameAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Pavel Buzek
 */
public class CatalogPanel extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager manager;
    private CatalogTreeView view;
    private JTree tree;
    static private Set newlyCreatedFolders;
    
    static private FileObject catalogRoot;
    
    /** Creates new form CatalogPanel */
    public CatalogPanel() {
        
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(getExplorerManager()));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(getExplorerManager()));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(getExplorerManager()));
        map.put("delete", ExplorerUtils.actionDelete(getExplorerManager(), true)); // NOI18N
        
        initComponents();
        createCatalogView();
        treePanel.add(view, BorderLayout.CENTER);
        
        associateLookup(ExplorerUtils.createLookup(getExplorerManager(), map));
        initialize();
        
    }
    
    public ExplorerManager getExplorerManager() {
        if (manager == null) {
            manager = new ExplorerManager();
        }
        return manager;
    }
    
    private void createCatalogView() {
        if (view == null) {
            view = new CatalogTreeView();
        }
        view.setRootVisible(true);
        view.setPopupAllowed(true);
        view.setDefaultActionAllowed(false);
    }
    
    private static class CatalogTreeView extends BeanTreeView {
        private Action startEditing;
        private void invokeInplaceEditing() {
            if (startEditing == null) {
                Object o = tree.getActionMap().get("startEditing"); // NOI18N
                if (o != null && o instanceof Action) {
                    startEditing = (Action) o;
                }
            }
            assert startEditing != null : "startEditing is on tree ActionMap " + Arrays.asList(tree.getActionMap().keys());
            startEditing.actionPerformed(new ActionEvent(tree, 0, "startEditing")); // NOI18N
        }
    }
    
    private class SelectionListener implements PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                final Node [] nodes = (Node []) evt.getNewValue();
                boolean res = nodes != null && nodes.length > 0;
                int i = 0;
                while (res && i < nodes.length) {
                    Node n = nodes [i++];
                    Object node = n.getCookie(CatalogEntryNode.class);
                    res = node instanceof CatalogEntryNode && ((CatalogEntryNode)node).isCatalogWriter();
                }
                removeButton.setEnabled(res);
                if (nodes.length>0)  {
                    Object node = nodes[0].getCookie(CatalogNode.class);
                    addLocalButton.setEnabled(node instanceof CatalogNode 
                            && ((CatalogNode)node).getCatalogReader() instanceof CatalogWriter);
                }
            }
        }
    }
    
    private void initialize() {
        getExplorerManager().setRootContext(getCatalogRootNode());
        getExplorerManager().addPropertyChangeListener(new SelectionListener());
        removeButton.setEnabled(false);
        addButton.setEnabled(true);
        addLocalButton.setEnabled(false);
    }
    
    static Node getCatalogRootNode() {
        return new CatalogRootNode();
    }
    
    static private final class TemplateFilter implements DataFilter {
        public boolean acceptDataObject(DataObject obj) {
            return acceptTemplate(obj);
        }
        
        private boolean acceptTemplate(DataObject d) {
            if (d.isTemplate() || d instanceof DataFolder) {
                Object o = d.getPrimaryFile().getAttribute("simple"); // NOI18N
                return o == null || Boolean.TRUE.equals(o);
            } else {
                return false;
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

        templatesLabel = new javax.swing.JLabel();
        treePanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        addLocalButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();

        setPreferredSize(new java.awt.Dimension(500, 300));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(templatesLabel, org.openide.util.NbBundle.getBundle(CatalogPanel.class).getString("LBL_CatalogPanel_CatalogLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(templatesLabel, gridBagConstraints);

        treePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        treePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(treePanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getBundle(CatalogPanel.class).getString("BTN_CatalogPanel_Add")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(CatalogPanel.class, "ACD_CatalogPanel_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CatalogPanel.class, "ACD_CatalogPanel_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addLocalButton, org.openide.util.NbBundle.getBundle(CatalogPanel.class).getString("BTN_CatalogPanel_AddLocal")); // NOI18N
        addLocalButton.setToolTipText(org.openide.util.NbBundle.getMessage(CatalogPanel.class, "ACD_CatalogPanel_AddLocal")); // NOI18N
        addLocalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocalButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(addLocalButton, gridBagConstraints);
        addLocalButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CatalogPanel.class, "ACD_CatalogPanel_AddLocal")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getBundle(CatalogPanel.class).getString("BTN_CatalogPanel_RemoveButton")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CatalogPanel.class, "ACD_CatalogPanel_Remove")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonsPanel.add(jSeparator2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonsPanel.add(jSeparator3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 8);
        add(buttonsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    private void addLocalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocalButtonActionPerformed
        AddCatalogEntryAction.perform(manager.getSelectedNodes());
    }//GEN-LAST:event_addLocalButtonActionPerformed
    
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes();
        try {
            for(Node n : nodes) {
                n.destroy();
            }
        } catch (IOException e) {
            Logger.getLogger(CatalogPanel.class.getName()).log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    
    
    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        CatalogRootNode.mountCatalog(new Node[] {manager.getRootContext()});
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addLocalButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel templatesLabel;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables
    
    static private DataObject getDOFromNode(Node n) {
        DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
        assert dobj != null : "DataObject for node " + n;
        return dobj;
    }
    
    static private DataFolder getTargetFolder(Node [] nodes) {
        DataFolder folder = null;
        if (nodes == null || nodes.length == 0) {
            folder = DataFolder.findFolder(getCatalogRoot());
        } else {
            // try if has a data folder (alert: leaf node can be a empty folder)
            folder = (DataFolder) nodes [0].getLookup().lookup(DataFolder.class);
            
            // if not this node then try its parent
            if (folder == null && nodes [0].isLeaf()) {
                Node parent = nodes [0].getParentNode();
                folder = (DataFolder) parent.getLookup().lookup(DataFolder.class);
            }
        }
        return folder;
    }
    
    static DataObject createTemplateFromFile(File file, DataFolder preferred) {
        if (file == null) {
            throw new IllegalArgumentException("Argument file cannot be null!"); // NOI18N
        }
        FileObject sourceFO = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        assert sourceFO != null : "FileObject found for file " + file;
        DataObject sourceDO = null;
        try {
            sourceDO = DataObject.find(sourceFO);
        } catch (DataObjectNotFoundException donfe) {
            Logger.getLogger(CatalogPanel.class.getName()).log(Level.WARNING, null, donfe);
        }
        assert sourceDO != null : "DataObject found for FileObject " + sourceFO;
        DataFolder folder = preferred == null ? DataFolder.findFolder(getCatalogRoot()) : preferred;
        DataObject template = null;
        try {
            template = sourceDO.copy(folder);
            template.setTemplate(true);
        } catch (IOException ioe) {
            Logger.getLogger(CatalogPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        return template;
    }
    
    
    private static DataFolder doNewFolder(Node [] nodes) {
        DataFolder df = null;
        
        // new folder
        DataFolder pref = getTargetFolder(nodes);
        if (pref == null) {
            pref = DataFolder.findFolder(getCatalogRoot());
            assert pref != null : "DataFolder found for FO " + getCatalogRoot();
        }
        
        try {
            df = DataFolder.create(pref, NbBundle.getBundle(CatalogPanel.class).getString("TXT_CatalogPanel_NewFolderName")); // NOI18N
            assert df != null : "New subfolder found in folder " + pref;
        } catch (IOException ioe) {
            Logger.getLogger(CatalogPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        
        return df;
    }
    
    static DataObject createDuplicateFromNode(Node n) {
        DataObject source = getDOFromNode(n);
        try {
            return source.copy(source.getFolder());
        } catch (IOException ioe) {
            Logger.getLogger(CatalogPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        return null;
    }
    
    static FileObject getCatalogRoot() {
        if (catalogRoot == null) {
            catalogRoot = Repository.getDefault().getDefaultFileSystem().findResource("Catalog"); // NOI18N
        }
        return catalogRoot;
    }
    
    private int getNodePosition(Node n) {
        Index supp = getIndexSupport(n);
        
        DataFolder df = (DataFolder) n.getParentNode().getLookup().lookup(DataFolder.class);
        df.getNodeDelegate().getChildren().getNodes(true);
        
        int pos = supp.indexOf(n);
        
        assert pos != -1 : "Node " + n + " has position " + pos + " in children " + Arrays.asList(n.getParentNode().getChildren().getNodes());
        
        return pos;
    }
    
    private Index getIndexSupport(Node n) {
        Node parent = n.getParentNode();
        assert parent != null : "Node " + n + " has a parent.";
        
        Index index = (Index) parent.getLookup().lookup(Index.class);
        assert index != null : "Node " + parent + " has Index cookie.";
        
        return index;
    }
}
