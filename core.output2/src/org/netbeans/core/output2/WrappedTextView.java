/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.output2;

import javax.swing.text.DefaultCaret;
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * A custom Swing text View which supports line wrapping.  The default Swing
 * line wrapping code is not appropriate for our purposes - particularly, it
 * will iterate the entire buffer multiple times to determine break positions.
 * Since it would defeat the purpose of using a memory mapped file to have to
 * pull the entire thing into memory every time it's painted or its size should
 * be calculated, we have this class instead.
 * <p>
 * All position/line calculations this view does are based on the integer array
 * of line offsets kept by OutWriter.
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
    private int charsPerLine = -1;
    /**
     * Precalculated font descent, used to adjust the bounding rectangle of
     * characters as returned by modelToView.
     */
    private int fontDescent = -1;
    /**
     * A scratch Segment object to avoid allocation while painting lines
     */
    private static final Segment SEGMENT = new Segment();
    /**
     * Precalculated width (in pixels) we are to paint into, the end being the wrap point
     */
    private int width = -1;
    /**
     * Flag indicating we need to recalculate metrics before painting
     */
    private boolean changed = true;
    /**
     * Precalculated width of a single character (assumes fixed width font).
     */
    private int charWidth = -1;
    /**
     * Precalculated height of a single character (assumes fixed width font).
     */
    private int charHeight = -1;
    /**
     * A scratchpad int array
     */
    static final int[] ln = new int[3];
    /**
     * Flag indicating that the antialiasing flag is set on the Graphics object.
     * We do a somewhat prettier arrow if it is.
     */
    private boolean aa = false;

    //Self explanatory...
    static Color selectedFg;
    static Color unselectedFg;
    static Color selectedLinkFg;
    static Color unselectedLinkFg;
    static Color selectedErr;
    static Color unselectedErr;
    static final Color arrowColor = new Color (80, 162, 80);

    static {
        selectedFg = UIManager.getColor ("nb.output.foreground.selected"); //NOI18N
        if (selectedFg == null) {
            selectedFg = UIManager.getColor("textText") == null ? Color.BLACK : //NOI18N
               UIManager.getColor("textText"); //NOI18N
        }
        
        unselectedFg = UIManager.getColor ("nb.output.foreground"); //NOI18N
        if (unselectedFg == null) {
            unselectedFg = selectedFg;
        }

        selectedLinkFg = UIManager.getColor("nb.output.link.foreground.selected"); //NOI18N
        if (selectedLinkFg == null) selectedLinkFg = Color.BLUE;
        
        unselectedLinkFg = UIManager.getColor("nb.output.link.foreground"); //NOI18N
        if (unselectedLinkFg == null) {
            unselectedLinkFg = selectedLinkFg;
        }

        selectedErr = UIManager.getColor ("nb.output.err.foreground.selected"); //NOI18N
        if (selectedErr == null) {
            selectedErr = new Color (164, 0, 0);
        }
        unselectedErr = UIManager.getColor ("nb.output.err.foreground"); //NOI18N
        if (unselectedErr == null) {
            unselectedErr = selectedErr;
        }
    }


    public WrappedTextView(Element elem, JTextComponent comp) {
        super(elem);
        this.comp = comp;
    }


    public float getPreferredSpan(int axis) {
        OutputDocument doc = odoc();
        float result = 0;
        if (doc != null) {
            switch (axis) {
                case X_AXIS :
                    result = getCharsPerLine();
                    break;
                case Y_AXIS :
                    result = doc.getLogicalLineCountIfWrappedAt(getCharsPerLine()) * charHeight() + fontDescent();
                    break;
                default :
                    throw new IllegalArgumentException (Integer.toString(axis));
            }
//            System.err.println ("Preferred " + a2s(axis) + "=" + result + " for " + doc.getElementCount() + " lines" + l2c(axis, result));
        }
        return result;
    }

    private String l2c(int axis, float count) {
        int ct = (int) count;
        String result = "";
        switch (axis) {
            case X_AXIS :
                result = " = " + (ct * charWidth()) + " pixels width";
                break;
            case Y_AXIS :
                result = " = " + (ct * charHeight()) + " pixels height";
                break;
        }
        result += " logical lines: " + odoc().getLogicalLineCountIfWrappedAt(getCharsPerLine());
        return result;
    }

    private static String a2s(int i) {
        return i == X_AXIS ? "X_AXIS" : "Y_AXIS";
    }

    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * Get the last calculated character width, returning a reasonable
     * default if none has been calculated.
     *
     * @return The character width
     */
    private int charWidth() {
        if (charWidth == -1) {
            return 12;
        }
        return charWidth;
    }

    /**
     * Get the last calculated character height, returning a reasonable
     * default if none has been calculated.
     *
     * @return The character height
     */
    private int charHeight() {
        if (charHeight == -1) {
            return 7;
        }
        return charHeight;
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

    /**
     * Set a flag indicating that on the next paint, widths, character widths,
     * etc. need to be recalculated.
     *
     * @param val Whether to recalculate or not.
     */
    public void setChanged(boolean val) {
        changed = val;
        updateInfo (null);
        preferenceChanged(this, true, true);
    }

    /**
     * Get the number of characters per line that can be displayed before wrapping
     * given the component's current font and size.
     *
     * @return Characters per line, or 80 if not yet calculated
     */
    private int getCharsPerLine() {
        if (charsPerLine == -1) {
            return 80;
        }
        return charsPerLine;
    }

    /**
     * Get the last known width of the component we're painting into
     *
     * @return The width we should paint into
     */
    private int getWidth() {
        if (width == -1) {
            return 1;
        }
        return width;
    }

    /**
     * Get the font descent for the last known font, or a reasonable default if unknown.
     * This is added to the y position of character rectangles in modelToView() so
     * painting the selection includes the complete character and does not interfere
     * with the line above.
     *
     * @return The font descent.
     */
    private int fontDescent() {
        if (fontDescent == -1) {
            return 4;
        }
        return fontDescent;
    }

    /**
     * Update metrics we use when painting, such as the visible area of the component,
     * the charcter width/height, etc.
     * @param g
     */
    public void updateInfo(Graphics g) {
        if (charWidth == -1 || changed) {
            if (g != null) {
                aa = ((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING) ==
                    RenderingHints.VALUE_ANTIALIAS_ON;

                FontMetrics fm = g.getFontMetrics(comp.getFont());
                charWidth = fm.charWidth('m'); //NOI18N
                charHeight = fm.getHeight();
                fontDescent = fm.getMaxDescent();
                charsPerLine = width / charWidth;
            }
            if (comp.getParent() instanceof JViewport) {
                JViewport jv = (JViewport) comp.getParent();
                width = jv.getExtentSize().width - (aa ? 18 : 17);
            } else {
                width = comp.getWidth() - (aa ? 18 : 17);
            }
        }
    }

    /**
     * Get the left hand margin required for printing line wrap decorations.
     *
     * @return
     */
    public static int margin() {
        return 9;
    }

    private static final Rectangle scratch = new Rectangle();
    
    private void highlightCaretRow (Graphics g, Shape allocation) {
        Rectangle scratch = SwingUtilities.isEventDispatchThread() ? this.scratch : new Rectangle();
        int dot = comp.getCaret().getDot();
        int mark = comp.getCaret().getMark();
        if (dot == mark && dot > 0 && dot <= getDocument().getLength()) {
            try {
                Rectangle r = comp.modelToView(dot);
                r.x = 0;
                r.width = comp.getWidth();
                Rectangle clip = allocation instanceof Rectangle ? (Rectangle) allocation : allocation.getBounds();
                if (clip.y < r.y && clip.y + clip.height > r.y) {
                    scratch.setBounds (r);
                    scratch.x = Math.max (r.x, clip.x);
                    scratch.y = Math.max (r.y, clip.y);
                    if (scratch.x > 0) {
                        scratch.width -= scratch.x;
                    }
                    if (scratch.y > r.y) {
                        scratch.height -= (scratch.y - r.y);
                    }
                    g.setColor (new Color (245, 245, 237));
                    g.fillRect (scratch.x, scratch.y, scratch.width, scratch.height);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    /**
     * Renders using the given rendering surface and area on that
     * surface.  The view may need to do layout and create child
     * views to enable itself to render into the given allocation.
     *
     * @param g          the rendering surface to use
     * @param allocation the allocated region to render into
     * @see javax.swing.text.View#paint
     */
    public void paint(Graphics g, Shape allocation) {
        updateInfo(g);
//        highlightCaretRow (g, allocation); //XXX commenting out for now
        
        comp.getHighlighter().paint(g);
        
        Rectangle vis = comp.getVisibleRect();
        
        OutputDocument d = odoc();
        if (d != null) {
            Rectangle clip = g.getClipBounds();
            clip.y = Math.max (0, clip.y - charHeight());
            clip.height += charHeight() * 2;

            int lineCount = d.getElementCount();
            if (lineCount == 0) {
                return;
            }

            int charsPerLine = getCharsPerLine();

            int physicalLine = clip.y / charHeight;

            ln[0] = physicalLine;

            d.toLogicalLineIndex(ln, charsPerLine);

            int firstline = ln[0];

            int count = (lineCount - firstline);          

            g.setColor (comp.getForeground());

            Segment seg = SwingUtilities.isEventDispatchThread() ? SEGMENT : new Segment();

            int margin = margin();

            int selStart = comp.getSelectionStart();
            int selEnd = comp.getSelectionEnd();
            
            int lineHeight = charHeight() + fontDescent();
            
            int y = (clip.y - (clip.y % charHeight()) + charHeight());
            
            try {
                for (int i=0; i < count; i++) {
                    int lineStart = d.getLineStart(i + firstline);
                    int lineEnd = d.getLineEnd (i + firstline);
                    int length = lineEnd - lineStart;

                    g.setColor(getColorForLocation(lineStart, d, true));

                    //Get the text to print into the segment's array
                    d.getText(lineStart, length, seg);

                    //Get the number of logical lines this physical line contains
                    int logicalLines = seg.count <= charsPerLine ? 1 :  1 + (length / charsPerLine);

                    int currLogicalLine = 0;

                    if (i == 0 && logicalLines > 0) {
                        while (ln[1] > currLogicalLine) {
                            //Fast forward through logical lines above the first one we want
                            //to paint, but do redraw the arrow so we don't erase it
                            currLogicalLine++;
                            drawArrow (g, y - ((logicalLines - currLogicalLine) * charHeight()), currLogicalLine == ln[1]);
                        }
                    }
                    //Iterate all the logicalLines lines
                    for (; currLogicalLine < logicalLines; currLogicalLine++) {
                        int charpos = currLogicalLine * charsPerLine;
                        int lenToDraw = Math.min(charsPerLine, length - charpos);

                        if (currLogicalLine != logicalLines-1) {
                            drawArrow (g, y, currLogicalLine == logicalLines-2);
                        }
                        int realPos = lineStart + charpos;

                        if (realPos >= selStart && realPos + lenToDraw <= selEnd) {
                            Color c = g.getColor();
                            g.setColor (comp.getSelectionColor());
                            g.fillRect (margin(), y+fontDescent()-charHeight(), lenToDraw * charWidth(), charHeight());
                            g.setColor (c);
                        } else if (realPos <= selStart && realPos + lenToDraw >= selStart) {
                            int selx = margin() + (charWidth() * (selStart - realPos));
                            int selLen = selEnd > realPos + lenToDraw ? ((lenToDraw + realPos) - selStart) * charWidth() :
                                    (selEnd - selStart) * charWidth();
                            Color c = g.getColor();
                            g.setColor (comp.getSelectionColor());
                            g.fillRect (selx, y + fontDescent() - charHeight(), selLen, charHeight());
                            g.setColor (c);
                        } else if (realPos > selStart && realPos + lenToDraw >= selEnd) {
                            //we're drawing the tail of a selection
                            int selLen = (selEnd - realPos) * charWidth();
                            Color c = g.getColor();
                            g.setColor (comp.getSelectionColor());
                            g.fillRect (margin(), y + fontDescent() - charHeight(), selLen, charHeight());
                            g.setColor (c);
                        }

                        g.drawChars(seg.array, charpos, lenToDraw, margin, y);
                        if (g.getColor() == unselectedLinkFg) {
                            g.drawLine (margin(), y+1, g.getFontMetrics().charsWidth(seg.array, charpos, lenToDraw), y+1);
                        }
                        y += charHeight();
                    }
                    if (y > clip.y + clip.height || i + firstline == lineCount -1) {
                        break;
                    }
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    /**
     * Draw the decorations used with wrapped lines.
     *
     * @param g A graphics to paint into
     * @param y The y coordinate of the line as a font baseline position
     */
    private void drawArrow (Graphics g, int y, boolean drawHead) {
        int fontHeight = charHeight();
        Color c = g.getColor();

        g.setColor (arrowColor());

        int w = getWidth() + 15;
        y+=2;


        int rpos = aa ? 8 : 4;
        if (aa) {
            g.drawArc(w - rpos, y - (fontHeight / 2), rpos + 1, fontHeight, 265, 185);
        } else {
            g.drawLine (w-rpos, y - (fontHeight / 2), w, y - (fontHeight / 2));
            g.drawLine (w, y - (fontHeight / 2)+1, w, y + (fontHeight / 2) - 1);
            g.drawLine (w-rpos, y + (fontHeight / 2), w, y + (fontHeight / 2));
        }
        if (aa) {
            w++;
        }
        if (drawHead) {
            rpos = aa ? 7 : 8;
            int[] xpoints = new int[] {
                w - rpos,
                w - rpos + 5,
                w - rpos + 5,
            };
            int[] ypoints = new int[] {
                y + (fontHeight / 2),
                y + (fontHeight / 2) - 5,
                y + (fontHeight / 2) + 5,
            };
            g.fillPolygon(xpoints, ypoints, 3);
        }

        g.setColor (arrowColor());
        g.drawLine (1, y - (fontHeight / 2), 5, y - (fontHeight / 2));
        g.drawLine (1, y - (fontHeight / 2), 1, y + (fontHeight / 2));
        g.drawLine (1, y + (fontHeight / 2), 5, y + (fontHeight / 2));

        g.setColor (c);

    }

    /**
     * Get the color used for the line wrap arrow
     *
     * @return The arrow color
     */
    private Color arrowColor() {
        return arrowColor;
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Rectangle result = new Rectangle();
        result.setBounds (0, 0, charWidth(), charHeight());
        OutputDocument od = odoc();
        if (od != null) {
            int line = od.getElementIndex(pos);
            int start = od.getLineStart(line);

            int column = pos - start;

            int charsPerLine = getCharsPerLine();

            int row = od.getLogicalLineCountAbove(line, charsPerLine);
            if (column > charsPerLine) {
                row += (column / charsPerLine);
                column = column % charsPerLine;
            }
            result.y = (row * charHeight()) + fontDescent();
            result.x = margin() + (column * charWidth());
//            System.err.println(pos + "@" + result.x + "," + result.y + " line " + line + " start " + start + " row " + row + " col " + column);
        }
        
        return result;
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        OutputDocument od = odoc();
        if (od != null) {
            int ix = (int) x - margin();
            int iy = (int) y - fontDescent();

            int charsPerLine = getCharsPerLine();

            int physicalLine = (iy / charHeight);

            ln[0] = physicalLine;
            od.toLogicalLineIndex(ln, charsPerLine);
            int logicalLine = ln[0];
            int wraps = ln[2] - 1;

            int totalLines = od.getElementCount();
            if (totalLines == 0) {
                return 0;
            }
            if (logicalLine >= totalLines) {
                return od.getLength() - 1;
            }

            int lineStart = od.getLineStart(logicalLine);
            int lineLength = od.getLineLength(logicalLine);

            int column = (ix / charWidth());
            if (column > lineLength-1) {
                column = lineLength-1;
            }

            int result = wraps > 0 ?
                Math.min(od.getLineEnd(logicalLine) - 1, lineStart + (ln[1] * charsPerLine) + column)
                : lineStart + column;
/*            System.err.println ("ViewToModel " + ix + "," + iy + " = " + result + " physical ln " + physicalLine +
                    " logical ln " + logicalLine + " on wrap line " + ln[1] + " of " + wraps + " charsPerLine " +
                    charsPerLine + " column " + column + " line length " + lineLength);
//            System.err.println ("v2m: [" + ix + "," + iy + "] = " + result);
*/
                
            return result;

        } else {
            return 0;
        }
    }

    /**
     * Get a color for a given position in the document - this will either be plain,
     * hyperlink or standard error colors.
     *
     * @param start A position in the document
     * @param d The document, presumably an instance of OutputDocument (though briefly it is not
     *  during an editor kit change)
     * @param selected Whether or not it is selected
     * @return The foreground color to paint with
     */
    private static Color getColorForLocation (int start, Document d, boolean selected) {
        OutputDocument od = (OutputDocument) d;
        int line = od.getElementIndex (start);
        boolean hyperlink = od.isHyperlink(line);
        boolean isErr = od.isErr(line);
        return hyperlink ? selected ? selectedLinkFg : unselectedLinkFg :
            selected ? isErr ? selectedErr : selectedFg : isErr ? unselectedErr : unselectedFg;
    }
}
