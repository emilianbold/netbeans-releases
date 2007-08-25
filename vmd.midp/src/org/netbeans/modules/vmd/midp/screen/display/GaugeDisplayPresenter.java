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

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.api.model.PropertyValue;
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
    
    private JPanel panel;
    private GaugeDisplayPresenterElement gauge;
    
    public GaugeDisplayPresenter() {
        gauge = new GaugeDisplayPresenterElement();
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                gauge.setPanel(this);
                gauge.paintGauge(g);
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(200, 40)); // TODO compute it from fontSize
        setContentComponent(panel);
        panel.repaint();
        panel.revalidate();
    }
    
    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        super.reload(deviceInfo);
        gauge.setSize(panel.getSize());
        
        PropertyValue pv = getComponent().readProperty(GaugeCD.PROP_INTERACTIVE);
        if (PropertyValue.Kind.VALUE.equals(pv.getKind())) {
            gauge.setInteractive(MidpTypes.getBoolean(pv));
        }
        
        pv = getComponent().readProperty(GaugeCD.PROP_MAX_VALUE);
        int maxValue = 1;
        if (PropertyValue.Kind.VALUE.equals(pv.getKind())) {
            maxValue = MidpTypes.getInteger(pv);
        }
        if (maxValue < 0) {
            maxValue = 1;
        }
        gauge.setMaxValue(maxValue);
        
        pv = getComponent().readProperty(GaugeCD.PROP_MAX_VALUE);
        int value = 0;
        if (PropertyValue.Kind.VALUE.equals(pv.getKind())) {
            value = MidpTypes.getInteger(pv);
        }
        if (value < 0) {
            value = 0;
        } else if (value > maxValue) {
            value = maxValue;
        }
        gauge.setValue(value);
        
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
