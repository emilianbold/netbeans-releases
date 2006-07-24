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
package org.netbeans.api.visual.anchor;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.util.GeomUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author David Kaspar
 */
public abstract class Anchor implements Widget.Dependency {

    public static final EnumSet<Direction> DIRECTION_ANY = EnumSet.allOf (Direction.class);
    
    public enum Direction {
        LEFT, TOP, RIGHT, BOTTOM
    }

    private boolean attachedToWidget;
    private Widget relatedWidget;
    private ArrayList<Widget.Dependency> dependencies;
    
    protected Anchor (Widget relatedWidget) {
        this.relatedWidget = relatedWidget;
    }

    public void addDependency (Widget.Dependency dependency) {
        if (dependency == null)
            return;
        if (dependencies == null)
            dependencies = new ArrayList<Widget.Dependency> ();
        dependencies.add (dependency);
        if (! attachedToWidget  ||  relatedWidget != null) {
            notifyUsed ();
            attachedToWidget = true;
        }
    }

    public void removeDependency (Widget.Dependency dependency) {
        if (dependencies != null  &&  dependencies.remove (dependency)) {
            if (dependencies.size () == 0  &&  attachedToWidget) {
                notifyUnused ();
                attachedToWidget = false;
            }
        }
    }

    public boolean isUsed () {
        return attachedToWidget;
    }

    public void notifyUsed () {
        if (relatedWidget != null)
            relatedWidget.addDependency (this);
    }
    
    public void notifyUnused () {
        if (relatedWidget != null)
            relatedWidget.removeDependency (this);
    }
    
    public void revalidateDependency () {
        if (dependencies != null)
            for (Widget.Dependency dependency : dependencies)
                dependency.revalidateDependency ();
    }
    
    public Widget getRelatedWidget () {
        return relatedWidget;
    }

    public Point getRelatedSceneLocation () {
        if (relatedWidget != null)
            return GeomUtil.center (relatedWidget.convertLocalToScene (relatedWidget.getBounds ()));
        assert false : "Anchor.getRelatedSceneLocation has to be overridden when a related widget is not used";
        return null;
    }

    public Point getOppositeSceneLocation (ConnectionWidget connectionWidget, boolean isThisSourceAnchor) {
        Anchor oppositeAnchor = getOppositeAnchor (connectionWidget, isThisSourceAnchor);
        return oppositeAnchor != null ? oppositeAnchor.getRelatedSceneLocation () : null;
    }

    public Anchor getOppositeAnchor (ConnectionWidget connectionWidget, boolean isThisSourceAnchor) {
        return isThisSourceAnchor ? connectionWidget.getTargetAnchor() : connectionWidget.getSourceAnchor();
    }
    
    public abstract Result compute (ConnectionWidget connectionWidget, boolean isThisSourceAnchor);

    public final class Result {

        private Point anchorSceneLocation;
        private EnumSet<Anchor.Direction> directions;
        
        public Result (Point anchorSceneLocation, Direction direction) {
            this (anchorSceneLocation, EnumSet.of (direction));
        }
        
        public Result (Point anchorSceneLocation, EnumSet<Direction> directions) {
            this.anchorSceneLocation = anchorSceneLocation;
            this.directions = directions;
        }
        
        public Point getAnchorSceneLocation () {
            return anchorSceneLocation;
        }
        
        public EnumSet<Direction> getDirections () {
            return directions;
        }

    }
    
}
