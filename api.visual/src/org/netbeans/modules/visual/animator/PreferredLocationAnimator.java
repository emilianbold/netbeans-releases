/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
