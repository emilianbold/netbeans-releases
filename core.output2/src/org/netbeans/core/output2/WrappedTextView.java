/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.core.output2;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import org.openide.util.Exceptions;

/**
 * A custom Swing text View which supports line wrapping.  The default Swing
 * line wrapping code is not appropriate for our purposes - particularly, it
 * will iterate the entire buffer multiple times to determine break positions.
 * Since it would defeat the purpose of using a memory mapped file to have to
 * pull the entire thing into memory every time it's painted or its size should
 * be calculated, we have this class instead.
 * <p>
 * All position/line calculations this view does are based on the integer array
 * of line offsets kept by the writer's Lines object.
 *
 * @author Tim Boudreau
 */
public class WrappedTextView extends View {
    /**
     * The component we will paint
     */
    private JTextComponent comp;
    /**
     * Precalculated number of characters per line
     */
    private int charsPerLine = 80;
    /**
     * Precalculated font descent, used to adjust the bounding rectangle of
     * characters as returned by modelToView. This is added to the y position 
     * of character rectangles in modelToView() so painting the selection 
     * includes the complete character and does not interfere with the line above.
     */
    private int fontDescent = 4;
    /**
     * A scratch Segment object to avoid allocation while painting lines
     */
    private static final Segment SEGMENT = new Segment();
    /**
     * Precalculated width (in pixels) we are to paint into, the end being the wrap point
     */
    private int width = 0;
    /**
     * Flag indicating we need to recalculate metrics before painting
     */
    private boolean changed = true;
    /**
     * Precalculated width of a single character (assumes fixed width font).
     */
    private int charWidth = 12;
    /**
     * Precalculated height of a single character (assumes fixed width font).
     */
    private int charHeight = 7;
    /**
     * A scratchpad int array
     */
    static final int[] ln = new int[3];
    /**
     * Flag indicating that the antialiasing flag is set on the Graphics object.
     * We do a somewhat prettier arrow if it is.
     */
    private boolean aa = false;
    /** set antialiasing hints when it's requested. */
    private static final boolean antialias = Boolean.getBoolean ("swing.aatext") || //NOI18N
                                             "Aqua".equals (UIManager.getLookAndFeel().getID()); // NOI18N

    static final Color arrowColor = new Color (80, 162, 80);

    private static Map hintsMap = null;
    
    @SuppressWarnings("unchecked")
    static final Map getHints() {
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap();
                if (antialias) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    hintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
            }
        }
        return hintsMap;
    }

    public WrappedTextView(Element elem, JTextComponent comp) {
        super(elem);
        this.comp = comp;
    }


    public float getPreferredSpan(int axis) {
        OutputDocument doc = odoc();
        float result = 0;
        if (doc != null) {
            updateWidth();
            switch (axis) {
                case X_AXIS :
                    result = charsPerLine;
                    break;
                case Y_AXIS :
                    result = doc.getLines().getLogicalLineCountIfWrappedAt(charsPerLine) * charHeight + fontDescent;
                    break;
                default :
                    throw new IllegalArgumentException (Integer.toString(axis));
            }
        }
        return result;
    }

    @Override
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    @Override
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    float viewWidth = -1;
    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (viewWidth != width) {
            viewWidth = width;
            updateMetrics();
        }
    }

    void updateMetrics() {
        Font font = comp.getFont();
        FontMetrics fm = comp.getFontMetrics(font);
        charWidth = fm.charWidth('m'); //NOI18N
        charHeight = fm.getHeight();
        fontDescent = fm.getMaxDescent();

        Graphics2D g2d = ((Graphics2D) comp.getGraphics());
        if (g2d != null) {
            aa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING) ==
                    RenderingHints.VALUE_ANTIALIAS_ON;
        }
        updateWidth();
    }

    /**
     * Get the component's document as an instance of OutputDocument, if it
     * is one, returning null if it is not (briefly it will not be after the
     * editor kit has been installed - this is unavoidable).
     *
     * @return An instance of OutputDocument or null.
     */
    private OutputDocument odoc() {
        Document doc = comp.getDocument();
        if (doc instanceof OutputDocument) {
            return (OutputDocument) doc;
        }
        return null;
    }

    private void updateWidth() {
        if (comp.getParent() instanceof JViewport) {
            JViewport jv = (JViewport) comp.getParent();
            width = jv.getExtentSize().width - (aa ? 18 : 17);
        } else {
            width = comp.getWidth() - (aa ? 18 : 17);
        }
        if (width < 0) {
            width = 0;
        }
        charsPerLine = width / charWidth;
    }

    /**
     * Get the left hand margin required for printing line wrap decorations.
     *
     * @return A margin in pixels
     */
    private static int margin() {
        return 9;
    }

    public void paint(Graphics g, Shape allocation) {
        
        ((Graphics2D)g).addRenderingHints(getHints());
        
        comp.getHighlighter().paint(g);
        
        OutputDocument doc = odoc();
        if (doc != null) {
            Rectangle clip = g.getClipBounds();
            clip.y = Math.max (0, clip.y - charHeight);
            clip.height += charHeight * 2;

            int lineCount = doc.getElementCount();
            if (lineCount == 0) {
                return;
            }

            ln[0] = clip.y / charHeight;
            Lines lines = doc.getLines();
            lines.toPhysicalLineIndex(ln, charsPerLine);

            int firstline = ln[0];
            g.setColor (comp.getForeground());
            Segment seg = SwingUtilities.isEventDispatchThread() ? SEGMENT : new Segment();

            int selStart = comp.getSelectionStart();
            int selEnd = comp.getSelectionEnd();
            int y = (clip.y - (clip.y % charHeight) + charHeight);
            int maxVisibleChars = ((clip.height + charHeight - 1) / charHeight) * charsPerLine;
            
            try {
                for (int i = firstline; i < lineCount; i++) {
                    if (y > clip.y + clip.height) {
                        return;
                    }
                    int lineStart = doc.getLineStart(i);
                    int lineEnd = doc.getLineEnd (i);
                    int length = lineEnd - lineStart;
                    if (length == 0) {
                        y += charHeight;
                        continue;
                    }
                    LineInfo info = lines.getLineInfo(i);

                    // get number of logical lines
                    int logicalLines = length <= charsPerLine ? 1 : 
                        (charsPerLine == 0 ? length : (length + charsPerLine - 1) / charsPerLine);
                    
                    // get current (first which we will draw) logical line
                    int currLogicalLine = (i == firstline && logicalLines > 0 && ln[1] > 0 ) ? ln[1] : 0;
                    
                    // shift lineStart to position of first logical line that will be drawn
                    int logLineOffset = currLogicalLine * charsPerLine;
                    lineStart += logLineOffset;
                    
                    // limit number of chars needed by estimation of maximum number of chars we need to repaint
                    length = Math.min(maxVisibleChars, lineEnd - lineStart);
                    
                    // get just small part of document we need (no need to get e.g. whole 10 MB line)
                    doc.getText(lineStart, length, seg);

                    int charpos = 0;
                    int arrowDrawn = currLogicalLine - 1;
                    int x = 0;
                    int remainCharsOnLogicalLine = charsPerLine;
                    for (LineInfo.Segment ls : info.getLineSegments()) {
                        if (ls.getEnd() < logLineOffset) {
                            continue;
                        }
                        g.setColor(ls.getColor());
                        while (charpos < ls.getEnd() - logLineOffset && currLogicalLine < logicalLines) {
                            int lenToDraw = Math.min(remainCharsOnLogicalLine, ls.getEnd() - logLineOffset - charpos);
                            if (lenToDraw > 0) {
                                if (currLogicalLine != logicalLines - 1 && arrowDrawn != currLogicalLine) {
                                    arrowDrawn = currLogicalLine;
                                    drawArrow(g, y, currLogicalLine == logicalLines - 2);
                                }
                                drawText(seg, g, x, y, lineStart, charpos, selStart, lenToDraw, selEnd);
                                if (ls.getListener() != null) {
                                    underline(g, seg, charpos, lenToDraw, x, y);
                                }
                            }
                            charpos += lenToDraw;
                            remainCharsOnLogicalLine -= lenToDraw;
                            x += lenToDraw * charWidth;
                            if (remainCharsOnLogicalLine == 0) {
                                remainCharsOnLogicalLine = charsPerLine;
                                currLogicalLine++;
                                x = 0;
                                y += charHeight;
                                if (y > clip.y + clip.height) {
                                    return;
                                }
                            }
                        }
                    }
                    if (charpos % charsPerLine != 0) {
                        y += charHeight;
                    }
                }
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    /**
     * Draw text
     *
     * @param seg A Segment object containing the text
     * @param g The graphics context
     * @param y The baseline in the graphics context
     * @param lineStart The character position at which the line starts
     * @param charpos The current character position within the segment
     * @param selStart The character index at which the selected range, if any, starts
     * @param lenToDraw The number of characters we'll draw before we're outside the clip rectangle
     * @param selEnd The end of the selected range of text, if any
     */
    private void drawText(Segment seg, Graphics g, int x, int y, int lineStart, int charpos, int selStart, int lenToDraw, int selEnd) {
        if (selStart != selEnd) {
            int realPos = lineStart + charpos;
            int a = Math.max(selStart, realPos);
            int b = Math.min(selEnd, realPos + lenToDraw);
            if (a < b) {
                int start = x + margin() + (a - realPos) * charWidth;
                int len = (b - a) * charWidth;
                Color c = g.getColor();
                g.setColor (comp.getSelectionColor());
                g.fillRect (start, y + fontDescent - charHeight, len, charHeight);
                g.setColor (c);
            }
        }
        g.drawChars(seg.array, charpos, lenToDraw, margin() + x, y);
    }

    private void underline(Graphics g, Segment seg, int charpos, int lenToDraw, int x, int y) {
        int underlineStart = margin() + x;
        FontMetrics fm = g.getFontMetrics();
        int underlineEnd = underlineStart + fm.charsWidth(seg.array, charpos, lenToDraw);
        int underlineShift = fm.getDescent() - 1;
        g.drawLine (underlineStart, y + underlineShift, underlineEnd, y + underlineShift);
    }

    /**
     * Draw the decorations used with wrapped lines.
     *
     * @param g A graphics to paint into
     * @param y The y coordinate of the line as a font baseline position
     */
    private void drawArrow (Graphics g, int y, boolean drawHead) {
        Color c = g.getColor();
        g.setColor (arrowColor());

        int w = width + 15;
        y+=2;

        int rpos = aa ? 8 : 4;
        if (aa) {
            g.drawArc(w - rpos, y - (charHeight / 2), rpos + 1, charHeight, 265, 185);
            w++;
        } else {
            g.drawLine (w-rpos, y - (charHeight / 2), w, y - (charHeight / 2));
            g.drawLine (w, y - (charHeight / 2)+1, w, y + (charHeight / 2) - 1);
            g.drawLine (w-rpos, y + (charHeight / 2), w, y + (charHeight / 2));
        }
        if (drawHead) {
            rpos = aa ? 7 : 8;
            int[] xpoints = new int[] {
                w - rpos,
                w - rpos + 5,
                w - rpos + 5,
            };
            int[] ypoints = new int[] {
                y + (charHeight / 2),
                y + (charHeight / 2) - 5,
                y + (charHeight / 2) + 5,
            };
            g.fillPolygon(xpoints, ypoints, 3);
        }

        g.setColor (arrowColor());
        g.drawLine (1, y - (charHeight / 2), 5, y - (charHeight / 2));
        g.drawLine (1, y - (charHeight / 2), 1, y + (charHeight / 2));
        g.drawLine (1, y + (charHeight / 2), 5, y + (charHeight / 2));

        g.setColor (c);
    }

    /**
     * Get the color used for the line wrap arrow
     *
     * @return The arrow color
     */
    private static Color arrowColor() {
        return arrowColor;
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Rectangle result = new Rectangle();
        result.setBounds (0, 0, charWidth, charHeight);
        OutputDocument od = odoc();
        if (od != null) {
            int line = Math.max(0, od.getElementIndex(pos));
            int start = od.getLineStart(line);

            int column = pos - start;

            int row = od.getLines().getLogicalLineCountAbove(line, charsPerLine);
            //#104307
            if (column > charsPerLine && charsPerLine != 0) {
                row += (column / charsPerLine);
                column %= charsPerLine;
            }
            result.y = (row * charHeight) + fontDescent;
            result.x = margin() + (column * charWidth);
//            System.err.println(pos + "@" + result.x + "," + result.y + " line " + line + " start " + start + " row " + row + " col " + column);
        }
        
        return result;
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        OutputDocument od = odoc();
        if (od != null) {
            int ix = Math.max((int) x - margin(), 0);
            int iy = (int) y - fontDescent;

            ln[0] = (iy / charHeight);
            od.getLines().toPhysicalLineIndex(ln, charsPerLine);
            int logicalLine = ln[0];
            int wraps = ln[2] - 1;

            int totalLines = od.getElementCount();
            if (totalLines == 0) {
                return 0;
            }
            if (logicalLine >= totalLines) {
                return od.getLength();
            }

            int lineStart = od.getLineStart(logicalLine);
            int lineEnd = od.getLineEnd(logicalLine);

            int column = ix / charWidth;
            if (column > lineEnd) {
                column = lineEnd;
            }

            int result = wraps > 0 ?
                Math.min(lineEnd, lineStart + (ln[1] * charsPerLine) + column)
                : Math.min(lineStart + column, lineEnd);
            result = Math.min (od.getLength(), result);
            return result;
/*            System.err.println ("ViewToModel " + ix + "," + iy + " = " + result + " physical ln " + physicalLine +
                    " logical ln " + logicalLine + " on wrap line " + ln[1] + " of " + wraps + " charsPerLine " +
                    charsPerLine + " column " + column + " line length " + lineLength);
//            System.err.println ("v2m: [" + ix + "," + iy + "] = " + result);
*/
        } else {
            return 0;
        }
    }
}
