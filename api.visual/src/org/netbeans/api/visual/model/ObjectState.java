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
package org.netbeans.api.visual.model;

/**
 * @author David Kaspar
 */
public class ObjectState {

    public static final ObjectState NORMAL = new ObjectState ();

    private boolean selected;
    private boolean highlighted;
    private boolean focused;
    private boolean hovered;

    public ObjectState () {
    }

    public ObjectState (boolean selected, boolean highlighted, boolean focused, boolean hovered) {
        this.selected = selected;
        this.highlighted = highlighted;
        this.focused = focused;
        this.hovered = hovered;
    }

    public boolean isSelected () {
        return selected;
    }

    public ObjectState deriveSelected (boolean selected) {
        return new ObjectState (selected, highlighted, focused, hovered);
    }

    public boolean isHighlighted () {
        return highlighted;
    }

    public ObjectState deriveHighlighted (boolean highlighted) {
        return new ObjectState (selected, highlighted, focused, hovered);
    }

    public boolean isFocused () {
        return focused;
    }

    public ObjectState deriveFocused (boolean focused) {
        return new ObjectState (selected, highlighted, focused, hovered);
    }

    public boolean isHovered () {
        return hovered;
    }

    public ObjectState deriveHovered (boolean hovered) {
        return new ObjectState (selected, highlighted, focused, hovered);
    }

}
