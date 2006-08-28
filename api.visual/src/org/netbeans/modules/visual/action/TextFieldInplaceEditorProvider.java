/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
@Deprecated
public final class TextFieldInplaceEditorProvider implements InplaceEditorProvider<JTextField> {

    private TextFieldInplaceEditor editor;

    @Deprecated
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
