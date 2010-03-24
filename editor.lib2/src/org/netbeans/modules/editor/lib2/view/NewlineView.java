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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;

/**
 * View that spans a newline character at an end of a line element.
 * It spans till end of screen and if there are any background highlights it highlights them.
 *
 * @author Miloslav Metelka
 */

public final class NewlineView extends EditorView {

    /** The default width should incorporate possible width of the wider caret once it blinks at line's end. */
    private static final float DEFAULT_WIDTH = 2f;

    /** Offset of start offset of this view. */
    private int rawOffset; // 24-super + 4 = 28 bytes

    private final AttributeSet attributes;

    public NewlineView(int offset, AttributeSet attributes) {
        super(null);
        this.rawOffset = offset;
        this.attributes = attributes;
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
    public int getStartOffset() {
        ParagraphView parent = (ParagraphView) getParent();
        return (parent != null) ? parent.getViewOffset(rawOffset) : rawOffset;
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public boolean setLength(int length) {
        return false;
    }

    @Override
    public AttributeSet getAttributes() {
        return attributes;
    }

    @Override
    public float getPreferredSpan(int axis) {
        DocumentView documentView = getDocumentView();
        if (axis == View.X_AXIS) {
            return (documentView != null)
                    ? (documentView.isShowNonprintingCharacters()
                        ? documentView.getNewlineCharTextLayout().getAdvance()
                        : DEFAULT_WIDTH)
                    : DEFAULT_WIDTH;
        } else {
            return (documentView != null) ? documentView.getDefaultLineHeight() : 1;
        }
    }

    /*private*/ ParagraphView getParagraphView() {
        return (ParagraphView) getParent();
    }

    /*private*/ DocumentView getDocumentView() {
        ParagraphView paragraphView = getParagraphView();
        return (paragraphView != null) ? paragraphView.getDocumentView() : null;
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        mutableBounds.width = getPreferredSpan(X_AXIS);
        return mutableBounds;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        DocumentView docView = getDocumentView();
        if (docView != null) {
            // Extend the bounds so that the view renders its attributes till docView's end
            Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
            mutableBounds.width = docView.getWidth() - mutableBounds.x;
            if (mutableBounds.intersects(clipBounds)) {
                PaintState paintState = PaintState.save(g);
                try {
                    // Paint background
                    JTextComponent textComponent = docView.getTextComponent();
                    Color componentBackground = textComponent.getBackground();
                    ViewUtils.applyBackgroundAttributes(attributes, componentBackground, g);
                    if (!componentBackground.equals(g.getColor())) {
                        ViewUtils.fillRect(g, mutableBounds);
                    }

                    // Possibly paint pilcrow sign
                    TextLayout textLayout;
                    if (docView.isShowNonprintingCharacters() && (textLayout = docView.getNewlineCharTextLayout()) != null) {
                        HighlightsView.paintForeground(g, mutableBounds, docView, textLayout, getAttributes());
                    }
                } finally {
                    paintState.restore();
                }
            }
        }
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        return getStartOffset();
    }

    @Override
    protected String getDumpName() {
        return "NV";
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

}
