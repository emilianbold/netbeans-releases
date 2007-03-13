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

import java.awt.*;
import java.beans.BeanInfo;
import javax.swing.*;
import javax.swing.tree.*;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 * @author Petr Hrebejk
 */
class CheckRenderer extends JPanel implements TreeCellRenderer {

    private JCheckBox check;
    private JLabel label;
            
    private static final JList LIST_FOR_COLORS = new JList();
    
    public CheckRenderer() {
        
        setLayout(new BorderLayout() );
        setOpaque(true);
        
        this.check = new JCheckBox();
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
            check.setVisible(description.isSelectable());
            check.setSelected(description.isSelected());
        }
            
        if ( isSelected ) {
            label.setForeground(LIST_FOR_COLORS.getSelectionForeground());
            setBackground(LIST_FOR_COLORS.getSelectionBackground());
        }
        else {
            label.setForeground(tree.getForeground());
            setBackground(tree.getBackground());
        }
        
        label.setText( n.getHtmlDisplayName() );
        label.setIcon( new ImageIcon( n.getIcon(BeanInfo.ICON_COLOR_16x16) ) ); // XXX Ask description directly
        
        return this;
        
    }
    
    public Rectangle getCheckBounds() {
        return (Rectangle)check.getBounds().clone();
    }
        
//    public void paintComponent (Graphics g) {
//        Dimension d_check = check == null ? new Dimension(0, 0) : check.getSize();
//        Dimension d_label = stringDisplayer == null ? new Dimension(0,0) : 
//            stringDisplayer.getPreferredSize();
//            
//        int y_check = 0;
//        int y_label = 0;
//        
//        if (d_check.height >= d_label.height) {
//            y_label = (d_check.height - d_label.height) / 2;
//        }
//        if (check != null) {
//            check.setBounds (0, 0, d_check.width, d_check.height);
//            check.paint(g);
//        }
//        if (stringDisplayer != null) {
//            int y = y_label-2;
//            stringDisplayer.setBounds (d_check.width, y, 
//                d_label.width, getHeight()-1);
//            g.translate (d_check.width, y_label);
//            stringDisplayer.paint(g);
//            g.translate (-d_check.width, -y_label);
//        }
//    }
//    
//    public Dimension getPreferredSize() {
//        if (stringDisplayer != null) {
//            stringDisplayer.setFont(getFont());
//        }
//        Dimension d_check = check == null ? new Dimension(0, checkDim.height) : 
//            check.getPreferredSize();
//            
//        Dimension d_label = stringDisplayer != null ? 
//            stringDisplayer.getPreferredSize() : new Dimension(0,0);
//            
//        return new Dimension(d_check.width  + d_label.width, (d_check.height < d_label.height ? d_label.height : d_check.height));
//    }
//    
//    public void doLayout() {
//        Dimension d_check = check == null ? new Dimension(0, 0) : check.getPreferredSize();
//        Dimension d_label = stringDisplayer == null ? new Dimension (0,0) : stringDisplayer.getPreferredSize();
//        int y_check = 0;
//        int y_label = 0;
//        
//        if (d_check.height < d_label.height)
//            y_check = (d_label.height - d_check.height) / 2;
//        else
//            y_label = (d_check.height - d_label.height) / 2;
//
//        if (check != null) {
//            check.setLocation(0, y_check);
//            check.setBounds(0, y_check, d_check.width, d_check.height);
//            if (checkBounds == null)
//                checkBounds = check.getBounds();
//        }
//    }
//
//    public static Rectangle getCheckBoxRectangle() {
//        return (Rectangle) checkBounds.clone();
//    }
}
