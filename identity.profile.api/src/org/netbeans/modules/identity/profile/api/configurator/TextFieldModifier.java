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

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This Modifier is used to synchronize the value between a JTextField and
 * a Configurable.
 *
 * @author ptliu
 */
class TextFieldModifier extends Modifier {
    private JTextField textField;

    /** Creates a new instance of TextFieldModifier */
    public TextFieldModifier(final Enum configurable,
            final JTextField textField, final Configurator configurator) {
        super(configurable, textField, configurator);

        this.textField = textField;
        
        // Initialize the textField with the value from the configurator.
        setValue(configurator.getValue(configurable));
        
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent event) {
                String text = textField.getText();
                configurator.setValue(configurable, text);
            }
            
            public void insertUpdate(DocumentEvent event) {
                changedUpdate(event);
            }
            
            public void removeUpdate(DocumentEvent event) {
                changedUpdate(event);
            }
        });
    }
    
    public JTextField getTextField() {
        return textField;
    }
    
    public void setValue(Object value) {
        if (value != null) {
            textField.setText(value.toString());
        } else {
            textField.setText("");      //NOI18N
        }
    }
    
    public Object getValue() {
        return textField.getText();
    }
    
}
