/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 * @author Karol Harezlak
 */
public final class PropertyEditorComboBox extends PropertyEditorUserCode implements PropertyEditorElement {

    private final Map<String, PropertyValue> values;
    private String[] tags;
    private String valueLabel;

    private TypeID typeID;
    private TypeID enableTypeID;

    private CustomEditor customEditor;
    private JRadioButton radioButton;
    private static String[] USERCODE_TAGS =  new String[]{(PropertyEditorUserCode.USER_CODE_TEXT)};

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        typeID = null;
        tags = null;
        if (customEditor != null) {
            customEditor.cleanUp();
            customEditor = null;
        }
        radioButton = null;
        enableTypeID = null;
    }
    
    private PropertyEditorComboBox(Map<String, PropertyValue> values, TypeID typeID,
            TypeID enableTypeID, String valueLabel, String userCodeLabel) {
        super(userCodeLabel);

        this.values = values;
        this.typeID = typeID;
        this.enableTypeID = enableTypeID;
        this.valueLabel = valueLabel;
        createTags();
        initComponents();

        initElements(Collections.<PropertyEditorElement>singleton(this));
    }

    public static PropertyEditorComboBox createInstance(Map<String, PropertyValue> values,
            TypeID typeID, String valueLabel, String userCodeLabel) {
        return createInstance(values, typeID, null, valueLabel, userCodeLabel);
    }

    public static PropertyEditorComboBox createInstance(Map<String, PropertyValue> values,
            TypeID typeID, TypeID enableTypeID, String valueLabel, String userCodeLabel) {
        if (values == null) {
            throw new IllegalArgumentException("Argument values can't be null"); // NOI18N
        }
        for (String key : values.keySet()) {
            PropertyValue value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("PropertyValue for " + key + " key can't be null"); // NOI18N
            }
        }

        PropertyEditorComboBox instance = new PropertyEditorComboBox(values, typeID, enableTypeID, valueLabel, userCodeLabel);
        return instance;
    }

    private void initComponents() {
        radioButton = new JRadioButton();
        Mnemonics.setLocalizedText(radioButton, valueLabel);
        
        radioButton.getAccessibleContext().setAccessibleName( radioButton.getText());
        radioButton.getAccessibleContext().setAccessibleDescription( radioButton.getText());
        
        customEditor = new CustomEditor();
        customEditor.updateModel();
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
        return false;
    }

    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }

        PropertyValue value = (PropertyValue) super.getValue();
        for (String key : values.keySet()) {
            PropertyValue tmpValue = values.get(key);
            if (value.getPrimitiveValue().equals(tmpValue.getPrimitiveValue())) {
                return key;
            }
        }
        return NbBundle.getMessage(PropertyEditorComboBox.class, "LBL_MULTIPLE"); // NOI18N
    }

    public void setTextForPropertyValue(String text) {
        saveValue(text);
    }

    public String getTextForPropertyValue() {
        return null;
    }

    public void updateState(PropertyValue value) {
        if (isCurrentValueANull() || value == null) {
            // clear customEditor if needed
        } else {
            customEditor.setValue(value);
        }
        radioButton.setSelected(!isCurrentValueAUserCodeType());
    }

    private void saveValue(String text) {
        if (text.length() > 0) {
            PropertyValue value = values.get(text);
            if (value != null) {
                super.setValue(value);
            }
        }
    }

    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (radioButton.isSelected()) {
            saveValue(customEditor.getText());
        }
    }

    @Override
    public String[] getTags() {
        if (isCurrentValueAUserCodeType()) {
            return USERCODE_TAGS;
        } 
        return tags;
    }

    private void createTags() {
        int i = 0;
        tags = new String[values.size()];
        for (String valueAsText : values.keySet()) {
            tags[i++] = valueAsText;
        }
    }

    @Override
    public Boolean canEditAsText() {
        return null;
    }

    @Override
    public boolean canWrite() {
        final boolean[] canWrite = new boolean[]{true};
        if (!MidpPropertyEditorSupport.singleSelectionEditAsTextOnly()) {
            canWrite[0] = false;
        } else if (enableTypeID != null) {
            if (enableTypeID == FontCD.TYPEID) {
                if (component != null && component.get() != null) {
                    final DesignComponent _component = component.get();
                    _component.getDocument().getTransactionManager().readAccess(new Runnable() {

                        public void run() {
                            int kind = MidpTypes.getInteger(_component.readProperty(FontCD.PROP_FONT_KIND));
                            canWrite[0] = kind == FontCD.VALUE_KIND_CUSTOM;
                        }
                    });
                }
            }
        }
        return canWrite[0];
    }

    private class CustomEditor extends JPanel implements ActionListener {

        private JComboBox combobox;

        void cleanUp() {
            combobox.removeActionListener(this);
            combobox = null;
            this.removeAll();
        }

        public CustomEditor() {
            initComponents();
        }

        private void initComponents() {
            setLayout(new BorderLayout());
            combobox = new JComboBox();
            combobox.setModel(new DefaultComboBoxModel());
            combobox.addActionListener(this);
            
            combobox.getAccessibleContext().setAccessibleName( 
                    radioButton.getAccessibleContext().getAccessibleName());
                    combobox.getAccessibleContext().setAccessibleDescription( 
                    radioButton.getAccessibleContext().getAccessibleDescription());
            
            add(combobox, BorderLayout.CENTER);
        }

        public void setValue(PropertyValue value) {
            for (String key : values.keySet()) {
                if (values.get(key).getPrimitiveValue().equals(value.getPrimitiveValue())) {
                    combobox.setSelectedItem(key);
                    break;
                }
            }
        }

        public String getText() {
            return (String) combobox.getSelectedItem();
        }

        public void updateModel() {
            DefaultComboBoxModel model = (DefaultComboBoxModel) combobox.getModel();
            model.removeAllElements();
            for (String tag : tags) {
                model.addElement(tag);
            }
        }

        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
}
