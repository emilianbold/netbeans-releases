/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * View with highlights. This is the most used view.
 *
 * @author Miloslav Metelka
 */

public class HighlightsView extends EditorView implements TextLayoutView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsView.class.getName());

    /** Offset of start offset of this view. */
    private int rawOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    /** Attributes for rendering */
    private final AttributeSet attributes; // 36 + 4 = 40 bytes

    public HighlightsView(int offset, int length, AttributeSet attributes) {
        super(null);
        assert (length > 0) : "length=" + length + " <= 0"; // NOI18N
        this.rawOffset = offset;
        this.length = length;
        this.attributes = attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return 0f;
        }
        float span = (axis == View.X_AXIS)
            ? textLayout.getAdvance()
            : textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
        return span;
    }

    @Override
    public int getRawOffset() {
        return rawOffset;
    }

    @Override
    public void setRawOffset(int rawOffset) {
        this.rawOffset = rawOffset;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean setLength(int length) {
        this.length = length;
        return true; // Possibly cached text layout gets released automatically
    }

    @Override
    public int getStartOffset() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getViewOffset(rawOffset) : rawOffset;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public Document getDocument() {
        View parent = getParent();
        return (parent != null) ? parent.getDocument() : null;
    }

    @Override
    public AttributeSet getAttributes() {
        return attributes;
    }

    @Override
    public TextLayout createTextLayout() {
        return createTextLayout(0, getLength());
    }

    TextLayout createTextLayout(int shift, int length) {
        DocumentView documentView = getDocumentView();
        if (documentView == null) {
            return null;
        }
        Document doc = documentView.getDocument();
        FontRenderContext frc = documentView.getFontRenderContext();
        if (doc == null || frc == null) {
            return null;
        }
        String text;
        try {
            text = doc.getText(getStartOffset() + shift, length);
            if (documentView.isShowNonprintingCharacters()) {
                text = text.replace(' ', DocumentView.PRINTING_SPACE);
            }
        } catch (BadLocationException e) {
            return null; // => Null text layout
        }
        Font font = ViewUtils.getFont(getAttributes(), documentView.getTextComponent().getFont());
        TextLayout textLayout = new TextLayout(text, font, frc);
        return textLayout;
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    private TextLayout getTextLayout() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getTextLayout(this) : null;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        return modelToViewChecked(offset, alloc, bias, getTextLayout(), getStartOffset(), getLength());
    }

    static Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias,
            TextLayout textLayout, int startOffset, int textLength)
    {
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
        assert (textLayout.getCharacterCount() == textLength) : "textLayout.getCharacterCount()=" + // NOI18N
                textLayout.getCharacterCount() + " != textLength=" + textLength; // NOI18N
        // If offset is >getEndOffset() use view-end-offset - otherwise it would throw exception from textLayout.getCaretInfo()
	int charIndex = Math.min(offset - startOffset, textLength);
	TextHitInfo hit = (bias == Position.Bias.Forward)
                ? TextHitInfo.afterOffset(charIndex)
                : TextHitInfo.beforeOffset(charIndex);
	float[] locs = textLayout.getCaretInfo(hit);
        float width;
        if (charIndex < textLength) {
            TextHitInfo endHit = (bias == Position.Bias.Forward)
                    ? TextHitInfo.afterOffset(charIndex + 1)
                    : TextHitInfo.beforeOffset(charIndex + 1);
            float endLocs[] = textLayout.getCaretInfo(endHit);
            width = endLocs[0] - locs[0];
        } else {
            width = 1;
        }

        Rectangle2D.Double bounds = ViewUtils.shape2Bounds(alloc);
	bounds.setRect(
                bounds.getX() + locs[0],
                bounds.getY(),
                width,
                bounds.getHeight()
        );
        return bounds;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        return viewToModelChecked(x, y, alloc, biasReturn, getTextLayout(), getStartOffset());

    }

    static int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn,
            TextLayout textLayout, int startOffset)
    {
        if (textLayout == null) {
            return startOffset;
        }
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        TextHitInfo hitInfo = x2RelOffset(textLayout, (float)(x - mutableBounds.getX()));
        if (biasReturn != null) {
            biasReturn[0] = hitInfo.isLeadingEdge() ? Position.Bias.Forward : Position.Bias.Backward;
        }
        return startOffset + hitInfo.getInsertionIndex();
    }

    static TextHitInfo x2RelOffset(TextLayout textLayout, float x) {
        TextHitInfo hit;
        if (x >= textLayout.getAdvance()) {
            hit = TextHitInfo.trailing(textLayout.getCharacterCount());
        } else {
            hit = textLayout.hitTestChar(x, 0); // What about backward bias -> with higher offsets it may go back visually
        }
        return hit;

    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        return getNextVisualPositionFromChecked(offset, bias, alloc, direction, biasRet,
                getTextLayout(), getStartOffset(), getLength(), getDocumentView());
    }

    static int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet,
            TextLayout textLayout, int startOffset, int textLength, DocumentView docView)
    {
        switch (direction) {
            case View.NORTH:
            case View.SOUTH:
                if (offset != -1) {
                    // Presumably pos is between startOffset and endOffset,
                    // since GlyphView is only one line, we won't contain
                    // the position to the north/south, therefore return -1.
                    return -1;
                }
                if (docView != null) {
                    JTextComponent textComponent = docView.getTextComponent();
                    Caret caret = textComponent.getCaret();
                    Point magicPoint;
                    magicPoint = (caret != null) ? caret.getMagicCaretPosition() : null;
                    if (magicPoint == null) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    return viewToModelChecked((double)magicPoint.x, 0d, alloc, biasRet,
                            textLayout, startOffset);
                }
                break;

            case WEST:
                if (offset == -1) {
                    offset = Math.max(0, startOffset + textLength - 1);
                } else {
                    offset = Math.max(0, offset - 1);
                }
                break;
            case EAST:
                if (offset == -1) {
                    offset = startOffset;
                } else {
                    if (docView != null) {
                        offset = Math.min(offset + 1, docView.getDocument().getLength());
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }
        return offset;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        paint(g, alloc, clipBounds, this, getTextLayout(), 0, getLength());
    }

    static void paint(Graphics2D g, Shape alloc, Rectangle clipBounds,
            HighlightsView view, TextLayout textLayout, int shift, int len)
    {
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        DocumentView docView = view.getDocumentView();
        if (docView != null && allocBounds.intersects(clipBounds)) {
            if (docView != null) {
                PaintState paintState = PaintState.save(g);
                try {
                    AttributeSet attrs = view.getAttributes();
                    paintBackground(g, allocBounds, attrs, docView);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest(view.getDumpId() + ":paint-bkg: " + ViewUtils.toString(g.getColor()) + // NOI18N
                                ", bounds=" + ViewUtils.toString(allocBounds) + '\n'); // NOI18N
                    }
                    if (textLayout != null) {
                        paintForeground(g, allocBounds, docView, textLayout, attrs);
                        if (LOG.isLoggable(Level.FINEST)) {
                            int startOffset = view.getStartOffset() + shift;
                            Document doc = docView.getDocument();
                            CharSequence text = DocumentUtilities.getText(doc).subSequence(startOffset, startOffset + len);
                            // Here it's assumed that 'text' var contains the same content as (possibly cached)
                            // textLayout but if textLayout caching would be broken then they could differ.
                            LOG.finest(view.getDumpId() + ":paint-txt: \"" + CharSequenceUtilities.debugText(text) + // NOI18N
                                    "\", XY["+ ViewUtils.toStringPrec1(allocBounds.getX()) + ";" +
                                    ViewUtils.toStringPrec1(allocBounds.getY()) + "(B" + // NOI18N
                                    ViewUtils.toStringPrec1(docView.getDefaultBaselineOffset()) + // NOI18N
                                    ")], color=" + ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                        }
                    }
                } finally {
                    paintState.restore();
                }
            }
        }
    }

    static void paintBackground(Graphics2D g, Rectangle2D.Double allocBounds, AttributeSet attrs, DocumentView docView) {
        // Paint background
        JTextComponent textComponent = docView.getTextComponent();
        Color componentBackground = textComponent.getBackground();
        float baselineOffset = docView.getDefaultBaselineOffset();
        ViewUtils.applyBackgroundAttributes(attrs, componentBackground, g);
        if (!componentBackground.equals(g.getColor())) { // Not yet cleared by BasicTextUI.paintBackground()
            // clearRect() uses g.getBackground() color
            ViewUtils.fillRect(g, allocBounds);
        }

        // Paint possible underlines
        if (attrs != null) {
            int xInt = (int) allocBounds.getX();
            int yInt = (int) allocBounds.getY();
            int endXInt = (int) (allocBounds.getX() + allocBounds.getWidth() - 1);
            int endYInt = (int) (allocBounds.getY() + allocBounds.getHeight() - 1);
            Color leftBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.LeftBorderLineColor);
            Color rightBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.RightBorderLineColor);
            Color topBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.TopBorderLineColor);
            Color bottomBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.BottomBorderLineColor);
            if (leftBorderLineColor != null) {
                g.setColor(leftBorderLineColor);
                g.drawLine(xInt, yInt, xInt, endYInt);
            }
            if (rightBorderLineColor != null) {
                g.setColor(rightBorderLineColor);
                g.drawLine(endXInt, yInt, endXInt, endYInt);
            }
            if (topBorderLineColor != null) {
                g.setColor(topBorderLineColor);
                g.drawLine(xInt, yInt, endXInt, yInt);
            }
            if (bottomBorderLineColor != null) {
                g.setColor(bottomBorderLineColor);
                g.drawLine(xInt, endYInt, endXInt, endYInt);
            }

            Color waveUnderlineColor = (Color) attrs.getAttribute(EditorStyleConstants.WaveUnderlineColor);
            if (waveUnderlineColor != null && bottomBorderLineColor == null) { // draw wave underline
                g.setColor(waveUnderlineColor);
                float underlineOffset = docView.getDefaultUnderlineOffset() + baselineOffset;
                int y = (int)(allocBounds.getY() + underlineOffset + 0.5);
                int wavePixelCount = (int) allocBounds.getWidth() + 1;
                if (wavePixelCount > 0) {
                    int[] waveForm = {0, 0, -1, -1};
                    int[] xArray = new int[wavePixelCount];
                    int[] yArray = new int[wavePixelCount];

                    int waveFormIndex = xInt % 4;
                    for (int i = 0; i < wavePixelCount; i++) {
                        xArray[i] = xInt + i;
                        yArray[i] = y + waveForm[waveFormIndex];
                        waveFormIndex = (++waveFormIndex) & 3;
                    }
                    g.drawPolyline(xArray, yArray, wavePixelCount - 1);
                }
            }
        }
    }


    static void paintForeground(Graphics2D g, Rectangle2D.Double allocBounds,
            DocumentView docView, TextLayout textLayout, AttributeSet attrs)
    {
        JTextComponent textComponent = docView.getTextComponent();
        ViewUtils.applyForegroundAttributes(attrs, textComponent.getFont(),
                textComponent.getForeground(), g);
        paintTextLayout(g, allocBounds, docView, textLayout);

    }

    static void paintTextLayout(Graphics2D g, Rectangle2D.Double bounds,
            DocumentView docView, TextLayout textLayout)
    {
        float baselineOffset = docView.getDefaultBaselineOffset();
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();
        // TextLayout is unable to do a partial render
        textLayout.draw(g, x, y + baselineOffset);
    }

    @Override
    public View breakView(int axis, int offset, float x, float len) {
        View part = breakView(axis, offset, x, len, this, 0, getLength(), getTextLayout());
        return (part != null) ? part : this;
    }

    static View breakView(int axis, int breakPartStartOffset, float x, float len,
            HighlightsView fullView, int partShift, int partLength, TextLayout textLayout)
    {
        if (axis == View.X_AXIS) {
            DocumentView docView = fullView.getDocumentView();
            // [TODO] Should check for RTL text
            if (docView != null && textLayout != null && partLength > 1) {
                // The logic
                int fullViewStartOffset = fullView.getStartOffset();
                int partStartOffset = fullViewStartOffset + partShift;
                if (breakPartStartOffset - partStartOffset < 0 || breakPartStartOffset - partStartOffset > partLength) {
                    throw new IllegalArgumentException("offset=" + breakPartStartOffset + // NOI18N
                            "partStartOffset=" + partStartOffset + // NOI18N
                            ", partLength=" + partLength // NOI18N
                    );
                }
                // Compute charIndex relative to given textLayout
                int breakCharIndex = breakPartStartOffset - partStartOffset;
                assert (breakCharIndex >= 0);
                float breakCharIndexX;
                if (breakCharIndex != 0) {
                    TextHitInfo hit = TextHitInfo.afterOffset(breakCharIndex);
                    float[] locs = textLayout.getCaretInfo(hit);
                    breakCharIndexX = locs[0];
                } else {
                    breakCharIndexX = 0f;
                }
                TextHitInfo hitInfo = x2RelOffset(textLayout, breakCharIndexX + len);
                int breakPartEndOffset = partStartOffset + hitInfo.getCharIndex();
                // Now perform corrections if wrapping at word boundaries is required
                // Currently a simple impl that checks adjacent char(s) in backward direction
                // is used. Consider BreakIterator etc. if requested.
                // If break is inside a word then check for word boundary in backward direction.
                // If none is found then go forward to find a word break if possible.
                if (docView.getLineWrapType() == DocumentView.LineWrapType.WORD_BOUND) {
                    CharSequence docText = DocumentUtilities.getText(docView.getDocument());
                    if (breakPartEndOffset > breakPartStartOffset) {
                        boolean searchNonLetterForward = false;
                        char ch = docText.charAt(breakPartEndOffset - 1);
                        // [TODO] Check surrogates
                        if (Character.isLetterOrDigit(ch)) {
                            if (breakPartEndOffset < docText.length() &&
                                    Character.isLetterOrDigit(docText.charAt(breakPartEndOffset)))
                            {
                                // Inside word
                                // Attempt to go back and search non-letter
                                int offset = breakPartEndOffset - 1;
                                while (offset >= breakPartStartOffset && Character.isLetterOrDigit(docText.charAt(offset))) {
                                    offset--;
                                }
                                offset++;
                                if (offset == breakPartStartOffset) {
                                    searchNonLetterForward = true;
                                } else { // move the break offset back
                                    breakPartEndOffset = offset;
                                }
                            }
                        }
                        if (searchNonLetterForward) {
                            breakPartEndOffset++; // char at breakPartEndOffset already checked
                            while (breakPartEndOffset < partStartOffset + partLength &&
                                    Character.isLetterOrDigit(docText.charAt(breakPartEndOffset)))
                            {
                                breakPartEndOffset++;
                            }
                        }
                    }
                }

                // Length must be > 0; BTW TextLayout can't be constructed with empty string.
                boolean breakFailed = (breakPartEndOffset - breakPartStartOffset == 0) ||
                        (breakPartEndOffset - breakPartStartOffset >= partLength);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("HV.breakView(): <"  + partStartOffset + "," + (partStartOffset+partLength) + // NOI18N
                        "> => <" + breakPartStartOffset + "," + (partStartOffset+breakPartEndOffset) + // NOI18N
                        ">, x=" + x + ", len=" + len + // NOI18N
                        ", charIndexX=" + breakCharIndexX + "\n"); // NOI18N
                }
                if (breakFailed) {
                    return null;
                }
                return new HighlightsViewPart(fullView, breakPartStartOffset - fullViewStartOffset,
                        breakPartEndOffset - breakPartStartOffset);
            }
        }
        return null;
    }

    @Override
    public View createFragment(int p0, int p1) {
        int startOffset = getStartOffset();
        ViewUtils.checkFragmentBounds(p0, p1, startOffset, getLength());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("HV.createFragment(" + p0 + "," + p1+ "): <" + getStartOffset() + "," + // NOI18N
                    getEndOffset() + ">\n"); // NOI18N
        }
        return new HighlightsViewPart(this, p0 - startOffset, p1 - p0);
    }

    @Override
    protected String getDumpName() {
        return "HV";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

}
