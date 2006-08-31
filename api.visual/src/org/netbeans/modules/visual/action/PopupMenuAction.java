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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.PopupMenuProvider;

import javax.swing.*;
import java.awt.*;

/**
 * @author William Headrick, David Kaspar
 */
public final class PopupMenuAction extends WidgetAction.Adapter {

    private PopupMenuProvider provider;

    public PopupMenuAction (PopupMenuProvider provider) {
        this.provider = provider;
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  Delegates check code
     * to {@link #handleMouseEvent(Widget, WidgetMouseEvent)}.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #handleMouseEvent(Widget, WidgetMouseEvent)
     */
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  Delegates check code
     * to {@link #handleMouseEvent(Widget, WidgetMouseEvent)}.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #handleMouseEvent(Widget, WidgetMouseEvent)
     */
    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  This method is called
     * by both {@link #mousePressed(Widget, WidgetMouseEvent)} and
     * {@link #mouseReleased(Widget, WidgetMouseEvent)} methods to handle
     * displaying a popup menu for the given widget and event.  Uses
     * {@link WidgetMouseEvent#isPopupTrigger() event.isPopupTrigger()} to
     * determine whether or not a popup menu should be displayed.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #mousePressed(Widget, WidgetMouseEvent)
     * @see #mouseReleased(Widget, WidgetMouseEvent)
     */
    protected State handleMouseEvent (Widget widget, WidgetMouseEvent event) {
        // Different OSes use different MouseEvents (Pressed/Released) to
        // signal that an event is a PopupTrigger.  So, the mousePressed(...)
        // and mouseReleased(...) methods delegate to this method to
        // handle the MouseEvent.
        if (event.isPopupTrigger ()) {
            JPopupMenu popupMenu = provider.getPopupMenu (widget);
            if (popupMenu != null) {
                Scene scene = widget.getScene ();
                Point point = scene.convertSceneToView (widget.convertLocalToScene (event.getPoint ()));
                popupMenu.show (scene.getComponent (), point.x, point.y);
                return State.CONSUMED;
            }
        }
        return State.REJECTED;
    }

}
