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

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.services.ToolbarFolderNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.awt.ToolbarPool;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.NewType;

/**
 * Toolbar Customizer showing a tree of all available actions. Users can drag actions
 * to toolbars to add new toolbar buttons.
 *
 * @author  Stanislav Aubrecht
 */
public class ConfigureToolbarPanel extends javax.swing.JPanel implements Runnable {
    
    private static WeakReference<Dialog> dialogRef; // is weak reference necessary?
    
    private Node root;

    /** Creates new form ConfigureToolbarPanel */
    private ConfigureToolbarPanel() {
        initComponents();
        
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject paletteFolder = fs.findResource( "Actions" ); // NOI18N
        DataFolder df = DataFolder.findFolder( paletteFolder );
        root = new FolderActionNode( new AbstractNode( df.createNodeChildren( new ActionIconDataFilter() ) ) );

        final JLabel lblWait = new JLabel( getBundleString("LBL_PleaseWait") );
        lblWait.setHorizontalAlignment( JLabel.CENTER );
        palettePanel.setPreferredSize( new Dimension( 440, 350 ) );
        palettePanel.add( lblWait );
        getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_ToolbarCustomizer") );
    }
    
    public void run() {
        ActionsTree tree = new ActionsTree( root );
        tree.getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_ActionsTree") );
        tree.getAccessibleContext().setAccessibleName( getBundleString("ACSN_ActionsTree") );
        palettePanel.removeAll();
        palettePanel.setBorder( BorderFactory.createEmptyBorder() );
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.getVerticalScrollBar().getAccessibleContext().setAccessibleName( getBundleString("ACSN_ActionsScrollBar") );
        scrollPane.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_ActionsScrollBar") );
        palettePanel.add( scrollPane, BorderLayout.CENTER );
        lblHint.setLabelFor( tree );
        invalidate();
        validate();
        repaint();
        setCursor( Cursor.getDefaultCursor() );
    }
    
    public static void showConfigureDialog() {
        java.awt.Dialog dialog = null;
        if (dialogRef != null)
            dialog = dialogRef.get();
        if (dialog == null) {
            JButton closeButton = new JButton();
            closeButton.getAccessibleContext().setAccessibleDescription( getBundleString("ACSD_Close") );
            org.openide.awt.Mnemonics.setLocalizedText(
                closeButton, getBundleString("CTL_Close")); 
            DialogDescriptor dd = new DialogDescriptor(
                new ConfigureToolbarPanel(),
                getBundleString("CustomizerTitle"), 
                false,
                new Object[] { closeButton },
                closeButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialogRef = new WeakReference<Dialog>(dialog);
        }
        dialog.addWindowListener( new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                endToolbarEditMode();
            }
        });
        dialog.setVisible(true);
        startToolbarEditMode();
    }
    
    static void startToolbarEditMode() {
        ToolbarPool.getDefault().putClientProperty( "editMode", new Object() ); // NOI18N
    }
    
    static void endToolbarEditMode() {
        ToolbarPool.getDefault().putClientProperty( "editMode", null ); // NOI18N
        //remove empty toolbars
        DataFolder folder = ToolbarPool.getDefault().getFolder();
        DataObject[] children = folder.getChildren();
        for( int i=0; i<children.length; i++ ) {
            final DataFolder subFolder = (DataFolder)children[i].getCookie( DataFolder.class );
            if( null != subFolder && subFolder.getChildren().length == 0 ) {
                SwingUtilities.invokeLater( new Runnable() {

                    public void run() {
                        try {
                            subFolder.delete();
                        }
                        catch (IOException e) {
                            Logger.getLogger(ConfigureToolbarPanel.class.getName()).log(Level.WARNING, null, e);
                        }
                    }
                });
            }
        }
    }
    
    /** @return returns string from bundle for given string pattern */
    static final String getBundleString (String bundleStr) {
        return NbBundle.getMessage(ConfigureToolbarPanel.class, bundleStr);
    }

    private boolean firstTimeInit = true;
    public void paint(java.awt.Graphics g) {
        super.paint(g);
        if( firstTimeInit ) {
            //this is not very nice but some Actions insist on being accessed
            //from the event queue only so let's wait till the dialog window is 
            //painted before filtering out Actions without an icon
            firstTimeInit = false;
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    //warm up action nodes so that 'expand all' in actions tree is fast
                    Node[] categories = root.getChildren().getNodes( true );
                    for( int i=0; i<categories.length; i++ ) {
                        categories[i].getChildren().getNodes( true );
                    }
                    //replace 'please wait' message with actions tree
                    SwingUtilities.invokeLater( ConfigureToolbarPanel.this );
                }
            });
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

        lblHint = new javax.swing.JLabel();
        palettePanel = new javax.swing.JPanel();
        checkSmallIcons = new javax.swing.JCheckBox();
        btnNewToolbar = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(453, 68));
        org.openide.awt.Mnemonics.setLocalizedText(lblHint, getBundleString("CTL_TreeLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 1, 10);
        add(lblHint, gridBagConstraints);

        palettePanel.setLayout(new java.awt.BorderLayout());

        palettePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 5, 10);
        add(palettePanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(checkSmallIcons, getBundleString("CTL_SmallIcons")); // NOI18N
        checkSmallIcons.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkSmallIcons.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkSmallIcons.setSelected( ToolbarPool.getDefault().getPreferredIconSize() == 16 );
        checkSmallIcons.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(checkSmallIcons, gridBagConstraints);
        checkSmallIcons.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_SmallIcons")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnNewToolbar, getBundleString("CTL_NewToolbar")); // NOI18N
        btnNewToolbar.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(btnNewToolbar, gridBagConstraints);
        btnNewToolbar.getAccessibleContext().setAccessibleDescription(getBundleString("ACSD_NewToolbar")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnReset, getBundleString("CTL_ResetToolbarsButton")); // NOI18N
        btnReset.addActionListener(formListener);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(btnReset, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == checkSmallIcons) {
                ConfigureToolbarPanel.this.switchIconSize(evt);
            }
            else if (evt.getSource() == btnNewToolbar) {
                ConfigureToolbarPanel.this.newToolbar(evt);
            }
            else if (evt.getSource() == btnReset) {
                ConfigureToolbarPanel.this.resetToolbars(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void resetToolbars(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetToolbars
        new ResetToolbarsAction().actionPerformed( evt );
    }//GEN-LAST:event_resetToolbars

    private void newToolbar(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newToolbar
        ToolbarFolderNode tf = new ToolbarFolderNode();
        NewType[] newTypes = tf.getNewTypes();
        if( null != newTypes && newTypes.length > 0 ) {
            try {
                newTypes[0].create();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }//GEN-LAST:event_newToolbar

    private void switchIconSize(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchIconSize
          boolean state = checkSmallIcons.isSelected();
          if (state) {
              ToolbarPool.getDefault().setPreferredIconSize(16);
          } else {
              ToolbarPool.getDefault().setPreferredIconSize(24);
          }
          //Rebuild toolbar panel
          //#43652: Find current toolbar configuration
          String name = ToolbarPool.getDefault().getConfiguration();
          ToolbarConfiguration tbConf = ToolbarConfiguration.findConfiguration(name);
          if (tbConf != null) {
              tbConf.refresh();
          }
    }//GEN-LAST:event_switchIconSize
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewToolbar;
    private javax.swing.JButton btnReset;
    private javax.swing.JCheckBox checkSmallIcons;
    private javax.swing.JLabel lblHint;
    private javax.swing.JPanel palettePanel;
    // End of variables declaration//GEN-END:variables

    private static class FolderActionNode extends FilterNode {
        public FolderActionNode( Node original  ) {
            super( original, new MyChildren( original ) );
        }

        public String getDisplayName() {
            return Actions.cutAmpersand( super.getDisplayName() );
        }

        public Transferable drag() throws IOException {
            return Node.EMPTY.drag();
        }

        public Transferable clipboardCut() throws IOException {
            return Node.EMPTY.clipboardCut();
        }

        public Transferable clipboardCopy() throws IOException {
            return Node.EMPTY.clipboardCopy();
        }

        private static class MyChildren extends FilterNode.Children {

            public MyChildren(Node original) {
                super(original);
            }

            protected Node copyNode(Node node) {
                DataFolder df = (DataFolder)node.getCookie( DataFolder.class );
                if( null == df )
                    return new ItemActionNode( node );
                return new FolderActionNode( node );
            }
        }
    }

    private static class ItemActionNode extends FilterNode {
        
        private static DataFlavor nodeDataFlavor = new DataFlavor( Node.class, "Action Node" ); // NOI18N
        
        public ItemActionNode( Node original  ) {
            super( original, Children.LEAF );
        }

        public Transferable drag() throws IOException {
            return new ExTransferable.Single( nodeDataFlavor ) {
                public Object getData() {
                   return ItemActionNode.this;
                }
            };
        }

        public String getDisplayName() {
            return Actions.cutAmpersand( super.getDisplayName() );
        }
    }
    
    /**
     * A filter that does not allow Action instances without an icon.
     */
    private static class ActionIconDataFilter implements DataFilter {
        private InstanceCookie instanceCookie;
        
        public boolean acceptDataObject( DataObject obj ) {
            instanceCookie = (InstanceCookie)obj.getCookie( InstanceCookie.class );
            if( null != instanceCookie ) {
                try {
                    Object instance = instanceCookie.instanceCreate();
                    if( null != instance ) {
                        if( instance instanceof Action ) {
                            Action action = (Action)instance;
                            try {
                                if( null == action.getValue( "iconBase" ) ) {
                                    return false;
                                }
                            } catch( AssertionError aE ) {
                                //hack: some action do not allow access outside
                                //event queue - so let's ignore their assertions
                            }
                        } else if( instance instanceof JSeparator ) {
                            return false;
                        }
                    }
                } catch( Throwable e ) {
                    Logger.getLogger(ConfigureToolbarPanel.class.getName()).log(Level.WARNING, null, e);
                }
                return true;
            } else {
                FileObject fo = obj.getPrimaryFile();
                if( fo.isFolder() ) {
                    boolean hasChildWithIcon = false;
                    FileObject[] children = fo.getChildren();
                    for( int i=0; i<children.length; i++ ) {
                        DataObject child = null;
                        try {
                            child = DataObject.find( children[i] );
                        } catch (DataObjectNotFoundException e) {
                            continue;
                        }
                        if( null != child && acceptDataObject( child ) ) {
                            hasChildWithIcon = true;
                            break;
                        }
                    }
                    return hasChildWithIcon;
                }
            }
            return true;
        }
    }
}
