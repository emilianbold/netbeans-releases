/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import javax.accessibility.AccessibleContext;
import javax.swing.JList;
import javax.swing.KeyStroke;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;

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
    /** Used to select nodes. */
    private ExplorerManager explorerManager;

    /**
     * Creates a new instance of ColumnListView.
     */
    public ColumnListView() {
        super();
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

    protected JList createList() {
        return new ColumnList();
    }

//    void createPopup(int xpos, int ypos, boolean context) {
//        if (explorerManager == null) {
//            return;
//        }
//        if (!isPopupAllowed()) {
//            return;
//        }
//
//        if (context) {
//            // For invisible root node, show its context menu.
//            // Must set the node selected for this to work.
//            Node[] nodes = new Node[] { explorerManager.getExploredContext() };
//            try {
//                explorerManager.setSelectedNodes(nodes);
//            } catch (PropertyVetoException pve) {
//                assert false : pve; // not permitted to be thrown
//            }
//        }
//        Action[] actions = NodeOp.findActions(explorerManager.getSelectedNodes());
//        JPopupMenu popup = Utilities.actionsToPopup(actions, this);
//        if (popup != null && popup.getSubElements().length > 0) {
//            popup.show(list, xpos, ypos);
//        }
//    }

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
