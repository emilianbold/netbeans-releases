/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author treyspiva
 */
public class MultilineLabelWidget extends LabelWidget
{
    private boolean useGlyphVector = false;
    private GlyphVector cacheGlyphVector;
    private String cacheLabel;
    private Font cacheFont;
    
    public MultilineLabelWidget (Scene scene)
    {
        this(scene, null);
    }
    
    public MultilineLabelWidget (Scene scene, String label)
    {
        super(scene, label);
    }
    
    /**
     * Calculates a client area for the label.
     * @return the client area
     */
    @Override
    protected Rectangle calculateClientArea()
    {
        if (getLabel() == null)
        {
            return super.calculateClientArea();
        }
        
        Rectangle rectangle;
//        if (isUseGlyphVector() == true)
//        {
//            assureGlyphVector();
//            rectangle = GeomUtil.roundRectangle(cacheGlyphVector.getVisualBounds());
//            rectangle.grow(1, 1); // WORKAROUND - even text antialiasing is included into the boundary
//        }
//        else
        {
            Graphics2D gr = getGraphics();
            FontMetrics fontMetrics = gr.getFontMetrics(getFont());
            
            Rectangle2D union = new Rectangle2D.Double();
            double x = 0;
            double y = 0;
            double width = 0;
            double height = 0;
            
            String[] lines = getLabel().split("\n");
            for(int index = 0; index < lines.length; index++)
            {
                String line = lines[index];
                Rectangle2D stringBounds = fontMetrics.getStringBounds(line, gr);
                
                if(index == 0)
                {
                    x = stringBounds.getX();
                    y = stringBounds.getY();
                    width = stringBounds.getWidth();
                }
                else 
                {
                    if(stringBounds.getX() < x)
                    {
                        x = stringBounds.getX();
                    }
                    
                    if(stringBounds.getY() < y)
                    {
                        y = stringBounds.getY();
                    }
                    
                    if(stringBounds.getWidth() > width)
                    {
                        width = stringBounds.getWidth();
                    }
                }
                
                height += stringBounds.getHeight();
            }
            rectangle = roundRectangle(new Rectangle2D.Double(x, y, width, height));
        }
        
        switch (getOrientation())
        {
            case NORMAL:
                return rectangle;
            case ROTATE_90:
                return new Rectangle(rectangle.y, -rectangle.x - rectangle.width, rectangle.height, rectangle.width);
            default:
                throw new IllegalStateException();
        }
    }
    
    @Override
    protected void paintWidget()
    {
        //System.out.println("MultilineLabelWidget: PaintWidget");
        Graphics2D g = getGraphics();
        
        g.setFont(getFont());
        FontMetrics fontMetrics = g.getFontMetrics(getFont());
       
        LineDetails lines = breakupLines(g, fontMetrics);
        if ( lines != null)
        {
            // save the current color
            Color currentColor = g.getColor();
            g.setColor(this.getForeground());
            
            int x;
            int y = (getSize().height - lines.getNumberOfLines() * fontMetrics.getHeight()) / 2;;

            for(int index = 0; index < lines.getNumberOfLines(); index++)
            {
                String line = lines.getLine(index);
                if(line == null)
                {
                    line = new String();
                }
                switch (getAlignment())
                {
                    case LEFT:
                        x = 0;
                        break;
                    case RIGHT:
                        x = getSize().width- fontMetrics.stringWidth(line);
                        break;
                    case CENTER:
                    default:
                        x = (getSize().width- fontMetrics.stringWidth(line)) / 2;
                }
                g.drawString(line, x, y);

                y += lines.getHeight(index);
            }

            // reset to the original color
            if ( !g.getColor().equals(currentColor))
            {
                g.setColor(currentColor);
            }
        }
      }
        
    protected LineDetails breakupLines(Graphics2D g, FontMetrics metrics)
    {
        LineDetails retVal = null;
        String text = getLabel();
        if ( text == null ||  text.length() == 0) 
        {
            return retVal;
        }
        
        retVal = new LineDetails();
        
        int width = getClientArea().width;
        //System.out.println("BreakupLine: node label: " + text);
        StringBuilder label = new StringBuilder(text);  
        
        int index = 0;
        int startLine = 0;
        int previousEnd = 0;
        String previousLine = null;
        double previousHeight = 0;
        
        while(index >= 0)
        {
            
            index = findEndOfNextWord(label, index);
            if(index == -1)
            {
                retVal.addLine(previousLine, previousHeight);
                break;
            }
            
            String line = label.substring(startLine, index);
            Rectangle2D strBounds = metrics.getStringBounds(line, g);

            if(strBounds.getWidth() <= width)
            {
                previousLine = line;
                previousHeight = strBounds.getHeight();
                
                if((index < label.length()) && (label.charAt(index) == '\n'))
                {
                    retVal.addLine(previousLine, previousHeight);
                    
                    // If the next character is a whitespace, then skip it.
                    if(label.length() > (index + 1))
                    {
                        if((label.charAt(index + 1) != '\n') && 
                           (Character.isWhitespace(label.codePointAt(index + 1)) == true))
                        {
                            index++;
                        }
                    }
                    
                    startLine = index;
                }
                
                previousEnd = index;
                index++;
            }
            else  // the width of the line is longer than the width of the Widget's client area.
            {
                if ( previousLine != null) 
                {
                    retVal.addLine(previousLine, previousHeight);
                    previousLine = null;  //Already added; hence reset
                }
                else 
                {
                    String subStr = null;
                    //int len = label.length();
                    int startIndx = startLine;
                    int endIndx = index;
                    while (startIndx < endIndx)
                    {
                        subStr = label.substring(startIndx, endIndx);
                        strBounds = metrics.getStringBounds(subStr, g);
                        if (strBounds.getWidth() < width) 
                        {
                            retVal.addLine(subStr, strBounds.getHeight());
                            // reset the subString' s start and end indices
                            startIndx = endIndx;
                            endIndx = index+1;
                        }
                        endIndx--;
                    }
                    previousEnd = index;
                }
                
                if((index < label.length()) && (label.charAt(index) == '\n'))
                {
                    // Since we are creating a new line, we should just move
                    // past the new line char.
                    index++;
                    previousEnd = index;
                    startLine = index;
                }
                
                // Find the next non-whitespace, start from the previous end
                // point, since that was the end of the line.
                index = findFirstNonWhitespace(label, previousEnd);
                startLine = index;
            }
        }
        
        return retVal;
    }
    
    protected int findFirstNonWhitespace(StringBuilder label, int start)
    {
        int retVal = -1;
        for(int index = start; index < label.length(); index++)
        {
            int codePointAt = label.codePointAt(index);
            if(Character.isWhitespace(codePointAt) == false)
            {
                retVal = index;
                break;
            }
            else if(codePointAt == '\n')
            {
                retVal = index;
                break;
            }
        }
        
        return retVal;
    }
    
    protected int findEndOfNextWord(StringBuilder label, int start)
    {
        int retVal = -1;

        if((start >= 0) && (start < label.length()))
        {
            retVal = label.length();
            for (int index = start; index < label.length(); index++)
            {
                int codePoint = label.codePointAt(index);
                if(codePoint == '\n')
                {
                    retVal = index;
                    break;
                }
                else if(Character.isWhitespace(label.codePointAt(index)) == true)
                {
                    retVal = index;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
//    private void assureGlyphVector () {
//        Font font = getFont ();
//        FontRenderContext fontRenderContext = getGraphics ().getFontRenderContext ();
//        if (cacheGlyphVector != null  && 
//            cacheFont == font  &&  
//            cacheLabel == getLabel())
//        {
//            return;
//        }
//        
//        cacheFont = font;
//        cacheLabel = getLabel();
//        cacheGlyphVector = font.createGlyphVector (new FontRenderContext (new AffineTransform (), fontRenderContext.isAntiAliased (), fontRenderContext.usesFractionalMetrics ()), cacheLabel);
//    }
    
    /**
     * Rounds Rectangle2D to Rectangle.
     * @param rectangle the rectangle2D
     * @return the rectangle
     */
    public Rectangle roundRectangle (Rectangle2D rectangle) {
        int x1 = (int) Math.floor (rectangle.getX ());
        int y1 = (int) Math.floor (rectangle.getY ());
        int x2 = (int) Math.ceil (rectangle.getMaxX ());
        int y2 = (int) Math.ceil (rectangle.getMaxY ());
        return new Rectangle (x1, y1, x2 - x1, y2 - y1);
    }
    
    protected Dimension getSize()
    {
        Insets labelInsets = getBorder().getInsets();
        int width = getBounds().width - labelInsets.left - labelInsets.right;      
        int height = getBounds().height - labelInsets.top - labelInsets.bottom;
        
        return new Dimension(width, height);
    }
            
    
    protected class LineDetails
    {
        private ArrayList < String > lines = new ArrayList < String >();
        private ArrayList < Float > heights = new ArrayList < Float >();
        
        public final void addLine(String line, double height)
        {
            lines.add(line);
            heights.add((float)height);
            //System.out.println("LineDetails.AddLine(): " + lines.toString());
        }
        
        public final String getLine(int index)
        {
            return lines.get(index);
        }
        
        public final float getHeight(int index)
        {
            return heights.get(index);
        }
        
        public int getNumberOfLines()
        {
            return lines.size();
        }
    }
}
