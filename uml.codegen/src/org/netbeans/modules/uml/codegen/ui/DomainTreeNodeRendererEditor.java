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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.uml.codegen.ui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.modules.uml.ui.swing.SelectableLabel;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class DomainTreeNodeRendererEditor extends JPanel
    implements TreeCellRenderer
{
    protected JCheckBox checkBox = null;
    protected SelectableLabel selectLabel = null;
    
    public DomainTreeNodeRendererEditor()
    {
        checkBox = new javax.swing.JCheckBox();
        checkBox.setBorder(
            javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        selectLabel = new SelectableLabel();
        selectLabel.setText("");

        org.jdesktop.layout.GroupLayout layout = 
            new org.jdesktop.layout.GroupLayout(this);
        
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(checkBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(selectLabel))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(checkBox)
                .add(selectLabel))
        );
    }
    
    public Component getTreeCellRendererComponent(
        JTree tree, 
        Object value,
        boolean isSelected, 
        boolean expanded, 
        boolean leaf,
        int row, 
        boolean hasFocus)
    {
        setEnabled(tree.isEnabled());

        DomainTreeNode node = (DomainTreeNode)
            ((DefaultMutableTreeNode)value).getUserObject();

        if (node.isDomain())
        {
            checkBox.setVisible(true);
            checkBox.setSelected(node.isChecked());
        }
        
        else
            checkBox.setVisible(false);
        
        selectLabel.setSelected(isSelected);
        selectLabel.setFont(tree.getFont());
        selectLabel.setText(node.getDisplayName());
        
        if (isSelected)
        {
            selectLabel.setSelectedBackground(
                UIManager.getColor("Tree.selectionBackground")); // NOI18N
            selectLabel.setForeground(
                UIManager.getColor("Tree.selectionForeground")); // NOI18N
        }
        
        else
        {
            selectLabel.setForeground(
                UIManager.getColor("Tree.textForeground")); // NOI18N
        }
        
        return this;
    }
    
    
    public Dimension getPreferredSize()
    {
        Dimension newDim = new Dimension();
        
        double width = checkBox.getPreferredSize().getWidth() +
            selectLabel.getPreferredSize().getWidth();
        
        double height = checkBox.getPreferredSize().getHeight() +
            selectLabel.getPreferredSize().getHeight();
        
        newDim.setSize(width, height);
        return newDim;
    }
    
    
    public void setBackground(Color color)
    {
        if (color instanceof ColorUIResource)
            color = null;
        
        super.setBackground(color);
    }
}
