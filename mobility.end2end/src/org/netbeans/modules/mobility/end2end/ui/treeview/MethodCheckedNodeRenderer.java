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
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;

/**
 * User: suchys
 * Date: Dec 20, 2003
 * Time: 12:30:14 PM
 */
public class MethodCheckedNodeRenderer implements TreeCellRenderer {
    private RendererComponent customRenderer = null;
    private Object defaultRenderer = null;
    private MethodCheckedTreeBeanView storage;
    private Color selectionForeground, selectionBackground, textForeground;//, textBackground;
    
    protected RendererComponent getRenderer(){
        return customRenderer;
    }
    
    public MethodCheckedNodeRenderer(Object defaultRenderer) {
        // System.out.println("in MethodCheckedNodeRenderer cc");
        this.defaultRenderer = defaultRenderer;
        selectionForeground = UIManager.getColor("Tree.selectionForeground"); //NOI18N
        selectionBackground = UIManager.getColor("Tree.selectionBackground"); //NOI18N
        textForeground = UIManager.getColor("Tree.textForeground"); //NOI18N
        customRenderer = new RendererComponent();
        Font fontValue = UIManager.getFont("Tree.font"); //NOI18N
        if (fontValue != null){
            customRenderer.setFont(fontValue);
        }
        Boolean booleanValue = (Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon"); //NOI18N
        customRenderer.setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
    }
    
    public MethodCheckedNodeRenderer() {
        this(null);
    }
    
    public void setContentStorage(final MethodCheckedTreeBeanView storage) {
        this.storage = storage;
    }
    
    public Component getTreeCellRendererComponent(final JTree tree, final Object value,
            final boolean selected, final boolean expanded,
            final boolean leaf, final int row, final boolean hasFocus) {
        // System.out.println("in getTreeCellRendererComponent");
        Component returnValue = null;
        final Node node = Visualizer.findNode(value);
        /* sigal */
        
        if (node != null){
            customRenderer.setText(node.getDisplayName());
            // System.out.println("node = " + node.getDisplayName() );
            customRenderer.setEnabled(tree.isEnabled());
            
            if (selected){
                // System.out.println("SELECTED");
                customRenderer.setForeground(selectionForeground, textForeground);
                customRenderer.setBackground(selectionBackground, tree.getBackground());
            } else {
                // System.out.println("NOT  SELECTED");
                customRenderer.setForeground(textForeground);
                customRenderer.setBackground(tree.getBackground());
            }
            // customRenderer.setText(node.getDisplayName());
            if (storage == null){
                // System.out.println("storage = null");
                customRenderer.setState(MethodCheckedTreeBeanView.UNSELECTED);
            } else {
                final Object state = storage.getState(node);
                //System.out.println("state = "+ state.toString());
                customRenderer.setState(state == null ? MethodCheckedTreeBeanView.UNSELECTED : state);
            }
            
            
            customRenderer.setIcon(new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16)));
            return customRenderer;
        }
        if (defaultRenderer != null){
            returnValue = ((TreeCellRenderer)defaultRenderer).getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        return returnValue;
    }
    
    static class RendererComponent extends JPanel{
        MultiStateCheckBox jCheckBox1;
        final private JLabel jLabel1;
        final private JLabel jLabel2;
        
        public RendererComponent() {
            jCheckBox1 = new MultiStateCheckBox();
            jCheckBox1.setBorder(null);
            jLabel1 = new JLabel();
            jLabel2 = new JLabel();
            //todo badges around icons !!!
            setLayout(new BorderLayout(5, 0));
            add(jCheckBox1, BorderLayout.WEST);
            add(jLabel1, BorderLayout.CENTER);
            add(jLabel2, BorderLayout.EAST);
        }
        
        public void setForeground(final Color fg){
            setForeground(fg, fg);
        }
        
        public void setBackground(final Color bg){
            setBackground(bg, bg);
        }
        
        public void setForeground(final Color selection, final Color text) {
            if ( jCheckBox1 == null || jLabel1 == null || jLabel2 == null) return;
            jCheckBox1.setForeground(text);
            jLabel1.setForeground(text);
            jLabel2.setForeground(selection);
            super.setForeground(selection);
        }
        
        public void setBackground(final Color selection, final Color text) {
            if ( jCheckBox1 == null || jLabel1 == null || jLabel2 == null) return;
            //System.err.println(jCheckBox1);
            jCheckBox1.setBackground(text);
            jLabel1.setBackground(text);
            jLabel2.setBackground(selection);
            super.setBackground(selection);
        }
        
        public void setFocusPainted(final boolean painted) {
            jCheckBox1.setFocusPainted(painted);
        }
        
        public void setText(final String text) {
            jLabel2.setText(text);
        }
        
        public String getText(){
            return jLabel2.getText();
        }
        
        public void setState(final Object state) {
            jCheckBox1.setState(state);
        }
        
        public void setIcon(final Icon icon){
            //System.err.println(icon);
            jLabel1.setIcon(icon);
        }
        
        public void addItemListener(final ItemListener itemListener) {
            // System.out.println("in add item listener");
            jCheckBox1.addItemListener(itemListener);
        }
        
        public void removeItemListener(final ItemListener itemListener) {
            // System.out.println("remove item listener");
            jCheckBox1.removeItemListener(itemListener);
        }
    }
    
}
