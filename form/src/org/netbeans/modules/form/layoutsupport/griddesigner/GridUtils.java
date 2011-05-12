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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import org.netbeans.modules.form.FormLoaderSettings;

/**
 * Utilities of the grid designer.
 *
 * @author Jan Stola
 */
public class GridUtils {
    /** Client property that marks padding component. */
    private static final String PADDING_COMPONENT = "dummyComponent"; // NOI18N
    /** Size of the empty column/row. */
    public static final int PADDING_SIZE = 20;

    /**
     * Determines whether the specified component is a padding component.
     *
     * @param comp component to check.
     * @return {@code true} if the specified component is a padding component,
     * returns {@code false} otherwise.
     */
    public static boolean isPaddingComponent(Component comp) {
        boolean padding = false;
        if (comp instanceof JComponent) {
            JComponent jcomp = (JComponent)comp;
            padding = jcomp.getClientProperty(PADDING_COMPONENT) != null;
        }
        return padding;
    }

    /**
     * Removes padding components from the grid managed by the specified manager.
     *
     * @param manager manager of the grid that we want to clean up.
     */
    public static void removePaddingComponents(GridManager manager) {
        Container cont = manager.getContainer();
        for (Component comp : cont.getComponents()) {
            if (isPaddingComponent(comp)) {
                manager.removeComponent(comp);
            }
        }
    }

    /**
     * Adds padding components into the grid managed by the specified manager.
     * It ensures that there are {@code columnNo} columns and {@code rowNo} rows.
     * It adds a padding into every row/column with height/width at most 1 (i.e. 0 or 1).
     *
     * @param manager manager if the grid that we want to update.
     * @param columnNo requested number of columns.
     * @param rowNo requested number of rows.
     */
    public static void addPaddingComponents(GridManager manager, int columnNo, int rowNo) {
        manager.updateLayout(false);
        boolean shouldPad = FormLoaderSettings.getInstance().getPadEmptyCells();
        if (!shouldPad) {
            return;
        }
        Container container = manager.getContainer();
        if (container.getComponentCount() == 0) {
            // Some layout managers (like GridBagLayout) do not layout empty
            // containers => add one obvious padding before revalidation
            Component padding = createPaddingComponent(true, true);
            manager.addComponent(padding, 0, 0, 1, 1);
        }
        // Avoid degenerated/empty case
        columnNo = Math.max(columnNo, 1);
        rowNo = Math.max(rowNo, 1);
        // The following arrays help to avoid infinite padding in some degenerated cases
        boolean[] paddedColumn = new boolean[columnNo];
        boolean[] paddedRow = new boolean[rowNo];
        // Workaround for problematic handling of components
        // with zero height or width by GridBagLayout. It always
        // put them into (0,0) location. We are padding them 
        // to avoid this problem.
        if (container.getLayout() instanceof GridBagLayout) {
            revalidateGrid(manager);
            int pad = 4;
            for (Component comp : container.getComponents()) {
                if (!GridUtils.isPaddingComponent(comp)) {
                    Dimension dim = comp.getSize();
                    if (dim.width == 0) {
                        Dimension minSize = comp.getMinimumSize();
                        Dimension prefSize = comp.getPreferredSize();
                        if (prefSize.width == 0) {
                            comp.setMinimumSize(new Dimension(pad, minSize.height));
                            comp.setPreferredSize(new Dimension(pad, prefSize.height));
                        }
                    }
                    if (dim.height == 0) {
                        Dimension minSize = comp.getMinimumSize();
                        Dimension prefSize = comp.getPreferredSize();
                        if (prefSize.height == 0) {
                            comp.setMinimumSize(new Dimension(minSize.width, pad));
                            comp.setPreferredSize(new Dimension(prefSize.width, pad));
                        }
                    }
                }
            }
        }
        boolean modified = true;
        while (modified) {
            // Addition of paddings can make other column/rows smaller
            // (when there is a component across several columns/rows)
            // => do several iterations of paddings
            modified = false;
            revalidateGrid(manager);
            GridInfoProvider info = manager.getGridInfo();
            int[] columnBounds = info.getColumnBounds();
            int[] rowBounds = info.getRowBounds();
            for (int i=0; i<columnNo; i++) {
                if (!paddedColumn[i] && (i>=columnBounds.length-1 || columnBounds[i]+1>=columnBounds[i+1])) {
                    Component padding = createPaddingComponent(true, false);
                    manager.addComponent(padding, i, 0, 1, 1);
                    paddedColumn[i] = true;
                    modified = true;
                }
            }
            for (int i=0; i<rowNo; i++) {
                if (!paddedRow[i] && (i>=rowBounds.length-1 || rowBounds[i]+1>=rowBounds[i+1])) {
                    Component padding = createPaddingComponent(false, true);
                    manager.addComponent(padding, 0, i, 1, 1);
                    paddedRow[i] = true;
                    modified = true;
                }
            }
        }
    }

    /**
     * Creates a padding component.
     *
     * @param horizontalPadding determines whether we are interested in horizontal padding.
     * @param verticalPadding determines whether we are interested in vertical padding.
     * @return new padding component.
     */
    private static Component createPaddingComponent(boolean horizontalPadding, boolean verticalPadding) {
        Dimension dim = new Dimension(horizontalPadding ? PADDING_SIZE : 0, verticalPadding ? PADDING_SIZE : 0);
        JComponent padding = (JComponent)Box.createRigidArea(dim);
        padding.putClientProperty(PADDING_COMPONENT, Boolean.TRUE);
        return padding;
    }

    /**
     * Forces revalidation of the grid managed by the specified manager.
     *
     * @param manager manager of the grid that we want to revalidate.
     */
    public static void revalidateGrid(GridManager manager) {
        Container cont = manager.getContainer();
        Container parent = cont.getParent();
        parent.invalidate();
        parent.doLayout();
        cont.invalidate();
        cont.doLayout();
    }

}
