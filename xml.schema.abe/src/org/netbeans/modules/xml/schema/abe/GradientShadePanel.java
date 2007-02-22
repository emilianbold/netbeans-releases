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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * $Id$
 */


/*
 * GradientShadePanel.java
 *
 * Created on May 23, 2006, 10:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 * @author girix
 */
public abstract class GradientShadePanel extends ABEBaseDropPanel{
    private static final long serialVersionUID = 7526472295622776147L;
    /** Creates a new instance of GradientShadePanel */
    public GradientShadePanel(InstanceUIContext context) {
        super(context);
    }
    /**
     *
     *
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        Rectangle rect = g2d.getClipBounds();
        
        Color start = normalTopGradientColor;
        Color end = normalBottomGradientColor;
        if(draging){
            start = dragTopGradientColor;
            end = dragBottomGradientColor;
        }else if(context.getComponentSelectionManager().isSelected(this)){
            start = selectedTopGradientColor;
            end = selectedBottomGradientColor;
        }
        
        GradientPaint fill=new GradientPaint(
                (float)rect.x, (float)rect.y, start,
                (float)rect.x, (float)rect.height, end);
        
        g2d.setPaint(fill);
        g2d.fill(rect);
        
        
    }
    
    public void dragExit(DropTargetEvent event) {
        draging = false;
        repaint();
    }
    
    public void dragOver(DropTargetDragEvent event) {
        draging = true;
        repaint();
    }
    
    public void dragEnter(DropTargetDragEvent event) {
        draging = true;
        repaint();
    }
    
    public void drop(DropTargetDropEvent event) {
        draging = false;
        repaint();
    }
    
    protected boolean draging = false;
    
    protected Color selectedTopGradientColor = Color.WHITE;
    protected Color selectedBottomGradientColor = InstanceDesignConstants.DARK_BLUE;
    
    protected Color dragTopGradientColor = Color.WHITE;
    protected Color dragBottomGradientColor = InstanceDesignConstants.XP_ORANGE;
    
    protected Color normalTopGradientColor = Color.WHITE;
    protected Color normalBottomGradientColor = Color.LIGHT_GRAY.brighter();
    
}
