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

package org.netbeans.core.windows.services;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.NbBundle;

/** File Selector
 *
 * @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
 */
final class FileSelector extends JPanel implements PropertyChangeListener, ExplorerManager.Provider {
    //XXX AFAIK nothing in NetBeans uses NodeOperation.select().  Probably this class can be deleted and NodeOperation.select deprecated. - Tim

    /** generated Serialized Version UID */
    static final long serialVersionUID = 6524404012203099065L;
    /** manages tree */
    private final ExplorerManager manager = new ExplorerManager();
    /** tree */
    private BeanTreeView tree;

    /** The OK Button */
    private JButton okButton;
    /** The Cancel Button */
    private JButton cancelButton;
    private JButton[] buttons;

    /** aceptor */
    private NodeAcceptor acceptor;

    /** reference to Frame that keeps our selected nodes synchronized with nodes actions */
    //  static TopFrameHack hack;

    /**
     * @param title is a title of the dialog
     * @param rootLabel label for the root node
     * @param root the base object to start browsing from
     * @param acceptor decides whether we have valid selection or not
     * @param top is a <code>Component</code> we just place on the top of the dialog
     * it can be <code>null</code>
     */
    public FileSelector ( String rootLabel, Node root, final NodeAcceptor acceptor, Component top) {
        super ();

        this.acceptor = acceptor;
        
        ResourceBundle bundle = NbBundle.getBundle(FileSelector.class);


        okButton = new JButton(bundle.getString("CTL_FileSelectorOkButton"));
        cancelButton = new JButton(bundle.getString("CTL_FileSelectorCancelButton"));
        okButton.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_FileSelectorOkButton"));
        cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_FileSelectorCancelButton"));
        buttons = new JButton[] { okButton, cancelButton };
        

        manager.setRootContext (root);//s[0]);
        
        // Center
        tree = new BeanTreeView ();
        tree.setPopupAllowed (false);
        tree.setDefaultActionAllowed (false);
        // install proper border for tree
        tree.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        tree.getAccessibleContext().setAccessibleName(NbBundle.getBundle(FileSelector.class).getString("ACSN_FileSelectorTreeView"));
        tree.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FileSelector.class).getString("ACSD_FileSelectorTreeView"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(FileSelector.class).getString("ACSD_FileSelectorDialog"));
        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);

        // component to place at the top
        try {
            Node[] roots;
            if (
                root instanceof org.netbeans.core.DataSystem &&
                (roots = root.getChildren ().getNodes ()).length > 0
            ) {
                final JComboBox combo = new JComboBox(roots);
                combo.setRenderer(new FileSelectRenderer());
                combo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        Node o = (Node) combo.getSelectedItem();
                        manager.setRootContext(o);
                    }
                });
                combo.getAccessibleContext().setAccessibleDescription(bundle.getString ("ACSD_FileSelectorComboBox"));
                manager.setSelectedNodes (new Node[] { roots[0] });

                // North - "Create In" // NOI18N
                JPanel comboPanel = new JPanel();
                // Space between label and drop-down list
                comboPanel.setLayout(new BorderLayout(5, 0));

                // support for mnemonics (defaults to first char)
                JLabel label = new JLabel(rootLabel.replace('&', ' '));
                label.setDisplayedMnemonic(rootLabel.charAt(rootLabel.indexOf('&') + 1));
                label.setLabelFor(combo);
                comboPanel.add(label, BorderLayout.WEST);
                comboPanel.add(combo, BorderLayout.CENTER);
                add(comboPanel, BorderLayout.NORTH);
            } else {
                manager.setSelectedNodes (new Node[] { root });
                JLabel label = new JLabel(rootLabel.replace('&', ' '));
                label.setLabelFor(tree);
                add(label, BorderLayout.NORTH);
            }
        } catch(PropertyVetoException pve) {
            throw new IllegalStateException(pve.getMessage());
        }



        // South
        if (top != null) {
            add(top, BorderLayout.SOUTH);
        }

        manager.addPropertyChangeListener (this);

//        if (top != null) top.requestFocus ();

        if (acceptor.acceptNodes (manager.getSelectedNodes())) {
            enableButton ();
        } else {
            disableButton ();
        }

    }
    
    Object[] getOptions() {
        return buttons;
    }
    
    Object getSelectOption() {
        return okButton;
    }
    
    

    /** Changing properties. Implements <code>PropertyChangeListener</code>. */
    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals (ExplorerManager.PROP_SELECTED_NODES)) {
            if (acceptor.acceptNodes (manager.getSelectedNodes())) {
                enableButton ();
            } else {
                disableButton ();
            }
        }
    }


    /** Gets preferred size. Overrides superclass method. Height is adjusted
     * to 1/2 screen. */
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = Math.max(dim.height, org.openide.util.Utilities.getUsableScreenBounds().height / 2);
        return dim;
    }

    /**
    * @return selected nodes
    */
    public Node[] getNodes() {
        return manager.getSelectedNodes();
    }

    /** enables ok button */
    void enableButton () {
        okButton.setEnabled(true);
    }

    /** disables ok button */
    void disableButton () {
        okButton.setEnabled(false);
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    

    /** Renderer used in list box of exit dialog */
    private static class FileSelectRenderer extends JLabel implements ListCellRenderer {
        /** Generated Serialized Version UID. */
        static final long serialVersionUID = -7071698027341621636L;

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        public FileSelectRenderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        /** Implements <code>ListCellRenderer</code>. */
        public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof Node)) return this;

            Node node = (Node)value;

            ImageIcon icon = new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
            setIcon(icon);

            setText(node.getDisplayName());
            if (isSelected){
                super.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                super.setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                super.setBackground(list.getBackground());
                super.setForeground(list.getForeground());
            }

            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    } // End of class FileSelectRenderer.
}
