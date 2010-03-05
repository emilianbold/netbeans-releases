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
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * View of (possibly multiple) '\t' characters.
 * <br/>
 * It needs to be measured specially - it needs to get visually aligned to multiples
 * of TAB_SIZE char width.
 * <br/>
 *
 *
 * @author Miloslav Metelka
 */

public final class TabView extends EditorView implements TextLayoutView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.TabView.level=FINE
    private static final Logger LOG = Logger.getLogger(TabView.class.getName());

    /** Offset of start offset of this view. */
    private int rawOffset; // 24-super + 4 = 28 bytes

    /** Number of subsequent '\t' characters. */
    private int length; // 28 + 4 = 32 bytes

    /** Attributes for rendering */
    private final AttributeSet attributes; // 36 + 4 = 40 bytes

    public TabView(int offset, int length, AttributeSet attributes) {
        super(null);
        assert (length > 0) : "Length == 0"; // NOI18N
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
        clearTextLayout(); // Ensure that text layout gets recreated
        return true;
    }

    @Override
    public int getStartOffset() {
        ParagraphView parent = (ParagraphView) getParent();
        return (parent != null) ? parent.getViewOffset(rawOffset) : rawOffset;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    public TextLayout createTextLayout() {
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
            text = doc.getText(getStartOffset(), length);
            if (documentView.isShowNonprintingCharacters()) {
                text = text.replace(' ', DocumentView.PRINTING_SPACE);
            }
        } catch (BadLocationException e) {
            return null; // => Null text layout
        }
        Font font = ViewUtils.getFont(attributes, documentView.getTextComponent().getFont());
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

    TextLayoutCache getTextLayoutCache() {
        DocumentView documentView = getDocumentView();
        return (documentView != null) ? documentView.getTextLayoutCache() : null;
    }

    TextLayout getTextLayout() {
        ParagraphView paragraphView = getParagraphView();
        if (paragraphView != null) {
            DocumentView documentView = paragraphView.getDocumentView();
            if (documentView != null) {
                return getTextLayoutCache().get(paragraphView, this);
            }
        }
        return null;
    }

    void clearTextLayout() {
        ParagraphView paragraphView = getParagraphView();
        if (paragraphView != null) {
            DocumentView documentView = paragraphView.getDocumentView();
            if (documentView != null) {
                getTextLayoutCache().put(paragraphView, this, null);
            }
        }
    }

    float getSpan() {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return 0f;
        }
        return textLayout.getAdvance();
    }

    float getSpan(int offset0, int offset1) {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return 0f;
        }
        int viewStartOffset = getStartOffset();
	if ((offset0 == viewStartOffset) && (offset1 == viewStartOffset + length)) {
	    return textLayout.getAdvance();
	}
	int charIndex0 = offset0 - viewStartOffset;
	int charIndex1 = offset1 - viewStartOffset;
	TextHitInfo hit0 = TextHitInfo.afterOffset(charIndex0);
	TextHitInfo hit1 = TextHitInfo.beforeOffset(charIndex1);
	float[] locs = textLayout.getCaretInfo(hit0);
	float x0 = locs[0];
	locs = textLayout.getCaretInfo(hit1);
	float x1 = locs[0];
	return (x1 > x0) ? x1 - x0 : x0 - x1;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
        assert (textLayout.getCharacterCount() == length) : "textLayout.getCharacterCount()=" + // NOI18N
                textLayout.getCharacterCount() + " != length=" + length; // NOI18N
        int viewStartOffset = getStartOffset();
        // If offset is >getEndOffset() use view-end-offset - otherwise it would throw exception from textLayout.getCaretInfo()
	int charIndex = Math.min(offset - viewStartOffset, length);
	TextHitInfo hit = (bias == Position.Bias.Forward)
                ? TextHitInfo.afterOffset(charIndex)
                : TextHitInfo.beforeOffset(charIndex);
	float[] locs = textLayout.getCaretInfo(hit);
        Rectangle2D.Double bounds = ViewUtils.shape2Bounds(alloc);
	bounds.setRect(
                bounds.getX() + locs[0],
                bounds.getY(),
                1, // ?? glyphpainter2 uses 1 but shouldn't be a char width ??
                bounds.getHeight()
        );
        return bounds;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        int startOffset = getStartOffset();
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return startOffset;
        }
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        TextHitInfo hit = textLayout.hitTestChar((float)(x - mutableBounds.getX()), 0);
        biasReturn[0] = hit.isLeadingEdge() ? Position.Bias.Forward : Position.Bias.Backward;
        return startOffset + hit.getInsertionIndex();
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        int startOffset = getStartOffset();
        switch (direction) {
            case View.NORTH:
            case View.SOUTH:
                if (offset != -1) {
                    // Presumably pos is between startOffset and endOffset,
                    // since GlyphView is only one line, we won't contain
                    // the position to the north/south, therefore return -1.
                    return -1;
                }
                DocumentView docView = getDocumentView();
                if (docView != null) {
                    JTextComponent textComponent = docView.getTextComponent();
                    Caret caret = textComponent.getCaret();
                    Point magicPoint;
                    magicPoint = (caret != null) ? caret.getMagicCaretPosition() : null;
                    if (magicPoint == null) {
                        biasRet[0] = Position.Bias.Forward;
                        return startOffset;
                    }
                    return viewToModelChecked((double)magicPoint.x, 0d, alloc, biasRet);
                }
                break;

            case WEST:
                if (offset == -1) {
                    offset = Math.max(0, getEndOffset() - 1);
                } else {
                    offset = Math.max(0, offset - 1);
                }
                break;
            case EAST:
                if (offset == -1) {
                    offset = getStartOffset();
                } else {
                    offset = Math.min(offset + 1, getDocument().getLength());
                }
                break;

            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }
        return offset;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        if (mutableBounds.intersects(clipBounds)) {
            boolean loggable = LOG.isLoggable(Level.FINE);
            DocumentView docView = getDocumentView();
            if (docView != null) {
                Color origColor = g.getColor();
                Font origFont = g.getFont();
                try {
                    // Paint background
                    JTextComponent textComponent = docView.getTextComponent();
                    Color componentBackground = textComponent.getBackground();
                    ViewUtils.applyBackgroundAttributes(attributes, componentBackground, g);
                    if (!componentBackground.equals(g.getColor())) { // Not yet cleared by BasicTextUI.paintBackground()
                        if (loggable) {
                            LOG.fine(getDumpId() + ":paint-bkg: " + ViewUtils.toString(g.getColor()) + // NOI18N
                                    ", bounds=" + ViewUtils.toString(mutableBounds) + '\n'); // NOI18N
                        }
                        // clearRect() uses g.getBackground() color
                        g.fillRect(
                                (int)mutableBounds.getX(),
                                (int)mutableBounds.getY(),
                                (int)mutableBounds.getWidth(),
                                (int)mutableBounds.getHeight()
                        );
                    }

                    // Paint foreground
                    ViewUtils.applyForegroundAttributes(attributes, textComponent.getFont(), textComponent.getForeground(), g);
                    TextLayout textLayout = getTextLayout();
                    if (textLayout == null) {
                        return;
                    }
                    float x = (float) mutableBounds.getX();
                    float y = (float) mutableBounds.getY();
                    float baselineOffset = docView.getDefaultBaselineOffset();
                    if (loggable) {
                        int startOffset = getStartOffset();
                        Document doc = docView.getDocument();
                        CharSequence text = DocumentUtilities.getText(doc).subSequence(startOffset, getEndOffset());
                        // Here it's assumed that 'text' var contains the same content as (possibly cached)
                        // textLayout but if textLayout caching would be broken then they could differ.
                        LOG.fine(getDumpId() + ":paint-txt: \"" + CharSequenceUtilities.debugText(text) + // NOI18N
                                "\", XY["+ x + ";" + y + "(B" + // NOI18N
                                ViewUtils.toStringPrec1(baselineOffset) + // NOI18N
                                ")], color=" + ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                    }
                    // TextLayout is unable to do a partial render
                    textLayout.draw(g, x, y + baselineOffset);
                } finally {
                    g.setFont(origFont);
                    g.setColor(origColor);
                }
            }
        }
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
