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
