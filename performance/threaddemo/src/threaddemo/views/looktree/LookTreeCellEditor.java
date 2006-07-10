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
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;

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
        return n.getLook().canRename(n.getData(), n.getLookup());
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
