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

package org.netbeans.modules.vmd.midp.propertyeditors.eventhandler;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.handlers.ListEventHandlerCD;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorElementFactory;
import org.netbeans.modules.vmd.midp.propertyeditors.element.PropertyEditorEventHandlerElement;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 * @version 1.0
 */
public class ListActionElement implements PropertyEditorEventHandlerElement {
    private JRadioButton radioButton;
    
    public ListActionElement() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(ListActionElement.class, "LBL_LIST_ACTION")); // NOI18N
    }
    
    public void createEventHandler(DesignComponent eventSource) {
        if (!radioButton.isSelected()) {
            return;
        }
        DesignComponent prevScreenEventHandler = eventSource.getDocument().createComponent(ListEventHandlerCD.TYPEID);
        MidpDocumentSupport.updateEventHandlerWithNew(eventSource, prevScreenEventHandler);
    }
    
    public void setText(String text) {
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
    
    public String getText() {
        return ""; // NOI18N
    }
    
    public void setPropertyValue(PropertyValue value) {
        if (value != null) {
            DesignComponent eventHandler = value.getComponent();
            if (eventHandler.getType().equals(ListEventHandlerCD.TYPEID)) {
                radioButton.setSelected(true);
            }
        }
    }
    
    public void setEnabled(boolean enabled) {
    }
    
    public static class ListActionElementFactory implements PropertyEditorElementFactory {
        public PropertyEditorEventHandlerElement createElement() {
            return new ListActionElement();
        }
    }
}
