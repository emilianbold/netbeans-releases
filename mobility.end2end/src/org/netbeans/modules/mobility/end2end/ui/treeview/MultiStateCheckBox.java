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
