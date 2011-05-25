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
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.util.logging.Level;
import org.netbeans.modules.form.FormUtils;

/**
 * {@code GridInfoProvider} for {@code GrigBagLayout} layout manager.
 *
 * @author Jan Stola, Petr Somol
 */
public class GridBagInfoProvider implements GridInfoProvider {
    private Container container;
    /**
     * {@code tempX} field of GridBagConstraints used to get real grid X
     * coordinate. We cannot use {@code gridx} field because it can contain
     * {@code RELATIVE} value.
     */
    private Field tempXField;
    /**
     * {@code tempY} field of GridBagConstraints used to get real grid Y
     * coordinate. We cannot use {@code gridy} field because it can contain
     * {@code RELATIVE} value.
     */
    private Field tempYField;
    /**
     * {@code tempWidth} field of GridBagConstraints used to get real grid width.
     * We cannot use {@code gridwidth} field because it can contain {@code RELATIVE}
     * or {@code REMAINDER} value.
     */
    private Field tempWidthField;
    /**
     * {@code tempHeight} field of GridBagConstraints used to get real grid height.
     * We cannot use {@code gridheight} field because it can contain {@code RELATIVE}
     * or {@code REMAINDER} value.
     */
    private Field tempHeightField;

    public GridBagInfoProvider(Container container) {
        this.container = container;
        LayoutManager containerLayout = container.getLayout();
        if (!(containerLayout instanceof GridBagLayout)) {
            throw new IllegalArgumentException();
        }
        try {
            tempXField = GridBagConstraints.class.getDeclaredField("tempX"); // NOI18N
            tempXField.setAccessible(true);
            tempYField = GridBagConstraints.class.getDeclaredField("tempY"); // NOI18N
            tempYField.setAccessible(true);
            tempHeightField = GridBagConstraints.class.getDeclaredField("tempHeight"); // NOI18N
            tempHeightField.setAccessible(true);
            tempWidthField = GridBagConstraints.class.getDeclaredField("tempWidth"); // NOI18N
            tempWidthField.setAccessible(true);
        } catch (NoSuchFieldException nsfex) {
            FormUtils.LOGGER.log(Level.INFO, nsfex.getMessage(), nsfex);
        }
    }

    private GridBagLayout getLayout() {
        return (GridBagLayout)container.getLayout();
    }

    @Override
    public int getX() {
        return getLayout().getLayoutOrigin().x;
    }

    @Override
    public int getY() {
        return getLayout().getLayoutOrigin().y;
    }

    @Override
    public int getWidth() {
        int[] widths = getLayout().getLayoutDimensions()[0];
        int sum = 0;
        for (int width : widths) {
            sum += width;
        }
        return sum;
    }

    @Override
    public int getHeight() {
        int[] heights = getLayout().getLayoutDimensions()[1];
        int sum = 0;
        for (int height : heights) {
            sum += height;
        }
        return sum;    }

    @Override
    public int getColumnCount() {
        return getLayout().getLayoutDimensions()[0].length;
    }

    @Override
    public int getRowCount() {
        return getLayout().getLayoutDimensions()[1].length;
    }

    @Override
    public int[] getColumnBounds() {
        int[] widths = getLayout().getLayoutDimensions()[0];
        int[] bounds = new int[widths.length+1];
        bounds[0] = getX();
        for (int i=0; i<widths.length; i++) {
            bounds[i+1] = bounds[i] + widths[i];
        }
        return bounds;
    }

    @Override
    public int[] getRowBounds() {
        int[] heights = getLayout().getLayoutDimensions()[1];
        int[] bounds = new int[heights.length+1];
        bounds[0] = getY();
        for (int i=0; i<heights.length; i++) {
            bounds[i+1] = bounds[i] + heights[i];
        }
        return bounds;
    }

    private int getIntFieldValue(Field intField, Object object) {
        int value = -1;
        try {
            value = intField.getInt(object);
        } catch (IllegalAccessException iaex) {
            FormUtils.LOGGER.log(Level.INFO, iaex.getMessage(), iaex);
        }
        return value;
    }

    @Override
    public int getGridX(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridx = getIntFieldValue(tempXField, constraints);
        int columns = getColumnCount();
        return Math.min(gridx, columns-1); // See Issue 198519
    }

    public boolean getGridXRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridx == GridBagConstraints.RELATIVE;
    }

    @Override
    public int getGridY(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridy = getIntFieldValue(tempYField, constraints);
        int rows = getRowCount();
        return Math.min(gridy, rows-1); // See Issue 198519
    }

    public boolean getGridYRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridy == GridBagConstraints.RELATIVE;
    }

    @Override
    public int getGridWidth(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridWidth = getIntFieldValue(tempWidthField, constraints);
        int columns = getColumnCount();
        int gridx = getGridX(component);
        return Math.min(gridWidth, columns-gridx); // See Issue 198519
    }

    public boolean getGridWidthRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridwidth == GridBagConstraints.RELATIVE;
    }

    public boolean getGridWidthRemainder(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridwidth == GridBagConstraints.REMAINDER;
    }

    @Override
    public int getGridHeight(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridHeight = getIntFieldValue(tempHeightField, constraints);
        int rows = getRowCount();
        int gridy = getGridY(component);
        return Math.min(gridHeight, rows-gridy); // See Issue 198519
    }

    public boolean getGridHeightRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridheight == GridBagConstraints.RELATIVE;
    }

    public boolean getGridHeightRemainder(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridheight == GridBagConstraints.REMAINDER;
    }

    public int getAnchor(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.anchor;
    }

    public int getFill(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.fill;
    }

    public double getWeightX(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.weightx;
    }

    public double getWeightY(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.weighty;
    }

    public int getIPadX(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.ipadx;
    }

    public int getIPadY(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.ipady;
    }

    public Insets getInsets(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.insets;
    }

    @Override
    public void paintConstraints(Graphics g, Component component, boolean selected) {
        // PENDING painting of insets and remainders
    }

}
