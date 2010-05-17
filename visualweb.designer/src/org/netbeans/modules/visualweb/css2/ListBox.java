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
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;


/**
 * ListBox represents a box that contains a numbered or lettered or
 * un-numbered list. Most of the list-painting code was derived from
 * Swing's StyleSheet.ListPainter class, with the following modifications:
 *  <ul>
 *   <li> Changed over to our own DOM/CSS lookup
 *   <li> Changed circle and square code to use size 5 instead of 8 to
 *        match what Mozilla &amp; Safari are doing
 *   <li> Support lower-latin and upper-latin (same as lower-alpha and
 *        upper alpha)
 *   <li> Alignment computation changed to use our own layout based
 *        linebox alignment
 *   </ul>
 * @todo Support list-style-type: decimal-leading-zero
 * @todo Support list-style-position
 * @todo Support list-style
 * @todo I don't seem to handle empty list items well (&lt;li&gt;&lt;/li&gt;);
 *   they don't currently take up a whole font-height line
 *
 * @author Tor Norbye
 */
public class ListBox extends ContainerBox {
    private static final int SHAPE_SIZE = 5; // Swing had 8, but

    /* list of roman numerals */
    private static final char[][] romanChars =
        {
            { 'i', 'v' },
            { 'x', 'l' },
            { 'c', 'd' },
            { 'm', '?' },
        };
    private boolean checkedForStart;
    private int start;
//    private Value type;
    private CssValue cssType;
    Icon img = null;
    private int bulletgap = 5;

    /**
     * Create a ListBox for the given string
     *
     * @param webform The <code>WebForm</code>
     * @param element The element this string box is associated with
     * @param boxType Type of box.
     * @param string The string to manage
     * @param width The width to use for the box
     * @param height The height to use for the box
     */
    public ListBox(WebForm webform, Element element, BoxType boxType, boolean inline, boolean replaced) {
        super(webform, element, boxType, inline, replaced);

        // Get the image to use as a list bullet
        img = getListStyleImage(webform, element);

        // Get the type of bullet to use in the list
        if (img == null) {
//            type = CssLookup.getValue(element, XhtmlCss.LIST_STYLE_TYPE_INDEX);
            cssType = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.LIST_STYLE_TYPE_INDEX);
        }

        start = 1;
    }

    public void paint(Graphics g, int px, int py) {
        super.paint(g, px, py);

        px += getX();
        py += getY();

        // Box model quirk: my coordinate system is based on the visual
        // extents of the boxes - e.g. location and size of the border
        // edge.  Because of this, when visually traversing the hierarchy,
        // I need to add in the margins.
        px += leftMargin;
        py += effectiveTopMargin;

        int n = getBoxCount();

        for (int i = 0; i < n; i++) {
            CssBox child = getBox(i);

            if (!child.hidden) {
                paintBullet(g, (float)(px + child.getX() + child.leftMargin),
                    (float)(py + child.getY()), // margins?
                    (float)child.getWidth(), (float)child.getHeight(), this, i);
            }
        }
    }

//    public String toString() {
//        return "ListBox[" + paramString() + "]";
//    }

    // both Mozilla and Safari seems to use roughly 5 pixels
    private static ImageIcon getListStyleImage(WebForm webform, Element element) {
//        Value value = CssLookup.getValue(element, XhtmlCss.LIST_STYLE_IMAGE_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.LIST_STYLE_IMAGE_INDEX);

//        if (value == CssValueConstants.NONE_VALUE) {
        if (CssProvider.getValueService().isNoneValue(cssValue)) {
            return null;
        }

//        String urlString = value.getStringValue();
        String urlString = cssValue.getStringValue();

        // XXX This is wrong. I should get the -stylesheet- URL.
        // And what about linked style sheets?
//        URL reference = webform.getMarkup().getBase();
        URL reference = webform.getBaseUrl();
        URL url = null;

        try {
            url = new URL(reference, urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return null;
        }

        ImageIcon image = new ImageIcon(url);

        return image;
    }

    /**
     * Returns a string that represents the value
     * of the HTML.Attribute.TYPE attribute.
     * If this attributes is not defined, then
     * then the type defaults to "disc" unless
     * the tag is on Ordered list.  In the case
     * of the latter, the default type is "decimal".
     */
    private CssValue getChildType(CssBox childBox) {
        Element child = childBox.getElement();
//        Value childtype = CssLookup.getValue(child, XhtmlCss.LIST_STYLE_TYPE_INDEX);
        CssValue cssChildType = CssProvider.getEngineService().getComputedValueForElement(child, XhtmlCss.LIST_STYLE_TYPE_INDEX);

//        if (childtype == null) {
//            if (type == null) {
        if (cssChildType == null) {
            if (cssType == null) {
                // Parent view.
                CssBox parent = childBox.getParent();

                if (parent.tag == HtmlTag.OL) {
//                    childtype = CssValueConstants.DECIMAL_VALUE;
                    cssChildType = CssProvider.getValueService().getDecimalCssValueConstant();
                } else {
//                    childtype = CssValueConstants.DISC_VALUE;
                    cssChildType = CssProvider.getValueService().getDiscCssValueConstant();
                }
            } else {
//                childtype = type;
                cssChildType = cssType;
            }
        }

//        return childtype;
        return cssChildType;
    }

    /**
     * Obtains the starting index from <code>parent</code>.
     */
    private void getStart(CssBox parent) {
        checkedForStart = true;

        Element element = parent.getElement();

        if (element != null) {
            String startValue = element.getAttribute(HtmlAttribute.START);

            if ((startValue != null) && (startValue.length() > 0)) {
                try {
                    start = Integer.parseInt(startValue);
                } catch (NumberFormatException nfe) {
                    // User has entered a bogus start attribute in the markup
                    // so ignore it.
                    start = 1;
                }
            }
        }
    }

    /**
     * Returns an integer that should be used to render the child at
     * <code>childIndex</code> with. The retValue will usually be
     * <code>childIndex</code> + 1, unless <code>parentBox</code>
     * has some child boxes that do not represent LI's, or one of the views
     * has a HtmlAttribute.START specified.
     */
    private int getRenderIndex(CssBox parentBox, int childIndex) {
        if (!checkedForStart) {
            getStart(parentBox);
        }

        int retIndex = childIndex;

        for (int counter = childIndex; counter >= 0; counter--) {
            CssBox cs = parentBox.getBox(counter);

            if (cs.tag != HtmlTag.LI) {
                retIndex--;
            } else {
                String value = cs.getElement().getAttribute(HtmlAttribute.VALUE);

                if ((value != null) && (value.length() > 0)) {
                    try {
                        int iValue = Integer.parseInt((String)value);

                        return retIndex - counter + iValue;
                    } catch (NumberFormatException nfe) {
                        // The user has entered a bogus value, so ignore it
                        continue;
                    }
                }
            }
        }

        return retIndex + start;
    }

    /**
     * Paints the CSS list decoration according to the
     * attributes given.
     *
     * @param g the rendering surface.
     * @param x the x coordinate of the list item allocation
     * @param y the y coordinate of the list item allocation
     * @param w the width of the list item allocation
     * @param h the height of the list item allocation
     * @param s the allocated area to paint into.
     * @param item which list item is being painted.  This
     *  is a number greater than or equal to 0.
     */
    public void paintBullet(Graphics g, float x, float y, float w, float h, CssBox parentBox,
        int item) {
        CssBox cv = parentBox.getBox(item);

        // Only draw something if the View is a list item. This won't
        // be the case for comments.
        if (cv.tag != HtmlTag.LI) { // XXX I should use CSS "display: list-item" instead for this!

            return;
        }

        // How the list indicator is aligned is not specified, it is
        // left up to the UA. IE and NS differ on this behavior.
        // This is closer to NS where we align to the first line of text.
        // If the child is not text we draw the indicator at the
        // origin (0).
        float align = 0;
        LineBox lineBox = findFirstLineBox(cv);

        if (lineBox != null) {
            h = lineBox.getHeight();
            y = lineBox.getAbsoluteY();
            align = 0.5f;
        }

        if (img != null) {
            drawIcon(g, (int)x, (int)y, (int)h, align, webform.getPane());

            return;
        }

//        Value childtype = getChildType(cv);
        CssValue cssChildType = getChildType(cv);

        /* TODO - is this necessary? I believe I'll still have the
           List item font in the graphics context since list painter
           is called right after the item itself is painted.
        Font font = ((StyledDocument)cv.getDocument()).
            getFont(cv.getElement());
        if (font != null) {
            g.setFont(font);
        }
        */
//        if ((childtype == CssValueConstants.SQUARE_VALUE) ||
//                (childtype == CssValueConstants.CIRCLE_VALUE) ||
//                (childtype == CssValueConstants.DISC_VALUE)) {
        if (CssProvider.getValueService().isSquareValue(cssChildType)
        || CssProvider.getValueService().isCircleValue(cssChildType)
        || CssProvider.getValueService().isDiscValue(cssChildType)) {
//            drawShape(g, childtype, (int)x, (int)y, (int)h, align);
            drawShape(g, cssChildType, (int)x, (int)y, (int)h, align);
            // XXX Needless, handled by above
//        } else if (childtype == CssValueConstants.CIRCLE_VALUE) {
//            drawShape(g, childtype, (int)x, (int)y, (int)h, align);
//        } else if (childtype == CssValueConstants.DECIMAL_VALUE) {
        } else if (CssProvider.getValueService().isDecimalValue(cssChildType)) {
            drawLetter(g, '1', (int)x, (int)y, (int)h, align, getRenderIndex(parentBox, item));
//        } else if ((childtype == CssValueConstants.LOWER_LATIN_VALUE) ||
//                (childtype == CssValueConstants.LOWER_ALPHA_VALUE)) {
        } else if (CssProvider.getValueService().isLowerLatinValue(cssChildType)
        || CssProvider.getValueService().isLowerAlphaValue(cssChildType)) {
            drawLetter(g, 'a', (int)x, (int)y, (int)h, align, getRenderIndex(parentBox, item));
//        } else if ((childtype == CssValueConstants.UPPER_LATIN_VALUE) ||
//                (childtype == CssValueConstants.UPPER_ALPHA_VALUE)) {
        } else if (CssProvider.getValueService().isUpperLatinValue(cssChildType)
        || CssProvider.getValueService().isUpperAlphaValue(cssChildType)) {
            drawLetter(g, 'A', (int)x, (int)y, (int)h, align, getRenderIndex(parentBox, item));
//        } else if (childtype == CssValueConstants.LOWER_ROMAN_VALUE) {
        } else if (CssProvider.getValueService().isLowerRomanValue(cssChildType)) {
            drawLetter(g, 'i', (int)x, (int)y, (int)h, align, getRenderIndex(parentBox, item));
//        } else if (childtype == CssValueConstants.UPPER_ROMAN_VALUE) {
        } else if (CssProvider.getValueService().isUpperRomanValue(cssChildType)) {
            drawLetter(g, 'I', (int)x, (int)y, (int)h, align, getRenderIndex(parentBox, item));
        }
    }

    /** Find first linebox within the given box IF the linebox is at
     * the top! In other words, it only returns a LineBox if it's the
     * first normal flow child (but of course it can be the first child
     * of a first child of a first child ...) */
    private LineBox findFirstLineBox(CssBox box) {
        if (box instanceof LineBox) {
            return (LineBox)box;
        }

        CssBox first = box.getFirstNormalBox();

        if (first == null) {
            return null;
        }

        return findFirstLineBox(first);
    }

    /**
     * Draws the bullet icon specified by the list-style-image argument.
     *
     * @param g     the graphics context
     * @param ax    x coordinate to place the bullet
     * @param ay    y coordinate to place the bullet
     * @param ah    height of the container the bullet is placed in
     * @param align preferred alignment factor for the child view
     */
    void drawIcon(Graphics g, int ax, int ay, int ah, float align, Component c) {
        // Align to bottom of icon.
        g.setColor(Color.black);

        int x = ax - img.getIconWidth() - bulletgap;
        int y = Math.max(ay, (ay + (int)(align * ah)) - img.getIconHeight());

        img.paintIcon(c, g, x, y);
    }

    /**
     * Draws the graphical bullet item specified by the type argument.
     *
     * @param g     the graphics context
     * @param type  type of bullet to draw (circle, square, disc)
     * @param ax    x coordinate to place the bullet
     * @param ay    y coordinate to place the bullet
     * @param ah    height of the container the bullet is placed in
     * @param align preferred alignment factor for the child view
     */
    void drawShape(Graphics g, CssValue cssType, int ax, int ay, int ah, float align) {
        // Align to bottom of shape.
//        Color fg = CssLookup.getColor(getElement(), XhtmlCss.COLOR_INDEX);
        Color fg = CssProvider.getValueService().getColorForElement(getElement(), XhtmlCss.COLOR_INDEX);

        if (fg == null) {
            fg = Color.black;
        }

        g.setColor(fg);

        int x = ax - bulletgap - SHAPE_SIZE;
        int y = Math.max(ay, (ay + (int)(align * ah)) - (SHAPE_SIZE / 2));

//        if (type == CssValueConstants.SQUARE_VALUE) {
        if (CssProvider.getValueService().isSquareValue(cssType)) {
            //g.drawRect(x, y, SHAPE_SIZE, SHAPE_SIZE);
            // Mozilla and Safari use solid squares
            g.fillRect(x, y, SHAPE_SIZE, SHAPE_SIZE);
//        } else if (type == CssValueConstants.CIRCLE_VALUE) {
        } else if (CssProvider.getValueService().isCircleValue(cssType)) {
            g.drawOval(x, y, SHAPE_SIZE, SHAPE_SIZE);
        } else {
            g.fillOval(x, y, SHAPE_SIZE, SHAPE_SIZE);
        }
    }

    /**
     * Draws the letter or number for an ordered list.
     *
     * @param g     the graphics context
     * @param letter type of ordered list to draw
     * @param ax    x coordinate to place the bullet
     * @param ay    y coordinate to place the bullet
     * @param ah    height of the container the bullet is placed in
     * @param index position of the list item in the list
     */
    void drawLetter(Graphics g, char letter, int ax, int ay, int ah, float align, int index) {
//        Color fg = CssLookup.getColor(getElement(), XhtmlCss.COLOR_INDEX);
        Color fg = CssProvider.getValueService().getColorForElement(getElement(), XhtmlCss.COLOR_INDEX);

        if (fg == null) {
            fg = Color.black;
        }

        g.setColor(fg);

        String str = formatItemNum(index, letter) + ".";
        FontMetrics fm = g.getFontMetrics();
        int stringwidth = fm.stringWidth(str);
        int x = ax - stringwidth - bulletgap;
        int y = Math.max(ay + fm.getAscent(), ay + (int)(ah * align));
        g.drawString(str, x, y);
    }

    /**
     * Converts the item number into the ordered list number
     * (i.e.  1 2 3, i ii iii, a b c, etc.
     *
     * @param itemNum number to format
     * @param type    type of ordered list
     */
    String formatItemNum(int itemNum, char type) {
        //String numStyle = "1";

        boolean uppercase = false;

        String formattedNum;

        switch (type) {
        case '1':default:
            formattedNum = String.valueOf(itemNum);

            break;

        case 'A':
            uppercase = true;

        // fall through
        case 'a':
            formattedNum = formatAlphaNumerals(itemNum);

            break;

        case 'I':
            uppercase = true;

        // fall through
        case 'i':
            formattedNum = formatRomanNumerals(itemNum);
        }

        if (uppercase) {
            formattedNum = formattedNum.toUpperCase();
        }

        return formattedNum;
    }

    /**
     * Converts the item number into an alphabetic character
     *
     * @param itemNum number to format
     */
    String formatAlphaNumerals(int itemNum) {
        String result = "";

        if (itemNum > 26) {
            result = formatAlphaNumerals(itemNum / 26) + formatAlphaNumerals(itemNum % 26);
        } else {
            // -1 because item is 1 based.
            result = String.valueOf((char)(('a' + itemNum) - 1));
        }

        return result;
    }

    /**
     * Converts the item number into a roman numeral
     *
     * @param num  number to format
     */
    private static String formatRomanNumerals(int num) {
        return formatRomanNumerals(0, num);
    }

    /**
     * Converts the item number into a roman numeral
     *
     * @param num  number to format
     */
    private static String formatRomanNumerals(int level, int num) {
        if (num < 10) {
            return formatRomanDigit(level, num);
        } else {
            return formatRomanNumerals(level + 1, num / 10) + formatRomanDigit(level, num % 10);
        }
    }

    /**
     * Converts the item number into a roman numeral
     *
     * @param level position
     * @param num   digit to format
     */
    private static String formatRomanDigit(int level, int digit) {
        // TODO When on 1.5, use StringBuilder.
        StringBuffer result = new StringBuffer();

        if (digit == 9) {
            result.append(romanChars[level][0]);
            result.append(romanChars[level + 1][0]);

            return result.toString();
        } else if (digit == 4) {
            result.append(romanChars[level][0]);
            result.append(romanChars[level][1]);

            return result.toString();
        } else if (digit >= 5) {
            result.append(romanChars[level][1]);
            digit -= 5;
        }

        for (int i = 0; i < digit; i++) {
            result.append(romanChars[level][0]);
        }

        return result.toString();
    }
}
