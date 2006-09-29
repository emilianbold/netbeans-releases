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
 * This class holds a state of a object or a widget. The object state is a set of those following flags:
 * selected, highlighted (also called secondary selection), object-hovered, widget-hovered, widget-aimed.
 * <p>
 * Initial (normal) value of object state is used for Widget.state and in ObjectScene class.
 *
 * @author David Kaspar
 */
// TODO - rename to VisualState?
public class ObjectState {

    private static final ObjectState NORMAL = new ObjectState (false, false, false, false, false);

    private boolean objectSelected;
    private boolean objectHighlighted;
    private boolean objectHovered;
    private boolean widgetHovered;
    private boolean widgetAimed;

    private ObjectState (boolean objectSelected, boolean objectHighlighted, boolean objectHovered, boolean widgetHovered, boolean widgetAimed) {
        this.objectSelected = objectSelected;
        this.objectHighlighted = objectHighlighted;
        this.objectHovered = objectHovered;
        this.widgetHovered = widgetHovered;
        this.widgetAimed = widgetAimed;
    }

    /**
     * Returns a value of selected-flag.
     * @return true, if selected
     */
    public boolean isSelected () {
        return objectSelected;
    }

    /**
     * Creates a state derived from this one where the selected flag will be set according to the parameter.
     * @param selected the new selected-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveSelected (boolean selected) {
        return new ObjectState (selected, objectHighlighted, objectHovered, widgetHovered, widgetAimed);
    }

    /**
     * Returns a value of highlighted-flag.
     * @return true, if highlighted
     */
    public boolean isHighlighted () {
        return objectHighlighted;
    }

    /**
     * Creates a state derived from this one where the highlighted flag will be set according to the parameter.
     * @param highlighted the new highlighted-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveHighlighted (boolean highlighted) {
        return new ObjectState (objectSelected, highlighted, objectHovered, widgetHovered, widgetAimed);
    }

    /**
     * Returns a value of hovered-flag.
     * @return true, if object-hovered of widget-hovered flag is set
     */
    public boolean isHovered () {
        return objectHovered  ||  widgetHovered;
    }

    /**
     * Returns a value of object-hovered-flag.
     * @return true, if object-hovered
     */
    public boolean isObjectHovered () {
        return objectHovered;
    }

    /**
     * Creates a state derived from this one where the object-hovered flag will be set according to the parameter.
     * @param hovered the new object-hovered-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveObjectHovered (boolean hovered) {
        return new ObjectState (objectSelected, objectHighlighted, hovered, widgetHovered, widgetAimed);
    }

    /**
     * Returns a value of widget-hovered-flag.
     * @return true, if widget-hovered
     */
    public boolean isWidgetHovered () {
        return widgetHovered;
    }

    /**
     * Creates a state derived from this one where the widget-hovered flag will be set according to the parameter.
     * @param hovered the new widget-hovered-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveWidgetHovered (boolean hovered) {
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, hovered, widgetAimed);
    }

    /**
     * Returns a value of widget-aimed-flag.
     * @return true, if widget-aimed
     */
    public boolean isWidgetAimed () {
        return widgetAimed;
    }

    /**
     * Creates a state derived from this one where the aimed flag will be set according to the parameter.
     * @param aimed the new aimed-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveWidgetAimed (boolean aimed) {
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, widgetHovered, aimed);
    }

    /**
     * Creates a normal (initial/default) state. No flags is set in the state.
     * @return the normal state
     */
    public static ObjectState createNormal () {
        return NORMAL;
    }

}
