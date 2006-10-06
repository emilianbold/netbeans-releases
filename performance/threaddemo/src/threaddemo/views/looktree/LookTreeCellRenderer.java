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

package threaddemo.views.looktree;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.spi.looks.Look;

/**
 * @author Jesse Glick
 */
@SuppressWarnings("unchecked")
class LookTreeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        LookTreeNode n = (LookTreeNode)value;
        Look l = n.getLook();
        if (leaf) {
            Image i = l.getIcon(n.getData(), BeanInfo.ICON_COLOR_16x16, n.getLookup() );
            if (i != null) {
                setLeafIcon(new ImageIcon(i));
            } else {
                setLeafIcon(getDefaultLeafIcon());
            }
        } else if (expanded) {
            Image i = l.getOpenedIcon(n.getData(), BeanInfo.ICON_COLOR_16x16, n.getLookup() );
            if (i != null) {
                setOpenIcon(new ImageIcon(i));
            } else {
                setOpenIcon(getDefaultOpenIcon());
            }
        } else {
            Image i = l.getIcon(n.getData(), BeanInfo.ICON_COLOR_16x16, n.getLookup() );
            if (i != null) {
                setClosedIcon(new ImageIcon(i));
            } else {
                setClosedIcon(getDefaultClosedIcon());
            }
        }
        String displayName = l.getDisplayName(n.getData(), n.getLookup() );
        setToolTipText(l.getShortDescription(n.getData(), n.getLookup() ));
        return super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
    }
    
}
