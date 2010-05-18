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
 */package org.netbeans.modules.vmd.midp.screen.display;

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
