/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AnnotatedBorderPanel.java
 *
 * Created on June 4, 2006, 6:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author girix
 */
public abstract class AnnotatedBorderPanel extends  ABEBaseDropPanel{
    
    private static final long serialVersionUID = 7526472295622776147L;
    private boolean drawAnnotation = true;
    
    /** Creates a new instance of AnnotatedBorderPanel */
    public AnnotatedBorderPanel(InstanceUIContext context) {
        super(context);
    }
    
    public void setDrawAnnotation(boolean drawAnnotation){
        this.drawAnnotation = drawAnnotation;
    }
    
    Point startPoint = null;
    void setStartDrawPoint(Point startPoint){
        this.startPoint = startPoint;
    }
    
    String annotationString;
    void setAnnotationString(String annotationString){
        this.annotationString = annotationString;
    }
    
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        
        //dont draw anthing if not required
        if(!drawAnnotation){
            return;
        }
        
        Rectangle area = getBounds();
        //int width = area.width - 10;
        int height = area.height;
        Stroke stroke = g2d.getStroke();
        if(shouldDrawBorder()){
            Color oldColor = g2d.getColor();
            g2d.setColor(borderColor);
            Stroke drawingStroke2 =
                    new BasicStroke(
                    2,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10,
                    new float[] {5},
                    0
                    );
            
            g2d.setStroke((drawingStroke2));
            g2d.drawRoundRect(area.x, area.y+1, area.width , area.height -2 , 10, 10);
            g2d.setColor(oldColor);
        }
        
        
        
        g2d.setPaint(Color.lightGray);
        float[] dashPattern = { 2, 2 };
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        //left
        g2d.drawLine(startPoint.x, startPoint.y, startPoint.x, startPoint.y + height);
        
        int FONTSIZE = new JLabel().getFont().getSize();
        
        Font oldFont = g2d.getFont();
        Color stringColor = Color.lightGray;
        if(drawBoldString){
            Font font = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
            g2d.setFont(font);
            stringColor = Color.BLACK;
        }
        
        int fWidth = SwingUtilities.computeStringWidth(g2d.getFontMetrics(), "s");
        int strH = (FONTSIZE) * annotationString.length();
        int segments = height / (strH);
        boolean strSeg = false;
        int currentY = startPoint.y;
        int fixedX = startPoint.x - 2 - fWidth;
        for(int i = 0; i<segments; i++){
            if(strSeg){
                //g2d.setPaint(Color.WHITE);
                //clear the area
                //g2d.fillRect(fixedX, currentY, fWidth + 4, strH);
                g2d.setPaint(stringColor);
                currentY += 8;
                //draw chars one below another
                for(char c: annotationString.toCharArray()){
                    g2d.drawString(""+c, fixedX, currentY);
                    currentY += FONTSIZE;
                }
                currentY += FONTSIZE;
                strSeg = false;
            }else{
                strSeg = true;
                currentY += strH;
            }
            
        }
        g2d.setStroke(stroke);
        g2d.setFont(oldFont);
    }
    
    public boolean shouldDrawBorder(){
        return this.drawBorder;
    }
    
    private boolean drawBorder = false;
    
    public void setDrawBorder(boolean drawBorder){
        this.drawBorder = drawBorder;
    }
    
    private Color borderColor = InstanceDesignConstants.XP_ORANGE;
    public void setBorderColor(Color borderColor){
        this.borderColor = borderColor;
    }
    
    boolean drawBoldString = false;
    public void drawBoldString(boolean drawBoldString){
        this.drawBoldString = drawBoldString;
    }
    
}
