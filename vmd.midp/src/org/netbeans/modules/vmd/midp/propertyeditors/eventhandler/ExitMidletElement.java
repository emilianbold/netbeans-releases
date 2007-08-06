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
 */

package org.netbeans.modules.vmd.midp.propertyeditors.eventhandler;

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.categories.PointsCategoryCD;
import org.netbeans.modules.vmd.midp.components.handlers.ExitMidletEventHandlerCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.List;

/**
 *
 * @author Anton Chechel
 */
public class ExitMidletElement implements PropertyEditorEventHandlerElement {
    private JRadioButton radioButton;
    
    public ExitMidletElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(ExitMidletElement.class, "LBL_EXIT_MIDLET")); // NOI18N
    }
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(eventSource.getDocument(), PointsCategoryCD.TYPEID);
        List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
        if (list.size() != 1) {
            Debug.warning("Can not retrieve MobileDevice from document"); // NOI18N
            return;
        }
        DesignComponent mobileDevice = list.get(0);
        MidpDocumentSupport.updateEventHandlerFromTarget(eventSource, mobileDevice);
    }
    
    public void setTextForPropertyValue (String text) {
    }
    
    public JComponent getCustomEditorComponent() {
        return null;
    }
    
    public JRadioButton getRadioButton() {
        return radioButton;
    }
    
    public boolean isInitiallySelected() {
        return false;
    }
    
    public boolean isVerticallyResizable() {
        return false;
    }
    
    public void updateModel(List<DesignComponent> components, int modelType) {
    }
    
    public String getTextForPropertyValue () {
        return "";
    }
    
    public void updateState(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            if (eventHandler.getType().equals(ExitMidletEventHandlerCD.TYPEID)) {
                radioButton.setSelected(true);
            }
        }
    }
    
    public void setElementEnabled(boolean enabled) {
        radioButton.setEnabled(enabled);
    }
    
    public static class ExitMidletElementFactory implements PropertyEditorElementFactory {
        public PropertyEditorEventHandlerElement createElement() {
            return new ExitMidletElement();
        }
    }
}
