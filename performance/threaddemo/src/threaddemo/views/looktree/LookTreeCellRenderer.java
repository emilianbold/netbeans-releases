/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.views.looktree;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.*;
import javax.swing.tree.*;
import org.netbeans.spi.looks.Look;

/**
 * @author Jesse Glick
 */
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
