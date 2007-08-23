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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Layes components in vertical axis one by one,
 * bottom by top.
 *
 * <p>Uses components preferred height
 * but forces them fixed width.
 *
 * @author Petr Kuzel
 */
public class VerticalFlowLayout implements LayoutManager {

    private final Component parent;

    /**
     * Creates a new instance of VerticalFlowLayout
     * @param parent <tt>parent.getWidth()</tt> defines the fixed width
     */
    public VerticalFlowLayout(Component parent) {
        this.parent = parent;
    }
    
    public void addLayoutComponent(String name, Component comp) {
    }
    
    public void removeLayoutComponent(Component comp) {
    }
    
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            
            for (int i = 0 ; i < nmembers ; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    dim.width = Math.max(dim.width, d.width);
                    dim.height += d.height;
                }
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
    }
    
    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            
            for (int i = 0 ; i < nmembers ; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getMinimumSize();
                    dim.width = Math.max(dim.width, d.width);
                    dim.height += d.height;
                }
            }
            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
    }
    
    public void layoutContainer(Container target) {
        
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxwidth = parent.getWidth() - (insets.left + insets.right);
            int nmembers = target.getComponentCount();
            int x = insets.left, y = insets.top;
            
            for (int i = 0 ; i < nmembers ; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    m.setSize(maxwidth, d.height);
                    m.setLocation(x, y);
                    y += d.height;
                }
            }
        }
    }
    
}
