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

package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * A JTextField implementation of the InplaceEditor interface.
 * @author Tim Boudreau
 */
class StringInplaceEditor extends JTextField implements InplaceEditor {

    protected PropertyEditor editor;
    protected PropertyEnv env;
    private boolean added;
    private String valFromEditor;
    private String valFromTextField;
    private PropertyModel pm;
    
    private KeyStroke[] strokes = new KeyStroke[] {
            KeyStroke.getKeyStroke(
                KeyEvent.VK_HOME, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK
            ),
            KeyStroke.getKeyStroke(
                KeyEvent.VK_END, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK
            ), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false)
        };

    public void removeNotify() {
        super.removeNotify();
    }

    public void clear() {
        editor = null;
        setEditable(true);
        setEnabled(true);
        setText("");
        pm = null;
        env = null;
        valFromEditor = null;
        valFromTextField = null;
    }

    public void connect(PropertyEditor p, PropertyEnv env) {
        setActionCommand(COMMAND_SUCCESS);
        this.env = env;

        if (editor == p) {
            return;
        }

        editor = p;

        boolean editable = PropUtils.checkEnabled(this, p, env);
        setEnabled(editable);

        //Undocumented, but in NB 3.5 and earlier, getAsText() returning null for
        //paintable editors was yet another way to disable a property editor
        if ((p.getTags() == null) && (p.getAsText() == null) && p.isPaintable()) {
            editable = false;
        }

        setEditable(editable);
        reset();
        added = false;
    }

    public void addNotify() {
        super.addNotify();
        added = true;
    }

    public JComponent getComponent() {
        return this;
    }

    public Object getValue() {
        if ((valFromTextField != null) && valFromTextField.equals(getText())) {
            //#47430 - JTextField will strip \n's from edited text.  If no
            //change to the text field value, return what we originally got
            return valFromEditor;
        } else {
            return getText();
        }
    }

    public void reset() {
        String txt;
        txt = editor.getAsText();

        //don't want an editor with the text "different values" in it //NOI18N
        if (editor instanceof PropUtils.DifferentValuesEditor) {
            txt = ""; //NOI18N
        }

        valFromEditor = txt;
        //issue 26367, form editor needs ability to set a custom value
        //when editing is initiated (event handler combos, part of them
        //cleaning up their EnhancedPropertyEditors).          
        if ((getClass() == StringInplaceEditor.class) && (env != null) && (env.getFeatureDescriptor() != null)) {
            String initialEditValue = (String) env.getFeatureDescriptor().getValue("initialEditValue"); //NOI18N

            if (initialEditValue != null) {
                txt = initialEditValue;
                valFromEditor = txt;
            }
        }

        if (txt == null) {
            txt = "";
        }

        setText(txt);
        valFromTextField = getText();
        setSelectionStart(0);
        setSelectionEnd(txt.length());
    }

    public KeyStroke[] getKeyStrokes() {
        return strokes;
    }

    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    private void handleInitialInputEvent(InputEvent e) {
        //issue 35296, select all the text
        String txt = getText();

        if (txt.length() > 0) {
            setSelectionStart(0);
            setSelectionEnd(getText().length());
        }
    }

    public void setValue(Object o) {
        if ((null != o) && (null != editor) && editor.supportsCustomEditor()) {
            editor.setValue(o);
            setText(editor.getAsText());
        } else {
            setText((o != null) ? o.toString() : ""); //NOI18N
        }
    }

    public boolean supportsTextEntry() {
        return true;
    }

    public PropertyModel getPropertyModel() {
        return pm;
    }

    public void setPropertyModel(PropertyModel pm) {
        this.pm = pm;
    }

    public boolean isKnownComponent(Component c) {
        return false;
    }

    public Dimension getPreferredSize() {
        Graphics g = PropUtils.getScratchGraphics(this);
        String s = getText();

        if (s.length() > 1000) {
            //IZ 44152, debugger can return 512K+ long strings
            return new Dimension(4196, g.getFontMetrics(getFont()).getHeight());
        }

        FontMetrics fm = g.getFontMetrics(getFont());
        Dimension result = new Dimension(fm.stringWidth(s), fm.getHeight());
        result.width = Math.max(result.width, PropUtils.getMinimumPropPanelWidth());
        result.height = Math.max(result.height, PropUtils.getMinimumPropPanelHeight());

        if (getBorder() != null) {
            Insets i = getBorder().getBorderInsets(this);
            result.width += (i.right + i.left);
            result.height += (i.top + i.bottom);
        }

        return result;
    }

    public void processMouseEvent(MouseEvent me) {
        super.processMouseEvent(me);

        if (added) {
            handleInitialInputEvent(me);
        }

        added = false;
    }

    protected void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);
        repaint();
    }

    public void paintComponent(Graphics g) {
        //For property panel usage, allow the editor to paint
        if ((editor != null) && !hasFocus() && editor.isPaintable()) {
            Insets ins = getInsets();
            Color c = g.getColor();

            try {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g.setColor(c);
            }

            ins.left += PropUtils.getTextMargin();
            editor.paintValue(
                g,
                new Rectangle(
                    ins.left, ins.top, getWidth() - (ins.right + ins.left), getHeight() - (ins.top + ins.bottom)
                )
            );
        } else {
            super.paintComponent(g);
        }
    }
}
