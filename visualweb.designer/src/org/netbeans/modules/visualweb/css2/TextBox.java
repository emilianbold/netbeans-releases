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
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.designer.DesignerPane;
import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;

import org.openide.ErrorManager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


/**
 * TextBox represents a box of text characters placed within
 * a LineBox.
 * <p>
 * See  http://www.w3.org/TR/REC-CSS2/visuren.html
 * <p>
 * @author Tor Norbye
 */
public final class TextBox extends CssBox {
    public static final int UNDERLINE = 1;
    public static final int STRIKE = 2;
    public static final int OVERLINE = 4;
    private int beginDomOffset = -1;
    private int endDomOffset = -1;
    private final int beginOffset;
    private final int endOffset;
    private char[] contentChars;
    private String xhtml; // Identical to the "contentChars" char array
    private String jspx; // Original jspx source whose entities were expanded into xhtml.
    private FontMetrics metrics;
    private Color fg;
    private int decoration;
//    private RaveText node;
    private Text node;

    /**
     *  Create an TextBox representing the text run from
     *  position [beginNode,beginOffset] to position
     *  [endNode,endOffset], formatted using the given style.
     *
     * @param contentChars The string to draw text from, as an array
     *        of chars.
     * @param beginOffset The offset into the contentChars array where the text
     *        for this inline box begins.
     * @param endOffset The offset into the contentChars array where the text
     *        for this inline box ends.
     * @param fg The color to draw the text with
     * @param decoration A bit mask indicating whether underline, overline,
     *          strikethrough etc. should be drawn according to the UNDERLINE,
     *          STRIKE etc. constant fields of the class.
     * @param metrics The font metrics (&amp; font, via metrics.getFont()) to
     *        use to render this text.
     */
    public TextBox(WebForm webform, Element styleElement, Text node, char[] contentChars,
        String xhtml, String jspx, int beginOffset, int endOffset, Color fg, Color bg,
        int decoration, FontMetrics metrics, boolean hidden) {
        // Text isn't technically replaced, but it acts like it in many
        // ways, e.g. it has an intrinsic size, should be treated "atomically"
        // like an <object> or an <iframe>, etc., and it's that meaning that
        // is used for the replaced flag
        super(webform, styleElement, BoxType.TEXT, true, true);
        this.contentChars = contentChars;
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

        int textWidth =
            DesignerUtils.getNonTabbedTextWidth(contentChars, beginOffset, endOffset, metrics);
        this.width = textWidth;

        if (metrics != null) {
            this.height = metrics.getHeight();
        } else {
            ErrorManager.getDefault().log("No metrics found");
        }

        this.contentWidth = this.width;
        this.contentHeight = this.height;
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
//        return "TextBox[" + paramString() + "]";
//    }

    protected String paramString() {
        return "text=\"" + (contentChars == null ? null : getText()) + "\", "
                + super.paramString()
                + (metrics == null ? "" :  ", font ascent=" +  metrics.getAscent() + ", descent=" + metrics.getDescent() + ", height=" + metrics.getHeight() + ", font=" + metrics.getFont())
                + ", boffset=" + beginOffset + ", eoffset=" + endOffset;
    }

    // For testsuite & debugging only!
    public String getText() {
        return new String(contentChars, beginOffset, endOffset - beginOffset);
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

        g.setFont(metrics.getFont());

        DesignerPane pane = webform.getPane();
//        DesignerCaret caret = (pane != null) ? pane.getCaret() : null;
//        if ((caret != null) && caret.hasSelection()) {
        if (pane != null && pane.hasCaretSelection()) {
            if (!paintSelectedText(g, px, py)) {
                // No selection overlap or other failure - do normal painting
                g.setColor(fg);

                int x = getX() + px;
                int y = getY() + py;
                int yadj = (y + metrics.getHeight()) - metrics.getDescent();
                g.drawChars(contentChars, beginOffset, endOffset - beginOffset, x, yadj);
                paintLines(g, x, y, yadj, getWidth());
            }

            return;
        }

        // We deliberately don't want the background painted yet, and
        // since we have no children to get called there's no reason to
        // call the super.
        //super.paint(g, px, py);
        int x = getX() + px;
        int y = getY() + py;

        g.setColor(fg);

        // determine the y coordinate to render the glyphs
        int yadj = (y + metrics.getHeight()) - metrics.getDescent();

        // Draw text!
        g.drawChars(contentChars, beginOffset, endOffset - beginOffset, x, yadj);

        // render underline or strikethrough if set.
        paintLines(g, x, y, yadj, getWidth());

        if (CssBox.paintText) {
            g.setColor(Color.BLUE);
            g.drawRect(getAbsoluteX(), getAbsoluteY(), width, height);
        }
    }

    /**
     * Paint the text if we have a selection somewhere. Might be much
     * slower than normal painting since each text box will do position
     * computations to determine if it overlaps the selection.
     * @return true iff there is overlap of this textbox and the selection -
     *   and it will handle the paint. Returns true otherwise and the caller
     *   needs to handle the paint.
     */
    private boolean paintSelectedText(Graphics g, int px, int py) {
        // Some text boxes are generated (such as the image labels for
        // images where the image is not found.)  These cannot possibly
        // contain selected text.
        if (node == null) {
            return false;
        }
        DesignerPane pane = webform.getPane();
//        assert pane != null;
        if (pane == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("WebForm has null pane, webForm=" + webform)); // NOI18N
            return false;
        }

//        DesignerCaret caret = pane.getCaret();
//        assert (caret != null) && caret.hasSelection();
        if (!pane.hasCaretSelection()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Pane doesn't have caret selection, pane=" + pane)); // NOI18N
            return false;
        }

        // Determine if the range intersects our line box group
//        Position sourceCaretBegin = caret.getFirstPosition();
//        DomPosition sourceCaretBegin = caret.getFirstPosition();
        DomPosition sourceCaretBegin = pane.getFirstPosition();

        // XXX I ought to have a cached method on the caret for obtaining the rendered
        // location!
//        Position caretBegin = sourceCaretBegin.getRenderedPosition();
//        Position sourceCaretEnd = caret.getLastPosition();
//        Position caretEnd = sourceCaretEnd.getRenderedPosition();
        DomPosition caretBegin = sourceCaretBegin.getRenderedPosition();
//        DomPosition sourceCaretEnd = caret.getLastPosition();
        DomPosition sourceCaretEnd = pane.getLastPosition();
        
        DomPosition caretEnd = sourceCaretEnd.getRenderedPosition();
        Node caretBeginNode = caretBegin.getNode();

        if (caretBeginNode == null) {
            return false;
        }

        Node caretEndNode = caretEnd.getNode();

        if (caretEndNode == null) {
            return false;
        }

        Text renderNode = node;

//        if (!renderNode.isRendered()) {
//            renderNode = renderNode.getRendered();
//        }
//        if (!MarkupService.isRenderedNode(renderNode)) {
        if (!webform.isRenderedNode(renderNode)) {
            renderNode = MarkupService.getRenderedTextForText(renderNode);
        }

//        int r1 =
//            Position.compareBoundaryPoints(caretBeginNode, caretBegin.getOffset(), renderNode,
//                endOffset);
//        int r2 =
//            Position.compareBoundaryPoints(caretEndNode, caretEnd.getOffset(), renderNode,
//                beginOffset);
        int r1 = webform.compareBoundaryPoints(caretBeginNode, caretBegin.getOffset(), renderNode, endOffset);
        int r2 = webform.compareBoundaryPoints(caretEndNode, caretEnd.getOffset(), renderNode, beginOffset);

        if (!((r1 >= 0) && (r2 <= 0))) {
            // No overlap - just do normal painting
            return false;
        }

        // Compute the before, during and after sections of the selection.
        // We will paint selection from selStartOffset to selEndOffset,
        // and non-selection from beginOffset to selStartOffset and
        // from selEndOffset to endOffset.
        int selStartOffset;
        int selEndOffset;

        if (caretBeginNode == renderNode) {
            selStartOffset = caretBegin.getOffset();

            if (selStartOffset < beginOffset) {
                selStartOffset = beginOffset;
            }
        } else {
            selStartOffset = beginOffset; // somewhere before this node
        }

        if (caretEndNode == renderNode) {
            selEndOffset = caretEnd.getOffset();

            if (selEndOffset > endOffset) {
                selEndOffset = endOffset;
            }
        } else {
            selEndOffset = endOffset; // somewhere after this node
        }

        int x = getX() + px;
        int y = getY() + py;

        // determine the y coordinate to render the glyphs
        int yadj = (y + metrics.getHeight()) - metrics.getDescent();

        // Paint region BEFORE selection (might be empty)
        if (selStartOffset > beginOffset) {
            g.setColor(fg);
            g.drawChars(contentChars, beginOffset, selStartOffset - beginOffset, x, yadj);

            int w =
                DesignerUtils.getNonTabbedTextWidth(contentChars, beginOffset, selStartOffset,
                    metrics);
            paintLines(g, x, y, yadj, w);
            x += w;
        }

        // Paint selection region:   selStartOffset to selEndOffset
        if (selEndOffset > selStartOffset) {
            g.setColor(pane.getSelectedTextColor());
            g.drawChars(contentChars, selStartOffset, selEndOffset - selStartOffset, x, yadj);

            int w =
                DesignerUtils.getNonTabbedTextWidth(contentChars, selStartOffset, selEndOffset,
                    metrics);
            paintLines(g, x, y, yadj, w);
            x += w;
        }

        // Paint region AFTER selection (might be empty)
        if (selEndOffset < endOffset) {
            g.setColor(fg);
            g.drawChars(contentChars, selEndOffset, endOffset - selEndOffset, x, yadj);

            int w =
                DesignerUtils.getNonTabbedTextWidth(contentChars, selEndOffset, endOffset, metrics);
            paintLines(g, x, y, yadj, w);
            x += w; // not strictly necessary, we're done with x
        }

        return true;
    }

    /** Paint underline, strike through and/or overline as appropriate */
    private void paintLines(Graphics g, int x, int y, int yBaseline, int width) {
        // render underline or strikethrough if set.
        if ((decoration & UNDERLINE) != 0) {
            int yTmp = yBaseline;
            yTmp += 1;
            g.drawLine(x, yTmp, x + width, yTmp);
        }

        if ((decoration & STRIKE) != 0) {
            int yTmp = yBaseline;

            // move y coordinate above baseline
            yTmp -= (int)(metrics.getAscent() * 0.4f);
            g.drawLine(x, yTmp, x + width, yTmp);
        }

        if ((decoration & OVERLINE) != 0) {
            g.drawLine(x, y, x + width, y);
        }
    }

    public FontMetrics getMetrics() {
        return metrics;
    }

    // Intended for testsuite
    public boolean isUnderline() {
        return (decoration & UNDERLINE) != 0;
    }

    // Intended for testsuite
    public boolean isStrikeThrough() {
        return (decoration & STRIKE) != 0;
    }

    // Intended for testsuite
    public boolean isOverline() {
        return (decoration & OVERLINE) != 0;
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
       // TODO - half leading?
       return metrics.getHeight() - metrics.getDescent();
    }

    /** Return the first position in the document of this text node */
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

    /** Return the last position in the document of this text node */
//    public Position getLastPosition() {
    public DomPosition getLastPosition() {
        if (node != null) {
//            return new Position(node, getDomEndOffset(), Bias.BACKWARD);
//            return Position.create(node, getDomEndOffset(), Bias.BACKWARD);
            return webform.createDomPosition(node, getDomEndOffset(), Bias.BACKWARD);
        } else {
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

        // XXX what about ' ' ?
        int offset =
//            DesignerUtils.getNonTabbedTextOffset(contentChars, beginOffset,
            getNonTabbedTextOffset(contentChars, beginOffset,
                endOffset - beginOffset, metrics, 0, px);

        // Pick the nearest offset. Alternatively enhance the above method
        // to return it (perhaps based on a flag, since it's also used
        // to compute how much we can fit which has to be more conservative.
        if (offset < endOffset) {
            // Distance up to the character that contains the x position
            int textWidth =
                DesignerUtils.getNonTabbedTextWidth(contentChars, beginOffset, offset, metrics);

            // Width of the character that contains the x position
            int charWidth =
                DesignerUtils.getNonTabbedTextWidth(contentChars, offset, offset + 1, metrics);

            //  px points to a distance between textWidth and textWidth+charWidth
            // so px-textWidth should be between 0 and charWidth
            if ((px - textWidth) > (charWidth / 2)) {
                offset++;
            }
        }

        // In the presence of entities we have to compute the position in the original
        // jspx dom
        if (jspx != xhtml && jspx.indexOf('&') != -1) {
            // <markup_separation>
//            offset = MarkupServiceProvider.getDefault().getUnexpandedOffset(jspx, offset);
            // ====
//            offset = InSyncService.getProvider().getUnexpandedOffset(jspx, offset);
            offset = webform.getDomProviderService().getUnexpandedOffset(jspx, offset);
            // </markup_separation>
        }

//        return new Position(node, offset, Bias.FORWARD); // XXX set bias depending on how it compares to end offset?
//        return Position.create(node, offset, Bias.FORWARD); // XXX set bias depending on how it compares to end offset?
        return webform.createDomPosition(node, offset, Bias.FORWARD); // XXX set bias depending on how it compares to end offset?
    }
    
    // XXX Moved from DesignerUtils.
    /**
     * Unlike the methods in javax.swing.text I'm returning the text offset itself, not the distance
     * from the passed in segment/string offset
     */
    private static final int getNonTabbedTextOffset(char[] s, int txtOffset, int len,
            FontMetrics metrics, int x0, int x) {
        if (x0 >= x) {
            // x before x0, return.
            return txtOffset;
        }
        
        int currX = x0;
        int nextX = currX;
        
        // s may be a shared segment, so it is copied prior to calling
        // the tab expander
        int n = txtOffset + len;
        final boolean round = true;
        
        for (int i = txtOffset; i < n; i++) {
            char c = s[i];
            
            // TODO if there are successive spaces, ignore them
            // TODO count a newline as a space!
            if ((c == '\t') || (c == '\n')) {
                nextX += metrics.charWidth(' ');
            } else {
                nextX += metrics.charWidth(c);
            }
            
            if ((x >= currX) && (x < nextX)) {
                // found the hit position... return the appropriate side
                if ((round == false) || ((x - currX) < (nextX - x))) {
                    return i;
                } else {
                    return i + 1;
                }
            }
            
            currX = nextX;
        }
        
        return txtOffset;
    }


    /** Return the bounding box of the character at the given position */
//    public Rectangle getBoundingBox(Position pos) {
    public Rectangle getBoundingBox(DomPosition pos) {
        // This is not always true, because for example if you're pointing to
        // a paragraph in the position, we adjust the visual location search to
        // refer to the first text node child in that paragraph instead
        // assert pos.getNode() == node;
        // XXX what about ' ' ?
        int htmlpos;
        if(jspx == xhtml || jspx.indexOf('&') == -1) {
            htmlpos = pos.getOffset();
        } else {
            // <markup_separation>
//            htmlpos = MarkupServiceProvider.getDefault().
//                    getExpandedOffset(jspx, pos.getOffset());
            // ====
//            htmlpos = InSyncService.getProvider().getExpandedOffset(jspx, pos.getOffset());
            htmlpos = webform.getDomProviderService().getExpandedOffset(jspx, pos.getOffset());
            // </markup_separation>
        }

        if (htmlpos > contentChars.length) {
            // Race condition; after we've modified a document node (in Document.insertString
            // for example, we immediately update the caret, and it may be painted
            // due to paint requests, before we've updated the view hiearchy (which
            // is always delayed one event loop iteration for various reasons, some
            // described in Document.handleEvent and some in DndHandler, if I recall
            // correctly
            contentChars = pos.getNode().getNodeValue().toCharArray();
        }

        int offset =
            DesignerUtils.getNonTabbedTextWidth(contentChars, beginOffset, htmlpos, metrics);
        int textWidth = 1; // can't compute position for last char in a text box

        if (pos.getOffset() < contentChars.length) {
            textWidth =
                DesignerUtils.getNonTabbedTextWidth(contentChars, htmlpos, htmlpos, metrics);
        }

        return new Rectangle(getAbsoluteX() + offset, getAbsoluteY(), textWidth, getHeight());
    }

    /** Return the node this text box is associated with */
    public Text getNode() {
        return node;
    }

    /* Used only by the unit tests?
        public int getLength() {
            return endOffset-beginOffset;
        }
    */

    /** Return the beginning offset within the text node that this box represents */
    public int getDomStartOffset() {
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
            int offset;
            if(jspx == xhtml || jspx.indexOf('&') == -1) {
                offset = pos.getOffset();
            } else {
                // <markup_separation>
//                offset = MarkupServiceProvider.getDefault().
//                        getExpandedOffset(jspx, pos.getOffset());
                // ====
//                offset = InSyncService.getProvider().getExpandedOffset(jspx, pos.getOffset());
                offset = webform.getDomProviderService().getExpandedOffset(jspx, pos.getOffset());
                // </markup_separation>
            }

            if (isCharacterPair(offset - 2)) {
                offset -= 2;
            } else {
                offset -= 1;
            }

            if(jspx != xhtml && jspx.indexOf('&') != -1) {
                // <markup_separation>
//                offset = MarkupServiceProvider.getDefault().
//                        getUnexpandedOffset(jspx, offset);
                // ====
//                offset = InSyncService.getProvider().getUnexpandedOffset(jspx, offset);
                offset = webform.getDomProviderService().getUnexpandedOffset(jspx, offset);
                // </markup_separation>
            }

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
            int offset;
            if(jspx == xhtml || jspx.indexOf('&') == -1) {
                offset = pos.getOffset();
            } else {
                // <markup_separation>
//                offset = MarkupServiceProvider.getDefault().
//                        getExpandedOffset(jspx, pos.getOffset());
                // ====
//                offset = InSyncService.getProvider().getExpandedOffset(jspx, pos.getOffset());
                offset = webform.getDomProviderService().getExpandedOffset(jspx, pos.getOffset());
                // </markup_separation>
            }

            if (isCharacterPair(offset)) {
                offset += 2;
            } else {
                offset += 1;
            }

            if(jspx != xhtml && jspx.indexOf('&') != -1) {
                // <markup_separation>
//                offset = MarkupServiceProvider.getDefault().
//                        getUnexpandedOffset(jspx, offset);
                // ====
//                offset = InSyncService.getProvider().getUnexpandedOffset(jspx, offset);
                offset = webform.getDomProviderService().getUnexpandedOffset(jspx, offset);
                // </markup_separation>
            }

//            return new Position(node, offset, Bias.FORWARD);
//            return Position.create(node, offset, Bias.FORWARD);
            return webform.createDomPosition(node, offset, Bias.FORWARD);
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /** Return true iff the character at the given offset is the  beginning
     * of a 2-char "unit" which should be deleted, caret'ed over, etc. as a single
     * item. If offset is outside the valid range for this text area, it will return
     * false.
     */
    private boolean isCharacterPair(int offset) {
        // See if this character is "attached" to the previous character -
        // if so delete the pair
        if ((offset >= 0) && (offset < (endOffset - 1))) {
            char c0 = contentChars[offset];
            char c1 = contentChars[offset + 1];

            // D800-DBFF: Unicode Low Surrogate Area Range
            if ((c0 >= '\uD800') && (c0 <= '\uDBFF') && (
                // DC00-DFFF: Unicode High Surrogate Area Range
                c1 >= '\uDC00') && (c1 <= '\uDFFF')) {
                // Yes!
                return true;
            }
        }

        return false;
    }
}
