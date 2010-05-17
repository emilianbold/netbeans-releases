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

package org.netbeans.modules.soa.ui.nodes;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.openide.nodes.Node;

/**
 *
 * @author nk160297
 */
public class NodesTreeRenderer extends DefaultTreeCellRenderer {
    
    public NodesTreeRenderer() {
    }

    public Component getTreeCellRendererComponent(
            JTree tree, Object value, 
            boolean sel, boolean expanded, 
            boolean leaf, int row, boolean hasFocus) {
        //
        super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);
        //
        if (value instanceof Node) {
            Node node = (Node)value;
            //
            this.setText(node.getHtmlDisplayName());
            //
            if (expanded) {
                Image img = node.getOpenedIcon(BeanInfo.ICON_COLOR_16x16);
                if (img != null) {
                    this.setIcon(new ImageIcon(img));
                }
            } else {
                Image img = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                if (img != null) {
                    this.setIcon(new ImageIcon(img));
                }
            }
        }
        //
        return this;
    }
    
    
    
}
