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


import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.designer.WebForm;

import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * SpaceBox represents a sequence of space characters in
 * a LineBox.
 * <p>
 * See  http://www.w3.org/TR/REC-CSS2/visuren.html
 * <p>
 * @todo I'm inheriting a lot of crap from CssBox -- margins, padding,
 *  etc. Is there a way I could make an even more primitive box than
 *  CssBox for this?

 * @author Tor Norbye
 */
public class SpaceBox extends CssBox {
    private int beginDomOffset = -1;
    private int endDomOffset = -1;
    private int beginOffset;
    private int endOffset;
    private String xhtml; // Identical to the "contentChars" char array
    private String jspx; // Original jspx source whose entities were expanded into xhtml.
    private FontMetrics metrics;
    private int decoration;
//    private RaveText node;
    private Text node;
    private Color fg;

    /**
     *  Create a SpaceBox representing the set of spaces running from
     *  position [beginNode,beginOffset] to position
     *  [endNode,endOffset], formatted using the given style.
     *
     * @param beginOffset The offset into the contentChars array where the text
     *        for this inline box begins.
     * @param endOffset The offset into the contentChars array where the text
     *        for this inline box ends.
     * @param decoration A bit mask indicating whether underline, overline,
     *          strikethrough etc. should be drawn according to the UNDERLINE,
     *          STRIKE etc. constant fields of the class.
     * @param metrics The font metrics (&amp; font, via metrics.getFont()) to
     *        use to render this text.
     */
    public SpaceBox(WebForm webform, Element styleElement, Text node, String xhtml, String jspx,
        int beginOffset, int endOffset, Color fg, Color bg, int decoration, FontMetrics metrics,
        boolean hidden) {
        // Space isn't technically replaced, but it acts like it in many
        // ways, e.g. it has an intrinsic size, should be treated "atomically"
        // like an <object> or an <iframe>, etc., and it's that meaning that is
        // used for the replaced flag
        super(webform, styleElement, BoxType.SPACE, true, true);
        this.beginOffset = beginOffset;
        this.endOffset = endOffset;
        this.node = node;
        this.decoration = decoration;
        this.fg = fg;
        this.bg = bg;
        this.metrics = metrics;
        this.xhtml = xhtml;
        this.jspx = jspx;
        this.hidden = hidden;

        this.contentWidth = this.width = metrics.charWidth(' ');
        this.contentHeight = this.height = metrics.getHeight();
    }

    protected void initialize() {
    }

    // Note that I pass in null to super's element reference because
    // the element corresponds to the parent, not this box, yet
    // we want to store it as our effective element such that css
    // lookups on this text box uses this element.
    protected void initializeDesignBean() {
    }

    protected void initializeInvariants() {
    }

    /** Can't set widths on text boxes themselves
     * @todo Can min-width be set on spans etc. ?
     */
    protected void initializeHorizontalWidths(FormatContext context) {
    }

//    public String toString() {
//        return "SpaceBox[" + paramString() + "]";
//    }

    protected String paramString() {
        return "space, " + super.paramString() + ", "
                + (metrics == null ? "" : "font ascent=" + metrics.getAscent() + ", descent=" + metrics.getDescent() + ", height=" + metrics.getHeight() + ", " + "font=" + metrics.getFont())
                + ", boffset=" + beginOffset + ", eoffset=" + endOffset;
    }

    protected void paintBackground(Graphics g, int px, int py) {
        if (hidden) {
            return;
        }

        if (bg != null) {
            int x = getX() + px;
            int y = getY() + py;
            g.setColor(bg);
            g.fillRect(x, y, width, height);
        }
    }

    public void paint(Graphics g, int px, int py) {
        if (hidden) {
            return;
        }

        // We deliberately don't want the background painted yet, and
        // since we have no children to get called there's no reason to
        // call the super.
        //super.paint(g, px, py);
        int x = getX() + px;
        int y = getY() + py;

        // No text to paint!
        g.setColor(fg);
        g.setFont(metrics.getFont());

        // determine the y coordinate to render the underline
        // render underline or strikethrough if set.
        if ((decoration & TextBox.UNDERLINE) != 0) {
            int yTmp = (y + metrics.getHeight()) - metrics.getDescent() + 1;
            g.drawLine(x, yTmp, x + getWidth(), yTmp);
        }

        if ((decoration & TextBox.STRIKE) != 0) {
            int yTmp = (y + metrics.getHeight()) - metrics.getDescent();

            // move y coordinate above baseline
            yTmp -= (int)(metrics.getAscent() * 0.4f);
            g.drawLine(x, yTmp, x + getWidth(), yTmp);
        }

        if ((decoration & TextBox.OVERLINE) != 0) {
            g.drawLine(x, y, x + getWidth(), y);
        }

        if (CssBox.paintSpaces) {
            g.setColor(Color.GREEN);
            g.drawRect(getAbsoluteX(), getAbsoluteY(), width, height);
        }
    }

    public FontMetrics getMetrics() {
        return metrics;
    }

    // Intended for testsuite
    public boolean isUnderline() {
        return (decoration & TextBox.UNDERLINE) != 0;
    }

    // Intended for testsuite
    public boolean isStrikeThrough() {
        return (decoration & TextBox.STRIKE) != 0;
    }

    // Intended for testsuite
    public boolean isOverline() {
        return (decoration & TextBox.OVERLINE) != 0;
    }

    // Intended for testsuite
    public Color getTextColor() {
        return fg;
    }

    public int getIntrinsicWidth() {
        return width;
    }

    public int getIntrinsicHeight() {
        return height;
    }

    public int getBaseline() {
       return metrics.getHeight() - metrics.getDescent();
    }

    /** Return the first position in the document of this space node */
//    public Position getFirstPosition() {
    public DomPosition getFirstPosition() {
        if (node != null) {
//            return new Position(node, getDomStartOffset(), Bias.FORWARD);
//            return Position.create(node, getDomStartOffset(), Bias.FORWARD);
            return webform.createDomPosition(node, getDomStartOffset(), Bias.FORWARD);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /** Return the last position in the document of this space node */
//    public Position getLastPosition() {
    public DomPosition getLastPosition() {
        if (node != null) {
//            return new Position(node, getDomEndOffset(), Bias.BACKWARD);
//            return Position.create(node, getDomEndOffset(), Bias.BACKWARD);
            return webform.createDomPosition(node, getDomEndOffset(), Bias.BACKWARD);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /** TODO: rename to computePosition */
//    public Position computePosition(int px) {
    public DomPosition computePosition(int px) {
        if (node == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        int offset = getDomStartOffset();

        /* XXX TODO -- look at the width of the space and pick the best fit
           based on which middle half you're on
        if ((px-textWidth) > charWidth/2) {
            offset++;
        }
        */
//        return new Position(node, offset, Bias.FORWARD); // XXX set bias to back if equal to endOffset?
//        return Position.create(node, offset, Bias.FORWARD); // XXX set bias to back if equal to endOffset?
        return webform.createDomPosition(node, offset, Bias.FORWARD); // XXX set bias to back if equal to endOffset?
    }

    /** Return the bounding box of the character at the given position */
//    public Rectangle getBoundingBox(Position pos) {
    public Rectangle getBoundingBox(DomPosition pos) {
        assert pos.getNode() == node;

        if (pos.getOffset() == beginOffset) {
            return new Rectangle(getAbsoluteX(), getAbsoluteY(), width, height);
        } else {
            return new Rectangle(getAbsoluteX() + width, getAbsoluteY(), width, height);
        }
    }

    /** Return the node this text box is associated with */
    public Text getNode() {
        return node;
    }

    /** Return the beginning offset within the text node that this box represents */
    public int getDomStartOffset() {
        // XXX Can I have &nbps;'s here?
        if (beginDomOffset == -1) {
            if(jspx == xhtml || jspx.indexOf('&') == -1) {
                beginDomOffset = beginOffset;
            } else {
                // <markup_separation>
//                beginDomOffset = MarkupServiceProvider.getDefault().
//                        getUnexpandedOffset(jspx, beginOffset);
                // ====
//                beginDomOffset = InSyncService.getProvider().getUnexpandedOffset(jspx, beginOffset);
                beginDomOffset = webform.getDomProviderService().getUnexpandedOffset(jspx, beginOffset);
                // </markup_separation>
            }
        }

        return beginDomOffset;
    }

    /** Return the ending offset within the text node that this box represents */
    public int getDomEndOffset() {
        // XXX Can I have &nbps;'s here?
        if (endDomOffset == -1) {
            if(jspx == xhtml || jspx.indexOf('&') == -1) {
                endDomOffset = endOffset;
            } else {
                // <markup_separation>
//                endDomOffset = MarkupServiceProvider.getDefault().
//                        getUnexpandedOffset(jspx, endOffset);
                // ====
//                endDomOffset = InSyncService.getProvider().getUnexpandedOffset(jspx, endOffset);
                endDomOffset = webform.getDomProviderService().getUnexpandedOffset(jspx, endOffset);
                // </markup_separation>
            }
        }

        return endDomOffset;
    }

    /**
     * Given the position, return the previous position within this textbox if available
     * or Position.NONE if not.
     */
//    public Position getPrev(Position pos) {
    public DomPosition getPrev(DomPosition pos) {
        if (node == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        if (pos.getOffset() > getDomStartOffset()) {
            // Jump over all spaces in one big jump
            int offset = getDomStartOffset();

//            return new Position(node, offset, Bias.BACKWARD);
//            return Position.create(node, offset, Bias.BACKWARD);
            return webform.createDomPosition(node, offset, Bias.BACKWARD);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /**
     * Given the position, return the next position within this textbox if available
     * or Position.NONE if not.
     */
//    public Position getNext(Position pos) {
    public DomPosition getNext(DomPosition pos) {
        if (node == null) {
//            return Position.NONE;
            return DomPosition.NONE;
        }

        if (pos.getOffset() < getDomEndOffset()) {
            // Jump over all spaces in one big jump
            int offset = getDomEndOffset();

//            return Position.create(node, offset, Bias.FORWARD);
            return webform.createDomPosition(node, offset, Bias.FORWARD);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }
}
