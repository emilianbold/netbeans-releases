/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.editors;

import java.beans.PropertyEditorSupport;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.reflect.*;
import org.netbeans.modules.form.util.*;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

public class KeyStrokeEditor extends PropertyEditorSupport
    implements XMLPropertyEditor
{
    public String getJavaInitializationString() {
        KeyStroke key =(KeyStroke) getValue();
        int mods = key.getModifiers();
        StringBuffer modsText = new StringBuffer();

        if (0 !=(mods
                 &(InputEvent.ALT_MASK | InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK))) {
            if (0 !=(mods & InputEvent.ALT_MASK))
                modsText.append("java.awt.event.InputEvent.ALT_MASK");
            if (0 !=(mods & InputEvent.SHIFT_MASK)) {
                if (modsText.length() > 0)
                    modsText.append(" | ");
                modsText.append("java.awt.event.InputEvent.SHIFT_MASK");
            }
            if (0 !=(mods & InputEvent.CTRL_MASK)) {
                if (modsText.length() > 0)
                    modsText.append(" | ");
                modsText.append("java.awt.event.InputEvent.CTRL_MASK");
            }
        }
        else
            modsText.append("0");

        return "javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent."
            + getVirtualkeyName(key.getKeyCode()) + ", " + modsText.toString() + ")";
    }

    public String getAsText() {
        KeyStroke key =(KeyStroke) getValue();
        return keyStrokeAsString(key);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null || "".equals(text))
            setValue(null);

        KeyStroke key = keyStrokeFromString(text);
        if (key == null)
            throw new IllegalArgumentException("Unrecognized  key: " + text);
        else
            setValue(key);
    }

    private static String getVirtualkeyName(int keycode) {
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int modifiers = f.getModifiers();
            if (Modifier.isPublic(modifiers)
                && Modifier.isStatic(modifiers)
                && Modifier.isFinal(modifiers)
                && f.getType() == Integer.TYPE
                && f.getName().startsWith("VK_")) {
                try {
                    if (f.getInt(KeyEvent.class) == keycode) {
                        return f.getName();
                    }
                }
                catch (IllegalAccessException ex) {
                    ex.printStackTrace(); // should not happen
                }
            }
        }
        return null;
    }

    private static KeyStroke keyStrokeFromString(String s) {
        StringTokenizer st = new StringTokenizer(s, "+");
        String token;
        int mods = 0;
        int keycode = 0;

        while (st.hasMoreTokens() &&(token = st.nextToken()) != null) {
            if ("alt".equalsIgnoreCase(token))
                mods |= InputEvent.ALT_MASK;
            else if ("shift".equalsIgnoreCase(token))
                mods |= InputEvent.SHIFT_MASK;
            else if ("ctrl".equalsIgnoreCase(token))
                mods |= InputEvent.CTRL_MASK;
            else {
                String keycodeName = "VK_" + token.toUpperCase();
                try {
                    keycode = KeyEvent.class.getField(keycodeName).getInt(KeyEvent.class);
                }
                catch (Exception e) {
                    // ignore
                }
            }
        }
        if (keycode != 0)
            return KeyStroke.getKeyStroke(keycode, mods);
        else
            return null;
    }

    private static String keyStrokeAsString(KeyStroke key) {
        StringBuffer buf = new StringBuffer();
        int mods = key.getModifiers();
        int modMasks[] = { InputEvent.SHIFT_MASK, InputEvent.CTRL_MASK,
                           InputEvent.ALT_MASK, };
        String modMaskStrings[] = { "Shift", "Ctrl", "Alt", };

        for (int i = 0; i < modMasks.length; i++) {
            if ((mods & modMasks[i]) != 0) {
                buf.append(modMaskStrings[i]);
                buf.append("+");
            }
        }
        String keyName = getVirtualkeyName(key.getKeyCode());
        if (keyName != null) {
            buf.append(keyName.substring(3));
        }
        return buf.toString();
    }

    //
    // XMLPropertyEditor
    //

    public static final String XML_KEYSTROKE = "KeyStroke"; // NOI18N
    public static final String ATTR_KEY = "key"; // NOI18N

    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_KEYSTROKE.equals(element.getNodeName())) {
            throw new java.io.IOException();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            String value = attributes.getNamedItem(ATTR_KEY).getNodeValue();
            setAsText(value);
        } catch (Exception e) {
            throw new java.io.IOException();
        }
    }

    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_KEYSTROKE);
        el.setAttribute(ATTR_KEY, getAsText());
        return el;
    }

    //
    // custom editor
    //

    public boolean supportsCustomEditor() {
        return true;
    }

    public java.awt.Component getCustomEditor() {
        return new CustomEditor();
    }

    private static String[] _virtualKeys;

    private CustomEditor _customEditor;

    private class CustomEditor extends JPanel
    {
        private KeyGrabberField _keyGrabber;
        private JCheckBox _ctrl, _alt, _shift;
        private JComboBox _virtualKey;

        CustomEditor() {
            setLayout(new FormLayout(5, 3));
            add(new JLabel("Virtual key:"), FormLayout.RIGHT);

            JPanel panel;

            add(panel = new JPanel(new RowLayout(3)));
            panel.add(_virtualKey = new JComboBox());
            panel.add(_ctrl = new JCheckBox("ctrl"));
            panel.add(_alt = new JCheckBox("alt"));
            panel.add(_shift = new JCheckBox("shift"));

            add(new JLabel("Key stroke:"), FormLayout.RIGHT);
            add(_keyGrabber = new KeyGrabberField());

            _keyGrabber.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    setAsText(_keyGrabber.getText());
                }
            });

            // fill in virtual key list

            if (_virtualKeys == null) {
                List list = new ArrayList();

                Field[] fields = KeyEvent.class.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    int modifiers = f.getModifiers();
                    if (Modifier.isPublic(modifiers)
                        && Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers)
                        && f.getType() == Integer.TYPE
                        && f.getName().startsWith("VK_")) {
                        list.add(f.getName());
                    }
                }
                _virtualKeys = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    _virtualKeys[i] =(String) list.get(i);
                }
            }
            _virtualKey.addItem("");
            for (int i = 0; i < _virtualKeys.length; i++)
                _virtualKey.addItem(_virtualKeys[i]);

            KeyStroke key =(KeyStroke) getValue();
            if (key != null)
                setKeyStroke(key);

            // listeners

            ItemListener il = new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                        virtualKeyChanged();
                }
            };
            _virtualKey.addItemListener(il);
            _ctrl.addItemListener(il);
            _alt.addItemListener(il);
            _shift.addItemListener(il);
        }

        java.awt.Component getKeyGrabber() {
            return _keyGrabber;
        }

        private void setKeyStroke(KeyStroke key) {
            _ctrl.setSelected(0 !=(InputEvent.CTRL_MASK & key.getModifiers()));
            _alt.setSelected(0 !=(InputEvent.ALT_MASK & key.getModifiers()));
            _shift.setSelected(0 !=(InputEvent.SHIFT_MASK & key.getModifiers()));

            int keycode = key.getKeyCode();
            String keyName = getVirtualkeyName(keycode);
            if (keyName != null) {
                _virtualKey.setSelectedItem(keyName);
                _keyGrabber.setText(getAsText());
            }
        }

        private void virtualKeyChanged() {
            String keyName =(String) _virtualKey.getSelectedItem();
            if ("".equals(keyName)) {
                _keyGrabber.setText("");
                setValue(null);
                return;
            }

            try {
                Field f = KeyEvent.class.getDeclaredField(keyName);
                int keycode = f.getInt(KeyEvent.class);
                int mods = 0;
                if (_ctrl.isSelected())
                    mods |= InputEvent.CTRL_MASK;
                if (_shift.isSelected())
                    mods |= InputEvent.SHIFT_MASK;
                if (_alt.isSelected())
                    mods |= InputEvent.ALT_MASK;

                setValue(KeyStroke.getKeyStroke(keycode, mods));
                _keyGrabber.setText(getAsText());
            }
            catch (NoSuchFieldException ex) {
                ex.printStackTrace(); // should not happen
            }
            catch (IllegalAccessException ex) {
                ex.printStackTrace(); // should not happen
            }
        }

        private class KeyGrabberField extends JTextField {
            protected void processComponentKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB)
                    super.processComponentKeyEvent(e);
                else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    int keycode = e.getKeyCode();
                    if (keycode != KeyEvent.VK_CONTROL
                        && keycode != KeyEvent.VK_ALT
                        && keycode != KeyEvent.VK_SHIFT) {
                        KeyStroke key = KeyStroke.getKeyStroke(keycode, e.getModifiers());
                        setKeyStroke(key);
                    }
                }
            }
        }
    }
}
