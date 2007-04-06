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

package org.netbeans.modules.form.editors2;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.form.ResourceSupport;
import org.netbeans.modules.form.ResourceWrapperEditor;
import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Property editor wrapping StringEditor in a resource editor, allowing to
 * produce resource values (ResourceValue) from strings.
 * 
 * @author Tomas Pavek
 */
public class StringEditor extends ResourceWrapperEditor implements NamedPropertyEditor {

    private JCheckBox noI18nCheckbox;

    public StringEditor() {
        super(new org.netbeans.modules.form.editors.StringEditor());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(StringEditor.class, "StringEditor_DisplayName"); // NOI18N
    }

    public String getJavaInitializationString() {
        String javaStr = super.getJavaInitializationString();
        if (getValue() instanceof String
            && ResourceSupport.isResourceableProperty(property)
            && ResourceSupport.isExcludedProperty(property))
        {   // intentionally excluded from resourcing/internationalization - add NOI18N comment
            javaStr = "*/\n\\1NOI18N*/\n\\0" + javaStr; // NOI18N
            // */\n\\1 is a special code mark for line comment
            // */\n\\0 is a special code mark to indicate that a real code follows
        }
        return javaStr;
    }

    public Component getCustomEditor() {
        Component customEd = super.getCustomEditor();
        if (noI18nCheckbox != null) {
            noI18nCheckbox.setSelected(ResourceSupport.isExcludedProperty(property));
        }
        return customEd;
    }

    protected Component createCustomEditorGUI(Component resourcePanelGUI) {
        if (resourcePanelGUI == null && ResourceSupport.isResourceableProperty(property)) {
            // not usable for full resourcing, only for internationalization
            // add a NOI18N checkbox so the user can mark the property as not to be internationalized
            Component customEd = delegateEditor.getCustomEditor();
            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            noI18nCheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(noI18nCheckbox, NbBundle.getMessage(StringEditor.class, "CTL_NOI18NCheckBox")); // NOI18N
            layout.setHorizontalGroup(layout.createParallelGroup()
                    .add(customEd)
                    .add(layout.createSequentialGroup()
                        .addContainerGap().add(noI18nCheckbox).addContainerGap()));
            layout.setVerticalGroup(layout.createSequentialGroup()
                    .add(customEd).addPreferredGap(LayoutStyle.UNRELATED).add(noI18nCheckbox));
            return panel;
        }
        else {
            noI18nCheckbox = null;
            return super.createCustomEditorGUI(resourcePanelGUI);
        }
    }

    // called when OK button is pressed in the custom editor dialog
    public void vetoableChange(PropertyChangeEvent ev) throws PropertyVetoException {
        super.vetoableChange(ev);
        if (PropertyEnv.PROP_STATE.equals(ev.getPropertyName())
            && resourcePanel == null && noI18nCheckbox  != null)
        {   // no resourcing, just internationalizing
            // mark the property excluded if the NOI18N checkbox is checked
            ResourceSupport.setExcludedProperty(property, noI18nCheckbox.isSelected());
        }
    }
}
