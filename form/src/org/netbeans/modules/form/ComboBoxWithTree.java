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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 * ComboBox with tree in popup.
 *
 * @author Jan Stola
 */
public class ComboBoxWithTree extends JComboBox {
    /** Window used as popup. */
    private Window popup;
    /** Tree in popup. */
    private JTree tree;
    /** Scroll pane enclosing the tree. */
    private JScrollPane scrollPane;
    /** Converter between tree path and its string representation. */
    private Converter converter;
    
    /**
     * Creates new <code>ComboBoxWithTree</code>.
     * 
     * @param treeModel tree model.
     * @param treeCellRenderer tree cell renderer.
     * @param converter converter between tree path and its string representation.
     */
    public ComboBoxWithTree(TreeModel treeModel, TreeCellRenderer treeCellRenderer, Converter converter) {
        this.converter = converter;
        initCombo();
        initTree(treeModel, treeCellRenderer);
    }

    /**
     * Initializes the combo. 
     */
    private void initCombo() {
        setEditable(true);
        addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {}

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                getPopup().setVisible(false);
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                updateTreeSelection();
                Dimension dim = getSize();
                Point p = getLocationOnScreen();
                Window w = getPopup();
                w.setLocation(p.x, p.y + dim.height);
                w.setSize(new Dimension(dim.width, scrollPane.getPreferredSize().height));
                w.setVisible(true);
            }
        });
        // Get rid of original popup
        setModel(new DefaultComboBoxModel(new Object[] {""})); // NOI18N
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (index == -1) { // Make sure that the combo has a correct preferred size
                    return super.getListCellRendererComponent(list, "null", index, isSelected, cellHasFocus); // NOI18N
                } else {
                    JLabel comp = new JLabel();
                    comp.setPreferredSize(new Dimension(0,-10000));
                    return comp;
                }
            }
        });
    }

    /**
     * Initializes the tree. 
     * 
     * @param treeModel tree model.
     * @param treeCellRenderer tree cell renderer.
     */
    private void initTree(TreeModel treeModel, TreeCellRenderer treeCellRenderer) {
        tree = new JTree();
        TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setSelectionModel(selectionModel);
        tree.setVisibleRowCount(10);
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if ((code == KeyEvent.VK_ENTER) || (code == KeyEvent.VK_ESCAPE)) {
                    setPopupVisible(false);
                }
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    setPopupVisible(false);
                }
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                String value = converter.pathToString(e.getPath());
                setSelectedItem(value);
            }
        });
        tree.setModel(treeModel);
        tree.setCellRenderer(treeCellRenderer);
    }

    /**
     * Returns the popup.
     * 
     * @return the popup.
     */
    private Window getPopup() {
        if (popup == null) {
            popup = new Window(SwingUtilities.getWindowAncestor(this));
            scrollPane = new JScrollPane(tree);
            // The scrollPane must be in JPopupMenu to ensure that
            // it is not closed when components within it obtain the focus
            scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            JPopupMenu menu = new JPopupMenu() {
                // Cannot use setVisible(true) on JDK 6 due to changes in isPopupMenu()
                @Override
                public boolean isVisible() {
                    return true;
                }
            };
            menu.setBorder(new EmptyBorder(0,0,0,0));
            menu.setLayout(new BorderLayout());
            menu.add(scrollPane);
            popup.add(menu);
        }
        return popup;
    }

    /**
     * Updates tree selection according to string in combo. 
     */
    private void updateTreeSelection() {
        final TreePath path = getSelectedTreePath();
        if (path == null) {
            tree.clearSelection();
        } else {
            tree.setSelectionPath(path);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    tree.scrollPathToVisible(path);
                }
            });
        }
    }

    public TreePath getSelectedTreePath() {
        String value = getEditor().getItem().toString();
        TreePath path = converter.stringToPath(value);
        return path;
    }

    /**
     * Converter between tree path and its string representation.
     */
    public static interface Converter {
        /**
         * Converts tree path to string representation.
         * 
         * @param path path to convert.
         * @return string representation of tree path.
         */
        String pathToString(TreePath path);

        /**
         * Converts string representation to tree path.
         * 
         * @param value string to convert.
         * @return tree path that corresponds to the given string representation.
         */
        TreePath stringToPath(String value);
    }
    
}
