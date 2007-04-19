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

import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyEditor;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class ScreenBooleanPropertyEditor implements ScreenPropertyEditor {

    private String propertyName;

    public ScreenBooleanPropertyEditor (String propertyName) {
        this.propertyName = propertyName;
    }

    public JComponent createEditorComponent (final ScreenPropertyDescriptor property) {
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                final DesignComponent relatedComponent = property.getRelatedComponent ();
                relatedComponent.getDocument ().getTransactionManager ().writeAccess (new Runnable() {
                    public void run () {
                        PropertyValue propertyValue = relatedComponent.readProperty (propertyName);
                        boolean state = propertyValue.getKind () == PropertyValue.Kind.VALUE  &&  MidpTypes.getBoolean (propertyValue);
                        state = ! state;
                        relatedComponent.writeProperty (propertyName, MidpTypes.createBooleanValue (state));
                    }
                });
            }
        });
        return null;
    }

    public Insets getEditorComponentInsets (JComponent editorComponent) {
        return null;
    }

}
