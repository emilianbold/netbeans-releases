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
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;

/**
 * A simple tree cell editor helper used to properly display a node while in editing mode.
 * @author Soot Phengsy
 */
class DirectoryCellEditor extends DefaultCellEditor {
    
    private final JPanel editorPanel = new JPanel(new BorderLayout());
    private static JTextField textField;
    private static JTree tree;
    private static JFileChooser fileChooser;
    
    public DirectoryCellEditor(JTree tree, JFileChooser fileChooser, final JTextField textField) {
        super(textField);
        this.tree = tree;
        this.textField = textField;
        this.fileChooser = fileChooser;
    }
    
    public boolean isCellEditable(EventObject event) {
        return ((event instanceof MouseEvent) ? false : true);
    }
    
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        DirectoryNode node = (DirectoryNode)value;
        editorPanel.setOpaque(false);
        editorPanel.add(new JLabel(fileChooser.getIcon(node.getFile())), BorderLayout.CENTER);
        editorPanel.add(c, BorderLayout.EAST);
        textField = (JTextField)getComponent();
        String text = fileChooser.getName(node.getFile());
        textField.setText(text);
        textField.setColumns(text.length());
        return editorPanel;
    }
    
    public static JTextField getTextField() {
        return textField;
    }
}
