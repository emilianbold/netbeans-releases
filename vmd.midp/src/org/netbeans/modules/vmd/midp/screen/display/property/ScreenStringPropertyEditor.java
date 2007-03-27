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
 *
 */

package org.netbeans.modules.vmd.midp.screen.display.property;

import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyEditor;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class ScreenStringPropertyEditor implements ScreenPropertyEditor {

    private DesignComponent component;
    private String propertyName;

    public ScreenStringPropertyEditor (DesignComponent component, String propertyName) {
        this.component = component;
        this.propertyName = propertyName;
    }

    public JComponent createEditorComponent (Controller controller, ScreenPropertyDescriptor descriptor) {
        JTextField editor = new JTextField ();
        editor.setMinimumSize (new Dimension (128, 21));
        return editor;
    }

    public void openNotify (Controller controller, ScreenPropertyDescriptor descriptor, JComponent editorComponent) {
        JTextField editor = (JTextField) editorComponent;
        PropertyValue value = component.readProperty (propertyName);
        String string = MidpTypes.getString (value);
        editor.setText (string != null ? string : ""); // NOI18N
    }

    public void closeNotify (Controller controller, ScreenPropertyDescriptor descriptor, JComponent editorComponent, boolean commit) {
        if (! commit)
            return;
        JTextField editor = (JTextField) editorComponent;
        PropertyValue value = MidpTypes.createStringValue (editor.getText ());
        component.writeProperty (propertyName, value);
    }

}
