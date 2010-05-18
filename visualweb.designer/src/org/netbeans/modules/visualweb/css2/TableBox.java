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
package org.netbeans.modules.visualweb.css2;

import java.util.List;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssComputedValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;

import org.openide.ErrorManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.Interaction;
import org.netbeans.modules.visualweb.designer.TableResizer;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;


/**
 * TableBox represents a &lt;table&gt; element.
 * <p>
 *
 * @todo Study http://www.mozilla.org/newlayout/doc/table-layout.html
 *   and see if we can improve this class a bit.
 * @todo Cell padding and cell spacing should be working; however,
 *   there are a couple of off-by-one errors, which means that small
 *   cell padding (1,2,3) does not look entirely right - clean this up.
 * @todo Gotta add more safety-checking. For example, the user can
 *   probably break things by specifying illegal (too large, or negative)
 *   colspan or rowspan attributes, or in the fixed table layout, include
 *   more columns than are indicated in the first row or with col
 *   elements.
 * @todo RFC 1942 suggests that the COLS attribute on a TABLE element
 *    can set the number of columns, and force fixed layout. Check whether
 *    this is still the case (and if it should be supported).
 * @todo Colspan work: when computing the number of columns, the column
 *    count might be affected by overlapping rowspans - e.g. even though
 *    row two only has a single td, the previous row might have a rowspan
 *    which blocks the first column, so it creates a new column. See if
 *    Mozilla handles this, and if so, change creation code to resize
 *    the cell table and adjust the column count if so.
 * @todo Collapsed borders needs more work. Here's a potentially useful
 *    resource: http://fantasai.inkedblade.net/style/discuss/collapsed-outer-border/
 *    Note also that border types "inset" and "outset" have different
 *    meanings in the collapsed border model - see 17.6.3
 * @todo Scale percentages:
 <pre>
        if (percentTotals > 100) {
            for (int i = 0; i < percentChildren.length; i++) {
                if (percentChildren[i] > 0) {
                    percentChildren[i] =
                        (percentChildren[i] * 100) / percentTotals;
                }
            }
            percentTotals = 100;
        }
 </pre>
 * @todo Handle relative lengths (*, 2*, 3*), like I do in FrameSetBox.
 * @todo Alignment: Look at this URL:
 *     http://www.nic.fi/~tapio1/Teaching/Taulukot0.php3
 *    and make sure I don't have those problems.
 * @todo Support proportional width definitions:
 *     http://www.w3.org/TR/html401/struct/tables.html#edef-TABLE
 * @todo Ensure that if the table is positioned, the CaptionedTableBox
 *     is repositioned, not just the table!
 * @todo Override CssBox.find here; for a table we can a lot more efficiently
 *     identify the box under a coordinate for our children since we know
 *     the exact table layout.
* @todo Handle division by zero problem in my "spread out width" code
 *     to deal with empty content tables.
 * @todo Handle the BORDERCOLOR, BORDERCOLORLIGHT and BORDERCOLORDARK attributes.
 * @todo Handle align/valign on the td's.
 * @todo Ensure that th's are handled correctly.
 * @todo Empty table cells should not get a cell border - they currently
 *        do
 *
 * @author Tor Norbye
 */
public class TableBox extends ContainerBox {
    /** Special marker in cell positions which simply indicate that
     * the cell is occupied by some previous cell with a colspan or
     * or rowspan greater than 1.
     */
    private static final CellBox OCCUPIED = new OccupiedBox();
    private static final int BORDER_RESIZE_DISTANCE = 5;

    /** The element for the &lt;table&gt; tag itself.*/
//    private RaveElement table;
    private Element table;

    /** Number of columns in the table. */
    private int columns = -1;

    /** Number of rows in the table */
    private int rows = -1;

//    /** Table Design Info. Can be null for table components that don't
//     *  implement this. */
//    private MarkupTableDesignInfo tableDesignInfo;
    private int cellPadding = 1; // What is mozilla's default?
    private int cellSpacing = 1; // What is mozilla's default?
    private int borderWidth = 0;

    /** The HTML4 spec says the default frame value is "void" but that does
     * not seem to be what Mozilla does. */
    private int frame = CssBorder.FRAME_UNSET;
    private int rules = CssBorder.FRAME_UNSET;

    /** Which kind of layout to use: fixed algorithm, or auto-layout algorithm.
     * This is chosen bvased on the value of the css property "table-layout" as
     * well as depending on whether the width property is set.
     */
    private boolean fixedLayout;

    /** Array of cells holding boxes for each cell. Some cells may be empty
     * (e.g. where the table does not specify all the cells, or has colspans/
     * rowspans > 1.)
     */
    private CellBox[][] cells;

    /** The rowspan for a given cell. If 0, it means uninitialized so use 1, the
     * default, instead. */
    private int[][] rowspans;

    /** The colspan for a given cell. If 0, it means uninitialized so use 1, the
     * default, instead. */
    private int[][] colspans;

    /** The row elements. Used for CSS lookups of row heights. */
    private Element[] rowElements;

    /** Computed height for each row, after layout */
    private int[] rowHeights;

    /** Sidedoor flag for column computation routine such that
     * it knows to pick the column maxes rather than look at the containing
     * block when picking final column widths. */
    private boolean computingPrefWidth = false;

    /** List of caption boxes, if any */
    private List<CssBox> captionBoxes;

    /** Applies only when there is a caption: if true, place caption above, otherwise below table */
    private boolean captionAbove;

    /** X coordinate of the table itself. 0 when there is no caption, otherwise possibly
     * offset.
     */
    private int tableLeft;

    /** Y coordinate of the table itself. 0 when there is no caption, otherwise possibly
     * offset when the caption is above the table.
     */
    private int tableTop;

    /** X coordinate of the table right hand side of the table. 0 when there is no caption,
     * otherwise possibly offset (this will be the case when the caption is wider than the
     * text.
     */
    private int tableRight;

    /** Y coordinate of the bottom of the table itself. 0 when there is no caption,
     * otherwise possibly offset when the caption is below the table.
     */
    private int tableBottom;

    // If you add additional arrays here, make sure you update the swapRow()
    // method to include it

    /**
     *  Create a TableBox representing a table for the given element
     *
     * @param element The table element
     * @todo A table is never replaced, is it? Can we get rid of that
     *   parameter?
     */
    private TableBox(WebForm webform, Element element, BoxType boxType,
    boolean inline, boolean replaced) {
        super(webform, element, boxType, inline, replaced);
//        this.table = (RaveElement)element;
        this.table = element;
    }

    /** Factory for creating a table */
    public static CssBox getTableBox(WebForm webform, Element element, BoxType boxType,
        boolean inline, boolean replaced) {
        return new TableBox(webform, element, boxType, inline, replaced);
    }

    /**
     * {@inheritDoc}
     *
     * Specialized in TableBox to handle centering, since when centering is in effect
     * we should assume a minimal table width (shrink to fit) and change the margins
     * to be auto
     */
    protected void computeHorizNonInlineNormalFlow(FormatContext context, int parentWidth) {
//        Value al = CssLookup.getValue(getElement(), XhtmlCss.TEXT_ALIGN_INDEX);
        CssValue cssAl = CssProvider.getEngineService().getComputedValueForElement(getElement(), XhtmlCss.TEXT_ALIGN_INDEX);

//        if (al == CssValueConstants.RAVECENTER_VALUE) {
        if (CssProvider.getValueService().isRaveCenterValue(cssAl)) {
            leftMargin = AUTO;
            rightMargin = AUTO;

            if (contentWidth == AUTO) {
                int availableWidth =
                    parentWidth - 0 - 0 //leftMargin=0,left=0
                        // Don't pre-subtracxt padding and borders since in the shrink to fit
                      // calculation for tables I add these in. Revisit this. (See comments
                        // under get preferred width calculation in the table.
                    // -leftBorderWidth - leftPadding - rightPadding - rightBorderWidth
                        - 0 - 0; // rightMargin=0, right=0;
                contentWidth = shrinkToFit(availableWidth, context);
                // For the table this will already include the borders and padding, since
                // the width property for tables are interpreted that way.
                // But that's not
                // how normal box computations work! Subtract them back out here.
                // TODO -- fix shrinkToFit so it doesn't do this for tables - and figure out
                // how that impacts nested tables and such.
                contentWidth -= (leftPadding+rightPadding+leftBorderWidth+rightBorderWidth);
            }
        }

        super.computeHorizNonInlineNormalFlow(context, parentWidth);
    }

    /**
     * Implement the fixed table layout algorithm, described in the
     * CSS2.1 spec:
     * http://www.w3.org/TR/CSS21/tables.html#fixed-table-layout
     */
    private void fixedLayout(int[] columnWidths, FormatContext context) {
        assert (rows > 0) && (columns > 0);

        computeFixedColumnWidths(columnWidths, false);
        formatCells(columnWidths, context);
        positionCells(columnWidths, context);
    }

    private void computeFixedColumnWidths(int[] columnWidths, boolean scan) {
        for (int i = 0; i < columns; i++) {
            columnWidths[i] = AUTO;
        }

        int tableWidth = getTableWidth(scan);

        // Compute column widths:
        // 1. "A column element with a value other than 'auto' for the
        // 'width' property sets the width for that column."
        // 2. "Otherwise, a cell in the first row with a value other
        // than 'auto' for the 'width' property sets the width for that
        // column. If the cell spans more than one column, the width is
        // divided over the columns."
        NodeList list = table.getChildNodes();
        int len = list.getLength();
        int currentColumn = 0;

        for (int i = 0; i < len; i++) {
            Node child = (Node)list.item(i);

            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element)child;
//            Value display = CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX);

//            if (display == CssValueConstants.TABLE_COLUMN_GROUP_VALUE) {
            if (CssProvider.getValueService().isTableColumnGroupValue(cssDisplay)) {
                // XXX I shouldn't look for and process width attributes on
                // <colgroups> should I?
                NodeList list2 = element.getChildNodes();
                int len2 = list2.getLength();

                for (int j = 0; j < len2; j++) {
                    Node child2 = (Node)list2.item(j);

                    if (child2.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element element2 = (Element)child2;

//                    if (CssLookup.getValue(element2, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_COLUMN_VALUE) {
                    if (CssProvider.getValueService().isTableColumnValue(CssProvider.getEngineService().getComputedValueForElement(element2, XhtmlCss.DISPLAY_INDEX))) {
                        int colSpan =
                            HtmlAttribute.getIntegerAttributeValue(element2, HtmlAttribute.SPAN, 1);

                        if (colSpan <= 0) { // HTML 11.2.6: "0" means fill the row. TODO.
                            colSpan = 1;
                        }

                        computeFixedColumnWidth(columnWidths, currentColumn, element2, colSpan,
                            tableWidth);
                        currentColumn += colSpan;
                    }
                }
//            } else if (display == CssValueConstants.TABLE_COLUMN_VALUE) {
            } else if (CssProvider.getValueService().isTableColumnValue(cssDisplay)) {
                int colSpan =
                    HtmlAttribute.getIntegerAttributeValue(element, HtmlAttribute.SPAN, 1);

                if (colSpan <= 0) { // HTML 11.2.6: "0" means fill the row. TODO.
                    colSpan = 1;
                }

                computeFixedColumnWidth(columnWidths, currentColumn, element, colSpan, tableWidth);
                currentColumn += colSpan;
            }
        }

        boolean needMoreWidths = false;

        for (int i = 0; i < columns; i++) {
            if (columnWidths[i] == AUTO) {
                needMoreWidths = true;

                break;
            }
        }

        if (needMoreWidths) {
            // I didn't get all the widths I needed from the col elements.
            // So look at the first row too.
            boolean seenFirstRow = false;

            for (int i = 0; i < len; i++) {
                if (seenFirstRow) {
                    break;
                }

                Node trn = (Node)list.item(i);

                if (trn.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element tr = (Element)trn;
//                Value display = CssLookup.getValue(tr, XhtmlCss.DISPLAY_INDEX);
                CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tr, XhtmlCss.DISPLAY_INDEX);

//                if (display == CssValueConstants.TABLE_ROW_VALUE) {
                if (CssProvider.getValueService().isTableRowValue(cssDisplay)) {
                    computeFixedColumnWidth(columnWidths, tr, tableWidth);

                    break;
//                } else if ((display == CssValueConstants.TABLE_ROW_GROUP_VALUE) ||
//                        (display == CssValueConstants.TABLE_HEADER_GROUP_VALUE) ||
//                        (display == CssValueConstants.TABLE_FOOTER_GROUP_VALUE)) {
                } else if (CssProvider.getValueService().isTableRowGroupValue(cssDisplay)
                || CssProvider.getValueService().isTableHeaderGroupValue(cssDisplay)
                || CssProvider.getValueService().isTableFooterGroupValue(cssDisplay)) {
                    NodeList list2 = tr.getChildNodes();
                    int len2 = list2.getLength();

                    for (int j = 0; j < len2; j++) {
                        Node trn2 = (Node)list2.item(j);

                        if (trn2.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        Element tr2 = (Element)trn2;

//                        if (CssLookup.getValue(tr2, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_ROW_VALUE) {
                        if (CssProvider.getValueService().isTableRowValue(CssProvider.getEngineService().getComputedValueForElement(tr2, XhtmlCss.DISPLAY_INDEX))) {
                            computeFixedColumnWidth(columnWidths, tr2, tableWidth);
                            seenFirstRow = true;

                            break;
                        }
                    }
                }
            }
        }

        int numRemaining = countUnassignedColumns(columnWidths);

        if (numRemaining > 0) {
            // 3. Any remaining columns equally divide the remaining
            // horizontal table space (minus borders or cell spacing).
            int portion = tableWidth / numRemaining;
            int remainder = tableWidth % numRemaining;
            int nextPos = 0;

            for (int k = 0; k < numRemaining; k++) {
                // Find next unassigned column
                while (columnWidths[nextPos] != AUTO) {
                    nextPos++;
                }

                columnWidths[nextPos] = portion;

                if (k == 0) {
                    // The first column also gets the
                    // remainder
                    columnWidths[nextPos] += remainder;
                }

                nextPos++;
            }
        }

        // From CSS21 17.5.2:
        //     The width of the table is then the greater of the value
        //     of the 'width' property for the table element and the sum
        //     of the column widths (plus cell spacing or borders). If
        //     the table is wider than the columns, the extra space
        //     should be distributed over the columns.
        // Should we distribute proportionally or assign equal amounts to
        // each column? Mozilla seems to do it proportionally so lets do that.
        int totalWidth = 0;

        for (int i = 0; i < columns; i++) {
            totalWidth += columnWidths[i];
        }

        if (totalWidth >= tableWidth) {
            tableWidth = totalWidth;
        } else {
            // Extra space - distribute.
            int leftOver = tableWidth - totalWidth;
            int assigned = 0;

            for (int i = 0; i < columns; i++) {
                int portion = (columnWidths[i] * leftOver) / totalWidth;
                columnWidths[i] += portion;
                assigned += portion;
            }

            columnWidths[0] += (leftOver - assigned); // remainder from rounding errors
        }
    }

    /** Set the position, width and height and alignment for all the cells of the table,
     * and set the total width/height of the table itself based on the
     * cumulative size of the rows and columns.
     */
    private void positionCells(int[] columnWidths, FormatContext context) {
        /** Special meaning: positive numbers: fixed width. Negative
         * numbers: negative percentage. AUTO: unconstrained
         * (default) */
        int[] constraints = null;
        int desiredHeight = getTableHeight();

        if (desiredHeight == AUTO) {
            // If there is no table height set, don't compoute
            // constraints since we won't need them.
            desiredHeight = 0;
        } else {
            constraints = new int[rows];
        }

        rowHeights = new int[rows];

        for (int i = 0; i < rows; i++) {
            int rowHeight = 0;

            if (rowElements[i] != null) {
//                Value value = CssLookup.getValue(rowElements[i], XhtmlCss.HEIGHT_INDEX);
                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(rowElements[i], XhtmlCss.HEIGHT_INDEX);

//                if (value == CssValueConstants.AUTO_VALUE) {
                if (CssProvider.getValueService().isAutoValue(cssValue)) {
                    rowHeight = 0;

                    if (constraints != null) {
                        // AUTO means use minimum necessary value
                        constraints[i] = AUTO;
                    }

                    // constraints left at AUTO, but don't set since we
                    // don't initialize constraints unless we have to
                } else {
//                    boolean wasPercentage =
//                        value instanceof ComputedValue &&
//                        (((ComputedValue)value).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE);
                    boolean wasPercentage = cssValue instanceof CssComputedValue
                                                && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue());

                    if (wasPercentage) {
                        rowHeight = 0;

//                        int percentage =
//                            (int)((ComputedValue)value).getCascadedValue().getFloatValue();
                        int percentage = (int)((CssComputedValue)cssValue).getCascadedValue().getFloatValue();

                        if (percentage < 0) {
                            percentage = 0;
                        }

                        if (constraints != null) {
                            // negative numbers indicates percentage
                            constraints[i] = -percentage;
                        }
                    } else {
//                        rowHeight = (int)value.getFloatValue();
                        rowHeight = (int)cssValue.getFloatValue();

                        if (rowHeight < 0) {
                            rowHeight = 0;
                        }

                        if (constraints != null) {
                            // positive numbers indicates actual length
                            constraints[i] = rowHeight;
                        }
                    }
                }
            }

            for (int j = 0; j < columns; j++) {
                CssBox box = cells[i][j];

                if ((box == null) || (box == OCCUPIED)) {
                    continue;
                }

                int rowspan = rowspans[i][j];

                if (rowspan == 1) {
                    if (box.getHeight() > rowHeight) {
                        rowHeight = box.getHeight();
                    }
                } // else: we take care of multi-row cells in a second pass

                // when we have all the individual-cell heights; we then
                // distribute the minimum height required by the cell
                // over the rows, if necessary.
                int colspan = colspans[i][j];
                int w = 0;

                for (int m = 0; m < colspan; m++) {
                    w += columnWidths[j + m];
                }

                box.width = w - cellSpacing;
                box.contentWidth =
                    box.width -
                    (box.leftBorderWidth + box.leftPadding + box.rightPadding +
                    box.rightBorderWidth);
            }

            rowHeights[i] = rowHeight;
        }

        // Account for tall cells that span multiple rows: here we need
        // to make sure that the sum of the rows spanning the cell is
        // as large as the cell height
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                CssBox box = cells[i][j];

                if ((box == null) || (box == OCCUPIED)) {
                    continue;
                }

                int rowspan = rowspans[i][j];

                if (rowspan > 1) {
                    int rowsum = 0;

                    for (int m = 0; m < rowspan; m++) {
                        rowsum += rowHeights[i + m];
                    }

                    if (box.getHeight() > rowsum) {
                        // Distribute extra height proportionally over the
                        // existing rows. Could probably use a better
                        // algorithm than that, but... revisit.
                        // 
                        int leftOver = box.getHeight() - rowsum;
                        int assigned = 0;

                        for (int m = 0; m < rowspan; m++) {
                            // XXX #91134 Possible division by zero.
//                            int portion = (rowHeights[i + m] * leftOver) / rowsum;
                            int portion = rowsum == 0 ? 0 : (rowHeights[i + m] * leftOver) / rowsum;
                            
                            rowHeights[i + m] += portion;
                            assigned += portion;
                        }

                        rowHeights[i] += (leftOver - assigned); // remainder from rounding errors
                    }
                }
            }
        }

        //contentHeight = y-topBorderWidth-topPadding;
        contentHeight = 0;

        for (int m = 0; m < rows; m++) {
            contentHeight += rowHeights[m];
        }

        // Now let's see if we should adjust the height upwards in case
        // the user has set a larger height Note that we DON'T shrink
        // the table. According to CSS2.1 section 17.5.3 the behavior
        // here is undefined.
        if (desiredHeight > contentHeight) {
            // Distribute the available space. First give space to the
            // percentage rows. Then assign the rest proportionally
            // to the remaining UNCONSTRAINED rows. Note that there
            // may be no such columns.  If so we will ignore the
            // requested height on the table.
            int leftOver = desiredHeight - contentHeight;
            int assigned = 0;

            // TODO - Mozilla seems to leave header cells alone, it does not
            // grow these at all....
            for (int i = 0; i < rows; i++) {
                if (constraints[i] < 0) {
                    int percent = -constraints[i];
                    int portion = (percent * leftOver) / 100;
                    rowHeights[i] += portion;
                    assigned += portion;

                    if (assigned > leftOver) {
                        rowHeights[i] -= (assigned - leftOver);
                        assigned = leftOver;

                        break;
                    }
                }
            }

            if (assigned < leftOver) {
                leftOver = leftOver - assigned;
                assigned = 0;

                // Spread the remainder over the unconstrained
                int unconstrained = 0;

                for (int i = 0; i < rows; i++) {
                    if (constraints[i] == AUTO) {
                        unconstrained += rowHeights[i];
                    }
                }

                if (unconstrained > 0) {
                    int lastCol = -1;

                    for (int i = 0; i < rows; i++) {
                        if (constraints[i] == AUTO) {
                            int portion = (rowHeights[i] * leftOver) / unconstrained;
                            rowHeights[i] += portion;
                            assigned += portion;
                            lastCol = i;
                        }
                    }

                    // Remainder from potential rounding errors
                    rowHeights[lastCol] += (leftOver - assigned);
                } else {
                    // There are no unconstrained columns. Hand out the
                    // rest to the percentages, if any
                    int percentageSum = 0;

                    for (int i = 0; i < rows; i++) {
                        if (constraints[i] < 0) {
                            int percent = -constraints[i];
                            percentageSum += percent;
                        }
                    }

                    if (percentageSum > 0) {
                        int lastCol = -1;

                        for (int i = 0; i < rows; i++) {
                            if (constraints[i] < 0) {
                                int percent = -constraints[i];
                                int portion = (percent * leftOver) / percentageSum;
                                rowHeights[i] += portion;
                                assigned += portion;
                                lastCol = i;
                            }
                        }

                        // Remainder from potential rounding errors
                        rowHeights[lastCol] += (leftOver - assigned);
                    }
//                    else {
//                        // You have a fully constrained table with
//                        // assigned heights -- height cannot be
//                        // accomodated. Use table computed height.
//                    }
                }
            }

            // Set contentHeight. I -might- be able to just do
            // contentHeight = desiredHeight but I need to check border
            // accounting etc.
            contentHeight = 0;

            for (int m = 0; m < rows; m++) {
                contentHeight += rowHeights[m];
            }
        }

        // We've already accounted for the cellspacing above all the cells
        // (as part of the cell bounds) but we need spacing below the bottom-most
        // row as well
        contentHeight += cellSpacing;
        super.height =
            topBorderWidth + topPadding + contentHeight + bottomPadding + bottomBorderWidth;
        contentWidth = 0;

        for (int m = 0; m < columns; m++) {
            contentWidth += columnWidths[m];
        }

        contentWidth += cellSpacing; // spacing behind rightmost column
        super.width =
            leftBorderWidth + leftPadding + contentWidth + rightPadding + rightBorderWidth;

        // Assign box height
        // Make another pass over the table and assign
        // heights to all the cells. We couldn't do that
        // in the first pass since we hadn't looked up the
        // total row height for the WHOLE table yet - and
        // we need all the row heights so that we can compute
        // cell heights for cells that have rowspans referring
        // to rows below the current row.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                CssBox box = cells[i][j];

                if ((box == null) || (box == OCCUPIED)) {
                    continue;
                }

                int rowspan = rowspans[i][j];
                int h = 0;

                for (int m = 0; m < rowspan; m++) {
                    h += rowHeights[i + m];
                }

                //box.contentHeight = h;
                //box.height = box.topBorderWidth+box.topPadding+box.contentHeight+box.bottomPadding+box.bottomBorderWidth;
                box.height = h - cellSpacing;
                box.contentHeight =
                    box.height -
                    (box.topBorderWidth + box.topPadding + box.bottomPadding +
                    box.bottomBorderWidth);
            }
        }

        formatCaption(context, true, super.width, super.height);

        // Assign positions
        int y = tableTop + topBorderWidth + topPadding + cellSpacing;

        for (int i = 0; i < rows; i++) {
            int x = leftBorderWidth + leftPadding + cellSpacing + tableLeft;

            for (int j = 0; j < columns; j++) {
                CellBox box = cells[i][j];

                if ((box != null) && (box != OCCUPIED)) {
                    box.setLocation(x, y);

                    // I can't move the box itself to align, since the
                    // box itself paints backgrounds, cell borders, etc.
                    // The box -contents- have to be shifted instead.
                    box.align();
                }

                x += columnWidths[j];
            }

            y += rowHeights[i];
        }

        formatCaption(context, false, super.width, super.height);
    }

    /** Format the optional caption. Called both above and below the table such that
     * the table can react accordingly. This method will update variables like the
     * width and height settings of the box, the tableTop/tableLeft/tableRight/tableBottom
     * fields, etc., when applicable.
     */
    private void formatCaption(FormatContext context, boolean above, int width, int height) {
        if (captionBoxes == null) {
            return;
        }

        if (above && captionAbove) {
            CssBox prevBox = null;

            int x = 0;
            int y = 0;

            for (int i = 0, n = captionBoxes.size(); i < n; i++) {
                CssBox box = captionBoxes.get(i);

                if (box.getBoxType().isNormalFlow()) {
                    prevBox = box;
                }

                // Should be a ContainerBox, for the caption element
                box.setX(x);
                box.setY(y);

                // The containing block as obtained in layoutChild's call to setContainingBlock
                // will be asking for the contentWidth of its parent, the table, so set that
                // here...
                int oldContentWidth = contentWidth;
                contentWidth = width;

                try {
                    layoutChild(box, context, true);
                } finally {
                    contentWidth = oldContentWidth;
                }

                super.height += box.height;
                super.contentHeight += box.height;
                y += box.height;

                if (box.width > this.width) {
                    int diff = box.width - this.width;
                    contentWidth += diff;
                    this.width = box.width;

                    // Firefox does not center the table when the caption is too wide
                    // so let's do the same
                    //int extra = (this.width-width)/2;
                    //tableRight = extra;
                    //tableLeft = extra;
                    // So instead just put more room on the right
                    tableRight = this.width - width;
                }
            }

            // Compute collapse between caption and table, and from there, the
            // table y position
            if (prevBox != null) {
                int margin;

                int prevMargin = prevBox.getCollapsedBottomMargin();
                int boxMargin = getCollapsedTopMargin(); // the table

                if ((prevMargin >= 0) && (boxMargin >= 0)) {
                    // Normal case
                    //  The larger of adjacent margin values is used.
                    margin = Math.max(prevMargin, boxMargin);

                    // OLD:
                } else if ((prevMargin < 0) && (boxMargin < 0)) {
                    // If the adjacent margins are all negative, the larger
                    // of the negative values is used.
                    // XXX this is not how I re-read the spec; it says "If
                    // there are no positive margins, the absolute maximum
                    // of the negative adjoining margins is deducted from
                    // zero."   So I take abs
                    //margin = Math.max(-prevMargin, -boxMargin);
                    margin = Math.min(prevMargin, boxMargin);
                } else {
                    // If positive and negative vertical margins are
                    // adjacent, the value should be collapsed thus: the
                    // largest of the negative margin values should be
                    // subtracted from the largest positive margin value.
                    if ((prevMargin >= 0) && (boxMargin < 0)) {
                        margin = prevMargin + boxMargin;
                    } else {
                        assert (prevMargin < 0) && (boxMargin >= 0);
                        margin = boxMargin + prevMargin;
                    }
                }

                tableTop = prevBox.getY() + prevBox.getHeight();
                effectiveTopMargin = margin;
                prevBox.effectiveTopMargin = 0;
            }

            tableRight = tableLeft + width;
            tableBottom = tableTop + height;
        } else if (!above && !captionAbove) {
            tableTop = 0;
            tableLeft = 0;
            tableBottom = this.height;

            int x = 0;
            int y = this.height;

            for (int i = 0, n = captionBoxes.size(); i < n; i++) {
                CssBox box = captionBoxes.get(i);

                // Should be a ContainerBox, for the caption element
                box.setX(x);
                box.setY(y);

                int oldContentWidth = contentWidth;
                contentWidth = width; // See related comment in captionAbove section

                try {
                    layoutChild(box, context, true);
                } finally {
                    contentWidth = oldContentWidth;
                }

                super.height += box.height;
                super.contentHeight += box.height;
                y += box.height;

                if (box.width > this.width) {
                    int diff = box.width - this.width;
                    contentWidth += diff;
                    this.width = box.width;

                    // XXX Can't do this here, it's too late; I've already positioned all
                    // the table cells....
                    //int extra = (this.width-width)/2;
                    //tableRight = extra;
                    //tableLeft = extra;
                    // So instead just put more room on the right
                    tableRight = this.width - width;
                }
            }

            tableRight = tableLeft + width;
        }
    }

    /** Count how many widths in the columnWidths array are still
     * unassigned.
     */
    private int countUnassignedColumns(int[] columnWidths) {
        int numRemaining = 0;

        for (int i = 0; i < columns; i++) {
            if (columnWidths[i] == AUTO) {
                numRemaining++;
            }
        }

        return numRemaining;
    }

    /** Initialize the column widths for the given element (may be
     * a &lt;col&gt; or a &lt;td&gt; or a &lt;th&gt;), following
     * the fixed table layout algorithm.
     */
    private void computeFixedColumnWidth(int[] columnWidths, int column, Element element,
        int colSpan, int parentWidth) {
        // Compute the width of the column. We can't just use the
        // default getLength behavior on the element, since we want
        // percentages to be relative to the parent width, which is not
        // the same thing as the containing block width (because for tables,
        // we subtract border widths etc. from the width unlike normal
        // box behavior. E.g. a table with "width: 100%" and "border-width: 10"
        // will exactly fill the containing block, rather than be 20 pixels
        // wider than it if you had set the same properties on a div.
        int w;
//        Value value = CssLookup.getValue(element, XhtmlCss.WIDTH_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.WIDTH_INDEX);

//        if (value == CssValueConstants.AUTO_VALUE) {
        if (CssProvider.getValueService().isAutoValue(cssValue)) {
            w = AUTO;
        } else {
//            boolean wasPercentage =
//                value instanceof ComputedValue &&
//                (((ComputedValue)value).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE);
            boolean wasPercentage = cssValue instanceof CssComputedValue 
                    && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue());

            if (wasPercentage) {
//                w = ((int)((ComputedValue)value).getCascadedValue().getFloatValue() * parentWidth) / 100;
                w = ((int)((CssComputedValue)cssValue).getCascadedValue().getFloatValue() * parentWidth) / 100;
            } else {
//                w = (int)value.getFloatValue();
                w = (int)cssValue.getFloatValue();
            }
        }

        if (w != AUTO) {
            if (colSpan > 1) {
                // Divide up between the columns
                int portion = w / colSpan;

                for (int k = 0; (k < colSpan) && ((column + k) < columns); k++) {
                    columnWidths[column + k] = portion;

                    if (k == 0) {
                        // The first column also gets the
                        // remainder
                        columnWidths[column] += (w % colSpan);
                    }
                }
            } else {
                columnWidths[column] = w;
            }
        }
    }

    /** Initialize the column widths for the given row following
     * the fixed table layout algorithm.
     */
    private void computeFixedColumnWidth(int[] columnWidths, Element tr, int parentWidth) {
        NodeList list2 = tr.getChildNodes();
        int len2 = list2.getLength();
        int column = 0;

        for (int j = 0; j < len2; j++) {
            Node tdthn = (Node)list2.item(j);

            if (tdthn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tdth = (Element)tdthn;
//            Value display = CssLookup.getValue(tdth, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tdth, XhtmlCss.DISPLAY_INDEX);

//            if (display != CssValueConstants.TABLE_CELL_VALUE) {
            if (CssProvider.getValueService().isTableCellValue(cssDisplay)) {
                continue;
            }

            int colSpan = HtmlAttribute.getIntegerAttributeValue(tdth, HtmlAttribute.COLSPAN, 1);

            if (colSpan <= 0) { // HTML 11.2.6: "0" means fill the row. TODO.
                colSpan = 1;
            }

            computeFixedColumnWidth(columnWidths, column, tdth, colSpan, parentWidth);
            column += colSpan;
        }
    }

    protected void initializeBorder() {
        int defStyle = (borderWidth == 0) ? CssBorder.STYLE_NONE : CssBorder.STYLE_OUTSET;
        border = CssBorder.getBorder(getElement(), borderWidth, defStyle, frame);

        if (border != null) {
            leftBorderWidth = border.getLeftBorderWidth();
            topBorderWidth = border.getTopBorderWidth();
            bottomBorderWidth = border.getBottomBorderWidth();
            rightBorderWidth = border.getRightBorderWidth();
        }

        considerDesignBorder();
    }

    protected void createChildren(CreateContext context) {
        // Layout Caption - top or bottom? 
        Element caption = findCaption(getElement());

        captionAbove = true;

        if (caption != null) {
//            Value side = CssLookup.getValue(caption, XhtmlCss.CAPTION_SIDE_INDEX);
            CssValue cssSide = CssProvider.getEngineService().getComputedValueForElement(caption, XhtmlCss.CAPTION_SIDE_INDEX);

//            if (side == CssValueConstants.BOTTOM_VALUE) {
            if (CssProvider.getValueService().isBottomValue(cssSide)) {
                captionAbove = false;
            }

            finishLineBox(context);

            if (captionAbove) {
                addNode(context, caption, null, null, null);

                int n = getBoxCount();
                captionBoxes = new ArrayList<CssBox>(n);

                for (int i = 0; i < n; i++) {
                    captionBoxes.add(getBox(i));
                }
            }
        }

        // Calculate the number of columns in the table
        // See section 19.2.1 in the XHTML Tables Module
        // (http://www.w3.org/TR/xhtml2/mod-tables.html)
        // (The number of rows = number of <tr> children of the <table>
        // element)
        columns = computeColumnCount(table);
        rows = computeRowCount(table);

        // TODO - these may have percents, which I'll totally botch
        // here - gotta look for that!
        // XXX Shouldn't I initialize these in TableBox.initialize(),
        // not here?
        cellSpacing = HtmlAttribute.getIntegerAttributeValue(table, HtmlAttribute.CELLSPACING, 2);

        // If cell spacing has not been set, initialize it using
        // the table margin? Or take the max again between cellspacing
        // and table margins, the way we take the max between an individual
        // td's padding and the cellpadding attribute?
        cellPadding = HtmlAttribute.getIntegerAttributeValue(table, HtmlAttribute.CELLPADDING, 1);

        // Frame attribute support
        int defaultBorderWidth = 0;

        if (table.hasAttribute(HtmlAttribute.FRAME)) {
            String attr = table.getAttribute(HtmlAttribute.FRAME);

            if (attr.equals("above")) { // NOI18N
                frame = CssBorder.FRAME_TOP;
            } else if (attr.equals("below")) { // NOI18N
                frame = CssBorder.FRAME_BOTTOM;
            } else if (attr.equals("hsides")) { // NOI18N
                frame = CssBorder.FRAME_TOP | CssBorder.FRAME_BOTTOM;
            } else if (attr.equals("vsides")) { // NOI18N
                frame = CssBorder.FRAME_LEFT | CssBorder.FRAME_RIGHT;
            } else if (attr.equals("lhs")) { // NOI18N
                frame = CssBorder.FRAME_LEFT;
            } else if (attr.equals("rhs")) { // NOI18N
                frame = CssBorder.FRAME_RIGHT;
            } else if (attr.equals("void")) { // NOI18N
                frame = 0;
            } else if ((attr.length() == 0) || attr.equals("box") || // NOI18N
                    attr.equals("border")) { // NOI18N
                frame = CssBorder.FRAME_BOX;
            }

            defaultBorderWidth = (frame != 0) ? CssBorder.WIDTH_THIN : 0;
        }

        // Border: percentages are not allowed
        borderWidth =
            HtmlAttribute.getIntegerAttributeValue(table, HtmlAttribute.BORDER, defaultBorderWidth);

        if (borderWidth < 0) {
            borderWidth = 0;
        }

        if (frame == 0) {
            borderWidth = 0;
        }

//        MarkupDesignBean bean = getDesignBean();
//        MarkupDesignBean bean = CssBox.getMarkupDesignBeanForCssBox(this);
//        if (bean != null) {
//            DesignInfo info = bean.getDesignInfo();
//
//            if (info instanceof MarkupTableDesignInfo) {
//                tableDesignInfo = (MarkupTableDesignInfo)info;
//            }
//        }

        // TODO - deal with the table header, if one is specified.
        // TODO Group the columns according to any column group specifications.
        // Render the cells, row by row and grouped in appropriate columns
        // Render the table footer, if one is specified
        fixedLayout = false;

        // A value of "auto" for width implies autoLayout, so only
        // consult the table-layout css property if it's non-auto.
        if (contentWidth != AUTO) {
//            Value layout = CssLookup.getValue(table, XhtmlCss.TABLE_LAYOUT_INDEX);
            CssValue cssLayout = CssProvider.getEngineService().getComputedValueForElement(table, XhtmlCss.TABLE_LAYOUT_INDEX);

//            if (layout == CssValueConstants.FIXED_VALUE) {
            if (CssProvider.getValueService().isFixedValue(cssLayout)) {
                fixedLayout = true;
            }
        }

//        Value collapse = CssLookup.getValue(table, XhtmlCss.BORDER_COLLAPSE_INDEX);
        CssValue cssCollapse = CssProvider.getEngineService().getComputedValueForElement(table, XhtmlCss.BORDER_COLLAPSE_INDEX);

        if (table.hasAttribute(HtmlAttribute.RULES)) {
            String attr = table.getAttribute(HtmlAttribute.RULES);

            if (attr.equals("groups")) { // NOI18N

                // XXX not yet supported
                //collapse = CssValueConstants.COLLAPSE_VALUE;
                rules = CssBorder.FRAME_BOX;
//                collapse = CssValueConstants.COLLAPSE_VALUE;
            } else if (attr.equals("rows")) { // NOI18N
                rules = CssBorder.FRAME_TOP | CssBorder.FRAME_BOTTOM;
//                collapse = CssValueConstants.COLLAPSE_VALUE;
                cssCollapse = CssProvider.getValueService().getCollapseCssValueConstant();
            } else if (attr.equals("cols")) { // NOI18N
                rules = CssBorder.FRAME_LEFT | CssBorder.FRAME_RIGHT;
//                collapse = CssValueConstants.COLLAPSE_VALUE;
                cssCollapse = CssProvider.getValueService().getCollapseCssValueConstant();
            } else if (attr.equals("all")) { // NOI18N
                rules = CssBorder.FRAME_BOX;
//                collapse = CssValueConstants.COLLAPSE_VALUE;
                cssCollapse = CssProvider.getValueService().getCollapseCssValueConstant();
            } // else: none -- FRAME_UNSET
        }

//        if (collapse == CssValueConstants.COLLAPSE_VALUE) {
        if (CssProvider.getValueService().isCollapseValue(cssCollapse)) {
            // Hack: simulate border-collapsing by setting cell spacing
            // to 0. Once I implement "real" border collapse, I should
            // take this out.
            cellSpacing = 0;
        }

        cells = new CellBox[rows][columns];
        rowspans = new int[rows][columns];
        colspans = new int[rows][columns];
        rowElements = new Element[rows];
        createCells();

        // Create the actual cell contents
        // We do this in reverse row order (last to first) for the following
        // reason: a JSF component, such as an output text, can be replicated
        // on every row by a JSF Data Table. For incremental layout purposes,
        // I stash the box associated with the LiveBean right with the
        // LiveBean. This box will always be the last-rendered box for the
        // LiveBean. (There's a way to turn this off, used by the paint
        // preview methods in PageBox).  However, when we're trying to
        // look up the position for a replicated component that's in a table
        // we want to show the entry on the FIRST row, not the last!
        // Therefore, render in reverse order. Ditto for horizontal order,
        // in case a JSF component were to replicate components horizontally
        // too!
        // XXX Does this mess up in-order caret traversal of the table??
        finishLineBox(context); // ensure that cell has its own linebox

        for (int i = rows - 1; i >= 0; i--) {
            //for (int j = columns-1; j >= 0; j--) {
            for (int j = 0; j < columns; j++) {
                CellBox box = cells[i][j];

                if ((box == null) || (box == OCCUPIED)) {
                    continue;
                }

                box.createChildren(context);
                finishLineBox(context); // ensure that content outside doesn't spill into cell linebox

                // XXX TODO - I should be clearing all floats here too!
            }
        }

        if ((caption != null) && !captionAbove) {
            int i = getBoxCount();
            addNode(context, caption, null, null, null);
            finishLineBox(context);

            // Add boxes added for the caption into the caption box list
            int n = getBoxCount();
            captionBoxes = new ArrayList<CssBox>(n - i);

            for (; i < n; i++) {
                captionBoxes.add(getBox(i));
            }
        }
    }

    /** Add a caption box, if applicable */
    private static Element findCaption(Element table) {
        // The <caption> element is supposed to be the first child.
        // At least it was in html 4.0:
        // http://www.w3.org/TR/REC-html40/struct/tables.html#h-11.2.2
        // TODO - make sure this is still required in xhtml.
        NodeList list = table.getChildNodes();
        Element caption = null;

        for (int i = 0, n = list.getLength(); i < n; i++) {
            Node node = (Node)list.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)node;

//                if (CssLookup.getValue(e, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_CAPTION_VALUE) {
                if (CssProvider.getValueService().isTableCaptionValue(CssProvider.getEngineService().getComputedValueForElement(e, XhtmlCss.DISPLAY_INDEX))) {
                    caption = (Element)node;
                }

                // Caption must be first element
                break;
            }
        }

        return caption;
    }

    /**
     * Compute a cell 2d array, and initialize it with boxes for all
     * the locations that have nonempty content. The boxes will point
     * to their elements, but are not formatted yet (the format depends
     * on column widths which have not yet been computed).
     */
    private void createCells() {
        setProbableChildCount(rows * columns);

        int row = 0;

        NodeList list = table.getChildNodes();
        int len = list.getLength();
        int footerBegin = -1;
        int footerEnd = -1;

        for (int i = 0; i < len; i++) {
            Node trn = (Node)list.item(i);

            if (trn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tr = (Element)trn;
//            Value display = CssLookup.getValue(tr, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tr, XhtmlCss.DISPLAY_INDEX);

//            if (display == CssValueConstants.TABLE_ROW_VALUE) {
            if (CssProvider.getValueService().isTableRowValue(cssDisplay)) {
                createRowCells(row, tr);
                row++;
//            } else if ((display == CssValueConstants.TABLE_ROW_GROUP_VALUE) ||
//                    (display == CssValueConstants.TABLE_HEADER_GROUP_VALUE) ||
//                    (display == CssValueConstants.TABLE_FOOTER_GROUP_VALUE)) {
            } else if (CssProvider.getValueService().isTableRowGroupValue(cssDisplay)
            || CssProvider.getValueService().isTableHeaderGroupValue(cssDisplay)
            || CssProvider.getValueService().isTableFooterGroupValue(cssDisplay)) {
//                boolean isFooter = display == CssValueConstants.TABLE_FOOTER_GROUP_VALUE;
                boolean isFooter = CssProvider.getValueService().isTableFooterGroupValue(cssDisplay);

                if (isFooter) {
                    assert footerBegin == -1; // Only one tfoot is allowed!

                    // Should we emit a warning for the user here?
                    footerBegin = row;
                }

                NodeList list2 = tr.getChildNodes();
                int len2 = list2.getLength();

                for (int j = 0; j < len2; j++) {
                    Node trn2 = (Node)list2.item(j);

                    if (trn2.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element tr2 = (Element)trn2;

//                    if (CssLookup.getValue(tr2, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_ROW_VALUE) {
                    if (CssProvider.getValueService().isTableRowValue(CssProvider.getEngineService().getComputedValueForElement(tr2, XhtmlCss.DISPLAY_INDEX))) {
                        createRowCells(row, tr2);
                        row++;
                    }
                }

                if (isFooter) {
                    footerEnd = row;
                }
            }
        }

        // Move footer rows to the end
        // This is hacky. I should support rowgroups instead; then it
        // would be a simple rowgroup swap!
        if ((footerBegin != -1) && (footerBegin != footerEnd)) {
            int footerLength = footerEnd - footerBegin;
            int targetRow = rows - footerLength;

            for (int i = 0; i < footerLength; i++) {
                swapRow(footerBegin + i, targetRow + i);
            }
        }
    }

    private void swapRow(int a, int b) {
        for (int i = 0; i < columns; i++) {
            CellBox tempBox = cells[b][i];
            cells[b][i] = cells[a][i];
            cells[a][i] = tempBox;

            int temp = rowspans[b][i];
            rowspans[b][i] = rowspans[a][i];
            rowspans[a][i] = temp;

            temp = colspans[b][i];
            colspans[b][i] = colspans[a][i];
            colspans[a][i] = temp;
        }

        Element tempE = rowElements[a];
        rowElements[a] = rowElements[b];
        rowElements[b] = tempE;
    }

    private void createRowCells(int row, Element tr) {
        rowElements[row] = tr;

        NodeList list2 = tr.getChildNodes();
        int len2 = list2.getLength();
        int col = 0;

        for (int j = 0; j < len2; j++) {
            Node tdthn = (Node)list2.item(j);

            if (tdthn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tdth = (Element)tdthn;
//            Value display = CssLookup.getValue(tdth, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tdth, XhtmlCss.DISPLAY_INDEX);

//            if (display != CssValueConstants.TABLE_CELL_VALUE) {
            if (!CssProvider.getValueService().isTableCellValue(cssDisplay)) {
                continue;
            }

            while ((col < columns) && (cells[row][col] == OCCUPIED)) {
                col++;
            }

            col += createCell(tdth, row, col);
        }
    }

    /** Create the given cell, and return its colspan */
    private int createCell(Element tdth, int row, int col) {
        if (col == columns) {
            // UGH! The table is "invalid" in that the computed column
            // count is less than the actual columns found. This can
            // only happen when we've counted columns using <col> and
            // <colgroup> elements, which the spec says that should
            // uniquely identify the number of columns. But users may
            // have thrown in additional columns in subsequent rows anyway,
            // so try to deal with that.
            // For now, we can't really - we'd have to resize the
            // cells, rowspan and colspan parameters - but they are
            // parameters, not fields. Another possibility is to
            // bump up the column count and try again.
            // For now, just bail - which will truncate overflow
            // cells.  Hopefully this will clue the user in to the
            // fact that the user has an error in the table
            // definition.
            return 0;
        }

        int colspan = HtmlAttribute.getIntegerAttributeValue(tdth, HtmlAttribute.COLSPAN, 1);
        int rowspan = HtmlAttribute.getIntegerAttributeValue(tdth, HtmlAttribute.ROWSPAN, 1);

        // Look for errors in the table definition
        if (colspan <= 0) { // HTML 11.2.6: "0" means fill the row. TODO.
            colspan = 1;
        }

        if (rowspan <= 0) { // HTML 11.2.6: "0" means fill the column. TODO.
            rowspan = 1;
        }

        if ((col + colspan) > columns) {
            colspan = columns - col;
        }

        if ((row + rowspan) > rows) {
            rowspan = rows - row;
        }

        colspans[row][col] = colspan;
        rowspans[row][col] = rowspan;

        CellBox box = new CellBox(webform, tdth, BoxType.STATIC, false, this);
        box.row = row;
        box.col = col;

        for (int m = 0; m < rowspan; m++) {
            for (int n = 0; n < colspan; n++) {
                cells[row + m][col + n] = OCCUPIED;
            }
        }

        cells[row][col] = box;

        box.initialize();

        addBox(box, null, null);

        // We create the children of the box itself when we're done with
        // this
        return colspan;
    }

    /** Format all the cells in the table */
    private void formatCells(int[] columnWidths, FormatContext context) {
        // The table cells should not be affected by floats in effect
        List<FloatingBoxInfo> oldFloats = context.floats;
        context.floats = null;

        boolean oldFloating = context.floating;
        context.floating = false;

        // Format all the cells in the table
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                CellBox box = cells[i][j];

                if ((box != null) && (box != OCCUPIED)) {
                    formatCell(i, j, columnWidths, context);
                }
            }
        }

        context.floats = oldFloats;
        context.floating = oldFloating;
    }

    private void formatCell(int row, int col, int[] columnWidths, FormatContext context) {
        CellBox box = cells[row][col];
        int colspan = colspans[row][col];

        //int rowspan = rowspans[row][col];
        int w = 0;

        for (int m = 0; m < colspan; m++) {
            w += columnWidths[col + m];
        }

        int ac = w;
        int ah = containingBlockHeight / columns;
        box.setContainingBlock(0, 0, ac, ah);
        box.contentWidth =
            ac - box.leftBorderWidth - box.leftPadding - box.rightPadding - box.rightBorderWidth;
        box.contentHeight =
            ah - box.topBorderWidth - box.topPadding - box.bottomPadding - box.bottomBorderWidth;

        box.relayout(context);

        // XXX why am I doing this?
        box.contentWidth = ac;
        box.contentHeight = AUTO;

        box.inline = false;
        box.replaced = false;

        box.boxType = BoxType.STATIC; // prevent "bad" docs from screwing things up

        // by setting position: absolute on <td>'s for example
        box.computeVerticalLengths(context);

        box.height =
            box.topBorderWidth + box.topPadding + box.contentHeight + box.bottomPadding +
            box.bottomBorderWidth + cellSpacing;

        // All floats must be cleared; nothing can extend outside the cell
//        box.clearBottom(context, CssValueConstants.BOTH_VALUE);
        box.clearBottom(context, CssProvider.getValueService().getBothCssValueConstant());
        box.originalHeight = box.height;

        // According to the table spec (CSS2.1 section 17.5.3) the content height used
        // for table cells should be the max of the specified height and the computed
        // minimum required height
//        int boxHeight = CssLookup.getLength(box.getElement(), XhtmlCss.HEIGHT_INDEX);
        int boxHeight = CssUtilities.getCssLength(box.getElement(), XhtmlCss.HEIGHT_INDEX);

        if ((boxHeight != AUTO) && (boxHeight > box.contentHeight)) {
            box.contentHeight = boxHeight;
            box.height =
                box.topBorderWidth + box.topPadding + box.contentHeight + box.bottomPadding +
                box.bottomBorderWidth + cellSpacing;
        }
        
        //only now, when we calculated the height, we can move the relatives
        box.finishAllRelatives(context);
    }

    private static int countRow(Element row) {
        int rowcols = 0; // columns for this row

        NodeList list2 = row.getChildNodes();
        int len2 = list2.getLength();

        for (int j = 0; j < len2; j++) {
            Node tdthn = (Node)list2.item(j);

            if (tdthn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tdth = (Element)tdthn;
//            Value display = CssLookup.getValue(tdth, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tdth, XhtmlCss.DISPLAY_INDEX);

//            if (display != CssValueConstants.TABLE_CELL_VALUE) {
            if (!CssProvider.getValueService().isTableCellValue(cssDisplay)) {
                continue;
            }

            int colspan = HtmlAttribute.getIntegerAttributeValue(tdth, HtmlAttribute.COLSPAN, 1);

            if (colspan <= 0) {
                // HTML 11.2.6: "The value zero ("0") means that the cell spans all 
                // columns from the current column to the last column of the column 
                // group (COLGROUP) in which the cell is defined."
                // I don't have a good way to simulate this. But for column count purposes
                // we'll consider it as 1.
                // I'm picking up negative values for this too to avoid badly
                // constructed pages from breaking rendering.
                colspan = 1;
            }

            rowcols += colspan;
        }

        return rowcols;
    }

    /**
     * Compute the number of columns.
     * See section 19.2.1 in the XHTML Tables Module
     *   http://www.w3.org/TR/xhtml2/mod-tables.html
     */
    private static int computeColumnCount(Element table) {
        // If the table contains any colgroup or col elements, use those
        // to compute the number of columns
        // XXX Can these puppies (colgroup, col) appear anywhere on only
        // as direct children of table or colgroup?
        int columns = 0;

        NodeList list = table.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            Node child = (Node)list.item(i);

            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element)child;
//            Value display = CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX);

//            if (display == CssValueConstants.TABLE_COLUMN_GROUP_VALUE) {
            if (CssProvider.getValueService().isTableColumnGroupValue(cssDisplay)) {
                // Sum up the spans of any <col> children of this
                // <colgroup>, and track whether we had any
                boolean empty = true;
                NodeList list2 = element.getChildNodes();
                int len2 = list2.getLength();

                for (int j = 0; j < len2; j++) {
                    Node child2 = (Node)list2.item(j);

                    if (child2.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element element2 = (Element)child2;

//                    if (CssLookup.getValue(element2, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_COLUMN_VALUE) {
                    if (CssProvider.getValueService().isTableColumnValue(CssProvider.getEngineService().getComputedValueForElement(element2, XhtmlCss.DISPLAY_INDEX))) {
                        empty = false;
                        columns += HtmlAttribute.getIntegerAttributeValue(element2,
                            HtmlAttribute.SPAN, 1);
                    }
                }

                if (empty) {
                    // For each empty colgroup element, take the value of 
                    // its span attribute (default 1)
                    columns += HtmlAttribute.getIntegerAttributeValue(element, HtmlAttribute.SPAN, 1);
                } // else: for colgroups that have children col elements

                // we ignore the colgroup span attribute
//            } else if (display == CssValueConstants.TABLE_COLUMN_VALUE) {
            } else if (CssProvider.getValueService().isTableColumnValue(cssDisplay)) {
                columns += HtmlAttribute.getIntegerAttributeValue(element, HtmlAttribute.SPAN, 1);
            }
        }

        if (columns > 0) {
            // We had <colgroup> or <col> elements - which means we're
            // done. According to 19.2.1, it's an error if a table contains
            // colgroup or col elements and the rest of the table implies
            // a different number of columns than what is computed above
            return columns;
        }

        // No <colgroup> or <col> elements: have to count columns the
        // "hard" way by looking at all table cells, computing the number
        // of columns in each row and finding the maximum.
        for (int i = 0; i < len; i++) {
            Node trn = (Node)list.item(i);

            if (trn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tr = (Element)trn;
//            Value display = CssLookup.getValue(tr, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tr, XhtmlCss.DISPLAY_INDEX);

//            if (display == CssValueConstants.TABLE_ROW_VALUE) {
            if (CssProvider.getValueService().isTableRowValue(cssDisplay)) {
                int rowcols = countRow(tr); // columns for this row

                if (rowcols > columns) {
                    columns = rowcols;
                }
//            } else if ((display == CssValueConstants.TABLE_ROW_GROUP_VALUE) ||
//                    (display == CssValueConstants.TABLE_HEADER_GROUP_VALUE) ||
//                    (display == CssValueConstants.TABLE_FOOTER_GROUP_VALUE)) {
            } else if (CssProvider.getValueService().isTableRowGroupValue(cssDisplay)
            || CssProvider.getValueService().isTableHeaderGroupValue(cssDisplay)
            || CssProvider.getValueService().isTableFooterGroupValue(cssDisplay)) {
                NodeList list2 = tr.getChildNodes();
                int len2 = list2.getLength();

                for (int j = 0; j < len2; j++) {
                    Node trn2 = (Node)list2.item(j);

                    if (trn2.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element tr2 = (Element)trn2;

//                    if (CssLookup.getValue(tr2, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_ROW_VALUE) {
                    if (CssProvider.getValueService().isTableRowValue(CssProvider.getEngineService().getComputedValueForElement(tr2, XhtmlCss.DISPLAY_INDEX))) {
                        int rowcols = countRow(tr2);

                        if (rowcols > columns) {
                            columns = rowcols;
                        }
                    }
                }
            }
        }

        return columns;
    }

    /**
     * Compute the number of rows; this is just the number
     * of <tr>'s in the table; these may be nested within
     * <thead>, <tbody> and <tfoot> tags.
     */
    private static int computeRowCount(Element table) {
        // Count number of <tr> - either directly below <table>, or
        // within <thead>, <tbody> or <tfoot>
        int rows = 0;
        NodeList list = table.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            Node trn = (Node)list.item(i);

            if (trn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element tr = (Element)trn;
//            Value display = CssLookup.getValue(tr, XhtmlCss.DISPLAY_INDEX);
            CssValue cssDisplay = CssProvider.getEngineService().getComputedValueForElement(tr, XhtmlCss.DISPLAY_INDEX);

//            if (display == CssValueConstants.TABLE_ROW_VALUE) {
            if (CssProvider.getValueService().isTableRowValue(cssDisplay)) {
                rows++;
//            } else if ((display == CssValueConstants.TABLE_ROW_GROUP_VALUE) ||
//                    (display == CssValueConstants.TABLE_HEADER_GROUP_VALUE) ||
//                    (display == CssValueConstants.TABLE_FOOTER_GROUP_VALUE)) {
            } else if (CssProvider.getValueService().isTableRowGroupValue(cssDisplay)
            || CssProvider.getValueService().isTableHeaderGroupValue(cssDisplay)
            || CssProvider.getValueService().isTableFooterGroupValue(cssDisplay)) {
                NodeList list2 = tr.getChildNodes();
                int len2 = list2.getLength();

                for (int j = 0; j < len2; j++) {
                    Node trn2 = (Node)list2.item(j);

                    if (trn2.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element tr2 = (Element)trn2;

//                    if (CssLookup.getValue(tr2, XhtmlCss.DISPLAY_INDEX) == CssValueConstants.TABLE_ROW_VALUE) {
                    if (CssProvider.getValueService().isTableRowValue(CssProvider.getEngineService().getComputedValueForElement(tr2, XhtmlCss.DISPLAY_INDEX))) {
                        rows++;
                    }
                }
            }
        }

        return rows;
    }

//    public String toString() {
//        return "TableBox[" + paramString() + "]";
//    }

    protected String paramString() {
        return /* super.paramString() + ", " + */ "rows=" + rows + ", " + "columns=" + columns // NOI18N
        + ", element=" + table + ", x=" + x + ", y=" + y // NOI18N
        + ", size=" + width + ":" + height // NOI18N
        + ", contentWidth=" + contentWidth // NOI18N
        + ", containingBlockWidth=" + containingBlockWidth; // NOI18N

    }

    public void relayout(FormatContext context) {
        // Ensure that createChildren has run and has initialized
        // the row and column fields
        assert (rows != -1) && (columns != -1);

        if ((columns == 0) || (rows == 0)) {
            rows = columns = 0;
            positionCells(new int[0], context);

//            int width = CssLookup.getLength(table, XhtmlCss.WIDTH_INDEX);
//            int height = CssLookup.getLength(table, XhtmlCss.HEIGHT_INDEX);
            int width = CssUtilities.getCssLength(table, XhtmlCss.WIDTH_INDEX);
            int height = CssUtilities.getCssLength(table, XhtmlCss.HEIGHT_INDEX);

            if (width != AUTO) {
                contentWidth = super.width = width;
            }

            if (height != AUTO) {
                contentHeight = super.height = height;
            }

            return;
        }

        int[] columnWidths = new int[columns];

        effectiveTopMargin = topMargin;
        effectiveBottomMargin = bottomMargin;

        if (fixedLayout) {
            fixedLayout(columnWidths, context);
        } else {
            autoLayout(columnWidths, context);
        }

    }

    /**
     * Implement the automatic table layout algorithm,
     * described in the CSS2.1 spec:
     * http://www.w3.org/TR/CSS21/tables.html#auto-table-layout
     */
    private void autoLayout(int[] columnWidths, FormatContext context) {
        assert (rows > 0) && (columns > 0);

        computeAutoColumnWidths(columnWidths, context, false);
        formatCells(columnWidths, context);
        positionCells(columnWidths, context);
    }

    /** Compute the width of the table. May return AUTO.
     * @param erase If set, "erase" the value from the CSS cached data
     *    when done with it. Typically set when we're just scanning the
     *    preferred width (when the containing block may be 0) so we
     *    want to recompute when we know the real containing block width.
     */
    private int getTableWidth(boolean erase) {
        //  From
        // http://www.nic.fi/~tapio1/Teaching/Taulukot3.php3:
        //
        //    In principle the width property means also in tables the
        //    content width. Because the element TABLE doesn't have
        //    direct actual content (between the actual content is at
        //    least one TR element), in ordinary cases only borders
        //    increase the total width of the block box of the table.
        //
        //    The problems is however the fact that in the HTML 4.01
        //    specification calculating the width property of the TABLE
        //    element is used another formula as calculating the width
        //    property in CSS. In the HTML 4.01 specification has been
        //    said about the attribute width following:
        //         This attribute specifies the desired width of the
        //         entire table...
        //    According that definition borders are counted to the total
        //    width of the table and it is not the content width like in
        //    CSS.
        //int tableWidth = Css.getLength(table, XhtmlCss.WIDTH_INDEX);
//        Value val = CssLookup.getValue(table, XhtmlCss.WIDTH_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(table, XhtmlCss.WIDTH_INDEX);

//        if (val == CssValueConstants.AUTO_VALUE) {
        if (CssProvider.getValueService().isAutoValue(cssValue)) {
            return AUTO;
        } else {
//            int tableWidth = (int)val.getFloatValue();
            int tableWidth = (int)cssValue.getFloatValue();

            // Empirically I've noticed mozilla treats tables differently; 
            // than regular boxes: the width: property affects the final
            // width of the table (including borders and padding).
            tableWidth -= (leftBorderWidth + leftPadding + rightPadding + rightBorderWidth);

            // Each of the cells will absorb the space for the top and left
            // padding area. However, we also need to have a padding area
            // on the right side of the rightmost column, so account for
            // that space here.
            tableWidth -= cellSpacing;

            if (erase) {
//                boolean wasPercentage =
//                    val instanceof ComputedValue &&
//                    (((ComputedValue)val).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE);
                boolean wasPercentage = cssValue instanceof CssComputedValue
                                        && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue());

                if (wasPercentage) {
//                    CssLookup.uncompute(table, XhtmlCss.WIDTH_INDEX);
                    CssProvider.getEngineService().uncomputeValueForElement(table, XhtmlCss.WIDTH_INDEX);
                }
            }

            return tableWidth;
        }
    }

    private void computeAutoColumnWidths(int[] columnWidths, FormatContext context, boolean scan) {
        int tableWidth = getTableWidth(scan);

        // The minimum width that is required to layout the content, 
        // all linebreak possibilities will be used
        int[][] minCellWidths = new int[rows][columns];

        // The width the content could fill without any linebreaks.
        int[][] maxCellWidths = new int[rows][columns];

        int fixedWidthColumns = 0;
        boolean[] fixedWidths = new boolean[columns];

        /** Special meaning: positive numbers: fixed width. Negative
         * numbers: negative percentage. AUTO: unconstrained
         * (default) */
        int[] constraints = new int[columns];

        for (int i = 0; i < columns; i++) {
            columnWidths[i] = AUTO;
            constraints[i] = AUTO;
        }

        // From RFC 1942:
        // In the first pass, line wrapping is disabled, and the user
        // agent keeps track of the minimum and maximum width of each
        // cell. The maximum width is given by the widest line. As line
        // wrap has been disabled, paragraphs are treated as long lines
        // unless broken by <BR> elements. The minimum width is given by
        // the widest word or image etc.  taking into account leading
        // indents and list bullets etc. In other words, if you were to
        // format the cell's content in a window of its own, determine
        // the minimum width you could make the window before the cell
        // begins to overflow. Allowing user agents to split words will
        // minimize the need for horizontal scrolling or in the worst
        // case clipping of cell contents.
        // The comments in this section (the numbered steps) is literally
        // the text from the CSS2 spec which lists the automatic table
        // algorithm
        // 1. Calculate the minimum content width (MCW) of each cell:
        // the formatted content may span any number of lines but may
        // not overflow the cell box. If the specified 'width' (W) of
        // the cell is greater than MCW, W is the minimum cell width. A
        // value of 'auto' means that MCW is the minimum cell width.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                CellBox box = cells[i][j];

                // XXX do I need to do layout on the cells first?
                if ((box == null) || (box == OCCUPIED)) {
                    continue;
                }

                // Ensure that margins etc. have been initialized
                if (context != null) {
                    box.initializeHorizontalWidths(context);
                }

                int mcw = box.getPrefMinWidth();

                Element element = box.getElement();
//                Value value = CssLookup.getValue(element, XhtmlCss.WIDTH_INDEX);
                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.WIDTH_INDEX);
                boolean wasPercentage = false;

//                if (value != CssValueConstants.AUTO_VALUE) {
                if (!CssProvider.getValueService().isAutoValue(cssValue)) {
                    if (colspans[i][j] == 1) { // Only count fixed columns when I -know- it applies to this one
                        fixedWidthColumns++;

                        if (!fixedWidths[j]) {
                            fixedWidths[j] = true;
                        }
                    }

//                    wasPercentage =
//                        value instanceof ComputedValue &&
//                        (((ComputedValue)value).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE);
                    wasPercentage = cssValue instanceof CssComputedValue
                                    && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue());

                    if (wasPercentage) {
//                        int percentage =
//                            (int)((ComputedValue)value).getCascadedValue().getFloatValue();
                        int percentage = (int)((CssComputedValue)cssValue).getCascadedValue().getFloatValue();

                        if (percentage < 0) {
                            percentage = 0;
                        }

                        // Empirically (firefox), percentages win over fixed
                        if ((constraints[j] == AUTO) || (constraints[j] >= 0) ||
                                (percentage > -constraints[j])) {
                            constraints[j] = -percentage;
                        }
                    } else {
                        // Only replace auto or other (smaller) fixed widths
                        // positive numbers indicates actual length
//                        int length = (int)value.getFloatValue();
//                        int length = (int)cssValue.getFloatValue();
                        // XXX #126240 Possible NPE
                        int length = cssValue == null ? null : (int)cssValue.getFloatValue();

                        if (length < 0) {
                            length = 0;
                        }

                        if ((constraints[j] == AUTO) ||
                                ((constraints[j] >= 0) && (constraints[j] < length))) {
                            constraints[j] = length;
                        }
                    }
                }

                // XXX Are the border widths and paddings okay to use at this
                // point - have they been initialized? I suppose they have, but
                // what are they relative to, if not the table containing block -
                // which hasn't been computed yet? They must be relative to the
                // table width! I may have to go and pre-look that up in my
                // box initializer, and make sure that box.initialize() on the
                // TD boxes use the correct "containing block" !
                // XXX NO - we've already added in padding etc. for getminwidth - so don't do it here.
                // Instead adjust w computation to do it too so we have an apples to oranges comparison
                // when picking max!
                minCellWidths[i][j] =
                    mcw + box.leftBorderWidth + box.leftPadding + box.rightPadding +
                    box.rightBorderWidth + cellSpacing;

                // Also, calculate the "maximum" cell width of each
                // cell: formatting the content without breaking lines
                // other than where explicit line breaks occur.
                int max = box.getPrefWidth();

                if (max < mcw) { // in case w was greater than mcw and max
                    max = mcw;
                }

                maxCellWidths[i][j] =
                    max + box.leftBorderWidth + box.leftPadding + box.rightPadding +
                    box.rightBorderWidth + cellSpacing;
            }
        }

        // 2. For each column, determine a maximum and minimum column
        // width from the cells that span only that column. The minimum
        // is that required by the cell with the largest minimum cell
        // width (or the column 'width', whichever is larger). The
        // maximum is that required by the cell with the largest maximum
        // cell width (or the column 'width', whichever is larger).
        int[] columnMaxes = new int[columns];
        int[] columnMins = new int[columns];

        //int[] percentageMax = new int[columns];
        //int[] percentageMin = new int[columns];
        for (int j = 0; j < columns; j++) {
            int max = 0; //Integer.MIN_VALUE;
            int min = 0; //Integer.MIN_VALUE;

            for (int i = 0; i < rows; i++) {
                if ((cells[i][j] == null) || (cells[i][j] == OCCUPIED)) {
                    continue;
                }

                if (colspans[i][j] == 1) {
                    if (maxCellWidths[i][j] > max) {
                        max = maxCellWidths[i][j];
                    }

                    if (minCellWidths[i][j] > min) {
                        min = minCellWidths[i][j];
                    }
                }
            }

            columnMaxes[j] = max;
            columnMins[j] = min;
        }

        // 3. For each cell that spans more than one column, increase
        // the minimum widths of the columns it spans so that together,
        // they are at least as wide as the cell. Do the same for the
        // maximum widths. If possible, widen all spanned columns by
        // approximately the same amount.
        // XXX need clarification: "at least as wide as the cell" - is this
        // referring to the minimum width (MCW) of the cell?
        for (int j = 0; j < columns; j++) {
            for (int i = 0; i < rows; i++) {
                if ((cells[i][j] == null) || (cells[i][j] == OCCUPIED)) {
                    continue;
                }

                if (colspans[i][j] > 1) {
                    int minCellWidth = minCellWidths[i][j];
                    int maxCellWidth = maxCellWidths[i][j];
                    int n = colspans[i][j];

                    int minSum = 0;
                    int maxSum = 0;

                    for (int k = 0; k < n; k++) {
                        minSum += columnMins[k];
                        maxSum += columnMaxes[k];
                    }

                    if (minSum < minCellWidth) {
                        int addition = (minCellWidth - minSum) / n;

                        for (int k = 0; k < n; k++) {
                            columnMins[k] += addition;
                        }
                    }

                    if (maxSum < maxCellWidth) {
                        int addition = (maxCellWidth - maxSum) / n;

                        for (int k = 0; k < n; k++) {
                            columnMaxes[k] += addition;
                        }
                    }
                }
            }
        }

        // This gives a maximum and minimum width for each
        // column. Column widths influence the final table width as
        // follows:
        // 1. If the 'table' or 'inline-table' element's 'width'
        // property has a computed value (W) other than 'auto', the
        // property's value as used for layout is the greater of W and
        // the minimum width required by all the columns plus cell
        // spacing or borders (MIN). If W is greater than MIN, the extra
        // width should be distributed over the columns.  
        if (tableWidth != AUTO) {
            int min = 0;

            for (int k = 0; k < columns; k++) {
                min += columnMins[k];
            }

            if (min >= tableWidth) {
                tableWidth = min;

                // Assign column widths that are the minimums
                for (int k = 0; k < columns; k++) {
                    columnWidths[k] = columnMins[k];
                }

                // TODO: should I allow tables to have arbitrary sizes?
                // If so, I should instead go and proportionally subtract
                // space here to make the table fit!!  However, the automatic
                // table layout algorithm doesn't call for that - and indeed
                // mozilla doesn't appear to behave that way either.
            } else if (min < tableWidth) {
                // if max < tableWidth too, I should go with the max widths,
                // and then tack on extra space!
                int max = 0;

                for (int k = 0; k < columns; k++) {
                    if (constraints[k] == AUTO) {
                        max += columnMaxes[k];
                    } else {
                        int portion = 0;

                        if (constraints[k] < 0) {
                            // Percentage
                            int percentage = -constraints[k];
                            // #108602 When table is auto, count the pref width (here as max).
                            if (tableWidth == AUTO) {
                                portion = (percentage * columnMaxes[k]) / 100;
                            } else {
                                portion = (percentage * tableWidth) / 100;
                            }
                        } else {
                            portion = constraints[k];
                        }

                        if (portion > columnMins[k]) {
                            max += portion;
                        } else {
                            max += columnMins[k];
                        }
                    }
                }

                if (max < tableWidth) {
                    for (int k = 0; k < columns; k++) {
                        if (constraints[k] == AUTO) {
                            columnWidths[k] = columnMaxes[k];
                        } else {
                            int portion = 0;

                            if (constraints[k] < 0) {
                                // Percentage
                                int percentage = -constraints[k];
                                // #108602 When table is auto, count the pref width (here as max).
                                if (tableWidth == AUTO) {
                                    portion = (percentage * columnMaxes[k]) / 100;
                                } else {
                                    portion = (percentage * tableWidth) / 100;
                                }
                            } else {
                                portion = constraints[k];
                            }

                            if (portion > columnMins[k]) {
                                columnWidths[k] = portion;
                            } else {
                                columnWidths[k] = columnMins[k];
                            }
                        }
                    }

                    int leftOver = tableWidth - max;
                    int assigned = 0;

                    // Check for empty table, <table><tr><td/></tr></table>
                    if (max == 0) {
                        // Spread remainder out evenly
                        distribute(columnWidths, leftOver, columns);
                    } else {
                        if ((fixedWidthColumns == columns) || (fixedWidthColumns == 0)) {
                            // All (or none) of the columns have fixed width.
                            // Spread remainder out proportionally according
                            // to how much each column has already received.
                            // Thus with a 30% and a 70% column, the 70% column
                            // would receive 70% of the remainder as well.
                            for (int i = 0; i < columns; i++) {
                                int portion = (columnWidths[i] * leftOver) / max;
                                columnWidths[i] += portion;
                                assigned += portion;
                            }

                            // remainder from rounding errors
                            columnWidths[0] += (leftOver - assigned);
                        } else {
                            // Only some of the columns have fixed width; spread
                            // the available space among the non-fixed width columns.
                            // Arguably, percentages should be treated differently
                            // than fixed pixel widths but for now we treat them
                            // as the same (with the computed percentage value of the
                            // containing block)
                            int sum = 0;

                            for (int i = 0; i < columns; i++) {
                                if (!fixedWidths[i]) {
                                    sum += columnWidths[i];
                                }
                            }

                            if (sum == 0) {
                                // Unexpected, but this can happen when we have large colspans
                                // etc. which means we have additional columns that have no
                                // content, so the additional "empty" columns are considered
                                // nonfixed. But in this case we really have the scenario
                                // where available space must be distributed among the other
                                // columns.
                                for (int i = 0; i < columns; i++) {
                                    int portion = (columnWidths[i] * leftOver) / max;
                                    columnWidths[i] += portion;
                                    assigned += portion;
                                }
                            } else {
                                for (int i = 0; i < columns; i++) {
                                    if (!fixedWidths[i]) {
                                        int portion = (columnWidths[i] * leftOver) / sum;
                                        columnWidths[i] += portion;
                                        assigned += portion;
                                    }
                                }
                            }

                            // remainder from rounding errors
                            columnWidths[0] += (leftOver - assigned);
                        }
                    }
                } else {
                    // The actual table width is somewhere between
                    // min and max. So assign the space above min 
                    // out proportionally - except if a column reaches
                    // its max, distributed the new extra space over
                    // the other columns, until things stabilize.
                    assignColumnWidths(tableWidth, min, columnWidths, columnMins, columnMaxes,
                        constraints);
                }
            }
        } else {
            // 2. If the 'table' or 'inline-table' element has 'width:
            // auto', the table width used for layout is the greater of the
            // table's containing block width and MIN. However, if the
            // maximum width required by the columns plus cell spacing or
            // borders (MAX) is less than that of the containing block, use
            // MAX.
            int containing =
                containingBlockWidth -
                (leftBorderWidth + leftPadding + rightPadding + rightBorderWidth + cellSpacing);

            if (computingPrefWidth) {
                // We want to pick the maxes when we're computing the
                // preferred width. I can't set the containingBlockWidth to
                // something large from within getPrefWidth because that
                // would be propagated into the cell children which may
                // do more dramatic things with the containingBlockWidth,
                // like have attachments to the right side
                containing = Integer.MAX_VALUE;
            }

            int max = 0;

            for (int k = 0; k < columns; k++) {
                if (constraints[k] == AUTO) {
                    max += columnMaxes[k];
                } else {
                    int portion = 0;

                    if (constraints[k] < 0) {
                        // Percentage
                        int percentage = -constraints[k];
                        // #108602 When table is auto, count the pref width (here as max).
                        if (tableWidth == AUTO) {
                            portion = (percentage * columnMaxes[k]) / 100;
                        } else {
                            portion = (percentage * tableWidth) / 100;
                        }
                    } else {
                        portion = constraints[k];
                    }

                    if (portion > columnMins[k]) {
                        max += portion;
                    } else {
                        max += columnMins[k];
                    }
                }
            }

            if (max <= containing) {
                tableWidth = max;

                // We can fit the maximums for all columns!
                for (int k = 0; k < columns; k++) {
                    if (constraints[k] == AUTO) {
                        columnWidths[k] = columnMaxes[k];
                    } else {
                        int portion = 0;

                        if (constraints[k] < 0) {
                            // Percentage
                            int percentage = -constraints[k];
                            // #108602 When table is auto, count the pref width (here as max).
                            if (tableWidth == AUTO) {
                                portion = (percentage * columnMaxes[k]) / 100;
                            } else {
                                portion = (percentage * tableWidth) / 100;
                            }
                        } else {
                            portion = constraints[k];
                        }

                        if (portion > columnMins[k]) {
                            columnWidths[k] = portion;
                        } else {
                            columnWidths[k] = columnMins[k];
                        }
                    }
                }
            } else {
                int min = 0;

                for (int k = 0; k < columns; k++) {
                    min += columnMins[k];
                }

                if (min >= containing) {
                    tableWidth = min;

                    // Use minimum cells
                    for (int k = 0; k < columns; k++) {
                        columnWidths[k] = columnMins[k];
                    }
                } else {
                    // XXX: should we add cell spacing etc. here?
                    tableWidth = containing;
                    assignColumnWidths(tableWidth, min, columnWidths, columnMins, columnMaxes,
                        constraints);
                }
            }
        }

        // XXX TODO - how do we deal with this:
        // A percentage value for a column width is relative to the
        // table width. If the table has 'width: auto', a percentage
        // represents a constraint on the column's width, which a UA
        // should try to satisfy. (Obviously, this is not always
        // possible: if the column's width is '110%', the constraint
        // cannot be satisfied.)
    }

    private void distribute(int[] lengths, int leftOver, int count) {
        // Spread remainder out evenly
        int portion = leftOver / count;
        int remainder = leftOver % count;

        for (int i = 0; i < count; i++) {
            lengths[i] = portion;
        }

        lengths[0] += remainder;
    }

    /**
     * Distribute the space over the columns in the case where
     * the table width is larger than the minimum required
     * by the minimum cells, but less than the width required
     * by the maximum/preferred cell widths.
     * @todo Consider fixed widths and don't add widths to these!!
     */
    private void assignColumnWidths(int tableWidth, int min, int[] columnWidths, int[] columnMins,
        int[] columnMaxes, int[] constraints) {
        /*
         * Turns out Mozilla does NOT do proportional assignment - and in fact the
         * Braveheart stylesheets rely on this. For example, they want the sorting
         * buttons pushed over on the right, so they simply set the cell width to 100%
         * with the net result that the second cell, the button gets assigned its
         * minimum width because it appears later.
         *
         * So I'll do that too...
         *
         */

        // Now assign the available width
        // Distribute the available space.
        // First assign all the minimum widths to the columns.
        // Then dole out the percentage columns space.
        // Finally hand out the remaining space proportionally
        // may be no such columns.  If so we will ignore the
        // requested height on the table.
        // Initially, assign minimum widths
        for (int k = 0; k < columns; k++) {
            columnWidths[k] = columnMins[k];
        }

        int leftOver = tableWidth - min;
        int assigned = 0;

        // Assign length-constrained columns
        for (int i = 0; i < columns; i++) {
            int constraint = constraints[i];

            if (constraint == AUTO) {
                continue;
            }

            if (constraint >= 0) {
                int portion = constraint;
                int original = columnWidths[i];

                if (portion < original) {
                    // Don't assign less than the computed minimum width for
                    // this table
                    continue;
                }

                // leftOver contains the amount of space we have left over
                // after assigning widths to all columns. That includes the
                // width already assigned to this column being considered.
                // We add that back in now since we'll be replacing the
                // assigned width, not add to it.
                int left = leftOver + columnWidths[i];

                if (portion > left) {
                    portion = left;
                }

                columnWidths[i] = portion;
                assigned += (portion - original);
                leftOver = left - portion;

                if (leftOver <= 0) {
                    break;
                }
            }
        }

        // Assign percentage-constrained columns
        for (int i = 0; i < columns; i++) {
            if (constraints[i] == AUTO) {
                continue;
            }

            if (constraints[i] < 0) {
                int original = columnWidths[i];
                int percent = -constraints[i];
                int portion = (percent * tableWidth) / 100;

                if (portion < original) {
                    // Don't assign less than the computed minimum width for
                    // this table
                    continue;
                }

                // leftOver contains the amount of space we have left over
                // after assigning widths to all columns. That includes the
                // width already assigned to this column being considered.
                // We add that back in now since we'll be replacing the
                // assigned width, not add to it.
                int left = leftOver + columnWidths[i];

                if (portion > left) {
                    portion = left;
                }

                columnWidths[i] = portion;
                assigned += (portion - original);
                leftOver = left - portion;

                if (leftOver <= 0) {
                    break;
                }
            }
        }

        if (leftOver <= 0) {
            return;
        }

        // I have extra space to be distributed among unconstrained columns.
        // If there are no unconstrained columns, distribute it to the percentage
        // columns, and if there are none of those, distribute it evenly.
        int unconstrained = 0;
        int percentages = 0;

        for (int i = 0; i < columns; i++) {
            if (constraints[i] == AUTO) {
                unconstrained++;
            } else if (constraints[i] < 0) {
                percentages++;
            }
        }

        if (unconstrained == 0) {
            // TODO -- do proportional assignment here rather than just distributing
            // evenly among the columns??
            // No unconstrained columns            
            if (percentages == 0) {
                // No percentage columns: distribute space proportionally
                // among all columns
                int portion = leftOver / columns;
                int remainder = leftOver % columns;

                for (int i = 0; i < columns; i++) {
                    columnWidths[i] += portion;
                }

                columnWidths[0] += remainder;
            } else {
                // Assign the extra space to the percentage columns
                int portion = leftOver / percentages;
                int remainder = leftOver % percentages;

                for (int i = 0; i < columns; i++) {
                    if ((constraints[i] != AUTO) && (constraints[i] < 0)) {
                        columnWidths[i] += portion;
                        columnWidths[i] += remainder;
                        remainder = 0;
                    }
                }
            }

            return;
        }

        // Distribute the remaining space among the unconstrained columns. This is
        // distributed based on the columnMins and columnMaxes arrays, which states
        // requirements (mins) and desires (maxes) from the cell contents.
        while (true) {
            // Find the total for the cells that still allow more space,
            // so we can divy up the available space proportionally
            int colSum = 0;
            int count = 0;

            for (int i = 0; i < columns; i++) {
                if (constraints[i] != AUTO) {
                    continue;
                }

                if (columnWidths[i] < columnMaxes[i]) {
                    colSum += columnWidths[i];
                    count++;
                }
            }

            if ((colSum == 0) && (count != 0)) {
                // We have some columns that have been assigned zero width
                // that still allow some width. Since they have zero width
                // we can't do a proportional assignment, we'll just distribute
                // it evenly. This is an unusual scenario, but can happen when
                // you for example have a table with colspans to make the table
                // wider but nothing actually filling it.
                int portion = leftOver / count;
                int remainder = leftOver % count;

                for (int i = 0; i < columns; i++) {
                    if (constraints[i] != AUTO) {
                        continue;
                    }

                    if (columnWidths[i] < columnMaxes[i]) {
                        // XXX This is not right!!!
                        // I should consider columnMaxes and fixedWidths!
                        // Oooh, since colSum was 0, I know they don't have
                        // fixedwidth!!! (unless it was zero, but that would
                        // be weird)
                        columnWidths[i] += portion;

                        if (remainder != 0) {
                            columnWidths[i] += remainder;
                            remainder = 0;
                        }
                    }
                }

                return;
            }

            assigned = 0;

            int maxedPixels = 0; // Amount of pixels we've handed back to the pool

            for (int i = 0; i < columns; i++) {
                if (constraints[i] != AUTO) {
                    continue;
                }

                if (columnWidths[i] >= columnMaxes[i]) {
                    continue;
                }

                int portion = (columnWidths[i] * leftOver) / colSum;
                columnWidths[i] += portion;

                if (columnWidths[i] >= columnMaxes[i]) {
                    maxedPixels += (columnWidths[i] - columnMaxes[i]);
                    columnWidths[i] = columnMaxes[i];
                } else {
                    assigned += portion;
                }
            }

            // remainder from rounding errors
            // TODO fix this - computation isn't right, and I shouldn't
            // arbitrarily assign column 0, I should assign to one
            // of the columns above that was incremented
            //columnWidths[0] += leftOver-assigned-maxedPixels;
            if (maxedPixels == 0) {
                break;
            }

            leftOver = maxedPixels;
        }

        // TODO - if we have rounding errors, column widths may not add
        // up - should we correct that here by bumping up one of the columns?
    }

    public boolean isBorderSizeIncluded() {
        return true;
    }

    public Insets getCssSizeInsets() {
        // Tables include their own borders so they should not be included here; however,
        // we have to account for the caption
        if (captionBoxes != null) {
            return new Insets(tableTop, tableLeft, height - tableBottom, width - tableRight);
        } else {
            return new Insets(0, 0, 0, 0);
        }
    }

    public int getPrefWidth() {
        // See comment under getPrefMinWidth; I should be able to do
        // something better here. XXX Optimize: return the same computation.
        try {
            computingPrefWidth = true;

            return getPrefMinWidth();
        } finally {
            computingPrefWidth = false;
        }
    }

    public int getPrefMinWidth() {
        if ((columns == 0) || (rows == 0)) {
//            int tableWidth = CssLookup.getLength(table, XhtmlCss.WIDTH_INDEX);
            int tableWidth = CssUtilities.getCssLength(table, XhtmlCss.WIDTH_INDEX);
//            CssLookup.uncompute(table, XhtmlCss.WIDTH_INDEX);
            CssProvider.getEngineService().uncomputeValueForElement(table, XhtmlCss.WIDTH_INDEX);

            if (tableWidth != AUTO) {
                return tableWidth;
            } else {
                return 0;
            }
        }

        /*
          I can't just return getTableWidth() because if the sum of the
          minimum cell widths is greater than the table width, we need
          to use the cell width minimum sum.
        int tableWidth = getTableWidth();
        if (tableWidth != AUTO) {
            // XXX Hm, this isn't really true. If the table cells themselves
            // don't fit in the allocated width, we enlarge the table.
            // Thus, I might not be able to do the below computation.
            return tableWidth;
        }
        */

        // TODO I can probably do faster than this, e.g. call the cells
        // column by column, calling getPrefWidth on each and taking the
        // max (and accounting for table cell widths, colspan/rowspan>1,
        // etc.)  Also, I want to make sure I truly pick up the maximum
        // widths, not something constrained by my
        // 0-containingBlockWidth (which is set to 0 to force
        // percentages to compute to 0, ... if not I'd like to set it to
        // something really large.)
        int[] columnWidths = new int[columns];

        if (fixedLayout) {
            computeFixedColumnWidths(columnWidths, true);
        } else {
            // Passing in null formatting context: we assume that the
            // parent calling getPrefWidth will already have called
            // initializeHorizontalWidths recursively down the box tree,
            // so our columns have already been initialized.
            computeAutoColumnWidths(columnWidths, null, true);
        }

        int w = 0;

        for (int m = 0; m < columns; m++) {
            w += columnWidths[m];
        }

        // XXX is this right?
        w += (leftPadding + rightPadding);

        if (leftMargin != AUTO) {
            w += leftMargin;
        }

        if (rightMargin != AUTO) {
            w += rightMargin;
        }

        // Note: we don't add borderwidths and padding to the result
        // since as a special case, tables already include them in 
        // their specified width property
        // XXX That doesn't seem right!  AHA! Perhaps I need to consider whether WIDTH was set or not!!!
        w += (leftBorderWidth + rightBorderWidth);

        return w;
    }

    /** Compute the height of the table. May return AUTO.
     */
    private int getTableHeight() {
//        Value val = CssLookup.getValue(table, XhtmlCss.HEIGHT_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(table, XhtmlCss.HEIGHT_INDEX);

//        if (val == CssValueConstants.AUTO_VALUE) {
        if (CssProvider.getValueService().isAutoValue(cssValue)) {
            return AUTO;
        } else {
            // Percentages in table heights don't apply!
//            boolean wasPercentage =
//                val instanceof ComputedValue &&
//                (((ComputedValue)val).getCascadedValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE);
            boolean wasPercentage = cssValue instanceof CssComputedValue
                                    && CssProvider.getValueService().isOfPrimitivePercentageType(((CssComputedValue)cssValue).getCascadedValue());

            if (wasPercentage) {
                return 0;
            }

//            int tableHeight = (int)val.getFloatValue();
            int tableHeight = (int)cssValue.getFloatValue();

            // Empirically I've noticed mozilla treats tables differently; 
            // than regular boxes: the width: property affects the final
            // width of the table (including borders and padding).
            tableHeight -= (topBorderWidth + topPadding + bottomPadding + bottomBorderWidth);

            // Each of the cells will absorb the space for the top and left
            // padding area. However, we also need to have a padding area
            // on the right side of the rightmost column, so account for
            // that space here.
            tableHeight -= cellSpacing;

            return tableHeight;
        }
    }

    /** {@inheritDoc}
     * Overridden so I can add in the table position, which may not be 0,0 because
     * we may have a caption
     * @todo How is the clip area affected by this?
     */
    protected void paintBackground(Graphics g, int x, int y) {
        if (hidden) {
            return;
        }

        if (captionBoxes != null) {
            paintBox(g, x + tableLeft, y + tableTop, tableRight - tableLeft, tableBottom -
                tableTop);
        } else {
            paintBox(g, x, y, getWidth(), getHeight());
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public CssBox getCell(int row, int col) {
        return cells[row][col];
    }

    public int getCellSpan(int axis, int row, int col) {
        return (axis == CssBox.X_AXIS) ? rowspans[row][col] : colspans[row][col];
    }

    /* TODO -- implement for table too, where we have a large cellspacing.
       Users may be trying to resize in the cell space areas where the
       cells themselves are not active...
    public int getInternalResizeDirection(int x, int y) {
        if (tableDesignInfo == null) {
            return Cursor.DEFAULT_CURSOR;
        }

        int left = getAbsoluteX();

        // See if it looks like this component is near the computed border
        // of any of our boxes

        // First find our row
        int cellY = getAbsoluteY();
        int row = 0;
        for (; row < rows; row++) {
            int next = cellY + rowHeights[row];
            if (next > y) {
                break;
            }
            cellY = next;
        }
        if (row == rows) {
            return Cursor.DEFAULT_CURSOR;
        }
        int top = cellY;
        int height = rowHeights[row];
        return Cursor.DEFAULT_CURSOR;
    }
    */

    /** Cells are just like regular css container boxes, except
     * they have special margin and padding handling */
    private static class CellBox extends ContainerBox {
        private TableBox tableBox;
        int row;
        int col;
        int originalHeight; // before forcing height to match the table grid

        /** Construct a new CellBox, with the given default
         * border width to use if the element does not have a
         * specific CSS border-related property. Ditto for padding. */
        public CellBox(WebForm webform, Element element, BoxType boxType, boolean inline,
            TableBox tableBox) {
            super(webform, element, boxType, inline, false);
            this.tableBox = tableBox;
            initializeBackgroundDelayed(); // see comment in initializeBackground()
        }

        
        public Decoration getDecoration() {
            // XXX No decorations for cells.
            return null;
        }
        
        protected void initializeMargins() {
            // Margins are not allowed on <td>'s
            // TODO find reference for that here, I believe I read it somewhere
            // and it certainly matches what Mozilla seems to do
            leftMargin = rightMargin = topMargin = bottomMargin = 0;
            effectiveTopMargin = effectiveBottomMargin = 0;
        }

        protected void initializePadding() {
            // Padding: mozilla seems to allow padding properties to be set
            // on individual cells - and if so, it takes the larger of
            // the default cell padding provided by the table and the
            // per cell padding
            leftPadding = rightPadding = AUTO;
            topPadding = bottomPadding = AUTO;

            super.initializePadding();

            int cellPadding = tableBox.cellPadding;

            if (leftPadding == AUTO) {
                leftPadding = cellPadding;
            } else {
                leftPadding = Math.max(leftPadding, cellPadding);
            }

            if (rightPadding == AUTO) {
                rightPadding = cellPadding;
            } else {
                rightPadding = Math.max(rightPadding, cellPadding);
            }

            if (topPadding == AUTO) {
                topPadding = cellPadding;
            } else {
                topPadding = Math.max(topPadding, cellPadding);
            }

            if (bottomPadding == AUTO) {
                bottomPadding = cellPadding;
            } else {
                bottomPadding = Math.max(bottomPadding, cellPadding);
            }
        }

        protected void initializeBorder() {
            int cellBorderWidth = -1;
            int style = CssBorder.STYLE_NONE;
            int rules = tableBox.rules;

            if (tableBox.borderWidth > 0) {
                // If the table has a nonzero border width attribute, ensure that
                // we pick up the following default style (inset border width 1)
                // if more specific CSS rules haven't been set for a particular cell
                cellBorderWidth = 1;
                style = CssBorder.STYLE_INSET;
            } else if (tableBox.rules != CssBorder.FRAME_UNSET) {
                // If we have some rules applied we need to draw the internal cell
                // structure. Arguably I should mask out the external border sides here.
                cellBorderWidth = 1;

                // Don't show cell borders on the edges of the table; "rules" applies
                // only to the internal lines (collapsing model).
                if (row == 0) {
                    rules = rules & ~CssBorder.FRAME_TOP;
                }

                if ((row + tableBox.colspans[row][col]) == tableBox.rows) {
                    rules = rules & ~CssBorder.FRAME_BOTTOM;
                }

                if (col == 0) {
                    rules = rules & ~CssBorder.FRAME_LEFT;
                }

                if ((col + tableBox.colspans[row][col]) == tableBox.columns) {
                    rules = rules & ~CssBorder.FRAME_RIGHT;
                }

                style = CssBorder.STYLE_SOLID;
            }

            border = CssBorder.getBorder(getElement(), cellBorderWidth, style, rules);

            if (border != null) {
                leftBorderWidth = border.getLeftBorderWidth();
                topBorderWidth = border.getTopBorderWidth();
                bottomBorderWidth = border.getBottomBorderWidth();
                rightBorderWidth = border.getRightBorderWidth();
            }

            // NO design border here!
            //considerDesignBorder();
        }

        /**
            Compute the background color to use for a cell.
            The order is specified in the CSS2.1 spec, section 17.5.1:
            <ul>
            <li> Cells
            <li> Rows
            <li> Row Groups
            <li> Columns
            <li> Column Groups
            <li> Table
            </ul>
            <p>
        XXX TODO: Do this via stylesheet inheritance instead:
        <pre>
        td, th, tr {
            background: inherit;
            background-color: inherit;
        }
        </pre>
        Of course, that will make it difficult to get the exact right
        color given the table layers specified in 17.5.1.
         */
        protected void initializeBackground() {
            // Not calling super
            // And in fact doing nothing -- that's because this shouldn't be called 
            // during initializeInvariants() since at that point, our own constructor
            // hasn't been called yet (only the super-constructor in CssBox which calls
            // initializeInvariants) and we need to wait until we've initialized
            // the tableBox field in our own constructor, so initializeBackground
            // does nothing, and in our own constructor we call initializeBackgroundDelayed instead
        }

        protected void initializeBackgroundDelayed() {
            initializeBackgroundImage();

            Element element = getElement();
//            bg = CssLookup.getColor(element, XhtmlCss.BACKGROUND_COLOR_INDEX);
            bg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.BACKGROUND_COLOR_INDEX);

            if (bg != null) {
                return;
            }

            // Check row
//            Element row = findParent(element, CssValueConstants.TABLE_ROW_VALUE);
            Element row = findParent(element, CssProvider.getValueService().getTableRowValueConstant());

            if (row == null) {
                return;
            }

//            bg = CssLookup.getColor(row, XhtmlCss.BACKGROUND_COLOR_INDEX);
            bg = CssProvider.getValueService().getColorForElement(row, XhtmlCss.BACKGROUND_COLOR_INDEX);

            if (bg != null) {
                return;
            }

            // Check row group:
            // Check columns:
            // Check column groups:
            // TODO - not keeping column/columngroup/rowgroup information
            // for cells!
            // tbody/tfoot/thead
//            Element section = findParent(row, CssValueConstants.TABLE_ROW_GROUP_VALUE);
            Element section = findParent(row, CssProvider.getValueService().getTableRowGroupValueConstant());

            if (section == null) {
//                section = findParent(row, CssValueConstants.TABLE_HEADER_GROUP_VALUE);
                section = findParent(row, CssProvider.getValueService().getTableHeaderGroupValueConstant());
                        
                if (section == null) {
//                    section = findParent(row, CssValueConstants.TABLE_FOOTER_GROUP_VALUE);
                    section = findParent(row, CssProvider.getValueService().getTableFooterGroupValueConstant());

                    if (section == null) {
                        // The element is not within a tbody, thead or tfoot;
                        // it may be within an "implied" tbody so use that.
//                        if (tableBox.table instanceof RaveTableElement) {
//                            section = ((RaveTableElement)tableBox.table).getTbody();
//                        }
                        section = MarkupService.getTBodyElementForTableElement(tableBox.table);

                        if (section == null) {
                            return;
                        }
                    }
                }
            }

//            bg = CssLookup.getColor(section, XhtmlCss.BACKGROUND_COLOR_INDEX);
            bg = CssProvider.getValueService().getColorForElement(section, XhtmlCss.BACKGROUND_COLOR_INDEX);

            if (bg != null) {
                return;
            }

            // Check table:
            // Not necessary. The table will paint its own background.
            // TODO: Improve performance here.
            // If none of the cells specify a background other than the
            // table, have the table do its own background. If more than
            // half of the cells specify and alternative background,
            // let ALL the cells do their own painting (by setting the
            // remaining cells to the parent bg color).
        }

        public boolean isBorderSizeIncluded() {
            return false;
        }

        public Insets getCssSizeInsets() {
            return new Insets(0, 0, 0, 0);
        }

        // Same as in parent ContainerBox, but does NOT include call to super
        // which looks at the content width of the box itself; that's computed
        // from the constraints. In other words, if you set a width of 500px on
        // a <td> that should not result in 500 being passed in a a minimum and
        // possibly maximum column width in the width algorithm.
        public int getPrefMinWidth() {
            if (inline) {
//                Value val = CssLookup.getValue(getElement(), XhtmlCss.WHITE_SPACE_INDEX);
                CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(getElement(), XhtmlCss.WHITE_SPACE_INDEX);

//                if ((val == CssValueConstants.PRE_VALUE) ||
//                        (val == CssValueConstants.NOWRAP_VALUE)) {
                if (CssProvider.getValueService().isPreValue(cssValue)
                || CssProvider.getValueService().isNoWrapValue(cssValue)) {
                    return getPrefWidth();
                }
            }

            int largest = 0;
            int n = getBoxCount();

            for (int i = 0; i < n; i++) {
                CssBox child = getBox(i);

                if (child.getBoxType().isAbsolutelyPositioned()) {
                    continue;
                }

                int min = child.getPrefMinWidth();

                if (min > largest) {
                    largest = min;
                }
            }

            if (leftMargin != AUTO) {
                largest += leftMargin;
            }

            if (rightMargin != AUTO) {
                largest += rightMargin;
            }

            // Borders and padding can't be left auto, can they?
            largest += (leftBorderWidth + leftPadding + rightBorderWidth + rightPadding);

            // Don't call super, but do compute its borders and margins just in case
            //int curr = super.getPrefMinWidth();
            int curr = (leftBorderWidth + leftPadding + rightPadding + rightBorderWidth);

            if (leftMargin != AUTO) {
                curr += leftMargin;
            }

            if (rightMargin != AUTO) {
                curr += rightMargin;
            }

            if (curr > largest) {
                largest = curr;
            }

            return largest;
        }

        public int getPrefWidth() {
            int largest = 0;
            int n = getBoxCount();

            if (inline && !boxType.isAbsolutelyPositioned()) {
                // Let the line box compute the size of these children
                CssBox curr = getParent();

                while ((curr != null) && !(curr instanceof LineBoxGroup)) {
                    curr = curr.getParent();
                }

                if (curr != null) {
                    largest = ((LineBoxGroup)curr).getPrefWidth(boxes);
                } else {
                    // Shouldn't happen - this is just for safety right now
                    // Inline tag: add up all the children and use that
                    for (int i = 0; i < n; i++) {
                        CssBox child = getBox(i);

                        if (child.getBoxType().isAbsolutelyPositioned()) {
                            continue;
                        }

                        // XXX does not properly handle LineBox.LINEBREAK
                        largest += child.getPrefWidth();
                    }
                }
            } else {
                // Block tag: find the widest child and use that
                for (int i = 0; i < n; i++) {
                    CssBox child = getBox(i);

                    if (child.getBoxType().isAbsolutelyPositioned()) {
                        continue;
                    }

                    int min = child.getPrefWidth();

                    if (min > largest) {
                        largest = min;
                    }
                }
            }

            if (leftMargin != AUTO) {
                largest += leftMargin;
            }

            if (rightMargin != AUTO) {
                largest += rightMargin;
            }

            // Borders and padding can't be left auto, can they?
            largest += (leftBorderWidth + leftPadding + rightBorderWidth + rightPadding);

            // Don't call super, but do compute its borders and margins just in case
            //int curr = super.getPrefWidth();
            int curr = (leftBorderWidth + leftPadding + rightPadding + rightBorderWidth);

            if (leftMargin != AUTO) {
                curr += leftMargin;
            }

            if (rightMargin != AUTO) {
                curr += rightMargin;
            }

            if (curr > largest) {
                largest = curr;
            }

            return largest;
        }

        /** Locate a particular type of parent within the table. */
        private Element findParent(Element start, CssValue cssValue) {
            while (start != null) {
                // We can cast since we should be within a <table>
                // element
                Node parent = start.getParentNode();

                if (parent.getNodeType() != Node.ELEMENT_NODE) {
                    return null;
                }

                start = (Element)parent;

                if ((start == null) || (start == tableBox.table)) {
                    return null;
                }

//                if (CssLookup.getValue(start, XhtmlCss.DISPLAY_INDEX) == value) {
                if (cssValue.equals(CssProvider.getEngineService().getComputedValueForElement(start, XhtmlCss.DISPLAY_INDEX))) {
                    return start;
                }
            }

            return null;
        }

        /** Align the cell contents within the cell. This method is called
         * when the box height and contentHeight have already been initialized
         * to their final values, and the cell contents have also been
         * laid out.
         */
        void align() {
//            Value align = CssLookup.getValue(getElement(), XhtmlCss.VERTICAL_ALIGN_INDEX);
            CssValue cssAlign = CssProvider.getEngineService().getComputedValueForElement(getElement(), XhtmlCss.VERTICAL_ALIGN_INDEX);

//            if ((align == CssValueConstants.BASELINE_VALUE) ||
//                    (align == CssValueConstants.SUB_VALUE) || // sub==baseline in tables
//                    (align == CssValueConstants.SUPER_VALUE) || // ditto (see 17.5.3)
//                    (align == CssValueConstants.TEXT_TOP_VALUE) || // ditto
//                    (align == CssValueConstants.TEXT_BOTTOM_VALUE)) { // ditto
            if (CssProvider.getValueService().isBaseLineValue(cssAlign)
            || CssProvider.getValueService().isSubValue(cssAlign)
            || CssProvider.getValueService().isSuperValue(cssAlign)
            || CssProvider.getValueService().isTextTopValue(cssAlign)
            || CssProvider.getValueService().isTextTopValue(cssAlign)) {

                // XXX for now: just leave it as is. This is ROUGHLY right,
                // except for the case where there are different fonts
                // in different cells; in that case smaller fonts on the
                // first line would cause the cell contents to shift down
                // to align with the baseline for the tallest line.
//            } else if (align == CssValueConstants.TOP_VALUE) {
            } else if (CssProvider.getValueService().isTopValue(cssAlign)) {
                // Already done!
//            } else if (align == CssValueConstants.BOTTOM_VALUE) {
            } else if (CssProvider.getValueService().isBottomValue(cssAlign)) {
                // This implements bottom, not text-bottom so add separate
                // computation for that
                int childrenHeight = originalHeight;
                int delta = height - childrenHeight;

                for (int i = 0, n = getBoxCount(); i < n; i++) {
                    CssBox box = getBox(i);
                    box.setY(box.getY() + delta);
                }
//            } else if (align == CssValueConstants.MIDDLE_VALUE) {
            } else if (CssProvider.getValueService().isMiddleValue(cssAlign)) {
                int childrenHeight = originalHeight;
                int delta = (height - childrenHeight) / 2;

                for (int i = 0, n = getBoxCount(); i < n; i++) {
                    CssBox box = getBox(i);
                    box.setY(box.getY() + delta);
                }
            } else {
//                assert false : align;
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Unexpected alignment value, cssAlign=" + cssAlign)); // NOI18N
            }
        }

        /** When we need to relayout a table cell, the table bounds itself may
         *  need to be recomputed.
         */

        /*
        protected void layoutChild(CssBox box, FormatContext context,
                                   boolean handleChildren) {
            assert parent instanceof TableBox;
            parent.relayout(context);
            parent.parent.notifyChildResize(parent, context);
        }
        */
        protected CssBox notifyChildResize(CssBox child, FormatContext context) {
            ContainerBox parent = getParent();
            assert parent instanceof TableBox;
            parent.relayout(context);

            return parent.getParent().notifyChildResize(parent, context);
        }

        public int getInternalResizeDirection(int x, int y) {
//            if (tableBox.tableDesignInfo == null) {
//                return Cursor.DEFAULT_CURSOR;
//            }
            Element tableComponentRootElement = CssBox.getElementForComponentRootCssBox(tableBox);
            if (!webform.getDomProviderService().hasTableResizeSupport(tableComponentRootElement)) {
                return Cursor.DEFAULT_CURSOR;
            }

            int left = getAbsoluteX();
            int top = getAbsoluteY();

            // TODO - should I allow two-dimension resizing? e.g. if you're
            // close to BOTH a row and a column border, can you resize both
            // at he same time? I suppose that makes sense...
            // TODO - decide if borderwidth affects this computation
            int spacing = tableBox.cellSpacing / 2;

//            MarkupDesignBean tableMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(tableBox);
            if ((col > 0) && (Math.abs(x - left) < (BORDER_RESIZE_DISTANCE + spacing))) {
                // Does it matter what height we pass in here?
                // I suppose I could pass in both higher and lower values
                // than the current size to see if we effectively will be able
                // to change the size, in case minimums and maximums constrain it
//                if (tableBox.tableDesignInfo.testResizeColumn(tableBox.getDesignBean(), row, col - 1, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeColumn(tableMarkupDesignBean, row, col - 1, 50) != -1) {
                if (webform.getDomProviderService().testResizeColumn(tableComponentRootElement, row, col - 1, 50) != -1) {
                    return Cursor.W_RESIZE_CURSOR;
                }
            }

            if ((col < (tableBox.columns - 1)) &&
                    (Math.abs(x - (left + width)) < (BORDER_RESIZE_DISTANCE + spacing))) {
//                if (tableBox.tableDesignInfo.testResizeColumn(tableBox.getDesignBean(), row, col, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeColumn(tableMarkupDesignBean, row, col, 50) != -1) {
                if (webform.getDomProviderService().testResizeColumn(tableComponentRootElement, row, col, 50) != -1) {
                    return Cursor.E_RESIZE_CURSOR;
                }
            }

            if ((row > 0) && (Math.abs(y - top) < (BORDER_RESIZE_DISTANCE + spacing))) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableBox.getDesignBean(), row - 1, col, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableMarkupDesignBean, row - 1, col, 50) != -1) {
                if (webform.getDomProviderService().testResizeRow(tableComponentRootElement, row - 1, col, 50) != -1) {
                    return Cursor.N_RESIZE_CURSOR;
                }
            }

            if ((row < (tableBox.rows - 1)) &&
                    (Math.abs(y - (top + height)) < (BORDER_RESIZE_DISTANCE + spacing))) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableBox.getDesignBean(), row, col, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableMarkupDesignBean, row, col, 50) != -1) {
                if (webform.getDomProviderService().testResizeRow(tableComponentRootElement, row, col, 50) != -1) {
                    return Cursor.S_RESIZE_CURSOR;
                }
            }

            return Cursor.DEFAULT_CURSOR;
        }

        public Interaction getInternalResizer(int x, int y) {
//            if (tableBox.tableDesignInfo == null) {
//                return null;
//            }
            Element tableComponentRootElement = CssBox.getElementForComponentRootCssBox(tableBox);
            if (!webform.getDomProviderService().hasTableResizeSupport(tableComponentRootElement)) {
                return null;
            }

            int minimum = 0;
            int maximum = Integer.MAX_VALUE;
            int left = getAbsoluteX();
            int top = getAbsoluteY();

            // TODO - should I allow two-dimension resizing? e.g. if you're
            // close to BOTH a row and a column border, can you resize both
            // at he same time? I suppose that makes sense...
            // TODO - decide if borderwidth affects this computation
            int spacing = tableBox.cellSpacing / 2;

            Element element = getElement();
//            MarkupDesignBean tableMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(tableBox);
            if (Math.abs(x - left) < (BORDER_RESIZE_DISTANCE + spacing)) {
                // Does it matter what height we pass in here?
                // I suppose I could pass in both higher and lower values
                // than the current size to see if we effectively will be able
                // to change the size, in case minimums and maximums constrain it
//                if (tableBox.tableDesignInfo.testResizeColumn(tableBox.getDesignBean(), row, col - 1, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeColumn(tableMarkupDesignBean, row, col - 1, 50) != -1) {
                if (webform.getDomProviderService().testResizeColumn(tableComponentRootElement, row, col - 1, 50) != -1) {
                    TableResizer tableResizer =
//                        new TableResizer(webform, tableBox.getDesignBean(), tableBox.tableDesignInfo,
                        new TableResizer(webform, /*tableMarkupDesignBean, tableBox.tableDesignInfo,*/ tableComponentRootElement,
                            CssBox.Y_AXIS, true, left, tableBox.getAbsoluteY(), getWidth(),
                            tableBox.getHeight(), minimum, maximum, row, col, element);

                    return tableResizer;
                }
            }

            if (Math.abs(x - (left + width)) < (BORDER_RESIZE_DISTANCE + spacing)) {
//                if (tableBox.tableDesignInfo.testResizeColumn(tableBox.getDesignBean(), row, col, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeColumn(tableMarkupDesignBean, row, col, 50) != -1) {
                if (webform.getDomProviderService().testResizeColumn(tableComponentRootElement, row, col, 50) != -1) {
                    TableResizer tableResizer =
//                        new TableResizer(webform, tableBox.getDesignBean(), tableBox.tableDesignInfo,
                        new TableResizer(webform, /*tableMarkupDesignBean, tableBox.tableDesignInfo,*/ tableComponentRootElement,
                            CssBox.Y_AXIS, false, left, tableBox.getAbsoluteY(), getWidth(),
                            tableBox.getHeight(), minimum, maximum, row, col, element);

                    return tableResizer;
                }
            }

            if (Math.abs(y - top) < (BORDER_RESIZE_DISTANCE + spacing)) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableBox.getDesignBean(), row - 1, col, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableMarkupDesignBean, row - 1, col, 50) != -1) {
                if (webform.getDomProviderService().testResizeRow(tableComponentRootElement, row - 1, col, 50) != -1) {
                    System.out.println("Probably should use row before here!");

                    TableResizer tableResizer =
//                        new TableResizer(webform, tableBox.getDesignBean(), tableBox.tableDesignInfo,
                        new TableResizer(webform, /*tableMarkupDesignBean, tableBox.tableDesignInfo,*/ tableComponentRootElement,
                            CssBox.X_AXIS, true, tableBox.getAbsoluteX(), top, getHeight(),
                            tableBox.getWidth(), minimum, maximum, row, col, element);

                    return tableResizer;
                }
            }

            if (Math.abs(y - (top + height)) < (BORDER_RESIZE_DISTANCE + spacing)) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableBox.getDesignBean(), row, col, 50) != -1) {
//                if (tableBox.tableDesignInfo.testResizeRow(tableMarkupDesignBean, row, col, 50) != -1) {
                if (webform.getDomProviderService().testResizeRow(tableComponentRootElement, row, col, 50) != -1) {
                    TableResizer tableResizer =
//                        new TableResizer(webform, tableBox.getDesignBean(), tableBox.tableDesignInfo,
                        new TableResizer(webform, /*tableMarkupDesignBean, tableBox.tableDesignInfo,*/ tableComponentRootElement,
                            CssBox.X_AXIS, false, tableBox.getAbsoluteX(), top, getHeight(),
                            tableBox.getWidth(), minimum, maximum, row, col, element);

                    return tableResizer;
                }
            }

            return null;
        }
    }

    static class OccupiedBox extends CellBox {
        OccupiedBox() {
            super(null, null, BoxType.NONE, true, null);
        }

        public void initialize() {
        }

        protected void initializeInvariants() {
        }

        protected void initializeBackgroundDelayed() {
        }

//        public String toString() {
//            return "OCCUPIED"; // NOI18N
//        }

        public boolean isPlaceHolder() {
            return true;
        }
    }
}
