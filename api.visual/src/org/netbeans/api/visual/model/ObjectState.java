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

    private static final ObjectState NORMAL = new ObjectState (false, false, false, false, false, false, false);

    private boolean objectSelected;
    private boolean objectHighlighted;
    private boolean objectHovered;
    private boolean objectFocused;

    private boolean widgetHovered;
    private boolean widgetFocused;
    private boolean widgetAimed;

    private ObjectState (boolean objectSelected, boolean objectHighlighted, boolean objectHovered, boolean objectFocused, boolean widgetHovered, boolean widgetFocused, boolean widgetAimed) {
        this.objectSelected = objectSelected;
        this.objectHighlighted = objectHighlighted;
        this.objectHovered = objectHovered;
        this.objectFocused = objectFocused;
        this.widgetHovered = widgetHovered;
        this.widgetFocused = widgetFocused;
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
        return new ObjectState (selected, objectHighlighted, objectHovered, objectFocused, widgetHovered, widgetFocused, widgetAimed);
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
        return new ObjectState (objectSelected, highlighted, objectHovered, objectFocused, widgetHovered, widgetFocused, widgetAimed);
    }

    /**
     * Returns a value of hovered-flag.
     * @return true, if object-hovered or widget-hovered flag is set
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
        return new ObjectState (objectSelected, objectHighlighted, hovered, objectFocused, widgetHovered, widgetFocused, widgetAimed);
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
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, objectFocused, hovered, widgetFocused, widgetAimed);
    }

    /**
     * Returns a value of focused-flag.
     * @return true, if object-focused or widget-focused flag is set
     */
    public boolean isFocused () {
        return objectFocused  ||  widgetFocused;
    }

    /**
     * Returns a value of object-focused flag.
     * @return true, if object-focused
     */
    public boolean isObjectFocused () {
        return objectFocused;
    }

    /**
     * Creates a state derived from this one where the object-focused flag will be set according to the parameter.
     * @param focused the new object-focused-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveObjectFocused (boolean focused) {
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, focused, widgetHovered, widgetFocused, widgetAimed);
    }

    /**
     * Returns a value of widget-focused-flag.
     * @return true, if widget-focused
     */
    public boolean isWidgetFocused () {
        return widgetFocused;
    }

    /**
     * Creates a state derived from this one where the widget-focused flag will be set according to the parameter.
     * @param focused the new widget-focused-flag of the new state.
     * @return the new state
     */
    public ObjectState deriveWidgetFocused (boolean focused) {
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, objectFocused, widgetHovered, focused, widgetAimed);
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
        return new ObjectState (objectSelected, objectHighlighted, objectHovered, objectFocused, widgetHovered, widgetFocused, aimed);
    }

    /**
     * Creates a normal (initial/default) state. No flags is set in the state.
     * @return the normal state
     */
    public static ObjectState createNormal () {
        return NORMAL;
    }

}
