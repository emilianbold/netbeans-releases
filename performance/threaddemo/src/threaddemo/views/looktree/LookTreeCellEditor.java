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
import java.util.EventObject;
import javax.swing.*;
import javax.swing.tree.*;
import org.netbeans.spi.looks.Look;

/**
 * @author Jesse Glick
 */
class LookTreeCellEditor extends DefaultTreeCellEditor {
    
    public LookTreeCellEditor(JTree tree, LookTreeCellRenderer r) {
        super(tree, r);
    }
    
    public boolean isCellEditable(EventObject ev) {
        if (!super.isCellEditable(ev)) {
            return false;
        }
        LookTreeNode n = (LookTreeNode)lastPath.getLastPathComponent();
        return n.getLook().canRename( n.getData(), n.getLookup() ).booleanValue();
        // XXX is it better to override JTree.isPathEditable?
    }
    
    protected TreeCellEditor createTreeCellEditor() {
        JTextField tf = new JTextField();
        Ed ed = new Ed(tf);
        ed.setClickCountToStart(1);
        return ed;
    }

    private static class Ed extends DefaultCellEditor {

        public Ed(JTextField tf) {
            super(tf);
        }

        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            LookTreeNode n = (LookTreeNode)value;
            delegate.setValue(n.getLook().getName(n.getData(), n.getLookup() ));
            ((JTextField)editorComponent).selectAll();
            return editorComponent;
        }
    }

}
