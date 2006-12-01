package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * This interface is used by ResizeAction to resolve if and which control point is being dragged for particular resizing.
 *
 * @author David Kaspar
 */
public interface ResizeControlPointResolver {

    /**
     * Resolves which control point is being dragged by user.
     * @param widget the widget where the user is invoking the resizing
     * @param point the mouse cursor location in local coordination system of the widget
     * @return the control point; if null, then resizing action is denied
     */
    ResizeProvider.ControlPoint resolveControlPoint (Widget widget, Point point);

}
