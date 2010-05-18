/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.properties;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class NumberTextFieldHelper implements
        FocusListener, DocumentListener
{
    private ConnectionPropertiesPanel connectionPropertiesPanel;
    private JTextField textField;

    private boolean defaultValueIsInstalled = false;

    private Color normalForeground = null;

    private int minValue;
    private int maxValue;

    private int defaultValue;

    private Class bundleClass;
    private String defaultMessageKey;

    private boolean suppressEvents = true;

    NumberTextFieldHelper(ConnectionPropertiesPanel connectionPropertiesPanel,
            JTextField textField, int defaultValue,
            Class bundleClass, String defaultMessageKey,
            int minValue, int maxValue)
    {
        this.connectionPropertiesPanel = connectionPropertiesPanel;
        this.textField = textField;
        this.bundleClass = bundleClass;
        this.defaultValue = defaultValue;
        this.defaultMessageKey = defaultMessageKey;
        this.minValue = minValue;
        this.maxValue = maxValue;

        textField.addFocusListener(this);
        
        installDefaultValue();

        suppressEvents = false;
    }

    public void valueChanged() {
        if (!suppressEvents) {
            connectionPropertiesPanel.updateValidState();
        }
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        if (this.defaultValue != defaultValue) {
            this.defaultValue = defaultValue;
            if (reinstallDefaultValue()) {
                valueChanged();
            }
        }
    }

    public Integer getValue() {
        if (defaultValueIsInstalled) {
            return defaultValue;
        }

        String text = textField.getText();
        if (text == null) {
            return defaultValue;
        }

        text = text.trim();
        if (text.length() == 0) {
            return defaultValue;
        }

        int value = 0;

        try {
            value = Integer.parseInt(text);
        } catch (Exception ex) {
            return null;
        }

        return (value < minValue || maxValue < value) ? null : value;
    }

    public void setValue(int value) {
        uninstallDefaultValue();
        textField.setText(Integer.toString(value));
    }

    public boolean isDefaultValueActivated() {
        return defaultValueIsInstalled;
    }

    public boolean isValid() {
        return (getValue() != null);
    }

    private String getDefaultValueString() {
        return NbBundle.getMessage(bundleClass, defaultMessageKey,
                defaultValue);
    }

    private boolean isEmptyText() {
        String text = textField.getText();
        return (text == null) || (text.trim().length() == 0);
    }

    private void installDefaultValue() {
        if (!defaultValueIsInstalled && !textField.hasFocus()
                && isEmptyText())
        {
            defaultValueIsInstalled = true;
            
            textField.getDocument().removeDocumentListener(this);

            textField.setText(getDefaultValueString());
            textField.setCaretPosition(0);

            if (normalForeground == null) {
                normalForeground = textField.getForeground();
            }

            textField.setForeground(DEFAULT_VALUE_COLOR);
            valueChanged();
        }
    }

    private void uninstallDefaultValue() {
        if (defaultValueIsInstalled) {
            defaultValueIsInstalled = false;

            textField.setText("");
            textField.setForeground(normalForeground);

            textField.getDocument().removeDocumentListener(this);
            textField.getDocument().addDocumentListener(this);
        }
    }

    private boolean reinstallDefaultValue() {
        if (defaultValueIsInstalled) {
            textField.setText(getDefaultValueString());
            textField.setCaretPosition(0);
            return true;
        }
        
        return false;
    }

    // FocusListener impl
    public void focusGained(FocusEvent e) {
        uninstallDefaultValue();
    }

    public void focusLost(FocusEvent e) {
        installDefaultValue();
    }

    // DocumentListener impl
    public void insertUpdate(DocumentEvent e) {
        valueChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        valueChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        valueChanged();
    }

    public static Color DEFAULT_VALUE_COLOR = Color.GRAY;
}

