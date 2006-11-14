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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author David Kaspar
 */
public final class TextFieldInplaceEditorProvider implements InplaceEditorProvider<JTextField> {

    private TextFieldInplaceEditor editor;

    public TextFieldInplaceEditorProvider (TextFieldInplaceEditor editor) {
        this.editor = editor;
    }

    private KeyListener keyListener;

    public JTextField createEditorComponent (EditorController controller, Widget widget) {
        if (! editor.isEnabled (widget))
            return null;
        return new JTextField (editor.getText (widget));
    }

    public void notifyOpened (final EditorController controller, Widget widget, JTextField editor) {
        editor.setMinimumSize (new Dimension (64, 19));
        keyListener = new KeyAdapter() {
            public void keyReleased (KeyEvent e) {
                switch (e.getKeyCode ()) {
                    case KeyEvent.VK_ESCAPE:
                        e.consume ();
                        controller.closeEditor (false);
                        break;
                    case KeyEvent.VK_ENTER:
                        e.consume ();
                        controller.closeEditor (true);
                        break;
                }
            }
        };
        editor.addKeyListener (keyListener);
        editor.selectAll ();
    }

    public void notifyClosing (EditorController controller, Widget widget, JTextField editor, boolean commit) {
        editor.removeKeyListener (keyListener);
        if (commit) {
            this.editor.setText (widget, editor.getText ());
            if (widget != null)
                widget.getScene ().validate ();
        }
    }

}
