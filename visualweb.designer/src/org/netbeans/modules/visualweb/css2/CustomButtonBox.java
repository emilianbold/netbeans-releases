/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.css2;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.UIManager;

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;


/**
 * <p>
 * CustomButtonBox represents the painting of a button that is not using
 * the native platform look and feel for buttons. You typically get these
 * buttons if your button specifies the button appearance for example using
 * background-image.
 * </p>
 * @todo Figure out why the button width does not seem to obey my width settings.
 *   Am I adding in the padding etc.
 * @todo Make sure that buttons which DON'T set a border but do set a
 *   background-image behave the same way as in the browser
 * @todo Paint watermark?
 * @todo I should make sure the text is clipped inside the borders!
 * @todo Try to make rounded borders!
 * @todo The button border on the ACME theme is cut off
 *
 * @author Tor Norbye
 */
public class CustomButtonBox extends CssBox {
    private String string;
    private Color fg;
    private char[] stringChars;
    private FontMetrics fm;
    private int textWidth;
    private int prefWidth;
    private int prefHeight;
    private int baseline;

    /**
     * Create a StringBox for the given string
     *
     * @param webform The <code>WebForm</code>
     * @param element The element this string box is associated with
     * @param boxType Type of box.
     * @param string The string to manage
     * @param width The width to use for the box
     * @param height The height to use for the box
     */
    public CustomButtonBox(WebForm webform, Element element, BoxType boxType, String string,
        CssBorder border) {
        this(webform, element, boxType, string, border, true);
    }
    /**
     * Create a StringBox for the given string
     *
     * @param webform The <code>WebForm</code>
     * @param element The element this string box is associated with
     * @param boxType Type of box.
     * @param string The string to manage
     * @param width The width to use for the box
     * @param height The height to use for the box
     */
    public CustomButtonBox(WebForm webform, Element element, BoxType boxType, String string,
        CssBorder border, boolean inline) {
        super(webform, element, boxType, inline, true);
        this.string = string;
        this.stringChars = string.toCharArray();
        this.border = border;

        // See if we have SPECIFIC font or color settings to apply, otherwise
        // stick with the defaults....
//        XhtmlCssEngine engine = CssLookup.getCssEngine(element);
        Font font;

        // TODO check font-family, font-variant etc. too
//        if (engine.isInheritedValue((RaveElement)element, XhtmlCss.FONT_SIZE_INDEX)) {
        if (CssProvider.getEngineService().isInheritedStyleValueForElement(element, XhtmlCss.FONT_SIZE_INDEX)) {
            font = UIManager.getFont("Button.font"); // NOI18N
        } else {
//            font = CssLookup.getFont(element, DesignerSettings.getInstance().getDefaultFontSize());
//            font = CssProvider.getValueService().getFontForElement(element, DesignerSettings.getInstance().getDefaultFontSize(), Font.PLAIN);
            font = CssUtilities.getDesignerFontForElement(element, string);
        }

//        fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
        fm = DesignerUtils.getFontMetrics(font);

        textWidth = DesignerUtils.getNonTabbedTextWidth(stringChars, 0, stringChars.length, fm);
        prefWidth = textWidth;
        prefHeight = fm.getHeight();
        this.width = prefWidth;
        this.height = prefHeight;
        this.contentWidth = this.width;
        this.contentHeight = this.height;

//        if (engine.isInheritedValue((RaveElement)element, XhtmlCss.COLOR_INDEX)) {
        if (CssProvider.getEngineService().isInheritedStyleValueForElement(element, XhtmlCss.COLOR_INDEX)) {
            // XXX Should we default to something else?
            fg = Color.black;
        } else {
//            fg = CssLookup.getColor(element, XhtmlCss.COLOR_INDEX);
            fg = CssProvider.getValueService().getColorForElement(element, XhtmlCss.COLOR_INDEX);

            if (fg == null) {
                if (fg == null) {
                    fg = Color.black;
                }
            }
        }
    }

    protected void initializeBorder() {
        if (border == null) {
            super.initializeBorder();
        }

        if (border == null) {
            border = CssBorder.getDesignerBorder();
        }

        leftBorderWidth = border.getLeftBorderWidth();
        topBorderWidth = border.getTopBorderWidth();
        bottomBorderWidth = border.getBottomBorderWidth();
        rightBorderWidth = border.getRightBorderWidth();

        baseline = fm.getHeight() - fm.getDescent() + topBorderWidth + topPadding;

        //baseline += (height-baseline)/2;
    }

    public int getIntrinsicWidth() {
        return prefWidth;
    }

    public int getIntrinsicHeight() {
        return prefHeight;
    }

    // Unlike normal replaced boxes, we don't want the buttons to
    // scale if only one dimension is set.
    protected void updateAutoContentSize() {
        if (contentWidth == AUTO) {
            contentWidth = getIntrinsicWidth();
        }

        if (contentHeight == AUTO) {
            contentHeight = getIntrinsicHeight();
        }
    }

    public int getBaseline() {
        return baseline;
    }

//    public String toString() {
//        return "CustomButtonBox[" + paramString() + "]";
//    }

    protected String paramString() {
        return super.paramString() + ", " + string;
    }

    protected void paintBackground(Graphics g, int px, int py) {
        // Do nothing. We paint by calling super directly. This
        // is because paintBackground() on inline boxes is called by
        // the paragraph formatter - it paints the backgrounds, then
        // the text selection highlight, then the foregrounds. However,
        // for these buttons we don't want the selection highlight to
        // show up between the background and foreground like we do
        // for text, so make this call do nothing.
    }

    public void paint(Graphics g, int px, int py) {
        if (hidden) {
            return;
        }

        px += getX();
        py += getY();

        px += leftMargin;
        py += effectiveTopMargin;

        super.paintBackground(g, px, py);
        
        // XXX decoration
        paintDecoration(g, px + getWidth(), py);

        int x = (width - textWidth) / 2;
        int y = (height - fm.getHeight()) / 2;

        if (x < 0) {
            x = 0;
        }

        if (y < 0) {
            y = 0;
        }

        x += px;
        y += py;

        g.setColor(fg);
        g.setFont(fm.getFont());

        // determine the y coordinate to render the glyphs
        int yadj = (y + fm.getHeight()) - fm.getDescent();

        // Clip contents in case text is too large
        Rectangle sharedClipRect = ContainerBox.sharedClipRect;
        g.getClipBounds(sharedClipRect);

        int cx = sharedClipRect.x;
        int cy = sharedClipRect.y;
        int cw = sharedClipRect.width;
        int ch = sharedClipRect.height;
        g.clipRect(px, py, width, height);

        // Draw text!
        g.drawChars(stringChars, 0, stringChars.length, x, yadj);

        //        if (width > WATERMARK_SIZE && height > WATERMARK_SIZE) { // don't draw watermarks for tiny images
        //           paintFacesWatermark(g, px, py);
        //        }
        // Restore clip
        g.setClip(cx, cy, cw, ch);
    }

    public boolean isBorderSizeIncluded() {
        return true;
    }

    public Insets getCssSizeInsets() {
        return new Insets(0, 0, 0, 0);
    }
}
