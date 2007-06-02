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
import javax.swing.JLabel;
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
    protected JCheckBox check = null;
    protected SelectableLabel label = null;
    
    public DomainTreeNodeRendererEditor()
    {
        setLayout(null);
        add(check = new JCheckBox());
        check.setLocation(50, 0);
        add(label = new SelectableLabel());
        check.requestFocus();
        setOpaque(false);
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
        initializeControl(value, tree, isSelected, hasFocus);
        return this;
    }
    
    protected void initializeControl(
        Object value,
        JTree tree,
        boolean isSelected,
        boolean hasFocus)
    {
        setEnabled(tree.isEnabled());

        DomainTreeNode node = (DomainTreeNode)
            ((DefaultMutableTreeNode)value).getUserObject();

        label.setSelected(isSelected);
        label.setFocus(hasFocus);

        
        if (node.isDomain())
        {
            check.setVisible(true);
            check.setSelected(node.isChecked());
        }
        
        else
            check.setVisible(false);
        
        label.setFont(tree.getFont());
        label.setText(" " + node.getDisplayName());
        
        if (isSelected)
        {
            label.setSelectedBackground(UIManager.getColor("Tree.selectionBackground"));
            label.setForeground(UIManager.getColor("Tree.selectionForeground"));
        }
        
        else
        {
            label.setForeground(UIManager.getColor("Tree.textForeground"));
        }
    }
    
    public Dimension getPreferredSize()
    {
        if (check.isVisible())
        {
            Dimension dimCheck = check.getPreferredSize();
            Dimension dimLabel = label.getPreferredSize();

            return new Dimension(
                dimCheck.width + dimLabel.width,
                dimCheck.height < dimLabel.height
                    ? dimLabel.height + 20 : dimCheck.height + 20);
        }
        
        else
        {
            Dimension dimLabel = label.getPreferredSize();
            return new Dimension(dimLabel.width, dimLabel.height);
        }
    }
    
    public void doLayout()
    {
        if (check.isVisible())
        {
            Dimension dimCheck = check.getPreferredSize();
            Dimension dimLabel = label.getPreferredSize();
            int yCheck = 0;
            int yLabel = 0;

            if (dimCheck.height < dimLabel.height)
                yCheck = (dimLabel.height - dimCheck.height) / 2;

            else
                yLabel = (dimCheck.height - dimLabel.height) / 2;

            check.setLocation(0, yCheck);
            check.setBounds(0, yCheck, dimCheck.width, dimCheck.height);
            label.setLocation(dimCheck.width, yLabel);
            label.setBounds(dimCheck.width, yLabel, dimLabel.width, dimLabel.height);
        }

        else
        {
            Dimension dimLabel = label.getPreferredSize();
            label.setLocation(0, dimLabel.height);
            label.setBounds(0, 0, dimLabel.width, dimLabel.height);
        }
    }
    
    public void setBackground(Color color)
    {
        if (color instanceof ColorUIResource)
            color = null;
        
        super.setBackground(color);
    }
    
}
