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
/*
 * ExpandCollapseButton.java
 *
 * Created on May 25, 2006, 10:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author girix
 */
class ExpandCollapseButton extends JPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    
    public static final int WIDTH = 12;
    public static final int HEIGHT = 12;
    
    boolean mouseInside = false;
    
    public ExpandCollapseButton(String str){
        this(str, true);
    }
    
    public ExpandCollapseButton(String str, final boolean autoChangeState){
        //super(str);
        if(!str.equals("+") && !str.equals("-"))
            throw new IllegalArgumentException("Arg can be only + or -");
        this.text = str;
        setOpaque(false);
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                fireActionPerformedEvent();
                if(autoChangeState)
                    setText(getText().equals("+") ?  "-" : "+");
            }
            
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                mouseInside = false;
                repaint();
            }
            
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                mouseInside = true;
                repaint();
            }
            
        });
        
    }
    
    
    List<ActionListener> all = new ArrayList<ActionListener>();
    public void addActionListener(ActionListener al){
        all.add(al);
    }
    
    private void fireActionPerformedEvent(){
        for(ActionListener al: all){
            al.actionPerformed(new ActionEvent(this, 1980, getText()));
        }
    }
    
    public boolean isExpanded(){
        return getText().equals("-") ? true : false;
    }
    
    public boolean isCollapsed(){
        return !isExpanded();
    }
    
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        int w = 10 - 2;//8
        int h = 10 - 2;//8
        int xo = 0;
        int yo = 0;//getHeight()/2 - 7;// (getHeight()-h)/2-2;
        
        if(mouseInside)
            g.setColor(InstanceDesignConstants.MOUSEOVER_EXPAND_BUTTON_COLOR);
        else
            g.setColor(InstanceDesignConstants.DARK_BLUE);
        if(dragMode)
            g.setColor(Color.WHITE);
        
        Polygon shape = null;
        if(getText().equals("+"))
            shape = new Polygon(
                    new int[] {xo, xo  , xo+w, xo},
                    new int[] {yo, yo+h, yo+h/2, yo}, 3);
        else{
            shape = new Polygon(
                    new int[] {xo, xo+w, xo+w/2, xo },
                    new int[] {yo, yo  , yo+h, yo }, 3);
        }
        g.drawPolygon(shape);
        g.fillPolygon(shape);
    }
    
    public Dimension getPreferredSize(){
        Dimension dim = new Dimension(WIDTH, HEIGHT);
        return dim;
    }
    
    public Dimension getMinimumSize(){
        return getPreferredSize();
    }
    
    
    public Dimension getMaximumSize(){
        return getPreferredSize();
    }
    
    String text;
    public synchronized String getText(){
        return text;
    }
    
    public synchronized void setText(String text){
        this.text = text;
        repaint();
    }
    
    boolean dragMode = false;
    public void setDragMode(boolean dragMode) {
        this.dragMode = dragMode;
        repaint();
    }
    
    public void setWatchForComponent(Component comp){
        comp.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        if(ExpandCollapseButton.this.isCollapsed())
                            ExpandCollapseButton.this.setText("-");
                    }
                });
            }
            public void componentHidden(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        if(ExpandCollapseButton.this.isExpanded())
                            ExpandCollapseButton.this.setText("+");
                    }
                    
                });
            }
        });
    }
}