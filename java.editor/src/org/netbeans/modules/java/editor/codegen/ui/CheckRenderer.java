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
package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.beans.BeanInfo;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeCellRenderer;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 * @author Petr Hrebejk
 */
class CheckRenderer extends JPanel implements TreeCellRenderer {

    private TristateCheckBox check;
    private JLabel label;
            
    private static final JList LIST_FOR_COLORS = new JList();
    
    public CheckRenderer() {
        
        setLayout(new BorderLayout() );
        setOpaque(true);
        
        this.check = new TristateCheckBox();
        this.label = new JLabel();
        
        add(check, BorderLayout.WEST );
        add(label, BorderLayout.CENTER );
        
        check.setOpaque(false);
        label.setOpaque(false);
    }
        
    /** The component returned by HtmlRenderer.Renderer.getTreeCellRendererComponent() */
    
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        Node n = Visualizer.findNode(value);
        ElementNode.Description description = n.getLookup().lookup(ElementNode.Description.class);
        
        if ( description != null ) {
            check.setVisible( description.isSelectable() || description.hasSelectableSubs() );
            if( description.isSelectable() ) {
                check.setSelected(description.isSelected());
            } else {
                check.setState( getCheckState( description.getSubs() ));
            }
        }
            
        if ( isSelected ) {
            label.setForeground(LIST_FOR_COLORS.getSelectionForeground());
            setOpaque(true);
            setBackground(LIST_FOR_COLORS.getSelectionBackground());
        }
        else {
            label.setForeground(tree.getForeground());
            setOpaque(false);
            //setBackground(tree.getBackground());
        }
        
        label.setText( n.getHtmlDisplayName() );
        label.setIcon( new ImageIcon( n.getIcon(BeanInfo.ICON_COLOR_16x16) ) ); // XXX Ask description directly
        
        return this;
        
    }
    
    private State getCheckState( List<ElementNode.Description> children ) {
        if( null == children )
            return State.OTHER;
        int selCounter = 0, unselCounter = 0;
        for( ElementNode.Description d : children ) {
            if( d.isSelectable() ) {
                if( d.isSelected() )
                    selCounter++;
                else
                    unselCounter++;
                if( selCounter > 0 && unselCounter > 0 )
                    return State.OTHER;
            }
        }
        return selCounter > 0 ? State.SELECTED : State.NOT_SELECTED;
    }
    
    public Rectangle getCheckBounds() {
        return (Rectangle)check.getBounds().clone();
    }
        
    private enum State {
        SELECTED, NOT_SELECTED, OTHER;
    };
    
    private static class TristateCheckBox extends JCheckBox {
        
        private final TristateDecorator model;
        
        public TristateCheckBox() {
            super(null, null);
            model = new TristateDecorator(getModel());
            setModel(model);
            setState(State.OTHER);
        }
        
        /** No one may add mouse listeners, not even Swing! */
        public void addMouseListener(MouseListener l) { }
        /**
         * Set the new state to either SELECTED, NOT_SELECTED or
         * OTHER.
         */
        public void setState(State state) { model.setState(state); }
        /** Return the current state, which is determined by the
         * selection status of the model. */
        public State getState() { return model.getState(); }
        public void setSelected(boolean b) {
            if (b) {
                setState(State.SELECTED);
            } else {
                setState(State.NOT_SELECTED);
            }
        }
        /**
         * Exactly which Design Pattern is this?  Is it an Adapter,
         * a Proxy or a Decorator?  In this case, my vote lies with the
         * Decorator, because we are extending functionality and
         * "decorating" the original model with a more powerful model.
         */
        private class TristateDecorator implements ButtonModel {
            private final ButtonModel other;
            private TristateDecorator(ButtonModel other) {
                this.other = other;
            }
            private void setState(State state) {
                if (state == State.NOT_SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(false);
                } else if (state == State.SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(true);
                } else { // either "null" or OTHER
                    other.setArmed(true);
                    setPressed(true);
                    setSelected(true);
                }
            }
            /**
             * The current state is embedded in the selection / armed
             * state of the model.
             *
             * We return the SELECTED state when the checkbox is selected
             * but not armed, DONT_CARE state when the checkbox is
             * selected and armed (grey) and NOT_SELECTED when the
             * checkbox is deselected.
             */
            private State getState() {
                if (isSelected() && !isArmed()) {
                    // normal black tick
                    return State.SELECTED;
                } else if (isSelected() && isArmed()) {
                    // don't care grey tick
                    return State.OTHER;
                } else {
                    // normal deselected
                    return State.NOT_SELECTED;
                }
            }
            /** Filter: No one may change the armed status except us. */
            public void setArmed(boolean b) {
            }
            /** We disable focusing on the component when it is not
             * enabled. */
            public void setEnabled(boolean b) {
                setFocusable(b);
                other.setEnabled(b);
            }
            /** All these methods simply delegate to the "other" model
             * that is being decorated. */
            public boolean isArmed() { return other.isArmed(); }
            public boolean isSelected() { return other.isSelected(); }
            public boolean isEnabled() { return other.isEnabled(); }
            public boolean isPressed() { return other.isPressed(); }
            public boolean isRollover() { return other.isRollover(); }
            public void setSelected(boolean b) { other.setSelected(b); }
            public void setPressed(boolean b) { other.setPressed(b); }
            public void setRollover(boolean b) { other.setRollover(b); }
            public void setMnemonic(int key) { other.setMnemonic(key); }
            public int getMnemonic() { return other.getMnemonic(); }
            public void setActionCommand(String s) {
                other.setActionCommand(s);
            }
            public String getActionCommand() {
                return other.getActionCommand();
            }
            public void setGroup(ButtonGroup group) {
                other.setGroup(group);
            }
            public void addActionListener(ActionListener l) {
                other.addActionListener(l);
            }
            public void removeActionListener(ActionListener l) {
                other.removeActionListener(l);
            }
            public void addItemListener(ItemListener l) {
                other.addItemListener(l);
            }
            public void removeItemListener(ItemListener l) {
                other.removeItemListener(l);
            }
            public void addChangeListener(ChangeListener l) {
                other.addChangeListener(l);
            }
            public void removeChangeListener(ChangeListener l) {
                other.removeChangeListener(l);
            }
            public Object[] getSelectedObjects() {
                return other.getSelectedObjects();
            }
        }
    }
}
