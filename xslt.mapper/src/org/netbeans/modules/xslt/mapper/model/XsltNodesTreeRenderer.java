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

package org.netbeans.modules.xslt.mapper.model;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;

/**
 * Tree renderer is intended to be used together with the XsltNodesTreeModel.
 *
 * @author nk160297
 */
public class XsltNodesTreeRenderer extends DefaultTreeCellRenderer {
    
    public XsltNodesTreeRenderer() {
    }
    
    public Component getTreeCellRendererComponent(
            JTree tree, Object value,
            boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        //
        super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);
        //
        if (value instanceof TreeNode) {
            TreeNode node = (TreeNode)value;
            //
            String name = node.getName(sel);
            if (name != null && name.length() != 0) {
                this.setText(name);
            }
            //
            Image img = node.getIcon();
            if (img != null) {
                this.setIcon(new ImageIcon(img));
            }
        }
        //
        return this;
    }
    
    
    
}
