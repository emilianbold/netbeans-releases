/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.undo.*;

import org.openide.nodes.Node;

import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FormModelEvent extends EventObject
{
    public static final int FORM_LOADED = 1;
    public static final int FORM_TO_BE_SAVED = 2;
    public static final int FORM_TO_BE_CLOSED = 3;
    public static final int CONTAINER_LAYOUT_EXCHANGED = 4;
    public static final int CONTAINER_LAYOUT_CHANGED = 5;
    public static final int COMPONENT_LAYOUT_CHANGED = 6;
    public static final int COMPONENT_ADDED = 7;
    public static final int COMPONENT_REMOVED = 8;
    public static final int COMPONENTS_REORDERED = 9;
    public static final int COMPONENT_PROPERTY_CHANGED = 10;
    public static final int SYNTHETIC_PROPERTY_CHANGED = 11;
    public static final int EVENT_HANDLER_ADDED = 12;
    public static final int EVENT_HANDLER_REMOVED = 13;
    public static final int EVENT_HANDLER_RENAMED = 14;
    public static final int OTHER_CHANGE = 15;

    private boolean createdDeleted;
    private RADComponent component;
    private ComponentContainer container;
    private LayoutConstraints constraints;
    private int componentIndex = -1;
    private int[] reordering;
    private Object codeUndoRedoStart;
    private Object codeUndoRedoEnd;
    private String propertyName;
    private Object oldPropertyValue;
    private Object newPropertyValue;
    private LayoutSupportDelegate oldLayoutSupport;
    private LayoutSupportDelegate newLayoutSupport;
    private Event componentEvent;

    private int changeType;

    private UndoableEdit undoableEdit;

    // -----------

    FormModelEvent(FormModel source, int changeType) {
        super(source);
        this.changeType = changeType;
    }

    void setProperty(String propName, Object oldValue, Object newValue) {
        propertyName = propName;
        oldPropertyValue = oldValue;
        newPropertyValue = newValue;
    }

    void setComponentAndContainer(RADComponent metacomp,
                                  ComponentContainer metacont)
    {
        component = metacomp;
        container = metacont != null ? metacont : deriveContainer(metacomp);
    }

    void setLayout(RADVisualContainer metacont,
                   LayoutSupportDelegate oldLayoutSupp,
                   LayoutSupportDelegate newLayoutSupp)
    {
        component = metacont;
        container = metacont;
        oldLayoutSupport = oldLayoutSupp;
        newLayoutSupport = newLayoutSupp;
    }

    void setReordering(int[] perm) {
        reordering = perm;
    }

    void setAddData(RADComponent metacomp,
                    ComponentContainer metacont,
                    boolean addedNew)
    {
        setComponentAndContainer(metacomp, metacont);
        createdDeleted = addedNew;

        if (component instanceof RADVisualComponent
            && container instanceof RADVisualContainer)
        {
            componentIndex = container.getIndexOf(component);
            if (componentIndex >= 0) {
                LayoutSupportManager laysup =
                    ((RADVisualContainer)container).getLayoutSupport();
                constraints = laysup.getConstraints(componentIndex);
            }
        }
    }

    void setRemoveData(RADComponent metacomp,
                       ComponentContainer metacont,
                       int index,
                       boolean removedFromModel,
                       Object codeStructureMark1,
                       Object codeStructureMark2)
    {
        component = metacomp;
        container = metacont;
        componentIndex = index;
        codeUndoRedoStart = codeStructureMark1;
        codeUndoRedoEnd = codeStructureMark2;
        createdDeleted = removedFromModel;

        if (metacomp instanceof RADVisualComponent
            && metacont instanceof RADVisualContainer)
        {
            LayoutSupportManager laysup =
                ((RADVisualContainer)metacont).getLayoutSupport();
            constraints =
                laysup.getStoredConstraints((RADVisualComponent)metacomp);
        }
    }

    void setEvent(Event event,
                  EventHandler handler,
                  String bodyText,
                  boolean createdNew)
    {
        component = event.getComponent();
        componentEvent = event;
        propertyName = handler.getName();
        newPropertyValue = bodyText;
        createdDeleted = createdNew;
    }

    void setEvent(EventHandler handler, String oldName) {
        propertyName = handler.getName();
        oldPropertyValue = oldName;
        newPropertyValue = handler.getName();
    }

    void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    private static ComponentContainer deriveContainer(RADComponent comp) {
        if (comp == null)
            return null;
        if (comp.getParentComponent() instanceof ComponentContainer)
            return (ComponentContainer) comp.getParentComponent();
        else if (comp.getParentComponent() == null)
            return comp.getFormModel().getModelContainer();
        return null;
    }

    // -------

    public final FormModel getFormModel() {
        return (FormModel) getSource();
    }

    public final int getChangeType() {
        return changeType;
    }

    public final boolean isModifying() {
        return changeType != FORM_LOADED
               && changeType != FORM_TO_BE_SAVED
               && changeType != FORM_TO_BE_CLOSED;
    }

    public final ComponentContainer getContainer() {
        return container;
    }

    public final RADComponent getComponent() {
        return component;
    }

    public final LayoutConstraints getComponentLayoutConstraints() {
        return constraints;
    }

    public final boolean getCreatedDeleted() {
        return createdDeleted;
    }

    public final String getPropertyName() {
        return propertyName;
    }

    public final RADProperty getComponentProperty() {
        if (component == null || propertyName == null)
            return null;

        FormProperty prop = component.getPropertyByName(propertyName);
        return prop instanceof RADProperty ? (RADProperty) prop : null;
    }

    public final Object getOldPropertyValue() {
        return oldPropertyValue instanceof FormProperty.ValueWithEditor ?
                 ((FormProperty.ValueWithEditor)oldPropertyValue).getValue() :
                 oldPropertyValue;
    }

    public final Object getNewPropertyValue() {
        return newPropertyValue instanceof FormProperty.ValueWithEditor ?
                 ((FormProperty.ValueWithEditor)newPropertyValue).getValue() :
                 newPropertyValue;
    }

    public final LayoutSupportDelegate getOldLayoutSupport() {
        return oldLayoutSupport;
    }

    public final LayoutSupportDelegate getNewLayoutSupport() {
        return newLayoutSupport;
    }

    public final int[] getReordering() {
        return reordering;
    }

    public final Event getComponentEvent() {
        return componentEvent;
    }

    public final String getEventHandlerName() {
        return propertyName;
    }

    public final String getEventHandlerText() {
        return changeType == EVENT_HANDLER_ADDED
                       || changeType == EVENT_HANDLER_REMOVED ?
               (String) newPropertyValue : null;
    }

    // ----------

    UndoableEdit getUndoableEdit() {
        if (undoableEdit == null)
            undoableEdit = new FormUndoableEdit();
        return undoableEdit;
    }

    // ----------

    private class FormUndoableEdit extends AbstractUndoableEdit {
        public void undo() throws CannotUndoException {
            super.undo();

            // turn off undo/redo monitoring in FormModel while undoing!
            boolean undoRedoOn = getFormModel().isUndoRedoRecording();
            if (undoRedoOn)
                getFormModel().setUndoRedoRecording(false);

            switch(changeType) {
                case CONTAINER_LAYOUT_EXCHANGED:
                    FormModel.t("UNDO: container layout change"); // NOI18N
                    undoContainerLayoutExchange();
                    break;
                case CONTAINER_LAYOUT_CHANGED:
                    FormModel.t("UNDO: container layout property change"); // NOI18N
                    undoContainerLayoutChange();
                    break;
                case COMPONENT_LAYOUT_CHANGED:
                    FormModel.t("UNDO: component layout constraints change"); // NOI18N
                    undoComponentLayoutChange();
                    break;
                case COMPONENTS_REORDERED:
                    FormModel.t("UNDO: components reorder"); // NOI18N
                    undoComponentsReorder();
                    break;
                case COMPONENT_ADDED:
                    FormModel.t("UNDO: component addition"); // NOI18N
                    undoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    FormModel.t("UNDO: component removal"); // NOI18N
                    undoComponentRemoval();
                    break;
                case COMPONENT_PROPERTY_CHANGED:
                    FormModel.t("UNDO: component property change"); // NOI18N
                    undoComponentPropertyChange();
                    break;
                case SYNTHETIC_PROPERTY_CHANGED:
                    FormModel.t("UNDO: synthetic property change"); // NOI18N
                    undoSyntheticPropertyChange();
                    break;
                case EVENT_HANDLER_ADDED:
                    FormModel.t("UNDO: event handler addition"); // NOI18N
                    undoEventHandlerAddition();
                    break;
                case EVENT_HANDLER_REMOVED:
                    FormModel.t("UNDO: event handler removal"); // NOI18N
                    undoEventHandlerRemoval();
                    break;
                case EVENT_HANDLER_RENAMED:
                    FormModel.t("UNDO: event handler renaming"); // NOI18N
                    undoEventHandlerRenaming();
                    break;

                default: FormModel.t("UNDO: "+changeType); // NOI18N
                         break;
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                getFormModel().setUndoRedoRecording(true);
        }

        public void redo() throws CannotRedoException {
            super.redo();

            // turn off undo/redo monitoring in FormModel while redoing!
            boolean undoRedoOn = getFormModel().isUndoRedoRecording();
            if (undoRedoOn)
                getFormModel().setUndoRedoRecording(false);

            switch(changeType) {
                case CONTAINER_LAYOUT_EXCHANGED:
                    FormModel.t("REDO: container layout change"); // NOI18N
                    redoContainerLayoutExchange();
                    break;
                case CONTAINER_LAYOUT_CHANGED:
                    FormModel.t("REDO: container layout property change"); // NOI18N
                    redoContainerLayoutChange();
                    break;
                case COMPONENT_LAYOUT_CHANGED:
                    FormModel.t("REDO: component layout constraints change"); // NOI18N
                    redoComponentLayoutChange();
                    break;
                case COMPONENTS_REORDERED:
                    FormModel.t("REDO: components reorder"); // NOI18N
                    redoComponentsReorder();
                    break;
                case COMPONENT_ADDED:
                    FormModel.t("REDO: component addition"); // NOI18N
                    redoComponentAddition();
                    break;
                case COMPONENT_REMOVED:
                    FormModel.t("REDO: component removal"); // NOI18N
                    redoComponentRemoval();
                    break;
                case COMPONENT_PROPERTY_CHANGED:
                    FormModel.t("REDO: component property change"); // NOI18N
                    redoComponentPropertyChange();
                    break;
                case SYNTHETIC_PROPERTY_CHANGED:
                    FormModel.t("REDO: synthetic property change"); // NOI18N
                    redoSyntheticPropertyChange();
                    break;
                case EVENT_HANDLER_ADDED:
                    FormModel.t("REDO: event handler addition"); // NOI18N
                    redoEventHandlerAddition();
                    break;
                case EVENT_HANDLER_REMOVED:
                    FormModel.t("REDO: event handler removal"); // NOI18N
                    redoEventHandlerRemoval();
                    break;
                case EVENT_HANDLER_RENAMED:
                    FormModel.t("REDO: event handler renaming"); // NOI18N
                    redoEventHandlerRenaming();
                    break;

                default: FormModel.t("REDO: "+changeType); // NOI18N
                         break;
            }

            if (undoRedoOn) // turn on undo/redo monitoring again
                getFormModel().setUndoRedoRecording(true);
        }

        public String getUndoPresentationName() {
            return ""; // NOI18N
        }
        public String getRedoPresentationName() {
            return ""; // NOI18N
        }

        public void die() {
            // it's very important to release undo changes from CodeStructure
            if (codeUndoRedoStart != null && codeUndoRedoEnd != null)
                getFormModel().getCodeStructure().releaseUndoableChanges(
                                       codeUndoRedoStart, codeUndoRedoEnd);
        }

        // -------------

        private void undoContainerLayoutExchange() {
            try {
                getFormModel().setContainerLayout((RADVisualContainer)container,
                                                  oldLayoutSupport,
                                                  null);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void redoContainerLayoutExchange() {
            try {
                getFormModel().setContainerLayout((RADVisualContainer)container,
                                                  newLayoutSupport,
                                                  null);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void undoContainerLayoutChange() {
            LayoutSupportManager laysup =
                component instanceof RADVisualContainer ?
                    ((RADVisualContainer)component).getLayoutSupport() : null;
            if (laysup != null) {
                Node.Property prop = laysup.getLayoutProperty(propertyName);
                if (prop != null)
                    try {
                        prop.setValue(oldPropertyValue);
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void redoContainerLayoutChange() {
            LayoutSupportManager laysup =
                component instanceof RADVisualContainer ?
                    ((RADVisualContainer)component).getLayoutSupport() : null;
            if (laysup != null) {
                Node.Property prop = laysup.getLayoutProperty(propertyName);
                if (prop != null)
                    try {
                        prop.setValue(newPropertyValue);
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void undoComponentLayoutChange() {
            if (component instanceof RADVisualComponent) {
                ((RADVisualComponent)component).getConstraintsProperties();
                FormProperty prop = component.getPropertyByName(propertyName);
                if (prop != null)
                    try {
                        prop.setValue(oldPropertyValue);
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void redoComponentLayoutChange() {
            if (component instanceof RADVisualComponent) {
                ((RADVisualComponent)component).getConstraintsProperties();
                FormProperty prop = component.getPropertyByName(propertyName);
                if (prop != null)
                    try {
                        prop.setValue(newPropertyValue);
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
            }
        }

        private void undoComponentAddition() {
            redoComponentRemoval();
        }

        private void redoComponentAddition() {
            undoComponentRemoval();
        }

        private void undoComponentRemoval() {
            if (codeUndoRedoStart != null // is null when called from redoComponentAddition()
                    && !getFormModel().getCodeStructure().undoToMark(
                                                   codeUndoRedoStart))
                return;

            RADComponent[] currentSubComps = container.getSubBeans();
            RADComponent[] undoneSubComps =
                new RADComponent[currentSubComps.length+1];

            if (componentIndex < 0)
                componentIndex = currentSubComps.length;

            for (int i=0,j=0; j < undoneSubComps.length; i++,j++) {
                if (i == componentIndex) {
                    undoneSubComps[j] = component;
                    if (i == currentSubComps.length)
                        break;
                    j++;
                }
                undoneSubComps[j] = currentSubComps[i];
            }

            container.initSubComponents(undoneSubComps);

            if (container instanceof RADVisualContainer
                && component instanceof RADVisualComponent)
            {
                LayoutSupportManager layoutSupport =
                    ((RADVisualContainer)container).getLayoutSupport();
                layoutSupport.addComponents(
                    new RADVisualComponent[] { (RADVisualComponent) component },
                    new LayoutConstraints[] { constraints },
                    componentIndex);
            }

            if (createdDeleted)
                FormModel.setInModelRecursively(component, true);

            getFormModel().fireComponentAdded(component, createdDeleted);
        }

        private void redoComponentRemoval() {
            if (createdDeleted)
                getFormModel().removeComponent(component);
            else
                getFormModel().removeComponentFromContainer(component);

            if (codeUndoRedoEnd != null)
                getFormModel().getCodeStructure().redoToMark(codeUndoRedoEnd);
        }

        private void undoComponentsReorder() {
            if (container != null && reordering != null) {
                int[] revPerm = new int[reordering.length];
                for (int i=0; i < reordering.length; i++)
                    revPerm[reordering[i]] = i;

                container.reorderSubComponents(revPerm);
                getFormModel().fireComponentsReordered(container, revPerm);
            }
        }

        private void redoComponentsReorder() {
            if (container != null && reordering != null) {
                container.reorderSubComponents(reordering);
                getFormModel().fireComponentsReordered(container, reordering);
            }
        }

        private void undoComponentPropertyChange() {
            FormProperty prop = component.getPropertyByName(propertyName);
            if (prop != null)
                try {
                    prop.setValue(oldPropertyValue);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
        }

        private void redoComponentPropertyChange() {
            FormProperty prop = component.getPropertyByName(propertyName);
            if (prop != null)
                try {
                    prop.setValue(newPropertyValue);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
        }

        private void undoSyntheticPropertyChange() {
            Node.Property[] props = component.getSyntheticProperties();
            for (int i=0; i < props.length; i++) {
                if (props[i].getName().equals(propertyName)) {
                    try {
                        props[i].setValue(oldPropertyValue);
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void redoSyntheticPropertyChange() {
            Node.Property[] props = component.getSyntheticProperties();
            for (int i=0; i < props.length; i++) {
                if (props[i].getName().equals(propertyName)) {
                    try {
                        props[i].setValue(newPropertyValue);
                    }
                    catch (Exception ex) { // should not happen
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void undoEventHandlerAddition() {
            FormEventHandlers handlers = getFormModel().getFormEventHandlers();
            newPropertyValue = handlers.getEventHandler(propertyName)
                                                            .getHandlerText();

            handlers.removeEventHandler(componentEvent, propertyName);

            // fire property change on node explicitly to update event in
            // Component Inspector
            component.getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + componentEvent.getName(), null, null);
        }

        private void redoEventHandlerAddition() {
            getFormModel().getFormEventHandlers().addEventHandler(
                componentEvent, propertyName, (String) newPropertyValue);

            // fire property change on node explicitly to update event in
            // Component Inspector
            component.getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + componentEvent.getName(), null, null);
        }

        private void undoEventHandlerRemoval() {
            getFormModel().getFormEventHandlers().addEventHandler(
                componentEvent, propertyName, (String) newPropertyValue);

            // fire property change on node explicitly to update event in
            // Component Inspector
            component.getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + componentEvent.getName(), null, null);
        }

        private void redoEventHandlerRemoval() {
            FormEventHandlers handlers = getFormModel().getFormEventHandlers();
            newPropertyValue = handlers.getEventHandler(propertyName)
                                                            .getHandlerText();

            handlers.removeEventHandler(componentEvent, propertyName);

            // fire property change on node explicitly to update event in
            // Component Inspector
            component.getNodeReference().firePropertyChangeHelper(
                FormEditor.EVENT_PREFIX + componentEvent.getName(), null, null);
        }

        private void undoEventHandlerRenaming() {
            FormEventHandlers handlers = getFormModel().getFormEventHandlers();
            handlers.renameEventHandler((String) newPropertyValue,
                                        (String) oldPropertyValue);

            // fire property change on nodes explicitly to update events in
            // Component Inspector
            java.util.Iterator events =
                handlers.getEventHandler((String)oldPropertyValue)
                    .getAttachedEvents().iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                event.getComponent().getNodeReference()
                    .firePropertyChangeHelper(
                        FormEditor.EVENT_PREFIX + event.getName(), null, null);
            }
        }

        private void redoEventHandlerRenaming() {
            FormEventHandlers handlers = getFormModel().getFormEventHandlers();
            handlers.renameEventHandler((String) oldPropertyValue,
                                        (String) newPropertyValue);

            // fire property change on nodes explicitly to update events in
            // Component Inspector
            java.util.Iterator events =
                handlers.getEventHandler((String)newPropertyValue)
                    .getAttachedEvents().iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                event.getComponent().getNodeReference()
                    .firePropertyChangeHelper(
                        FormEditor.EVENT_PREFIX + event.getName(), null, null);
            }
        }
    }
}
