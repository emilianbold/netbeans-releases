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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;

/**
 * This Modifier is used to synchronize the value between a JComboBox and
 * a Configurable.
 *
 * @author ptliu
 */
class ComboBoxModifier extends Modifier {
    private JComboBox comboBox;

    /** Creates a new instance of ComboBoxModifier */
    public ComboBoxModifier(final Enum configurable, final JComboBox comboBox,
            final Configurator configurator) {
        super(configurable, comboBox, configurator);
        
        this.comboBox = comboBox;
        
        comboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED)  {
                    configurator.setValue(configurable, getValue());
                }
            }
        });

        setValue(configurator.getValue(configurable));   
    }
    
    public Object getValue() {
        return comboBox.getSelectedItem();
    }
    
    public void setValue(Object value) {
        if (value != null) {
            comboBox.setSelectedItem(value);
        } else {
            //
            // This is a hack to trigger a itemStateChange event
            // so we can set the default value for the
            // configurator.
            //
            comboBox.setSelectedIndex(1);
            comboBox.setSelectedIndex(0);
        }
    }
}
