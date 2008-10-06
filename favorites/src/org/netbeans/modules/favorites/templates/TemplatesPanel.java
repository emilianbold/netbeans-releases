/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.favorites.templates;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
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
import org.openide.util.Exceptions;
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
 * @author  Jiri Rechtacek
 */
public class TemplatesPanel extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager manager;
    private TemplateTreeView view;
    
    static private FileObject templatesRoot;
    
    /** Creates new form TemplatesPanel */
    public TemplatesPanel () {
        
        ActionMap map = getActionMap ();
        map.put (DefaultEditorKit.copyAction, ExplorerUtils.actionCopy (getExplorerManager ()));
        map.put (DefaultEditorKit.cutAction, ExplorerUtils.actionCut (getExplorerManager ()));
        map.put (DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste (getExplorerManager ()));
        map.put ("delete", ExplorerUtils.actionDelete (getExplorerManager (), true)); // NOI18N
        
        initComponents ();
        createTemplateView ();
        treePanel.add (view, BorderLayout.CENTER);
        
        associateLookup (ExplorerUtils.createLookup (getExplorerManager (), map));
        initialize ();
        
    }
    
    public ExplorerManager getExplorerManager () {
        if (manager == null) {
            manager = new ExplorerManager ();
        }
        return manager;
    }
    
    private void createTemplateView () {
        if (view == null) {
            view = new TemplateTreeView ();
        }
        view.setRootVisible (false);
        view.setPopupAllowed (true);
        view.setDefaultActionAllowed (false);
    }
    
    private static class TemplateTreeView extends BeanTreeView {
        private Action startEditing;
        private void invokeInplaceEditing () {
            if (startEditing == null) {
                Object o = tree.getActionMap ().get ("startEditing"); // NOI18N
                if (o != null && o instanceof Action) {
                    startEditing = (Action) o;
                }
            }
            assert startEditing != null : "startEditing is on tree ActionMap " + Arrays.asList (tree.getActionMap ().keys ());
            startEditing.actionPerformed (new ActionEvent (tree, 0, "startEditing")); // NOI18N
        }
    }
    
    private class SelectionListener implements PropertyChangeListener {
        public void propertyChange (java.beans.PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                final Node [] nodes = (Node []) evt.getNewValue ();
                deleteButton.setEnabled (nodes != null && nodes.length > 0);
                renameButton.setEnabled (nodes != null && nodes.length == 1);
                duplicateButton.setEnabled (nodes != null && nodes.length == 1 && nodes [0].isLeaf ());
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        moveUpButton.setEnabled (isMoveUpEnabled (nodes));
                        moveDownButton.setEnabled (isMoveDownEnabled (nodes));
                    }
                });
            }
        }
    }
    
    private void initialize () {
        getExplorerManager ().setRootContext (getTemplateRootNode ());
        getExplorerManager ().addPropertyChangeListener (new SelectionListener ());
        deleteButton.setEnabled (false);
        renameButton.setEnabled (false);
        duplicateButton.setEnabled (false);
        moveUpButton.setEnabled (false);
        moveDownButton.setEnabled (false);
        addButton.setEnabled (true);
    }
    
    static Node getTemplateRootNode () {
        DataFolder df = DataFolder.findFolder (getTemplatesRoot ());
        try {
            df.setOrder (orderFolders (df.getChildren ()));
        } catch (IOException ex) {
            Exceptions.printStackTrace (ex);
        }
        return new TemplateNode (new FilterNode (df.getNodeDelegate (), df.createNodeChildren (new TemplateFilter ())));
    }
    
    private static DataObject [] orderFolders (DataObject [] original) {
        SortedSet<DataObject> sorted = new TreeSet<DataObject> (new Comparator<DataObject> () {
            public int compare (DataObject o1, DataObject o2) {
                int res = o1.getNodeDelegate ().getDisplayName ().compareTo (o2.getNodeDelegate ().getDisplayName ());
                // compare primary files if display names are equals
                if (res == 0 && o1 != o2) {
                    res = o1.getPrimaryFile ().getPath ().compareTo (o2.getPrimaryFile ().getPath ());
                }
                return res;
            }
        });
        for (DataObject o : original) {
            sorted.add (o);
        }
        return sorted.toArray (new DataObject [0]);
    }
    
    private static final class TemplateFilter implements DataFilter {
        public boolean acceptDataObject (DataObject obj) {
            return acceptTemplate (obj);
        }

        private boolean acceptTemplate (DataObject d) {
            if (d.isTemplate () || d instanceof DataFolder) {
                Object o = d.getPrimaryFile ().getAttribute ("simple"); // NOI18N
                return o == null || Boolean.TRUE.equals (o);
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
        newFolderButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        duplicateButton = new javax.swing.JButton();
        renameButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        deleteButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(500, 300));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(templatesLabel, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("LBL_TemplatesPanel_TemplatesLabel")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_Add")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "TT_TemplatesPanel_Add")); // NOI18N
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
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(newFolderButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_NewFolder")); // NOI18N
        newFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(newFolderButton, gridBagConstraints);
        newFolderButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_New")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonsPanel.add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(duplicateButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_DuplicateButton")); // NOI18N
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(duplicateButton, gridBagConstraints);
        duplicateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_Duplicate")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(renameButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_RenameButton")); // NOI18N
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(renameButton, gridBagConstraints);
        renameButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_Rename")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonsPanel.add(jSeparator2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_MoveUp")); // NOI18N
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(moveUpButton, gridBagConstraints);
        moveUpButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_MoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_MoveDown")); // NOI18N
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(moveDownButton, gridBagConstraints);
        moveDownButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_MoveDown")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonsPanel.add(jSeparator3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_Delete")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(deleteButton, gridBagConstraints);
        deleteButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_Delete")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 11, 8);
        add(buttonsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void moveDownButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        moveDown (manager.getSelectedNodes ());//GEN-LAST:event_moveDownButtonActionPerformed
    }                                              

    private void moveUpButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        moveUp (manager.getSelectedNodes ());//GEN-LAST:event_moveUpButtonActionPerformed
    }                                            
    
    private void newFolderButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFolderButtonActionPerformed

        DataFolder df = doNewFolder (manager.getSelectedNodes ());
        assert df != null : "New DataFolder has been created.";
        
        try {
            // invoke inplace editing
            Node [] nodes = manager.getSelectedNodes ();
            Node targerNode = null;
            if (nodes == null || nodes.length == 0) {
                targerNode = manager.getRootContext ();
            } else {
                targerNode = nodes [0].isLeaf () ? nodes [0].getParentNode () : nodes [0];
            }
            
            targerNode.getChildren ().getNodes (true);
            Node newSubfolder = targerNode.getChildren ().findChild (df.getName ());
            assert newSubfolder != null : "Node for subfolder found in nodes: " + Arrays.asList (targerNode.getChildren ().getNodes ());
            manager.setSelectedNodes (new Node [] { newSubfolder });
            view.invokeInplaceEditing ();
        } catch (PropertyVetoException pve) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, pve);//GEN-LAST:event_newFolderButtonActionPerformed
        }
                                               
    }                                               
    
    private void deleteButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes (); 
        for (int i = 0; i < nodes.length; i++) {
            try {
                nodes[i].destroy();
            }
            catch (IOException ioe) {
                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);//GEN-LAST:event_deleteButtonActionPerformed
            }
        }                                            
    }                                            

    private void duplicateButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes ();
        assert nodes != null : "Selected Nodes cannot be null.";
        assert nodes.length == 1 : "One one node can be selected, but was " + Arrays.asList (nodes);
        createDuplicateFromNode (nodes [0]);//GEN-LAST:event_duplicateButtonActionPerformed
                                               
    }                                               
    
    private void renameButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes ();
        assert nodes != null : "Selected Nodes cannot be null.";
        assert nodes.length == 1 : "One one node can be selected, but was " + Arrays.asList (nodes);//GEN-LAST:event_renameButtonActionPerformed
        view.invokeInplaceEditing ();                                            
    }                                            
    
    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        doAdd (manager.getSelectedNodes ());//GEN-LAST:event_addButtonActionPerformed
    }                                         
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JButton renameButton;
    private javax.swing.JLabel templatesLabel;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables
    
    private static class TemplateNode extends FilterNode {

        private static Action [] ACTIONS_ON_LEAF = new Action [] {
                                    SystemAction.get (CutAction.class),
                                    SystemAction.get (CopyAction.class),
                                    SystemAction.get (PasteAction.class),
                                    null,
                                    SystemAction.get (DeleteAction.class),
                                    SystemAction.get (RenameAction.class),
        };
        
        private static Action [] ACTIONS_ON_FOLDER = new Action [] {
                                    SystemAction.get (AddAction.class),
                                    SystemAction.get (NewFolderAction.class),
                                    null,
                                    SystemAction.get (CutAction.class),
                                    SystemAction.get (CopyAction.class),
                                    SystemAction.get (PasteAction.class),
                                    null,
                                    SystemAction.get (DeleteAction.class),
                                    SystemAction.get (RenameAction.class),
        };
        
        public TemplateNode (Node n) { 
            this (n, new DataFolderFilterChildren (n), new InstanceContent ());
        }
        
        private TemplateNode (Node n, org.openide.nodes.Children ch) { 
            this (n, ch, new InstanceContent ());
        }
        
        private TemplateNode (Node originalNode, org.openide.nodes.Children ch, InstanceContent content) {
            super (originalNode, ch,
                   new ProxyLookup (new Lookup [] { new AbstractLookup (content), originalNode.getLookup () } )
                   );

            DataObject dobj = getDOFromNode (originalNode);
            
            // #69623: IllegalArgumentException when call getFolder() on an unvalid DataObject
            if (dobj.isValid ()) {
                DataFolder folder = null;
                if (dobj instanceof DataFolder) {
                    folder = (DataFolder) dobj;
                } else {
                    // check parent
                    if (dobj.getPrimaryFile ().getParent () != null && dobj.getPrimaryFile ().getParent ().isValid ()) {
                        folder = dobj.getFolder ();
                    }
                }
                if (folder != null) {
                    content.add (new DataFolder.Index (folder, this));
                }
            }
            
            content.add (this);
        }
        @Override
        public Action [] getActions (boolean context) {
            return isLeaf () ? ACTIONS_ON_LEAF : ACTIONS_ON_FOLDER;
        }
        
        @Override
        public Action getPreferredAction () {
            return null;
        }

        @Override
        public void setName(String name) {
            // #140308 - get attributtes before rename and set them for renamed FileObject
            FileObject fo = this.getLookup().lookup(FileObject.class);
            final HashMap<String, Object> attributes = getAttributes(fo);
            super.setName(name);
            try {
                setAttributes (fo, attributes);
            } catch (IOException ex) {
                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ex);
            }
        }
    }
    
    private static class DataFolderFilterChildren extends FilterNode.Children {
        public DataFolderFilterChildren (Node n) {
            super (n);
        }
        
        @Override
        protected Node[] createNodes(Node key) {
            Node [] orig = super.createNodes (key);
            Node [] filtered = new Node [orig.length];
            for (int i = 0; i < orig.length; i++) {
                DataObject dobj = getDOFromNode (orig [i]);
                if (dobj.isTemplate ()) {
                    filtered [i] = new TemplateNode (orig [i], Children.LEAF);
                } else {
                    filtered [i] = new TemplateNode (orig [i]);
                }
            }
            return filtered;
        }
        
    }
    
    static private DataObject getDOFromNode (Node n) {
        DataObject dobj = n.getLookup ().lookup (DataObject.class);
        assert dobj != null : "DataObject for node " + n;
        return dobj;
    }
    
    static private DataFolder getTargetFolder (Node [] nodes) {
        DataFolder folder = null;
        if (nodes == null || nodes.length == 0) {
            folder = DataFolder.findFolder (getTemplatesRoot ());
        } else {
            // try if has a data folder (alert: leaf node can be a empty folder)
            folder = nodes[0].getLookup ().lookup (DataFolder.class);
            
            // if not this node then try its parent
            if (folder == null && nodes [0].isLeaf ()) {
                Node parent = nodes [0].getParentNode ();
                folder = parent.getLookup ().lookup (DataFolder.class);
            }
        }
        return folder;
    }
    
    static DataObject createTemplateFromFile (File file, DataFolder preferred) {
        if (file == null) {
            throw new IllegalArgumentException ("Argument file cannot be null!"); // NOI18N
        }
        FileObject sourceFO = FileUtil.toFileObject (FileUtil.normalizeFile (file));
        assert sourceFO != null : "FileObject found for file " + file;
        DataObject sourceDO = null;
        try {
            sourceDO = DataObject.find (sourceFO);
        } catch (DataObjectNotFoundException donfe) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, donfe);
        }
        assert sourceDO != null : "DataObject found for FileObject " + sourceFO;
        DataFolder folder = preferred == null ? DataFolder.findFolder (getTemplatesRoot ()) : preferred;
        DataObject template = null;
        try {
            template = sourceDO.copy (folder);
            template.setTemplate (true);
        } catch (IOException ioe) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        return template;
    }
    
    private static void doAdd (Node [] nodes) {
        JFileChooser chooser = new JFileChooser ();
        chooser.setDialogTitle (NbBundle.getBundle(TemplatesPanel.class).getString("LBL_TemplatesPanel_JFileChooser_Title")); // NOI18N
        chooser.setApproveButtonText (NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_JFileChooser_AddButtonName")); // NOI18N
        chooser.setFileHidingEnabled (false);
        chooser.setMultiSelectionEnabled (false);
        int result = chooser.showOpenDialog (null);
        if (JFileChooser.APPROVE_OPTION == result) {
            File f = chooser.getSelectedFile ();
            assert f != null;
            createTemplateFromFile (f, getTargetFolder (nodes));
        }    
    }
    
    private static DataFolder doNewFolder (Node [] nodes) {
        DataFolder df = null;
        
        // new folder
        DataFolder pref = getTargetFolder (nodes);
        if (pref == null) {
            pref = DataFolder.findFolder (getTemplatesRoot ());
            assert pref != null : "DataFolder found for FO " + getTemplatesRoot ();
        }
        
        try {
            df = DataFolder.create (pref, NbBundle.getBundle(TemplatesPanel.class).getString("TXT_TemplatesPanel_NewFolderName")); // NOI18N
            assert df != null : "New subfolder found in folder " + pref;
        } catch (IOException ioe) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        
        return df;
    }
    
    static DataObject createDuplicateFromNode (Node n) {
        DataObject source = getDOFromNode (n);
        try {
            DataObject target = source.copy(source.getFolder());
            FileObject srcFo = source.getPrimaryFile();
            FileObject targetFo = target.getPrimaryFile();
            setAttributes(targetFo, getAttributes(srcFo));
            return target;
        } catch (IOException ioe) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        return null;
    }
    
    /** Returns map of attributes for given FileObject. */
    private static HashMap<String, Object> getAttributes(FileObject fo) {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        Enumeration<String> attributeNames = fo.getAttributes();
        while(attributeNames.hasMoreElements()) {
            String attrName = attributeNames.nextElement();
            if (attrName == null) {
                continue;
            }
            Object attrValue = fo.getAttribute(attrName);
            if (attrValue != null) {
                attributes.put(attrName, attrValue);
            }
        }
        return attributes;
    }

    /** Sets attributes for given FileObject. */
    private static void setAttributes(FileObject fo, HashMap<String, Object> attributes) throws IOException {
        for (Entry<String, Object> entry : attributes.entrySet()) {
            fo.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    static FileObject getTemplatesRoot () {
        if (templatesRoot == null) {
            templatesRoot = Repository.getDefault ().getDefaultFileSystem ().findResource ("Templates"); // NOI18N
        }
        return templatesRoot;
    }
    
    private boolean isMoveUpEnabled (Node [] nodes) {
        if (nodes == null || nodes.length != 1 || ! nodes [0].isLeaf ()) {
            return false;
        }
        
        int pos = getNodePosition (nodes [0]);
        return pos != -1 && pos > 0;
    }
    
    private boolean isMoveDownEnabled (Node [] nodes) {
        if (nodes == null || nodes.length != 1 || ! nodes [0].isLeaf ()) {
            return false;
        }
        int count = nodes [0].getParentNode ().getChildren ().getNodesCount ();
        int pos = getNodePosition (nodes [0]);
        return pos != -1 && pos < (count - 1);
    }
    
    private int getNodePosition (Node n) {
        Index supp = getIndexSupport (n);

        DataFolder df = n.getParentNode ().getLookup ().lookup (DataFolder.class);
        df.getNodeDelegate ().getChildren ().getNodes (true);

        int pos = supp.indexOf (n);          

        // #141851: getNodes()/getNodePosition() is not called under Children.MUTEX 
        // therefore it is not guaranteed that node will be found (node could be deleted meanwhile)
        // assert pos != -1 : "Node " + n + " has position " + pos + " in children " + Arrays.asList (n.getParentNode ().getChildren ().getNodes ());

        return pos;
    }
    
    private Index getIndexSupport (Node n) {
        Node parent = n.getParentNode ();
        assert parent != null : "Node " + n + " has a parent.";

        Index index = parent.getLookup ().lookup (Index.class);
        assert index != null : "Node " + parent + " has Index cookie.";
        
        return index;
    }
    
    private void moveUp (Node[] nodes) {
        assert nodes != null : "Nodes to moveUp cannot be null.";
        assert nodes.length == 1 : "Only one node can be moveUp, not " + Arrays.asList (nodes);
        assert nodes [0].isLeaf () : "Only leaf node can be moveUp, not " + nodes [0];
        Node n = nodes  [0];
        
        Index supp = getIndexSupport (n);
        int origPos = getNodePosition (n);
        
        // workaround issue 62192, don't try to move on broken index
        if (origPos == -1) {
            return ;
        }
        
        supp.moveUp (origPos);
        assert origPos - 1 == getNodePosition (n) : "Node " + n + " has been moved from " + origPos + " to pos " + getNodePosition (n);
    }
    
    private void moveDown (Node[] nodes) {
        assert nodes != null : "Nodes to moveDown cannot be null.";
        assert nodes.length == 1 : "Only one node can be moveDown, not " + Arrays.asList (nodes);
        assert nodes [0].isLeaf () : "Only leaf node can be moveDown, not " + nodes [0];
        Node n = nodes  [0];
        
        Index supp = getIndexSupport (n);
        int origPos = getNodePosition (n);
        
        // workaround issue 62192, don't try to move on broken index
        if (origPos == -1) {
            return ;
        }
        
        supp.moveDown (origPos);
        assert origPos + 1 == getNodePosition (n) : "Node " + n + " has been moved from " + origPos + " to pos " + getNodePosition (n);
    }
    
    // action
    private static class AddAction extends NodeAction {
        protected void performAction (Node[] activatedNodes) {
            doAdd (activatedNodes);
        }

        protected boolean enable (Node[] activatedNodes) {
            return activatedNodes != null && activatedNodes.length == 1;
        }

        public String getName () {
            return NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_Add"); // NOI18N
        }

        public HelpCtx getHelpCtx () {
            return null;
        }
        
        @Override
        protected boolean asynchronous () {
            return true;
        }
    }
    
    private static class NewFolderAction extends NodeAction {
        protected void performAction (Node[] activatedNodes) {
            doNewFolder (activatedNodes);
        }

        protected boolean enable (Node[] activatedNodes) {
            return activatedNodes != null && activatedNodes.length == 1;
        }

        public String getName () {
            return NbBundle.getBundle(TemplatesPanel.class).getString("BTN_TemplatesPanel_NewFolder"); // NOI18N
        }

        public HelpCtx getHelpCtx () {
            return null;
        }
        
        @Override
        protected boolean asynchronous () {
            return true;
        }
    }
    
}
