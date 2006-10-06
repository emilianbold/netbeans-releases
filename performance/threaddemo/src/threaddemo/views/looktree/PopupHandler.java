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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import org.netbeans.api.nodes2looks.Nodes;
import org.netbeans.spi.looks.Look;
import org.netbeans.spi.looks.Selectors;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

// XXX NodeAction's not necessarily handled well, e.g. Save... doesn't grok selection
// XXX other callback actions, like Copy, etc.

/**
 * Handle the popup menus on a look tree view.
 * @author Jesse Glick
 */
class PopupHandler extends MouseAdapter {
    
    private final LookTreeView view;
    private final ActionMap actionMap;
    private Reference<LookTreeNode> clicked;
    
    public PopupHandler(LookTreeView view) {
        this.view = view;
        actionMap = new ActionMap();
        actionMap.put("delete", new DeleteAction());
    }
    
    public void mousePressed(MouseEvent ev) {
        if (ev.isPopupTrigger()) {
            int x = ev.getX();
            int y = ev.getY();
            TreePath path = view.getClosestPathForLocation(x, y);
            if (path != null) {
                LookTreeNode n = (LookTreeNode)path.getLastPathComponent();
                clicked = new WeakReference<LookTreeNode>(n);
                @SuppressWarnings("unchecked")
                Action[] actions = n.getLook().getActions(n.getData(), n.getLookup() );
                // XXX handle multiselects...
                Node selection = makeNode( n );
                Lookup context = Lookups.fixed(new Object[] {selection, actionMap});
                JPopupMenu menu = Utilities.actionsToPopup(actions, context);
                menu.show(view, x, y);
                // XXX selection does not appear to be collected... do we need to
                // also destroy the popup menu?
            }
        }
    }
    
    /**
     * Makes a fake LookNode to serve as a "selection" for actions.
     * Only needed because actions currently expect a Node[].
     */
    private Node makeNode(LookTreeNode n) {
        final Look l = n.getLook();
        // Looks.node(o, l) does *not* work; it still tries to find
        // the node for the root, based on the default selector (naming).
        // Looks.defaultTypes is called but the result is ignored...
        return Nodes.node(n.getData(), l, Selectors.singleton( l ) );
    }
    
    private final class DeleteAction extends AbstractAction {
        
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            // XXX should confirm deletion first
            LookTreeNode n = clicked.get();
            try {
                n.getLook().destroy(n.getData(), n.getLookup() );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
}
