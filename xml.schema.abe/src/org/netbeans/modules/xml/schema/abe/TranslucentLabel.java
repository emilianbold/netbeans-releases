/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

/*
 * TranslucentLabel.java
 *
 * Created on June 21, 2006, 6:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *
 * @author girix
 */
public class TranslucentLabel extends JLabel{
    private static final long serialVersionUID = 7526472295622776147L;
    
    public TranslucentLabel(Icon icon){
        super(icon);
    }
    
    public TranslucentLabel(Icon icon, int allignment){
        super(icon, allignment);
    }
    
    public TranslucentLabel(String str, int allignment){
        super(str, allignment);
    }
    
    public void paint(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        Rectangle rect = g2d.getClipBounds();
        
        int rule = AlphaComposite.SRC_OVER;
        float alpha = 1.0f;
        g2d.setComposite(AlphaComposite.getInstance(rule, alpha));
        
        GradientPaint fill=new GradientPaint(
                (float)rect.x,(float)rect.y,getBackground() ,
                (float)rect.x,(float)rect.height,getBackground());
        
        
        g2d.setPaint(fill);
        
        g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        super.paint(g2d);
    }
    
}
