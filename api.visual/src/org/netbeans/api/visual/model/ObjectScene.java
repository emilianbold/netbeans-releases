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
import org.netbeans.api.visual.action.MoveAction;
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

    private HashSet<ObjectController> objects = new HashSet<ObjectController> ();
    private HashMap<Widget, ObjectController> widgets2controllers = new HashMap<Widget, ObjectController> ();
    private Set<ObjectController> objectsUm = Collections.unmodifiableSet (objects);
    private HashSet<ObjectController> selectedObjects = new HashSet<ObjectController> ();
    private Set<ObjectController> selectedObjectsUm = Collections.unmodifiableSet (selectedObjects);
    private HashSet<ObjectController> highlightedObjects = new HashSet<ObjectController> ();
    private Set<ObjectController> highlightedObjectsUm = Collections.unmodifiableSet (highlightedObjects);
    private ObjectController focusedObject = null;
    private ObjectController hoveredObject = null;

    private WidgetAction selectAction = new ObjectSelectAction ();
    private WidgetAction hoverAction;
    private WidgetAction moveAction = new MoveAction ();

    public void addObject (ObjectController objectController) {
        if (objectController == null)
            return;
        objects.add (objectController);
        for (Widget widget : objectController.getWidgets ()) {
            assert widget.getScene () == this  &&  widget.getParentWidget () != null;
            ObjectController oldValue = widgets2controllers.put (widget, objectController);
            assert oldValue == null;
        }
    }

    public void removeObject (ObjectController objectController) {
        if (objectController == null)
            return;
        selectedObjects.remove (objectController);
        highlightedObjects.remove (objectController);
        if (objectController.equals (focusedObject))
            focusedObject = null;
        if (objectController.equals (hoveredObject))
            hoveredObject = null;
        objectController.setState (ObjectState.NORMAL);
        objects.remove (objectController);
        for (Widget widget : objectController.getWidgets ())
            widgets2controllers.remove (widget);
    }

    public Set<ObjectController> getObjects () {
        return objectsUm;
    }

    public Set<ObjectController> getSelectedObjects () {
        return selectedObjectsUm;
    }

    public void setSelectedObjects (Set<ObjectController> selectedObjects) {
        for (Iterator<ObjectController> iterator = this.selectedObjects.iterator (); iterator.hasNext ();) {
            ObjectController controller = iterator.next ();
            if (! selectedObjects.contains (controller)) {
                iterator.remove ();
                controller.setState (controller.getState ().deriveSelected (false));
            }
        }
        for (ObjectController controller : selectedObjects) {
            if (! this.selectedObjects.contains (controller)) {
                this.selectedObjects.add (controller);
                controller.setState (controller.getState ().deriveSelected (true));
            }
        }
    }

    public Set<ObjectController> getHighlightedObjects () {
        return highlightedObjectsUm;
    }

    public void setHighlightedObjects (Set<ObjectController> highlightedObjects) {
        for (Iterator<ObjectController> iterator = this.highlightedObjects.iterator (); iterator.hasNext ();) {
            ObjectController controller = iterator.next ();
            if (! highlightedObjects.contains (controller)) {
                iterator.remove ();
                controller.setState (controller.getState ().deriveHighlighted (false));
            }
        }
        for (ObjectController controller : highlightedObjects) {
            if (! this.highlightedObjects.contains (controller)) {
                this.highlightedObjects.add (controller);
                controller.setState (controller.getState ().deriveHighlighted (true));
            }
        }
    }

    public ObjectController getFocusedObject () {
        return focusedObject;
    }

    public void setFocusedObject (ObjectController focusedObject) {
        if (focusedObject != null) {
            if (this.focusedObject.equals (focusedObject))
               return;
        } else {
            if (this.focusedObject == null)
                return;
        }
        if (this.focusedObject != null)
            this.focusedObject.setState (this.focusedObject.getState ().deriveFocused (false));
        this.focusedObject = focusedObject;
        if (this.focusedObject != null)
            this.focusedObject.setState (this.focusedObject.getState ().deriveFocused (true));
    }

    public ObjectController getHoveredObject () {
        return hoveredObject;
    }

    public void setHoveredObject (ObjectController hoveredObject) {
        if (hoveredObject != null) {
            if (hoveredObject.equals (this.hoveredObject))
                return;
        } else {
            if (this.hoveredObject == null)
                return;
        }
        if (this.hoveredObject != null)
            this.hoveredObject.setState (this.hoveredObject.getState ().deriveHovered (false));
        this.hoveredObject = hoveredObject;
        if (this.hoveredObject != null)
            this.hoveredObject.setState (this.hoveredObject.getState ().deriveHovered (true));
    }

    public WidgetAction createSelectAction () {
        return selectAction;
    }

    public WidgetAction createHoverAction () {
        if (hoverAction == null) {
            hoverAction = new ObjectHoverAction ();
            getActions ().addAction (hoverAction);
        }
        return hoverAction;
    }

    public WidgetAction createMoveAction () {
        return moveAction;
    }

    public ObjectController findObjectController (Widget widget) {
        if (widget == null)
            return null;
        ObjectController objectController = widgets2controllers.get (widget);
        if (objectController != null)
            return objectController;
        return findObjectController (widget.getParentWidget ());
    }

    public void userSelectionSuggested (Set<ObjectController> objectControllers) {
        setSelectedObjects (objectControllers);
    }

    private class ObjectSelectAction extends SelectAction {
        public void doSelect (Widget widget, Point localLocation) {
            ObjectController objectController = findObjectController (widget);
            Set<ObjectController> set;

            if (objectController != null) {
                if (getSelectedObjects ().contains (objectController))
                    return;
                set = Collections.singleton (objectController);
            } else
                set = Collections.emptySet ();
            userSelectionSuggested (set);
        }
    }

    private class ObjectHoverAction extends MouseHoverAction {

        protected void widgetHovered (Widget widget) {
            if (ObjectScene.this == widget)
                widget = null;
            setHoveredObject (findObjectController (widget));
        }

    }

}
