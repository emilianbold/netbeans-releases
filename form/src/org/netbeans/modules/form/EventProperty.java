/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import java.beans.*;
import java.awt.event.*;
import java.lang.reflect.Method;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.util.Utilities;

/** 
 * Property implementation class for events of metacomponents (RADComponent).
 * (Events are treated as properties on Events tab of Component Inspector.)
 *
 * @author Tomas Pavek
 */

class EventProperty extends PropertySupport.ReadWrite {

    private static String NO_EVENT;

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
        return getComponent().getFormModel().getFormEvents();
    }

    private RADComponent getComponent() {
        return event.getComponent();
    }

    private Method getListenerMethod() {
        return event.getListenerMethod();
    }

    String[] getEventHandlers() {
        return event.getEventHandlers();
    }

    // -------

    /** Getter for the value of the property. It returns name of the last
     * selected event handler (for property sheet), not the event.
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
                change = new Change();
                change.getAdded().add((String)val);
                newSelectedHandler = (String) val;
            }
        }
        else if (val == null) {
            if (selectedEventHandler == null)
                return;
        }
        else throw new IllegalArgumentException();

        if (change != null) {
            FormEvents formEvents = getFormEvents();

            if (change.hasRemoved()) // some handlers to remove
                for (Iterator it=change.getRemoved().iterator(); it.hasNext(); )
                    formEvents.detachEvent(event, (String) it.next());

            if (change.hasRenamed()) // some handlers to rename
                for (int i=0; i < change.getRenamedOldNames().size(); i++) {
                    String oldName = (String)change.getRenamedOldNames().get(i);
                    String newName = (String)change.getRenamedNewNames().get(i);

                    if (!Utilities.isJavaIdentifier(newName))
                        continue; // invalid name (checked by EventCustomEditor)

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
                    String handlerName = (String) it.next();

                    if (!Utilities.isJavaIdentifier(handlerName)) { // invalid name
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                FormUtils.getFormattedBundleString(
                                    "FMT_MSG_InvalidJavaIdentifier", // NOI18N
                                    new Object [] {handlerName} ),
                                NotifyDescriptor.ERROR_MESSAGE));
                        continue;
                    }

                    try {
                        formEvents.attachEvent(event, handlerName, null);
                    }
                    catch (IllegalArgumentException ex) { // name already used
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
                        newSelectedHandler = null;
                    }
                }
        }

        selectedEventHandler = newSelectedHandler;

        RADComponentNode node = getComponent().getNodeReference();
        if (node != null)
            node.firePropertyChangeHelper(getName(), null, null);
    }

    public String getDisplayName() {
        String displayName = super.getDisplayName();
        if (selectedEventHandler != null)
            displayName = "<html><b>" + displayName + "</b>"; // NOI18N
        return displayName;
    }

    public boolean canWrite() {
        return !isReadOnly();
    }

    private boolean isReadOnly() {
        return getComponent().isReadOnly();
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

    /** Property editor class for EventProperty. It provides in-place editor
     * and custom editor for adding/removing/renaming event handlers.
     */
    private class EventEditor extends PropertyEditorSupport
                              implements EnhancedPropertyEditor
    {
        ActionListener comboSelectListener = null;
        FocusListener comboEditFocusListener = null;

        EventComboBox eventCombo;

        public String getAsText() {
            if (this.getValue() == null) {
                if (NO_EVENT == null)
                    NO_EVENT = FormUtils.getBundleString("CTL_NoEvent"); // NOI18N
                return NO_EVENT;
            }
            else return this.getValue().toString();
        }

        public void setAsText(String selected) {
            this.setValue(selected);
        }

        public boolean supportsEditingTaggedValues() {
            return false;
        }

        /**
         * @return custom property editor to be shown inside
         * the property sheet.
         */
        public java.awt.Component getInPlaceCustomEditor() {
            eventCombo = new EventComboBox();
            eventCombo.setEditable(!isReadOnly());

            String[] handlers = getEventHandlers();
            if (handlers.length == 0) {
                String newName = getFormEvents().findFreeHandlerName(
                                                   getEvent(), getComponent());
                eventCombo.getEditor().setItem(newName);
            }
            else {
                for (int i=0; i < handlers.length; i++)
                    eventCombo.addItem(handlers[i]);
                if (selectedEventHandler != null)
                    eventCombo.setSelectedItem(selectedEventHandler);
            }

            // listening to combobox selection change
            // (we remember the listener in a field so we can remove it)
            if (comboSelectListener == null)
                comboSelectListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int i;
                        if (!event.hasEventHandlers()
                              || (i = eventCombo.getSelectedIndex()) < 0)
                            return;

                        eventCombo.getEditor().getEditorComponent()
                            .removeFocusListener(comboEditFocusListener);

                        String selected = (String) eventCombo.getItemAt(i);
                        EventEditor.this.setValue(selected);

                        // redundant operation - just switches to the editor
                        getFormEvents().attachEvent(getEvent(),
                                                    selected,
                                                    null);
                    }
                };
            eventCombo.addActionListener(comboSelectListener);

            if (isReadOnly())
                return eventCombo;

            // listening to combobox's editor focus lost
            // (we remember the listener in a field so we can remove it
            if (comboEditFocusListener == null)
                comboEditFocusListener = new FocusAdapter() {
                    public void focusLost(FocusEvent evt) {
                        eventCombo.removeActionListener(comboSelectListener);
                        EventEditor.this.setValue(selectedEventHandler);
                    }
                    public void focusGained(FocusEvent evt) {
                        eventCombo.getEditor().selectAll();
                    }
                };
            eventCombo.getEditor().getEditorComponent().addFocusListener(
                                                   comboEditFocusListener);

            // listening to Esc key pressed in combobox's editor
            eventCombo.getEditor().getEditorComponent()
                                       .addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        eventCombo.removeActionListener(comboSelectListener);
                        eventCombo.getEditor().getEditorComponent()
                            .removeFocusListener(comboEditFocusListener);

                        EventEditor.this.setValue(selectedEventHandler);
                    }
                    else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        eventCombo.removeActionListener(comboSelectListener);
                        eventCombo.getEditor().getEditorComponent()
                            .removeFocusListener(comboEditFocusListener);

                        String selected = (String) eventCombo.getEditor().getItem();
                        EventEditor.this.setValue(selected);

                        if (selected == null || "".equals(selected)) { // NOI18N
                            if (selectedEventHandler != null)
                                EventEditor.this.setValue(selectedEventHandler);
                        }
                        else if (selectedEventHandler != null)
                            // redundant operation - just switches to the editor
                            getFormEvents().attachEvent(getEvent(),
                                                        selectedEventHandler,
                                                        null);
                    }
                }
            });

            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    eventCombo.getEditor().getEditorComponent().requestFocus();
                }
            });

            return eventCombo;
        }

        /** @return true if this PropertyEditor provides a enhanced in-place
         * custom property editor, false otherwise
         */
        public boolean hasInPlaceCustomEditor() {
            return !isReadOnly() || event.hasEventHandlers();
        }

        public boolean supportsCustomEditor() {
            return true;
        }

        /** @return the custom property editor (a standalone panel) for
         * editing event handlers attached to the event.
         */
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

    // --------

    private static class EventComboBox extends javax.swing.JComboBox {
        public void addKeyListener(KeyListener l) {
            super.addKeyListener(l);
            getEditor().getEditorComponent().addKeyListener(l);
        }
        public void removeKeyListener(KeyListener l) {
            super.removeKeyListener(l);
            getEditor().getEditorComponent().removeKeyListener(l);
        }
        public void addFocusListener(FocusListener l) {
            super.addFocusListener(l);
            if (getEditor() != null)
                getEditor().getEditorComponent().addFocusListener(l);
        }
        public void removeFocusListener(FocusListener l) {
            super.removeFocusListener(l);
            if (getEditor() != null)
                getEditor().getEditorComponent().removeFocusListener(l);
        }
    }
}
