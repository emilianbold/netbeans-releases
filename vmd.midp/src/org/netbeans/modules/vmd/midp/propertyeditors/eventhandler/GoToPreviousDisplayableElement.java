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

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.handlers.PreviousScreenEventHandlerCD;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class GoToPreviousDisplayableElement implements PropertyEditorEventHandlerElement {
    private JRadioButton radioButton;
    
    public GoToPreviousDisplayableElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(GoToPreviousDisplayableElement.class, "LBL_PREV_DISPL")); // NOI18N
    }
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        DesignComponent prevScreenEventHandler = eventSource.getDocument().createComponent(PreviousScreenEventHandlerCD.TYPEID);
        MidpDocumentSupport.updateEventHandlerWithNew(eventSource, prevScreenEventHandler);
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
        return ""; // NOI18N
    }
    
    public void updateState(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            if (eventHandler.getType().equals(PreviousScreenEventHandlerCD.TYPEID)) {
                radioButton.setSelected(true);
            }
        }
    }
    
    public void setElementEnabled(boolean enabled) {
    }
    
    public static class GoToPreviousDisplayableElementFactory implements PropertyEditorElementFactory {
        public PropertyEditorEventHandlerElement createElement() {
            return new GoToPreviousDisplayableElement();
        }
    }
}
