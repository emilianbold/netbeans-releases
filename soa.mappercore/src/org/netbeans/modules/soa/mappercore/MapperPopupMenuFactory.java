/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.mappercore;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

/**
 *
 * @author AlexanderPermyakov
 */

public class MapperPopupMenuFactory {
    public static JPopupMenu createMapperPopupMenu(Canvas canvas, GraphItem graphItem) {
        JPopupMenu menu = new JPopupMenu();

        Action action = new CutMapperAction(canvas);
        if (graphItem == null || graphItem instanceof Link) {
            action.setEnabled(false);
        }
        JMenuItem item = new JMenuItem(action);
        Mnemonics.setLocalizedText(item, NbBundle.getMessage(CutAction.class, "Cut"));
        menu.add(item);

        action = new CopyMapperAction(canvas);
        if (graphItem == null || graphItem instanceof Link) {
            action.setEnabled(false);
        }
        item = new JMenuItem(action);
        Mnemonics.setLocalizedText(item, NbBundle.getMessage(CopyAction.class, "Copy"));
        menu.add(item);

        action = new PasteMapperAction(canvas);
        if (canvas.getBufferCopyPaste() == null) {
            action.setEnabled(false);
        }
        item = new JMenuItem(action);
        Mnemonics.setLocalizedText(item, NbBundle.getMessage(PasteAction.class, "Paste"));
        menu.add(item);

        action = new DeleteMapperAction(canvas);
        if (graphItem == null) {
            action.setEnabled(false);
        }
        item = new JMenuItem(action);
        Mnemonics.setLocalizedText(item, NbBundle.getMessage(DeleteAction.class, "Delete"));
        menu.add(item);

        if (graphItem instanceof VertexItem) {
            action = new EditAction((VertexItem) graphItem, canvas);
            menu.add(action);
        }
        
        if (graphItem instanceof Link) {
            menu.addSeparator();
            action = new ExpandAction((Link) graphItem, canvas.getLeftTree());
            menu.add(action);
        }
        return menu;
    }

    private static class ExpandAction extends AbstractAction {
        private Link link;
        private LeftTree leftTree;

        public ExpandAction(Link link, LeftTree leftTree) {
            super(NbBundle.getMessage(Canvas.class, "ExpandLeftTree")); // NOI18N
            this.leftTree = leftTree;
            this.link = link;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
                    KeyEvent.CTRL_DOWN_MASK));
            if (!(link.getSource() instanceof TreeSourcePin)) {
                setEnabled(false);
            }
        }

        public void actionPerformed(ActionEvent e) {
            TreePath leftTreePath = ((TreeSourcePin) link.getSource()).getTreePath();
            leftTree.setSelectionPath(leftTreePath);
        }
    }
    
    private static class EditAction extends AbstractAction {
        private VertexItem item;
        private Canvas canvas;

        public EditAction(VertexItem item, Canvas canvas) {
            super(NbBundle.getMessage(Canvas.class, "Edit")); // NOI18N
            this.canvas = canvas;
            this.item = item;

//            if (!(link.getSource() instanceof TreeSourcePin)) {
//                setEnabled(false);
//            }
        }

        public void actionPerformed(ActionEvent e) {
            TreePath treePath = canvas.getSelectionModel().getSelectedPath();
            canvas.startEdit(treePath, item);
        }
    }
}
