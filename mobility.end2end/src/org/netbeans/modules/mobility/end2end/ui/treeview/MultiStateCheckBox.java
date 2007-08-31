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

package org.netbeans.modules.mobility.end2end.ui.treeview;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
import java.awt.event.*;

public class MultiStateCheckBox extends JCheckBox {
    public static enum State {
        SELECTED, UNSELECTED, MIXED;
    }

    protected final MultiStateModel model;
    private static final String PROP_PRESSED = "pressed"; //NOI18N
    private static final String PROP_RELEASED = "released"; //NOI18N
    
    public MultiStateCheckBox(String text, State initial) {
        super(text);
        super.addMouseListener(new MouseAdapter() {
            public void mousePressed(final MouseEvent e) {
                grabFocus();
                model.nextState();
            }
        });
        ActionMap map = new ActionMapUIResource();
        map.put(PROP_PRESSED, new AbstractAction() {  //NOI18N
            public void actionPerformed(final ActionEvent e) {
                grabFocus();
                model.nextState();
            }
        });
        map.put(PROP_RELEASED, null);  //NOI18N
        SwingUtilities.replaceUIActionMap(this, map);
        // set the model to the adapted model
        model = new MultiStateModel(getModel());
        setModel(model);
        setState(initial);
    }
    
    public MultiStateCheckBox(String text) {
        this(text, null);
    }
    
    public MultiStateCheckBox() {
        this(null);
    }
    
    public void addMouseListener(final MouseListener l) {
    }
    
    final public void setState(final State state) {
        model.setState(state);
    }
    
    public State getState() {
        return model.getState();
    }
    
    private class MultiStateModel implements ButtonModel {
        private final ButtonModel other;
        
        private MultiStateModel(ButtonModel other) {
            this.other = other;
        }
        
        protected void setState(final State state) {
            if (state == State.MIXED) {
                other.setArmed(true);
                setPressed(true);
                setSelected(true);
            } else if (state == State.SELECTED) {
                other.setArmed(false);
                setPressed(false);
                setSelected(true);
            } else {
                other.setArmed(false);
                setPressed(false);
                setSelected(false);
            }
        }
        
        protected State getState() {
            if (isSelected() && !isArmed()) {
                return State.SELECTED;
            } else if (isSelected() && isArmed()) {
                return State.MIXED;
            } else {
                return State.UNSELECTED;
            }
        }
        
        protected void nextState() {
            setState(getState() == State.UNSELECTED ? State.SELECTED : State.UNSELECTED);
        }
        
        public void setArmed(final boolean b) {
        }
        
        public void setEnabled(final boolean b) {
            setFocusable(b);
            other.setEnabled(b);
        }
        
        public boolean isArmed() {
            return other.isArmed();
        }
        
        public boolean isSelected() {
            return other.isSelected();
        }
        
        public boolean isEnabled() {
            return other.isEnabled();
        }
        
        public boolean isPressed() {
            return other.isPressed();
        }
        
        public boolean isRollover() {
            return other.isRollover();
        }
        
        public void setSelected(final boolean b) {
            other.setSelected(b);
        }
        
        public void setPressed(final boolean b) {
            other.setPressed(b);
        }
        
        public void setRollover(final boolean b) {
            other.setRollover(b);
        }
        
        public void setMnemonic(final int key) {
            other.setMnemonic(key);
        }
        
        public int getMnemonic() {
            return other.getMnemonic();
        }
        
        public void setActionCommand(final String s) {
            other.setActionCommand(s);
        }
        
        public String getActionCommand() {
            return other.getActionCommand();
        }
        
        public void setGroup(final ButtonGroup group) {
            other.setGroup(group);
        }
        
        public void addActionListener(final ActionListener l) {
            other.addActionListener(l);
        }
        
        public void removeActionListener(final ActionListener l) {
            other.removeActionListener(l);
        }
        
        public void addItemListener(final ItemListener l) {
            other.addItemListener(l);
        }
        
        public void removeItemListener(final ItemListener l) {
            other.removeItemListener(l);
        }
        
        public void addChangeListener(final ChangeListener l) {
            other.addChangeListener(l);
        }
        
        public void removeChangeListener(final ChangeListener l) {
            other.removeChangeListener(l);
        }
        
        public Object[] getSelectedObjects() {
            return other.getSelectedObjects();
        }
    }
}
