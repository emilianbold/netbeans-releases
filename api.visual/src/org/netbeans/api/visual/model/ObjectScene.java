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

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.util.*;
import java.awt.*;

/**
 * This class manages mapping between model-objects and widgets on a scene. Object mapping is added/removed using addObject and removeObject methods.
 * You can query the mapping using the findWidget(Object) and the findObject(Widget) methods.
 * <p>
 * It also manages object-oriented states and creates a object-specific action that could be assigned to widgets to provide
 * functionality like object-based selection, object-based hovering, ...
 *
 * @author David Kaspar
 */
public class ObjectScene extends Scene {

    private HashMap<Object, Object> objects = new HashMap<Object, Object> ();
    private Set<Object> objectsUm = Collections.unmodifiableSet (objects.keySet ());

    private HashMap<Object, Widget> object2widgets = new HashMap<Object, Widget> ();
    private HashMap<Widget, Object> widget2objects = new HashMap<Widget, Object> ();

    private HashMap<Object, ObjectState> objectStates = new HashMap<Object, ObjectState> ();

    private HashSet<Object> selectedObjects = new HashSet<Object> ();
    private Set<Object> selectedObjectsUm = Collections.unmodifiableSet (selectedObjects);

    private HashSet<Object> highlightedObjects = new HashSet<Object> ();
    private Set<Object> highlightedObjectsUm = Collections.unmodifiableSet (highlightedObjects);

    private Object focusedObject = null;
    private Object hoveredObject = null;

    private WidgetAction selectAction = ActionFactory.createSelectAction (new ObjectSelectProvider ());
    private WidgetAction objectHoverAction;

    /**
     * Adds a mapping between an object and a widget.
     * @param object the model object; the object must not be a Widget
     * @param widget the scene widget; if null then the object is non-visual and does not have any widget assigned
     */
    public final void addObject (Object object, Widget widget) {
        assert object != null  &&  ! (object instanceof Widget)  &&  ! objects.containsKey (object);
        if (widget != null)
            assert ! widget2objects.containsKey (widget)  &&  widget.getScene () == this  &&  widget.getParentWidget () != null;
        objects.put (object, object);
        object2widgets.put (object, widget);
        objectStates.put (object, ObjectState.createNormal ());
        if (widget != null) {
            widget2objects.put (widget, object);
            widget.setState (ObjectState.createNormal ());
        }
    }

    /**
     * Removes a mapping for an object.
     * @param object the object for which the mapping is removed
     */
    public final void removeObject (Object object) {
        assert object != null  &&   objects.containsKey (object);
        selectedObjects.remove (object);
        highlightedObjects.remove (object);
        if (object.equals (hoveredObject))
            hoveredObject = null;
        objectStates.remove (object);
        Widget widget = object2widgets.remove (object);
        if (widget != null) {
            widget.setState (ObjectState.createNormal ());
            widget2objects.remove (widget);
        }
        objects.remove (object);
    }

    /**
     * Returns a set of objects with registered mapping.
     * @return the set of register objects
     */
    public final Set<?> getObjects () {
        return objectsUm;
    }

    /**
     * Returns whether a specified object is registered.
     * @param object the object to be checked
     * @return true if the object is register; false if the object is not registered
     */
    public final boolean isObject (Object object) {
        return objects.containsKey (object);
    }

    /**
     * Returns a set of selected objects.
     * @return the set of selected objects
     */
    public final Set<?> getSelectedObjects () {
        return selectedObjectsUm;
    }

    /**
     * Sets a set of selected objects.
     * @param selectedObjects the set of selected objects
     */
    // TODO - could be final?
    public void setSelectedObjects (Set<?> selectedObjects) {
        for (Iterator<Object> iterator = this.selectedObjects.iterator (); iterator.hasNext ();) {
            Object object = iterator.next ();
            if (! selectedObjects.contains (object)) {
                iterator.remove ();
                objectStates.put (object, objectStates.get (object).deriveSelected (false));
                Widget widget = object2widgets.get (object);
                if (widget != null)
                    widget.setState (widget.getState ().deriveSelected (false));
            }
        }
        for (Object object : selectedObjects) {
            if (! this.selectedObjects.contains (object)) {
                this.selectedObjects.add (object);
                objectStates.put (object, objectStates.get (object).deriveSelected (true));
                Widget widget = object2widgets.get (object);
                if (widget != null)
                    widget.setState (widget.getState ().deriveSelected (true));
            }
        }
    }

    /**
     * Returns a set of highlighted objects.
     * @return the set of highlighted objects
     */
    public final Set<?> getHighlightedObjects () {
        return highlightedObjectsUm;
    }

    /**
     * Sets a set of highlighted objects.
     * @param highlightedObjects the set of highlighted objects
     */
    // TODO - could be final?
    public void setHighlightedObjects (Set<?> highlightedObjects) {
        for (Iterator<Object> iterator = this.highlightedObjects.iterator (); iterator.hasNext ();) {
            Object object = iterator.next ();
            if (! highlightedObjects.contains (object)) {
                iterator.remove ();
                objectStates.put (object, objectStates.get (object).deriveHighlighted (false));
                Widget widget = object2widgets.get (object);
                if (widget != null)
                    widget.setState (widget.getState ().deriveHighlighted (false));
            }
        }
        for (Object object : highlightedObjects) {
            if (! this.highlightedObjects.contains (object)) {
                this.highlightedObjects.add (object);
                objectStates.put (object, objectStates.get (object).deriveHighlighted (true));
                Widget widget = object2widgets.get (object);
                if (widget != null)
                    widget.setState (widget.getState ().deriveHighlighted (true));
            }
        }
    }

    /**
     * Returns a hovered object. There could be only one hovered object at maximum at the same time.
     * @return the hovered object; null if no object is hovered
     */
    public final Object getHoveredObject () {
        return hoveredObject;
    }

    /**
     * Sets a hovered object.
     * @param hoveredObject the hovered object; if null, then the scene does not have hovered object
     */
    // TODO - could be final?
    public void setHoveredObject (Object hoveredObject) {
        if (hoveredObject != null) {
            if (hoveredObject.equals (this.hoveredObject))
                return;
        } else {
            if (this.hoveredObject == null)
                return;
        }
        if (this.hoveredObject != null) {
            objectStates.put (this.hoveredObject, objectStates.get (this.hoveredObject).deriveObjectHovered (false));
            Widget widget = object2widgets.get (this.hoveredObject);
            if (widget != null)
                widget.setState (widget.getState ().deriveObjectHovered (false));
        }
        this.hoveredObject = hoveredObject;
        if (this.hoveredObject != null) {
            objectStates.put (this.hoveredObject, objectStates.get (this.hoveredObject).deriveObjectHovered (true));
            Widget widget = object2widgets.get (this.hoveredObject);
            if (widget != null)
                widget.setState (widget.getState ().deriveObjectHovered (true));
        }
    }

    /**
     * Returns a focused object. There could be only one focused object at maximum at the same time.
     * @return the focused object; null if no object is focused
     */
    public final Object getFocusedObject () {
        return hoveredObject;
    }

    /**
     * Sets a focused object.
     * @param focusedObject the focused object; if null, then the scene does not have focused object
     */
    public final void setFocusedObject (Object focusedObject) {
        if (focusedObject != null) {
            if (focusedObject.equals (this.focusedObject))
                return;
        } else {
            if (this.focusedObject == null)
                return;
        }
        if (this.focusedObject != null) {
            objectStates.put (this.focusedObject, objectStates.get (this.focusedObject).deriveObjectFocused (false));
            Widget widget = object2widgets.get (this.focusedObject);
            if (widget != null)
                widget.setState (widget.getState ().deriveObjectFocused (false));
        }
        this.focusedObject = focusedObject;
        if (this.focusedObject != null) {
            objectStates.put (this.focusedObject, objectStates.get (this.focusedObject).deriveObjectFocused (true));
            Widget widget = object2widgets.get (this.focusedObject);
            if (widget != null)
                widget.setState (widget.getState ().deriveObjectFocused (true));
        }
    }

    /**
     * Creates a object-oriented select action.
     * @return the object-oriented select action
     */
    public final WidgetAction createSelectAction () {
        return selectAction;
    }

    /**
     * Returns a object-oriented hover action.
     * @return the object-oriented hover action
     */
    public final WidgetAction createObjectHoverAction () {
        if (objectHoverAction == null) {
            objectHoverAction = ActionFactory.createHoverAction (new ObjectHoverProvider ());
            getActions ().addAction (objectHoverAction);
        }
        return objectHoverAction;
    }

    /**
     * Returns the widget that is mapped to a specified object.
     * @param object the object; must not be a Widget
     * @return the widget from the registered mapping; null if object is non-visual or no mapping is registered
     */
    public final Widget findWidget (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return object2widgets.get (object);
    }

    /**
     * Returns an object which is assigned to a widget.
     * If the widget is not mapped to any object then the method recursively searches for an object of the parent widget.
     * @param widget the widget
     * @return the mapped object; null if no object is assigned to a widget or any of its parent widgets
     */
    public final Object findObject (Widget widget) {
        while (widget != null) {
            Object o = widget2objects.get (widget);
            if (o != null)
                return o;
            widget = widget.getParentWidget ();
        }
        return null;
    }

    /**
     * Returns an instance of stored object.
     * It searches for an instance of an object stored internally in the class using "equals" method on an object.
     * @param object the object that is equals (observed by calling the "equals" method on the instances stored in the class);
     *           the object must not be a Widget
     * @return the stored instance of the object
     */
    public final Object findStoredObject (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return objects.get (object);
    }

    /**
     * Returns an object-state of a specified object.
     * @param object the object
     * @return the object-state of the specified object; null if the object is not registered
     */
    public final ObjectState getObjectState (Object object) {
        return objectStates.get (object);
    }

    /**
     * Set by actions for setting selected objects invoked by an user.
     * @param suggestedSelectedObjects the selected objects suggested by an user
     * @param invertSelection the invert selection is specified by an user
     */
    public void userSelectionSuggested (Set<?> suggestedSelectedObjects, boolean invertSelection) {
        if (invertSelection) {
            HashSet<Object> objects = new HashSet<Object> (getSelectedObjects ());
            for (Object o : suggestedSelectedObjects) {
                if (objects.contains (o))
                    objects.remove (o);
                else
                    objects.add (o);
            }
            setSelectedObjects (objects);
        } else {
            setSelectedObjects (suggestedSelectedObjects);
        }
    }

    private class ObjectSelectProvider implements SelectProvider {

        public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject (widget);
            return object != null  &&  (invertSelection  ||  ! getSelectedObjects ().contains (object));
        }

        public void select (Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject (widget);

            setFocusedObject (object);
            if (object != null) {
                if (getSelectedObjects ().contains (object))
                    return;
                userSelectionSuggested (Collections.singleton (object), invertSelection);
            } else
                userSelectionSuggested (Collections.emptySet (), invertSelection);
        }
    }

    private class ObjectHoverProvider implements HoverProvider {

        public void widgetHovered (Widget widget) {
            if (ObjectScene.this == widget)
                widget = null;
            setHoveredObject (findObject (widget));
        }

    }

}
