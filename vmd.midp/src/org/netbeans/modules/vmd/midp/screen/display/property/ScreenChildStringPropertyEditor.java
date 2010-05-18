/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
public class ScreenChildStringPropertyEditor implements ScreenPropertyEditor {

    private String propertyName;
    private int alignment;

    public ScreenChildStringPropertyEditor (String propertyName) {
        this (propertyName, JTextField.LEFT);
    }

    public ScreenChildStringPropertyEditor (String propertyName, int alignment) {
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
        editor.selectAll ();

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
