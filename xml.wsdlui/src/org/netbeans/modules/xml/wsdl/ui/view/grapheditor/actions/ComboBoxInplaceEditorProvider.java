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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumSet;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 */
public final class ComboBoxInplaceEditorProvider implements InplaceEditorProvider<JComboBox> {

    private ComboBoxInplaceEditor localEditor;

    private EnumSet<ExpansionDirection> expansionDirections;
    
    private FocusListener focusListener;
    
    public ComboBoxInplaceEditorProvider (ComboBoxInplaceEditor editor, EnumSet<ExpansionDirection> expansionDirections) {
        this.localEditor = editor;
        this.expansionDirections = expansionDirections;
    }

    private KeyListener keyListener;

    public JComboBox createEditorComponent (EditorController controller, Widget widget) {
        if (! localEditor.isEnabled (widget))
            return null;
        ComboBoxModel model = localEditor.getModel();
        JComboBox comboBox = new JComboBox(model);
        comboBox.setEditable(localEditor.getEditable());
        comboBox.setPreferredSize(widget.getBounds().getSize());
        
        
        
        return comboBox;
    }

    public void notifyOpened (final EditorController controller, final Widget widget, JComboBox editor) {
        editor.setMinimumSize (new Dimension (64, 19));
        keyListener = new KeyAdapter() {
            @Override
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
        editor.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e) {
                controller.closeEditor(true);
            }
        
        });
        
        FocusAdapter focusListener = new FocusAdapter() {
            public void focusLost (FocusEvent e) {
            	controller.closeEditor (true);
            }
        };
        editor.addFocusListener(focusListener);
        
        //editor.selectAll ();
    }

    public void notifyClosing (EditorController controller, Widget widget, JComboBox editor, boolean commit) {
        editor.removeKeyListener (keyListener);
        if (commit) {
            if (widget != null) {
                widget.revalidate();
                widget.getScene().validate();
            }
        }
        localEditor.setSelectedItem(editor.getSelectedItem());
    }

    public EnumSet<ExpansionDirection> getExpansionDirections(EditorController controller, Widget widget, JComboBox editor) {
        return expansionDirections;
    }

    public Rectangle getInitialEditorComponentBounds(EditorController controller, Widget widget, JComboBox editor, Rectangle viewBounds) {
        return null;
    }

}
