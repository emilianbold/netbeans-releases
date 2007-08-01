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

package org.netbeans.modules.vmd.midp.screen.display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.items.GaugeCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class GaugeDisplayPresenter extends ItemDisplayPresenter {
    
    private static final int PANEL_HEIGHT = 40;
    private static final int BAR_WIDTH = 8;
    private static final int BAR_GAP = 5;
    private static final BasicStroke GAUGE_STROKE = new BasicStroke(1, BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_BEVEL, 0, new float[] {3,2}, 0);
    
    private JPanel panel;
    private Dimension size;
    private boolean interactive;
    private int maxValue;
    private int value;
    
    public GaugeDisplayPresenter() {
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                paintGauge(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(200, 40)); // TODO compute it from fontSize
        setContentComponent(panel);
        panel.repaint();
        panel.revalidate();
    }
    
    private void paintGauge(Graphics g) {
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
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        
        size = panel.getSize();
        interactive = MidpTypes.getBoolean(getComponent().readProperty(GaugeCD.PROP_INTERACTIVE));
        maxValue = MidpTypes.getInteger(getComponent().readProperty(GaugeCD.PROP_MAX_VALUE));
        if (maxValue < 0) {
            maxValue = 1;
        }
        value = MidpTypes.getInteger(getComponent().readProperty(GaugeCD.PROP_VALUE));
        if (value < 0) {
            value = 0;
        } else if (value > maxValue) {
            value = maxValue;
        }
        
        panel.repaint();
    }
    
    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        ResourcePropertyEditor gaugePropertyEditor = new ResourcePropertyEditor(GaugeCD.PROP_VALUE, getComponent());
        List<ScreenPropertyDescriptor> descriptors = new ArrayList<ScreenPropertyDescriptor>();
        descriptors.addAll(super.getPropertyDescriptors());
        descriptors.add(new ScreenPropertyDescriptor(getComponent(), panel, gaugePropertyEditor));
        return descriptors;
    }
}
