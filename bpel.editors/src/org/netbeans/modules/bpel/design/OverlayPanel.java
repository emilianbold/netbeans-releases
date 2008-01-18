/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.design;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JComponent;
import org.netbeans.modules.bpel.design.geometry.FPoint;

/**
 *
 * @author Alexey
 */
public class OverlayPanel extends JComponent{

    private DesignView designView;
    
    public OverlayPanel(DesignView designView){
        this.designView = designView;
        setOpaque(false);
        
    }
    
    @Override
    protected void paintComponent(Graphics g) {
       
   
        double zoom = designView.getCorrectedZoom();
        
        Graphics2D g2 = GUtils.createGraphics(g);
        
        g2.scale(zoom, zoom);

        designView.getGhost().paint(g2);
        designView.getFlowLinkTool().paint(g2);
        
    }


     public FPoint convertScreenToDiagram(Point point) {
        double x = point.x / designView.getCorrectedZoom();
        
        double y = point.y / designView.getCorrectedZoom();

        return new FPoint(x, y);
    }

}
