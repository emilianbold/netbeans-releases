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
package org.netbeans.modules.vmd.screen.device;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.screen.display.DeviceTheme;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfoPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.screen.ScreenAccessController;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class DevicePanel extends JPanel {

    private static final Color BACKGROUND_COLOR = new Color(0xFBF9F3);
    
    private final ScreenDisplayPresenter dummyPresenter = new DummyDisplayPresenter();
    
    private ScreenAccessController controller;
    //private ScreenDeviceInfo deviceInfo;
    
    private JPanel displayPanel;
    private TopPanel topPanel;
    
    public DevicePanel(ScreenAccessController controller) {
        this.controller = controller;
        setBackground(BACKGROUND_COLOR);
        
        topPanel = new TopPanel(this);
        
        displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());
        displayPanel.setBackground(BACKGROUND_COLOR);
        
        initializeUI();
    }
    
    private ScreenDeviceInfo getDeviceInfo() {
        final ScreenDeviceInfo[] screenDevice = new ScreenDeviceInfo[1];
        final DesignDocument document = controller.getDocument();
        if (document == null)
            return null;
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                DesignComponent rootComponent = document.getRootComponent();
                ScreenDeviceInfoPresenter presenter = rootComponent.getPresenter(ScreenDeviceInfoPresenter.class);
                assert (presenter != null) : "No ScreenDevice attached to the root component"; //NOI18N
                screenDevice[0] = presenter.getScreenDeviceInfo();
            }  
        });
        return screenDevice[0];
    }
    
    public ScreenAccessController getController() {
        return controller;
    }
    
    private void initializeUI() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 3;
        constraints.fill = GridBagConstraints.BOTH;
        add(topPanel,constraints);
        
        constraints = new GridBagConstraints();
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
        //constraints.weighty = 1.0;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.LEFT),constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        add(displayPanel,constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.VERTICAL;
        add(getDeviceInfo().getDeviceBorder(ScreenDeviceInfo.Edge.RIGHT),constraints);
        
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        //constraints.weighty = 0.0;
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
        
        JPanel fillPanel = new JPanel();
        fillPanel.setBackground(BACKGROUND_COLOR);
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weighty = 1.0;
        add(fillPanel, constraints);
    }
    
    public void reload() {

        Component component = KeyboardFocusManager.getCurrentKeyboardFocusManager().
                getFocusOwner();

        DesignComponent editedScreen = controller.getEditedScreen();
        ScreenDisplayPresenter presenter = editedScreen != null ? editedScreen.getPresenter(ScreenDisplayPresenter.class) : null;
        if (presenter == null)
            presenter = dummyPresenter;
        displayPanel.setVisible(false);
        displayPanel.removeAll();
        displayPanel.setPreferredSize(null);

        displayPanel.add(presenter.getView(), BorderLayout.CENTER);
        displayPanel.setBackground(getDeviceInfo().getDeviceTheme().getColor(DeviceTheme.Colors.BACKGROUND));
        
        presenter.reload(getDeviceInfo());
        
        //due to issues in GridBagLayout which ignores minSize, we need to compute necessary height for component
        int requiredHeight = 0;
        Component[] content = presenter.getView().getComponents();
        for (Component jComponent : content) {
            requiredHeight += jComponent.getPreferredSize().getHeight();
            //            GridBagConstraints constrains = ((GridBagLayout)comp.getLayout()).getConstraints(jComponent);
            //            requiredHeight += constrains.insets.top;
            //            requiredHeight += constrains.insets.bottom;
        }
        Dimension size = getDeviceInfo().getCurrentScreenSize();
        displayPanel.setMinimumSize(size);
        //if the size of component is less than required size, force it. Otherwise force the size to computed one
        if (size.height >= requiredHeight){
            displayPanel.setPreferredSize(size);
        } else {
            displayPanel.setPreferredSize(new Dimension(size.width, requiredHeight));
        }
        displayPanel.setMaximumSize(new Dimension(size.width, Integer.MAX_VALUE));
        
        displayPanel.setVisible(true);
        displayPanel.validate();
        
        topPanel.reload();

        if ( component != null ){
            component.requestFocusInWindow();
        }
    }
    
    public DesignComponent getDesignComponentAt(Point point) {
        return getDesignComponentAt(controller.getEditedScreen(), this, point);
    }

    private static DesignComponent getDesignComponentAt(DesignComponent component, JComponent parentView, Point point) {
        if (component == null)
            return null;
        ScreenDisplayPresenter presenter = component.getPresenter(ScreenDisplayPresenter.class);
        if (presenter == null)
            return null;
        JComponent view = presenter.getView();
        Point viewLocation = presenter.getLocation();
        if (viewLocation != null && !viewLocation.equals(view.getLocation())){
            view.setLocation(viewLocation);
        }
        Component c = view;
        Point viewPoint = new Point(point);
        for (;;) {
            if (c == null) {
                return null;
            }
            if (c == parentView) {
                break;
            }
            Point childPoint = c.getLocation();
            viewPoint.x -= childPoint.x;
            viewPoint.y -= childPoint.y;
            c = c.getParent();
        }
        for (DesignComponent child : presenter.getChildren()) {
            DesignComponent ret = getDesignComponentAt(child, view, viewPoint);
            if (ret != null)
                return ret;
        }
        Shape shape = presenter.getSelectionShape();
        if (shape != null && shape.contains(viewPoint)) {
            return presenter.getRelatedComponent();
        }
        return null;
    }
    
    public Point calculateTranslation(Container view, Point viewLocation) {
        Point point = new Point();
        if (viewLocation != null && !viewLocation.equals(view.getLocation())){
            view.setLocation(viewLocation);
        }
        for (;;) {
            if (view == null)
                return null;
            if (view == this)
                break;
            Point childPoint = view.getLocation();
            point.x += childPoint.x;
            point.y += childPoint.y;
            view = view.getParent();
        }
        return point;
    }

    //    /**
    //     * Helper debugging method for inspecting component's hiearchy
    //     */
    //    private void dump(JComponent component){
    //        System.out.println("Type " + component.getClass() + " Layout " + component.getLayout() + " Size: " + component.getSize() + " Preferred size: " + component.getPreferredSize());
    //        Component comps[] = component.getComponents();
    //        for (int i = 0; i < comps.length; i++) {
    //            Component component1 = comps[i];
    //            dump((JComponent)component1);
    //        }
    //    }
    
    public void setScreenSize(Dimension deviceScreenSize) {
        ScreenDeviceInfo deviceInfo = getDeviceInfo();
        if (deviceScreenSize == null)
            deviceScreenSize = new Dimension(240, 320);
        if (deviceScreenSize.equals(deviceInfo.getCurrentScreenSize()))
            return;
        deviceInfo.setArbitraryScreenSize(deviceScreenSize);
        reload();
    }
    
}
