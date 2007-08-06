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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.usercode.PropertyEditorUserCode;
import org.openide.explorer.propertysheet.InplaceEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.swing.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyEditorColorChooser extends PropertyEditorUserCode implements PropertyEditorElement {

    private static final String ERROR_WARNING = NbBundle.getMessage(PropertyEditorArrayInteger.class, "LBL_ARRAY_INTEGER_DIALOG"); //NOI18N
    private static final String BUTTON_NAME = NbBundle.getMessage(PropertyEditorArrayInteger.class, "LBL_BUTTON_NAME"); //NOI18N
    private static final String NULL = NbBundle.getMessage(PropertyEditorColorChooser.class, "LBL_STRING_USER_CODE"); //NOI18N
    
    private JColorChooser customEditorElement;
    private JRadioButton radioButton;
    private InplaceEditor inplaceEditor;
    private boolean supportsCustomEditor;

    public PropertyEditorColorChooser(boolean supportsCustomEditor) {
        this.supportsCustomEditor = supportsCustomEditor;
        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplaceEditor == null) {
            inplaceEditor = new PickColorInplaceEditor();
        }
        return inplaceEditor;
    }

    @Override
    public void paintValue(Graphics g, Rectangle box) {
        PropertyValue value = (PropertyValue) super.getValue();
        Integer rgb = null;
        if (value.getPrimitiveValue() instanceof Integer) {
            rgb = (Integer) value.getPrimitiveValue();
        } else {
            return;
        }
        
        g.drawRect(box.x + 1, box.y + 2, 11, 11);
        Color color = new Color(rgb);
        StringBuffer str = new StringBuffer();
        str.append('['); //NOI18N
        str.append(color.getRed());
        str.append(','); //NOI18N
        str.append(color.getGreen());
        str.append(','); //NOI18N
        str.append(color.getBlue());
        str.append(']'); //NOI18N
        g.drawString(str.toString(), box.x + 18, box.y + 11);
        g.setColor(color);
        g.fillRect(box.x + 2, box.y + 3, 10, 10);
        g.dispose();
    }


    @Override
    public boolean supportsCustomEditor() {
        if (!supportsCustomEditor) {
            return false;
        }
        return super.supportsCustomEditor();
    }

    public JComponent getCustomEditorComponent() {
        if (customEditorElement == null) {
            customEditorElement = new JColorChooser();
        }
        return customEditorElement;
    }

    public JRadioButton getRadioButton() {
        if (radioButton == null) {
            radioButton = new JRadioButton(BUTTON_NAME);
             Mnemonics.setLocalizedText(radioButton, BUTTON_NAME);
        }
        return radioButton;
    }

    @Override
    public boolean isPaintable() {
        PropertyValue propertyValue = (PropertyValue) getValue();
        return propertyValue.getKind() == PropertyValue.Kind.VALUE;
    }

    public boolean isVerticallyResizable() {
        return true;
    }

    public boolean isInitiallySelected() {
        return false;
    }
    
    @Override
    public String getAsText() {
        if (isCurrentValueAUserCodeType()) {
            return USER_CODE_TEXT;
        } else if (isCurrentValueANull()) {
            return NULL;
        }
        PropertyValue value = (PropertyValue) getValue();
        if (value.getKind() == PropertyValue.Kind.VALUE && value.getPrimitiveValue() instanceof Integer) {
            return getFormatedRGB((Integer) value.getPrimitiveValue());
        }
        throw new IllegalStateException();
    }

    private String getFormatedRGB(Integer rgb) {
        Color color = new Color(rgb);
        StringBuffer stringRGB = new StringBuffer();
        stringRGB.append('['); //NOI18N
        stringRGB.append(color.getRed());
        stringRGB.append(','); //NOI18N
        stringRGB.append(color.getGreen());
        stringRGB.append(','); //NOI18N
        stringRGB.append(color.getBlue());
        stringRGB.append(']'); //NOI18N
        return stringRGB.toString();
    }

    public void setTextForPropertyValue(String text) {
    }

    public String getTextForPropertyValue() {
        return ""; //NOI18N
    }

    public void updateState(PropertyValue value) {
        radioButton.setSelected(!isCurrentValueAUserCodeType());
        if (value == null || value.getKind() != PropertyValue.Kind.VALUE || value.getKind() == PropertyValue.Kind.NULL) {
            return;
        }
        if (!(value.getPrimitiveValue() instanceof Integer)) {
            throw new IllegalStateException();
        }
        customEditorElement.setColor(new Color((Integer) value.getPrimitiveValue()));
    }

    private void saveValue(Integer rgb) {
        PropertyValue rgbValue = MidpTypes.createIntegerValue(rgb);
        setValue(rgbValue);
        JTextField tf = (JTextField) inplaceEditor.getComponent();
        tf.setText(getFormatedRGB(rgb));
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditorElement.getColor().getRGB());
        }
    }

    @Override
    public boolean canWrite() {
        return MidpPropertyEditorSupport.singleSelectionEditAsTextOnly();
    }

    private class PickColorInplaceEditor implements InplaceEditor {

        private PropertyModel model;
        private JTextField component;

        public void connect(PropertyEditor pe, PropertyEnv env) {
        }

        public JComponent getComponent() {
            if (component == null) {
                component = new JTextField();
                PropertyValue value = (PropertyValue) getValue();
                if (value.getKind() == PropertyValue.Kind.VALUE && value.getPrimitiveValue() instanceof Integer) {
                    component.setText(getFormatedRGB((Integer) value.getPrimitiveValue()));
                }
            }
            component.selectAll();
            return component;
        }

        public void clear() {
        }

        public Object getValue() {
            return PropertyEditorColorChooser.this.getValue();
        }

        public void setValue(Object o) {
        }

        public boolean supportsTextEntry() {
            return true;
        }

        public void reset() {
        }

        public void addActionListener(ActionListener al) {
        }

        public void removeActionListener(ActionListener al) {
        }

        public KeyStroke[] getKeyStrokes() {
            return null;
        }

        public PropertyEditor getPropertyEditor() {
            return PropertyEditorColorChooser.this;
        }

        public PropertyModel getPropertyModel() {
            if (model == null) {
                model = new ColorPropertyModel(component);
            }
            return model;
        }

        public void setPropertyModel(PropertyModel pm) {
        }

        public boolean isKnownComponent(Component c) {
            return true;
        }
    }

    private class ColorPropertyModel implements PropertyModel {

        private JTextField component;

        ColorPropertyModel(JTextField component) {
            this.component = component;
        }

        public Object getValue() throws InvocationTargetException {
            return component.getText();
        }

        public void setValue(Object o) throws InvocationTargetException {
            String text = component.getText();
            text = text.trim().replaceAll(Pattern.compile("[\\[$\\]]").pattern(), ""); //NOI18N
            if (Pattern.compile("[^0123456789,]").matcher(text).find() || text.split(",").length != 3) { //NOI18N
                PropertyValue value = (PropertyValue) o;
                component.setText(getFormatedRGB((Integer) value.getPrimitiveValue()));
                wrongValueWarning((PropertyValue) o, text);
                return;
            }
            int[] colors = new int[3];
            int i = 0;
            for (String number : text.split(",")) { //NOI18N
                try {
                    colors[i++] = Integer.valueOf(number);
                } catch (NumberFormatException ex) {
                    wrongValueWarning((PropertyValue) o, text);
                    return;
                }
                if (Integer.valueOf(number) < 0 || Integer.valueOf(number) > 255) {
                    wrongValueWarning((PropertyValue) o, text);
                    return;
                }
            }
            int rgb = new Color(colors[0], colors[1], colors[2]).getRGB();
            PropertyEditorColorChooser.this.saveValue(rgb);
            PropertyEditorColorChooser.this.invokeSaveToModel();
        }

        public Class getPropertyType() {
            return PropertyEditorColorChooser.class;
        }

        public Class getPropertyEditorClass() {
            return PropertyEditorColorChooser.class;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        private void wrongValueWarning(PropertyValue value, String text) {
            component.setText(getFormatedRGB((Integer) value.getPrimitiveValue()));
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERROR_WARNING + ' ' + text)); //NOI18N
        }
    }
}
