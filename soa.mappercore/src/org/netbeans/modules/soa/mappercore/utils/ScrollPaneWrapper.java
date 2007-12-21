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

package org.netbeans.modules.soa.mappercore.utils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author anjeleevich
 */
public class ScrollPaneWrapper extends JPanel {
    
    private JScrollPane scrollPane;
    
    
    /** Creates a new instance of ScrollPaneWrapper */
    public ScrollPaneWrapper(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
        add(scrollPane);
    }
    
    
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
    
    
    public Dimension getPreferredSize() {
        Insets insets = scrollPane.getInsets();
        Dimension size = scrollPane.getPreferredSize();
        
        size.width = Math.max(24, size.width - insets.left - insets.right);
        size.height = Math.max(24, size.height - insets.top - insets.bottom);
        
        return size;
    }
    
    
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        
        Insets insets = scrollPane.getInsets();
        
        int x = -insets.left;
        int y = -insets.top;
        
        w += insets.left + insets.right;
        h += insets.top + insets.bottom;
        
        scrollPane.setBounds(x, y, w, h);
    }
    
    
    protected void paintBorder(Graphics g) {}
    protected void paintComponent(Graphics g) {}
}
