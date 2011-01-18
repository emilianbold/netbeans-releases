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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Utilities related to HighlightsView and TextLayout management.
 * <br/>
 * Unfortunately the TextLayout based on AttributedCharacterIterator does not handle
 * correctly italic fonts (at least on Mac it renders background rectangle non-italicized).
 * Therefore child views with foreground that differs from text layout's "global" foreground
 * are rendered by changing graphic's color
 * and clipping the Graphics to textLayout.getVisualBounds() of the part.
 *
 * @author Miloslav Metelka
 */

public class HighlightsViewUtils {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewUtils.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsViewUtils.class.getName());

    private HighlightsViewUtils() {
    }

    /**
     * Find index affected by a change in paragraph view's children.
     *
     * @param boxView non-null box view (paragraph view).
     * @param index >= 0 index where children be added/removed.
     * @param viewCount total view count of the box view. Font of a view at "index"
     *  needs to be examined.
     * @return index from which the children's text layout should be recomputed.
     */
    static <V extends EditorView> int findAffectedLayoutIndex(EditorBoxView<V> boxView,
            VisualUpdate<V> visualUpdate, int index)
    {
        assert (boxView instanceof ParagraphView) : "Not ParagraphView instance"; // NOI18N
        ParagraphView pView = (ParagraphView) boxView;
        DocumentView docView = pView.getDocumentView();
        assert (docView != null) : "docView==null for pView=" + pView; // NOI18N
        TextLayoutCache textLayoutCache = docView.getTextLayoutCache();
        assert (textLayoutCache != null) : "textLayoutCache is null for pView=" + pView; // NOI18N
        boolean inTLCache = textLayoutCache.contains(pView);
        if (inTLCache) {
            visualUpdate.markInCache();
        } else { // Not in cache => return 0
            return 0;
        }
        if (index > 0) {
            Font defaultFont = docView.getTextComponent().getFont();
            EditorView prevView = boxView.getEditorView(index - 1);
            if (!(prevView instanceof HighlightsView)) {
                return index;
            }
            HighlightsView prevHView = (HighlightsView) prevView;
            Object prevLayout = prevHView.layoutRaw();
            // Layouts should be inited (otherwise return earlier above)
            assert (prevLayout != null) : "Null prevLayout"; // NOI18N
            boolean prevRebuild;
            if (prevLayout instanceof TextLayoutPart) {
                prevRebuild = !((TextLayoutPart) prevLayout).isLast();
            } else { // TextLayout instance
                prevRebuild = false;
            }
            int viewCount = boxView.getViewCount();
            if (!prevRebuild && index != viewCount) { // Check font at index
                EditorView view = boxView.getEditorView(index);
                if (view instanceof HighlightsView) {
                    AttributeSet attrs = ((HighlightsView)view).getAttributes();
                    AttributeSet prevAttrs = prevHView.getAttributes();
                    Font font = ViewUtils.getFont(attrs, defaultFont);
                    Font prevFont = ViewUtils.getFont(prevAttrs, defaultFont);
                    // If font that follows at index is the same like the one that precedes index
                    // then the preceding "round" must be recomputed to concat with the replaced view(s)
                    prevRebuild = prevFont.equals(font) &&
                            colorsEqual(foreColor(attrs), foreColor(prevAttrs)) &&
                            strikeThroughEqual(strikeThrough(attrs), strikeThrough(prevAttrs));
                }
            }
            if (prevRebuild) {
                index--;
                prevHView.setLayout(null);
                if (prevLayout instanceof TextLayoutPart) {
                    // Goto first
                    while (((TextLayoutPart)prevLayout).index() != 0) { // Not first yet
                        index--;
                        prevHView = ((HighlightsView)boxView.getEditorView(index));
                        prevLayout = prevHView.layoutRaw();
                        prevHView.setLayout(null);
                    }
                }
            }
        }
        return index;
    }
    
    private static Color foreColor(AttributeSet attrs) {
        return (attrs != null)
                ? (Color) attrs.getAttribute(StyleConstants.Foreground)
                : null;
    }
    
    private static Color validForeColor(AttributeSet attrs, JTextComponent textComponent) {
        Color foreColor = foreColor(attrs);
        if (foreColor == null) {
            foreColor = textComponent.getForeground();
        }
        return foreColor;
    }

    private static Color backColor(AttributeSet attrs) {
        return (attrs != null)
                ? (Color) attrs.getAttribute(StyleConstants.Background)
                : null;
    }
    
    private static Color validBackColor(AttributeSet attrs, JTextComponent textComponent) {
        Color backColor = backColor(attrs);
        if (backColor == null) {
            backColor = textComponent.getBackground();
        }
        return backColor;
    }

    private static boolean colorsEqual(Color color1, Color color2) {
        return (color1 != null)
                ? color1.equals(color2)
                : color2 == null;
    }

    private static Boolean strikeThrough(AttributeSet attrs) {
        return (attrs != null)
                ? (Boolean) attrs.getAttribute(StyleConstants.StrikeThrough)
                : null;
    }

    private static boolean strikeThroughEqual(Boolean strikeThrough1, Boolean strikeThrough2) {
        return (strikeThrough1 != null)
                ? strikeThrough1.equals(strikeThrough2)
                : strikeThrough2 == null;
    }

    /**
     * Fix layouts in a range of indices.
     *
     * @param boxView non-null paragraph view.
     * @param startIndex index of first child view to fix.
     * @param endIndex end index of fixing.
     * @param viewCount total view count in boxView.
     * @return possibly increased endIndex if fixing went beyond it (continuous group occurred).
     */
    static <V extends EditorView> void fixLayouts(EditorBoxView<V> boxView, VisualUpdate<V> visualUpdate) {
        ParagraphView pView = (ParagraphView) boxView;
        DocumentView docView = pView.getDocumentView();
        Document doc = docView.getDocument();
        CharSequence docText = DocumentUtilities.getText(doc);
        JTextComponent textComponent = docView.getTextComponent();
        Font defaultFont = textComponent.getFont();
        int groupStartIndex = -1;
        HighlightsView hViewFirst = null;
        Font fontFirst = null;
        Color foreColorFirst = null;
        Color backColorFirst = null;
        int viewCount = boxView.getViewCount();
        if (!visualUpdate.isInCache()) {
            assert (visualUpdate.visualIndex == 0); // Should be set to zero in findAffectedLayoutIndex()
            visualUpdate.endVisualIndex = viewCount;
            // Will become part of the cache
            docView.getTextLayoutCache().activate(pView);
        }
        int i;
        for (i = visualUpdate.visualIndex; i < viewCount; i++) {
            EditorView view = boxView.getEditorView(i);
            if (view instanceof HighlightsView) {
                AttributeSet attrs = view.getAttributes();
                Font font = ViewUtils.getFont(attrs, defaultFont);
                docView.notifyFontUse(font); // Notify that this font is being used (possibly update line height)
                Color foreColor = validForeColor(attrs, textComponent);
                Color backColor = validBackColor(attrs, textComponent);
                
                assert (font != null) : "Null font";
                if (hViewFirst == null) { // First hView
                    groupStartIndex = i;
                    hViewFirst = (HighlightsView) view;
                    fontFirst = font;
                    foreColorFirst = foreColor;
                    backColorFirst = backColor;
                } else { // Inside a group
                    if (!font.equals(fontFirst)) {
                        // [TODO] Possibly check for a different foreground color
                        // since textLayout.draw() only works for whole layout
                        // so it may be more efficient to break and create new text layout.
//                            if (!foreColorsEqual(foreColor, foreColorFirst)) {
//                                break;
//                            }
                        fixLayoutViewGroup(boxView, docView, groupStartIndex, i, hViewFirst,
                                docText, fontFirst, foreColorFirst, backColorFirst);
                        // Create next group
                        groupStartIndex = i;
                        hViewFirst = (HighlightsView) view;
                        fontFirst = font;
                        foreColorFirst = foreColor;
                        backColorFirst = backColor;
                    } // Otherwise continue the group
                }
            } else { // Not HighlightsView => End possible group
                if (hViewFirst != null) {
                    fixLayoutViewGroup(boxView, docView, groupStartIndex, i, hViewFirst,
                            docText, fontFirst, foreColorFirst, backColorFirst);
                    groupStartIndex = -1;
                    hViewFirst = null;
                }
            }
            
            // Check if there's nothing "opened" at endIndex.
            if (i >= visualUpdate.endVisualIndex && hViewFirst == null) {
                // Check that existing layout in the next existing view is not "opened"
                // i.e. the next child view must not be a textlayoutpart with index != 0.
                Object layout;
                if (i + 1 >= viewCount ||
                        !((view = boxView.getEditorView(i + 1)) instanceof HighlightsView) ||
                        !((layout = ((HighlightsView)view).layoutRaw()) instanceof TextLayoutPart) ||
                        ((TextLayoutPart)layout).index() == 0)
                {
                    visualUpdate.endVisualIndex = i;
                    return;
                }
            }
        }
        if (hViewFirst != null) { // Check possible group till viewCount
            assert (i == viewCount);
            fixLayoutViewGroup(boxView, docView, groupStartIndex, i, hViewFirst,
                    docText, fontFirst, foreColorFirst, backColorFirst);
        }
        visualUpdate.endVisualIndex = viewCount;
    }
    
    private static void fixLayoutViewGroup(EditorBoxView boxView, DocumentView docView,
            int groupStartIndex, int groupEndIndex, HighlightsView hViewFirst,
            CharSequence docText, Font fontFirst, Color foreColorFirst, Color backColorFirst)
    {
        int startOffset = hViewFirst.getStartOffset();
        int groupLength = groupEndIndex - groupStartIndex;
        assert (groupLength > 0) : "groupLength=" + groupLength; // NOI18N
        int textLength = (groupLength == 1)
                ? hViewFirst.getLength()
                : boxView.getEditorView(groupEndIndex - 1).getEndOffset() - startOffset;
        String text = docText.subSequence(startOffset, startOffset + textLength).toString();        
        TextLayout textLayout = docView.createTextLayout(text, fontFirst);

        if (groupLength == 1) { // Construct TextLayout
            hViewFirst.setLayout(textLayout);
        } else { // Text layout wrapper
            TextLayoutWrapper wrapper = new TextLayoutWrapper(textLayout, groupLength,
                    foreColorFirst, backColorFirst);
            TextLayoutPart textLayoutPart = new TextLayoutPart(wrapper, 0, 0, 0f, null, null);
            hViewFirst.setLayout(textLayoutPart);
            int len = hViewFirst.getLength();
            JTextComponent textComponent = docView.getTextComponent();
            for (int i = groupStartIndex + 1; i < groupEndIndex; i++) {
                HighlightsView view = (HighlightsView) boxView.getEditorView(i);
                AttributeSet attrs = view.getAttributes();
                Color foreColor = validForeColor(attrs, textComponent);
                if (colorsEqual(foreColor, foreColorFirst)) {
                    foreColor = null;
                }
                Color backColor = validBackColor(attrs, textComponent);
                if (colorsEqual(backColor, backColorFirst)) {
                    backColor = null;
                }
                float x = TextLayoutUtils.index2X(textLayout, len);
                textLayoutPart = new TextLayoutPart(wrapper, i - groupStartIndex, len, x,
                        foreColor, backColor);
                view.setLayout(textLayoutPart);
                len += view.getLength();
            }
        }
    }

    static String findLayoutIntegrityError(EditorBoxView boxView) {
        String err = null;
        int viewCount = boxView.getViewCount();
        int partCount = -1;
        int lastPartIndex = -1;
        for (int i = 0; i < viewCount; i++) {
            EditorView child = boxView.getEditorView(i);
            if (child instanceof HighlightsView) {
                Object layout = ((HighlightsView) child).layoutRaw();
                if (layout instanceof TextLayoutPart) {
                    TextLayoutPart part = (TextLayoutPart) layout;
                    if (partCount != -1) { // Member
                        lastPartIndex++;
                        if (part.index() != lastPartIndex) {
                            err = "part.index()=" + part.index() + " != (lastPartIndex+1)=" + // NOI18N
                                    lastPartIndex;
                        }
                        if (lastPartIndex == part.viewCount() - 1) {
                            partCount = -1;
                        }
                    } else { // partCount == -1
                        partCount = part.viewCount();
                        if (partCount <= 1) {
                            err = "partCount=" + partCount + " <= 1"; // NOI18N
                        }
                        if (err == null && part.index() != 0) {
                            err = "part.index()=" + part.index() + " != 0"; // NOI18N
                        }
                        lastPartIndex = 0;
                    }
                } else { // Non-TextLayoutPart
                    if (partCount != -1) {
                        err = "HV: Unterminated layout parts: partCount=" + partCount; // NOI18N
                    }
                }
            } else {
                if (partCount != -1) {
                    err = "Non-HV: Unterminated layout parts: partCount=" + partCount; // NOI18N
                }
            }
            if (err != null) {
                err = "=" + boxView.getDumpId() + "[" + i + "]=" + child.getDumpId() + ": " + err; // NOI18N
                break;
            }
        }
        return err;
    }
    
    static Shape indexToView(TextLayoutPart part,
             int index, Position.Bias bias, int maxIndex, Shape alloc)
    {
        return indexToView(part.textLayout(), TextLayoutUtils.textLayoutBounds(part, alloc),
                index, bias, maxIndex, alloc);
    }

    static Shape indexToView(TextLayout textLayout, Rectangle2D textLayoutBounds,
             int index, Position.Bias bias, int maxIndex, Shape alloc)
    {
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
        assert (textLayout.getCharacterCount() >= maxIndex) : "textLayout.getCharacterCount()=" + // NOI18N
                textLayout.getCharacterCount() + " < maxIndex=" + maxIndex; // NOI18N
        // If offset is >getEndOffset() use view-end-offset - otherwise it would throw exception from textLayout.getCaretInfo()
	int charIndex = Math.min(index, maxIndex);
        // When e.g. creating fold-preview the offset can be < startOffset
        charIndex = Math.max(charIndex, 0);
        Shape ret;
        if (charIndex < maxIndex) {
            TextHitInfo startHit;
            TextHitInfo endHit;
            if (bias == Position.Bias.Forward) {
                startHit = TextHitInfo.leading(charIndex);
                endHit = TextHitInfo.trailing(charIndex);
            } else { // backward bias
                startHit = TextHitInfo.trailing(charIndex - 1);
                endHit = TextHitInfo.trailing(charIndex);
            }
            if (textLayoutBounds == null) {
                textLayoutBounds = ViewUtils.shapeAsRect(alloc);
            }
            ret = TextLayoutUtils.getRealAlloc(textLayout, textLayoutBounds, startHit, endHit);
        } else { // index == maxIndex
            Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
            mutableBounds.setRect(
                    mutableBounds.getX() + TextLayoutUtils.getWidth(textLayout),
                    mutableBounds.getY(),
                    1,
                    mutableBounds.getHeight());
            ret = mutableBounds;
        }
        return ret;
    }

    static int viewToIndex(TextLayoutPart part, double x, Shape alloc, Position.Bias[] biasReturn) {
        return viewToIndex(part.textLayout(), x, TextLayoutUtils.textLayoutBounds(part, alloc), biasReturn);
    }

    static int viewToIndex(TextLayout textLayout, double x, Shape alloc, Position.Bias[] biasReturn) {
        Rectangle2D bounds = ViewUtils.shapeAsRect(alloc);
        TextHitInfo hitInfo = x2Index(textLayout, (float)(x - bounds.getX()));
        if (biasReturn != null) {
            biasReturn[0] = hitInfo.isLeadingEdge() ? Position.Bias.Forward : Position.Bias.Backward;
        }
        return hitInfo.getInsertionIndex();
    }

    static TextHitInfo x2Index(TextLayout textLayout, float x) {
        TextHitInfo hit;
        if (x >= textLayout.getAdvance()) {
            hit = TextHitInfo.trailing(textLayout.getCharacterCount());
        } else {
            hit = textLayout.hitTestChar(x, 0);
            // Use forward bias only since BaseCaret and other code is not sensitive to backward bias yet
            if (!hit.isLeadingEdge()) {
                hit = TextHitInfo.leading(hit.getInsertionIndex());
            }
        }
        return hit;
    }
    
    static int getNextVisualPosition(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet,
            TextLayout textLayout, int textLayoutOffset, int viewStartOffset, int viewLength, DocumentView docView)
    {
        int retOffset = -1;
        boolean viewIsLeftToRight = false; // [TODO] AbstractDocument.isLeftToRight();
        TextHitInfo currentHit, nextHit;
        switch (direction) {
            case View.EAST:
                if (offset == -1) { // Entering view from the left.
                    if (viewIsLeftToRight) {
                        biasRet[0] = Bias.Forward;
                        return viewStartOffset;
                    } else {
                        biasRet[0] = Bias.Backward;
                        return viewStartOffset + viewLength;
                    }
                } else { // Regular offset
                    if (bias == Bias.Forward) {
                        currentHit = TextHitInfo.afterOffset(offset - viewStartOffset);
                    } else {
                        currentHit = TextHitInfo.beforeOffset(offset - viewStartOffset);
                    }
                    nextHit = textLayout.getNextRightHit(currentHit);
                    if (nextHit != null) {
                        if (viewIsLeftToRight != textLayout.isLeftToRight()) {
                            // If the layout's base direction is different from
                            // this view's run direction, we need to use the weak
                            // carrat. 
                            nextHit = textLayout.getVisualOtherHit(nextHit);
                        }
                        if (nextHit.getInsertionIndex() == viewLength) {
                            biasRet[0] = Bias.Backward;
                        } else {
                            biasRet[0] = Bias.Forward;
                        }
                        retOffset = viewStartOffset + nextHit.getInsertionIndex();
                    } // Leave retOffset == -1
                }
                break;


            case View.WEST:
                if (offset == -1) { // Entering view from the right
                    if (viewIsLeftToRight) {
                        biasRet[0] = Bias.Backward;
                        return viewStartOffset + viewLength;
                    } else {
                        biasRet[0] = Bias.Forward;
                        return viewStartOffset;
                    }
                } else { // Regular offset
                    if (bias == Bias.Forward) {
                        currentHit = TextHitInfo.afterOffset(offset - viewStartOffset);
                    } else {
                        currentHit = TextHitInfo.beforeOffset(offset - viewStartOffset);
                    }
                    nextHit = textLayout.getNextLeftHit(currentHit);
                    if (nextHit != null) {
                        if (viewIsLeftToRight != textLayout.isLeftToRight()) {
                            // If the layout's base direction is different from
                            // this view's run direction, we need to use the weak
                            // carrat. 
                            nextHit = textLayout.getVisualOtherHit(nextHit);
                        }
                        if (nextHit.getInsertionIndex() == viewLength) {
                            // A move to the left from an internal position will
                            // only take us to the endOffset in a right to left run.
                            biasRet[0] = Bias.Backward;
                        } else {
                            biasRet[0] = Bias.Forward;
                        }
                    } // Leave retOffset == -1
                }
                break;

            case View.NORTH:
            case View.SOUTH:
                break; // returns -1
            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }

        return retOffset;
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
                        paintForeground(g, allocBounds, textLayout, attrs, docView);
                        if (LOG.isLoggable(Level.FINEST)) {
                            int startOffset = view.getStartOffset() + shift;
                            Document doc = docView.getDocument();
                            CharSequence text = DocumentUtilities.getText(doc).subSequence(startOffset, startOffset + len);
                            // Here it's assumed that 'text' var contains the same content as (possibly cached)
                            // textLayout but if textLayout caching would be broken then they could differ.
                            LOG.finest(view.getDumpId() + ":paint-txt: \"" + CharSequenceUtilities.debugText(text) + // NOI18N
                                    "\", XY["+ ViewUtils.toStringPrec1(allocBounds.getX()) + ";" +
                                    ViewUtils.toStringPrec1(allocBounds.getY()) + "(B" + // NOI18N
                                    ViewUtils.toStringPrec1(docView.getDefaultAscent()) + // NOI18N
                                    ")], color=" + ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                        }
                    }
                } finally {
                    paintState.restore();
                }
            }
        }
    }

    static void paintBackground(Graphics2D g, Shape alloc, AttributeSet attrs, DocumentView docView) {
        // Paint background
        Rectangle2D allocBounds = ViewUtils.shapeAsRect(alloc);
        JTextComponent textComponent = docView.getTextComponent();
        Color componentBackground = textComponent.getBackground();
        ViewUtils.applyBackgroundAttributes(attrs, componentBackground, g);
        if (!componentBackground.equals(g.getColor())) { // Not yet cleared by BasicTextUI.paintBackground()
            // Fill the alloc (not allocBounds) since it may be non-rectangular
            g.fill(alloc);
        }
        paintBackgroundExtras(g, allocBounds, attrs, docView);
    }
    
    static void partPaintBackground(Graphics2D g, Shape alloc,
            TextLayoutPart textLayoutPart, Shape textLayoutAlloc,
            AttributeSet attrs, DocumentView docView)
    {
        // Paint background
        Rectangle2D allocBounds = ViewUtils.shapeAsRect(alloc);
        JTextComponent textComponent = docView.getTextComponent();
        Color componentBackground = textComponent.getBackground();
        ViewUtils.applyBackgroundAttributes(attrs, componentBackground, g);
        // If part's foreground != null then the background must be forcibly cleared
        // since there was text layout already rendered in "global" foreground color
        // and if the TL would be rendered in part's foreground color by partPaintForeground()
        // later over it then the result would look blur.
        if (!componentBackground.equals(g.getColor()) || textLayoutPart.foreground() != null) {
            // Fill the alloc (not allocBounds) since it may be non-rectangular
            g.fill(alloc);
        }
        paintBackgroundExtras(g, allocBounds, attrs, docView);
    }

    static void paintBackgroundExtras(Graphics2D g, Rectangle2D allocBounds,
            AttributeSet attrs, DocumentView docView)
    {
        // Paint possible underlines
        if (attrs != null) {
            // For now operate with allocBounds although the alloc may be non-rectangular
            int xInt = (int) allocBounds.getX();
            int yInt = (int) allocBounds.getY();
            int endXInt = (int) (allocBounds.getX() + allocBounds.getWidth() - 1);
            int endYInt = (int) (allocBounds.getY() + allocBounds.getHeight() - 1);
            Color leftBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.LeftBorderLineColor);
            Color rightBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.RightBorderLineColor);
            Color topBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.TopBorderLineColor);
            Color bottomBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.BottomBorderLineColor);
            Color textLimitLineColor = docView.getTextLimitLineColor();
            boolean drawTextLimitLine = docView.isTextLimitLineDrawn();
            int textLimitWidth = docView.getTextLimitWidth();
            float defaultCharWidth = docView.getDefaultCharWidth();

            if (drawTextLimitLine && textLimitWidth > 0) { // draw limit line
                int lineX = (int)(textLimitWidth * defaultCharWidth);
                if (lineX >= xInt && lineX <= endXInt){
                    g.setColor(textLimitLineColor);
                    g.drawLine(lineX, yInt, lineX, endYInt);
                }
            }
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
                float ascent = docView.getDefaultAscent();
                Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                float[] underlineAndStrike = docView.getUnderlineAndStrike(font);
                int y = (int)(allocBounds.getY() + underlineAndStrike[0] + ascent + 0.5);
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

            Object underlineValue = attrs.getAttribute(StyleConstants.Underline);
            if (underlineValue != null) {
                Color underlineColor;
                if (underlineValue instanceof Boolean) { // Correct swing-way
                    underlineColor = Boolean.TRUE.equals(underlineValue)
                            ? docView.getTextComponent().getForeground()
                            : null;
                } else { // NB bug - it's Color instance
                    underlineColor = (Color) underlineValue;
                }
                if (underlineColor != null) {
                    g.setColor(underlineColor);
                    Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                    float[] underlineAndStrike = docView.getUnderlineAndStrike(font);
                    g.fillRect(
                            (int) allocBounds.getX(),
                            (int) (allocBounds.getY() + docView.getDefaultAscent() + underlineAndStrike[0]),
                            (int) allocBounds.getWidth(),
                            (int) Math.max(1, Math.round(underlineAndStrike[1]))
                    );
                }
            }
        }
    }

    static void paintForeground(Graphics2D g, Shape alloc,
            TextLayout textLayout, AttributeSet attrs, DocumentView docView)
    {
        JTextComponent textComponent = docView.getTextComponent();
        ViewUtils.applyForegroundAttributes(attrs, textComponent.getFont(),
                textComponent.getForeground(), g);
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        paintTextLayout(g, allocBounds, textLayout, docView);

        if (attrs != null) {
            Object strikeThroughValue = attrs.getAttribute(StyleConstants.StrikeThrough);
            if (strikeThroughValue != null) {
                Color strikeThroughColor;
                if (strikeThroughValue instanceof Boolean) { // Correct swing-way
                    strikeThroughColor = Boolean.TRUE.equals(strikeThroughValue) ? textComponent.getForeground() : null;
                } else { // NB bug - it's Color instance
                    strikeThroughColor = (Color) strikeThroughValue;
                }
                if (strikeThroughColor != null) {
                    g.setColor(strikeThroughColor);
                    Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                    float[] underlineAndStrike = docView.getUnderlineAndStrike(font);
                    g.fillRect(
                            (int) allocBounds.getX(),
                            (int) (allocBounds.getY() + docView.getDefaultAscent() + underlineAndStrike[2]), // strikethrough offset
                            (int) TextLayoutUtils.getWidth(textLayout), // Full width of text layout
                            (int) Math.max(1, Math.round(underlineAndStrike[3])) // strikethrough thickness
                    );
                }
            }
        }
    }

    /**
     * Paint foreground by text layout's part.
     * Expects g.getColor() set to foreground color of the text layout.
     * @param g
     * @param alloc
     * @param docView
     * @param textLayoutPart
     * @param attrs non-null attributes
     */
    static void partPaintForeground(Graphics2D g, Shape alloc,
            TextLayoutPart textLayoutPart, Shape textLayoutAlloc,
            AttributeSet attrs, DocumentView docView)
    {
        if (attrs == null) { // XXX #191257 hotfix
            return;
        }
        JTextComponent textComponent = docView.getTextComponent();
        Color origColor = null;
        try {
            if (textLayoutPart.foreground() != null) {
                origColor = g.getColor();
                // Unfortunately the text layout can only fully paint so limit painting
                // by setting appropriate visual bounds.
                Shape origClip = g.getClip();
                g.clip(alloc); // Update current clip
                g.setColor(textLayoutPart.foreground());
                Rectangle2D textLayoutBounds = ViewUtils.shapeAsRect(textLayoutAlloc);
                TextLayout textLayout = textLayoutPart.textLayout();
                // Both x and ascentedY should already be floor/ceil-ed
                float x = (float) textLayoutBounds.getX();
                float ascentedY = (float) (textLayoutBounds.getY() + docView.getDefaultAscent());
                textLayout.draw(g, x, ascentedY);
    //            g.fill(shape); // Just for testing visual bounds
                g.setClip(origClip);
                // Leave foreground color for possible strike through rendering
            }
            // Text may have extra foreground color

            Object strikeThroughValue;
            if (attrs != null && (strikeThroughValue = attrs.getAttribute(StyleConstants.StrikeThrough)) != null) {
                Color strikeThroughColor = null;
                if (strikeThroughValue instanceof Boolean) { // Correct swing-way
                    if (Boolean.TRUE.equals(strikeThroughValue)) {
                        strikeThroughColor = textLayoutPart.foreground();
                        if (strikeThroughColor == null) {
                            // No extra foreground => default to component's foreground
                            //strikeThroughColor = (Color) attrs.getAttribute(StyleConstants.Foreground);
                            strikeThroughColor = textComponent.getForeground();
                        }
                    }
                } else { // NB bug - it's Color instance
                    strikeThroughColor = (Color) strikeThroughValue;
                }
                if (strikeThroughColor != null) {
                    if (origColor == null) {
                        origColor = g.getColor();
                    }
                    Rectangle2D allocBounds = ViewUtils.shapeAsRect(alloc);
                    g.setColor(strikeThroughColor);
                    Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                    float[] underlineAndStrike = docView.getUnderlineAndStrike(font);
                    g.fillRect(
                            (int) allocBounds.getX(),
                            (int) (allocBounds.getY() + docView.getDefaultAscent() + underlineAndStrike[2]),
                            (int) allocBounds.getWidth(), // Full width of text layout
                            (int) Math.max(1, Math.round(underlineAndStrike[3]))
                    );
                }
            }
        } finally {
            if (origColor == null) {
                g.setColor(origColor);
            }
        }
    }

    static void paintTextLayout(Graphics2D g, Rectangle2D bounds,
            TextLayout textLayout, DocumentView docView)
    {
        float x = (float) bounds.getX();
        float ascentedY = (float) (bounds.getY() + docView.getDefaultAscent());
        // TextLayout is unable to do a partial render
        // Both x and ascentedY should already be floor/ceil-ed
        textLayout.draw(g, x, ascentedY);
    }

    static View breakView(int axis, int breakPartStartOffset, float x, float len,
            HighlightsView fullView, int partShift, int partLength,
            TextLayout textLayout, int textLayoutIndex)
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
                    TextHitInfo hit = TextHitInfo.afterOffset(textLayoutIndex + breakCharIndex);
                    float[] locs = textLayout.getCaretInfo(hit);
                    breakCharIndexX = locs[0];
                } else {
                    breakCharIndexX = 0f;
                }
                TextHitInfo hitInfo = x2Index(textLayout, breakCharIndexX + len);
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
                        ", textLayoutIndex=" + textLayoutIndex + // NOI18N
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

}
