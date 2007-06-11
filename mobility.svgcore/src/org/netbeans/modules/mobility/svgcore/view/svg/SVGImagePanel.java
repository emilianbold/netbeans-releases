/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * SVGImagePanel.java
 * Created on May 30, 2007, 11:25 AM
 */

package org.netbeans.modules.mobility.svgcore.view.svg;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Pavel Benes
 */
public abstract class SVGImagePanel extends JPanel {
    public static final int CROSS_SIZE = 10;
    
    private final JComponent imagePanel;
    
    private class CenteredLayoutManager implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {
            assert imagePanel.equals(comp);
        }

        public Dimension preferredLayoutSize(Container parent) {
            return imagePanel.getSize();
        }

        public Dimension minimumLayoutSize(Container parent) {
            return imagePanel.getSize();
        }

        public void layoutContainer(Container parent) {
            imagePanel.setLocation( (parent.getWidth() - imagePanel.getWidth()) / 2,
                    (parent.getHeight() - imagePanel.getHeight()) / 2);
        }

        public void removeLayoutComponent(Component comp) {
            assert imagePanel.equals(comp);
        }            
    };
        
    
    public SVGImagePanel(JComponent imagePanel) {
        this.imagePanel = imagePanel;
        setLayout( new CenteredLayoutManager());
        add(imagePanel);
    }
    
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        int xOff = imagePanel.getX();
        int yOff = imagePanel.getY();

        g.setColor(Color.BLACK);
        //g.drawRect( xOff - 1, yOff - 1, imagePanel.getWidth() + 2, imagePanel.getHeight() + 2);
        drawCross( g, xOff - 1, yOff -1);
        drawCross( g, xOff - 1, yOff + imagePanel.getHeight() + 1);
        drawCross( g, xOff + imagePanel.getWidth() + 1, yOff -1);
        drawCross( g, xOff + imagePanel.getWidth() + 1, yOff + imagePanel.getHeight() + 1);

        paintPanel(g, xOff, yOff, imagePanel.getWidth(), imagePanel.getHeight());
    }   

    protected abstract void paintPanel(Graphics g, int x, int y, int w, int h);

    private static void drawCross(Graphics g, int x, int y) {
        g.drawLine( x - CROSS_SIZE, y, x + CROSS_SIZE, y);
        g.drawLine( x, y - CROSS_SIZE, x, y + CROSS_SIZE);                
    }            
}
