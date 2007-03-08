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

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontFace;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo.DeviceTheme.FontSize;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.sources.ListElementEventSourceCD;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel
 */
public class ListElementEventSourceDisplayPresenter extends ScreenDisplayPresenter {
    
    private JPanel panel;
    private JLabel label;
    private JComponent contentComponent;
    
    public ListElementEventSourceDisplayPresenter() {
        panel = new JPanel() {
            public JPopupMenu getComponentPopupMenu() {
                return Utilities.actionsToPopup(ActionsSupport.createActionsArray(getRelatedComponent()), this);
            }
        };
        panel.setLayout(new BorderLayout());
        label = new JLabel();
        panel.add(label, BorderLayout.NORTH);
    }
    
    public boolean isTopLevelDisplay() {
        return false;
    }
    
    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }
    
    public JComponent getView() {
        return panel;
    }
    
    protected JPanel getPanel() {
        return panel;
    }
    
    protected void setContentComponent(JComponent contentComponent) {
        if (this.contentComponent != null) {
            panel.remove(this.contentComponent);
        }
        this.contentComponent = contentComponent;
        if (contentComponent != null) {
            panel.add(contentComponent, BorderLayout.CENTER);
        }
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        panel.setBorder(deviceInfo.getDeviceTheme().getBorder(getComponent().getDocument().getSelectedComponents().contains(getComponent())));
        label.setText(MidpValueSupport.getHumanReadableString(getComponent().readProperty(ListElementEventSourceCD.PROP_STRING)));
        
        DesignComponent font = getComponent().readProperty(ListElementEventSourceCD.PROP_FONT).getComponent();
        if (font != null) {
            int faceCode = MidpTypes.getInteger(font.readProperty(FontCD.PROP_FACE));
            FontFace face = FontFace.SYSTEM;
            if (faceCode == FontCD.VALUE_FACE_MONOSPACE) {
                face = FontFace.MONOSPACE;
            } else if (faceCode == FontCD.VALUE_FACE_PROPORTIONAL) {
                face = FontFace.PROPORTIONAL;
            }
            
            int sizeCode = MidpTypes.getInteger(font.readProperty(FontCD.PROP_SIZE));
            FontSize size = FontSize.MEDIUM;
            if (sizeCode == FontCD.VALUE_SIZE_SMALL) {
                size = FontSize.SMALL;
            } else if (sizeCode == FontCD.VALUE_SIZE_LARGE) {
                size = FontSize.LARGE;
            }

            label.setFont(deviceInfo.getDeviceTheme().getFont(face, size));
        }
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(panel.getSize());
    }
    
}
