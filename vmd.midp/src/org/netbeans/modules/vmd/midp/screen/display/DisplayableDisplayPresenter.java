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
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
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
                    if (tickerText == null || tickerText.length() == 0)
                        tickerText = NbBundle.getMessage(DisplayableDisplayPresenter.class, "DISP_empty_ticker_string"); // NOI18N
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
