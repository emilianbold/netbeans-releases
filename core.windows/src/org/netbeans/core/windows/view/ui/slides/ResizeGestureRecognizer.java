/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.Constants;

/**
 *
 * @author mkleint
 */
public class ResizeGestureRecognizer implements AWTEventListener {
    

     void attachResizeRecognizer(String side, Component component) {
         update(side, component);
         Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
     }
     void detachResizeRecognizer(String side, Component component) {
         Toolkit.getDefaultToolkit().removeAWTEventListener(this);
         update(null, null);
     }
     
    
    private static final int RESIZE_BUFFER = 5;
    private boolean isResizing = false;
    private Component comp;
    private String side;
    private CommandManager mgr;
    
    private GlassPane glass;
    private Component oldGlass;
    
    private int state;
    private Point startPoint;
    private static final int STATE_NOOP = 0;
    private static final int STATE_START = 1;
    private static final int STATE_DRAGGING = 2;
    
    /** Creates a new instance of ResizeGestureRecognizer */
    public ResizeGestureRecognizer(CommandManager mgr) {
        this.mgr = mgr;
        glass = new GlassPane();
    }
    
    public void update(String side, Component component) {
        this.side = side;
        comp = component;
        state = STATE_NOOP;
        resetState();
    }

    private boolean isInResizeArea(MouseEvent event) {
        if (comp == null || side == null) {
            return false;
        }
        Point leftTop = new Point(0, 0);
        leftTop = SwingUtilities.convertPoint(comp, leftTop, SwingUtilities.getRoot(comp));
        Point evtPoint = SwingUtilities.convertPoint(event.getComponent(), 
                              event.getPoint(), SwingUtilities.getRoot(event.getComponent()));
        if (Constants.BOTTOM.equals(side)) {
            if (evtPoint.x > leftTop.x && evtPoint.x < (leftTop.x + comp.getBounds().width)) {
                if ( Math.abs(evtPoint.y - leftTop.y) < RESIZE_BUFFER) {
                    return true;
                } 
            }
        }
        if (Constants.LEFT.equals(side)) {
            if (evtPoint.y > leftTop.y && evtPoint.y < (leftTop.y + comp.getBounds().height)) {
                int right = comp.getBounds().width + leftTop.x;
                if (Math.abs(evtPoint.x - right) < RESIZE_BUFFER) {
                    return  true;
                }
            }
        }
        if (Constants.RIGHT.equals(side)) {
            if (evtPoint.y > leftTop.y && evtPoint.y < (leftTop.y + comp.getBounds().height)) {
                if ( Math.abs(evtPoint.x - leftTop.x) < RESIZE_BUFFER) {
                    return  true;
                }
            }
        }
        return false;
    }

    private int resize(MouseEvent event, Point dragPoint) {
        if (comp == null || side == null) {
            return 0;
        }
        Point leftTop = SwingUtilities.convertPoint(comp, 
                              new Point(0,0), SwingUtilities.getRoot(comp));
        Point evtPoint = SwingUtilities.convertPoint(event.getComponent(), 
                              event.getPoint(), SwingUtilities.getRoot(event.getComponent()));
        if (Constants.BOTTOM.equals(side)) {
            if (evtPoint.x > leftTop.x && evtPoint.x < (leftTop.x + comp.getBounds().width)) {
                return evtPoint.y - dragPoint.y;
            }
        }
        if (Constants.LEFT.equals(side) || Constants.RIGHT.equals(side)) {
            if (evtPoint.y > leftTop.y && evtPoint.y < (leftTop.y + comp.getBounds().height)) {
                return evtPoint.x - dragPoint.x;
            }
        }
        return 0;
    }
    
    public void eventDispatched(java.awt.AWTEvent aWTEvent) {
        if (comp == null || side == null) {
            state = STATE_NOOP;
            resetState();
            return;
        }
        MouseEvent evt = (MouseEvent)aWTEvent;
        if (evt.getID() == MouseEvent.MOUSE_MOVED) {
            boolean noModif = evt.getModifiersEx() == 0;
            if (noModif && isInResizeArea(evt)) {
                // make glasspane visible
                state = STATE_START;
                JRootPane pane = SwingUtilities.getRootPane(comp);
                oldGlass = pane.getGlassPane();
                glass.setCursor(side);
                pane.setGlassPane(glass);
                glass.setVisible(true);
                return;
            } else if (state != STATE_NOOP) {
                resetState();
            }
            return;
        } 
        if (evt.getID() == MouseEvent.MOUSE_PRESSED && state == STATE_START) {
            boolean button1 = (evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK;
            if (button1) {
                if (isInResizeArea(evt)) {
                    state = STATE_DRAGGING;
                    startPoint = SwingUtilities.convertPoint(evt.getComponent(), 
                                         evt.getPoint(), SwingUtilities.getRoot(evt.getComponent()));
                    evt.consume();
                    return;
                }
            }
            resetState();
            return;
        }
        if (evt.getID() == MouseEvent.MOUSE_DRAGGED && state == STATE_DRAGGING) {
            boolean button1 = (evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK;
            if (button1 && startPoint != null) {
                int delta = resize(evt, startPoint);
                if (Math.abs(delta) > 3) {
                    startPoint = SwingUtilities.convertPoint(evt.getComponent(), 
                                         evt.getPoint(), SwingUtilities.getRoot(evt.getComponent()));
                    mgr.slideResize(delta);
                }
            }
            return;
        }
    }
    
    private void resetState() {
        state = STATE_NOOP;
        JRootPane pane = SwingUtilities.getRootPane(comp);
        glass.setVisible(false);
        if (pane != null && oldGlass != null) {
            // when clicking results in hidden slide window, pne can be null?
            // how to avoid?
            pane.setGlassPane(oldGlass);
        }
        oldGlass = null;
        startPoint = null;
    }
    
    private class GlassPane extends JPanel {

        private MouseListener list = new MouseAdapter() {};
        
        public GlassPane() {
            setOpaque(false);
            putClientProperty("dontActivate", Boolean.TRUE);
            // have a listener to make the galsspane consume mouse events.
            addMouseListener(list);
        }
        
        public void setCursor(String side) {
            setCursor(Constants.BOTTOM.equals(side) ? 
                      Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR) :
                      Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        }
        
    }
}
