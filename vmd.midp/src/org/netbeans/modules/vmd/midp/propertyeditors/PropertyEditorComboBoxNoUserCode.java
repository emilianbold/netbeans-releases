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

import java.util.Map;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public final class PropertyEditorComboBoxNoUserCode extends DesignPropertyEditor {

    private Map<String, PropertyValue> values;
    private String[] tags;
    private TypeID enableTypeID;

    //private CustomEditor customEditor;
    private PropertyEditorComboBoxNoUserCode(Map<String, PropertyValue> values, TypeID enableTypeID) {
        this.values = values;
        this.enableTypeID = enableTypeID;
        createTags();
    }

    public static PropertyEditorComboBoxNoUserCode createInstance(Map<String, PropertyValue> values, TypeID typeID) {

        if (values == null) {
            throw new IllegalArgumentException("Argument values can't be null"); // NOI18N
        }
        for (String key : values.keySet()) {
            PropertyValue value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("PropertyValue for " + key + " key can't be null"); // NOI18N
            }
        }

        return new PropertyEditorComboBoxNoUserCode(values, typeID);
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        tags = null;
        enableTypeID = null;
        values.clear();
        values = null;
    }

    @Override
    public void setAsText(String text) {
        if (canWrite()) {
            saveValue(text);
        }
    }

    private void saveValue(String text) {
        if (text.length() > 0) {
            PropertyValue value = values.get(text);
            if (value != null) {
                super.setValue(value);
            }
        }
    }

//    private void initComponents() {
//        
//        customEditor = new CustomEditor();
//        customEditor.updateModel();
//    }
    @Override
    public boolean supportsCustomEditor() {
        return false;
    }

//    @Override
//    public Component getCustomEditor() {
//        if (customEditor == null)
//            initComponents();
//        return customEditor;
//    }
    @Override
    public String getAsText() {
        PropertyValue value = (PropertyValue) super.getValue();
        for (String key : values.keySet()) {
            PropertyValue tmpValue = values.get(key);
            if (value.getPrimitiveValue().equals(tmpValue.getPrimitiveValue())) {
                return key;
            }
        }
        return NbBundle.getMessage(PropertyEditorComboBoxNoUserCode.class, "LBL_MULTIPLE"); // NOI18N
    }

//    private void saveValue(String text) {
//        if (text.length() > 0) {
//            PropertyValue value = values.get(text);
//            if (value != null) {
//                super.setValue(value);
//            }
//        }
//    }

    //@Override
//    public void customEditorOKButtonPressed() {
//        super.customEditorOKButtonPressed();
//        saveValue(customEditor.getText());
//        
//    }
    @Override
    public String[] getTags() {
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
//    private class CustomEditor extends JPanel  {
//
//        private JComboBox combobox;
//
//        public CustomEditor() {
//            initComponents();
//        }
//
//        private void initComponents() {
//            setLayout(new BorderLayout());
//            combobox = new JComboBox();
//            combobox.setModel(new DefaultComboBoxModel());
//            
//            add(combobox, BorderLayout.CENTER);
//        }
//
//        public void setValue(PropertyValue value) {
//            for (String key : values.keySet()) {
//                if (values.get(key).getPrimitiveValue().equals(value.getPrimitiveValue())) {
//                    combobox.setSelectedItem(key);
//                    break;
//                }
//            }
//        }
//
//        public String getText() {
//            return (String) combobox.getSelectedItem();
//        }
//
//        public void updateModel() {
//            DefaultComboBoxModel model = (DefaultComboBoxModel) combobox.getModel();
//            model.removeAllElements();
//            for (String tag : tags) {
//                model.addElement(tag);
//            }
//        }
//
//        
//    }
}
