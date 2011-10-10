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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
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
    private static ExplorerManager manager;
    private static TemplateTreeView view;
    private static final RequestProcessor rp = new RequestProcessor("Templates", 1);
    
    static private FileObject templatesRoot;
    private static Node templatesRootNode = null;
    
    private static final String TEMPLATE_DISPLAY_NAME_ATTRIBUTE = "displayName"; // NOI18N
    private static final String TEMPLATE_LOCALIZING_BUNDLE_ATTRIBUTE = "SystemFileSystem.localizingBundle"; // NOI18N
    private static final String TEMPLATE_SCRIPT_ENGINE_ATTRIBUTE = "javax.script.ScriptEngine"; // NOI18N
    private static final String TEMPLATE_CATEGORY_ATTRIBUTE = "templateCategory"; // NOI18N

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
        view.setPreferredSize(new Dimension(350, 250));
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
                addButton.setEnabled (nodes != null && nodes.length == 1);
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
        addButton.setEnabled (false);
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                Node[] nodes = templatesRootNode.getChildren ().getNodes(true);
                if (nodes.length > 0) {
                    Node firstNode = nodes[0];
                    try {
                        manager.setSelectedNodes (new Node[]{firstNode});
                    } catch (PropertyVetoException ex) {
                        Logger.getLogger(TemplatesPanel.class.getName()).log(Level.FINE, ex.getLocalizedMessage (), ex);
                    }
                }
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        view.requestFocus ();
                    }
                });
            }
        });
    }
    
    static Node getTemplateRootNode () {
        if (templatesRootNode == null) {
            DataFolder df = DataFolder.findFolder (getTemplatesRoot ());
            templatesRootNode = new TemplateNode (new FilterNode (df.getNodeDelegate (), df.createNodeChildren (new TemplateFilter ())));
        }
        return templatesRootNode;
    }
    
    private static final class TemplateFilter implements DataFilter {
        public boolean acceptDataObject (DataObject obj) {
            return acceptTemplate (obj);
        }

        private boolean acceptTemplate (DataObject d) {
            if (d instanceof DataFolder &&
                "Templates/Properties".equals(d.getPrimaryFile().getPath())) {
                
                return false;
            }
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        jSeparator4 = new javax.swing.JSeparator();
        settingsButton = new javax.swing.JButton();

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
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(deleteButton, gridBagConstraints);
        deleteButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "ACD_TemplatesPanel_Delete")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 0);
        buttonsPanel.add(jSeparator4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(settingsButton, org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "BTN_TemplatesPanel_Settings")); // NOI18N
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 3, 0);
        buttonsPanel.add(settingsButton, gridBagConstraints);

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
        moveDown (manager.getSelectedNodes ());
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        moveUp (manager.getSelectedNodes ());
    }//GEN-LAST:event_moveUpButtonActionPerformed
    
    private void newFolderButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFolderButtonActionPerformed

        final Node [] nodes = manager.getSelectedNodes ();
        rp.post(new Runnable() {
            public void run() {
                DataFolder df = doNewFolder (nodes);
                assert df != null : "New DataFolder can not be created under "+Arrays.toString(nodes);

                // invoke inplace editing
                Node targerNode;
                if (nodes == null || nodes.length == 0) {
                    targerNode = manager.getRootContext ();
                } else {
                    targerNode = nodes [0].isLeaf () ? nodes [0].getParentNode () : nodes [0];
                }

                final Node newSubfolder = findChild (targerNode, df.getName (), 3);
                assert newSubfolder != null : "Node for subfolder found in nodes: " + Arrays.asList (targerNode.getChildren ().getNodes ());
                if (newSubfolder != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                manager.setSelectedNodes (new Node [] { newSubfolder });
                            } catch (PropertyVetoException pve) {
                                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, pve);
                            }
                            view.invokeInplaceEditing ();
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_newFolderButtonActionPerformed
    
    private Node findChild(Node node, String name, int i) {
        node.getChildren ().getNodes (true);
        Node newSubfolder = node.getChildren ().findChild (name);
        if (newSubfolder == null && i > 0) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
            }
            newSubfolder = findChild(node, name, i--);
        }
        return newSubfolder;
    }

    private void deleteButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes (); 
        for (int i = 0; i < nodes.length; i++) {
            try {
                nodes[i].destroy();
            }
            catch (IOException ioe) {
                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void duplicateButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes ();
        assert nodes != null : "Selected Nodes cannot be null.";
        assert nodes.length == 1 : "One one node can be selected, but was " + Arrays.asList (nodes);
        createDuplicateFromNode (nodes [0]);
    }//GEN-LAST:event_duplicateButtonActionPerformed
    
    private void renameButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
        Node [] nodes = manager.getSelectedNodes ();
        assert nodes != null : "Selected Nodes cannot be null.";
        assert nodes.length == 1 : "One one node can be selected, but was " + Arrays.asList (nodes);
        showRename((TemplateNode) nodes[0]);
    }//GEN-LAST:event_renameButtonActionPerformed
    
    private void addButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        doAdd (manager.getSelectedNodes ());
    }//GEN-LAST:event_addButtonActionPerformed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        FileObject dir = FileUtil.getConfigFile("Templates/Properties");
        if (dir == null) {
            settingsButton.setEnabled(false);
            return ;
        }
        for (Enumeration<? extends FileObject> en = dir.getChildren(true); en.hasMoreElements(); ) {
            FileObject fo = en.nextElement();
            try {
                DataObject dobj = DataObject.find(fo);
                EditCookie ec = dobj.getCookie(EditCookie.class);
                if (ec != null) {
                    ec.edit ();
                } else {
                    OpenCookie oc = dobj.getCookie(OpenCookie.class);
                    if (oc != null) {
                        oc.open ();
                    } else {
                        continue;
                    }
                }
                // Close the Templates dialog
                closeDialog(this);
            } catch (DataObjectNotFoundException ex) {
                continue;
            }
        }
    }//GEN-LAST:event_settingsButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JButton renameButton;
    private javax.swing.JButton settingsButton;
    private javax.swing.JLabel templatesLabel;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables
    
    private void closeDialog(java.awt.Component c) {
        if (c instanceof JDialog) {
            ((JDialog) c).setVisible(false);
        } else {
            c = c.getParent();
            if (c != null) {
                closeDialog(c);
            }
        }
    }

    private static class TemplateNode extends FilterNode {

        private static Action [] ACTIONS_ON_LEAF = new Action [] {
                                    SystemAction.get (CutAction.class),
                                    SystemAction.get (CopyAction.class),
                                    SystemAction.get (PasteAction.class),
                                    null,
                                    SystemAction.get (DeleteAction.class),
                                    SystemAction.get (RenameTemplateAction.class),
                                    null,
                                    SystemAction.get (PropertiesAction.class),
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
                                    SystemAction.get (RenameTemplateAction.class),
        };

        public TemplateNode (Node n) { 
            this (n, new DataFolderFilterChildren (n), new LazyLookup ());
        }
        
        private TemplateNode (Node n, org.openide.nodes.Children ch) { 
            this (n, ch, new LazyLookup ());
        }
        
        private TemplateNode (final Node originalNode, org.openide.nodes.Children ch, LazyLookup contentLookup) {
            super (originalNode, ch,
                   new ProxyLookup (new Lookup [] { contentLookup, originalNode.getLookup () } )
                   );

            contentLookup.setInstanceGetter(Index.class, new LazyLookup.InstanceGetter<Index>() {

                @Override
                public Index getInstance() {
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
                            return new DataFolder.Index (folder, TemplateNode.this);
                        }
                    }
                    return null;
                }
            });
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
        public String getName () {
            return super.getDisplayName ();
        }

        @Override
        public void setName(String name) {
            FileObject fo = this.getLookup().lookup(FileObject.class);
            try {
                fo.setAttribute (TEMPLATE_DISPLAY_NAME_ATTRIBUTE, name);
                fo.setAttribute (TEMPLATE_LOCALIZING_BUNDLE_ATTRIBUTE, null);
            } catch (IOException ex) {
                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ex);
            }
            setDisplayName (name);
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public PropertySet[] getPropertySets () {
            return new Node.PropertySet [] { createTemplateProperties (this) };
        }

        public String getFileName () {
            return super.getName ();
        }

        public void setFileName (String name) {
            String origDisplayName = getDisplayName ();
            super.setName (name);
            FileObject fo = this.getLookup().lookup(FileObject.class);
            try {
                fo.setAttribute (TEMPLATE_DISPLAY_NAME_ATTRIBUTE, origDisplayName);
                fo.setAttribute (TEMPLATE_LOCALIZING_BUNDLE_ATTRIBUTE, null);
            } catch (IOException ex) {
                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ex);
            }
            setDisplayName (origDisplayName);
        }

    }

    private static class RenameTemplateAction extends NodeAction {

        @Override
        protected boolean surviveFocusChange() {
            return false;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(RenameTemplateAction.class, "Action_Rename");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(RenameTemplateAction.class);
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            // exactly one node should be selected
            if ((activatedNodes == null) || (activatedNodes.length != 1)) {
                return false;
            }

            return true;
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            TemplateNode n = (TemplateNode) activatedNodes[0]; // we supposed that one node is activated
            showRename(n);
        }

    }

    private static Sheet.Set createTemplateProperties (final TemplateNode templateNode) {
        Sheet.Set properties = Sheet.createPropertiesSet ();
        // display name
        properties.put (new PropertySupport.ReadWrite<String> (
                    DataObject.PROP_NAME,
                    String.class,
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_DisplayName"), // NOI18N
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_DisplayName_Desc") // NOI18N
                ) {
                    @Override
                    public String getValue () throws IllegalAccessException, InvocationTargetException {
                        return templateNode.getDisplayName ();
                    }

                    @Override
                    public void setValue (String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        templateNode.setName (val);
                    }
        });
        // name == primary file filename
        properties.put (new PropertySupport.ReadWrite<String> (
                    DataObject.PROP_PRIMARY_FILE,
                    String.class,
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_FileName"), // NOI18N
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_FileName_Desc") // NOI18N
                ) {
                    @Override
                    public String getValue () throws IllegalAccessException, InvocationTargetException {
                        return templateNode.getFileName ();
                    }

                    @Override
                    public void setValue (String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        templateNode.setFileName (val);
                    }
        });
        // ScriptEngine
        properties.put (new PropertySupport.ReadWrite<String> (
                    TEMPLATE_SCRIPT_ENGINE_ATTRIBUTE,
                    String.class,
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_ScriptEngine"), // NOI18N
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_ScriptEngine_Desc") // NOI18N
                ) {
                    @Override
                    public String getValue () throws IllegalAccessException, InvocationTargetException {
                        DataObject dobj = getDOFromNode (templateNode);
                        Object o = dobj.getPrimaryFile ().getAttribute (TEMPLATE_SCRIPT_ENGINE_ATTRIBUTE);
                        return o == null ? "" : o.toString ();
                    }

                    @Override
                    public void setValue (String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        DataObject dobj = getDOFromNode (templateNode);
                        try {
                            dobj.getPrimaryFile ().setAttribute (TEMPLATE_SCRIPT_ENGINE_ATTRIBUTE, val);
                        } catch (IOException ex) {
                            Logger.getLogger(TemplatesPanel.class.getName()).log (Level.INFO, ex.getLocalizedMessage (), ex);
                        }
                    }
        });
        properties.put (new PropertySupport.ReadWrite<String []> (
                    TEMPLATE_CATEGORY_ATTRIBUTE,
                    String [].class,
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_TemplateCategories"), // NOI18N
                    NbBundle.getMessage (TemplatesPanel.class, "TemplatesPanel_TemplateNode_TemplateCategories_Desc") // NOI18N
                ) {
                    @Override
                    public String [] getValue () throws IllegalAccessException, InvocationTargetException {
                        DataObject dobj = getDOFromNode (templateNode);
                        Object o = dobj.getPrimaryFile ().getAttribute (TEMPLATE_CATEGORY_ATTRIBUTE);
                        if (o != null) {
                            List<String> list = new ArrayList<String> ();
                            StringTokenizer tokenizer = new StringTokenizer (o.toString (), ","); // NOI18N
                            while (tokenizer.hasMoreTokens ()) {
                                String token = tokenizer.nextToken ();
                                list.add (token.trim ());
                            }
                            return list.toArray (new String [0]);
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public void setValue (String [] val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                        DataObject dobj = getDOFromNode (templateNode);
                        try {
                            if (val == null) {
                                dobj.getPrimaryFile ().setAttribute (TEMPLATE_CATEGORY_ATTRIBUTE, null);
                            } else {
                                String list = Arrays.asList (val).toString();
                                list = list.substring(1, list.length() - 1);
                                dobj.getPrimaryFile ().setAttribute (TEMPLATE_CATEGORY_ATTRIBUTE, list);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(TemplatesPanel.class.getName()).log (Level.INFO, ex.getLocalizedMessage (), ex);
                        }
                    }
        });

        return properties;
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
                FileObject fo = orig[i].getLookup ().lookup(FileObject.class);
                boolean isTemplate;
                if (fo != null) {
                    isTemplate = isTemplate(fo);
                } else {
                    DataObject dobj = getDOFromNode (orig [i]);
                    isTemplate = dobj.isTemplate();
                }
                if (isTemplate) {
                    filtered [i] = new TemplateNode (orig [i], Children.LEAF);
                } else {
                    filtered [i] = new TemplateNode (orig [i]);
                }
            }
            return filtered;
        }
        
        private boolean isTemplate(FileObject obj) {
            Object o = obj.getAttribute(DataObject.PROP_TEMPLATE);
            boolean ret = false;
            if (o instanceof Boolean)
                ret = ((Boolean) o).booleanValue();
            return ret;
        }

        @Override
        protected void addNotify () {
            super.addNotify ();
            if (getTemplateRootNode ().equals (this.getNode ())) {
                sortNodes ();
            }
        }

        @Override
        protected void filterChildrenAdded (NodeMemberEvent ev) {
            super.filterChildrenAdded (ev);
            if (getTemplateRootNode ().equals (this.getNode ())) {
                sortNodes ();
            }
        }

        @Override
        protected void filterChildrenRemoved (NodeMemberEvent ev) {
            super.filterChildrenRemoved (ev);
            if (getTemplateRootNode ().equals (this.getNode ())) {
                sortNodes ();
            }
        }

        @Override
        protected void filterChildrenReordered (NodeReorderEvent ev) {
            super.filterChildrenReordered (ev);
            if (getTemplateRootNode ().equals (this.getNode ())) {
                sortNodes ();
            }
        }

        private void sortNodes () {
            Node [] originalNodes = original.getChildren ().getNodes ();
            Collection<Node> sortedNodes = new TreeSet<Node> (new TemplateCategotyComparator ());
            for (Node n : originalNodes) {
                sortedNodes.add (n);
            }
            setKeys (sortedNodes.toArray (new Node[0]));
        }

        private static final class TemplateCategotyComparator implements Comparator<Node> {
            public int compare (Node o1, Node o2) {
                return o1.getDisplayName ().compareToIgnoreCase (o2.getDisplayName ());
            }
        }
    }

    static private DataObject getDOFromNode (Node n) {
        DataObject dobj = n.getLookup ().lookup (DataObject.class);
        if (dobj == null) {
            throw new NullPointerException("DataObject can not be found for node " + n);
        }
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
            DataObject templateSample = null;
            for (DataObject d : folder.getChildren ()) {
                if (d.isTemplate ()) {
                    templateSample = d;
                    break;
                }
            }
            template.setTemplate(true);
            if (templateSample == null) {
                // a fallback if no template sample found
                template.getPrimaryFile ().setAttribute (TEMPLATE_SCRIPT_ENGINE_ATTRIBUTE, "freemarker"); // NOI18N
            } else {
                setTemplateAttributes (template.getPrimaryFile (), templateSample.getPrimaryFile ());
            }
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
            if (! f.isFile()) {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(NbBundle.getMessage(TemplatesPanel.class, "MSG_TemplatesPanel_Nonexistent_File", f.toString()));
                DialogDisplayer.getDefault().notify(msg);
            } else {
                createTemplateFromFile (f, getTargetFolder (nodes));
            }
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

        //#161963: Create new DataFolder if DataFolder with given name already exists
        String baseName = NbBundle.getBundle(TemplatesPanel.class).getString("TXT_TemplatesPanel_NewFolderName");
        String name = baseName;
        DataObject [] arr = pref.getChildren();
        boolean exists = true;
        int counter = 0;
        while (exists) {
            exists = false;
            for (int i = 0; i < arr.length; i++) {
                if (name.equals(arr[i].getName())) {
                    counter++;
                    name = baseName + " " + counter;
                    exists = true;
                    break;
                }
            }
        }
        try {
            df = DataFolder.create (pref, name);
            assert df != null : "New subfolder found in folder " + pref;
        } catch (IOException ioe) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        
        return df;
    }
    
    static DataObject createDuplicateFromNode (Node n) {
        DataObject source = getDOFromNode (n);
        try {
            Node parent = n.getParentNode ();
            DataObject target = source.copy(source.getFolder());
            FileObject srcFo = source.getPrimaryFile();
            FileObject targetFo = target.getPrimaryFile();
            setTemplateAttributes(targetFo, srcFo);
            if (parent != null) {
                Node duplicateNode = null;
                for (Node k : parent.getChildren ().getNodes (true)) {
                    if (k.getName ().startsWith (targetFo.getName ())) {
                        duplicateNode = k;
                        break;
                    }
                }
                if (duplicateNode != null) {
                    final Node finalNode = duplicateNode;
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            try {
                                manager.setSelectedNodes (new Node [] { finalNode });
                                view.invokeInplaceEditing ();
                            } catch (PropertyVetoException ex) {
                                Logger.getLogger (TemplatesPanel.class.getName ()).log (Level.INFO, ex.getLocalizedMessage (), ex);
                            }
                        }
                    });
                }
            }
            return target;
        } catch (IOException ioe) {
            Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ioe);
        }
        return null;
    }
    
    private static void setTemplateAttributes(FileObject fo, FileObject from) throws IOException {
        FileUtil.copyAttributes(from, fo);
        fo.setAttribute(TEMPLATE_LOCALIZING_BUNDLE_ATTRIBUTE, null);
    }

    static FileObject getTemplatesRoot () {
        if (templatesRoot == null) {
            templatesRoot = FileUtil.getConfigFile("Templates"); // NOI18N
        }
        return templatesRoot;
    }
    
    private boolean isMoveUpEnabled (Node [] nodes) {
        if (nodes == null || nodes.length != 1 || ! nodes [0].isLeaf ()) {
            return false;
        }
        
        Node parent = nodes [0].getParentNode ();
        if (parent == null) {
            return false;
        }
        int pos = getNodePosition (nodes [0]);
        return pos != -1 && pos > 0;
    }
    
    private boolean isMoveDownEnabled (Node [] nodes) {
        if (nodes == null || nodes.length != 1 || ! nodes [0].isLeaf ()) {
            return false;
        }
        Node parent = nodes [0].getParentNode ();
        if (parent == null) {
            return false;
        }
        int count = parent.getChildren ().getNodesCount ();
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
        // getNodePosition() is not really reliable here.
        // assert origPos - 1 == getNodePosition (n) : "Node " + n + " has been moved from " + origPos + " to pos " + getNodePosition (n);
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
        // getNodePosition() is not really reliable here.
        // assert origPos + 1 == getNodePosition (n) : "Node " + n + " has been moved from " + origPos + " to pos " + getNodePosition (n);
    }

    private static void showRename(TemplateNode n) {
        String name = n.getFileName();
        String displayName = n.getDisplayName();
        FileObject fo = n.getLookup().lookup(FileObject.class);
        RenameTemplatePanel editPanel = new RenameTemplatePanel(isUserFile(fo));
        editPanel.setFileName(name);
        editPanel.setFileDisplayName(displayName);
        String title = org.openide.util.NbBundle.getMessage(TemplatesPanel.class, "RenameTemplatePanel.title.text");
        DialogDescriptor dd = new DialogDescriptor(editPanel, title);
        Object res = DialogDisplayer.getDefault().notify(dd);
        if (DialogDescriptor.OK_OPTION.equals(res)) {
            name = editPanel.getFileName();
            displayName = editPanel.getFileDisplayName();
            n.setFileName(name);
            try {
                fo.setAttribute (TEMPLATE_DISPLAY_NAME_ATTRIBUTE, displayName);
                fo.setAttribute (TEMPLATE_LOCALIZING_BUNDLE_ATTRIBUTE, null);
            } catch (IOException ex) {
                Logger.getLogger(TemplatesPanel.class.getName()).log(Level.WARNING, null, ex);
            }
            n.setDisplayName(displayName);
        }
    }

    /** Test if the file physically exists on disk and thus was created by user and can be renamed. */
    private static boolean isUserFile(FileObject fo) {
        String path = fo.getPath();
        // Find if this path is on non-local filesystems
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException ex) {
            return false;
        }
        return !isOnNonLocalFS(fs, path);
    }

    private static boolean isOnNonLocalFS(FileSystem fs, String path) {
        if (fs instanceof MultiFileSystem) {
            MultiFileSystem mfs = (MultiFileSystem) fs;
            try {
                java.lang.reflect.Method getDelegatesMethod;
                getDelegatesMethod = MultiFileSystem.class.getDeclaredMethod("getDelegates"); // NOI18N
                getDelegatesMethod.setAccessible(true);
                FileSystem[] delegates = (FileSystem[]) getDelegatesMethod.invoke(mfs);
                for (FileSystem fsd : delegates) {
                    if (isOnNonLocalFS(fsd, path)) {
                        return true;
                    }
                }
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        } else {
            if (fs instanceof LocalFileSystem) {
                return false; // is local FS
            } else {
                return fs.findResource(path) != null;
            }
        }
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
            return false;
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
    
    /**
     * A lookup that loads the instance from {@link InstanceGetter} when needed.
     * @param <T> the instance type.
     */
    private static final class LazyLookup<T> extends AbstractLookup {
        
        private Class<T> clazz;
        private InstanceGetter<T> instanceGetter;
        private boolean initialized = false;
        
        public LazyLookup() {
        }
        
        public void setInstanceGetter(Class<T> clazz, InstanceGetter<T> instanceGetter) {
            this.clazz = clazz;
            this.instanceGetter = instanceGetter;
        }

        @Override
        protected void beforeLookup(Template<?> template) {
            super.beforeLookup(template);
            
            if (clazz.equals(template.getType())) {
                synchronized (this) {
                    if (!initialized) {
                        T instance = instanceGetter.getInstance();
                        if (instance != null) {
                            addPair(new SimpleItem<T>(instance));
                        }
                        initialized = true;
                    }
                }
            }
        }
        
        /** Copy from AbstractLookup.SimpleItem */
        private final static class SimpleItem<T> extends Pair<T> {
            private T obj;

            /** Create an item.
             * @obj object to register
             */
            public SimpleItem(T obj) {
                if (obj == null) {
                    throw new NullPointerException();
                }
                this.obj = obj;
            }

            /** Tests whether this item can produce object
             * of class c.
             */
            @Override
            public boolean instanceOf(Class<?> c) {
                return c.isInstance(obj);
            }

            /** Get instance of registered object. If convertor is specified then
             *  method InstanceLookup.Convertor.convertor is used and weak reference
             * to converted object is saved.
             * @return the instance of the object.
             */
            @Override
            public T getInstance() {
                return obj;
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof SimpleItem) {
                    return obj.equals(((SimpleItem) o).obj);
                } else {
                    return false;
                }
            }

            @Override
            public int hashCode() {
                return obj.hashCode();
            }

            /** An identity of the item.
             * @return string representing the item, that can be used for
             *   persistance purposes to locate the same item next time
             */
            @Override
            public String getId() {
                return "IL[" + obj.toString(); // NOI18N
            }

            /** Getter for display name of the item.
             */
            @Override
            public String getDisplayName() {
                return obj.toString();
            }

            /** Method that can test whether an instance of a class has been created
             * by this item.
             *
             * @param obj the instance
             * @return if the item has already create an instance and it is the same
             *  as obj.
             */
            @Override
            protected boolean creatorOf(Object obj) {
                return obj == this.obj;
            }

            /** The class of this item.
             * @return the correct class
             */
            @SuppressWarnings("unchecked")
            @Override
            public Class<? extends T> getType() {
                return (Class<? extends T>)obj.getClass();
            }
        }
        
        private static interface InstanceGetter<T> {
            
            T getInstance();
            
        }
        
    }
    
}
