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

import org.netbeans.modules.visualweb.designer.CssUtilities;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import org.openide.ErrorManager;

import org.w3c.dom.Element;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


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
            metrics = CssUtilities.getDesignerFontMetricsForElement(getElement(), null, webform.getDefaultFontSize());
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
