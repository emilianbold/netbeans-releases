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
        
        pv = getComponent().readProperty(GaugeCD.PROP_VALUE);
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
