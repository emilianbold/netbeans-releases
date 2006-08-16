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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class FixedAnchor extends Anchor {

    private Point location;

    public FixedAnchor (Point location) {
        super (null);
        this.location = location;
    }

    public Point getRelatedSceneLocation () {
        return location;
    }

    public Result compute (Entry entry) {
        return new Result (location, Anchor.DIRECTION_ANY);
    }

}
