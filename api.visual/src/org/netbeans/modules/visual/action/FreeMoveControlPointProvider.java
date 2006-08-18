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

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.action.MoveControlPointProvider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class FreeMoveControlPointProvider implements MoveControlPointProvider {

    public List<Point> locationSuggested (ConnectionWidget connectionWidget, int index, Point suggestedLocation) {
        List<Point> controlPoints = connectionWidget.getControlPoints ();
        if (index <= 0 || index >= controlPoints.size () - 1)
            return null;

        ArrayList<Point> list = new ArrayList<Point> (controlPoints);
        list.set (index, suggestedLocation);
        return list;
    }

}
