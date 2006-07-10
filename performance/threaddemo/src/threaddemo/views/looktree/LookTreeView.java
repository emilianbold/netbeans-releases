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

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import org.netbeans.spi.looks.LookSelector;

/**
 * A raw tree view of objects and looks.
 * @author Jesse Glick
 */
public class LookTreeView extends JTree {

    public LookTreeView(Object o, LookSelector s) {
        super(new LookTreeModel(o, s));
        setLargeModel(true);
        LookTreeCellRenderer r = new LookTreeCellRenderer();
        setCellRenderer(r);
        setCellEditor(new LookTreeCellEditor(this, r));
        setEditable(true);
        setShowsRootHandles(true);
        setRowHeight( 20 );
        addTreeExpansionListener(new TreeCollector());
        addMouseListener(new PopupHandler(this));
    }
    
    private LookTreeModel getLookTreeModel() {
        return (LookTreeModel)getModel();
    }
    
    public void addNotify() {
        getLookTreeModel().addNotify();
        super.addNotify();
    }
    
    public void removeNotify() {
        super.removeNotify();
        getLookTreeModel().removeNotify();
    }
    
    private static final class TreeCollector implements TreeExpansionListener {
        
        public void treeCollapsed(TreeExpansionEvent event) {
            LookTreeNode n = (LookTreeNode)event.getPath().getLastPathComponent();
            n.forgetChildren();
        }
        
        public void treeExpanded(TreeExpansionEvent event) {
            // ignored
        }
        
    }
    
}
