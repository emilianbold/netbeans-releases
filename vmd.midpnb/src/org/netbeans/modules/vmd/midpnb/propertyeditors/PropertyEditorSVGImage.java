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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors;

import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElement.DesignComponentWrapper;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElementEvent;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElementListener;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorSVGImage extends PropertyEditorUserCode implements PropertyEditorElement, PropertyEditorResourceElementListener {

    private JRadioButton radioButton;
    private SVGImageEditorElement customEditor;
    private String resourcePath;

    private PropertyEditorSVGImage() {
        super(NbBundle.getMessage(PropertyEditorSVGImage.class, "LBL_SVGIMAGE_UCLABEL")); // NOI18N;
        initComponents();
        
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorSVGImage createInstance() {
        return new PropertyEditorSVGImage();
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorSVGImage.class, "LBL_SVGIMAGE_STR")); // NOI18N;
        
        customEditor = new SVGImageEditorElement();
        customEditor.addPropertyEditorResourceElementListener(this);
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(resourcePath);
        }
    }

    public void updateState(PropertyValue value) {
        if (customEditor.isShowing()) {
            if (value == null) {
                customEditor.setDesignComponentWrapper(null);
            } else if (component != null && component.get() != null) {
                customEditor.setDesignComponentWrapper(new DesignComponentWrapper(component.get()));
            }
            customEditor.setAllEnabled(true);
        }
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }

        PropertyValue value = (PropertyValue) super.getValue();
        return MidpTypes.getString(value);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public JComponent getCustomEditorComponent() {
        return customEditor;
    }

    public JRadioButton getRadioButton() {
        return radioButton;
    }

    public boolean isInitiallySelected() {
        return true;
    }

    public boolean isVerticallyResizable() {
        return true;
    }

    public void elementChanged(PropertyEditorResourceElementEvent event) {
        PropertyValue propertyValue = event.getPropertyValue();
        resourcePath = MidpTypes.getString(propertyValue);
        radioButton.setSelected(true);
    }

    private void saveValue(String text) {
        super.setValue(MidpTypes.createStringValue(text));
    }

}
