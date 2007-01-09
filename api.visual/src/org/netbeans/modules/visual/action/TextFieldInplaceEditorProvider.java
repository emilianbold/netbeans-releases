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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;

/**
 * @author David Kaspar
 */
public final class TextFieldInplaceEditorProvider implements InplaceEditorProvider<JTextField> {

    private TextFieldInplaceEditor editor;
    private EnumSet<InplaceEditorProvider.ExpansionDirection> expansionDirections;

    private KeyListener keyListener;
    private FocusListener focusListener;
    private DocumentListener documentListener;

    public TextFieldInplaceEditorProvider (TextFieldInplaceEditor editor, EnumSet<InplaceEditorProvider.ExpansionDirection> expansionDirections) {
        this.editor = editor;
        this.expansionDirections = expansionDirections;
    }

    public JTextField createEditorComponent (EditorController controller, Widget widget) {
        if (! editor.isEnabled (widget))
            return null;
        JTextField field = new JTextField (editor.getText (widget));
        Scene scene = widget.getScene();
        double zoomFactor = scene.getZoomFactor ();
        if (zoomFactor > 1.0) {
            Font font = scene.getDefaultFont();
            font = font.deriveFont((float) (font.getSize2D() * zoomFactor));
            field.setFont (font);
        }
        return field;
    }

    public void notifyOpened (final EditorController controller, Widget widget, JTextField editor) {
        editor.setMinimumSize (new Dimension (64, 19));
        keyListener = new KeyAdapter() {
            public void keyPressed (KeyEvent e) {
                switch (e.getKeyChar ()) {
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
        focusListener = new FocusAdapter() {
            public void focusLost (FocusEvent e) {
                controller.closeEditor (true);
            }
        };
        documentListener = new DocumentListener () {
            public void insertUpdate (DocumentEvent e) {
                controller.notifyEditorComponentBoundsChanged ();
            }

            public void removeUpdate (DocumentEvent e) {
                controller.notifyEditorComponentBoundsChanged ();
            }

            public void changedUpdate (DocumentEvent e) {
                controller.notifyEditorComponentBoundsChanged ();
            }
        };
        editor.addKeyListener (keyListener);
        editor.addFocusListener (focusListener);
        editor.getDocument ().addDocumentListener (documentListener);
        editor.selectAll ();
    }

    public void notifyClosing (EditorController controller, Widget widget, JTextField editor, boolean commit) {
        editor.getDocument ().removeDocumentListener (documentListener);
        editor.removeFocusListener (focusListener);
        editor.removeKeyListener (keyListener);
        if (commit) {
            this.editor.setText (widget, editor.getText ());
            if (widget != null)
                widget.getScene ().validate ();
        }
    }

    public Rectangle getInitialEditorComponentBounds(EditorController controller, Widget widget, JTextField editor, Rectangle viewBounds) {
        return null;
    }

    public EnumSet<ExpansionDirection> getExpansionDirections (EditorController controller, Widget widget, JTextField editor) {
        return expansionDirections;
    }

}
