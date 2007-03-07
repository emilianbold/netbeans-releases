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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.vmd.screen.device;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.screen.ScreenAccessController;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class DevicePanel extends JPanel {

    private static final Color BACKGROUND_COLOR = new Color (0xFCFAF5);

    private final ScreenDisplayPresenter dummyPresenter = new DummyDisplayPresenter ();

    private ScreenAccessController controller;
    private ScreenDeviceInfo deviceInfo;

    private JPanel displayPanel;
    private TopPanel topPanel;

    public DevicePanel (ScreenAccessController controller) {
        this.controller = controller;
        setBackground (BACKGROUND_COLOR);

        topPanel = new TopPanel (this);
        
        displayPanel = new JPanel ();
        displayPanel.setLayout (new BorderLayout ());
        displayPanel.setBackground (BACKGROUND_COLOR);

        initializeUI ();
    }

    private ScreenDeviceInfo getDeviceInfo() {
        if (deviceInfo == null) {
            deviceInfo = new ScreenDeviceInfo ();
        }
        return deviceInfo;
    }

    public ScreenAccessController getController () {
        return controller;
    }

    private void initializeUI () {
        setLayout(new GridBagLayout ());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.TOP_LEFT), constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.TOP),constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.TOP_RIGHT),constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.VERTICAL;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.LEFT),constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(displayPanel,constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(topPanel,constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.VERTICAL;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.RIGHT),constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.BOTTOM_LEFT),constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.BOTTOM),constraints);

        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.BOTTOM_RIGHT),constraints);

        JPanel fillPanel = new JPanel ();
        fillPanel.setBackground (BACKGROUND_COLOR);
        add(fillPanel, new GridBagConstraints (0, 3, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 0));
    }

    public void reload () {
        DesignComponent editedScreen = controller.getEditedScreen ();
        ScreenDisplayPresenter presenter = editedScreen != null ? editedScreen.getPresenter (ScreenDisplayPresenter.class) : null;
        if (presenter == null)
            presenter = dummyPresenter;
        displayPanel.removeAll ();
        displayPanel.add (presenter.getView (), BorderLayout.CENTER);
        displayPanel.setBackground (getDeviceInfo ().getDeviceTheme ().getColor (ScreenDeviceInfo.DeviceTheme.COLOR_BACKGROUND));

        presenter.reload (getDeviceInfo ());

        Dimension size = getDeviceInfo ().getCurrentScreenSize ();
        displayPanel.setMinimumSize (new Dimension (size.width, 0));
        displayPanel.setPreferredSize (size);
        displayPanel.setMaximumSize (new Dimension (size.width, Integer.MAX_VALUE));

        topPanel.reload ();
    }

    public DesignComponent getDesignComponentAt (Point point) {
        return getDesignComponentAt (controller.getEditedScreen (), displayPanel, point);
    }

    private DesignComponent getDesignComponentAt (DesignComponent component, JComponent parentView, Point point) {
        if (component == null)
            return null;
        ScreenDisplayPresenter presenter = component.getPresenter (ScreenDisplayPresenter.class);
        if (presenter == null)
            return null;
        JComponent view = presenter.getView ();
        Component c = view;
        Point viewPoint = new Point (point);
        for (;;) {
            if (c == null)
                return null;
            if (c == parentView)
                break;
            Point childPoint = c.getLocation ();
            viewPoint.x -= childPoint.x;
            viewPoint.y -= childPoint.y;
            c = c.getParent ();
        }
        for (DesignComponent child : presenter.getChildren ()) {
            DesignComponent ret = getDesignComponentAt (child, view, viewPoint);
            if (ret != null)
                return ret;
        }
        return presenter.getSelectionShape ().contains (viewPoint) ? presenter.getRelatedComponent () : null;
    }

}
