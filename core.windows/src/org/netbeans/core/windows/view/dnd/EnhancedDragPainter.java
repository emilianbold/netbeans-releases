/*
 * EnhancedDragPainter.java
 *
 * Created on January 14, 2005, 9:35 AM
 */

package org.netbeans.core.windows.view.dnd;

import java.awt.Graphics2D;

/**
 * allows to intercept the painting of the drag&drop shape. Used to add sliding background to the 
 * drags..
 * @author mkleint
 */
public interface EnhancedDragPainter {
    
    void additionalDragPaint(Graphics2D g);
    
}
