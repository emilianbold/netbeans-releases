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
package org.netbeans.modules.visual.animator;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class PreferredLocationAnimator extends Animator {

    private HashMap<Widget, Point> sourceLocations = new HashMap<Widget, Point> ();
    private HashMap<Widget, Point> targetLocations = new HashMap<Widget, Point> ();

    public PreferredLocationAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setPreferredLocation (Widget widget, Point preferredLocation) {
        assert widget != null;
        assert preferredLocation != null;
        sourceLocations.clear ();
        targetLocations.put (widget, preferredLocation);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Point> entry : targetLocations.entrySet ()) {
            Widget widget = entry.getKey ();
            Point sourceLocation = sourceLocations.get (widget);
            if (sourceLocation == null) {
                sourceLocation = widget.getPreferredLocation ();
                if (sourceLocation == null) {
                    sourceLocation = widget.getLocation ();
                    if (sourceLocation == null) {
                        sourceLocation = new Point ();
                    }
                }
                sourceLocations.put (widget, sourceLocation);
            }
            Point targetLocation = entry.getValue ();
            if (targetLocation == null)
                continue;
            Point point;
            if (progress >= 1.0)
                point = targetLocation;
            else
                point = new Point (
                        (int) (sourceLocation.x + progress * (targetLocation.x - sourceLocation.x)),
                        (int) (sourceLocation.y + progress * (targetLocation.y - sourceLocation.y)));
            widget.setPreferredLocation (point);
        }
        if (progress >= 1.0) {
            sourceLocations.clear ();
            targetLocations.clear ();
        }
    }

}
