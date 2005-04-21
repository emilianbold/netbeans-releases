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
/*
 * Boolean3WayEditor.java
 *
 * Created on April 16, 2003, 7:05 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.util.*;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.*;

import java.beans.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;


/** A property editor for Boolean values which can also be null to
 *  indicate the editor represents multiple conflicting values.
 *
 * @author  Tim Boudreau
 */
final class Boolean3WayEditor implements ExPropertyEditor, InplaceEditor.Factory {
    Boolean v = null;

    /** Utility field holding list of PropertyChangeListeners. */
    private transient List propertyChangeListenerList;
    private Boolean3Inplace renderer = null;

    public Boolean3WayEditor() {
    }

    public String getAsText() {
        if (v == null) {
            return NbBundle.getMessage(Boolean3WayEditor.class, "CTL_Different_Values");
        } else if (Boolean.TRUE.equals(v)) {
            return Boolean.TRUE.toString(); //XXX use hinting
        } else {
            return Boolean.FALSE.toString(); //XXX use hinting
        }
    }

    public java.awt.Component getCustomEditor() {
        return null;
    }

    public String getJavaInitializationString() {
        if (v == null) {
            return "null"; //NOI18N
        } else if (Boolean.TRUE.equals(v)) {
            return "Boolean.TRUE"; //NOI18N
        } else {
            return "Boolean.FALSE"; //NOI18N
        }
    }

    public String[] getTags() {
        return null;
    }

    public Object getValue() {
        return v;
    }

    public boolean isPaintable() {
        return true;
    }

    public void paintValue(Graphics gfx, Rectangle box) {
        if (renderer == null) {
            renderer = new Boolean3Inplace();
        }

        renderer.setSize(box.width, box.height);
        renderer.doLayout();

        Graphics g = gfx.create(box.x, box.y, box.width, box.height);
        renderer.setOpaque(false);
        renderer.paint(g);
        g.dispose();
    }

    public void setAsText(String text) {
        if (Boolean.TRUE.toString().compareToIgnoreCase(text) == 0) {
            setValue(Boolean.TRUE);
        } else {
            setValue(Boolean.FALSE);
        }
    }

    public void setValue(Object value) {
        if (v != value) {
            v = (Boolean) value;
            firePropertyChange();
        }
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }

    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     *
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListenerList == null) {
            propertyChangeListenerList = new java.util.ArrayList();
        }

        propertyChangeListenerList.add(listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListenerList != null) {
            propertyChangeListenerList.remove(listener);
        }
    }

    /** Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     *
     */
    private void firePropertyChange() {
        List list;

        synchronized (this) {
            if (propertyChangeListenerList == null) {
                return;
            }

            list = (List) ((ArrayList) propertyChangeListenerList).clone();
        }

        PropertyChangeEvent event = new PropertyChangeEvent(this, null, null, null);

        for (int i = 0; i < list.size(); i++) {
            ((PropertyChangeListener) list.get(i)).propertyChange(event);
        }
    }

    /** Implementation of InplaceEditor.Factory to create an inplace editor on demand.
     *   With the current implementation, this will actually never be called, because
     *   edit requests for boolean properties automatically toggle the value.  This may,
     *   however, be desirable for the reimplementation of PropertyPanel. */
    public InplaceEditor getInplaceEditor() {
        return new Boolean3Inplace();
    }

    class Boolean3Inplace extends JCheckBox implements InplaceEditor {
        private PropertyModel propertyModel = null;

        Boolean3Inplace() {
            setModel(new ButtonModel3Way());
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }

        public String getText() {
            return PropUtils.noCheckboxCaption ? "" : NbBundle.getMessage(
                Boolean3WayEditor.class, "CTL_Different_Values"
            ); //NOI18N
        }

        public void clear() {
            propertyModel = null;
        }

        public void connect(PropertyEditor pe, PropertyEnv env) {
            //do nothing
        }

        public javax.swing.JComponent getComponent() {
            return this;
        }

        public javax.swing.KeyStroke[] getKeyStrokes() {
            return null;
        }

        public PropertyEditor getPropertyEditor() {
            return Boolean3WayEditor.this;
        }

        public Object getValue() {
            return Boolean3WayEditor.this.getValue();
        }

        public void reset() {
            //do nothing
        }

        public void setValue(Object o) {
            //do nothing
        }

        public boolean supportsTextEntry() {
            return false;
        }

        public void setPropertyModel(PropertyModel pm) {
            propertyModel = pm;
        }

        public PropertyModel getPropertyModel() {
            return propertyModel;
        }

        public boolean isKnownComponent(Component c) {
            return false;
        }
    }

    private class ButtonModel3Way extends DefaultButtonModel {
        public boolean isPressed() {
            return Boolean3WayEditor.this.v == null;
        }

        public boolean isArmed() {
            return true;
        }

        public boolean isSelected() {
            if (v == null) {
                return true;
            }

            return super.isSelected();
        }
    }
}
