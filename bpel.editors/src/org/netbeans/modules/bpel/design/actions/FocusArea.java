/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.bpel.design.actions;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;


/**
 *
 * @author Alexey
 */
public class FocusArea {
    private DiagramView diagramView;
    
    public FocusArea (DiagramView diagramView){
        this.diagramView = diagramView;
    }
    
    
    public Point getFocusAreaCenter(Pattern pattern) {
       return null; //fixme
       /* if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            
            if (border != null) {
                return diagramView
                        .convertDiagramToScreen(border.getBounds().getTopCenter());
            }
        }
        
        return diagramView
                .convertDiagramToScreen(pattern.getFirstElement().getBounds()
                .getCenter());*/
    }
    
    
    public Rectangle getFocusAreaBounds(Pattern pattern) {
       /* FPoint topLeft = null;
        FPoint bottomRight = null;
        
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            
            if (border != null) {
                FBounds bounds = border.getBounds();
                topLeft = bounds.getTopLeft();
                bottomRight = bounds.getTopRight(); // currect!
            }
        }
        
        if (topLeft == null) {
            FBounds bounds = pattern.getFirstElement().getBounds();
            topLeft = bounds.getTopLeft();
            bottomRight = bounds.getBottomRight();
        }
        //FIXME
        Point p1 = diagramView.convertDiagramToScreen(topLeft);
        Point p2 = diagramView.convertDiagramToScreen(bottomRight);
        
        return new Rectangle(p1.x - 24, p1.y - 24, 
                p2.x - p1.x + 48, p2.y - p1.y + 48);*/
        return null;
    }
}
