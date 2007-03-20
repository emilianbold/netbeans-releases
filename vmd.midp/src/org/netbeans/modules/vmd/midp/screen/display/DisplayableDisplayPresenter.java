/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;

/**
 * A presenter for Displayable MIDP class. ALl other presenters should
 * inherit from this presenter (e.g. TextBoxDisplayPresenter,
 * FormDisplayPresenter, ...)
 *
 * @author breh
 */
public class DisplayableDisplayPresenter extends ScreenDisplayPresenter {
    
    private static final Image BATTERY = Utilities.loadImage("org/netbeans/modules/vmd/midp/screen/display/resources/battery.png"); // NOI18N
    private static final Image SIGNAL = Utilities.loadImage("org/netbeans/modules/vmd/midp/screen/display/resources/signal.png"); // NOI18N
    
    private DisplayableDisplayPanel panel;
    
    public DisplayableDisplayPresenter() {
        panel = new DisplayableDisplayPanel(this);
        panel.getBattery().setIcon(new ImageIcon(BATTERY));
        panel.getSignal().setIcon(new ImageIcon(SIGNAL));
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
        panel.setBorder(deviceInfo.getDeviceTheme().getBorder(getComponent().getDocument().getSelectedComponents().contains(getComponent())));
        DesignComponent ticker = getComponent().readProperty(DisplayableCD.PROP_TICKER).getComponent();
        String tickerText = "<ticker not set>"; // NOI18N
        if (ticker != null) {
            tickerText = MidpValueSupport.getHumanReadableString(ticker.readProperty(TickerCD.PROP_STRING));
        }
        panel.getTicker().setText(tickerText);
        panel.getTitle().setText(MidpValueSupport.getHumanReadableString(getComponent().readProperty(DisplayableCD.PROP_TITLE)));
    }
    
}
