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

import org.netbeans.api.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;
import java.util.EnumSet;
import java.util.Collections;
import java.util.ArrayList;

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
    private ArrayList<Entry> entries = new ArrayList<Entry> ();

    protected Anchor (Widget relatedWidget) {
        this.relatedWidget = relatedWidget;
    }

    public final void addEntry (Anchor.Entry entry) {
        if (entry == null)
            return;
        notifyEntryAdded (entry);
        entries.add (entry);
        if (! attachedToWidget  &&  entries.size () > 0) {
            attachedToWidget = true;
            if (relatedWidget != null)
                relatedWidget.addDependency (this);
            notifyUsed ();
        }
        revalidateDependency ();
    }

    public void removeEntry (Entry entry) {
        entries.remove (entry);
        if (attachedToWidget  &&  entries.size () <= 0) {
            attachedToWidget = false;
            if (relatedWidget != null)
                relatedWidget.removeDependency (this);
            notifyUnused ();
        }
        revalidateDependency ();
    }

    public void addEntries (List<Entry> entries) {
        for (Entry entry : entries)
            addEntry (entry);
    }

    public void removeEntries (List<Entry> entries) {
        for (Entry entry : entries)
            removeEntry (entry);
    }

    public List<Entry> getEntries () {
        return Collections.unmodifiableList (entries);
    }

    protected boolean isUsed () {
        return attachedToWidget;
    }

    protected void notifyEntryAdded (Entry entry) {
    }

    protected void notifyEntryRemoved (Entry entry) {
    }

    protected void notifyUsed () {
    }

    protected void notifyUnused () {
    }

    public void revalidateDependency () {
        for (Entry entry : entries)
            entry.revalidateEntry ();
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

    public Point getOppositeSceneLocation (Entry entry) {
        Anchor oppositeAnchor = entry.getOppositeAnchor ();
        return oppositeAnchor != null ? oppositeAnchor.getRelatedSceneLocation () : null;
    }

    public abstract Result compute (Entry entry);

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

    public interface Entry {

        void revalidateEntry ();

        ConnectionWidget getAttachedConnectionWidget ();

        boolean isAttachedToConnectionSource ();

        Anchor getAttachedAnchor ();

        Anchor getOppositeAnchor ();

    }

}
