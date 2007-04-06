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
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.io.PopupUtil;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * @author David Kaspar
 */
public class ScreenStringPropertyEditor implements ScreenPropertyEditor {

    private String propertyName;
    private int alignment;

    public ScreenStringPropertyEditor (String propertyName) {
        this (propertyName, JTextField.LEFT);
    }

    public ScreenStringPropertyEditor (String propertyName, int alignment) {
        this.alignment = alignment;
        assert propertyName != null;
        this.propertyName = propertyName;
    }

    public JComponent createEditorComponent (ScreenPropertyDescriptor property) {
        StringTextField editor = new StringTextField (property);
        editor.setMinimumSize (new Dimension (128, 21));

        PropertyValue value = property.getRelatedComponent ().readProperty (propertyName);
        String string = MidpTypes.getString (value);
        editor.setText (string != null ? string : ""); // NOI18N

        return editor;
    }

    public Insets getEditorComponentInsets (JComponent editorComponent) {
        return editorComponent.getBorder ().getBorderInsets (editorComponent);
    }

    private class StringTextField extends JTextField implements KeyListener {

        private ScreenPropertyDescriptor property;

        public StringTextField (ScreenPropertyDescriptor property) {
            this.property = property;
            addKeyListener (this);
            setHorizontalAlignment (alignment);
        }

        public void keyTyped (KeyEvent e) {
            if (e.getKeyChar () != KeyEvent.VK_ENTER)
                return;
            final DesignComponent relatedComponent = property.getRelatedComponent ();
            relatedComponent.getDocument ().getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    PropertyValue value = MidpTypes.createStringValue (getText ());
                    relatedComponent.writeProperty (propertyName, value);
                }
            });
            PopupUtil.hidePopup ();
        }

        public void keyPressed (KeyEvent e) {
        }

        public void keyReleased (KeyEvent e) {
        }

    }

}
