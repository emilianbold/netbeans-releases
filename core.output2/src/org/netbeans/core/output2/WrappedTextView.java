package org.netbeans.core.output2;

import org.openide.ErrorManager;

import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: tim
 * Date: May 19, 2004
 * Time: 4:22:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class WrappedTextView extends View {
    private JTextComponent comp;
    private static Color selectedFg;
    private static Color unselectedFg;
    private static Color selectedLinkFg;
    private static Color unselectedLinkFg;
    private static Color selectedErr;
    private static Color unselectedErr;

    static {
        //XXX clean these colors up a bit for different look and feels
        selectedFg = UIManager.getColor("textText") == null ? Color.BLACK : //NOI18N
            UIManager.getColor("textText"); //NOI18N
        unselectedFg = selectedFg;

        selectedLinkFg = Color.BLUE;
        unselectedLinkFg = selectedLinkFg;

        //XXX it looks like current ant runs simply print *everything* to
        //stderr, which may make trying to color it separately a bit useless.
        selectedErr = new Color (164, 0, 0);
        unselectedErr = selectedErr;
    }
    public WrappedTextView(Element elem, JTextComponent comp) {
        super(elem);
        System.err.println ("WrappedTextView for " + elem);
        this.comp = comp;
    }

    /**
     * Determines the preferred span for this view along an
     * axis.
     *
     * @param axis may be either <code>View.X_AXIS</code> or
     *             <code>View.Y_AXIS</code>
     * @return the span the view would like to be rendered into.
     *         Typically the view is told to render into the span
     *         that is returned, although there is no guarantee.
     *         The parent may choose to resize or break the view
     * @see javax.swing.text.View#getPreferredSpan
     */
    public float getPreferredSpan(int axis) {
        OutputDocument doc = odoc();
        if (doc != null) {
            switch (axis) {
                case X_AXIS :
                    return doc.getLongestLineLength();
                case Y_AXIS :
                    return doc.getLogicalLineCountIfWrappedAt(wcharCount());
                default :
                    throw new IllegalArgumentException (Integer.toString(axis));
            }
        } else {
            return 0;
        }
    }

    private int charWidth = -1;
    private int charWidth() {
        if (charWidth == -1) {
            return 12;
        }
        return charWidth;
    }

    private int charHeight = -1;
    private int charHeight() {
        if (charHeight == -1) {
            return 7;
        }
        return charHeight;
    }


    private OutputDocument odoc() {
        Document doc = comp.getDocument();
        if (doc instanceof OutputDocument) {
            return (OutputDocument) doc;
        }
        return null;
    }

    private boolean changed = true;
    public void setChanged(boolean val) {
        changed = val;
    }

    private int wcharCount() {
        if (charsPerLine == -1) {
            return 80;
        }
        return charsPerLine;
    }

    private int width = -1;
    private int getWidth() {
        if (width == -1) {
            return 1;
        }
        return width;
    }

    private int charsPerLine = -1;
    private void updateInfo(Graphics g) {
//        if (charWidth == -1 || changed) {
            aa = true; /*((Graphics2D) g).getRenderingHint(RenderingHints.KEY_ANTIALIASING) ==
                RenderingHints.VALUE_ANTIALIAS_ON; */

            FontMetrics fm = g.getFontMetrics(comp.getFont());
            charWidth = fm.charWidth('m'); //NOI18N
            charHeight = fm.getHeight();
            if (comp.getParent() instanceof JViewport) {
                JViewport jv = (JViewport) comp.getParent();
                width = jv.getViewSize().width - (aa ? 18 : 24);
            } else {
                width = comp.getWidth() - (aa ? 18 : 24);
            }
            charsPerLine = width / charWidth;
//        }
    }
    private static final Segment SEGMENT = new Segment();

    private int margin() {
        return 9;
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
        OutputDocument d = odoc();
        if (d != null) {
            Rectangle clip = g.getClipBounds();
//            System.err.println("Clip: " + clip);

            int lineCount = d.getElementCount();
            if (lineCount == 0) {
                return;
            }

            int charsPerLine = wcharCount();

            int physicalLine = clip.y / charHeight;

            int[] logline = new int[] {physicalLine, 0, 0};


            d.toLogicalLineIndex(logline, charsPerLine);

//            System.err.println ("Starting physical line: " + physicalLine + " first line: " + logline[0] + " wrap index " + logline[1] + " of " + logline[1] + " clip y " + clip.y);

            int firstline = logline[0]; //XXX

            int count = lineCount - firstline;

            g.setColor (comp.getForeground());

            Segment seg = SwingUtilities.isEventDispatchThread() ? SEGMENT : new Segment();

            int margin = margin();

            int selStart = comp.getSelectionStart();
            int selEnd = comp.getSelectionEnd();

            int y = (clip.y - (clip.y % charHeight())) + charHeight();
            try {
                for (int i=0; i < count; i++) {
                    int lineStart = d.getLineStart(i + firstline);
                    int lineEnd = d.getLineEnd (i + firstline);
                    int length = lineEnd - lineStart;
//                    System.err.println ("Painting " + (i + firstline) + " start " + lineStart + " length " + length);

                    g.setColor(getColorForLocation(lineStart, d, true));

                    //Get the text to print into the segment's array
                    d.getText(lineStart, length, seg);

                    //Get the number of logical lines this physical line contains
                    int logicalLines = seg.count <= charsPerLine ? 1 :  1 + (length / charsPerLine);

                    int currLogicalLine = 0;

                    if (i == 0 && logicalLines > 0) {
                        while (logline[1] > currLogicalLine) {
                            currLogicalLine++;
                        }
                    }
                    //Iterate all the logicalLines lines
                    for (; currLogicalLine < logicalLines; currLogicalLine++) {
                        int charpos = currLogicalLine * charsPerLine;
                        int lenToDraw = Math.min(charsPerLine, length - charpos);

                        if (currLogicalLine != logicalLines-1) {
                            drawArrow (g, y);
                        }

                        g.drawChars(seg.array, charpos, lenToDraw, margin, y);
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

    private boolean aa = false;
    private void drawArrow (Graphics g, int y) {
        Graphics2D g2d = (Graphics2D) g;
        int fontHeight = charHeight();
        Color c = g.getColor();

        g.setColor (new Color (80, 162, 80));

        int w = getWidth() + 15;
        y+=2;

        Stroke s = g2d.getStroke();
        if (aa) {
            g2d.setStroke (new BasicStroke(1.4f));
            g.drawArc(w - 8, y - (fontHeight / 2), 8, fontHeight, 265, 185);
        } else {
            g.drawLine (w-8, y - (fontHeight / 2), w-1, y - (fontHeight / 2));
            g.drawLine (w-8, y - (fontHeight / 2) + 1, w, y - (fontHeight / 2) + 1);
            g.drawLine (w-1, y - (fontHeight / 2), w-1, y + (fontHeight / 2));
            g.drawLine (w, y - (fontHeight / 2)+1, w, y + (fontHeight / 2) - 1);

            g.drawLine (w-6, y + (fontHeight / 2)-1, w, y + (fontHeight / 2)-1);
            g.drawLine (w-6, y + (fontHeight / 2), w-1, y + (fontHeight / 2));
        }
        if (aa) {
            w++;
        }
        int[] xpoints = new int[] {
            w - 10,
            w - 5,
            w - 5,
        };
        int[] ypoints = new int[] {
            y + (fontHeight / 2),
            y + (fontHeight / 2) - 5,
            y + (fontHeight / 2) + 5,
        };
        g.fillPolygon(xpoints, ypoints, 3);

        if (aa) {
            g2d.setStroke(s);
        }

        g.setColor (UIManager.getColor("controlShadow")); //NOI18N
        g.drawLine (1, y - (fontHeight / 2), 5, y - (fontHeight / 2));
        g.drawLine (1, y - (fontHeight / 2), 1, y + (fontHeight / 2));
        g.drawLine (1, y + (fontHeight / 2), 5, y + (fontHeight / 2));

        g.setColor (c);

    }

    private int lastPos = -1;
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Rectangle result = new Rectangle(0, 0, charWidth(), charHeight());
        OutputDocument od = odoc();
        if (od != null) {
            int line = od.getElementIndex(pos);
            int start = od.getLineStart(line);

            int column = pos - start;

            int charsPerLine = wcharCount();

            int linesAbove = od.getLogicalLineCountAbove(line, charsPerLine);

            int row = od.getLogicalLineCountAbove(line, charsPerLine);
            if (column > charsPerLine) {
                row += (column / charsPerLine);
                column = column % charsPerLine;
            }
            result.y = (row * charHeight());

//            result.x = Math.min (od.getLineLength(line), column * charWidth());
            result.x = margin() + (column * charWidth());
              if (pos != lastPos && pos == comp.getCaret().getDot())
                System.err.println ("m2v: pos=" + pos + "=" + result + " line=" + line + " row= " + row + " start=" + start + " column=" + column + " linesAbove=" + linesAbove);
            lastPos = pos;
        }
        return result;
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        OutputDocument od = odoc();
        if (od != null) {
            int ix = (int) x - margin();
            int iy = (int) y;

            int charsPerLine = wcharCount();

            int physicalLine = (iy / charHeight);

            ln[0] = physicalLine;
            od.toLogicalLineIndex(ln, charsPerLine);
            int logicalLine = ln[0];
            int wraps = ln[2] - 1;

            int totalLines = od.getElementCount();
            if (logicalLine >= totalLines) {
                return od.getLength() - 1;
            }

            int lineStart = od.getLineStart(logicalLine);
            int lineLength = od.getLineLength(logicalLine);

            int column = (ix / charWidth());
            if (column > lineLength) {
                return lineStart + lineLength-1;
            }
            //XXX don't return coordinates past the end of a multi-line thingy

            int result = wraps > 0 ? lineStart + (ln[1] * charsPerLine) + column : lineStart + column;
            System.err.println ("ViewToModel " + ix + "," + iy + " = " + result + " physical ln " + physicalLine +
                    " logical ln " + logicalLine + " on wrap line " + ln[1] + " of " + wraps + " charsPerLine " +
                    charsPerLine + " column " + column + " line length " + lineLength);
//            System.err.println ("v2m: [" + ix + "," + iy + "] = " + result);
            return result;

        } else {
            return 0;
        }
    }

    /**
     * A scratchpad int array
     */
    int[] ln = new int[3];

    private static Color getColorForLocation (int start, Document d, boolean selected) {
        OutputDocument od = (OutputDocument) d;
        int line = od.getElementIndex (start);
        boolean hyperlink = od.isHyperlink(line);
        boolean isErr = od.isErr(line);
        return hyperlink ? selected ? selectedLinkFg : unselectedLinkFg :
            selected ? isErr ? selectedErr : selectedFg : isErr ? unselectedErr : unselectedFg;
    }
}
