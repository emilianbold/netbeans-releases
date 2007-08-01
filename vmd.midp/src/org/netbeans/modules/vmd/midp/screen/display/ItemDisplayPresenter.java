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
package org.netbeans.modules.vmd.midp.screen.display;

import org.netbeans.modules.vmd.midp.screen.display.ScreenMoveArrayAcceptSuggestion;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpValueSupport;
import org.netbeans.modules.vmd.midp.components.items.ItemCD;
import org.netbeans.modules.vmd.midp.screen.display.property.ScreenStringPropertyEditor;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayDataFlavorSupport;
import org.openide.util.Exceptions;

/**
 * @author David Kaspar
 */
public class ItemDisplayPresenter extends ScreenDisplayPresenter {
    
    private JPanel panel;
    private JLabel label;
    private JComponent contentComponent;
    private Transferable transferable;
    
    public ItemDisplayPresenter() {
        panel = new JPanel() {
            @Override
            public JPopupMenu getComponentPopupMenu() {
                return Utilities.actionsToPopup(ActionsSupport.createActionsArray(getRelatedComponent()), this);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        
        label = new JLabel();
        Font bold = label.getFont().deriveFont(Font.BOLD);
        label.setFont(bold);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.REMAINDER;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.NORTH;
        panel.add(label, constraints);
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
    
    protected final JPanel getViewPanel() {
        return panel;
    }
    
    protected final void setContentComponent(JComponent contentComponent) {
        panel.setVisible(false);
        if (this.contentComponent != null) {
            panel.remove(this.contentComponent);
        }
        this.contentComponent = contentComponent;
        if (contentComponent != null) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.insets = new Insets(0, 0, 0, 0);
            constraints.fill = GridBagConstraints.BOTH;
            constraints.gridx = GridBagConstraints.REMAINDER;
            constraints.gridy = GridBagConstraints.RELATIVE;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            
            panel.add(contentComponent, constraints);
        }
        panel.setVisible(true);
        panel.invalidate();
        panel.validate();
        panel.repaint();
    }
    
    public void reload(ScreenDeviceInfo deviceInfo) {
        String text = MidpValueSupport.getHumanReadableString(getComponent().readProperty(ItemCD.PROP_LABEL));
        label.setText(text);
    }
    
    public Shape getSelectionShape() {
        return new Rectangle(panel.getSize());
    }
    
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Collections.singleton(
                new ScreenPropertyDescriptor(getComponent(), label, new ScreenStringPropertyEditor(ItemCD.PROP_LABEL))
                );
    }
    
    @Override
    public boolean isDraggable() {
        return true;
    }
    
    @Override
    public AcceptSuggestion createSuggestion(Transferable transferable) {
        if (!(transferable.isDataFlavorSupported(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR)))
            return null;
        if (!(transferable.isDataFlavorSupported(ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR)))
            return null;
        
        ScreenDeviceInfo.Edge horizontalPosition = null;
        ScreenDeviceInfo.Edge verticalPosition = null;
        
        try {
            horizontalPosition = (ScreenDeviceInfo.Edge) transferable.getTransferData(ScreenDisplayDataFlavorSupport.HORIZONTAL_POSITION_DATA_FLAVOR);
            verticalPosition = (ScreenDeviceInfo.Edge) transferable.getTransferData(ScreenDisplayDataFlavorSupport.VERTICAL_POSITION_DATA_FLAVOR);
        } catch (UnsupportedFlavorException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new ScreenMoveArrayAcceptSuggestion(horizontalPosition, verticalPosition);
    }
    
}
