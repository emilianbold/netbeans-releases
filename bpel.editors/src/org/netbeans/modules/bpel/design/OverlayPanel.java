/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.decoration.components.AbstractGlassPaneButton;
import org.netbeans.modules.bpel.design.decoration.components.GlassPane;
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
    public void doLayout() {
        super.doLayout();
        
        for (Component c: getComponents()){
            if (!(c instanceof GlassPane)){
                continue;
            }
            GlassPane gp = (GlassPane) c;
            AbstractGlassPaneButton btn = (AbstractGlassPaneButton) gp.getDecoration();
            
           // btn.updateGlassPaneBounds();
            Point coords = SwingUtilities.convertPoint(btn, 0, 0, this);
            
            
            Rectangle visible = getVisibleRect();
            Dimension size = gp.getPreferredSize();
            int y = coords.y + btn.getHeight();//default position: glass pane below the button
            if ((y + size.height) > visible.getMaxY()){
                //not enough space to position below, put it under the button;
                y = coords.y - size.height;
            }
            
            int x = coords.x + btn.getWidth();
            if ((x + size.width) >  visible.getMaxX()){
                x -= (x + size.width) - visible.getMaxX();
            }
            
            gp.setAnchorPoint(coords.x - x + btn.getWidth() / 2, 
                              coords.y - y + btn.getHeight() / 2);
            
            gp.setBounds(x, y, size.width, size.height);
            
//            Point p = new Point (
//                    btn.getX() + btn.getWidth() / 2,
//                    btn.getY() + btn.getHeight() / 2);
//            
//            Container view = btn.getParent();
//                    
//            while(view != designView){
//                p.x += view.getX();
//                p.y += view.getY();
//                view = view.getParent();
//            }
//            
//            view = this;
//            
//            while(view != designView){
//                p.x -= view.getX();
//                p.y -= view.getY();
//                view = view.getParent();
//            }
//            
//            gp.setLocation(p);
            
            
            
        }
    }

    
    
    @Override
    protected void paintComponent(Graphics g) {
       
   
//        double zoom = designView.getCorrectedZoom();
//        
        Graphics2D g2 = GUtils.createGraphics(g);
//        
//        g2.scale(zoom, zoom);

        //fixme - render DND status here
        
        designView.getFlowLinkTool().paint(g2);
        
    }


     public FPoint convertScreenToDiagram(Point point) {
        double x = point.x / designView.getCorrectedZoom();
        
        double y = point.y / designView.getCorrectedZoom();

        return new FPoint(x, y);
    }

}
