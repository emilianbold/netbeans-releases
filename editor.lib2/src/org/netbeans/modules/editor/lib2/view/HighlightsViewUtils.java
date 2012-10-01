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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

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
        if (foreColor == null) {
            foreColor = Color.BLACK;
        }
        return foreColor;
    }

    static Shape indexToView(TextLayout textLayout, Rectangle2D textLayoutBounds,
             int index, Position.Bias bias, int maxIndex, Shape alloc)
    {
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
        assert (maxIndex <= textLayout.getCharacterCount()) : "textLayout.getCharacterCount()=" + // NOI18N
                textLayout.getCharacterCount() + " < maxIndex=" + maxIndex; // NOI18N
        // If offset is >getEndOffset() use view-end-offset - otherwise it would throw exception from textLayout.getCaretInfo()
	int charIndex = Math.min(index, maxIndex);
        // When e.g. creating fold-preview the offset can be < startOffset
        charIndex = Math.max(charIndex, 0);
        TextHitInfo startHit;
        TextHitInfo endHit;
        if (bias == Position.Bias.Forward) {
            startHit = TextHitInfo.leading(charIndex);
        } else { // backward bias
            startHit = TextHitInfo.trailing(charIndex - 1);
        }
        endHit = (charIndex < maxIndex) ? TextHitInfo.trailing(charIndex) : startHit;
        if (textLayoutBounds == null) {
            textLayoutBounds = ViewUtils.shapeAsRect(alloc);
        }
        return TextLayoutUtils.getRealAlloc(textLayout, textLayoutBounds, startHit, endHit);
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
        hit = textLayout.hitTestChar(x, 0);
        // Use forward bias only since BaseCaret and other code is not sensitive to backward bias yet
        if (!hit.isLeadingEdge()) {
            hit = TextHitInfo.leading(hit.getInsertionIndex());
        }
        return hit;
    }
    
    static double getMagicX(DocumentView docView, EditorView view, int offset, Bias bias, Shape alloc) {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return 0d;
        }
        Caret caret = textComponent.getCaret();
        Point magicCaretPoint = (caret != null) ? caret.getMagicCaretPosition() : null;
        double x;
        if (magicCaretPoint == null) {
            Shape offsetBounds = view.modelToViewChecked(offset, alloc, bias);
            if (offsetBounds == null) {
                x = 0d;
            } else {
                x = offsetBounds.getBounds2D().getX();
            }
        } else {
            x = magicCaretPoint.x;
        }
        return x;
    }

    static int getNextVisualPosition(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet,
            TextLayout textLayout, int textLayoutOffset, int viewStartOffset, int viewLength, DocumentView docView)
    {
        int retOffset = -1;
        biasRet[0] = Bias.Forward; // BaseCaret ignores bias
        TextHitInfo currentHit, nextHit;
        switch (direction) {
            case View.EAST:
                if (offset == -1) { // Entering view from the left.
                    retOffset = viewStartOffset;
                } else { // Regular offset
                    int index = offset - viewStartOffset;
                    if (index >= 0 && index <= viewLength) {
                        currentHit = TextHitInfo.afterOffset(index);
                        nextHit = textLayout.getNextRightHit(currentHit);
                        if (nextHit != null) {
                            retOffset = viewStartOffset + nextHit.getInsertionIndex();
                        } // Leave retOffset == -1
                    } // Leave retOffset == -1
                }
                break;

            case View.WEST:
                if (offset == -1) { // Entering view from the right
                    retOffset = viewStartOffset + viewLength - 1;
                } else { // Regular offset
                    int index = offset - viewStartOffset;
                    if (index >= 0 && index <= viewLength) {
                        currentHit = TextHitInfo.afterOffset(index);
                        nextHit = textLayout.getNextLeftHit(currentHit);
                        if (nextHit != null) {
                            retOffset = viewStartOffset + nextHit.getInsertionIndex();
                        } // Leave retOffset == -1
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

    /**
     * Paint text layout that corresponds fully or partially to the given highlights view.
     *
     * @param g graphics
     * @param textLayoutAlloc
     * @param clipBounds
     * @param docView
     * @param view
     * @param textLayout
     * @param startIndex first index inside textLayout to be rendered.
     * @param endIndex end index inside textLayout to be rendered.
     */
    static void paintHiglighted(Graphics2D g, Shape textLayoutAlloc, Rectangle clipBounds,
            DocumentView docView, EditorView view, int viewStartOffset,
            boolean newline, TextLayout textLayout, int textLayoutOffset,
            int startIndex, int endIndex)
    {
        Rectangle2D textLayoutRect = ViewUtils.shape2Bounds(textLayoutAlloc);
        PaintState paintState = PaintState.save(g);
        Shape origClip = g.getClip();
        try {
            // Render individual parts of the text layout with the correct highlighting
            // 1. Only the whole text layout can be rendered by its TL.draw() (in a single color)
            // 2. when TL is rendered in a color (e.g. black) it cannot be over-rendered
            //    in another color (for custom foreground) since it would look blurry.
            //
            // Therefore do the rendering in the following way:
            // 1. Collect bounds of each part (use map to get less TL.draw() invocations).
            // 2. Render background of part's bounds by clipping to collected shape.
            // 3. Render part's text in custom color by clipping of rendering of whole TL to collected shape.
            //
            JTextComponent textComponent = docView.getTextComponent();
            HighlightsSequence highlights = docView.getPaintHighlights(view,
                    textLayoutOffset + startIndex - viewStartOffset);
            TextLayout renderTextLayout = textLayout;
            Boolean showNonPrintingChars = null;
            boolean log = LOG.isLoggable(Level.FINEST);
            // For regular textLayout do aggregation of rendered parts into compound area
            // to decrease TL.draw() invocations.
            // Unfortunately using java.awt.geom.Area on Mac OSX results in white horizontal lines
            // throughout the rendered text (when dragging vertical scrollbar slowly).
            // Therefore the aggregation optimization is currently disabled.
            Map<Color,Area> foreColor2Area = (false && textLayout != null && !newline)
                    ? new HashMap<Color,Area>()
                    : null;
            boolean done = false;
            while (!done && highlights.moveNext()) {
                int hiStartOffset = highlights.getStartOffset();
                int hiEndOffset = Math.min(highlights.getEndOffset(), textLayoutOffset + endIndex);
                if (hiEndOffset <= hiStartOffset) {
                    break;
                }
                // For visualized TABs it is necessary to render each TAB char text layout individually
                int renderEndOffset;
                do {
                    renderEndOffset = hiEndOffset;
                    AttributeSet attrs = highlights.getAttributes();
                    Shape renderPartAlloc;
                    if (textLayout != null) {
                        TextHitInfo startHit = TextHitInfo.leading(hiStartOffset - textLayoutOffset);
                        TextHitInfo endHit = TextHitInfo.leading(renderEndOffset - textLayoutOffset);
                        renderPartAlloc = TextLayoutUtils.getRealAlloc(textLayout, textLayoutRect, startHit, endHit);
                        if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINER)) {
                            ViewHierarchyImpl.PAINT_LOG.finer("      Fragment: hit<" + // NOI18N
                                    startHit.getCharIndex() + "," + endHit.getCharIndex() + // NOI18N
                                    ">, text='" + DocumentUtilities.getText(docView.getDocument()).subSequence( // NOI18N
                                    hiStartOffset, renderEndOffset) + "', fAlloc=" + // NOI18N
                                    ViewUtils.toString(renderPartAlloc.getBounds()) + '\n');
                        }
                    } else { // No text layout => Newline or TAB(s)
                        if (showNonPrintingChars == null) {
                            showNonPrintingChars = docView.op.isNonPrintableCharactersVisible();
                        }
                        if (newline) {
                            renderPartAlloc = textLayoutAlloc; // Single '\n' => render whole alloc
                            renderTextLayout = showNonPrintingChars
                                    ? docView.op.getNewlineCharTextLayout()
                                    : null;
                        } else { // It's TAB(s)
                            if (showNonPrintingChars) {
                                // Render just single TAB char's text layout
                                renderEndOffset = hiStartOffset + 1;
                            }
                            Shape renderStartAlloc = view.modelToViewChecked(hiStartOffset, textLayoutRect, Bias.Forward);
                            Rectangle2D.Double r = ViewUtils.shape2Bounds(renderStartAlloc);
                            // Tab view should support doing modelToView() for its ending offset
                            Shape renderEndAlloc = view.modelToViewChecked(renderEndOffset, textLayoutRect, Bias.Forward);
                            Rectangle2D rEnd = ViewUtils.shapeAsRect(renderEndAlloc);
                            r.width = rEnd.getX() - r.x;
                            renderPartAlloc = r;
                            if (showNonPrintingChars) {
                                renderTextLayout = docView.op.getTabCharTextLayout(r.width);
                            } else {
                                renderTextLayout = null;
                            }
                        }
                    }
                    Rectangle2D renderPartBounds = renderPartAlloc.getBounds();
                    boolean hitsClip = (clipBounds == null) || renderPartAlloc.intersects(clipBounds);
                    if (hitsClip) {
                        // First render background and background related highlights
                        // Do not g.clip() before background is filled since otherwise there would be
                        // painting artifacts for italic fonts (one-pixel slanting lines) at certain positions.
                        fillBackground(g, renderPartAlloc, attrs, textComponent);
                        // Clip to part's alloc since textLayout.draw() renders fully the whole text layout
                        g.clip(renderPartAlloc);
                        paintBackgroundHighlights(g, renderPartAlloc, attrs, docView);
                        // Render foreground with proper color
                        g.setColor(HighlightsViewUtils.validForeColor(attrs, textComponent));
                        Object strikeThroughValue = (attrs != null)
                                ? attrs.getAttribute(StyleConstants.StrikeThrough)
                                : null;
                        if (renderTextLayout != null) {
                            // Strikethrough must be rendered over the text => do not aggregate in that case
                            if (foreColor2Area != null && strikeThroughValue == null) { // Allow aggregation
                                Area renderArea = new Area(renderPartAlloc);
                                Area compoundArea = foreColor2Area.get(g.getColor());
                                if (compoundArea == null) {
                                    compoundArea = renderArea;
                                    foreColor2Area.put(g.getColor(), compoundArea);
                                } else {
                                    compoundArea.add(renderArea);
                                }
    //                            // Check if the path is closed (Area closes unclosed paths automatically)
    //                            if (renderPartAlloc instanceof java.awt.geom.Path2D) {
    //                                java.awt.geom.Path2D path = (java.awt.geom.Path2D) renderPartAlloc;
    //                                float[] coords = new float[6];
    //                                boolean closedPath = true; // Empty path is closed
    //                                for (java.awt.geom.PathIterator pathIt = path.getPathIterator(null);
    //                                        !pathIt.isDone(); pathIt.next())
    //                                {
    //                                    int type = pathIt.currentSegment(coords);
    //                                    closedPath = (type == java.awt.geom.PathIterator.SEG_CLOSE);
    //                                }
    //                                if (!closedPath) {
    //                                    System.err.println("Unclosed PATH!! " + path);
    //                                }
    //                            }

                            } else { // Aggregation not done => regular painting
                                if (renderTextLayout != null) {
                                    paintTextLayout(g, textLayoutRect, renderTextLayout, docView);
                                }
                            }
                        }
                        if (strikeThroughValue != null) {
                            paintStrikeThrough(g, textLayoutRect, strikeThroughValue, attrs, docView);
                        }
                        g.setClip(origClip);
                        if (log) {
                            Document doc = docView.getDocument();
                            CharSequence text = DocumentUtilities.getText(doc).subSequence(hiStartOffset, hiEndOffset);
                            // Here it's assumed that 'text' var contains the same content as (possibly cached)
                            // textLayout but if textLayout caching would be broken then they could differ.
                            LOG.finest(view.getDumpId() + ":paint-txt: \"" + CharSequenceUtilities.debugText(text) + // NOI18N
                                    "\", XY[" + ViewUtils.toStringPrec1(textLayoutRect.getX()) + ";"
                                    + ViewUtils.toStringPrec1(textLayoutRect.getY()) + "(B" + // NOI18N
                                    ViewUtils.toStringPrec1(docView.op.getDefaultAscent()) + // NOI18N
                                    ")], color=" + ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                        }

                    } else { // Part does not hit clip
                        if (clipBounds != null && (renderPartBounds.getX() > clipBounds.getMaxX())) {
                            done = true;
                            break;
                        }
                    }
                    hiStartOffset = renderEndOffset;
                } while (!done && renderEndOffset < hiEndOffset);
            }

            if (foreColor2Area != null) {
                for (Map.Entry<Color, Area> entry : foreColor2Area.entrySet()) {
                    Color foreColor = entry.getKey();
                    g.setColor(foreColor);
                    Area compoundArea = entry.getValue();
                    g.clip(compoundArea);
                    paintTextLayout(g, textLayoutRect, renderTextLayout, docView);
                    g.setClip(origClip);
                }
            }

        } finally {
            g.setClip(origClip);
            paintState.restore();
        }
    }

    static void fillBackground(Graphics2D g, Shape partAlloc, AttributeSet attrs, JTextComponent c) {
        // Render background
        if (ViewUtils.applyBackgroundColor(g, attrs, c)) {
            // Fill the alloc (not allocBounds) since it may be non-rectangular
            g.fill(partAlloc);
        }
    }

    /**
     * Render border lines, underline or wave underline and text limit line (but do not clear background).
     * 
     * @param g
     * @param partAlloc
     * @param attrs
     * @param docView 
     */
    static void paintBackgroundHighlights(Graphics2D g, Shape partAlloc, AttributeSet attrs, DocumentView docView) {
        // Paint background
        Rectangle2D partAllocBounds = ViewUtils.shapeAsRect(partAlloc);
        // Also get integer coords for text limit line and other renderings
        int x = (int) partAllocBounds.getX();
        int y = (int) partAllocBounds.getY();
        int lastX = (int) (Math.ceil(partAllocBounds.getMaxX()) - 1);
        int lastY = (int) (Math.ceil(partAllocBounds.getMaxY()) - 1);
        paintTextLimitLine(g, docView, x, y, lastX, lastY);
        // Paint extra 
        if (attrs != null) {
            Color leftBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.LeftBorderLineColor);
            Color rightBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.RightBorderLineColor);
            Color topBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.TopBorderLineColor);
            Color bottomBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.BottomBorderLineColor);

            // Possibly paint underline
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
                    float[] underlineAndStrike = docView.op.getUnderlineAndStrike(font);
                    g.fillRect(
                            (int) partAllocBounds.getX(),
                            (int) (partAllocBounds.getY() + docView.op.getDefaultAscent() + underlineAndStrike[0]),
                            (int) partAllocBounds.getWidth(),
                            (int) Math.max(1, Math.round(underlineAndStrike[1]))
                    );
                }
            }

            // Possibly paint wave underline
            Color waveUnderlineColor = (Color) attrs.getAttribute(EditorStyleConstants.WaveUnderlineColor);
            if (waveUnderlineColor != null && bottomBorderLineColor == null) { // draw wave underline
                g.setColor(waveUnderlineColor);
                float ascent = docView.op.getDefaultAscent();
                Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                float[] underlineAndStrike = docView.op.getUnderlineAndStrike(font);
                int yU = (int)(partAllocBounds.getY() + underlineAndStrike[0] + ascent + 0.5);
                int wavePixelCount = (int) partAllocBounds.getWidth() + 1;
                if (wavePixelCount > 0) {
                    int[] waveForm = {0, 0, -1, -1};
                    int[] xArray = new int[wavePixelCount];
                    int[] yArray = new int[wavePixelCount];

                    int waveFormIndex = x % 4;
                    for (int i = 0; i < wavePixelCount; i++) {
                        xArray[i] = x + i;
                        yArray[i] = yU + waveForm[waveFormIndex];
                        waveFormIndex = (++waveFormIndex) & 3;
                    }
                    g.drawPolyline(xArray, yArray, wavePixelCount - 1);
                }
            }

            // Possibly paint an extra framing (e.g. for code templates)
            if (leftBorderLineColor != null) {
                g.setColor(leftBorderLineColor);
                g.drawLine(x, y, x, lastY);
            }
            if (rightBorderLineColor != null) {
                g.setColor(rightBorderLineColor);
                g.drawLine(lastX, y, lastX, lastY);
            }
            if (topBorderLineColor != null) {
                g.setColor(topBorderLineColor);
                g.drawLine(x, y, lastX, y);
            }
            if (bottomBorderLineColor != null) {
                g.setColor(bottomBorderLineColor);
                g.drawLine(x, lastY, lastX, lastY);
            }
        }
    }
    
    static void paintTextLimitLine(Graphics2D g, DocumentView docView, int x, int y, int lastX, int lastY) {
        int textLimitLineX = docView.op.getTextLimitLineX();
        if (textLimitLineX > 0 && textLimitLineX >= x && textLimitLineX <= lastX) {
            g.setColor(docView.op.getTextLimitLineColor());
            g.drawLine(textLimitLineX, y, textLimitLineX, lastY);
        }
    }

    /**
     * Paint strike-through line for a font currently set to the graphics
     * with the color currently set to the graphics.
     * <br/>
     * It's assumed that the clipping is set appropriately because the method
     * renders whole textLayoutAlloc with the strike-through.
     *
     * @param g
     * @param textLayoutBounds
     * @param strikeThroughValue non-null value for StyleConstants.StrikeThrough attribute in attrs
     * @param attrs non-null attrs
     * @param docView
     */
    static void paintStrikeThrough(Graphics2D g, Rectangle2D textLayoutBounds,
            Object strikeThroughValue, AttributeSet attrs, DocumentView docView)
    {
        Color strikeThroughColor;
        if (strikeThroughValue instanceof Boolean) { // Correct swing-way
            JTextComponent c = docView.getTextComponent();
            strikeThroughColor = Boolean.TRUE.equals(strikeThroughValue) ? g.getColor() : null;
        } else { // NB bug - it's Color instance
            strikeThroughColor = (Color) strikeThroughValue;
        }
        if (strikeThroughColor != null) {
            Color origColor = g.getColor();
            try {
                g.setColor(strikeThroughColor);
                Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                float[] underlineAndStrike = docView.op.getUnderlineAndStrike(font);
                g.fillRect(
                        (int) textLayoutBounds.getX(),
                        (int) (textLayoutBounds.getY() + docView.op.getDefaultAscent() + underlineAndStrike[2]), // strikethrough offset
                        (int) textLayoutBounds.getWidth(),
                        (int) Math.max(1, Math.round(underlineAndStrike[3])) // strikethrough thickness
                );
            } finally {
                g.setColor(origColor);
            }
        }
    }

    static void paintTextLayout(Graphics2D g, Rectangle2D textLayoutBounds,
            TextLayout textLayout, DocumentView docView)
    {
        float x = (float) textLayoutBounds.getX();
        float ascentedY = (float) (textLayoutBounds.getY() + docView.op.getDefaultAscent());
        // TextLayout is unable to do a partial render
        // Both x and ascentedY should already be floor/ceil-ed
        textLayout.draw(g, x, ascentedY);
    }

    static View breakView(int axis, int breakPartStartOffset, float x, float len,
            HighlightsView fullView, int partShift, int partLength, TextLayout partTextLayout)
    {
        if (axis == View.X_AXIS) {
            DocumentView docView = fullView.getDocumentView();
            // [TODO] Should check for RTL text
            assert (partTextLayout != null) : "Null partTextLayout";
            if (docView != null && partLength > 1) {
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
                    TextHitInfo hit = TextHitInfo.leading(breakCharIndex);
                    float[] locs = partTextLayout.getCaretInfo(hit);
                    breakCharIndexX = locs[0];
                } else {
                    breakCharIndexX = 0f;
                }
                TextHitInfo hitInfo = x2Index(partTextLayout, breakCharIndexX + len);
                // Check that the width is not too wide
                float[] locs = partTextLayout.getCaretInfo(hitInfo);
                float endX = locs[0];
                if (endX - breakCharIndexX > len) {
                    if (hitInfo.getCharIndex() > 0) {
                        hitInfo = TextHitInfo.leading(hitInfo.getCharIndex() - 1);
                    }
                }

                // Now perform corrections if wrapping at word boundaries is required
                // Currently a simple impl that checks adjacent char(s) in backward direction
                // is used. Consider BreakIterator etc. if requested.
                // If break is inside a word then check for word boundary in backward direction.
                // If none is found then go forward to find a word break if possible.
                int breakPartEndOffset = partStartOffset + hitInfo.getCharIndex();
                if (breakPartEndOffset > breakPartStartOffset) {
                    if (docView.op.getLineWrapType() == LineWrapType.WORD_BOUND) {
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
                }

                // Length must be > 0; BTW TextLayout can't be constructed with empty string.
                boolean breakFailed = (breakPartEndOffset - breakPartStartOffset == 0) ||
                        (breakPartEndOffset - breakPartStartOffset >= partLength);
//                if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
//                    ViewHierarchyImpl.BUILD_LOG.fine("HV.breakView(): <"  + partStartOffset + // NOI18N
//                            "," + (partStartOffset+partLength) + // NOI18N
//                        "> => <" + breakPartStartOffset + "," + (partStartOffset+breakPartEndOffset) + // NOI18N
//                        ">, x=" + x + ", len=" + len + // NOI18N
//                        ", charIndexX=" + breakCharIndexX + "\n"); // NOI18N
//                }
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
