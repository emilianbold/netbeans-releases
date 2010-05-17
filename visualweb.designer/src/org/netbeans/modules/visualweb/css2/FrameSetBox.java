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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * FrameSetBox represents a &lt;frameset&gt; tag.
 * Info on how to combine frames and JSP:
 *  http://swforum.sun.com/jive/thread.jspa?threadID=47488&tstart=255
 * This is not working well yet.
 *
 * @author Tor Norbye
 *
 */
public class FrameSetBox extends PageBox {
    /*
    protected String paramString() {
        return super.paramString() + ", " + markup;
    }
    */

    /** Number of columns in the table. */
    private int columns = -1;

    /** Number of rows in the table */
    private int rows = -1;

    /** Array of cells holding boxes for each cell. Some cells may be empty
     * (e.g. when no frame was specified)
     */
    private CssBox[][] cells;
    private int[] colSizes;
    private byte[] colTypes;
    private int[] rowSizes;
    private byte[] rowTypes;
    private int colRelativeTotals;
    private int rowRelativeTotals;

    /** Use the "getFrameSetBox" factory method instead */
    private FrameSetBox(DesignerPane pane, WebForm webform, Element element, BoxType boxType,
        boolean inline, boolean replaced) {
        super(pane, webform, element, boxType, inline, replaced);
    }

    /** Create a new FrameSetBox, or provide one from a cache */
    public static FrameSetBox getFrameSetBox(DesignerPane pane, WebForm webform, Element element,
        BoxType boxType, HtmlTag tag, boolean inline) {
        FrameSetBox box = new FrameSetBox(pane, webform, element, boxType, inline, tag.isReplacedTag());

        return box;
    }

//    public String toString() {
//        return "FrameSetBox[" + paramString() + "]";
//    }

    protected void layoutContext(FormatContext context) {
        relayout(context);
    }

    protected void createChildren(CreateContext context) {
        boolean newContext = false;

        if (context == null) {
            newContext = true;

            // TODO instead of checking on the box count, which could be 0
            // for valid reasons, have a dedicated flag here which is
            // invalidated on document edits, etc.
            context = new CreateContext();
            context.pushPage(webform);

//            Font font = CssLookup.getFont(body, DesignerSettings.getInstance().getDefaultFontSize());
//            Font font = CssProvider.getValueService().getFontForElement(body, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
//            context.metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
            // XXX Missing text.
            context.metrics = CssUtilities.getDesignerFontMetricsForElement(body, null, webform.getDefaultFontSize());

            //super.createChildren(cc);
        }

        Element element = getElement();
        String colString = element.getAttribute(HtmlAttribute.COLS);
        String[] split = null;

        if (colString.length() > 0) {
            // TODO - precompile the regular expression?
            split = colString.split(","); // NOI18N
        }

        if ((split == null) || (split.length == 0) || (split.length == 1)) {
            colSizes = new int[] { 1 };
            colTypes = new byte[] { HtmlAttribute.VALUE_RELATIVE };
        } else {
            colSizes = new int[split.length];
            colTypes = new byte[split.length];

            int percentTotals = 0;

            for (int i = 0; i < split.length; i++) {
                colSizes[i] = HtmlAttribute.parseInt(split[i]);
                colTypes[i] = HtmlAttribute.getNumberType(split[i]);

                if ((colSizes[i] == 100) && (colTypes[i] == HtmlAttribute.VALUE_PERCENTAGE)) {
                    // As per the spec, 100% is the same as *
                    // hence the mapping.
                    colSizes[i] = 1;
                    colTypes[i] = HtmlAttribute.VALUE_RELATIVE;
                } else if ((colSizes[i] == 0) && (colTypes[i] == HtmlAttribute.VALUE_RELATIVE)) {
                    // "*" should be treated as 1*, not 0*
                    colSizes[i] = 1;
                }

                if (colTypes[i] == HtmlAttribute.VALUE_PERCENTAGE) {
                    percentTotals += colSizes[i];
                } else if (colTypes[i] == HtmlAttribute.VALUE_RELATIVE) {
                    colRelativeTotals += colSizes[i];
                }
            }

            if (percentTotals > 100) {
                for (int i = 0; i < colSizes.length; i++) {
                    // XXX make isPercentage() method
                    if (colTypes[i] == HtmlAttribute.VALUE_PERCENTAGE) {
                        colSizes[i] = (colSizes[i] * 100) / percentTotals;
                    }
                }

                percentTotals = 100;
            } // XXX what if percentTotals < 100 ???
        }

        String rowString = element.getAttribute(HtmlAttribute.ROWS);

        if (rowString.length() > 0) {
            // TODO - precompile the regular expression?
            split = rowString.split(","); // NOI18N
        }

        if ((split == null) || (split.length == 0) || (split.length == 1)) {
            colSizes = new int[] { 1 };
            rowTypes = new byte[] { HtmlAttribute.VALUE_RELATIVE };
        } else {
            rowSizes = new int[split.length];
            rowTypes = new byte[split.length];

            int percentTotals = 0;

            for (int i = 0; i < split.length; i++) {
                rowSizes[i] = HtmlAttribute.parseInt(split[i]);
                rowTypes[i] = HtmlAttribute.getNumberType(split[i]);

                if ((rowSizes[i] == 100) && (rowTypes[i] == HtmlAttribute.VALUE_PERCENTAGE)) {
                    // As per the spec, 100% is the same as *
                    // hence the mapping.
                    rowSizes[i] = 1;
                    rowTypes[i] = HtmlAttribute.VALUE_RELATIVE;
                } else if ((rowSizes[i] == 0) && (rowTypes[i] == HtmlAttribute.VALUE_RELATIVE)) {
                    // "*" should be treated as 1*, not 0*
                    rowSizes[i] = 1;
                }

                if (rowTypes[i] == HtmlAttribute.VALUE_PERCENTAGE) {
                    percentTotals += rowSizes[i];
                } else if (rowTypes[i] == HtmlAttribute.VALUE_RELATIVE) {
                    rowRelativeTotals += rowSizes[i];
                }
            }

            if (percentTotals > 100) {
                for (int i = 0; i < rowSizes.length; i++) {
                    // XXX make isPercentage() method
                    if (rowTypes[i] == HtmlAttribute.VALUE_PERCENTAGE) {
                        rowSizes[i] = (rowSizes[i] * 100) / percentTotals;
                    }
                }

                percentTotals = 100;
            }
        }

        // XXX can framesets contain anything but frame boxes (no)?
        // Can they be arbitrarily nested (yes)? Can they occur anywhere
        // EXCEPT as a replacement for body? (no, except as children
        // of other frames). Do CSS widths and heights on frames have
        // any effect on the layout? (no)
        //
        // (Answers determined empirically from Mozilla and
        // Safari. Safari accepts a frameset inside a body tag,
        // mozilla does not.)
        rows = rowSizes.length;
        columns = colSizes.length;
        cells = new CssBox[rows][columns];

        // The cell boxes should be either FrameSetBoxes or FrameBoxes
        setProbableChildCount(rows * columns);

        int currentRow = 0;
        int currentCol = 0;

        NodeList list = element.getChildNodes();
        int len = list.getLength();

        for (int i = 0; i < len; i++) {
            Node trn = (Node)list.item(i);

            if (trn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element e = (Element)trn;
            String tagName = e.getTagName();
            HtmlTag tag = HtmlTag.getTag(tagName);

            if ((tag == HtmlTag.FRAME) || (tag == HtmlTag.FRAMESET)) {
                boolean inline = false;
                ContainerBox box = null;

                // FrameSetBoxes are not created by the BoxFactory since
                // they are only allowed in place of the body tag (handled
                // in PageBox' factory method) or as a child of another
                // FrameSetBox (handled here)
                if (tag == HtmlTag.FRAMESET) {
                    FrameSetBox fsb =
                        FrameSetBox.getFrameSetBox(webform.getPane(), webform, e, boxType,
                            tag, inline);
                    fsb.tag = HtmlTag.FRAMESET;
                    fsb.isTopLevel = false;
                    box = fsb;
                } else {
                    box = FrameBox.getFrameBox(context, webform, e, BoxType.STATIC, tag, false);
                    box.tag = HtmlTag.FRAME;
                }

                box.clipOverflow = true;
                box.initialize();
                addBox(box, null, null);
                finishLineBox(context); // ensure that cell has its own linebox
                box.createChildren(context);
                finishLineBox(context); // ensure that content outside doesn't spill into cell linebox

                cells[currentRow][currentCol] = box;
                currentCol++;

                if (currentCol == columns) {
                    currentCol = 0;
                    currentRow++;

                    if (currentRow == rows) {
                        break; // additional frames are ignored
                    }
                }
            }
        }

        if (newContext) {
            fixedBoxes = context.getFixedBoxes();
            context.popPage();
        }
    }

    public void relayout(FormatContext context) {
        // Ensure that createChildren has run and has initialized
        // the row and column fields
        if ((columns <= 0) || (rows <= 0)) {
            return;
        }

        // Compute column widths
        // Compute row heights
        int[] colWidths = new int[columns];
        int[] rowHeights = new int[rows];

        layout(colWidths, rowHeights, context);

        // layout(colWidths, rowHeights, context);
        effectiveTopMargin = topMargin;
        effectiveBottomMargin = bottomMargin;
    }

    private void layout(int[] columnWidths, int[] rowHeights, FormatContext context) {
        spread(containingBlockWidth, columnWidths, colSizes, colTypes, colRelativeTotals);
        spread(containingBlockHeight, rowHeights, rowSizes, rowTypes, rowRelativeTotals);

        // For debugging only: color frame backgrounds
        //Color[] colors = { Color.red, Color.blue, Color.green, Color.gray, 
        //                   Color.pink, Color.yellow, Color.orange, 
        //                   Color.lightGray, Color.magenta, Color.cyan };
        //int currentColor = 0;
        CssBorder border = CssBorder.getBorder(CssBorder.STYLE_INSET, 3, Color.GRAY);

        // TODO -- I should use cell spacing here, like Safari.
        // Alternatively, rather than having individual frame borders
        // I could paint a complete grid, the way Mozilla does (similar
        // to collapsing border model.)
        int y = 0;

        for (int i = 0; i < rows; i++) {
            int x = 0;

            for (int j = 0; j < columns; j++) {
                CssBox box = cells[i][j];

                if (box != null) {
                    formatCell(i, j, columnWidths, rowHeights, context);

                    box.border = border;
                    box.leftBorderWidth = border.getLeftBorderWidth();
                    box.topBorderWidth = border.getTopBorderWidth();
                    box.bottomBorderWidth = border.getBottomBorderWidth();
                    box.rightBorderWidth = border.getRightBorderWidth();

                    box.width = columnWidths[j];
                    box.contentWidth =
                        box.width -
                        (box.leftBorderWidth + box.leftPadding + box.rightPadding +
                        box.rightBorderWidth);
                    box.height = rowHeights[i];
                    box.contentHeight =
                        box.height -
                        (box.topBorderWidth + box.topPadding + box.bottomPadding +
                        box.bottomBorderWidth);

                    // For debugging only: color frame backgrounds
                    //cells[i][j].bg = colors[currentColor];
                    //currentColor = (currentColor + 1) % colors.length;
                    cells[i][j].setLocation(x, y);
                }

                x += columnWidths[j];
            }

            y += rowHeights[i];
        }
    }

    private void formatCell(int row, int col, int[] columnWidths, int[] rowHeights,
        FormatContext context) {
        CssBox box = cells[row][col];
        int ac = columnWidths[col];
        int ah = rowHeights[row];
        box.setContainingBlock(0, 0, ac, ah);
        box.contentWidth =
            ac - box.leftBorderWidth - box.leftPadding - box.rightPadding - box.rightBorderWidth;
        box.contentHeight =
            ah - box.topBorderWidth - box.topPadding - box.bottomPadding - box.bottomBorderWidth;

        if (box instanceof FrameSetBox) {
            ((FrameSetBox)box).layoutContext(context);
        } else {
            box.relayout(context);
        }

        box.inline = false;
        box.replaced = false;

        box.boxType = BoxType.STATIC; // prevent "bad" docs from screwing things up

        // by setting position: absolute on <td>'s for example
        box.computeVerticalLengths(context);
        box.height =
            box.topBorderWidth + box.topPadding + box.contentHeight + box.bottomPadding +
            box.bottomBorderWidth /* XXX + cellSpacing*/;
    }

    /**
     * This method is based on
     *  javax.swing.text.html.FrameSetView.spread().
     *
     *
     * This method is responsible for returning in span[] the
     * span for each child view along the major axis.  it
     * computes this based on the information that extracted
     * from the value of the ROW/COL attribute.
     */
    private void spread(int targetSpan, int[] span, int[] sizes, byte[] types, int relativeTotals) {
        if (targetSpan == 0) {
            return;
        }

        int tempSpace = 0;
        int remainingSpace = targetSpan;

        // allocate the absolute's first, they have
        // precedence
        //
        for (int i = 0; i < span.length; i++) {
            if (types[i] == HtmlAttribute.VALUE_ABSOLUTE) {
                span[i] = sizes[i];
                remainingSpace -= span[i];
            }
        }

        // then deal with percents.
        //
        tempSpace = remainingSpace;

        for (int i = 0; i < span.length; i++) {
            if (types[i] == HtmlAttribute.VALUE_PERCENTAGE) {
                if (tempSpace > 0) {
                    span[i] = (sizes[i] * tempSpace) / 100;
                    remainingSpace -= span[i];
                } else { // tempSpace <= 0
                    span[i] = targetSpan / span.length;
                    remainingSpace -= span[i];
                }
            }
        }

        // allocate remainingSpace to relative
        if ((remainingSpace > 0) && (relativeTotals > 0)) {
            for (int i = 0; i < span.length; i++) {
                if (types[i] == HtmlAttribute.VALUE_RELATIVE) {
                    span[i] = (remainingSpace * sizes[i]) / relativeTotals;
                }
            }
        } else if (remainingSpace > 0) {
            // There are no relative columns/rows and the space has been
            // under- or overallocated.  In this case, turn all the 
            // percentage and pixel specified columns/rows to percentage 
            // columns/rows based on the ratio of their pixel count to the
            // total "virtual" size. (In the case of percentage columns/rows,
            // the pixel count would equal the specified percentage
            // of the screen size.
            // This action is in accordance with the HTML
            // 4.0 spec (see section 8.3, the end of the discussion of
            // the FRAMESET tag).  The precedence of percentage and pixel
            // specified columns/rows is unclear (spec seems to indicate that
            // they share priority, however, unspecified what happens when
            // overallocation occurs.)
            // addendum is that we behave similiar to netscape in that specified
            // widths have precedance over percentage widths...
            float vTotal = (float)(targetSpan - remainingSpace);
            float[] tempPercents = new float[span.length];
            remainingSpace = targetSpan;

            for (int i = 0; i < span.length; i++) {
                // ok we know what our total space is, and we know how large each
                // column/rows should be relative to each other... therefore we can use
                // that relative information to deduce their percentages of a whole
                // and then scale them appropriately for the correct size
                tempPercents[i] = ((float)span[i] / vTotal) * 100.00f;
                span[i] = (int)(((float)targetSpan * tempPercents[i]) / 100.00f);
                remainingSpace -= span[i];
            }

            // this is for just in case there is something left over.. if there is we just
            // add it one pixel at a time to the frames in order.. We shouldn't really ever get
            // here and if we do it shouldn't be with more than 1 pixel, maybe two.
            int i = 0;

            while (remainingSpace != 0) {
                if (remainingSpace < 0) {
                    span[i++]--;
                    remainingSpace++;
                } else {
                    span[i++]++;
                    remainingSpace--;
                }

                // just in case there are more pixels than frames...should never happen..
                if (i == span.length) {
                    i = 0;
                }
            }
        }
    }

    // Because PageBox overrides these
    // TODO -- should I create a BodyBox which does some of the specific stuff
    // I don't want inherited into framesetbox?
    public int getAbsoluteX() {
        ContainerBox parent = getParent();
        if (positionedBy != parent) {
            return positionedBy.getAbsoluteX() + getX() + leftMargin;
        }

        if (parent != null) {
            return parent.getAbsoluteX() + x + leftMargin;
        } else {
            return x + leftMargin;
        }
    }

    // Because PageBox overrides these
    public int getAbsoluteY() {
        ContainerBox parent = getParent();
        if (positionedBy != parent) {
            return positionedBy.getAbsoluteY() + getY() + effectiveTopMargin;
        }

        if (parent != null) {
            return parent.getAbsoluteY() + y + effectiveTopMargin;
        } else {
            return y + effectiveTopMargin;
        }
    }
}
