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
        if (dependencies != null)
            if (! dependencies.remove (dependency))
                return;
        if (dependencies.size () == 0  &&  attachedToWidget) {
            notifyUnused ();
            attachedToWidget = false;
        }
    }
    
    public void notifyUsed () {
        relatedWidget.addDependency (this);
    }
    
    public void notifyUnused () {
        relatedWidget.removeDependency (this);
    }
    
    public void revalidate () {
        if (dependencies != null)
            for (Widget.Dependency dependency : dependencies)
                dependency.revalidate();
    }
    
    public Widget getRelatedWidget () {
        return relatedWidget;
    }
    
    public Widget getOppositeWidget (ConnectionWidget connectionWidget, boolean isThisSourceAnchor) {
        Anchor oppositeAnchor = isThisSourceAnchor ? connectionWidget.getTargetAnchor() : connectionWidget.getSourceAnchor();
        return oppositeAnchor != null ? oppositeAnchor.getRelatedWidget() : null;
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
