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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;

/**
 * This Modifier is used to synchronize the value between a JCheckBox and
 * a Configurable.
 *
 * @author ptliu
 */
class CheckBoxModifier extends Modifier {
    private JCheckBox checkBox;

    /** Creates a new instance of CheckBoxModifier */
    public CheckBoxModifier(final Enum configurable, final JCheckBox checkBox,
            final Configurator configurator) {
        super(configurable, checkBox, configurator);

        this.checkBox = checkBox;
        
        setValue(configurator.getValue(configurable));
        
        checkBox.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent event) {
               configurator.setValue(configurable, getValue());
           } 
        });
    }
    
    public void setValue(Object value) {
        if (Boolean.TRUE.equals(value)) {
            checkBox.setSelected(true);
        } else {
            checkBox.setSelected(false);
        }
    }
    
    public Object getValue() {
        if (checkBox.isSelected()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
  
}
