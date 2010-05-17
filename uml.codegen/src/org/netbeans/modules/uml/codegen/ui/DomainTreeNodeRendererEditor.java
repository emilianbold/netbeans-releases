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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
