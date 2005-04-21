/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CleanComboUI.java
 *
 * Created on 04 October 2003, 23:03
 */
package org.openide.explorer.propertysheet;

import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.basic.BasicComboBoxUI.ComboBoxLayoutManager;
import javax.swing.plaf.metal.MetalComboBoxIcon;


/** A combobox ui delegate that hides the border for use in the property
 * sheet, and does not have problems with firing unexpected focus lost
 * events that confuse the property sheet.
 *
 * @author  Tim Boudreau
 */
class CleanComboUI extends BasicComboBoxUI {
    private JButton button = null;
    private boolean tableUI;
    private ComboPopup popup = null;

    public CleanComboUI(boolean tableUI) {
        this.tableUI = tableUI;
    }

    protected void installDefaults() {
        LookAndFeel.installColorsAndFont(comboBox, "ComboBox.background", "ComboBox.foreground", "ComboBox.font"); //NOI18N

        if (tableUI) {
            comboBox.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
        } else {
            comboBox.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PropUtils.getShadowColor()),
                    
            //leave room for the focus rect on the left, so it doesn't
            //overpaint the first pixel column of text
            BorderFactory.createEmptyBorder(0, 2, 0, 0)
                )
            );
        }

        installComboDefaults(comboBox);
    }

    protected ComboPopup createPopup() {
        popup = new CleanComboPopup(comboBox);

        return popup;
    }

    protected void installKeyboardActions() {
        super.installKeyboardActions();

        //Basic UI won't install an action to open the combo on spacebar,
        //so we do it ourselves
        if (!tableUI) {
            comboBox.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "showPopup"); //NOI18N
            comboBox.getActionMap().put(
                "showPopup",
                new AbstractAction() {
                    public void actionPerformed(ActionEvent ae) {
                        if (!comboBox.isPopupVisible()) {
                            comboBox.showPopup();
                        }
                    }
                }
            ); //NOI18N
        }
    }

    protected JButton createArrowButton() {
        Icon i = UIManager.getIcon("ComboBox.icon"); //NOI18N

        if (i == null) {
            i = new MetalComboBoxIcon();
        }

        button = new JButton(i);
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBorder(null);

        return button;
    }

    protected Insets getInsets() {
        java.awt.Insets i = super.getInsets();
        i.right += 2;

        return i;
    }

    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        if (c.hasFocus() && !tableUI) {
            Color prev = g.getColor();

            try {
                g.setColor(PropUtils.getShadowColor());
                g.drawRect(2, 2, c.getWidth() - 5, c.getHeight() - 5);
            } finally {
                g.setColor(prev);
            }
        }
    }

    /** This focus listener is a workaround for JDK bug 4168483 -
     *  a bogus FocusLost event is sent to the combo box when the
     *  popup is shown.  This results in a variety of messy behaviors.
     *  The main workaround here is to always show the popup on a
     *  focus gained event, and ignore focus lost events (they will be
     *  trapped by the property sheet if focus moves to another component,
     *  and removeEditor() will be called anyway;  other focus lost events
     *  will be events in which removeEditor() will be called because the
     *  editor's action has been performed.  */
    protected FocusListener createFocusListener() {
        return super.createFocusListener();

        /*

        //Code below create a focus listener without the problems associated
        //with using standard combo boxes.  We may need this in the future,
        //so do not delete

        return new FocusListener () {
            public void focusGained( FocusEvent e ) {
                if (comboBox.getParent() == null) {
                    // believe it or not, this can happen if a dialog
                    //(such as open file server can't start) pops up
                    //while an editor is being instantiated.  Some kind
                    //of order-of-operations problem
                    return;
                }
                hasFocus = true;
                try {
                    //Ensure the combo box has a selection.  Avoid messing
                    //with legacy editors like the form editor's - can cause
                    //exceptions
                    if (comboBox instanceof ComboInplaceEditor) {
                        Object o = comboBox.getSelectedItem();
                        if (o != null) {
                            comboBox.getModel().setSelectedItem(o);
                        } else {
                            if (comboBox.getModel().getSize() >= 0) {
                                comboBox.setSelectedIndex(0);
                            }
                        }
                        if (!comboBox.isEditable() &&
                            //don't uatomatically show popup if custom editor button is
                            //only visible when editing, give the user a chance to choose
                            !PropUtils.noCustomButtons) {
                                if (tableUI) {
                                    comboBox.showPopup();
                                } else {
                                    comboBox.repaint();
                                }
                        }
                    }
                } catch (IllegalComponentStateException icse) {
                    //Workaround for peculiar JDK bug - it tries to set focus to a
                    //combobox that is not on screen
                }

                // Notify assistive technologies that the combo box
                // gained focus.
                if (comboBox instanceof Accessible) {
                    AccessibleContext ac =
                        ((Accessible)comboBox).getAccessibleContext();
                    if (ac != null) {
                        ac.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            null, AccessibleState.FOCUSED);
                    }
                }
            }

            public void focusLost( FocusEvent e ) {
                hasFocus = false;
                comboBox.hidePopup();
                if (!tableUI) {
                    comboBox.repaint();
                }
                if (comboBox instanceof Accessible) {
                    AccessibleContext ac =
                        ((Accessible)comboBox).getAccessibleContext();
                    if (ac != null) {
                        ac.firePropertyChange(
                            AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                            AccessibleState.FOCUSED, null);
                    }
                }
            }
        };
         */
    }

    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
        ListCellRenderer renderer = comboBox.getRenderer();

        //Fix for an obscure condition when renderer may be null -
        //can't figure how this can happen unless the combo box is
        //painted before installUI() has completed (which is called
        //by the superclass constructor calling updateUI().  Only
        //happens when opening an individual Properties window.  Maybe
        //the window is constructed off the AWT thread?
        if ((listBox == null) || (renderer == null)) {
            return;
        }

        Component c;
        c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
        c.setFont(comboBox.getFont());
        c.setForeground(comboBox.isEnabled() ? comboBox.getForeground() : PropUtils.getDisabledForeground());

        c.setBackground(comboBox.getBackground());

        boolean shouldValidate = false;

        if (c instanceof JPanel) {
            shouldValidate = true;
        }

        currentValuePane.paintComponent(
            g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, shouldValidate
        );
    }

    protected Rectangle rectangleForCurrentValue() {
        Rectangle r = super.rectangleForCurrentValue();

        if (editor != null) {
            r.x += 1;
            r.y += 1;
            r.width -= 1;
            r.height -= 1;
        }

        return r;
    }

    protected ComboBoxEditor createEditor() {
        return new CleanComboBoxEditor();
    }

    private static final void installComboDefaults(JComponent jc) {
        Color c = UIManager.getColor("ComboBox.background"); //NOI18N

        if (c == null) {
            c = UIManager.getColor("text"); //NOI18N
        }

        if (c != null) {
            jc.setBackground(c);
        }

        c = UIManager.getColor("ComboBox.foreground"); //NOI18N

        if (c == null) {
            c = UIManager.getColor("textText"); //NOI18N
        }

        if (c != null) {
            jc.setForeground(c);
        }

        Font f = UIManager.getFont("ComboBox.font"); //NOI18N

        if (f != null) {
            jc.setFont(f);
        }
    }

    private class CleanComboLayout extends ComboBoxLayoutManager {
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);

            if (editor != null) {
                java.awt.Rectangle r = rectangleForCurrentValue();
                r.x = 0;
                r.y = 0;
                r.height = comboBox.getHeight();
                editor.setBounds(r);
            }
        }
    }

    private static class CleanComboPopup extends BasicComboPopup {
        public CleanComboPopup(JComboBox box) {
            super(box);
            installComboDefaults(this);
        }

        protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            Dimension d = list.getPreferredSize();
            Rectangle r = Utilities.getUsableScreenBounds();

            if (pw < d.width) {
                pw = Math.min(d.width, r.width - px);
            }

            if (ph < d.height) {
                ph = Math.min(r.height - py, d.height);
            }

            if ((px + pw) > (r.width - px)) {
                px -= (r.width - pw);
            }

            Rectangle result = new Rectangle(px, py, pw, ph);

            return result;
        }
    }

    static class CleanComboBoxEditor extends BasicComboBoxEditor {
        public CleanComboBoxEditor() {
            editor = new JTextField();

            Color c = UIManager.getColor("Table.selectionBackground"); //NOI18N

            if (c == null) {
                c = Color.BLACK;
            }

            editor.setBorder(BorderFactory.createLineBorder(c));

            //            editor.setBorder (BorderFactory.createEmptyBorder());
        }
    }
}
