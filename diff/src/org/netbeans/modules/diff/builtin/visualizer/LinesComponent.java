/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin.visualizer;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.FontMetrics;
import org.netbeans.editor.FontMetricsCache;
import java.awt.Insets;
import java.awt.image.ImageObserver;
import org.netbeans.editor.*;

/** GlyphGutter is component for displaying line numbers and annotation
 * glyph icons. Component also allow to "cycle" through the annotations. It
 * means that if there is more than one annotation on the line, only one of them
 * might be visible. And clicking the special cycling button in the gutter the user
 * can cycle through the annotations.
 *
 * @author  David Konecny
 * @since 07/2001
 */

public class LinesComponent extends JComponent {

    
    /** Document to which this gutter is attached*/
    private JEditorPane editorPane;
    
    /** Backroung color of the gutter */
    private Color backgroundColor;
    
    /** Foreground color of the gutter. Used for drawing line numbers. */
    private Color foreColor;
    
    /** Font used for drawing line numbers */
    private Font font;
    
    /** Height of the line as it was calculated in EditorUI. */
    private int lineHeight = 1;

    private float lineHeightCorrection = 1.0f;

    /** Flag whther the gutter was initialized or not. The painting is disabled till the
     * gutter is not initialized */
    private boolean init;
    
    /** Width of the column used for drawing line numbers. The value contains
     * also line number margins. */
    private int numberWidth;

    /** Whether the line numbers are shown or not */
    private boolean showLineNumbers = true;
    
    /** The gutter height is enlarged by number of lines which specifies this constant */
    private static final int ENLARGE_GUTTER_HEIGHT = 300;
    
    /** The hightest line number. This value is used for calculating width of the gutter */
    private int highestLineNumber = 0;

    /** Holds value of property lineNumberMargin. */
    private Insets lineNumberMargin;
    
    /** Holds value of property lineNumberDigitWidth. */
    private int lineNumberDigitWidth;
    
    /** Holds value of property lineAscent. */
    private int lineAscent;
    
    private LinkedList linesList;
    
    /** Holds value of property activeLine. */
    private int activeLine = -1;

    private static final long serialVersionUID = -4861542695772182147L;
    
    public LinesComponent(JEditorPane pane) {
        super();
        init = false;
        editorPane = pane;
        font = editorPane.getFont();
        foreColor = editorPane.getForeground();
        backgroundColor = editorPane.getBackground();
        setLineNumberDigitWidth(10);
        setLineNumberMargin(new Insets(2, 2, 2, 4));
        init();
        update ();
    }

    /** Do initialization of the glyph gutter*/
    protected void init() {
        createLines();

    }
    
    private void createLines() {
        linesList = new LinkedList();
        int lineCnt;
        StyledDocument doc = (StyledDocument)editorPane.getDocument();
        int lastOffset = doc.getEndPosition().getOffset();
        lineCnt = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);            
        for (int i = 0; i < lineCnt; i++) {
            linesList.add("" + (i + 1));
        }
    }
    
    public void addEmptyLines(int line, int count) {
        boolean appending = line > linesList.size();
        for (int i = 0; i < count; i++) {
            if (appending) {
                linesList.add("");
            } else {
                linesList.add(line, "");
            }
        }
    }
    
    /**
     * Insert line numbers. If at the end, then line numbers are added to the end of the component.
     * If in the middle, subsequent lines are overwritten.
     */
    public void insertNumbers(int line, int startNum, int count) {
        boolean appending = line > linesList.size();
        if (appending) {
            for (int i = 0; i < count; i++, startNum++) {
                linesList.add(Integer.toString(startNum));
            }
        } else {
            for (int i = 0; i < count; i++, startNum++, line++) {
                linesList.set(line, Integer.toString(startNum));
            }
        }
    }
    
    /** Update colors, fonts, sizes and invalidate itself. This method is
     * called from EditorUI.update() */
    private void update() {
        Class kitClass = editorPane.getEditorKit().getClass();
        Object value = Settings.getValue(kitClass, SettingsNames.LINE_HEIGHT_CORRECTION);
        if (!(value instanceof Float) || ((Float)value).floatValue() < 0) {
            value = SettingsDefaults.defaultLineHeightCorrection;
        }
        lineHeightCorrection = ((Float)value).floatValue();
        Object colValue = Settings.getValue(kitClass, SettingsNames.LINE_NUMBER_COLORING);
        Coloring col = null;
        if (colValue != null && colValue instanceof Coloring) {
            col = (Coloring)colValue;
        } else {
            col = SettingsDefaults.defaultLineNumberColoring;
        }
        foreColor = col.getForeColor();
        backgroundColor = col.getBackColor();
        
        font = editorPane.getFont();
        FontMetrics fm = editorPane.getFontMetrics(font);
        int maxHeight = 1;
        int maxAscent = 0;
        if (fm != null) {
            maxHeight = Math.max(maxHeight, fm.getHeight());
            maxAscent = Math.max(maxAscent, fm.getAscent());
        }

        // Apply lineHeightCorrection
        lineHeight = (int)(maxHeight * lineHeightCorrection);
        lineAscent = (int)(maxAscent * lineHeightCorrection);
//        System.out.println("lineheight=" + lineHeight);
//        System.out.println("lineascent=" + lineAscent);
        showLineNumbers = true;

        init = true;

        // initialize the value with current number of lines
        if (highestLineNumber <= getLineCount()) {
            highestLineNumber = getLineCount();
        }
//        System.out.println("highestLineNumber=" + highestLineNumber);
        // width of a digit..
        int maxWidth = 1;
        char[] digit = new char[1]; // will be used for '0' - '9'
        for (int i = 0; i <= 9; i++) {
             digit[0] = (char)('0' + i);
             maxWidth = Math.max(maxWidth, fm.charsWidth(digit, 0, 1));
        }
        setLineNumberDigitWidth(maxWidth);
//        System.out.println("maxwidth=" + maxWidth);
//        System.out.println("numner of lines=" + highestLineNumber);
        
        resize();
    }
    
    protected void resize() {
        Dimension dim = new Dimension();
//        System.out.println("resizing...................");
        dim.width = getWidthDimension();
        dim.height = getHeightDimension();
        // enlarge the gutter so that inserting new lines into 
        // document does not cause resizing too often
        dim.height += ENLARGE_GUTTER_HEIGHT * lineHeight;
        
        numberWidth = getLineNumberWidth();
        setPreferredSize(dim);

        revalidate();
    }

    /** Return number of lines in the document */
    protected int getLineCount() {
        return linesList.size();
    }

    /** Gets number of digits in the number */
    protected int getDigitCount(int number) {
        return Integer.toString(number).length();
    }

    protected int getLineNumberWidth() {
        int newWidth = 0;
        Insets insets = getLineNumberMargin();
        if (insets != null) {
            newWidth += insets.left + insets.right;
        }
        newWidth += (getDigitCount(highestLineNumber) + 1) * getLineNumberDigitWidth();
//        System.out.println("new width=" + newWidth);
        return newWidth;
    }

    protected int getWidthDimension() {
        int newWidth = 0;
        
        if (showLineNumbers) {
            newWidth += getLineNumberWidth();
        }

        return newWidth;
    }
    
    protected int getHeightDimension() {
        return highestLineNumber * lineHeight /*TEMP+ (int)editorPane.getSize().getHeight() */;
    }
    
    /** Paint the gutter itself */
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        update();
        // if the gutter was not initialized yet, skip the painting
        if (!init)
            return;
        
        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(backgroundColor);
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        g.setFont(font); 
        g.setColor(foreColor);

        FontMetrics fm = FontMetricsCache.getFontMetrics(font, this);
        int rightMargin = 0;
        Insets margin = getLineNumberMargin();
        if (margin != null)
            rightMargin = margin.right;
        // calculate the first line which must be drawn
        int line = (int)( (float)drawHere.y / (float)lineHeight );
        if (line > 0)
            line--;

        // calculate the Y of the first line
        int y = line * lineHeight;

        if (showLineNumbers) {
            int lastLine = (int)( (float)(drawHere.y+drawHere.height) / (float)lineHeight )+1;
            if (lastLine > highestLineNumber) {
                int prevHighest = highestLineNumber;
                highestLineNumber = lastLine;
                if (getDigitCount(highestLineNumber) > getDigitCount(prevHighest)) {
//                    System.out.println("resizing in paintComponent()");
//                    System.out.println("lastline=" + lastLine);
//                    System.out.println("highestLineNumber=" + highestLineNumber);
                    resize();
                    return;
                }
            }
        }
        
        
        // draw liune numbers and annotations while we are in visible area
        // "+(lineHeight/2)" means to don't draw less than half of the line number
        while ( (y+(lineHeight/2)) <= (drawHere.y + drawHere.height) )
        {
            // draw line numbers if they are turned on
            if (showLineNumbers) {
                String lineStr = null;
                if (line < linesList.size()) {
                    lineStr = (String)linesList.get(line);
                }
                if (lineStr == null) {
                    lineStr = "";
                }
                String activeSymbol = "*";
                int lineNumberWidth = fm.stringWidth(lineStr);
                if (line == activeLine - 1) {
                    lineStr = lineStr + activeSymbol;
                } 
                int activeSymbolWidth = fm.stringWidth(activeSymbol);
                lineNumberWidth = lineNumberWidth + activeSymbolWidth;
                g.drawString(lineStr, numberWidth-lineNumberWidth-rightMargin, y + getLineAscent());
            }
            
            y += lineHeight;
            line++;
        }
    }

    /** Data for the line has changed and the line must be redraw. */
    public void changedLine(int line) {
        
        if (!init)
            return;
        
        // redraw also lines around - three lines will be redrawn
        if (line > 0)
            line--;
        int y = line * lineHeight;
        
        repaint(0, y, (int)getSize().getWidth(), 3*lineHeight);
        checkSize();
    }

    /** Repaint whole gutter.*/
    public void changedAll() {

        if (!init)
            return;

/*        int lineCnt;
        try {
            lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
        } catch (BadLocationException e) {
            lineCnt = 1;
        }
 */

        repaint();
        checkSize();
    }

    protected void checkSize() {
        int count = getLineCount();
        if (count > highestLineNumber) {
            highestLineNumber = count;
        }
        Dimension dim = getPreferredSize();
        if (getWidthDimension() > dim.width ||
            getHeightDimension() > dim.height) {
                resize();
        }
    }
    
    /** Getter for property lineNumberMargin.
     * @return Value of property lineNumberMargin.
     */
    public Insets getLineNumberMargin() {
        return this.lineNumberMargin;
    }
    
    /** Setter for property lineNumberMargin.
     * @param lineNumberMargin New value of property lineNumberMargin.
     */
    public void setLineNumberMargin(Insets lineNumberMargin) {
        this.lineNumberMargin = lineNumberMargin;
    }
    
    /** Getter for property lineNumberDigitWidth.
     * @return Value of property lineNumberDigitWidth.
     */
    public int getLineNumberDigitWidth() {
        return this.lineNumberDigitWidth;
    }
    
    /** Setter for property lineNumberDigitWidth.
     * @param lineNumberDigitWidth New value of property lineNumberDigitWidth.
     */
    public void setLineNumberDigitWidth(int lineNumberDigitWidth) {
        this.lineNumberDigitWidth = lineNumberDigitWidth;
    }
    
    /** Getter for property lineAscent.
     * @return Value of property lineAscent.
     */
    public int getLineAscent() {
        return this.lineAscent;
    }
    
    /** Setter for property lineAscent.
     * @param lineAscent New value of property lineAscent.
     */
    public void setLineAscent(int lineAscent) {
        this.lineAscent = lineAscent;
    }
    
    /** Getter for property activeLine.
     * @return Value of property activeLine.
     */
    public int getActiveLine() {
        return this.activeLine;
    }
    
    /** Setter for property activeLine.
     * @param activeLine New value of property activeLine.
     */
    public void setActiveLine(int activeLine) {
        this.activeLine = activeLine;
    }

}
