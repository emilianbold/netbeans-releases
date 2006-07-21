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
    private boolean objectSelected;
    private boolean objectHighlighted;
    private boolean objectHovered;
    private boolean widgetHovered;

    public ObjectState () {
    }

    public ObjectState (boolean objectSelected, boolean objectHighlighted, boolean objectHovered, boolean widgetHovered) {
        this.objectSelected = objectSelected;
        this.objectHighlighted = objectHighlighted;
        this.objectHovered = objectHovered;
        this.widgetHovered = widgetHovered;
    }

    public boolean isSelected () {
        return objectSelected;
    }

    public ObjectState deriveSelected (boolean selected) {
        return new ObjectState (selected, objectHighlighted, objectHovered, widgetHovered);
    }

    public boolean isHighlighted () {
        return objectHighlighted;
    }

    public ObjectState deriveHighlighted (boolean highlighted) {
        return new ObjectState (objectSelected, highlighted, objectHovered, widgetHovered);
    }

    public boolean isHovered () {
        return objectHovered  ||  widgetHovered;
    }

    public boolean isObjectHovered () {
        return objectHovered;
    }

    public ObjectState deriveObjectHovered (boolean hovered) {
        return new ObjectState (objectSelected, objectHighlighted, hovered, widgetHovered);
    }

    public boolean isWidgetHovered () {
        return widgetHovered;
    }

    public ObjectState deriveWidgetHovered (boolean hovered) {
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, hovered);
    }

}
