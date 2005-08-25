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

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.services.ToolbarFolderNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.NewType;

/**
 * Toolbar Customizer showing a tree of all available actions. Users can drag actions
 * to toolbars to add new toolbar buttons.
 *
 * @author  Stanislav Aubrecht
 */
public class ConfigureToolbarPanel extends javax.swing.JPanel implements Runnable {
    
    private static WeakReference dialogRef; // is weak reference necessary?
    
    private Node root;

    /** Creates new form ConfigureToolbarPanel */
    private ConfigureToolbarPanel() {
        initComponents();
        
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject paletteFolder = fs.findResource( "Actions" ); // NOI18N
        DataFolder df = DataFolder.findFolder( paletteFolder );
        root = new FolderActionNode( df.getNodeDelegate() );

        final JLabel lblWait = new JLabel( getBundleString("LBL_PleaseWait") );
        lblWait.setHorizontalAlignment( JLabel.CENTER );
        palettePanel.setPreferredSize( new Dimension( 350, 350 ) );
        palettePanel.add( lblWait );
        RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                Node[] categories = root.getChildren().getNodes( true );
                for( int i=0; i<categories.length; i++ ) {
                    categories[i].getChildren().getNodes( true );
                }
                SwingUtilities.invokeLater( ConfigureToolbarPanel.this );
            }
        });
    }
    
    public void run() {
        ActionsTree tree = new ActionsTree( root );
        palettePanel.removeAll();
        palettePanel.setBorder( BorderFactory.createEmptyBorder() );
        palettePanel.add( new JScrollPane(tree), BorderLayout.CENTER );
        invalidate();
        validate();
        repaint();
        setCursor( Cursor.getDefaultCursor() );
    }
    
    public static void showConfigureDialog() {
        java.awt.Dialog dialog = null;
        if (dialogRef != null)
            dialog = (JDialog) dialogRef.get();
        if (dialog == null) {
            JButton closeButton = new JButton();
            closeButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    endToolbarEditMode();
                }
            });
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
            dialogRef = new WeakReference(dialog);
        }
        dialog.setVisible(true);
        startToolbarEditMode();
    }
    
    static void startToolbarEditMode() {
        ToolbarPool.getDefault().putClientProperty( "editMode", new Object() ); // NOI18N
    }
    
    static void endToolbarEditMode() {
        ToolbarPool.getDefault().putClientProperty( "editMode", null ); // NOI18N
    }
    
    /** @return returns string from bundle for given string pattern */
    static final String getBundleString (String bundleStr) {
        return NbBundle.getMessage(ConfigureToolbarPanel.class, bundleStr);
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

        setLayout(new java.awt.GridBagLayout());

        lblHint.setLabelFor(palettePanel);
        lblHint.setText(getBundleString("CTL_TreeLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 1, 10);
        add(lblHint, gridBagConstraints);

        palettePanel.setLayout(new java.awt.BorderLayout());

        palettePanel.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 10, 5, 10);
        add(palettePanel, gridBagConstraints);

        checkSmallIcons.setText(getBundleString("CTL_SmallIcons"));
        checkSmallIcons.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        checkSmallIcons.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkSmallIcons.setSelected( ToolbarPool.getDefault().getPreferredIconSize() == 16 );
        checkSmallIcons.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchIconSize(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(checkSmallIcons, gridBagConstraints);

        btnNewToolbar.setText(getBundleString("CTL_NewToolbar"));
        btnNewToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newToolbar(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        add(btnNewToolbar, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void newToolbar(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newToolbar
        ToolbarFolderNode tf = new ToolbarFolderNode();
        NewType[] newTypes = tf.getNewTypes();
        if( null != newTypes && newTypes.length > 0 ) {
            try {
                newTypes[0].create();
            } catch (IOException e) {
                ErrorManager.getDefault().notify( e );
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
}
