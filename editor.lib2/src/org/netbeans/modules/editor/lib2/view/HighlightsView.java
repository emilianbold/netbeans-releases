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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * View with highlights. This is the most used view.
 *
 * @author Miloslav Metelka
 */

public class HighlightsView extends EditorView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsView.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsView.class.getName());

    /** Offset of start offset of this view. */
    private int rawOffset; // 24-super + 4 = 28 bytes

    /** Length of text occupied by this view. */
    private int length; // 28 + 4 = 32 bytes

    /** Attributes for rendering */
    private final AttributeSet attributes; // 32 + 4 = 36 bytes

    /**
     * Either direct TextLayout object or TextLayoutPart or null.
     */
    private Object textLayoutOrPart;

    public HighlightsView(int offset, int length, AttributeSet attributes) {
        super(null);
        assert (length > 0) : "length=" + length + " <= 0"; // NOI18N
        this.rawOffset = offset;
        this.length = length;
        this.attributes = attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        float span = (axis == View.X_AXIS)
            ? Math.abs(TextLayoutUtils.getWidth(layout(), length)) // Could be negative
            : TextLayoutUtils.getHeight(layout());
        return ViewUtils.ceilFractions(span);
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

    /**
     * @return TextLayout instance or TextLayoutPart or null.
     */
    Object layout() {
        if (textLayoutOrPart == null) { // Must init whole row
            getParagraphView().initTextLayouts();
        }
        return textLayoutOrPart;
    }
    
    Object layoutRaw() {
        return layout(); // textLayoutOrPart;
    }

    void setLayout(Object layoutOrPart) {
        this.textLayoutOrPart = layoutOrPart;
    }
    
    TextLayout createPartTextLayout(int shift, int length) {
        DocumentView docView = getDocumentView();
        Document doc = docView.getDocument();
        CharSequence docText = DocumentUtilities.getText(doc);
        int startOffset = getStartOffset();
        String text = docText.subSequence(startOffset + shift, startOffset + shift + length).toString();
        return docView.createTextLayout(text, getAttributes());
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        return modelToViewChecked(offset, alloc, bias, -1);
    }

    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias, int index) {
        Shape ret;
        int relOffset = Math.max(0, offset - getStartOffset());
        Object layout = layout();
        if (layout instanceof TextLayoutPart) {
            TextLayoutPart part = (TextLayoutPart) layout;
            ParagraphView paragraphView = getParagraphView();
            if (paragraphView != null) {
                if (index == -1) {
                    index = paragraphView.getViewIndex(getStartOffset());
                }
                int layoutStartViewIndex = index - part.index();
                Rectangle2D.Double textLayoutBounds = ViewUtils.shape2Bounds(alloc);
                double relX = paragraphView.getViewVisualOffset(index) -
                        paragraphView.getViewVisualOffset(layoutStartViewIndex);
                textLayoutBounds.x -= relX;
                textLayoutBounds.width = part.textLayoutWidth();
                ret = HighlightsViewUtils.indexToView(part.textLayout(), textLayoutBounds,
                        part.offsetShift() + relOffset, bias,
                        part.offsetShift() + getLength(), alloc);
            } else {
                ret = alloc;
            }
        } else { // TextLayoutPart
            TextLayout textLayout = (TextLayout) layout;
            ret = HighlightsViewUtils.indexToView(textLayout, null, relOffset, bias, 
                    getLength(), alloc);
        }
        return ret;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        return viewToModelChecked(x, y, alloc, biasReturn, -1);
    }

    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn, int index) {
        int offset;
        Object layout = layout();
        if (layout instanceof TextLayoutPart) {
            TextLayoutPart part = (TextLayoutPart) layout;
            ParagraphView paragraphView = getParagraphView();
            if (paragraphView != null) {
                if (index == -1) {
                    index = paragraphView.getViewIndex(getStartOffset());
                }
                int layoutStartViewIndex = index - part.index();
                Rectangle2D.Double textLayoutBounds = ViewUtils.shape2Bounds(alloc);
                double relX = paragraphView.getViewVisualOffset(index) -
                        paragraphView.getViewVisualOffset(layoutStartViewIndex);
                textLayoutBounds.x -= relX;
                textLayoutBounds.width = part.textLayoutWidth();
                offset = HighlightsViewUtils.viewToIndex(part.textLayout(), x, textLayoutBounds, biasReturn) +
                        getStartOffset() - part.offsetShift();
            } else {
                offset = getStartOffset();
            }
        } else { // TextLayoutPart
            TextLayout textLayout = (TextLayout) layout;
            offset = HighlightsViewUtils.viewToIndex(textLayout, x, alloc, biasReturn) +
                    getStartOffset();
        }
        return offset;
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        Object layout = layout();
        int startOffset = getStartOffset();
        TextLayout textLayout;
        int textLayoutStartOffset;
        if (layout instanceof TextLayoutPart) {
            TextLayoutPart part = (TextLayoutPart) layout;
            textLayout = part.textLayout();
            textLayoutStartOffset = startOffset - part.offsetShift();
        } else {
            textLayout = (TextLayout) layout;
            textLayoutStartOffset = startOffset;
        }
        return HighlightsViewUtils.getNextVisualPosition(
                offset, bias, alloc, direction, biasRet,
                textLayout, textLayoutStartOffset, startOffset, getLength(), getDocumentView());
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        TextLayout textLayout;
        Object layout = layout();
        if (layout instanceof TextLayoutPart) {
            throw new IllegalStateException("Invalid rendering of layout part"); // NOI18N
        } else { // TextLayout
            textLayout = (TextLayout) layout;
            HighlightsViewUtils.paint(g, alloc, clipBounds, this, textLayout, 0, getLength());
        }
    }
    
    /**
     * Paint extra foreground things such as text in different color
     * (different from the rendered text layout) or strike through.
     * @param g
     * @param alloc precise real allocation of this view.
     * @param clipBounds
     */
    void partPaintForeground(Graphics2D g, Shape alloc, Shape textLayoutAlloc, Rectangle clipBounds) {
        HighlightsViewUtils.partPaintForeground(g, alloc, (TextLayoutPart) layout(),
                textLayoutAlloc, getAttributes(), getDocumentView());
    }
    
    /**
     * Paint extra background things such as background in different color
     * (different from the rendered text layout) or strike through.
     * @param g
     * @param alloc precise real allocation of this view.
     * @param clipBounds
     */
    void partPaintBackground(Graphics2D g, Shape alloc, Shape textLayoutAlloc, Rectangle clipBounds) {
        HighlightsViewUtils.partPaintBackground(g, alloc, (TextLayoutPart) layout(),
                textLayoutAlloc, getAttributes(), getDocumentView());
    }
    
    @Override
    public View breakView(int axis, int offset, float x, float len) {
        TextLayout textLayout;
        int textLayoutIndex;
        Object layout = layout();
        if (layout instanceof TextLayoutPart) {
            TextLayoutPart part = (TextLayoutPart) layout;
            textLayout = part.textLayout();
            textLayoutIndex = part.offsetShift();
        } else { // TextLayout
            textLayout = (TextLayout) layout;
            textLayoutIndex = 0;
        }
        View part = HighlightsViewUtils.breakView(axis, offset, x, len, this, 0, getLength(),
                textLayout, textLayoutIndex);
        return (part != null) ? part : this;
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
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, int importantChildIndex) {
        super.appendViewInfo(sb, indent, importantChildIndex);
        sb.append(" L=");
        if (textLayoutOrPart == null) {
            sb.append("<NULL>");
        } else if (textLayoutOrPart instanceof TextLayoutPart) {
            TextLayoutPart part = (TextLayoutPart) textLayoutOrPart;
            sb.append(part.toStringShort());
        } else {
            sb.append("TL");
            sb.append(TextLayoutUtils.toStringShort((TextLayout) textLayoutOrPart));
        }
        return sb;
    }
    
    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

}
