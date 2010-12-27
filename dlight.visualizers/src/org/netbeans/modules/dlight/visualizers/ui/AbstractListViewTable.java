/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.visualizers.ui;

import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;

/**
 *
 * @author ak119685
 */
public abstract class AbstractListViewTable extends OutlineView {

    private Map<Integer, Boolean> ascColumnValues = new HashMap<Integer, Boolean>();

    public AbstractListViewTable(String nodeColumnName) {
        super(nodeColumnName);
        setDragSource(false);
        setDropTarget(false);
        setAllowedDragActions(DnDConstants.ACTION_NONE);
        setAllowedDropActions(DnDConstants.ACTION_NONE);

        final Outline outline = getOutline();
        outline.setRootVisible(false);
        outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outline.getTableHeader().setReorderingAllowed(false);
        outline.setRootVisible(false);

        ETableColumnModel colModel = (ETableColumnModel) getOutline().getColumnModel();
        TableColumn firstColumn = colModel.getColumn(0);
        ETableColumn col = (ETableColumn) firstColumn;
        col.setNestedComparator(new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    protected final void initActionMap() {
        final Outline outline = getOutline();

        // add Alt + Column Number for sorting

        int columnCount = outline.getColumnCount();

        int firstKey = KeyEvent.VK_1;

        for (int i = 1; i <= columnCount; i++) {
            final int columnNumber = i - 1;
            KeyStroke columnKey = KeyStroke.getKeyStroke(firstKey++, KeyEvent.ALT_MASK, true);
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(columnKey, "ascSortFor" + i); // NOI18N
            getActionMap().put("ascSortFor" + i, new AbstractAction() { // NOI18N

                @Override
                public void actionPerformed(ActionEvent e) {
                    // ok, do the sorting
                    int column = columnNumber;
                    ETableColumnModel columnModel = null;
                    if (outline.getColumnModel() instanceof ETableColumnModel) {
                        columnModel = (ETableColumnModel) outline.getColumnModel();
                        columnModel.clearSortedColumns();
                    }
                    boolean asc = !ascColumnValues.containsKey(column) ? true : ascColumnValues.get(column);
                    outline.setColumnSorted(column, asc, 1);
                    ascColumnValues.put(column, !asc);
                    outline.getTableHeader().resizeAndRepaint();
                }
            });
        }

        // On Escape focus parent component..
        KeyStroke returnKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);

//        outline.setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.singleton(returnKey));

        outline.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(returnKey, "return"); // NOI18N
        outline.getActionMap().put("return", new AbstractAction() { // NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                getFocusCycleRootAncestor().getFocusTraversalPolicy().getComponentBefore(getFocusCycleRootAncestor(), outline).requestFocus();
            }
        });
    }
}
