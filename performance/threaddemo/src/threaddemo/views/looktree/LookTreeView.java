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

import java.awt.event.*;
import javax.swing.JTree;
import javax.swing.event.*;
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
