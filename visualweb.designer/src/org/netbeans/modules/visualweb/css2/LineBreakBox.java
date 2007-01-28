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
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.visualweb.designer.WebForm;
import com.sun.rave.designer.html.HtmlTag;


/**
 * A LineBreakBox represents a &lt;br/&gt; in a LineBox.
 * <p>
 * @todo I'm inheriting a lot of crap from CssBox -- margins, padding,
 *  etc. Is there a way I could make an even more primitive box than
 *  CssBox for this?

 * @author Tor Norbye
 */
public class LineBreakBox extends CssBox {
    private FontMetrics metrics;

    /*
        public boolean isPlaceHolder() {
            return true;
        }
    */

    /**
     *  Create a LineBreakBox representing a &lt;br/&gt;.
     * @param metrics The font metrics (&amp; font, via metrics.getFont()) to
     *        use to render this text.
     */
    public LineBreakBox(WebForm webform, Element element, HtmlTag tag) {
        super(webform, element, BoxType.LINEBREAK, true, true);
        this.contentWidth = this.width = 0;
        this.contentHeight = this.height = getMetrics().getHeight();
        this.tag = tag;
    }

    protected void initialize() {
    }

    protected void initializeInvariants() {
        Element element = getElement();
        // Help users find errors: <br> elements cannot contain content!
        if ((tag == HtmlTag.BR) && (element.getChildNodes().getLength() > 0)) {
////            org.netbeans.modules.visualweb.insync.markup.MarkupUnit unit = webform.getMarkup();
//            Element e = MarkupService.getCorrespondingSourceElement(element);
////            MarkupService.displayError(unit.getFileObject(), unit.computeLine(e),
////                NbBundle.getMessage(LineBreakBox.class, "BrNoChildren"));
//            Document doc = e.getOwnerDocument();
//            FileObject fo = InSyncService.getProvider().getFileObject(doc);
//            int line = InSyncService.getProvider().computeLine(doc, e);
//            InSyncService.getProvider().getRaveErrorHandler().displayErrorForFileObject(NbBundle.getMessage(LineBreakBox.class, "BrNoChildren"), fo, line, 0);
            // XXX This validation should be done in parser, not here!
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Element <br> may not contain children, children=" + element.getChildNodes())); // NOI18N
        }
    }

    protected void initializeHorizontalWidths(FormatContext context) {
    }

//    public String toString() {
//        return "LineBreakBox[" + paramString() + "]"; // NOI18N
//    }

    protected String paramString() {
        return super.paramString() + ", " + "font ascent=" + metrics.getAscent() + ", descent=" +
        metrics.getDescent() + ", height=" + metrics.getHeight() + ", " + "font=" +
        metrics.getFont();
    }

    public void paint(Graphics g, int px, int py) {
        if (CssBox.paintSpaces) {
            g.setColor(Color.RED);
            g.drawRect(getAbsoluteX(), getAbsoluteY(), width, height);

            int zx = getAbsoluteX() + 3;
            int zy = getAbsoluteY() + (height / 2) + 3;
            g.drawLine(zx, zy, zx + 5, zy);
            g.drawLine(zx + 5, zy, zx + 5, zy - 5);
        }
    }

    public FontMetrics getMetrics() {
        if (metrics == null) {
//            metrics = CssLookup.getFontMetrics(getElement());
//            metrics = CssProvider.getValueService().getFontMetricsForElement(getElement());
            // XXX Missing text.
            metrics = CssUtilities.getDesignerFontMetricsForElement(getElement(), null);
        }

        return metrics;
    }

    public int getIntrinsicWidth() {
        return width;
    }

    public int getIntrinsicHeight() {
        return height;
    }
}
