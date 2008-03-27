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

package org.netbeans.modules.soa.mappercore;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 *
 * @author anjeleevich
 */
public class MapperLayout implements LayoutManager {
    
    private JComponent leftScroll;
    private JComponent rightScroll;
    private JComponent centerScroll;
    
    private JComponent leftDivider;
    private JComponent rightDivider;
    
    private JComponent toolBar;
    
    
    public void addLayoutComponent(String name, Component comp) {
        if (LEFT_SCROLL.equals(name)) {
            leftScroll = (JComponent) comp;
        } else if (RIGHT_SCROLL.equals(name)) {
            rightScroll = (JComponent) comp;
        } else if (CENTER_SCROLL.equals(name)) {
            centerScroll = (JComponent) comp;
        } else if (LEFT_DIVIDER.equals(name)) {
            leftDivider = (JComponent) comp;
        } else if (RIGHT_DIVIDER.equals(name)) {
            rightDivider = (JComponent) comp;
        } else if (TOOL_BAR.equals(name)) {
            toolBar = (JComponent) comp;
        }
    }
    
    
    public void removeLayoutComponent(Component comp) {}

    
    public Dimension preferredLayoutSize(Container parent) {
        return calculateSize((Mapper) parent, false);
    }


    public Dimension minimumLayoutSize(Container parent) {
        return calculateSize((Mapper) parent, true);
    }
    
    
    private Dimension calculateSize(Mapper mapper, boolean minimum) {
        synchronized (mapper.getTreeLock()) {
            Insets insets = mapper.getInsets();
            
            Dimension leftSize = (minimum) 
                    ? leftScroll.getMinimumSize() 
                    : leftScroll.getPreferredSize();
            Dimension centerSize = (minimum)
                    ? centerScroll.getMinimumSize()
                    : centerScroll.getPreferredSize();
            Dimension rightSize = (minimum) 
                    ? rightScroll.getMinimumSize()
                    : rightScroll.getPreferredSize();
            
            int w = leftSize.width + DIVIDER_WIDTH + centerSize.width 
                    + DIVIDER_WIDTH + rightSize.width;
                    
            int h = Math.max(centerSize.height, Math.max(leftSize.height, 
                    rightSize.height));
            
            if (toolBar != null) {
                Dimension toolBarSize = (minimum) 
                        ? toolBar.getMinimumSize()
                        : toolBar.getPreferredSize();
                
                w = Math.max(w, toolBarSize.width);
                h += toolBarSize.height;
            }

            w += insets.left + insets.right;
            h += insets.top + insets.bottom;
            
            return new Dimension(w, h);
        }
    }


    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Mapper mapper = (Mapper) parent;
            Insets insets = mapper.getInsets();
            
            int w = mapper.getWidth();
            int h = mapper.getHeight();
            
            int leftDividerPosition = mapper.getLeftDividerPosition();
            int rightDividerPosition = mapper.getRightDividerPosition();
            
            if (leftDividerPosition < 0 || rightDividerPosition < 0) {
                int size = w - insets.right - insets.left - 2 * DIVIDER_WIDTH;
                
                leftDividerPosition = size / 4;
                rightDividerPosition = size / 4;
            }
            
            leftDividerPosition = Math.max(leftDividerPosition, MIN_WIDTH);
            rightDividerPosition = Math.max(rightDividerPosition, MIN_WIDTH);
            
            int prefWidth = leftDividerPosition + rightDividerPosition 
                    + 2 * DIVIDER_WIDTH + MIN_WIDTH + insets.left 
                    + insets.right;
            
            if (prefWidth > w) {
                // we should decrease width of left and right components
                int extraLeftSpace = leftDividerPosition - MIN_WIDTH;
                int extraRightSpace = rightDividerPosition - MIN_WIDTH;
                int delta = prefWidth - w;
                extraLeftSpace -= delta / 2;
                extraRightSpace -= delta - delta / 2;
                
                if (extraLeftSpace < 0) {
                    extraRightSpace += extraLeftSpace;
                } else if (extraRightSpace < 0) {
                    extraLeftSpace += extraRightSpace;
                }
                
                leftDividerPosition = MIN_WIDTH + Math.max(extraLeftSpace, 0);
                rightDividerPosition = MIN_WIDTH + Math.max(extraRightSpace, 0);
            }
            
            int y = insets.top;
            h -= insets.top + insets.bottom;
            
            if (toolBar != null) {
                int toolBarHeight = toolBar.getPreferredSize().height;
                
                toolBar.setBounds(insets.left, y, 
                        w - insets.right - insets.left, toolBarHeight);
                
                y += toolBarHeight;
                h -= toolBarHeight;
            }
            
            // (x1 - x2) - left
            // (x3 - x4) - center
            // (x5 - x6) - right
            
            int x1 = insets.left;
            int x6 = w - insets.right;
            
            int x2 = x1 + leftDividerPosition;
            int x5 = x6 - rightDividerPosition;
                    
            int x3 = x2 + DIVIDER_WIDTH;
            int x4 = x5 - DIVIDER_WIDTH;
            
            int d = x4 - x3 - MIN_WIDTH;
            
            if (d < 0) {
                x4 -= d;
                x5 -= d;
                x6 -= d;
            }
            
            leftScroll.setBounds(x1, y, x2 - x1, h);
            leftDivider.setBounds(x2, y, x3 - x2, h);
            centerScroll.setBounds(x3, y, x4 - x3, h);
            rightDivider.setBounds(x4, y, x5 - x4, h);
            rightScroll.setBounds(x5, y, x6 - x5, h);
            
            mapper.setDividerPositions(
                    leftDividerPosition, 
                    rightDividerPosition);
        }
    }
    
    
    public static final String TOOL_BAR = "TOOL_BAR";
    public static final String LEFT_SCROLL = "LEFT_SCROLL";
    public static final String RIGHT_SCROLL = "RIGHT_SCROLL";
    public static final String CENTER_SCROLL = "CENTER_SCROLL";
    public static final String LEFT_DIVIDER = "LEFT_DIVIDER";
    public static final String RIGHT_DIVIDER = "RIGHT_DIVIDER";

    public static final int MIN_WIDTH = 64;
    public static final int DIVIDER_WIDTH = 6;
    public static final int MIN_DELTA = MIN_WIDTH + 2 * DIVIDER_WIDTH;
}
