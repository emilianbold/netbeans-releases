package org.netbeans.modules.iep.editor.designer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;

public class Port extends SimpleNodePort {

    private JGoPen mPen;
    
    public Port(boolean input, JGoArea parent) {
        super(input, parent);
        mPen = new JGoPen(JGoPen.SOLID, 1, Color.RED);
        this.setStyle(JGoPort.StyleHidden);
        
    }
    
    @Override
    public void paint(Graphics2D g, JGoView view) {
        //super.paint(g, view);
        //don't paint anything if it's a hidden port
        if(getStyle() == StyleHidden) return;
        else if (getStyle() == StyleObject) {
        Rectangle r = getBoundingRect();
        int x1 = r.x;
        int y1 = r.y;
        
        int x2 = r.x + r.width;
        int y2 = r.y + r.height;
        
        drawLine(g, mPen, x1, y1, x2, y2);
        
        int x3 = r.x + r.width;
        int y3 = r.y;
        
        int x4 = r.x;
        int y4 = r.y + r.height;
        
        drawLine(g, mPen, x3, y3, x4, y4);
        }
    }
}
