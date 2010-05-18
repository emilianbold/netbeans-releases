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
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;


/**
 * A border which paints according to CSS2 specifications. In particular,
 * the border brush may have different thickness for the top, bottom,
 * left and right edges, and each edge may have a different style, such
 * as solid, dashed, dotted, or inset.
 *
 * @todo Consider supporting the proposed CSS3 border-radius property.
 *  http://www.w3.org/TR/2002/WD-css3-border-20021107/#the-border-radius
 *  The mozilla one is -moz-border-radius (-topright, -topleft, -bottomright,
 *  -bottomleft).  The Braveheart stylesheets already use this property to
 *  get rounded buttons.
 *
 * @author  Tor Norbye
 */
public class CssBorder {
    /** No sides */
    public static final int FRAME_VOID = 0;

    /** Side above box */
    public static final int FRAME_TOP = 1;

    /** Side below box */
    public static final int FRAME_BOTTOM = 2;

    /** Left-hand-side of box */
    public static final int FRAME_LEFT = 4;

    /** Right-hand-side of box */
    public static final int FRAME_RIGHT = 8;

    /** All sides */
    public static final int FRAME_BOX = FRAME_TOP | FRAME_BOTTOM | FRAME_LEFT | FRAME_RIGHT;

    /** Same as all sides, but used for the case where we haven't specifically set
     * the frame/rules property, so I can tell "all" from "not set". */
    public static final int FRAME_UNSET = FRAME_BOX | 128;

    /* Now defined in BorderWidthManager
    // Border thickness - these are the values Mozilla 1.5 empirically
    // seems to use (on Solaris, hopefully not platform specific)
    // In the working draft for the CSS3 Box Model, they mention UA's
    // could make it depend on the font size, e.g. use the below sizes
    // when the font size is less than 17pt, and bump them up for bigger
    // fonts.
    public static final int WIDTH_THICK = 5;
    */
    public static final int WIDTH_THIN = 1;
    public static final int WIDTH_MEDIUM = 3;
    static final int STYLE_UNKNOWN = -1;
    static final int STYLE_NONE = 0;
    static final int STYLE_SOLID = 1;
    static final int STYLE_DASHED = 2;
    static final int STYLE_DOTTED = 3;
    static final int STYLE_DOUBLE = 4;
    static final int STYLE_GROOVE = 5;
    static final int STYLE_RIDGE = 6;
    static final int STYLE_INSET = 7;
    static final int STYLE_OUTSET = 8;
    private static CssBorder designerBorder = null;
    private static CssBorder emptyBorder = null;
    private static final Color TRANSPARENT = new Color(0, 0, 0);
    private final Color topColor;
    private final Color bottomColor;
    private final Color leftColor;
    private final Color rightColor;
    private Color topAltColor;
    private Color bottomAltColor;
    private Color leftAltColor;
    private Color rightAltColor;
    private Stroke topStroke;
    private Stroke bottomStroke;
    private Stroke leftStroke;
    private Stroke rightStroke;
    private final int topWidth;
    private final int bottomWidth;
    private final int rightWidth;
    private final int leftWidth;
    private Stroke prevStroke = null;
    private int prevWidth = -1;
    private int prevStyle = -1;
    int[] pointsX = new int[4];
    int[] pointsY = new int[4];

    /**
     * Don't use directly - see static getBorder() factory method.
     * @todo Improve color computations here to do the right thing when
     *  I have a fully saturated (or unsaturated) color. Also see
     *  HtmlLabelUI for some color computation code which might be
     *  interesting.
     */
    private CssBorder(int topWidth, int bottomWidth, int rightWidth, int leftWidth, int topStyle,
        int rightStyle, int bottomStyle, int leftStyle, Color topColor, Color bottomColor,
        Color rightColor, Color leftColor) {
        this.topWidth = topWidth;
        this.bottomWidth = bottomWidth;
        this.leftWidth = leftWidth;
        this.rightWidth = rightWidth;

        // Reuse strokes when possible        
        if ((leftStyle == STYLE_SOLID) || (leftStyle == STYLE_DOUBLE)) {
            // Use color as-is
        } else if (leftStyle == STYLE_INSET) {
            leftColor = leftColor.darker();
        } else if (leftStyle == STYLE_OUTSET) {
            leftColor = leftColor.brighter();
        } else if (leftStyle == STYLE_RIDGE) {
            leftAltColor = leftColor.darker();
            leftColor = leftColor.brighter();
        } else if (leftStyle == STYLE_GROOVE) {
            leftAltColor = leftColor.brighter();
            leftColor = leftColor.darker();
        } else {
            leftStroke = getStroke(leftWidth, leftStyle);
        }

        if ((topStyle == STYLE_SOLID) || (topStyle == STYLE_DOUBLE)) {
            // Use color as-is
        } else if (topStyle == STYLE_INSET) {
            topColor = topColor.darker();
        } else if (topStyle == STYLE_OUTSET) {
            topColor = topColor.brighter();
        } else if (topStyle == STYLE_RIDGE) {
            topAltColor = topColor.darker();
            topColor = topColor.brighter();
        } else if (topStyle == STYLE_GROOVE) {
            topAltColor = topColor.brighter();
            topColor = topColor.darker();
        } else if (topStyle != STYLE_NONE) {
            topStroke = getStroke(topWidth, topStyle);
        }

        if ((rightStyle == STYLE_SOLID) || (rightStyle == STYLE_DOUBLE)) {
            // Use color as-is
        } else if (rightStyle == STYLE_INSET) {
            rightColor = rightColor.brighter();
        } else if (rightStyle == STYLE_OUTSET) {
            rightColor = rightColor.darker();
        } else if (rightStyle == STYLE_RIDGE) {
            rightAltColor = rightColor.brighter();
            rightColor = rightColor.darker();
        } else if (rightStyle == STYLE_GROOVE) {
            rightAltColor = rightColor.darker();
            rightColor = rightColor.brighter();
        } else if (rightStyle != STYLE_NONE) {
            rightStroke = getStroke(rightWidth, rightStyle);
        }

        if ((bottomStyle == STYLE_SOLID) || (bottomStyle == STYLE_DOUBLE)) {
            // Use color as-is
        } else if (bottomStyle == STYLE_INSET) {
            bottomColor = bottomColor.brighter();
        } else if (bottomStyle == STYLE_OUTSET) {
            bottomColor = bottomColor.darker();
        } else if (bottomStyle == STYLE_RIDGE) {
            bottomAltColor = bottomColor.brighter();
            bottomColor = bottomColor.darker();
        } else if (bottomStyle == STYLE_GROOVE) {
            bottomAltColor = bottomColor.darker();
            bottomColor = bottomColor.brighter();
        } else {
            bottomStroke = getStroke(bottomWidth, bottomStyle);
        }

        // TODO [PERFORMANCE] - try to "share" strokes and colors here
        // when edges have the same original colors, widths
        // or styles.

        this.leftColor = leftColor;
        this.rightColor = rightColor;
        this.bottomColor = bottomColor;
        this.topColor = topColor;
    }

    /* (This comment might be obsolete, I might have done this already)
    TODO: force widths to 0 if no style has been set

    From http://web.oreilly.com/news/csstop10_0500.html:

    7) Borders without style don't exist. Since the default border-style is
    none, if you don't explicitly declare a border-style for a border then
    it will have no existence at all, and therefore zero width. After all,
    something can have width only if it exists. If you want extra space to
    appear around an element such as an image, use margins, not a border
    width.
    */

    /** Return a border capable of painting the given element's border
     * preferences. The top, bottom, right and left parameters list
     * the border thickness to use for each edge. (These are looked up
     * by the box (the client of this class) rather than here in the
     * border since the border dimensions are part of the geometry of
     * the box.)    May return null if no border should be painted.
     */
    public static CssBorder getBorder(Element element) {
        return getBorder(element, -1, STYLE_NONE, FRAME_BOX);
    }

    /** Same as {@link getBorder(Document, Element)} but allows you
     * to specify a default width and style which will be used for
     * any border edge that does not have an explicit CSS property
     * setting. This is used to force table cells to take on the
     * table element's border attribute width, for example.
     * @param defaultWidth The default width to use, or -1 if a
     * default border width should not be set.
     * @param defaultStyle The default style to use for edges that
     * do not specify a border, or STYLE_NONE.
     */
    public static CssBorder getBorder(Element element, int defaultWidth, int defaultStyle, int mask) {
        // Set to -1 so we can detect later that one wasn't set
        int leftStyle = getBorderStyle(element, XhtmlCss.BORDER_LEFT_STYLE_INDEX);
        int rightStyle = getBorderStyle(element, XhtmlCss.BORDER_RIGHT_STYLE_INDEX);
        int topStyle = getBorderStyle(element, XhtmlCss.BORDER_TOP_STYLE_INDEX);
        int bottomStyle = getBorderStyle(element, XhtmlCss.BORDER_BOTTOM_STYLE_INDEX);

        if (leftStyle == STYLE_UNKNOWN) {
            if ((mask & FRAME_LEFT) != 0) {
                leftStyle = defaultStyle;
            } else {
                leftStyle = STYLE_NONE;
            }
        }

        if (rightStyle == STYLE_UNKNOWN) {
            if ((mask & FRAME_RIGHT) != 0) {
                rightStyle = defaultStyle;
            } else {
                rightStyle = STYLE_NONE;
            }
        }

        if (topStyle == STYLE_UNKNOWN) {
            if ((mask & FRAME_TOP) != 0) {
                topStyle = defaultStyle;
            } else {
                topStyle = STYLE_NONE;
            }
        }

        if (bottomStyle == STYLE_UNKNOWN) {
            if ((mask & FRAME_BOTTOM) != 0) {
                bottomStyle = defaultStyle;
            } else {
                bottomStyle = STYLE_NONE;
            }
        }

        if ((topStyle == STYLE_NONE) && (bottomStyle == STYLE_NONE) && (leftStyle == STYLE_NONE) &&
                (rightStyle == STYLE_NONE) && (mask == FRAME_VOID)) { // XXX IS THIS RIGHT?

            return null;
        }

        // Section 10.3.3 indicates that a border-style of "none" should
        // use 0 as the border width. Is that correct?
        int topWidth;

        // Section 10.3.3 indicates that a border-style of "none" should
        // use 0 as the border width. Is that correct?
        int bottomWidth;

        // Section 10.3.3 indicates that a border-style of "none" should
        // use 0 as the border width. Is that correct?
        int leftWidth;

        // Section 10.3.3 indicates that a border-style of "none" should
        // use 0 as the border width. Is that correct?
        int rightWidth;

        if (topStyle == STYLE_NONE) {
            topWidth = 0;
        } else {
            topWidth = getBorderWidth(element, XhtmlCss.BORDER_TOP_WIDTH_INDEX, defaultWidth);
        }

        if (bottomStyle == STYLE_NONE) {
            bottomWidth = 0;
        } else {
            bottomWidth = getBorderWidth(element, XhtmlCss.BORDER_BOTTOM_WIDTH_INDEX, defaultWidth);
        }

        if (leftStyle == STYLE_NONE) {
            leftWidth = 0;
        } else {
            leftWidth = getBorderWidth(element, XhtmlCss.BORDER_LEFT_WIDTH_INDEX, defaultWidth);
        }

        if (rightStyle == STYLE_NONE) {
            rightWidth = 0;
        } else {
            rightWidth = getBorderWidth(element, XhtmlCss.BORDER_RIGHT_WIDTH_INDEX, defaultWidth);
        }

        // Border widths have to be positive - TODO - check and enforce this
        // If a border-style is set the width defaults to medium, not zero.
        if (topWidth == -1) {
            if (topStyle == STYLE_NONE) {
                topWidth = 0;
            } else {
                topWidth = WIDTH_MEDIUM;
            }
        }

        if (bottomWidth == -1) {
            if (bottomStyle == STYLE_NONE) {
                bottomWidth = 0;
            } else {
                bottomWidth = WIDTH_MEDIUM;
            }
        }

        if (rightWidth == -1) {
            if (rightStyle == STYLE_NONE) {
                rightWidth = 0;
            } else {
                rightWidth = WIDTH_MEDIUM;
            }
        }

        if (leftWidth == -1) {
            if (leftStyle == STYLE_NONE) {
                leftWidth = 0;
            } else {
                leftWidth = WIDTH_MEDIUM;
            }
        }

        // See if we already know we can bail
        if ((topWidth == 0) && (bottomWidth == 0) && (leftWidth == 0) && (rightWidth == 0)) {
            return null;
        }

        // Look up colors
        Color topColor = null;
        Color bottomColor = null;
        Color rightColor = null;
        Color leftColor = null;

        if (leftWidth != 0) { // no point looking for style when 0-sized
            leftColor = getColor(element, XhtmlCss.BORDER_LEFT_COLOR_INDEX);
        }

        if (rightWidth > 0) {
            rightColor = getColor(element, XhtmlCss.BORDER_RIGHT_COLOR_INDEX);
        }

        if (topWidth > 0) {
            topColor = getColor(element, XhtmlCss.BORDER_TOP_COLOR_INDEX);
        }

        if (bottomWidth > 0) {
            bottomColor = getColor(element, XhtmlCss.BORDER_BOTTOM_COLOR_INDEX);
        }

        if ((topWidth > 0) && (topStyle != STYLE_NONE) && (topColor == null)) {
            topColor = getDefaultColor(element);
        }

        if ((bottomWidth > 0) && (bottomStyle != STYLE_NONE) && (bottomColor == null)) {
            bottomColor = getDefaultColor(element);
        }

        if ((leftWidth > 0) && (leftStyle != STYLE_NONE) && (leftColor == null)) {
            leftColor = getDefaultColor(element);
        }

        if ((rightWidth > 0) && (rightStyle != STYLE_NONE) && (rightColor == null)) {
            rightColor = getDefaultColor(element);
        }

        if (topColor == TRANSPARENT) {
            topStyle = STYLE_NONE;
        }

        if (bottomColor == TRANSPARENT) {
            bottomStyle = STYLE_NONE;
        }

        if (leftColor == TRANSPARENT) {
            leftStyle = STYLE_NONE;
        }

        if (rightColor == TRANSPARENT) {
            rightStyle = STYLE_NONE;
        }

        // Check again in case we've set transparent colors to the point
        // that we don't need any borders
        if ((topStyle == STYLE_NONE) && (bottomStyle == STYLE_NONE) && (leftStyle == STYLE_NONE) &&
                (rightStyle == STYLE_NONE)) {
            return null;
        }

        // XXX #126609 NPE.
        if (topColor == null) {
            topColor = getDefaultColor(element);
        }
        if (bottomColor == null) {
            bottomColor = getDefaultColor(element);
        }
        if (leftColor == null) {
            leftColor = getDefaultColor(element);
        }
        if (rightColor == null) {
            rightColor = getDefaultColor(element);
        }
        
        return new CssBorder(topWidth, bottomWidth, rightWidth, leftWidth, topStyle, rightStyle,
            bottomStyle, leftStyle, topColor, bottomColor, rightColor, leftColor);
    }

    private static int getBorderWidth(Element element, int property, int defaultWidth) {
        // CSS will return the default width as a value - e.g. "medium", 
        // but in case the user has defined anything I want to use my own
        // default width not from the stylesheet (for example, I want to
        // apply the "border" attribute's value unless a specific css
        // property has been set)
        if (defaultWidth != -1) {
//            CSSEngine engine = CssLookup.getCssEngine(element);
//            if (engine.isDefaultValue((CSSStylableElement)element, null, property)) {
            if (CssProvider.getEngineService().isDefaultStyleValueForElement(element, null, property)
            ) {
                return defaultWidth;
            }
        }

//        int len = CssLookup.getLength(element, property);
        int len = CssUtilities.getCssLength(element, property);

        if (len < 0) {
            len = 0;
        }

        return len;
    }

    private static Color getColor(Element element, int propidx) {
//        return CssLookup.getColor(element, propidx);
        return CssProvider.getValueService().getColorForElement(element, propidx);
    }

    /** Return default color to use for a border span if one wasn't
     * specified by the stylesheet. As per the spec, this reverts
     * to using the color attribute on the element. */
    private static Color getDefaultColor(Element element) {
        // XXX cache this to avoid repeated lookups? Probably worthwhile
        // since color lookup is a bit expensive.
//        Color defaultColor = CssLookup.getColor(element, XhtmlCss.COLOR_INDEX);
        Color defaultColor = CssProvider.getValueService().getColorForElement(element, XhtmlCss.COLOR_INDEX);

        if (defaultColor == null) { // shouldn't happen
            defaultColor = Color.black;
        }

        return defaultColor;
    }

    private static int getBorderStyle(Element element, int property) {
//        CSSEngine engine = CssLookup.getCssEngine(element);
//        if (engine.isDefaultValue((CSSStylableElement)element, null, property)) {
        if (CssProvider.getEngineService().isDefaultStyleValueForElement(element, null, property)
        ) {
            return STYLE_UNKNOWN;
        }

//        Value val = CssLookup.getValue(element, property);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, property);
//        if (CssValueConstants.NONE_VALUE == val) {
        if (CssProvider.getValueService().isNoneValue(cssValue)) {
            return STYLE_NONE;

            // XXX This is not right! How do I solve this? I need
            // to know whether or not a border has not been set
            // locally on the element so I can apply a default style
            // in that case, but if the border-style has actually
            // been SET to "none", I should be returning STYLE_NONE!
            //return STYLE_UNKNOWN;
//        } else if (CssValueConstants.HIDDEN_VALUE == val) {
        } else if (CssProvider.getValueService().isHiddenValue(cssValue)) {
            return STYLE_NONE;
//        } else if (CssValueConstants.DOTTED_VALUE == val) {
        } else if (CssProvider.getValueService().isDottedValue(cssValue)) {
            return STYLE_DOTTED;
//        } else if (CssValueConstants.DASHED_VALUE == val) {
        } else if (CssProvider.getValueService().isDashedValue(cssValue)) {
            return STYLE_DASHED;
//        } else if (CssValueConstants.SOLID_VALUE == val) {
        } else if (CssProvider.getValueService().isSolidValue(cssValue)) {
            return STYLE_SOLID;
//        } else if (CssValueConstants.DOUBLE_VALUE == val) {
        } else if (CssProvider.getValueService().isDoubleValue(cssValue)) {
            return STYLE_DOUBLE;
//        } else if (CssValueConstants.GROOVE_VALUE == val) {
        } else if (CssProvider.getValueService().isGrooveValue(cssValue)) {
            return STYLE_GROOVE;
//        } else if (CssValueConstants.RIDGE_VALUE == val) {
        } else if (CssProvider.getValueService().isRidgeValue(cssValue)) {
            return STYLE_RIDGE;
//        } else if (CssValueConstants.INSET_VALUE == val) {
        } else if (CssProvider.getValueService().isInsetValue(cssValue)) {
            return STYLE_INSET;
//        } else if (CssValueConstants.OUTSET_VALUE == val) {
        } else if (CssProvider.getValueService().isOutsetValue(cssValue)) {
            return STYLE_OUTSET;
        }

        return STYLE_UNKNOWN;
    }

    /** Return a "designer" border - a border suitable for showing
     * a selection around a box, distinguishable from a typical
     * document box.  The border will therefore probably be in
     * some light color, with a dashed pattern. */
    public static CssBorder getDesignerBorder() {
        if (designerBorder == null) {
            Color color = Color.lightGray;
            designerBorder =
                new CssBorder(1, 1, 1, 1, 
                //STYLE_SOLID,STYLE_SOLID,STYLE_SOLID,STYLE_SOLID,
                STYLE_DASHED, STYLE_DASHED, STYLE_DASHED, STYLE_DASHED, color, color, color, color);
        }

        return designerBorder;
    }

    /**
     * Return a border of the given style, width and color.
     * Style should be one of STYLE_INSET, etc.
     *
     */
    static CssBorder getBorder(int style, int width, Color color) {
        return new CssBorder(width, width, width, width, style, style, style, style, color, color,
            color, color);
    }

    public static CssBorder getEmptyBorder() {
        if (emptyBorder == null) {
            emptyBorder =
                new CssBorder(0, 0, 0, 0, STYLE_NONE, STYLE_NONE, STYLE_NONE, STYLE_NONE,
                    Color.black, Color.black, Color.black, Color.black);
        }

        return emptyBorder;
    }

    private Stroke getStroke(int width, int style) {
        if (width == 0) {
            return null;
        }

        if ((prevWidth == width) && (prevStyle == style)) {
            return prevStroke;
        }

        Stroke stroke = null;

        switch (style) {
        case STYLE_UNKNOWN:
        case STYLE_NONE:
            break;

        case STYLE_DASHED:
            stroke =
                new BasicStroke((float)width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                    10.0f, new float[] { 6 * width, (6 * width) + width }, 0.0f);

            break;

        case STYLE_DOTTED:
            stroke =
                new BasicStroke((float)width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
                    10.0f, new float[] { width, 3 * width }, 0.0f);

            break;

        case STYLE_SOLID:
        case STYLE_DOUBLE:
        case STYLE_GROOVE:
        case STYLE_RIDGE:
        case STYLE_INSET:
        case STYLE_OUTSET:

            // Special handling in the painter
            break;
        }

        prevStroke = stroke; // cache for next time
        prevStyle = style;
        prevWidth = width;

        return stroke;
    }

    /**
     * Paints the border for the specified component with the specified
     * position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D)g;

        Color oldColor = g.getColor();
        Stroke oldStroke = g2d.getStroke();

        if ((topStroke == bottomStroke) && (bottomStroke == rightStroke) &&
                (rightStroke == leftStroke) &&
                ((topStroke == null) ||
                ((topColor == bottomColor) && (bottomColor == rightColor) &&
                (rightColor == leftColor)))) {
            if (topStroke != null) {
                g.setColor(topColor);
                g2d.setStroke(topStroke);

                if (topWidth == 1) {
                    //g2d.drawRect(x, y, width-2, height-2);
                    g2d.drawRect(x, y, width - 1, height - 1);
                } else {
                    int x1 = x + (leftWidth / 2);

                    if ((leftWidth & 1) == 1) {
                        x1--;
                    }

                    int x2 = (x + width) - 1 - (rightWidth / 2);
                    int y1 = y + (topWidth / 2);

                    if ((topWidth & 1) == 1) {
                        y1--;
                    }

                    int y2 = (y + height) - 1 - (bottomWidth / 2);
                    g2d.drawRect(x1, y1, x2 - x1, y2 - y1);
                }
            } else {
                // solid/inset/outset/ridge/groove/double
                g2d.setStroke(oldStroke);

                if ((topAltColor != null) && (topWidth > 1)) {
                    g.setColor(topColor);
                    paintTop(g2d, x, y, width, topWidth / 2, leftWidth / 2, rightWidth / 2);
                    g.setColor(topAltColor);
                    paintTop(g2d, x + (leftWidth / 2), y + (topWidth / 2),
                        width - (leftWidth / 2) - (rightWidth / 2),
                        (topWidth / 2) + (topWidth % 2), (leftWidth / 2) + (leftWidth % 2),
                        (rightWidth / 2) + (rightWidth % 2));
                } else {
                    g.setColor(topColor);
                    paintTop(g2d, x, y, width, topWidth, leftWidth, rightWidth);
                }

                if ((bottomAltColor != null) && (bottomWidth > 1)) {
                    g.setColor(bottomColor);
                    paintBottom(g2d, x, y + height, width, bottomWidth / 2, leftWidth / 2,
                        rightWidth / 2);
                    g.setColor(bottomAltColor);
                    paintBottom(g2d, x + (leftWidth / 2), (y + height) - (bottomWidth / 2),
                        width - (leftWidth / 2) - (rightWidth / 2),
                        (bottomWidth / 2) + (bottomWidth % 2), (leftWidth / 2) + (leftWidth % 2),
                        (rightWidth / 2) + (rightWidth % 2));
                } else {
                    g.setColor(bottomColor);
                    paintBottom(g2d, x, y + height, width, bottomWidth, leftWidth, rightWidth);
                }

                g.setColor(leftColor);

                if ((leftAltColor != null) && (leftWidth > 1)) {
                    g.setColor(leftColor);
                    paintLeft(g2d, x, y, leftWidth / 2, height, topWidth / 2, bottomWidth / 2);
                    g.setColor(leftAltColor);
                    paintLeft(g2d, x + (leftWidth / 2) + (leftWidth % 2),
                        y + (topWidth / 2) + (topWidth % 2), (leftWidth / 2) + (leftWidth % 2),
                        height - (topWidth / 2) - (bottomWidth / 2),
                        (topWidth / 2) + (topWidth % 2), (bottomWidth / 2) + (bottomWidth % 2));
                } else {
                    g.setColor(leftColor);
                    paintLeft(g2d, x, y, leftWidth, height, topWidth, bottomWidth);
                }

                if ((rightAltColor != null) && (rightWidth > 1)) {
                    g.setColor(rightColor);
                    paintRight(g2d, x + width, y, rightWidth / 2, height, topWidth / 2,
                        bottomWidth / 2);
                    g.setColor(rightAltColor);
                    paintRight(g2d, (x + width) - (rightWidth / 2) + (rightWidth % 2),
                        y + (topWidth / 2) + (topWidth % 2), (rightWidth / 2) + (rightWidth % 2),
                        height - (topWidth / 2) - (bottomWidth / 2),
                        (topWidth / 2) + (topWidth % 2), (bottomWidth / 2) + (bottomWidth % 2));
                } else {
                    g.setColor(rightColor);
                    paintRight(g2d, x + width, y, rightWidth, height, topWidth, bottomWidth);
                }
            }
        } else {
            if (topStroke != null) {
                g.setColor(topColor);
                g2d.setStroke(topStroke);

                int tx1 = x - leftWidth + (topWidth / 2);

                if ((topWidth & 1) == 1) {
                    tx1--;
                }

                int ty = y + (topWidth / 2);

                if ((topWidth & 1) == 1) {
                    ty--;
                }

                int tx2 = (x + width) - rightWidth - (topWidth / 2);
                g2d.drawLine(tx1, ty, tx2, ty);
            } else {
                // inset/outset/ridge/groove
                if (topAltColor != null) {
                    // ridge or groove
                    g2d.setStroke(oldStroke);
                    g.setColor(topColor);
                    paintTop(g2d, x, y, width, topWidth / 2, leftWidth / 2, rightWidth / 2);
                    g.setColor(topAltColor);
                    paintTop(g2d, x + (leftWidth / 2), y + (topWidth / 2),
                        width - (leftWidth / 2) - (rightWidth / 2),
                        (topWidth / 2) + (topWidth % 2), (leftWidth / 2) + (leftWidth % 2),
                        (rightWidth / 2) + (rightWidth % 2));
                } else {
                    g2d.setStroke(oldStroke);
                    g.setColor(topColor);
                    paintTop(g2d, x, y, width, topWidth, leftWidth, rightWidth);
                }
            }

            if (bottomStroke != null) {
                g.setColor(bottomColor);
                g2d.setStroke(bottomStroke);

                int tx1 = x - leftWidth + (bottomWidth / 2);

                if ((bottomWidth & 1) == 1) {
                    tx1--;
                }

                int ty = (y + height) - (bottomWidth / 2);
                int tx2 = (x + width) - rightWidth - (bottomWidth / 2);
                g2d.drawLine(tx1, ty, tx2, ty);
            } else {
                // inset/outset/ridge/groove
                g.setColor(bottomColor);
                g2d.setStroke(oldStroke);
                paintBottom(g2d, x, y + height, width, bottomWidth, leftWidth, rightWidth);
            }

            if (leftStroke != null) {
                g.setColor(leftColor);
                g2d.setStroke(leftStroke);

                int tx = x - (leftWidth / 2);
                int ty1 = y - topWidth + (leftWidth / 2);
                int ty2 = (y + height + bottomWidth) - (leftWidth / 2);

                if ((leftWidth & 1) == 1) {
                    tx--;
                }

                if ((leftWidth & 1) == 1) {
                    ty1--;
                }

                g2d.drawLine(tx, ty1, tx, ty2);
            } else {
                // inset/outset/ridge/groove
                g.setColor(leftColor);
                g2d.setStroke(oldStroke);
                paintLeft(g2d, x, y, leftWidth, height, topWidth, bottomWidth);
            }

            if (rightStroke != null) {
                g.setColor(rightColor);
                g2d.setStroke(rightStroke);

                int tx = (x + width) - (rightWidth / 2);
                int ty1 = y - topWidth + (rightWidth / 2);

                if ((rightWidth & 1) == 0) {
                    ty1--;
                }

                int ty2 = (y + height + bottomWidth) - (rightWidth / 2);
                g2d.drawLine(tx, ty1, tx, ty2);
            } else {
                // inset/outset/ridge/groove
                g.setColor(rightColor);
                g2d.setStroke(oldStroke);
                paintRight(g2d, x + width, y, rightWidth, height, topWidth, bottomWidth);
            }
        }

        g.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    /**
       Paint this:
       <pre>
     (x,y)       w
       ---------------------
        \                 /
         \               /    h
          ---------------
        (x2,y2)       (x3,y2)
       </pre>
       x,y points to the top left corner. h is the height.
       left is x2-x.  Right is w-x3.
     */
    private void paintTop(Graphics2D g2d, int x, int y, int w, int h, int left, int right) {
        if (h == 1) {
            g2d.drawLine(x, y, (x + w) - 1, y);
        } else {
            pointsX[0] = x;
            pointsY[0] = y;
            pointsX[1] = x + w;
            pointsY[1] = y;
            pointsX[2] = (x + w) - right;
            pointsY[2] = y + h;
            pointsX[3] = x + left;
            pointsY[3] = pointsY[2];
            g2d.fillPolygon(pointsX, pointsY, 4); // not including endpoint!
        }
    }

    /**
       Paint this:
       <pre>
        (x2,y2)       (x3,y2)
          ---------------
         /               \    h
        /                 \
       ---------------------
     (x,y)        w
       </pre>
       x,y points to the bottom left corner. h is the height.
       left is x2-x.  Right is x+w-x3.
     */
    private void paintBottom(Graphics2D g2d, int x, int y, int w, int h, int left, int right) {
        if (bottomWidth == 1) {
            g2d.drawLine(x, y - 1, (x + w) - 1, y - 1);
        } else {
            pointsX[0] = x;
            pointsY[0] = y;
            pointsX[1] = x + w;
            pointsY[1] = y;
            pointsX[2] = (x + w) - right;
            pointsY[2] = y - h;
            pointsX[3] = x + left;
            pointsY[3] = pointsY[2];
            g2d.fillPolygon(pointsX, pointsY, 4); // not including endpoint!
        }
    }

    /**
       Paint this:
       <pre>
    (x,y) |\
         | \
         |  | (x2, y2)
       h |  |
         |  |
         |  | (x2, y3)
         | /
         |/
           w
       </pre>
       x,y points to the top left corner. h is the height, w is the width.
       top is y2-y.  Bottom is y+h-y3.
     */
    private void paintLeft(Graphics2D g2d, int x, int y, int w, int h, int top, int bottom) {
        if (leftWidth == 1) {
            g2d.drawLine(x, y, x, (y + h) - 1);
        } else {
            pointsX[0] = x;
            pointsY[0] = y;
            pointsX[1] = x;
            pointsY[1] = y + h;
            pointsX[2] = x + w;
            pointsY[2] = (y + h) - bottom;
            pointsX[3] = pointsX[2];
            pointsY[3] = y + top;
            g2d.fillPolygon(pointsX, pointsY, 4); // not including endpoint!
        }
    }

    /**
       Paint this:
       <pre>
           /| (x,y)
          / |
    (x2,y2) |  |
         |  | h
         |  |
    (x2,y3) |  |
          \ |
           \|
          w
       </pre>
       x,y points to the top right corner. h is the height, w is the width.
       top is y2-y.  Bottom is y+h-y3.
     */
    private void paintRight(Graphics2D g2d, int x, int y, int w, int h, int top, int bottom) {
        if (rightWidth == 1) {
            g2d.drawLine(x - 1, y, x - 1, (y + h) - 1);
        } else {
            pointsX[0] = x;
            pointsY[0] = y;
            pointsX[1] = x;
            pointsY[1] = y + h;
            pointsX[2] = x - w;
            pointsY[2] = (y + h) - bottom;
            pointsX[3] = pointsX[2];
            pointsY[3] = y + top;
            g2d.fillPolygon(pointsX, pointsY, 4); // not including endpoint!
        }
    }

    /**
     * Returns the insets of the border.
     * @param c the component for which this border insets value applies
     */
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, 1, 1);
    }

    /**
     * Returns whether or not the border is opaque.  If the border
     * is opaque, it is responsible for filling in it's own
     * background when painting.
     */
    public boolean isBorderOpaque() {
        return false;
    }

    public int getLeftBorderWidth() {
        return leftWidth;
    }

    public int getRightBorderWidth() {
        return rightWidth;
    }

    public int getTopBorderWidth() {
        return topWidth;
    }

    public int getBottomBorderWidth() {
        return bottomWidth;
    }

    public String toString() {
        return super.toString() + "[" + leftWidth + "," + rightWidth + "," + topWidth + "," + bottomWidth + // NOI18N
        "," + leftColor + "]"; // NOI18N
    }
}
