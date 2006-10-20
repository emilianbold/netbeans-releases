/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.identity.profile.api.configurator;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

/**
 * Support class used to create instances of Modifier based on the source
 * components.
 *
 * @author ptliu
 */
class ConfiguratorSupport {

    public static Modifier createModifier(Enum configurable, Object source,
            Configurator configurator) {
        if (source instanceof JTextField) {
            return new TextFieldModifier(configurable, (JTextField) source,
                    configurator);
        } else if (source instanceof JComboBox) {
            return new ComboBoxModifier(configurable, (JComboBox) source,
                    configurator);
        } else if (source instanceof JTable) {
            TableModel model = ((JTable) source).getModel();
            
            if (model instanceof MultiSelectTableModel) {
                return new MultiSelectTableModifier(configurable, (JTable) source,
                        configurator);
            } else if (model instanceof DataEntryTableModel) {
                return new DataEntryTableModifier(configurable, (JTable) source,
                        configurator);
            }
        } else if (source instanceof JCheckBox) {
            return new CheckBoxModifier(configurable, (JCheckBox) source,
                    configurator);
        }
        
        // Should throw an unsupported exception.
        return null;
    }
}
