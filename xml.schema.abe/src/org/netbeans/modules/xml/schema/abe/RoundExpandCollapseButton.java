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
 * RoundExpandCollapseButton.java
 *
 * Created on June 19, 2006, 1:00 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.JButton;

/**
 *
 * @author girix
 */
public class RoundExpandCollapseButton extends JButton{
    private static final long serialVersionUID = 7526472295622776147L;
    public static final int WIDTH = 15;
    public static final int HEIGHT = 15;
    boolean mouseOverMe = false;
    /** Creates a new instance of RoundExpandCollapseButton */
    public RoundExpandCollapseButton(String str, boolean autoChangeState) {
        super(str);
        setOpaque(false);
        addMouseListener(new MouseAdapter(){
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                mouseOverMe = false;
                repaint();
            }

            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                mouseOverMe = true;
                repaint();
            }
            
        });
    }
    
    public void paint(Graphics g){
        //super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        //clear stuff drawn by parent
        //g.clearRect(0, 0, getWidth(), getHeight());
        //g.setColor(Color.GRAY);
        
        
        if (super.getText().equals("+")){
            //draw >
            String str = "/org/netbeans/modules/xml/schema/abe/resources/";
            str += mouseOverMe ? "expandButtonMouseOver.png" : "expandButton.png";
            str = dragMode ? "/org/netbeans/modules/xml/schema/abe/resources/expandButtonDrag.png" : str;
            URL url = RoundExpandCollapseButton.class.getResource(str);
            Image img = new javax.swing.ImageIcon(url).getImage();
            g2d.drawImage(img, 4, 4, null);
        }else{
            //draw <
            String str = "/org/netbeans/modules/xml/schema/abe/resources/";
            str += mouseOverMe ? "collapseButtonMouseOver.png" : "collapseButton.png";
            str = dragMode ? "/org/netbeans/modules/xml/schema/abe/resources/collapseButtonDrag.png" : str;
            URL url = RoundExpandCollapseButton.class.getResource(str);
            Image img = new javax.swing.ImageIcon(url).getImage();
            g2d.drawImage(img, 4, 4, null);
        }
        
        
        
        
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
    
    boolean dragMode = false;
    public void setDragMode(boolean dragMode){
        this.dragMode = dragMode;
        repaint();
    }
}
