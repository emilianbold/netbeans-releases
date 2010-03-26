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

package org.netbeans.modules.editor.fold;

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
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.modules.editor.lib2.view.EditorView;
import org.netbeans.modules.editor.lib2.view.TextLayoutView;
import org.netbeans.modules.editor.lib2.view.ViewUtils;

/**
 * View with highlights. This is the most used view.
 *
 * @author Miloslav Metelka
 */

public class FoldView extends EditorView implements TextLayoutView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(FoldView.class.getName());

    /** Offset of start offset of this view. */
    private int rawOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    private final JTextComponent textComponent; // 32 + 4 = 36 bytes

    private final Fold fold; // 36 + 4 = 40 bytes

    public FoldView(JTextComponent textComponent, Fold fold) {
        super(null);
        int offset = fold.getStartOffset();
        int len = fold.getEndOffset() - offset;
        assert (len > 0) : "length=" + len + " <= 0"; // NOI18N
        this.rawOffset = offset;
        this.length = len;
        this.textComponent = textComponent;
        this.fold = fold;
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
        return false; // Recreate upon modification
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
        return null;
    }

    @Override
    public TextLayout createTextLayout() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        FontRenderContext frc = parent.getFontRenderContext();
        if (frc == null) {
            return null;
        }
        Font font = textComponent.getFont();
        String text = fold.getDescription();
        TextLayout textLayout = new TextLayout(text, font, frc);
        return textLayout;
    }

    private TextLayout getTextLayout() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getTextLayout(this) : null;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        TextLayout textLayout = getTextLayout();
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
	TextHitInfo hit = TextHitInfo.afterOffset(0);
	float[] locs = textLayout.getCaretInfo(hit);
        Rectangle2D.Double bounds = ViewUtils.shape2Bounds(alloc);
	bounds.setRect(
                bounds.getX(),
                bounds.getY(),
                locs[0],
                bounds.getHeight()
        );
        return bounds;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        int startOffset = getStartOffset();
        return startOffset;
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
                Caret caret = textComponent.getCaret();
                Point magicPoint;
                magicPoint = (caret != null) ? caret.getMagicCaretPosition() : null;
                if (magicPoint == null) {
                    biasRet[0] = Position.Bias.Forward;
                    return startOffset;
                }
                TextLayout textLayout = getTextLayout();
                if (textLayout != null) {
                    return viewToModelChecked((double) magicPoint.x, 0d, alloc, biasRet);
                }
                break;

            case WEST:
                if (offset == -1) {
                    offset = Math.max(0, startOffset + fold.getDescription().length() - 1);
                } else {
                    offset = Math.max(0, offset - 1);
                }
                break;
            case EAST:
                if (offset == -1) {
                    offset = startOffset;
                } else {
                    Document doc = getDocument();
                    if (doc != null) {
                        offset = Math.min(offset + 1, doc.getLength());
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
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        if (allocBounds.intersects(clipBounds)) {
            Font origFont = g.getFont();
            Color origColor = g.getColor();
            try {
                // Leave component font
                g.setColor(textComponent.getForeground());
                int xInt = (int) allocBounds.getX();
                int yInt = (int) allocBounds.getY();
                int endXInt = (int) (allocBounds.getX() + allocBounds.getWidth() - 1);
                int endYInt = (int) (allocBounds.getY() + allocBounds.getHeight() - 1);
                g.drawRect(xInt, yInt, endXInt - xInt, endYInt - yInt);
                TextLayout textLayout = getTextLayout();
                if (textLayout != null) {
                    float x = (float) allocBounds.getX();
                    float y = (float) allocBounds.getY();
                    textLayout.draw(g, x, y + textLayout.getAscent());
                }
            } finally {
                g.setColor(origColor);
                g.setFont(origFont);
            }
        }
    }

    @Override
    protected String getDumpName() {
        return "FV";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

}
