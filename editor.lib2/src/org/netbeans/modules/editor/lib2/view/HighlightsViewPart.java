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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;

/**
 * Part of view with highlights.
 *
 * @author Miloslav Metelka
 */

public final class HighlightsViewPart extends EditorView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewPart.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsViewPart.class.getName());

    private HighlightsView fullView;

    /** Shift of start of this view relative to HighlightsView. */
    private int shift;

    private int length;

    /**
     * Use dedicated text layout since measurements may differ for a part of original text.
     */
    private TextLayout partTextLayout;

    public HighlightsViewPart(HighlightsView fullView, int shift, int length) {
        super(null);
        int totalLength = fullView.getLength();
        if (shift < 0 || length < 0 || shift + length > totalLength) {
            throw new IllegalArgumentException("shift=" + shift + ", length=" + length + // NOI18N
                    ", totalLength=" + totalLength); // NOI18N
        }
        this.fullView = fullView;
        this.shift = shift;
        this.length = length;
        setParent(fullView.getParent());
    }

    @Override
    public void setParent(View view) {
        super.setParent(view);
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
        return 0;
    }

    @Override
    public void setRawOffset(int rawOffset) {
        throw new IllegalStateException();
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean setLength(int length) {
        throw new IllegalStateException();
    }

    @Override
    public int getStartOffset() {
        return fullView.getStartOffset() + shift;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public Document getDocument() {
        return fullView.getDocument();
    }

    @Override
    public AttributeSet getAttributes() {
        return fullView.getAttributes();
    }

    ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    TextLayout getTextLayout() {
        if (partTextLayout == null) {
            partTextLayout = fullView.createTextLayout(shift, getLength());
        }
        return partTextLayout;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        return HighlightsView.modelToViewChecked(offset, alloc, bias, getTextLayout(), getStartOffset(), getLength());
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        return HighlightsView.viewToModelChecked(x, y, alloc, biasReturn, getTextLayout(), getStartOffset());
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        return HighlightsView.getNextVisualPositionFromChecked(offset, bias, alloc, direction, biasRet,
                getTextLayout(), getStartOffset(), getLength(), getDocumentView());
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        HighlightsView.paint(g, alloc, clipBounds, fullView, getTextLayout(), shift, getLength());
    }

    @Override
    public View breakView(int axis, int offset, float x, float len) {
        View part = HighlightsView.breakView(axis, offset, x, len, fullView, shift, getLength(), getTextLayout());
        return (part != null) ? part : this;
    }

    @Override
    public View createFragment(int p0, int p1) {
        int startOffset = getStartOffset();
        ViewUtils.checkFragmentBounds(p0, p1, startOffset, getLength());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("HVP.createFragment(" + p0 + "," + p1+ "): <" + startOffset + "," + // NOI18N
                    getEndOffset() + ">\n"); // NOI18N
        }
        return new HighlightsViewPart(fullView, shift + p0 - startOffset, p1 - p0);
    }

    @Override
    protected String getDumpName() {
        return "HVP";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

}
