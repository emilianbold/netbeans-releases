/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2.ui;

import org.netbeans.core.output2.Controller;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A basic output pane.  This class implements the non-output window specific
 * gui management for the output window - creating the text component, 
 * locking the caret and scrollbar to the bottom of the document to the 
 * bottom, etc.  Could be merged with OutputView, but it's more readable
 * and maintainable to keep the pure gui code separate.  Mainly contains 
 * logic for layout and showing and hiding a toolbar and input area.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputTab extends JComponent implements ActionListener {
    private JToolBar toolbar = null;
    private InputPanel input = null;
    private AbstractOutputPane outputPane;
    
    public AbstractOutputTab() {
        outputPane = createOutputPane();
        add (outputPane);
        setFocusable(false);
    }
    
    public void setDocument (Document doc) {
        outputPane.setDocument(doc);
    }

    public void requestFocus() {
        if (isInputVisible()) {
            input.requestFocus();
        } else {
            outputPane.requestFocus();
        }
    }
    
    public boolean requestFocusInWindow() {
        if (isInputVisible()) {
            return input.requestFocusInWindow();
        } else {
            return getOutputPane().requestFocusInWindow();
        }
    }    

    protected abstract AbstractOutputPane createOutputPane();
    
    protected abstract void inputSent (String txt);
    
    public final AbstractOutputPane getOutputPane() {
        return outputPane;
    }

    public final void setToolbarActions (Action[] a) {
        if (a == null || a.length == 0) {
            setToolbarVisible(false);
            return;
        }
        if (a.length > 5) {
            throw new IllegalArgumentException ("No more than 5 actions allowed" //NOI18N
                + "in the output window toolbar"); //NOI18N
        }
        setToolbarVisible(true);
        if (toolbar.getComponentCount() > 0) {
            toolbar.removeAll();
        }
        actions = new Action[a.length];
        JButton[] jb = new JButton[a.length];
        for (int i=0; i < jb.length; i++) {
            actions[i] = a[i];
            // mkleint - ignore the WeakAction referencing as it introduces
            // additional non obvious contract to using the the toolbar actions.
//            actions[i] = new WeakAction(a[i]);
            installKeyboardAction (actions[i]);
            jb[i] = new JButton(actions[i]);
            jb[i].setBorderPainted(false);
            jb[i].setOpaque(false);
            jb[i].setText(null);
            jb[i].putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            if (a[i].getValue (Action.SMALL_ICON) == null) {
                throw new IllegalStateException ("No icon provided for " + a); //NOI18N
            }
            toolbar.add(jb[i]);
        }
    }

    private Action[] actions = new Action[0];
    /**
     * Get the toolbar actions, if any, which have been supplied by the client.
     * Used to add them to the popup menu if they return a non-null name.
     *
     * @return An array of actions
     */
    public Action[] getToolbarActions() {
        return actions;
    }

    /**
     * Install a keyboard action.  This is used in two places - all toolbar actions with
     * accelerator keys and names will also be installed as keyboard actions.  Also, the
     * master controller installs its actions which should be accessible via the keyboard.
     * The actions are actually installed into the text control.
     *
     * @param a An action to install, if its name and accelerator are non-null
     */
    public void installKeyboardAction (Action a) {
        if (!(a instanceof WeakAction)) {
            //It is a Controller.ControllerAction - don't create a memory leak by listening to it
            a = new WeakAction(a);
        }
        KeyStroke accel = null;
        String name;
        Object o = a.getValue (Action.ACCELERATOR_KEY);
        if (o instanceof KeyStroke) {
            accel = (KeyStroke) o;
        }
        name = (String) a.getValue(Action.NAME);
        if (accel != null) {
            if (Controller.log) Controller.log ("Installed action " + name + " on " + accel);
            // if the logic here changes, check the popup escaping hack in Controller
            // it temporarily removes the VK_ESCAPE from input maps..
            JComponent c = getOutputPane().textView;
            c.getInputMap().put(accel, name);
            c.getActionMap().put(name, a);
            getInputMap (WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put (accel, name);
            getActionMap().put(name, a);
        }
    }

    public final boolean isInputVisible() {
        return input != null && input.getParent() == this && input.isVisible();
    }
    
    public final boolean isToolbarVisible() {
        return toolbar != null && toolbar.getParent() == this && 
            toolbar.isVisible();
    }
    
    public final void setInputVisible (boolean val) {
        if (val == isInputVisible()) {
            return;
        }
        if (val) {
            if (input == null) {
                input = new InputPanel();
                input.addActionListener(this);
            }
            if (input.getParent() != this) {
                add (input);
                validate();
            }
        }
        input.setVisible (val);
        validate();
        getOutputPane().ensureCaretPosition();
    }
    
    public final void setToolbarVisible (boolean val) {
        if (val == isToolbarVisible()) {
            return;
        }
        if (val) {
            if (toolbar == null) {
                toolbar = new JToolBar();
                toolbar.setOrientation(JToolBar.VERTICAL);
                toolbar.setLayout (new BoxLayout(toolbar, BoxLayout.Y_AXIS));
                toolbar.setFloatable(false);
                toolbar.setOpaque(false);
            }
            if (toolbar.getParent() != this) {
                add (toolbar);
            }
        }
        toolbar.setVisible (val);
    }    

    public void actionPerformed(ActionEvent ae) {
        InputPanel ip = (InputPanel) ae.getSource();
        if (InputPanel.ACTION_EOF.equals(ae.getActionCommand())) {
            inputEof();
        }  else {
            inputSent (ip.getText());
        }
    }

    protected abstract void inputEof();

    public void doLayout() {
        boolean hasToolbar = isToolbarVisible();
        boolean hasInput = isInputVisible();
        Insets ins = getInsets();
        int left = ins.left;
        int bottom = hasInput ? (getHeight() - ins.bottom - 
            (input.getPreferredSize().height)) - 3 : getHeight() - ins.bottom;
        
        if (hasToolbar) {
            left = ins.left + Math.max(32, toolbar.getPreferredSize().width);
            toolbar.setBounds (ins.left, ins.top, left, getHeight() 
                - (ins.top + ins.bottom));
        }
        Component main = outputPane;
        
        if (main != null) {
            main.setBounds (left, ins.top, getWidth() - (left + ins.right), 
                bottom - ins.top);
        }
        if (hasInput) {
            input.setBounds (left, bottom, getWidth() - (left + ins.right), 
                getHeight() - bottom);
        }
    }

    public abstract void hasSelectionChanged(boolean val);
    
    void notifyInputFocusGained(){
        getOutputPane().lockScroll();
        getOutputPane().ensureCaretPosition();
    }

}
