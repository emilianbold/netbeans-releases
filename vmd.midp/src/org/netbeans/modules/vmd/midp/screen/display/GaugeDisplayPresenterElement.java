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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.screen.display;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Karol Harezlak
 */
public class GaugeDisplayPresenterElement {
    
    private static final int BAR_WIDTH = 8;
    private static final int BAR_GAP = 5;
    private static final BasicStroke GAUGE_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_BEVEL, 0, new float[] {3,2}, 0);
   
    private JPanel panel;
    private Dimension size;
    private int value;
    private int maxValue;
    private boolean interactive;
   
    public void setPanel(JPanel panel) {
        if (panel == null)
            throw new IllegalArgumentException();
        this.panel = panel;
    }
    
    public Dimension getSize() {
        return size;
    }
    
    public void setSize(Dimension size) {
        if (size == null)
            throw new IllegalArgumentException();
        this.size = size;
    }
    
    public void setValue(int value) {
        this.value = value; 
    }
    
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
    
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
    
    public void paintGauge(Graphics g) {
        if (panel == null)
            throw new IllegalArgumentException();
        size = panel.getSize();
        if (size == null) { // did not realoaded yet
            return;
        }
        int gaugeWidth = size.width;
        int gaugeHeight = size.height - 5;
        int barsCount = gaugeWidth / (BAR_WIDTH + BAR_GAP) + 1;
        int selectionBarCount = 0;
        if (value > 0) {
            selectionBarCount = barsCount * value / maxValue ;
        }
        int px = 0;
        
        Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(Color.GRAY);
        g2D.setStroke(GAUGE_STROKE);
        
        float barHeight = (float) gaugeHeight;
        float heightStep = 0f;
        if (interactive) {
            barHeight = 1f;
            heightStep = (float) gaugeHeight / (float) barsCount;
        }
        
        for (int i=0; i < barsCount; i++) {
            if (i < selectionBarCount) {
                g2D.fillRect(px, (int) (gaugeHeight - barHeight), BAR_WIDTH, (int) barHeight);
            } else {
                g2D.drawRect(px, (int) (gaugeHeight - barHeight), BAR_WIDTH, (int) barHeight);
            }
            px += BAR_WIDTH + BAR_GAP;
            if (interactive && (barHeight < gaugeHeight)) {
                barHeight += heightStep;
                if (barHeight > gaugeHeight) {
                    barHeight = gaugeHeight;
                }
            }
        }
    }
    

}
