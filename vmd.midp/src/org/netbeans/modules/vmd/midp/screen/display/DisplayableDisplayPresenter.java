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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ResourcePropertyEditor;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * A presenter for Displayable MIDP class. ALl other presenters should
 * inherit from this presenter (e.g. TextBoxDisplayPresenter,
 * FormDisplayPresenter, ...)
 *
 * 
 */
public class DisplayableDisplayPresenter extends ScreenDisplayPresenter {
    
    private static final Image BATTERY = ImageUtilities.loadImage("org/netbeans/modules/vmd/midp/screen/display/resources/battery.png"); // NOI18N
    private static final Image SIGNAL = ImageUtilities.loadImage("org/netbeans/modules/vmd/midp/screen/display/resources/signal.png"); // NOI18N
    
    private DisplayableDisplayPanel panel;
    
    public DisplayableDisplayPresenter() {
        panel = new DisplayableDisplayPanel(this);
        panel.getBattery().setIcon(new ImageIcon(BATTERY));
        panel.getSignal().setIcon(new ImageIcon(SIGNAL));

        // Fix for #79636 - Screen designer tab traversal
        ScreenSupport.addKeyboardSupport(this);
    }
    
    public DisplayableDisplayPresenter(Image image) {
        this();
        panel.add(new JLabel(new ImageIcon(image)));
    }
    
    public boolean isTopLevelDisplay() {
        return true;
    }
    
    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }
    
    public JComponent getView() {
        return panel;
    }
    
    protected DisplayableDisplayPanel getPanel() {
        return panel;
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(panel.getSize());
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        String tickerText = null;
        if (getComponent().readProperty(DisplayableCD.PROP_TICKER).getKind() == PropertyValue.Kind.USERCODE)
            tickerText = NbBundle.getMessage(DisplayableDisplayPresenter.class, "DISP_user_code"); // NOI18N
        else {
            DesignComponent ticker = getComponent().readProperty(DisplayableCD.PROP_TICKER).getComponent();
            if (ticker != null) {
                PropertyValue value = ticker.readProperty(TickerCD.PROP_STRING);
                if (value.getKind() == PropertyValue.Kind.USERCODE)
                    tickerText = NbBundle.getMessage(DisplayableDisplayPresenter.class, "DISP_ticker_string_user_code"); // NOI18N
                else {
                    tickerText = MidpValueSupport.getHumanReadableString(value);
                    if (tickerText == null || tickerText.length() == 0) {
                        tickerText = NbBundle.getMessage(DisplayableDisplayPresenter.class, "DISP_empty_ticker_string"); // NOI18N
                    }
                }
            }
        }
        panel.getTicker().setText(tickerText);
        panel.getTitle().setText(MidpValueSupport.getHumanReadableString(getComponent().readProperty(DisplayableCD.PROP_TITLE)));
    }
    
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        DesignComponent ticker = getComponent().readProperty(DisplayableCD.PROP_TICKER).getComponent();
        ScreenPropertyEditor tickerEditor;
        if (ticker == null)
            tickerEditor = new ResourcePropertyEditor(DisplayableCD.PROP_TICKER, getComponent());
        else
            tickerEditor = new ScreenStringPropertyEditor(TickerCD.PROP_STRING, DisplayableCD.PROP_TICKER, JTextField.CENTER);
        return Arrays.asList(
                new ScreenPropertyDescriptor(getComponent(), panel.getTitle(), new ScreenStringPropertyEditor(DisplayableCD.PROP_TITLE, JTextField.CENTER)),
                new ScreenPropertyDescriptor(getComponent(), panel.getTicker(), tickerEditor)
                );
    }
    
}
