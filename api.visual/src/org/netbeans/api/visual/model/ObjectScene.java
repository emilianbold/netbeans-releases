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

import org.netbeans.api.visual.action.MouseHoverAction;
import org.netbeans.api.visual.action.SelectAction;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.util.*;
import java.awt.*;

/**
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

    private Object hoveredObject = null;

    private WidgetAction selectAction = new ObjectSelectAction ();
    private WidgetAction objectHoverAction;

    public void addObject (Object object, Widget widget) {
        assert object != null  &&  ! objects.containsKey (object);
        if (widget != null)
            assert ! widget2objects.containsKey (widget)  &&  widget.getScene () == this  &&  widget.getParentWidget () != null;
        objects.put (object, object);
        object2widgets.put (object, widget);
        objectStates.put (object, ObjectState.NORMAL);
        if (widget != null) {
            widget2objects.put (widget, object);
            widget.setState (ObjectState.NORMAL);
        }
    }

    public void removeObject (Object object) {
        assert object != null  &&   objects.containsKey (object);
        selectedObjects.remove (object);
        highlightedObjects.remove (object);
        if (object.equals (hoveredObject))
            hoveredObject = null;
        objectStates.remove (object);
        Widget widget = object2widgets.remove (object);
        if (widget != null) {
            widget.setState (ObjectState.NORMAL);
            widget2objects.remove (widget);
        }
        objects.remove (object);
    }

    public Set<?> getObjects () {
        return objectsUm;
    }

    public boolean isObject (Object object) {
        return objects.containsKey (object);
    }

    public Set<?> getSelectedObjects () {
        return selectedObjectsUm;
    }

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

    public Set<?> getHighlightedObjects () {
        return highlightedObjectsUm;
    }

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

    public Object getHoveredObject () {
        return hoveredObject;
    }

    public void setHoveredObject (Object hoveredObject) {
        if (hoveredObject != null) {
            if (hoveredObject.equals (this.hoveredObject))
                return;
        } else {
            if (this.hoveredObject == null)
                return;
        }
        if (this.hoveredObject != null) {
            Widget widget = object2widgets.get (this.hoveredObject);
            objectStates.put (this.hoveredObject, objectStates.get (this.hoveredObject).deriveObjectHovered (false));
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

    public WidgetAction createSelectAction () {
        return selectAction;
    }

    public WidgetAction createObjectHoverAction () {
        if (objectHoverAction == null) {
            objectHoverAction = new ObjectHoverAction ();
            getActions ().addAction (objectHoverAction);
        }
        return objectHoverAction;
    }

    public Widget findWidget (Object object) {
        assert ! (object instanceof Widget);
        return object2widgets.get (object);
    }

    public Object findObject (Widget widget) {
        while (widget != null) {
            Object o = widget2objects.get (widget);
            if (o != null)
                return o;
            widget = widget.getParentWidget ();
        }
        return null;
    }

    public Object findStoredObject (Object object) {
        assert ! (object instanceof Widget);
        return objects.get (object);
    }

    public ObjectState getObjectState (Object object) {
        return objectStates.get (object);
    }

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

    private class ObjectSelectAction extends SelectAction {
        public void doSelect (Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject (widget);

            if (object != null) {
                if (getSelectedObjects ().contains (object))
                    return;
                userSelectionSuggested (Collections.singleton (object), invertSelection);
            } else
                userSelectionSuggested (Collections.emptySet (), invertSelection);
        }
    }

    private class ObjectHoverAction extends MouseHoverAction {

        protected void widgetHovered (Widget widget) {
            if (ObjectScene.this == widget)
                widget = null;
            setHoveredObject (findObject (widget));
        }

    }

}
