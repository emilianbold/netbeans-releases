/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.dnd;


import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.view.Controller;


/**
 * Glass pane which is used for <code>DefaultContainerImpl</code>
 * as a component associated with <code>DropTarget</code> to be able
 * to paint 'drag under' indications for that container. 
 *
 *
 * @author  Peter Zavadsky
 *
 * @see java.awt.dnd.DropTarget
 * @see org.netbeans.core.windows.DefaultContainerImpl
 */
public final class DropTargetGlassPane extends JPanel implements DropTargetListener {

    // XXX PENDING
    private final Observer observer;
    // XXX PENDING
    private final Informer informer;
    
    /** Current location of cursor in over the glass pane,
     * or <code>null</code> in the case there it is not above
     * this component currently. */
    private Point location;
    
    /** <code>TopComponentDroppable</code> used in paint to get indication
     * rectangle. */
    private TopComponentDroppable droppable;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(DropTargetGlassPane.class);

    

    /** Creates non initialized <code>DropTargetGlassPane</code>. */
    public DropTargetGlassPane(Observer observer, Informer informer) {
        this.observer = observer;
        this.informer = informer;
        
        setOpaque(false);
    }

    
    /** Called when started drag operation, to save the old visibility state. */
    public void initialize() {
        if(isVisible()) {
            // For unselected internal frame the visibility could
            // be already set, but due to a bug is needed to revalidate it.
            revalidate();
        } else {
            setVisible(true);
        }
    }

    /** Called when finished drag operation, to reset the old visibility state. */
    public void uninitialize() {
        if(location != null) {
            // #22123. Not removed drop inidication.
            dragFinished();
        }

        setVisible(false);
    }
    
    /** Called when the drag operation performed over this drop target. */
    void dragOver(Point location, TopComponentDroppable droppable) {
        this.droppable = droppable;
        repaintForLocation(location);
    }
    
    /** Called when the drag operation exited from this drop target. */
    private void dragExited() {
        clear();
    }
    
    /** Hacks the problem when exiting of drop target, sometimes the framework
     * "forgets" to send drag exit event (when moved from the drop target too
     * quickly??) thus the indication rectangle remains visible. Used to fix
     * this problem. */
    public void clearIndications() {
        clear();
    }

    /** Called when changed drag action. */
    private void dragActionChanged(Point location) {
        repaintForLocation(location);
    }

    /** Called when drag operation finished. */
    private void dragFinished() {
        clear();
    }

    /** Repaints indications. */
    private void repaintForLocation(Point location) {
        if(this.location == location) {
            return;
        }
        this.location = location;
        repaint();
    }
    
    /** Clears glass pane. */
    private void clear() {
        this.location = null;
        this.droppable = null;
        repaint();
    }
    

    /** Overrides superclass method, to indicate the 'drag under' gesture
     * in the case there is cursor drag operation in progress and cursor
     * is above this drop target. */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

//        // Draws rectangle indicating glass pane presence, debug only.
//        Color oo = g.getColor();
//        g.setColor(Color.green);
//        Rectangle bounds = getBounds();
//        g.drawRect(0, 0 , bounds.width - 1, bounds.height - 1);
//        g.setColor(oo);
       
        Point position = location;
        if(position == null) {
            return;
        }

        TopComponentDroppable dr = this.droppable;
        if(dr == null) {
            return;
        }

        Point p = SwingUtilities.convertPoint(
                this, position, dr.getDropComponent());
        Shape s = dr.getIndicationForLocation(p);
        drawIndication(g, s, dr.getDropComponent());
    }
    
    // PENDING Take the color from UI Defaults
    private static final Color FILL_COLOR = new Color( 200, 200, 200, 120 );
    
    private void drawIndication(Graphics gr, Shape s, Component source) {
        if(s == null) {
            return;
        }

        // Actually paint.
        Graphics2D g = (Graphics2D)gr;

        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();

	// PENDING Take the color from UI Defaults
        g.setColor(Color.red);        	
	
        g.setStroke(createIndicationStroke());
        
	Color fillColor = Constants.SWITCH_DROP_INDICATION_FADE ? FILL_COLOR : null; 
        if(s instanceof Rectangle) {
            drawIndicationRectangle(g, (Rectangle)s, source, fillColor);
        } else if(s instanceof GeneralPath) {
            drawIndicationGeneralPath(g, (GeneralPath)s, source, fillColor);	    
        }
                
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }
    
    private void drawIndicationRectangle(Graphics2D g, Rectangle r, Component source, Color fillColor ) {
        r = SwingUtilities.convertRectangle(source, r, this);
        // XXX Shrinks the rectangle to take into account the width of pen stroke.	
        g.drawRect(r.x+1, r.y+1, r.width-2, r.height-2);
	if ( fillColor != null ) {
	    g.setColor(fillColor);
	    g.fillRect(r.x+1, r.y+1, r.width-2, r.height-2);
	}
    }
    
    private void drawIndicationGeneralPath(Graphics2D g, GeneralPath path, Component source, Color fillColor ) {
        // Convert path to this coordinates.
        Point point = new Point(0, 0);
        point =  SwingUtilities.convertPoint(source, point, this);
        path.transform(new AffineTransform(1D, 0D, 0D, 1D, point.x, point.y));
        
        g.draw(path);
	if ( fillColor != null ) {
	    g.setColor( fillColor );
	    g.fill(path);
	}
    }
    
    
    
    
    // >> DropTargetListener implementation >>
    /** Implements <code>DropTargetListener</code> method.
     * accepts/rejects the drag operation if move or copy operation
     * is specified. */
    public void dragEnter(DropTargetDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragEnter"); // NO18N
        }
        
        int dropAction = evt.getDropAction();
        // Mask action NONE to MOVE one.
        if(dropAction == DnDConstants.ACTION_NONE) {
            dropAction = DnDConstants.ACTION_MOVE;
        }
        
        if((dropAction & DnDConstants.ACTION_COPY_OR_MOVE) > 0) {
            evt.acceptDrag(dropAction);
        } else {
            evt.rejectDrag();
        }
    }

    /** Implements <code>DropTargetListener</code> method.
     * Unsets the glass pane to show 'drag under' gestures. */
    public void dragExit(DropTargetEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragExit"); // NO18N
        }
        
        Component c = evt.getDropTargetContext().getComponent();
        if(c == this) {
            this.dragExited();
        }
    }
    
    /** Implements <code>DropTargetListener</code> method.
     * Informs the glass pane about the location of dragged cursor above
     * the component. */
    public void dragOver(DropTargetDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dragOver"); // NOI18N
        }
        
        // XXX Eliminate bug, see dragExitedHack.
        observer.setLastDropTarget(this);
    }

    /** Implements <code>DropTargetListener</code> method.
     * When changed the drag action accepts/rejects the drag operation
     * appropriatelly */
    public void dropActionChanged(DropTargetDragEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("dropActionChanged"); // NOI18N
        }
        
        int dropAction = evt.getDropAction();
        boolean acceptDrag;
        
        if((dropAction == DnDConstants.ACTION_MOVE)
        || (dropAction == DnDConstants.ACTION_COPY
            && informer.isCopyOperationPossible())) {
                
            acceptDrag = true;
        } else {
            acceptDrag = false;
        }

        if(acceptDrag) {
            evt.acceptDrag(dropAction);
        } else {
            evt.rejectDrag();
        }
        
        Component c = evt.getDropTargetContext().getComponent();
        if(c == this) {
            this.dragActionChanged(acceptDrag ? evt.getLocation() : null);
        }
    }

    /** Implements <code>DropTargetListener</code> method. 
     * Perfoms the actual drop operation. */
    public void drop(DropTargetDropEvent evt) {
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("drop"); // NOI18N
        }
        
        // Inform glass pane about finished drag operation.
        Component c = evt.getDropTargetContext().getComponent();
        if(c == this) {
            this.dragFinished();
        }

        int dropAction = evt.getDropAction();
        if(dropAction != DnDConstants.ACTION_MOVE
        && dropAction != DnDConstants.ACTION_COPY) {
            // Not supported dnd operation.
            evt.rejectDrop();
            return;
        }
        
        // Accepts drop operation.
        evt.acceptDrop(dropAction);
        
        boolean success = false;

        try {
            Point location = evt.getLocation();

            // Checks whetger it is in around center panel area.
            // In that case the drop will be tried later.
            // PENDING unify it.
            SwingUtilities.convertPointToScreen(location, c);
            if(WindowDnDManager.isAroundCenterPanel(location)) {
                return;
            }

            success = WindowDnDManager.tryPerformDrop(
                    informer.getController(), informer.getFloatingFrames(),
                    location, dropAction, evt.getTransferable());
        } finally {
            // Complete the drop operation.
            // XXX #21917.
            observer.setDropSuccess(success);
            evt.dropComplete(false);
            //evt.dropComplete(success);
        }
    }
    // >> DropTargetListener implementation >>

    /** Creates indication pen stroke. Utility method. */
    private static Stroke createIndicationStroke() {
        return new BasicStroke(2); // width of stroke is bigger to default one
//        float[] dashPattern = { 1, 1 };
//        return new BasicStroke(
//            3.0F, // width
//            BasicStroke.CAP_BUTT, // decoration of the ends
//            BasicStroke.JOIN_MITER, // decoration applied when segments meet
//            1.0F, // mitter limit where to trim the join
//            dashPattern, // dashing pattern
//            0.0F // offset to start the dashing pattern
//        ); // PENDING
    }

    private static void debugLog(String message) {
        Debug.log(DropTargetGlassPane.class, message);
    }
    
    
    // XXX
    /** Glass pane uses this interface to inform about changes. */
    interface Observer {
        public void setDropSuccess(boolean success);
        public void setLastDropTarget(DropTargetGlassPane glassPane);
    } // End of Observer.

    // XXX
    interface Informer {
        public boolean isCopyOperationPossible();
        public Controller getController();
        public Set getFloatingFrames();
    }

}
