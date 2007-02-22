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
 * HyperlinkLabel.java
 *
 * Created on September 18, 2006, 7:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

/**
 *
 * @author girix
 */
public class HyperlinkLabel extends JLabel{
    private static final long serialVersionUID = -483941387931729295L;
    /** Creates a new instance of HyperlinkLabel */
    public HyperlinkLabel() {
        super();
        initialize();
    }
    
    private void initialize(){
        initMouseListener();
    }
    
    boolean mouseIn = false;
    private void initMouseListener() {
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if(hyperlinkClickHandler != null)
                    hyperlinkClickHandler.handleClick();
            }
            public void mouseEntered(MouseEvent e) {
                if(hyperlinkClickHandler != null){
                    mouseIn = true;
                    HyperlinkLabel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if(hyperlinkClickHandler != null){
                    mouseIn = false;
                    HyperlinkLabel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
            public void mousePressed(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
        });
    }
    
    protected void paintComponent(Graphics g) {
        if(mouseIn){
            //draw diff color and underline
            Color origC = getForeground();
            Font origF = getFont();
            Rectangle bounds = g.getClipBounds();
            Color bak = getForeground();
            setForeground(Color.BLUE);
            Color gbak = g.getColor();
            g.setColor(Color.blue);
            super.paintComponent(g);
            int width = bounds.width;
            g.drawLine(bounds.x+5, bounds.y + bounds.height -1,
                    bounds.x+5 + width - 5,  bounds.y + bounds.height -1);
            setForeground(bak);
            g.setColor(gbak);
        }else{
            super.paintComponent(g);
        }
    }
    
    
    HyperlinkClickHandler hyperlinkClickHandler;
    public void setHyperlinkClickHandler(HyperlinkClickHandler hyperlinkClickHandler){
        this.hyperlinkClickHandler = hyperlinkClickHandler;
        
    }
    
    public interface HyperlinkClickHandler{
        public void handleClick();
    }
    
}
