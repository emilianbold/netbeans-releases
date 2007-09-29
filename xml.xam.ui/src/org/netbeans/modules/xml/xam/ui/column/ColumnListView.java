/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

/**
 * This is a slightly hackish solution to get the ListView to show the
 * context menu of the root node if the user clicks in the area below
 * the last list node.
 *
 * <p>Note that we ignore the performObjectAt() method since we don't
 * have a need for that functionality. And it is not possible to override
 * anyway, due to package-private code.</p>
 *
 * @author  Nathan Fiedler
 */
public class ColumnListView extends ListView {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Popup menu support. */
    private transient PopupSupport popupSupport;
    /** Used to select nodes. */
    private ExplorerManager explorerManager;

    /**
     * Creates a new instance of ColumnListView.
     */
    public ColumnListView() {
        super();
        // For deserialization only
    }

    /**
     * Creates a new instance of ColumnListView.
     *
     * @param  em  ExplorerManager for selecting nodes.
     */
    public ColumnListView(ExplorerManager em) {
        super();
        explorerManager = em;
    }

    public void addNotify() {
        super.addNotify();
        MouseListener[] l = list.getMouseListeners();
        for (MouseListener ml : l) {
            if (ml instanceof ActionPerformer &&
                    ml instanceof FocusListener) {
                // This is the PopupSupport that is installed by ListView.
                // It is broken and needs to be replaced with our own.
                list.removeMouseListener(ml);
                list.removeFocusListener((FocusListener) ml);
                break;
            }
        }
        popupSupport = new PopupSupport();
        list.addFocusListener(popupSupport);
        list.addMouseListener(popupSupport);
    }

    protected JList createList() {
        return new ColumnList();
    }

    public void removeNotify() {
        super.removeNotify();
        list.removeFocusListener(popupSupport);
        list.removeMouseListener(popupSupport);
    }

    void createPopup(int xpos, int ypos, boolean context) {
        if (explorerManager == null) {
            return;
        }
        if (!isPopupAllowed()) {
            return;
        }

        if (context) {
            // For invisible root node, show its context menu.
            // Must set the node selected for this to work.
            Node[] nodes = new Node[] { explorerManager.getExploredContext() };
            try {
                explorerManager.setSelectedNodes(nodes);
            } catch (PropertyVetoException pve) {
                assert false : pve; // not permitted to be thrown
            }
        }
        Action[] actions = NodeOp.findActions(explorerManager.getSelectedNodes());
        JPopupMenu popup = Utilities.actionsToPopup(actions, this);
        if (popup != null && popup.getSubElements().length > 0) {
            popup.show(list, xpos, ypos);
        }
    }

    final class PopupSupport extends MouseUtils.PopupMouseAdapter
            implements ActionPerformer, Runnable, FocusListener {
        private CallbackSystemAction csa;

        protected void showPopup(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int i = list.locationToIndex(p);
            Rectangle r = list.getCellBounds(i, i);
            boolean contextMenu = (r == null) || !r.contains(p);
            if (!contextMenu && !list.isSelectedIndex(i)) {
                // Do not set the last item selected unless the user
                // actually clicked on it. This is to avoid conflicting
                // with createPopup() which will set the root node
                // selected if contextMenu is true.
                list.setSelectedIndex(i);
            }
            createPopup(e.getX(), e.getY(), contextMenu);
        }

        public void performAction(SystemAction act) {
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            boolean multisel = (list.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION);
            int i = (multisel ? list.getLeadSelectionIndex() : list.getSelectedIndex());
            if (i < 0) {
                return;
            }
            Point p = list.indexToLocation(i);
            if (p == null) {
                return;
            }
            createPopup(p.x, p.y, false);
        }

        @SuppressWarnings("deprecation")
        public void focusGained(FocusEvent ev) {
            if (csa == null) {
                try {
                    ClassLoader l = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
                    if (l == null) {
                        l = getClass().getClassLoader();
                    }
                    Class popup = Class.forName("org.openide.actions.PopupAction", true, l); // NOI18N
                    csa = (CallbackSystemAction) CallbackSystemAction.get(popup);
                } catch (ClassNotFoundException e) {
                    Error err = new NoClassDefFoundError();
                    ErrorManager.getDefault().annotate(err, e);
                    throw err;
                }
            }
            csa.setActionPerformer(this);
        }

        @SuppressWarnings("deprecation")
        public void focusLost(FocusEvent ev) {
            if (csa != null && csa.getActionPerformer() instanceof PopupSupport) {
                csa.setActionPerformer(null);
            }
        }
    }

    /**
     * Specialized JList that tracks the viewport width in order to
     * prevent horizontal scrolling within the columns view. This works
     * in concert with the list cell renderer to show the node display
     * name in truncated form (with ...) and an arrow border.
     *
     * @author  Nathan Fiedler
     */
    private class ColumnList extends JList {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        ColumnList() {
            super();

            // fix for 83915
            // copied from ListView.NbList
            // fix for #18292
            // default action map for JList defines these shortcuts
            // but we use our own mechanism for handling them
            // following lines disable default L&F handling (if it is
            // defined on Ctrl-c, Ctrl-v and Ctrl-x)
            getInputMap().put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
        }

        public boolean getScrollableTracksViewportWidth() {
            // Prevent horizontal scrolling in the column view.
            return true;
        }

        // Accessibility:
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleColumnList();
            }

            return accessibleContext;
        }

        private class AccessibleColumnList extends AccessibleJList {
            /** silence compiler warnings */
            private static final long serialVersionUID = 1L;

            AccessibleColumnList() {
            }

            public String getAccessibleName() {
                return ColumnListView.this.getAccessibleContext().getAccessibleName();
            }

            public String getAccessibleDescription() {
                return ColumnListView.this.getAccessibleContext().getAccessibleDescription();
            }
        }
    }
}
