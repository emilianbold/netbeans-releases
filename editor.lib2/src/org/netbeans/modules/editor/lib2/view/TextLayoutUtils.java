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

import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Utilities related to text layout and TextLayoutPart.
 * 
 * @author Miloslav Metelka
 */
final class TextLayoutUtils {

    private TextLayoutUtils() {
        // NO instances
    }

    public static float getHeight(Object layout) {
        TextLayout textLayout = (layout instanceof TextLayoutPart)
                ? ((TextLayoutPart)layout).textLayout()
                : (TextLayout) layout;
        return getHeight(textLayout);
    }
    
    public static float getHeight(TextLayout textLayout) {
        float height = textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
        // Ceil to whole points since when doing a compound TextLayout and then
        // using TextLayoutUtils.getRealAlloc() with its TL.getVisualHighlightShape() and doing
        // Graphics2D.fill(Shape) on the returned shape then for certain fonts such as
        // Lucida Sans Typewriter size=10 on Ubuntu 10.04 the background is rendered one pixel down for certain lines
        // so there appear white lines inside a selection.
        return (float) Math.ceil(height);
    }
    
    public static float getWidth(Object layout, int textLength) {
        if (layout instanceof TextLayoutPart) {
            TextLayoutPart part = (TextLayoutPart) layout;
            TextLayout textLayout = part.textLayout();
            // Ceil the last part to whole number to prevent horizontal white lines inside selection.
            // The individual parts should be fine since they are computed by getVisualHighlightShape().
            float endX = part.isLast()
                    ? getWidth(textLayout)
                    : index2X(textLayout, part.offsetShift() + textLength);
            return endX - part.xShift();
        } else {
            return getWidth((TextLayout) layout);
        }
    }
    
    public static float getWidth(TextLayout textLayout) {
        // Ceil the width to whole number to prevent horizontal white lines inside selection.
//        float width = (float) Math.ceil(textLayout.getAdvance());
        // Since textLayout.getAdvance() includes some extra blank space for italic fonts
        // we instead use getCaretInfo() which seems to produce more appropriate result.
        float width = (float) Math.ceil(index2X(textLayout, textLayout.getCharacterCount()));
        return width;
    }
    
    public static float index2X(TextLayout textLayout, int index) {
        TextHitInfo hit = TextHitInfo.leading(index);
        float[] info = textLayout.getCaretInfo(hit);
        return info[0];
    }

    public static Rectangle2D.Double textLayoutBounds(TextLayoutPart part, Shape alloc) {
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        allocBounds.x -= part.xShift();
        allocBounds.width = part.textLayoutWidth();
        return allocBounds;
    }

    /**
     * Get real allocation (possibly not rectangular) of this layout part.
     *
     * @param length Total number of characters for which the allocation is computed.
     * @param alloc Allocation given by a parent view.
     * @return
     */
    public static Shape getRealAlloc(TextLayout textLayout, Rectangle2D textLayoutBounds,
            TextHitInfo startHit, TextHitInfo endHit)
    {
        Shape ret;
        if (true && textLayoutBounds.getX() != 0d || textLayoutBounds.getY() != 0d) {
            Rectangle2D.Double zeroBasedBounds = ViewUtils.shape2Bounds(textLayoutBounds);
            zeroBasedBounds.x = 0;
            zeroBasedBounds.y = 0;
            ret = textLayout.getVisualHighlightShape(startHit, endHit, zeroBasedBounds);
            AffineTransform transform = AffineTransform.getTranslateInstance(
                    textLayoutBounds.getX(),
                    textLayoutBounds.getY()
            );
            ret = transform.createTransformedShape(ret);

        } else {
            ret = textLayout.getVisualHighlightShape(startHit, endHit, textLayoutBounds);
        }
        return ret;
    }
    
    public static TextLayoutPart textLayoutPart(EditorBoxView boxView, int index) {
        return (TextLayoutPart) ((HighlightsView)boxView.getEditorView(index)).layout();
    }

    /**
     * @param boxView
     * @param anyPart
     * @param layoutStartViewIndex
     * @param endPartRelIndex
     * @return hit at shift-offset of a part corresponding to relEndIndex
     *  or textLayout.getCharacterCount() if it points right at TextLayoutWrapper.viewCount().
     */
    public static TextHitInfo endHit(EditorBoxView boxView, TextLayoutPart anyPart,
            int layoutStartViewIndex, int endPartRelIndex)
    {
        assert (endPartRelIndex <= anyPart.viewCount());
        int endCharIndex = (endPartRelIndex == anyPart.viewCount())
            ? anyPart.textLayout().getCharacterCount()
            : textLayoutPart(boxView, layoutStartViewIndex + endPartRelIndex).offsetShift();
        return TextHitInfo.leading(endCharIndex);
    }
    
    public static String toStringShort(TextLayout textLayout) {
        return "c[]:" + textLayout.getCharacterCount() + ";W=" + getWidth(textLayout); // NOI18N
    }

    public static String toString(TextLayout textLayout) {
        return toStringShort(textLayout) + "; " + // NOI18N
                textLayout.toString();
    }

}
