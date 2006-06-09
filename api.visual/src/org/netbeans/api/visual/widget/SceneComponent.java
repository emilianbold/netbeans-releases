/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.action.WidgetAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * @author David Kaspar
 */
final class SceneComponent extends JPanel implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener,FocusListener {

    private Scene scene;
    private Widget lockedWidget;
    private WidgetAction lockedAction;
    private long eventIDcounter = 0;

    public SceneComponent (Scene scene) {
        this.scene = scene;
        setOpaque (false);
        setDoubleBuffered (true);
        addMouseListener (this);
        addMouseMotionListener (this);
        addMouseWheelListener (this);
        addKeyListener (this);
    }

    public void addNotify () {
        super.addNotify ();
        scene.setGraphics ((Graphics2D) getGraphics ());
        scene.validate ();
    }

    public void paint (Graphics g) {
//        long s = System.currentTimeMillis ();
        Graphics2D gr = (Graphics2D) g;

        super.paint (g);
//        gr.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        gr.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        scene.setGraphics (gr);

        AffineTransform previousTransform = gr.getTransform ();
        double zoomFactor = scene.getZoomFactor ();
        gr.scale (zoomFactor, zoomFactor);
        scene.paint ();
        gr.setTransform (previousTransform);

//        System.out.println ("PAINT Time: " + (System.currentTimeMillis () - s));
    }
    
    public void focusGained(FocusEvent e) {
        processFocusOperator (FocusOperator.FOCUS_GAINED, e);
    }

    public void focusLost(FocusEvent e) {
        processFocusOperator (FocusOperator.FOCUS_LOST, e);
    }
    
    public void mouseClicked (MouseEvent e) {
        processMouseOperator (MouseOperator.MOUSE_CLICKED, e);
    }

    public void mousePressed (MouseEvent e) {
        processMouseOperator (MouseOperator.MOUSE_PRESSED, e);
    }

    public void mouseReleased (MouseEvent e) {
        processMouseOperator (MouseOperator.MOUSE_RELEASED, e);
    }

    public void mouseEntered (MouseEvent e) {
        processMouseOperator (MouseOperator.MOUSE_ENTERED, e);
    }

    public void mouseExited (MouseEvent e) {
        processMouseOperator (MouseOperator.MOUSE_EXITED, e);
    }

    public void mouseDragged (MouseEvent e) {
        processMouseOperator (MouseOperator.MOUSE_DRAGGED, e);
    }

    public void mouseMoved (MouseEvent e) {
        MouseContext context = new MouseContext ();
        resolveContext (scene, scene.convertViewToScene (e.getPoint ()), context);
        context.commit (this);
        processMouseOperator (MouseOperator.MOUSE_MOVED, e);
    }

    public void mouseWheelMoved (MouseWheelEvent e) {
        WidgetAction.WidgetMouseWheelEvent event = new WidgetAction.WidgetMouseWheelEvent (++ eventIDcounter, e);
        processMouseOperator (MouseOperator.MOUSE_WHEEL, event);
    }

    public void keyTyped (KeyEvent e) {
        WidgetAction.WidgetKeyEvent event = new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e);
        processKeyOperator (KeyOperator.KEY_TYPED, event);
    }

    public void keyPressed (KeyEvent e) {
        WidgetAction.WidgetKeyEvent event = new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e);
        processKeyOperator (KeyOperator.KEY_PRESSED, event);
    }

    public void keyReleased (KeyEvent e) {
        WidgetAction.WidgetKeyEvent event = new WidgetAction.WidgetKeyEvent (++ eventIDcounter, e);
        processKeyOperator (KeyOperator.KEY_RELEASED, event);
    }

    private void processMouseOperator (MouseOperator operator, MouseEvent e) {
        WidgetAction.WidgetMouseEvent event = new WidgetAction.WidgetMouseEvent (++ eventIDcounter, e);
        processMouseOperator (operator, event);
    }

    private void processMouseOperator (MouseOperator operator, WidgetAction.WidgetMouseEvent event) {
        event.setPoint (scene.convertViewToScene (event.getPoint ()));

        WidgetAction.State state;

        if (lockedAction != null) {
            Point location = lockedWidget.convertSceneToLocal (new Point ());
            event.translatePoint (location.x, location.y);
            state = operator.operate (lockedAction, lockedWidget, event);
            event.translatePoint (- location.x, - location.y);

            if (! state.isConsumed ())
                state = processMouseOperator (operator, scene, event);
        } else
            state = processMouseOperator (operator, scene, event);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));
    }

    private WidgetAction.State processMouseOperator (MouseOperator operator, Widget widget, WidgetAction.WidgetMouseEvent event) {
        Point location = widget.getLocation ();
        event.translatePoint (- location.x, - location.y);

        if (widget.getBounds ().contains (event.getPoint ())) {
            WidgetAction.State state;

            List<Widget> children = widget.getChildren ();
            Widget[] childrenArray = children.toArray (new Widget[children.size ()]);

            for (int i = childrenArray.length - 1; i >= 0; i --) {
                Widget child = childrenArray[i];
                state = processMouseOperator (operator, child, event);
                if (state.isConsumed ())
                    return state;
            }

            if (widget.isHitAt (event.getPoint ())) {
                state = operator.operate (widget.getActions (), widget, event);
                if (state.isConsumed ())
                    return state;
            }
        }

        event.translatePoint (location.x, location.y);
        return WidgetAction.State.REJECTED;
    }

    private void processKeyOperator (KeyOperator operator, WidgetAction.WidgetKeyEvent event) {
        WidgetAction.State state;

        if (lockedAction != null) {
            state = operator.operate (lockedAction, lockedWidget, event);
            if (! state.isConsumed ())
                state = processKeyOperator (operator, scene, event);
        } else
            state = processKeyOperator (operator, scene, event);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));
    }

    private WidgetAction.State processKeyOperator (KeyOperator operator, Widget widget, WidgetAction.WidgetKeyEvent event) {
        WidgetAction.State state;

        List<Widget> children = widget.getChildren ();
        Widget[] childrenArray = children.toArray (new Widget[children.size ()]);

        for (int i = childrenArray.length - 1; i >= 0; i --) {
            Widget child = childrenArray[i];
            state = processKeyOperator (operator, child, event);
            if (state.isConsumed ())
                return state;
        }

        state = operator.operate (widget.getActions (), widget, event);
        if (state.isConsumed ())
            return state;

        return WidgetAction.State.REJECTED;
    }

    private boolean resolveContext (Widget widget, Point point, MouseContext context) {
        Point location = widget.getLocation ();
        point.translate (- location.x, - location.y);

        if (widget.getBounds ().contains (point)) {
            List<Widget> children = widget.getChildren ();
            for (int i = children.size () - 1; i >= 0; i --) {
                Widget child = children.get (i);
                if (resolveContext (child, point, context)) {
                    point.translate (location.x, location.y);
                    return true;
                }
            }
            if (widget.isHitAt (point))
                context.update (widget);
        }

        point.translate (location.x, location.y);
        return false;
    }

    
    private void processFocusOperator (SceneComponent.FocusOperator operator, FocusEvent e) {
        WidgetAction.WidgetFocusEvent event = new WidgetAction.WidgetFocusEvent (++ eventIDcounter, e);
        processFocusOperator (operator, event);
    }

    private void processFocusOperator (SceneComponent.FocusOperator operator,  WidgetAction.WidgetFocusEvent e) {
        WidgetAction.State state;

        if (lockedAction != null) {
            state = operator.operate (lockedAction, lockedWidget, e);
            if (! state.isConsumed ())state = processFocusOperator (operator, scene, e);
        } else
            state = processFocusOperator (operator, scene, e);

        lockedWidget = state.getLockedWidget ();
        lockedAction = state.getLockedAction ();
        scene.validate ();

        if (lockedWidget != null)
            scrollRectToVisible (scene.convertSceneToView (lockedWidget.convertLocalToScene (lockedWidget.getBounds ())));
    }

    private WidgetAction.State processFocusOperator (SceneComponent.FocusOperator operator, Widget widget, WidgetAction.WidgetFocusEvent event) {
        WidgetAction.State state;

        List<Widget> children = widget.getChildren ();
        Widget[] childrenArray = children.toArray (new Widget[children.size ()]);

        for (int i = childrenArray.length - 1; i >= 0; i --) {
            Widget child = childrenArray[i];
            state = processFocusOperator (operator, child, event);
            if (state.isConsumed ())
                return state;
        }

        state = operator.operate (widget.getActions (), widget, event);
        if (state.isConsumed ())
            return state;

        return WidgetAction.State.REJECTED;
    }

    
    private interface MouseOperator {

        public static final MouseOperator MOUSE_CLICKED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseClicked (widget, event);
            }
        };

        public static final MouseOperator MOUSE_PRESSED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mousePressed (widget, event);
            }
        };

        public static final MouseOperator MOUSE_RELEASED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseReleased (widget, event);
            }
        };

        public static final MouseOperator MOUSE_ENTERED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseEntered (widget, event);
            }
        };

        public static final MouseOperator MOUSE_EXITED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseExited (widget, event);
            }
        };

        public static final MouseOperator MOUSE_DRAGGED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseDragged (widget, event);
            }
        };

        public static final MouseOperator MOUSE_MOVED = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseMoved (widget, event);
            }
        };

        public static final MouseOperator MOUSE_WHEEL = new MouseOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event) {
                return action.mouseWheelMoved (widget, (WidgetAction.WidgetMouseWheelEvent) event);
            }
        };

        public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetMouseEvent event);

    }

    private interface KeyOperator {

        public static final KeyOperator KEY_TYPED = new KeyOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetKeyEvent event) {
                return action.keyTyped (widget, event);
            }
        };

        public static final KeyOperator KEY_PRESSED = new KeyOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetKeyEvent event) {
                return action.keyPressed (widget, event);
            }
        };

        public static final KeyOperator KEY_RELEASED = new KeyOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetKeyEvent event) {
                return action.keyReleased (widget, event);
            }
        };

        public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetKeyEvent event);

    }
    
    private interface FocusOperator {

        public static final FocusOperator FOCUS_GAINED = new FocusOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetFocusEvent event) {
                return action.focusGained (widget, event);
            }
        };

        public static final FocusOperator FOCUS_LOST = new FocusOperator() {
            public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetFocusEvent event) {
                return action.focusLost (widget, event);
            }
        };

        public WidgetAction.State operate (WidgetAction action, Widget widget, WidgetAction.WidgetFocusEvent event);

    }

    private static final class MouseContext {

        private String toolTipText;

        private Cursor cursor;

        public boolean update (Widget widget) {
            if (cursor == null)
                cursor = widget.getCursor ();
            if (toolTipText == null)
                toolTipText = widget.getToolTipText ();
            return cursor == null  ||  toolTipText == null;
        }

        public void commit (SceneComponent component) {
            component.setToolTipText (toolTipText);
            component.setCursor (cursor);
        }

    }

}
