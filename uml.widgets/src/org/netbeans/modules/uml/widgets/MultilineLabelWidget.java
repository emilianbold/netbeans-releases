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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
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

        Graphics2D gr = getGraphics();
        FontMetrics fontMetrics = gr.getFontMetrics(getFont());

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
        Graphics2D g = getGraphics();

        g.setFont(getFont());
        FontMetrics fontMetrics = g.getFontMetrics(getFont());

        String txt=getLabel();
        if(txt!=null && txt.length()>0)
        {
            String[] lines0 = getLabel().split("\n");
            ArrayList<LineDetails> lineDeltails=new ArrayList<LineDetails>();
            for(int i=0;i<lines0.length;i++)
            {
                String lineTxt=lines0[i];
                if(lineTxt.length()==0)lineTxt=" ";//draw one space if empty line
                LineDetails lines = breakupLinesInNoBreakLinedTxt(lineTxt,g, fontMetrics);
                lineDeltails.add(lines);
            }
            LineDetails lines = new LineDetails();
            if ( lineDeltails.size()>0)
            {
                for(int i=0;i<lineDeltails.size();i++)
                {
                    for(int j=0;j<lineDeltails.get(i).getNumberOfLines();j++)
                    {
                        lines.addLine(lineDeltails.get(i).getLine(j), lineDeltails.get(i).getHeight(j));
                    }
                }
                // save the current color
                Color currentColor = g.getColor();
                g.setColor(this.getForeground());


                int x;
                int y = (getSize().height - lines.getNumberOfLines() * fontMetrics.getHeight()) / 2;

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
      }

    protected LineDetails breakupLinesInNoBreakLinedTxt(String text,Graphics2D g, FontMetrics metrics)
    {
        LineDetails retVal = null;
        if ( text == null ||  text.length() == 0)
        {
            return retVal;
        }

        retVal = new LineDetails();

        int width = getClientArea().width;
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

                // Find the next non-whitespace, start from the previous end
                // point, since that was the end of the line.
                index = findFirstNonWhitespace(label, previousEnd);
                startLine = index;
            }
        }

        return retVal;
    }

    /**
     *
     * @param label
     * @param start
     * @return
     */
    protected int findFirstNonWhitespace(StringBuilder label, int start)
    {
        int retVal = -1;
        for(int index = start; index < label.length(); index++)
        {
            int codePointAt = label.codePointAt(index);
            if(!Character.isWhitespace(codePointAt))
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

    /**
     * find end of word using whitespace separation(space, linebreak etc)
     * consist from one whitespace symbol at least.
     * @param label
     * @param start
     * @return
     */
    protected int findEndOfNextWord(StringBuilder label, int start)
    {
        int retVal = -1;

        if((start >= 0) && (start < label.length()))
        {
            retVal = label.length();
            for (int index = start+1; index < label.length(); index++)
            {
                int codePoint = label.codePointAt(index);
                if(Character.isWhitespace(codePoint))//currently split only on whitespace, may be consider to split on any not letter-digit
                {
                    retVal = index;
                    break;
                }
            }
        }

        return retVal;
    }

    /**
     * Rounds Rectangle2D to Rectangle.
     * @param rectangle the rectangle2D
     * @return the rectangle
     */
    public Rectangle roundRectangle (Rectangle2D rectangle) {
        return rectangle.getBounds();
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
