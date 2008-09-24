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

package org.netbeans.modules.vmd.midp.propertyeditors.timezone;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorElement;
import org.netbeans.modules.vmd.midp.propertyeditors.api.usercode.PropertyEditorUserCode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

/**
 *
 * @author Anton Chechel
 */
public class PropertyEditorTimeZone extends PropertyEditorUserCode {

    private PropertyEditorTimeZone.TimeZoneEditor timeZoneEditor;

    private PropertyEditorTimeZone() {
        super(NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_TIME_ZONE_UCLABEL")); // NOI18N

        timeZoneEditor = new TimeZoneEditor ();
        initElements(Collections.<PropertyEditorElement>singleton (timeZoneEditor));
    }
    
    public static PropertyEditorTimeZone createInstance() {
        return new PropertyEditorTimeZone();
    }

    @Override
    public void cleanUp(DesignComponent component) {
        super.cleanUp(component);
        timeZoneEditor.cleanUp();
        timeZoneEditor = null;
    }
    
    @Override
    public String getAsText() {
        String superText = super.getAsText();
        if (superText != null) {
            return superText;
        }
        
        PropertyValue value = (PropertyValue) super.getValue();
        return (String) value.getPrimitiveValue();
    }
    
    private void saveValue(String text) {
        if (text.length() > 0) {
            super.setValue(MidpTypes.createStringValue(text));
        }
    }
    
    @Override
    public void customEditorOKButtonPressed() {
        super.customEditorOKButtonPressed();
        if (timeZoneEditor.getRadioButton().isSelected())
            saveValue(timeZoneEditor.getTextForPropertyValue ());
    }
    
    private final class TimeZoneEditor implements PropertyEditorElement, ActionListener {
        private JRadioButton radioButton;
        private TimeZoneComboboxModel model;
        private JComboBox combobox;
        
        public TimeZoneEditor() {
            radioButton = new JRadioButton();
            Mnemonics.setLocalizedText(radioButton, NbBundle.getMessage(PropertyEditorTimeZone.class, "LBL_TIMEZONE")); // NOI18N
            model = new TimeZoneComboboxModel();
            combobox = new JComboBox(model);
            combobox.setEditable (true);
            combobox.addActionListener(this);
        }

        void cleanUp() {
            radioButton = null;
            model = null;
            combobox.removeActionListener(this);
            combobox = null;
        }
        
        public void updateState(PropertyValue value) {
            if (!isCurrentValueANull() && value != null) {
                String timeZone;
                for (int i = 0; i < model.getSize(); i++) {
                    timeZone = (String) model.getElementAt(i);
                    if (timeZone.equals(value.getPrimitiveValue())) {
                        model.setSelectedItem(timeZone);
                        break;
                    }
                }
            }
        }
        
        public void setTextForPropertyValue (String text) {
            saveValue(text);
        }
        
        public String getTextForPropertyValue () {
            return (String) combobox.getSelectedItem();
        }
        
        public JComponent getCustomEditorComponent() {
            return combobox;
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
        
        public void actionPerformed(ActionEvent evt) {
            radioButton.setSelected(true);
        }
    }
    
}
