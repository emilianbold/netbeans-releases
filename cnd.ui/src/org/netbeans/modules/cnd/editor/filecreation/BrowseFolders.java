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

package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 *
 */
public class BrowseFolders extends javax.swing.JPanel implements ExplorerManager.Provider {
    
    private final ExplorerManager manager;
    private final BeanTreeView btv;
    
    private static final JScrollPane SAMPLE_SCROLL_PANE = new JScrollPane();
    
    /** Creates new form BrowseFolders */
    public BrowseFolders( SourceGroup[] folders, Project project, String preselectedFileName ) {
        initComponents();
        
        manager = new ExplorerManager();        
        AbstractNode rootNode = new AbstractNode( new SourceGroupsChildren( folders, project ) );
        manager.setRootContext( rootNode );
        
        // Create the templates view
        btv = new BeanTreeView();
        btv.setRootVisible( false );
        btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );
        btv.setBorder( SAMPLE_SCROLL_PANE.getBorder() );        
        btv.setPopupAllowed( false );
        btv.getAccessibleContext ().setAccessibleName (NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders_folderPanel"));
        btv.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(BrowseFolders.class, "ACSD_BrowseFolders_folderPanel"));
        expandSelection( preselectedFileName );
        //expandAllNodes( btv, manager.getRootContext() );
        folderPanel.add( btv, java.awt.BorderLayout.CENTER );        
    }
        
    // ExplorerManager.Provider implementation ---------------------------------
    
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        folderPanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(folderPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BrowseFolders.class, "LBL_BrowseFolders_jLabel1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders_jLabel1")); // NOI18N

        folderPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(folderPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrowseFolders.class, "ACSN_BrowseFolders")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel folderPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
        
    public static FileObject showDialog( SourceGroup[] folders, Project project, String preselectedFileName ) {
        
        BrowseFolders bf = new BrowseFolders( folders, project, preselectedFileName );
        
        JButton options[] = new JButton[] { 
            new JButton(), 
            new JButton( NbBundle.getMessage( BrowseFolders.class, "BTN_BrowseFolders_Cancel_Option")  ), // NOI18N
        };
                
        OptionsListener optionsListener = new OptionsListener( bf );
        
        options[ 0 ].setActionCommand( OptionsListener.COMMAND_SELECT );
        options[ 0 ].addActionListener( optionsListener );
        Mnemonics.setLocalizedText(options[0], NbBundle.getMessage( BrowseFolders.class, "BTN_BrowseFolders_Select_Option") );
        options[ 0 ].getAccessibleContext ().setAccessibleName (NbBundle.getMessage( BrowseFolders.class, "ACSN_BrowseFolders_Select_Option"));
        options[ 0 ].getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage( BrowseFolders.class, "ACSD_BrowseFolders_Select_Option"));
        options[ 1 ].setActionCommand( OptionsListener.COMMAND_CANCEL );
        options[ 1 ].addActionListener( optionsListener );    
        options[ 1 ].getAccessibleContext ().setAccessibleName (NbBundle.getMessage( BrowseFolders.class, "ACSN_BrowseFolders_Cancel_Option"));
        options[ 1 ].getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage( BrowseFolders.class, "ACSD_BrowseFolders_Cancel_Option"));
        
        DialogDescriptor dialogDescriptor = new DialogDescriptor( 
            bf,                                     // innerPane
            NbBundle.getMessage( BrowseFolders.class, "LBL_BrowseFolders_Dialog"),                       // displayName
            true,                                   // modal
            options,                                // options
            options[ 0 ],                           // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            null,                                   // helpCtx
            null  );                                 // listener 

        dialogDescriptor.setClosingOptions( new Object[] { options[ 0 ], options[ 1 ] } );
            
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
        } finally {
            dialog.dispose();
        }
        
        return optionsListener.getResult();
    }
    
    private void expandSelection( String preselectedFileName ) {
        
        Node root = manager.getRootContext();
        Children ch = root.getChildren();
        if ( ch == Children.LEAF ) {
            return;
        }
        Node nodes[] = ch.getNodes( true );
        
        Node sel = null;        
        
        if ( preselectedFileName != null && preselectedFileName.length() > 0 ) {
             // Try to find the node
             for ( int i = 0; i < nodes.length; i++ ) {            
                try { 
                    sel = NodeOp.findPath(nodes[i], NbCollections.checkedEnumerationByFilter(new StringTokenizer(preselectedFileName, "/"), String.class, true)); // NOI18N
                    break;
                }
                catch ( NodeNotFoundException e ) {            
                    System.out.println( e.getMissingChildName() );
                    // Will select the first node
                }
             }
        }
                        
        if ( sel == null ) {
            // Node not found => expand first level
            btv.expandNode( root );
            for ( int i = 0; i < nodes.length; i++ ) {            
                btv.expandNode( nodes[i] );
                if ( i == 0 ) {
                    sel = nodes[i];
                }
            }
        }
        
        
        if ( sel != null ) {
            // Select the node
            try {
                manager.setSelectedNodes( new Node[] { sel } );
            }
            catch ( PropertyVetoException e ) {
                // No selection for some reason
            }
        }
                
    }
    
    
    /*
    private static void expandAllNodes( BeanTreeView btv, Node node ) {
        
        btv.expandNode( node );
        
        Children ch = node.getChildren();
        if ( ch == Children.LEAF ) {
            return;
        }
        Node nodes[] = ch.getNodes( true );
        
        for ( int i = 0; i < nodes.length; i++ ) {
            expandAllNodes( btv, nodes[i] );
        }
        
    }
    */
    
    // Innerclasses ------------------------------------------------------------

    private static final class SourceGroupsChildren extends Children.Keys<SourceGroup> {

        private final SourceGroup[] groups;
        private final Project project;

        public SourceGroupsChildren(SourceGroup[] groups, Project project) {
            assert groups != null;
            assert project != null;
            this.groups = groups;
            this.project = project;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(groups);
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<SourceGroup>emptySet());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes(SourceGroup g) {
            try{ 
                FileObject folder = g.getRootFolder();
                FilterNode fn = new FilterNode(
                        new PhysicalView.GroupNode(project, g, folder.equals(project.getProjectDirectory()), DataFolder.findFolder(folder)),
                        new SourceGroupChildren(folder, g));
                return new Node[] { fn };
            }catch (Exception ex){
                return null;
            }
        }

    }

    private static final class SourceGroupChildren extends Children.Keys<FileObject> {

        private final SourceGroup group;
        private final FileObject fo;

        public SourceGroupChildren(FileObject fo, SourceGroup group) {
            assert fo != null;
            assert group != null;
            this.fo = fo;
            this.group = group;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            List<FileObject> l = new ArrayList<FileObject>();
            for (FileObject f : fo.getChildren()) {
                if (f.isFolder() && /*group.contains(f) &&*/ VisibilityQuery.getDefault().isVisible(f)) {
                    l.add(f);
                }
            }
            Collections.sort(l, new Comparator<FileObject>() { // #116545
                Collator COLL = Collator.getInstance();
                @Override
                public int compare(FileObject f1, FileObject f2) {
                    return COLL.compare(f1.getNameExt(), f2.getNameExt());
                }
            });
            setKeys(l);
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.<FileObject>emptySet());
            super.removeNotify();
        }

        @Override
        protected Node[] createNodes(FileObject folder) {
            FilterNode fn = new FilterNode(
                    DataFolder.findFolder(folder).getNodeDelegate(),
                    new SourceGroupChildren(folder, group));
            return new Node[] { fn };
        }

    }

    
    private static final class OptionsListener implements ActionListener {
    
        public static final String COMMAND_SELECT = "SELECT"; // NOI18N
        public static final String COMMAND_CANCEL = "CANCEL"; // NOI18N
            
        private final BrowseFolders browsePanel;
        
        private FileObject result;
        
        public OptionsListener( BrowseFolders browsePanel  ) {
            this.browsePanel = browsePanel;
        }
        
        @Override
        public void actionPerformed( ActionEvent e ) {
            String command = e.getActionCommand();

            if ( COMMAND_SELECT.equals( command ) ) {
                Node selection[] = browsePanel.getExplorerManager().getSelectedNodes();
                
                if ( selection != null && selection.length > 0 ) {
                    DataObject dobj = selection[0].getLookup().lookup(DataObject.class);
                    if ( dobj != null ) {
                        FileObject fo = dobj.getPrimaryFile();
                        if ( fo.isFolder() ) {
                            result = fo;
                        }
                    }
                }
                
                
            }
        }
        
        public FileObject getResult() {
            return result;
        }
    }
    
    
}
