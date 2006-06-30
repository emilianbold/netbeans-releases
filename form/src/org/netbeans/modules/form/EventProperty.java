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

package org.netbeans.modules.form;

import java.util.*;
import java.beans.*;
import java.awt.event.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;

/**
 * Property implementation class for events of metacomponents.
 * (Events are treated as properties on Events tab of Component Inspector.)
 *
 * @author Tomas Pavek
 */

class EventProperty extends PropertySupport.ReadWrite {

    private static String NO_EVENT;

    private static boolean somethingChanged; // flag for "postSetAction" relevance
    private static boolean invalidValueTried; // flag for "postSetAction" relevance

    private Event event;

    private String selectedEventHandler;

    EventProperty(Event event, String eventId) {
        super(eventId,
              String.class,
              event.getListenerMethod().getName(),
              event.getListenerMethod().getName());
        this.event = event;
        setShortDescription(
            event.getEventSetDescriptor().getListenerType().getName());
    }

    Event getEvent() {
        return event;
    }

    private FormEvents getFormEvents() {
        return event.getComponent().getFormModel().getFormEvents();
    }

    private java.lang.reflect.Method getListenerMethod() {
        return event.getListenerMethod();
    }

    String[] getEventHandlers() {
        return event.getEventHandlers();
    }

    // -------

    /** Getter for the value of the property. It returns name of the last
     * selected event handler (for property sheet), not the Event object.
     * @return String name of the selected event handler attached to the event
     */
    public Object getValue() {
        if (selectedEventHandler == null && event.hasEventHandlers())
            selectedEventHandler = (String) event.getEventHandlerList().get(0);
        return selectedEventHandler;
    }

    /** Setter for the value of the property. It accepts String (for adding
     * new or renaming the last selected event handler), or Change object
     * (describing multiple changes in event handlers), or null (to refresh
     * property sheet due to a change in handlers made outside).
     */
    public void setValue(Object val) {
        Change change = null;
        String newSelectedHandler = null;

        if (val instanceof Change) {
            change = (Change) val;
        }
        else if (val instanceof String) {
            String[] handlers = getEventHandlers();
            if (handlers.length > 0) {
                // there are already some handlers attached
                String current = selectedEventHandler != null ?
                                 selectedEventHandler : handlers[0];

                if ("".equals(val)) { // NOI18N
                    // empty String => remove current handler
                    change = new Change();
                    change.getRemoved().add(current);
                    for (int i=0; i < handlers.length; i++)
                        if (!handlers[i].equals(current)) {
                            newSelectedHandler = handlers[i];
                            break;
                        }
                }
                else { // non-empty String => rename current handler
                    newSelectedHandler = (String) val;

                    boolean ignore = false;
                    for (int i=0; i < handlers.length; i++)
                        if (handlers[i].equals(val)) { // not a new name
                            ignore = true;
                            break;
                        }

                    if (!ignore) { // do rename
                        change = new Change();
                        change.getRenamedNewNames().add(val);
                        change.getRenamedOldNames().add(current);
                    }
                }
            }
            else { // no handlers yet, add a new one
                if (!"".equals(val)) {
                    change = new Change();
                    change.getAdded().add((String)val);
                    newSelectedHandler = (String) val;
                }
            }
        }
        else if (val == null) {
            if (selectedEventHandler == null)
                return;
        }
        else throw new IllegalArgumentException();

        if (change != null) {
            somethingChanged = true; // something was changed

            FormEvents formEvents = getFormEvents();

            if (change.hasRemoved()) // some handlers to remove
                for (Iterator it=change.getRemoved().iterator(); it.hasNext(); )
                    formEvents.detachEvent(event, (String) it.next());

            if (change.hasRenamed()) // some handlers to rename
                for (int i=0; i < change.getRenamedOldNames().size(); i++) {
                    String oldName = (String)change.getRenamedOldNames().get(i);
                    String newName = (String)change.getRenamedNewNames().get(i);

                    try {
                        formEvents.renameEventHandler(oldName, newName);

                        // hack: update all properties using the renamed handler
                        Event[] events = formEvents.getEventsForHandler(newName);
                        for (int j=0 ; j < events.length; j++) {
                            Node.Property prop = events[j].getComponent()
                                                  .getPropertyByName(getName());
                            if (prop != null && prop != this) {
                                try {
                                    if (oldName.equals(prop.getValue()))
                                        prop.setValue(newName);
                                }
                                catch (Exception ex) { // should not happen
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                    catch (IllegalArgumentException ex) { // name already used
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        newSelectedHandler = null;
                    }
                }

            if (change.hasAdded()) // some handlers to add
                for (Iterator it=change.getAdded().iterator(); it.hasNext(); ) {
                    try {
                        formEvents.attachEvent(event, (String) it.next(), null);
                    }
                    catch (IllegalArgumentException ex) { // name already used
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        newSelectedHandler = null;
                    }
                }
        }

        selectedEventHandler = newSelectedHandler;

        RADComponentNode node = event.getComponent().getNodeReference();
        if (node != null)
            node.firePropertyChangeHelper(getName(), null, null);
    }

    public Object getValue(String key) {
        if ("canEditAsText".equals(key)) // NOI18N
            return Boolean.TRUE;

        if ("initialEditValue".equals(key)) { // NOI18N
            somethingChanged = false; // entering edit mode
            invalidValueTried = false;
            return selectedEventHandler != null ? null :
                getFormEvents().findFreeHandlerName(event, event.getComponent());
        }

        if ("postSetAction".equals(key)) // NOI18N
            return new javax.swing.AbstractAction() {
                public void actionPerformed(ActionEvent ev) {
                    // if Enter was pressed without echange or existing handler
                    // chosen, switch to editor
                    if (!somethingChanged && !invalidValueTried)
                        getFormEvents().attachEvent(event,
                                                    selectedEventHandler,
                                                    null);
                }
            };

        return super.getValue(key);
    }


//    public String getDisplayName() {
//        String displayName = super.getDisplayName();
//        if (selectedEventHandler != null)
//            displayName = "<html><b>" + displayName + "</b>"; // NOI18N
//        return displayName;
//    }
//
    public boolean canWrite() {
        return !isReadOnly();
    }

    private boolean isReadOnly() {
        return event.getComponent().isReadOnly();
    }

    /** Returns property editor for this property.
     * @return the property editor for adding/removing/renaming event handlers
     */
    public PropertyEditor getPropertyEditor() {
        return new EventEditor();
    }

    // --------

    /** Helper class describing changes in event handlers attached to an event.
     */
    static class Change {
        boolean hasAdded() {
            return added != null && added.size() > 0;
        }
        boolean hasRemoved() {
            return removed != null && removed.size() > 0;
        }
        boolean hasRenamed() {
            return renamedOldName != null && renamedOldName.size() > 0;
        }
        List getAdded() {
            if (added == null)
                added = new ArrayList();
            return added;
        }
        List getRemoved() {
            if (removed == null)
                removed = new ArrayList();
            return removed;
        }
        List getRenamedOldNames() {
            if (renamedOldName == null)
                renamedOldName = new ArrayList();
            return renamedOldName;
        }
        List getRenamedNewNames() {
            if (renamedNewName == null)
                renamedNewName = new ArrayList();
            return renamedNewName;
        }
        private List added;
        private List removed;
        private List renamedOldName;
        private List renamedNewName;
    }

    // --------

    private class EventEditor extends PropertyEditorSupport {

        public String getAsText() {
            if (this.getValue() == null) {
                if (NO_EVENT == null)
                    NO_EVENT = FormUtils.getBundleString("CTL_NoEvent"); // NOI18N
                return NO_EVENT;
            }
            return this.getValue().toString();
        }

        public void setAsText(String txt) {
            if (!"".equals(txt) && !Utilities.isJavaIdentifier(txt)) { // NOI18N
                // invalid handler name entered
                invalidValueTried = true;
                IllegalArgumentException iae = new IllegalArgumentException();
                String annotation = FormUtils.getFormattedBundleString(
                                        "FMT_MSG_InvalidJavaIdentifier", // NOI18N
                                        new Object [] { txt } );
                ErrorManager.getDefault().annotate(
                    iae, ErrorManager.ERROR, "Not a java identifier", // NOI18N
                    annotation, null, null);
                throw iae;
            }
            if ("".equals(txt) && (this.getValue() == null)) {
                // empty string entered when no event handler exist
                invalidValueTried = true;
                IllegalArgumentException iae = new IllegalArgumentException();
                String emptyStringTxt = FormUtils.getBundleString("FMT_MSG_EmptyString"); // NOI18N
                String annotation = FormUtils.getFormattedBundleString(
                                        "FMT_MSG_InvalidJavaIdentifier", // NOI18N
                                        new Object [] { emptyStringTxt } );
                ErrorManager.getDefault().annotate(
                    iae, ErrorManager.ERROR, "Not a java identifier", // NOI18N
                    annotation, null, null);
                throw iae;
            }
            invalidValueTried = false;
            this.setValue(txt);
        }

        public String[] getTags() {
            String[] handlers = getEventHandlers();
            return handlers.length > 1 ? handlers : null;
        }

        public boolean supportsCustomEditor() {
            return isReadOnly() ? false : true;
        }

        public java.awt.Component getCustomEditor() {
            if (isReadOnly())
                return null;

            final EventCustomEditor ed = new EventCustomEditor(EventProperty.this);
            DialogDescriptor dd = new DialogDescriptor(
                ed,
                FormUtils.getFormattedBundleString(
                    "FMT_MSG_HandlersFor", // NOI18N
                    new Object [] { getListenerMethod().getName() }),
                true,
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                            ed.doChanges();
                        }
                    }
                });

            return DialogDisplayer.getDefault().createDialog(dd);
        }
    }
}
