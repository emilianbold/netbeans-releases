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
public final class PreferredBoundsAnimator extends Animator {

    private HashMap<Widget, Rectangle> sourceBounds = new HashMap<Widget, Rectangle> ();
    private HashMap<Widget, Rectangle> targetBounds = new HashMap<Widget, Rectangle> ();
    private HashMap<Widget, Boolean> nullBounds = new HashMap<Widget, Boolean> ();

    public PreferredBoundsAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setPreferredBounds (Widget widget, Rectangle preferredBounds) {
        assert widget != null;
        sourceBounds.clear ();
        boolean extra = preferredBounds == null;
        nullBounds.put (widget, extra);
        Rectangle rect = null;
        if (extra  &&  widget.isPreferredBoundsSet ()) {
            rect = widget.getPreferredBounds ();
            widget.setPreferredBounds (null);
        }
        targetBounds.put (widget, extra ? widget.getPreferredBounds () : preferredBounds);
        if (rect != null)
            widget.setPreferredBounds (rect);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Rectangle> entry : targetBounds.entrySet ()) {
            Widget widget = entry.getKey ();
            Rectangle sourceBoundary = sourceBounds.get (widget);
            if (sourceBoundary == null) {
                sourceBoundary = widget.getBounds ();
                if (sourceBoundary == null)
                    sourceBoundary = new Rectangle ();
                sourceBounds.put (widget, sourceBoundary);
            }
            Rectangle targetBoundary = entry.getValue ();
            Rectangle boundary;
            if (progress >= 1.0) {
                boundary = nullBounds.get (widget) ? null : targetBoundary;
            } else
                boundary = new Rectangle (
                        (int) (sourceBoundary.x + progress * (targetBoundary.x - sourceBoundary.x)),
                        (int) (sourceBoundary.y + progress * (targetBoundary.y - sourceBoundary.y)),
                        (int) (sourceBoundary.width + progress * (targetBoundary.width - sourceBoundary.width)),
                        (int) (sourceBoundary.height + progress * (targetBoundary.height - sourceBoundary.height)));
            widget.setPreferredBounds (boundary);
        }
        if (progress >= 1.0) {
            sourceBounds.clear ();
            targetBounds.clear ();
            nullBounds.clear ();
        }
    }

}
